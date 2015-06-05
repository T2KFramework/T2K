/**
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of
							Mannheim (t2k@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dwslab.T2K.matching.firstline;

import java.util.Collection;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.MatchingHierarchyAdapater;
import de.dwslab.T2K.matching.MatchingPair;
import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.matching.blocking.Blocking;
import de.dwslab.T2K.matching.blocking.IdentityBlocking;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.math.M;

/**
 * Generic first-level matcher class generates one similarity value per value
 * pair (= multiple values per instance pair)
 * 
 * @author Oliver
 * 
 */
public class ValueBasedMatcher<T, U> extends FirstLineMatcher<T> {

    private Blocking<U> valueBlocking;

    public Blocking<U> getValueBlocking() {
        return valueBlocking;
    }

    public void setValueBlocking(Blocking<U> valueBlocking) {
        this.valueBlocking = valueBlocking;
    }

    public ValueBasedMatcher() {
        super();
        setValueBlocking(new IdentityBlocking<U>());
    }

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
                return null;
            }
        }

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
            log.append(String.format("Matching %s", instance));
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
                
                // iterate over all values of the candidate
                for (U second : blockingValues) {
                    
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
                            .calculate(first, second, adapter, range);
                    
                    if(isCollectMatchingInfo()) {
                        log.append(String.format(" %s (%.2f)", second, similarity));
                    }
                    
                    // only add if not null
                    if (similarity != null) {
                        synchronized (sim) {
                            sim.setLabel(first, instance);
                            sim.setLabel(second, candidate);
                            sim.set(first, second, similarity);
                        }
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
