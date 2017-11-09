package de.dwslab.T2K.matching.evaluation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import de.dwslab.T2K.matching.correspondences.Correspondence;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates a collection of correspondences against a gold standard
 * @author Oliver
 *
 */
public class MatchingEvaluator {

    private boolean verbose = false;

    public boolean isVerbose() {
        return verbose;
    }

    /**
     * sets whether the evaluator will print all steps to the console
     * 
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Evaluates a list of correspondences against a gold standard
     * 
     * @param correspondences
     *            each correspondence consists of a 'first' and 'second' objects
     * @param goldStandard
     *            the IDs (as given by the adapter) of the 'first' objects in
     *            the correspondences are the keys of this map and the IDs of
     *            the 'second' objects are the values
     * @param correspondenceAdapter
     *            returns an ID for each of the objects that form the
     *            correspondences
     * @return
     */
    public <TCorrespondence> EvaluationResult evaluateMatching(
            Collection<Correspondence<TCorrespondence>> correspondences,
            Map<Object, List<Object>> goldStandard,
            int totalInstances,
            EvaluationAdapter<TCorrespondence> firstAdapter,
            EvaluationAdapter<TCorrespondence> secondAdapter) {

        // iterate all correspondences and count
        int correct = 0;
        int matches = correspondences.size();
        int total = goldStandard.keySet().size();

//        System.out.println("GS size evaluator: " +goldStandard.size());
        
        HashSet<Object> mapped = new HashSet<Object>();        
        
        for (Correspondence<TCorrespondence> cor : correspondences) {
            List<Object> correctIds = goldStandard.get(firstAdapter.getUniqueIdentifier(cor.getFirst()));
                        
//            System.out.println("cor.getFirst: " + cor.getFirst());            
//            System.out.println("cor.getFirst first Adapter: " + firstAdapter.getUniqueIdentifier(cor.getFirst()));
//            for(Object o : goldStandard.keySet()) {
//                System.out.println("key: " + o + " velaue: " + goldStandard.get(o));
//            }
//            String s = firstAdapter.getUniqueIdentifier(cor.getFirst()).toString();
//            System.out.println("GS cor.getFirst first Adapter: " + goldStandard.get(s));
            
            Object actualId = null;

            mapped.add(firstAdapter.getUniqueIdentifier(cor.getFirst()));
            
            if (cor.getSecond() != null) {
                actualId = secondAdapter.getUniqueIdentifier(cor.getSecond());
            }
            
//            System.out.println("cor.getSecond: " + cor.getSecond());            
//            System.out.println("cor.getSecond second Adapter: " + secondAdapter.getUniqueIdentifier(cor.getSecond()));
//            System.out.println("correct IDS: " + correctIds);

            if (correctIds == null && actualId == null
                    || (correctIds != null && actualId != null && correctIds.contains(actualId))) {
                correct++;

                cor.setCorrect(true);

                if (isVerbose()) {
                    System.out.println(firstAdapter.getUniqueIdentifier(cor
                            .getFirst()) + " -> " + actualId + " == CORRECT");
                }
            } else {

                cor.setCorrect(false);

                if (isVerbose()) {
                    System.out.println(firstAdapter.getUniqueIdentifier(cor
                            .getFirst())
                            + " -> "
                            + actualId
                            + " == WRONG (should be " + correctIds + ")");
                }
            }
            
            cor.setCorrectValue(correctIds);

        }

        if(isVerbose()) {
            for(Object key : goldStandard.keySet()) {
                System.out.println("key!! " +key);
                if(!mapped.contains(key)) {
                    System.out.println(key + " -> missing, should be " + goldStandard.get(key));
                }
            }
        }
        
        return new EvaluationResult(correct, matches, total, totalInstances);
    }

}
