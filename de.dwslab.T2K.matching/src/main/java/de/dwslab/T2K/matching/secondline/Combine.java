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

import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;

/**
 * Combines the similarities of two SimilarityMatrices using the selected
 * AggregationType. Both Matrices are indexed using the same type.
 * 
 * @author Oliver
 * 
 * @param <T>
 */
public class Combine<T> extends SecondLineMatcher {

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

    public SimilarityMatrix<T> match(SimilarityMatrix<T> first,
            SimilarityMatrix<T> second) {
        
        // create similarity matrix
        SimilarityMatrix<T> sim = getSimilarityMatrixFactory()
                .createSimilarityMatrix(first.getFirstDimension().size(),
                        first.getSecondDimension().size());

        for (T instance : first.getFirstDimension()) {

            for (T candidate : first.getSecondDimension()) {

                Double firstScore = first.get(instance, candidate);
                Double secondScore = second.get(instance, candidate);

                Double finalScore = null;

                if(firstScore!=null && secondScore!=null) {                    
                    
                    switch (getAggregationType()) {
                    case Sum:
                        finalScore = (double)firstScore + (double)secondScore;
                        break;
                    case Average:
                        finalScore = (firstScore + secondScore) / 2.0;
                        break;
                    case WeightedSum:
                        finalScore = getFirstWeight() * firstScore
                                + getSecondWeight() * secondScore;
                        break;
                    case Multiply:
                        finalScore = firstScore * secondScore;
                        break;
                    default:
                        break;
                    }
                }  
                sim.set(instance, candidate, finalScore);
            }

        }

        return sim;
    }

}
