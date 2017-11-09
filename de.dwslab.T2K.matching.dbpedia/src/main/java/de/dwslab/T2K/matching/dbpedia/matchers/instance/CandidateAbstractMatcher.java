package de.dwslab.T2K.matching.dbpedia.matchers.instance;

import de.dwslab.T2K.matching.dbpedia.DocumentOverlapBlocking;
import de.dwslab.T2K.matching.dbpedia.algorithm.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowTokenMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowUriMatchingAdapter;
import de.dwslab.T2K.matching.firstline.TextBasedMatcher;
import de.dwslab.T2K.similarity.functions.text.CosineSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
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
public class CandidateAbstractMatcher extends PartialMatcher<TableRow> {

    public CandidateAbstractMatcher(Similarities similarities,
            MatchingParameters matchingParameters, Timer rootTimer,
            GoldStandard goldStandard, MatchingLogger logger) {
        super(similarities, matchingParameters, rootTimer, goldStandard, logger);
        this.similarites = similarities;
    }
    Similarities similarites;
    private double threshold;

    @Override
    public SimilarityMatrix<TableRow> match(MatchingData data) {
        Timer t = Timer.getNamed("Candidate Abstract Selection", getRootTimer());
        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Candidate abstract selection ... ");
        }

        Timer tm = Timer.getNamed("setup candidate abstract selection", t);
        // run candidate selection

//        LabelBasedMatcher<TableRow> candidateSelection = new LabelBasedMatcher<TableRow>();
        TextBasedMatcher<TableRow> candidateSelection = new TextBasedMatcher<>();

        // calculate scores based on key/label string similarity
        TextSimilarityMeasure<TableRow> keyMeasure = new TextSimilarityMeasure<>();
        keyMeasure.setSimilarityFunction(new CosineSimilarity());

        candidateSelection.setSimilarityMeasure(keyMeasure);
        candidateSelection.setDocumentTermCount(data.getDocumentTermCount());
        candidateSelection.setVectors(data.getVectors());
        candidateSelection.setUriAdapter(new TableRowUriMatchingAdapter());
        //TODO: AS PARAMETER
        candidateSelection.setThreshold(threshold);

        // use lucene index lookup as blocking strategy
        DocumentOverlapBlocking<TableRow> luceneBlocking = new DocumentOverlapBlocking<>();
        luceneBlocking.setOverallTermMap(data.getOverallTermMap());
        luceneBlocking.setCandidateMap(data.getCandidateMap());

        List<String> allowedClasses = new ArrayList<>();
        for (Table tables : similarites.getClassSimilarity().getMatchesAboveThreshold(similarites.getClassSimilarity().getFirstDimension().iterator().next(), 0.0)) {
            String s = tables.getHeader().replace(".csv", "").replace(".gz", "");
            System.out.println("allowed class: " + s);
            allowedClasses.add(s);
        }

        //restrict the rows which are allowed, not used yet
        //TODO: other idea/implementation? learn a rule?
//        List<TableRow> allowedRows = new ArrayList<>();
//        for(TableRow obj1 : similarites.getInitialCandidateSimilarity().getFirstDimension()) {
//            if(similarites.getInitialCandidateSimilarity().getMatchesAboveThreshold(obj1, 0.1).size()<1) {
//                if(obj1.getRowIndexInFile()==0 && obj1.getTable().getNumHeaderRows()>0){
//                    continue;
//                }
//                allowedRows.add(obj1);
//            }
//        }

        List<TableRow> allowedRows = new ArrayList<>();
        for (TableRow r : data.getWebtableRowSet()) {
            allowedRows.add(r);
//            double sum = 0.0;
//            for (TableRow cands : similarites.getCandidateSimilarity().getMatchesAboveThreshold(r, 0.0)) {
//                sum += similarites.getCandidateSimilarity().get(r, cands);
//            }
//            if ((double) similarites.getCandidateSimilarity().getMatchesAboveThreshold(r, 0.0).size() == 0.0) {
//                allowedRows.add(r);
//                //1.174
//            } else if (sum / (double) similarites.getCandidateSimilarity().getMatchesAboveThreshold(r, 0.0).size() <= 1) {
//                allowedRows.add(r);
//            }
        }
        
        luceneBlocking.setAllowedClasses(allowedClasses);
//        luceneBlocking.setAllowedRows(allowedRows);
        luceneBlocking.setAllowedRows(allowedRows);
        candidateSelection.setBlocking(luceneBlocking);
        tm.stop();

        // run matching
        tm = Timer.getNamed("LabelBasedMatcher: candidate selection", t);

        candidateSelection.setParentTimer(tm);
        SimilarityMatrix<TableRow> initialCandidateSimilarity = null;
        try {
            //should be allows rows!
            initialCandidateSimilarity = candidateSelection.match(data.getWebtableRowSet(),
                    data.getDbpediaRowSet(), new TableRowTokenMatchingAdapter());
            //TODO?
            initialCandidateSimilarity.normalize();
        } catch (Exception ex) {
            Logger.getLogger(CandidateAbstractMatcher.class.getName()).log(Level.SEVERE, null, ex);
        }

        t.stop();

        return initialCandidateSimilarity;
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
