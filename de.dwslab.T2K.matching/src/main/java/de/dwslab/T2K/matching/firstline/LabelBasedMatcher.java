package de.dwslab.T2K.matching.firstline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.MatchingPair;
import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.timer.Timer;

/**
 * Generic first-level matcher class generates one similarity value per instance
 * pair
 *
 * @author Oliver
 *
 */
public class LabelBasedMatcher<T> extends FirstLineMatcher<T> {

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

        try {
            new Parallel<T>(isRunInParallel() ? 0 : 1).foreach(instancesToMatch, new Consumer<T>() {

                public void execute(T parameter) {
                    Collection<T> blockedCandidates = null;

                    Timer t = null;

                    if (getParentTimer() != null) {
                        t = Timer.getNamed("LabelBasedMatcher: Blocking", getParentTimer(), true);
                    }
                    blockedCandidates = getBlocking().getCandidates(parameter, candidates);
                    if (t != null) {
                        t.stop();
                    }

                    // iterate over all candidates (all instances that could be matched to the current instance)
                    for (T candidate : blockedCandidates) {
                        // calculate similarity
                        if (getParentTimer() != null) {
                            t = Timer.getNamed("LabelBasedMatcher: Similarity Calculation", getParentTimer(), true);
                        }
                        Double similarity = getSimilarityMeasure().calculate(parameter, candidate, adapter, new ValueRange(null, null));
                        if (t != null) {
                            t.stop();
                        }

                        if (isCollectMatchingInfo()) {
                            getPairs().add(new MatchingPair<T>(parameter, candidate));
                            synchronized (matchingLog) {
                                matchingLog.append(String.format("%s <> %s (%.2f)", adapter.getLabel(parameter), adapter.getLabel(candidate), similarity));
                            }
                        }

                        // only add if greater than zero
                        if (similarity != null) {
                            if (similarity >= getSimilarityThreshold()) {
                                synchronized (sim) {
                                    sim.set(parameter, candidate, similarity);
                                }
                            }
                        }
                    }
                }

            }, "LabelBasedMatcher");
        } catch (Exception e) {
            e.printStackTrace();
        }
//		// iterate over all instances to match
//		for(T instance : instancesToMatch) {
//			
//			// iterate over all candidates (all instances that could be matched to the current instance)
//			for(T candidate : getBlocking().getCandidates(instance, candidates))
//			{
//				// calculate similarity
//				Double similarity = getSimilarityMeasure().calculate(instance, candidate, adapter);
//				
//				// only add if greater than zero
//				if(similarity!=null)
//				{
//					sim.set(instance, candidate, similarity);
//				}
//			}
//			
//		}

        return sim;
    }

}
