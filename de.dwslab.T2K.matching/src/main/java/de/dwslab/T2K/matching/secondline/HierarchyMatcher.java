/*
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
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
package de.dwslab.T2K.matching.secondline;

import java.util.Collection;

import de.dwslab.T2K.matching.MatchingHierarchyAdapater;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;

/**
 * abstract base class for matchers dealing with hierarchies. Such a matcher
 * uses two similarity matrices and a MatchingHierarchyAdapter as input. This
 * class provides basic methods for enumerating the pairs to calculate in
 * parallel.
 * 
 * @author Oliver
 * 
 * @param <TFirst>
 * @param <TSecond>
 */
public abstract class HierarchyMatcher<TFirst, TSecond> extends
        SecondLineMatcher {

    private SimilarityMatrix<TFirst> firstMatrix;

    public SimilarityMatrix<TFirst> getFirstMatrix() {
        return firstMatrix;
    }

    private SimilarityMatrix<TSecond> secondMatrix;

    public SimilarityMatrix<TSecond> getSecondMatrix() {
        return secondMatrix;
    }

    private MatchingHierarchyAdapater<TFirst, TSecond> hierarchy;

    public MatchingHierarchyAdapater<TFirst, TSecond> getHierarchy() {
        return hierarchy;
    }

//    private int numThreads = Runtime.getRuntime().availableProcessors();
//    public void setNumThreads(int numThreads) {
//        this.numThreads = numThreads;
//    }
//    public int getNumThreads() {
//        return numThreads;
//    }
//    
//    protected ThreadPoolExecutor createThreadPool() {
//        return new ThreadPoolExecutor(numThreads, numThreads, 0,
//                TimeUnit.SECONDS,
//                new java.util.concurrent.LinkedBlockingQueue<Runnable>(),
//                new ThreadFactory() {
//
//                    public Thread newThread(Runnable r) {
//                        return new Thread(r, "HierarchyMatcher thread");
//                    }
//                });
//    }

    protected abstract String getProgressMessage();

    protected void initialise(SimilarityMatrix<TFirst> first,
            SimilarityMatrix<TSecond> second,
            MatchingHierarchyAdapater<TFirst, TSecond> hierarchy) {
        this.firstMatrix = first;
        this.secondMatrix = second;
        this.hierarchy = hierarchy;
    }

    /**
     * Enumerates all matches from the first SimilarityMatrix and calls
     * calculateScoreForFirstMatrix for each pair
     */
    protected void enumerateFirst() {

//        ThreadPoolExecutor pool = createThreadPool();
//
//        RunnableProgressReporter p = new RunnableProgressReporter();
//        p.setPool(pool);
//        p.setMessage(getProgressMessage());
//        p.start();

        // enumerate all instances of the first dimension of the first matrix
//        for (final TFirst instance : getFirstMatrix().getFirstDimension()) {
//
//            final Collection<TSecond> firstParts = getHierarchy().getParts(
//                    instance);
//
//            // and enumerate all matching candidates of the second dimension
//            for (final TFirst candidate : getFirstMatrix().getMatches(instance)) {
//                // this combination of instance and candidate will appear in the
//                // resulting matrix
//
//                final Double firstScore = getFirstMatrix().get(instance,
//                        candidate);
//
//                final Collection<TSecond> secondParts = getHierarchy()
//                        .getParts(candidate);
//
//                pool.execute(new Runnable() {
//
//                    public void run() {
//                        calculateScoreForFirstMatrix(instance, candidate,
//                                firstParts, secondParts, firstScore);
//                    }
//                });
//
//            }

        try {
            new Parallel<TFirst>(isRunInParallel() ? 0 : 1).foreach(getFirstMatrix().getFirstDimension(), new Consumer<TFirst>() {

                @Override
                public void execute(TFirst parameter) {
                    final Collection<TSecond> firstParts = getHierarchy().getParts(
                            parameter);

                    // and enumerate all matching candidates of the second dimension
                    for (final TFirst candidate : getFirstMatrix().getMatches(parameter)) {
                        // this combination of instance and candidate will appear in the
                        // resulting matrix

                        final Double firstScore = getFirstMatrix().get(parameter,
                                candidate);

                        final Collection<TSecond> secondParts = getHierarchy()
                                .getParts(candidate);

                        calculateScoreForFirstMatrix(parameter, candidate,
                                firstParts, secondParts, firstScore);

                    }
                    
                }
            }, "HierarchyMatcher");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        
//        pool.shutdown();
//        try {
//            pool.awaitTermination(1, TimeUnit.DAYS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        p.stop();
    }

    /**
     * Calculates the matching score if the resultung matrix has the dimensions
     * of the first input matrix. The default implementation just calls
     * enumerateSecond()
     * 
     * @param instance
     * @param candidate
     * @param firstParts
     * @param secondParts
     * @param firstSimilarity
     */
    protected void calculateScoreForFirstMatrix(TFirst instance,
            TFirst candidate, Collection<TSecond> firstParts,
            Collection<TSecond> secondParts, Double firstSimilarity) {

        enumerateSecond(instance, candidate, firstParts, secondParts,
                firstSimilarity, null);

    }

    /**
     * Enumerates all pairs from the second input matrix based on the pair from
     * the first matrix that is passed as parameter. For each pair,
     * calculateScoreForSecondMatrix is called.
     * 
     * @param instance
     * @param candidate
     * @param firstParts
     * @param secondParts
     * @param firstSimilarity
     * @param secondScores
     */
    protected void enumerateSecond(TFirst instance, TFirst candidate,
            Collection<TSecond> firstParts, Collection<TSecond> secondParts,
            Double firstSimilarity, Collection<Double> secondScores) {
        // for each of the instances, list all values
        for (TSecond firstValue : firstParts) {

            if(isCollectMatchingInfo()) {
                setLabel(firstValue, instance);
            }
            
            // (A)and do the same for all candidates
            // (B) try the following for speed up: dont access second matrix getMatches, just retrieve all scores for secondParts and skip if the score is null
            // (C) decide which list to iterate based on their sizes ...
            for (TSecond secondValue : getSecondMatrix().getMatches(firstValue)) {    //    (A)
            //for (TSecond secondValue : secondParts) {   // (B)
            //Collection<TSecond> lst = secondParts.size() > getSecondMatrix().getSecondDimension().size() ? getSecondMatrix().getMatches(firstValue) : secondParts;   // (C)
            //for (TSecond secondValue : lst) {   // (C)
                

                if (secondParts.contains(secondValue)) {      //        (A)

                    // Collect the scores from the second matrix
                    Double secondSimilarity = getSecondMatrix().get(firstValue,
                            secondValue);

                //if (secondSimilarity!=null) {       // (B) & (C)
                    
                    Double combined = calculateScoreForSecondMatrix(instance, candidate,
                            firstValue, secondValue, firstSimilarity,
                            secondSimilarity);

                    if (secondScores != null && secondSimilarity != null) {
                        secondScores.add(secondSimilarity);
                    }
                    
                    if(isCollectMatchingInfo()) {
                        setLabel(secondValue, candidate);
                        logValue(instance, candidate, firstValue, secondValue, firstSimilarity, secondSimilarity, combined);
                    }
                }
            }
        }
    }

    /**
     * Calculates the matching score if the resulting matrix has the dimensions
     * of the second input matrix. The default implementation does nothing.
     * @param instance
     * @param candidate
     * @param firstValue
     * @param secondValue
     * @param firstSimilarity
     * @param secondSimilarity
     */
    protected Double calculateScoreForSecondMatrix(TFirst instance,
            TFirst candidate, TSecond firstValue, TSecond secondValue,
            Double firstSimilarity, Double secondSimilarity) {
        return null;
    }

    /**
     * assigns the resulting matrix a label (of TFirst) for a given value (of TSecond)
     * @param value
     * @param instance
     */
    protected void setLabel(TSecond value, TFirst label) {
        
    }
    
    protected void logValue(TFirst instance, TFirst candidate, TSecond firstValue, TSecond secondValue, Double firstSimilarity, Double secondSimilarity, Double combinedSimilarity) {
        
    }
}
