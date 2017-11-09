/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.dbpedia.matchers.instance;

import de.dwslab.T2K.matching.dbpedia.algorithm.PartialMatcher;
import static de.dwslab.T2K.matching.dbpedia.algorithm.PropertyBasedClassRefinementComponent.PAR_PROP_NUM_CANDIDATES;
import de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.tableType;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.Combine;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrixFactory;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.timer.Timer;
import java.util.List;
import java.util.Map;

/**
 *
 * @author domi
 */
public class LinkBasedMatcher extends PartialMatcher<TableRow> {

    public LinkBasedMatcher(Similarities similarities,
            MatchingParameters matchingParameters, Timer rootTimer,
            GoldStandard goldStandard, MatchingLogger logger) {
        super(similarities, matchingParameters, rootTimer, goldStandard, logger);
        this.similarites = similarities;
    }
    
    Similarities similarites;    
    
    private double similarityThreshold;

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }
    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
    
    @Override
    public SimilarityMatrix<TableRow> match(MatchingData data) {
        SimilarityMatrix<TableRow> initialCandidateSimilarity = similarites.getCandidateSimilarity();
        SimilarityMatrix<TableRow> equivalentInstances = new SparseSimilarityMatrixFactory().createSimilarityMatrix(initialCandidateSimilarity.getFirstDimension().size(), initialCandidateSimilarity.getSecondDimension().size());

        for (TableRow tr : initialCandidateSimilarity.getFirstDimension()) {
            for (TableRow tr2 : initialCandidateSimilarity.getSecondDimension()) {
                equivalentInstances.set(tr, tr2, 0.0);
            }
        }

        TableColumn sameAs = null;
        for (TableColumn c : data.getWebtable().getColumns()) {
            if (c.getHeader().toString().contains("owl#sameAs")) {
                sameAs = c;
            }
        }
        if (sameAs != null) {
            Map<Integer, Object> sameAsValues = sameAs.getValues();
            for (TableRow tr : initialCandidateSimilarity.getFirstDimension()) {
                Object link = sameAsValues.get(tr.getRowIndex());
                if (link == null) {
                    continue;
                }
                for (TableRow tr2 : initialCandidateSimilarity.getSecondDimension()) {
                    if (link instanceof List) {
                        List l = (List) link;
                        for (Object u : l) {
                            if (u.toString().startsWith("http://dbpedia.org")) {
                                if (u.toString().equals(tr2.getURI())) {
                                    //System.out.println(u);
                                    equivalentInstances.set(tr, tr2, 1.0);
                                }
                            }
                        }
                    } else {
                        if (link.equals(tr2.getURI())) {
                            //System.out.println(link);
                            equivalentInstances.set(tr, tr2, 1.0);
                        }
                    }
                }
            }
        }
        Combine<TableRow> c = new Combine();
        c.setAggregationType(CombinationType.Sum);
        SimilarityMatrix<TableRow> weightedCandidates = c.match(initialCandidateSimilarity, equivalentInstances);
        weightedCandidates.normalize();

        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("weighted.");
            System.out.println(initialCandidateSimilarity.getOutput());
        }
        weightedCandidates.prune(similarityThreshold);
        
        return weightedCandidates;
    }
}
