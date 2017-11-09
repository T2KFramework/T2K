package de.dwslab.T2K.matching.firstline;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.MatchingPair;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.concurrent.Task;
import de.dwslab.T2K.utils.data.Triple;
import de.dwslab.T2K.utils.timer.Timer;

/**
 * First-level Matcher that matches complete objects with a complex similarity measure (i.e. the similarity measure must accept objects) 
 * @author Oliver
 *
 * @param <T>
 */
public class ObjectMatcher<T> extends FirstLineMatcher<T> {

    private StringBuilder matchingLog;
    public StringBuilder getMatchingLog() {
        return matchingLog;
    }
    
    public SimilarityMatrix<T> match(Collection<T> instancesToMatch,
            final Collection<T> candidates, final MatchingAdapter<T> adapter) {
        
        matchingLog = new StringBuilder();
        
        final SimilarityMatrix<T> sim = getSimilarityMatrixFactory().createSimilarityMatrix(instancesToMatch.size(), candidates.size());
        final Queue<Triple<T, T, Double>> similarities = new ConcurrentLinkedQueue<>();
        final AtomicBoolean done = new AtomicBoolean(false);
        
        Task addSimilarities = new Task() {
            
            @Override
            public void execute() {
                while(!done.get() || !similarities.isEmpty()) {
                    Triple<T, T, Double> t = similarities.poll();
                    
                    if(t!=null) {
                        sim.set(t.getFirst(), t.getSecond(), t.getThird());
                    };
                }
            }
        };
        
        Parallel.run(addSimilarities);
        
        new Parallel<T>(isRunInParallel() ? 0 : 1).tryForeach(instancesToMatch, new Consumer<T>() {

            public void execute(T parameter) {
                Collection<T> blockedCandidates = null;
                
                Timer t = null;
                
                if(getParentTimer()!=null) {
                    t = Timer.getNamed("ObjectMatcher: Blocking", getParentTimer(), true);
                }
                
                blockedCandidates = getBlocking().getCandidates(parameter, candidates);
                if(t!=null) {
                    t.stop();
                }
                
                // iterate over all candidates (all instances that could be matched to the current instance)
                for(T candidate : blockedCandidates)
                {
                    // calculate similarity
                    if(getParentTimer()!=null) {
                        t = Timer.getNamed("ObjectMatcher: Similarity Calculation", getParentTimer(), true);
                    }
                    Double similarity = getSimilarityMeasure().calculate(parameter, candidate, adapter, null);
                    if(t!=null) {
                        t.stop();
                    }
                    
                    if(isCollectMatchingInfo()) {
                        getPairs().add(new MatchingPair<T>(parameter, candidate));
                        synchronized (matchingLog) {
                            matchingLog.append(String.format("%s <> %s (%.2f)", parameter, candidate, similarity));
                        }
                    }
                    
                    // only add if greater than zero
                    if(similarity!=null)
                    {
//                        synchronized (sim) {
//                            sim.set(parameter, candidate, similarity);
//                        }
                        similarities.add(new Triple<>(parameter, candidate, similarity));
                    }
                }
            }
            
        }, "ObjectMatcher");
        
        done.set(true);
        Parallel.join(addSimilarities);
        
        return sim;
    }
    
}
