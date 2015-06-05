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
package de.dwslab.T2K.matching.secondline;

import de.dwslab.T2K.matching.MatchingHierarchyAdapater;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;

/**
 * Combines the similarities of two SimilarityMatrices using the selected
 * AggregationType. Both Matrices are indexed using the different types. The
 * relation between these types is a hierarchy that is given by the provided
 * MatchingHierarchyAdapter
 * 
 * @author Oliver
 * 
 * @param <TFirst>
 * @param <TSecond>
 */
public class CombineHierarchy<TFirst, TSecond> extends
        HierarchyMatcher<TFirst, TSecond> {

    private CombinationType aggregationType;

    public void setAggregationType(CombinationType aggregationType) {
        this.aggregationType = aggregationType;
    }

    public CombinationType getAggregationType() {
        return aggregationType;
    }

    private double firstWeight = 1.0;
    private double secondWeight = 1.0;

    public double getFirstWeight() {
        return firstWeight;
    }

    public void setFirstWeight(double firstWeight) {
        this.firstWeight = firstWeight;
    }

    public double getSecondWeight() {
        return secondWeight;
    }

    public void setSecondWeight(double secondWeight) {
        this.secondWeight = secondWeight;
    }

    private SimilarityMatrix<TSecond> result;

    @Override
    protected void setLabel(TSecond value, TFirst instance) {
        synchronized (result) {
            result.setLabel(value, instance);
        }
    }
    
    @Override
    protected String getProgressMessage() {
        return "CombineHierarchy";
    }
    
    public SimilarityMatrix<TSecond> match(SimilarityMatrix<TFirst> first,
            SimilarityMatrix<TSecond> second,
            MatchingHierarchyAdapater<TFirst, TSecond> hierarchy) {

        log = new StringBuilder();
        
        initialise(first, second, hierarchy);

        result = getSimilarityMatrixFactory().createSimilarityMatrix(
                second.getFirstDimension().size(),
                second.getSecondDimension().size());

        enumerateFirst();

        return result;
    }
    
    @Override
    protected Double calculateScoreForSecondMatrix(TFirst instance, TFirst candidate, TSecond firstValue, TSecond secondValue, Double firstSimilarity,
            Double secondSimilarity) {

        Double score = 0.0;

        if(firstSimilarity==null || secondSimilarity==null) {
            score = null;
        } else {
            switch (getAggregationType()) {
            case Sum:
                score = firstSimilarity + secondSimilarity;
                break;
            case Average:
                score = (firstSimilarity + secondSimilarity) / 2.0;
                break;
            case WeightedSum:
                score = getFirstWeight() * firstSimilarity + getSecondWeight()
                        * secondSimilarity;
                break;
            case Multiply:
                score = firstSimilarity * secondSimilarity;
                break;
            default:
                break;
            }
        }

        synchronized (result) {
            result.set(firstValue, secondValue, score);
        }
        
        return score;
    }

    private StringBuilder log;
    
    @Override
    protected void logValue(TFirst instance, TFirst candidate,
            TSecond firstValue, TSecond secondValue, Double firstSimilarity,
            Double secondSimilarity, Double combinedSimilarity) {
        synchronized (log) {
            log.append(instance);
            log.append("<->");
            log.append(candidate);
            log.append(String.format(" (%.2f)", firstSimilarity));
            log.append(": ");
            log.append(firstValue);
            log.append(" = ");
            log.append(secondValue);
            log.append(String.format(" (%.2f)", secondSimilarity));
            log.append(String.format(" => %.2f", combinedSimilarity));
            log.append("\n");
        }
    }
    
    public StringBuilder getLog() {
        return log;
    }
    
    // public SimilarityMatrix<TSecond> match(SimilarityMatrix<TFirst> first,
    // SimilarityMatrix<TSecond> second,
    // MatchingHierarchyAdapater<TFirst, TSecond> hierarchy) {
    //
    // // create similarity matrix
    // SimilarityMatrix<TSecond> sim = getSimilarityMatrixFactory()
    // .createSimilarityMatrix(second.getFirstDimension().size(),
    // second.getSecondDimension().size());
    //
    // int steps = 0;
    //
    // // enumerate all instances of the first dimension of the first matrix
    // for (TFirst instance : first.getFirstDimension()) {
    //
    // Collection<TSecond> firstParts = hierarchy.getParts(instance);
    //
    // // do the same for all candidates of the second dimension of the
    // // first matrix
    // // (only get those with a similarity > 0. As we multiply, the rest
    // // stays 0 anyway)
    // for (TFirst candidate : first.getMatchesAboveThreshold(instance, 0)) {
    //
    // double firstScore = first.get(instance, candidate);
    //
    // Collection<TSecond> secondParts = hierarchy.getParts(candidate);
    //
    // boolean anyMatches = false;
    // if (isCollectMatchingInfo()) {
    // getPairs().add(
    // new MatchingPair<TFirst>(instance, candidate));
    // }
    //
    // // for each of these instances, list all values
    // for (TSecond firstValue : firstParts) {
    //
    // if (isCollectMatchingInfo()) {
    // sim.setLabel(firstValue, instance);
    // }
    //
    // // and get all matching values of the candidate in the
    // // second matrix
    // Collection<TSecond> matches = second.getMatches(firstValue);
    // for (TSecond secondValue : secondParts) {
    //
    // if (matches.contains(secondValue)) {
    //
    // if (isCollectMatchingInfo()) {
    // sim.setLabel(secondValue, candidate);
    // }
    //
    // double secondScore = second.get(firstValue,
    // secondValue);
    //
    // double score = 0.0;
    //
    // switch (getAggregationType()) {
    // case Sum:
    // score = firstScore + secondScore;
    // break;
    // case Average:
    // score = (firstScore + secondScore) / 2.0;
    // break;
    // case WeightedSum:
    // score = getFirstWeight() * firstScore
    // + getSecondWeight() * secondScore;
    // break;
    // case Multiply:
    // score = firstScore * secondScore;
    // break;
    // default:
    // break;
    // }
    //
    // sim.set(firstValue, secondValue, score);
    //
    // if (score > 0.0) {
    // anyMatches = true;
    // }
    //
    // steps++;
    //
    // }
    // }
    // }
    //
    // if (isCollectMatchingInfo() && anyMatches) {
    // getNonZeroPairs().add(
    // new MatchingPair<TFirst>(instance, candidate));
    // }
    // }
    // }
    //
    // System.out.println(steps + " steps during CombineHierarchy");
    //
    // return sim;
    // }

}
