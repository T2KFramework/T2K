package de.dwslab.T2K.matching.dbpedia.matchers.schema;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.DocumentOverlapBlocking;
import de.dwslab.T2K.matching.dbpedia.algorithm.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateAbstractMatcher;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowTokenMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowUriMatchingAdapter;
import de.dwslab.T2K.matching.firstline.TextBasedMatcher;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.Combine;
import de.dwslab.T2K.similarity.functions.text.CosineSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrixFactory;
import de.dwslab.T2K.similarity.measures.TextSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.utils.timer.Timer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author domi
 */
public class ContextBasedClassMatcher extends PartialMatcher<Table> {

    public ContextBasedClassMatcher(Similarities similarities, MatchingParameters matchingParameters, Timer rootTimer, GoldStandard goldStandard, MatchingLogger logger) {
        super(similarities, matchingParameters, rootTimer, goldStandard, logger);
    }
    Similarities similarites;
    private MatchingAdapter adapter;
    private double threshold;

    @Override
    public SimilarityMatrix<Table> match(MatchingData data) {
        Timer t = Timer.getNamed("Context Matching", getRootTimer());
        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Context Matching ... ");
        }

        Timer tm = Timer.getNamed("setup context matching", t);
        // run candidate selection

        TextBasedMatcher<Table> candidateSelection = new TextBasedMatcher<>();

        TextSimilarityMeasure<Table> keyMeasure = new TextSimilarityMeasure<>();
        keyMeasure.setSimilarityFunction(new CosineSimilarity());

        candidateSelection.setSimilarityMeasure(keyMeasure);
        candidateSelection.setDocumentTermCount(data.getDocumentClassTermCount());
        candidateSelection.setVectors(data.getClassDocuments());
        //TODO: AS PARAMETER?
        candidateSelection.setThreshold(threshold);
        tm.stop();

        // run matching
        tm = Timer.getNamed("LabelBasedMatcher: candidate selection", t);

        candidateSelection.setParentTimer(tm);
        SimilarityMatrix<Table> initialCandidateSimilarity = null;
        SimilarityMatrix<Table> weightedCandidates = null;
        try {
            initialCandidateSimilarity = candidateSelection.match(Arrays.asList(data.getWebtable()), data.getDbpediaTables(), getAdapter());

            SimilarityMatrix<Table> classWeights = new SparseSimilarityMatrixFactory().createSimilarityMatrix(initialCandidateSimilarity.getFirstDimension().size(), initialCandidateSimilarity.getSecondDimension().size());

            int max = -1;
            for (Table tx : data.getDbpediaTables()) {
                if (tx.getKey() == null) {
                    continue;
                }
                if (tx.getKey().getValues().size() > max) {
                    max = tx.getKey().getValues().size();
                }
            }
            for (Table t1 : initialCandidateSimilarity.getFirstDimension()) {
                for (Table s : initialCandidateSimilarity.getMatches(t1)) {
                    double value;
                    if (s.getKey() == null) {
                        value = 1;
                    } else {
                        value = (double) s.getKey().getValues().size() / (double) max;
                        value = 1 - value;
                    }
                    classWeights.set(t1, s, value);
                }
            }

            Combine<Table> c = new Combine();
            c.setAggregationType(CombinationType.Sum);
            weightedCandidates = c.match(initialCandidateSimilarity, classWeights);

        } catch (Exception ex) {
            Logger.getLogger(CandidateAbstractMatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        t.stop();
        return weightedCandidates;
        //return initialCandidateSimilarity;

    }

    /**
     * @return the adapter
     */
    public MatchingAdapter getAdapter() {
        return adapter;
    }

    /**
     * @param adapter the adapter to set
     */
    public void setAdapter(MatchingAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * @return the threshold
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * @param threshold the threshold to set
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
