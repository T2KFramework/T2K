package de.dwslab.T2K.matching.firstline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.MatchingHierarchyAdapater;
import de.dwslab.T2K.matching.MatchingPair;
import de.dwslab.T2K.matching.SimilarityCollector;
import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.matching.blocking.Blocking;
import de.dwslab.T2K.matching.blocking.IdentityBlocking;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.data.Triple;
import de.dwslab.T2K.utils.math.M;

/**
 * Generic first-level matcher class generates one similarity value per value
 * pair (= multiple values per instance pair)
 * 
 * @author Oliver
 * 
 */
public class ValueBasedMatcherWithFiltering<T, U> extends FirstLineMatcher<T> {

    private Blocking<U> valueBlocking;

    public Blocking<U> getValueBlocking() {
        return valueBlocking;
    }

    public void setValueBlocking(Blocking<U> valueBlocking) {
        this.valueBlocking = valueBlocking;
    }

    public ValueBasedMatcherWithFiltering() {
        super();
        setValueBlocking(new IdentityBlocking<U>());
    }

    private StringBuilder matchingLog;
    public StringBuilder getMatchingLog() {
        return matchingLog;
    }
    
//    private SimilarityCollector<U> collector;
    
    /**
     * runs the matching on the provided instance sets for all instances in
     * instancesToMatch, candidate instances from candidates are selected by the
     * blocking instance and then the similarity scores are calculated by the
     * similarity measure instance
     */
    public SimilarityMatrix<U> match(Collection<T> instancesToMatch,
            final Collection<T> candidates,
            final MatchingHierarchyAdapater<T, U> hierarchy,
            final MatchingAdapter<U> adapter) {

        matchingLog = new StringBuilder();
        
        // create similarity matrix
        final SimilarityMatrix<U> sim = getSimilarityMatrixFactory()
                .createSimilarityMatrix(instancesToMatch.size(),
                        candidates.size());

//        matchSingleThreaded(instancesToMatch, candidates, hierarchy,
//                adapter, sim);
        
//        collector = new SimilarityCollector<>();
//        collector.startCollectingSimilarities(sim);
        
        if (isCollectMatchingInfo() || !isRunInParallel()) {
            matchSingleThreaded(instancesToMatch, candidates, hierarchy,
                    adapter, sim);
        } else {
//         not working yet ...?
            try {
                matchMultiThreaded(instancesToMatch, candidates, hierarchy,
                        adapter, sim);
            } catch (Exception e) {
                e.printStackTrace();
//                collector.finishCollectingSimilarities();
                return null;
            }
        }

//        collector.finishCollectingSimilarities();
        
        return sim;
    }

