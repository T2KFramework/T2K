package de.dwslab.T2K.matching.firstline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.MatchingPair;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.timer.Timer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic first-level matcher class generates one similarity value per instance
 * pair
 *
 * @author Oliver
 *
 */
public class LabelBasedMatcherWithFiltering<T> extends FirstLineMatcher<T> {

    private StringBuilder matchingLog;

    public StringBuilder getMatchingLog() {
        return matchingLog;
    }

    /**
     * runs the matching on the provided instance sets for all instances in
     * instancesToMatch, candidate instances from candidates are selected by the
     * blocking instance and then the similarity scores are calculated by the
     * similarity measure instance
     */
    public SimilarityMatrix<T> match(Collection<T> instancesToMatch,
            final Collection<T> candidates, final MatchingAdapter<T> adapter) {

        matchingLog = new StringBuilder();
        // create similarity matrix
        final SimilarityMatrix<T> sim = getSimilarityMatrixFactory().createSimilarityMatrix(instancesToMatch.size(), candidates.size());
        final Map<String,Double> labelSim = new ConcurrentHashMap<>();
        
        try {
            new Parallel<T>(isRunInParallel() ? 0 : 1).foreach(instancesToMatch, new Consumer<T>() {

                public void execute(T parameter) {
                    Collection<T> blockedCandidates = null;

                    Timer t = null;

                    if (getParentTimer() != null) {
                        t = Timer.getNamed("LabelBasedMatcher: Blocking", getParentTimer(), true);
                    }
                    blockedCandidates = getBlocking().getCandidates(parameter, candidates);                   
                    
                    //System.out.println("blocked: " +blockedCandidates.size());
                    //System.out.println("blocked sim measure: " +getSimilarityMeasure().toString());
                    if (t != null) {
                        t.stop();
                    }
                    List<T> valList = new ArrayList<>(1);
                    valList.add(parameter);
                    Collection<Pair<T, T>> filtered = new ArrayList<Pair<T, T>>();

                    for (T o : blockedCandidates) {
                        filtered.add(new Pair<>(parameter, o));
                    }
                    // iterate over all candidates (all instances that could be matched to the current instance)
                    for (Pair<T, T> p : filtered) {
                        //System.out.println("pair: " + p.getFirst() + " vs " +p.getSecond());
                        // calculate similarity
                        if (getParentTimer() != null) {
                            t = Timer.getNamed("LabelBasedMatcher: Similarity Calculation", getParentTimer(), true);
                        }
                        Double similarity = null;
                        if(labelSim.containsKey(p.getFirst()+"_"+p.getSecond())) {
                            similarity = labelSim.get(p.getFirst()+"_"+p.getSecond());
                        }
                        else {
                            similarity = getSimilarityMeasure().calculate(p.getFirst(), p.getSecond(), adapter, null);
                            if(p.getFirst()!=null && p.getSecond()!=null && similarity != null) {
                                labelSim.put(p.getFirst()+"_"+p.getSecond(),similarity);
                            }
                        }
                        //System.out.println("sim comp: " +p.getFirst() + " vs." + p.getSecond() + " score " + similarity);
                        if (t != null) {
                            t.stop();
                        }

                        if (isCollectMatchingInfo()) {
                            getPairs().add(new MatchingPair<T>(p.getFirst(), p.getSecond()));
                            synchronized (matchingLog) {
                                matchingLog.append(String.format("%s <> %s (%.2f)", adapter.getLabel(p.getFirst()), adapter.getLabel(p.getSecond()), similarity));
                            }
                        }

                        // only add if greater than zero
                        if (similarity != null && similarity > 0.0) {
                            synchronized (sim) {
//                                System.out.println("set in matrix: " +p.getFirst() + " --- " + p.getSecond() + "---" + similarity);
                                sim.set(p.getFirst(), p.getSecond(), similarity);
                            }
                        }
                    }
                }

            }, "LabelBasedMatcher");
        } catch (Exception e) {
            System.out.println("error label based filtering");
            StackTraceElement l = new Exception().getStackTrace()[0];
            System.out.println(l.getLineNumber());
            e.printStackTrace();
        }
        return sim;
    }

}