    private void matchMultiThreaded(Collection<T> instancesToMatch,
            final Collection<T> candidates,
            final MatchingHierarchyAdapater<T, U> hierarchy,
            final MatchingAdapter<U> adapter, 
            final SimilarityMatrix<U> sim) throws Exception {

        // iterate over all instances to match
//        try {
            new Parallel<T>().foreach(instancesToMatch, new Consumer<T>() {

                public void execute(T instance) {
                   matchInstance(instance, candidates, hierarchy, adapter, sim);
                }

            }, "ValueBasedMatcher");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void matchSingleThreaded(Collection<T> instancesToMatch,
            Collection<T> candidates,
            MatchingHierarchyAdapater<T, U> hierarchy,
            MatchingAdapter<U> adapter, 
            SimilarityMatrix<U> sim) {

        for (T instance : instancesToMatch) {
            matchInstance(instance, candidates, hierarchy, adapter, sim);
        }
    }
    
    private void matchInstance(T instance, Collection<T> candidates,
            MatchingHierarchyAdapater<T, U> hierarchy,
            MatchingAdapter<U> adapter, 
            SimilarityMatrix<U> sim) {

        StringBuilder log = new StringBuilder();
        
        if(isCollectMatchingInfo()) {
            log.append(String.format("\nMatching %s", instance));
        }
        
        // iterate over all candidates (all instances that could be
        // matched
        // to the current instance)
        Collection<T> blockingCandidates = getBlocking()
                .getCandidates(instance, candidates);
        for (T candidate : blockingCandidates) {
            
            if(isCollectMatchingInfo()) {
                log.append(String.format("%n\twith %s:", candidate));
            }
            
            // iterate over all values of the instance
            for (U first : hierarchy.getParts(instance)) {

                ValueRange range = hierarchy.getValueRange(instance, first);
                
                Collection<U> blockingValues = getValueBlocking()
                        .getCandidates(first,
                                hierarchy.getParts(candidate));

                if(isCollectMatchingInfo()) {
                    log.append(String.format("%n\t\t%s <>", first));
                }
                
                List<U> valList = new ArrayList<>(1);
                valList.add(first);
                Collection<Pair<U, U>> filtered = getSimilarityMeasure().applyFilter(valList, blockingValues, adapter, getSimilarityThreshold());
                
                // iterate over all values of the candidate
                //for (U second : blockingValues) {
                for(Pair<U, U> p : filtered) {
                    
                    //HierarchyStatistics secondStatistics = hierarchy.getStatistics(candidate, second);
                    
//                    getSimilarityMeasure().setValueRange(
//                            M.min(
//                                    firstStatistics.getMinValue(), 
//                                    secondStatistics.getMinValue()), 
//                            M.max(
//                                    firstStatistics.getMaxValue(), 
//                                    secondStatistics.getMaxValue()));
                            
                    //ValueRange range = new ValueRange(firstStatistics.getMinValue(), firstStatistics.getMaxValue());
                    
                    Double similarity = getSimilarityMeasure()
                            .calculate(p.getFirst(), p.getSecond(), adapter, range);
                    
                    if(isCollectMatchingInfo()) {
                        log.append(String.format(" %s (%.2f)", p.getSecond(), similarity));
                    }
                    
                    // only add if not null
                    //if (similarity != null) {
                    if (similarity != null && similarity > getSimilarityThreshold()) {
                        synchronized (sim) {
                            if(isCollectMatchingInfo()) {
                                sim.setLabel(p.getFirst(), instance);
                                sim.setLabel(p.getSecond(), candidate);
                            }
                            sim.set(p.getFirst(), p.getSecond(), similarity);
                        }
//                        if(isCollectMatchingInfo()) {
//                            collector.queueLabel(new Pair<>(p.getFirst(), (Object)instance));
//                            collector.queueLabel(new Pair<>(p.getSecond(), (Object)candidate));
//                        }
//                        collector.queueSimilarity(new Triple<>(p.getFirst(), p.getSecond(), similarity));
                    }
                }
            }
        }
        
        if(isCollectMatchingInfo()) {
            getMatchingLog().append(log);
        }
        
        
//        // iterate over all candidates (all instances that could be matched
//        // to the current instance)
//        Collection<T> blockingCandidates = getBlocking().getCandidates(
//                instance, candidates);
//        for (T candidate : blockingCandidates) {
//
//            boolean anyMatches = false;
//            if (isCollectMatchingInfo()) {
//                getPairs().add(new MatchingPair<T>(instance, candidate));
//            }
//
//            // iterate over all values of the instance
//            for (U first : hierarchy.getParts(instance)) {
//
//                if (isCollectMatchingInfo()) {
//                    sim.setLabel(first, instance);
//                }
//
//                Collection<U> blockingValues = getValueBlocking()
//                        .getCandidates(first, hierarchy.getParts(candidate));
//
//                // iterate over all values of the candidate
//                for (U second : blockingValues) {
//
//                    sim.setLabel(second, candidate);
//
//                    Double similarity = getSimilarityMeasure().calculate(
//                            first, second, adapter);
//                    // only add if not null
//                    if (similarity != null) {
//                        sim.set(first, second, similarity);
//                        anyMatches = true;
//                    }
//                }
//            }
//
//            if (isCollectMatchingInfo() && anyMatches) {
//                getNonZeroPairs().add(
//                        new MatchingPair<T>(instance, candidate));
//            }
//        }
//    }
    }
}
