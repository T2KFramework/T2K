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
 *
 * @author domi
 */
public class CombineNonOverlapping<T> extends SecondLineMatcher {

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

        if (first == null) {
            return second;
        } else if (second == null) {
            return first;
        }
        // create similarity matrix
        SimilarityMatrix<T> sim = getSimilarityMatrixFactory().createSimilarityMatrix(first.getFirstDimension().size() + second.getFirstDimension().size(),
                first.getSecondDimension().size() + second.getSecondDimension().size());

        for (T instance : first.getFirstDimension()) {
            for (T candidate : first.getSecondDimension()) {
                Double firstScore = first.get(instance, candidate);
                Double secondScore = second.get(instance, candidate);

                Double finalScore = null;
                if (secondScore == null && firstScore == null) {
                    continue;
                }

                if (firstScore != null && secondScore != null) {
                    switch (getAggregationType()) {
                        case Sum:
                            finalScore = (double) firstScore + (double) secondScore;
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
//                    case Max:
//                        if(firstScore>secondScore) {
//                            finalScore= firstScore;
//                        }
//                        else {
//                            finalScore = secondScore;
//                        }
//                        break;    
                    }
                    System.out.println("first: " + instance + " score: " + firstScore + " --- " + secondScore + " second: " + candidate + " final: " + finalScore);
                    //multiply with 2?
                    sim.set(instance, candidate, finalScore);
                } //                if(firstScore==null && secondScore!=null) {
                //                    finalScore = (double)secondScore*getSecondWeight();
                //                   //System.out.println(secondScore + " second " + candidate);
                //                }
                else if (secondScore == null && firstScore != null) {
                    finalScore = (double) firstScore;
//                    if(getAggregationType()==CombinationType.WeightedSum) {
//                        finalScore = (double)firstScore*getFirstWeight();
//                    }
//                    else {
//                        finalScore = (double)firstScore;
//                    }                    
                    sim.set(instance, candidate, finalScore);
                    System.out.println(instance + " score first: " + firstScore);
                }
//                sim.set(instance, candidate, finalScore);
            }
//            System.out.println("instance set before: " + instance);
//            for(T allCand: sim.getMatches(instance)) {
//                System.out.println(allCand);
//            }

        }
        for (T instance : second.getFirstDimension()) {
            for (T candidate : second.getSecondDimension()) {
                Double firstScore = first.get(instance, candidate);
                Double secondScore = second.get(instance, candidate);

                Double finalScore = null;

                if (secondScore == null && firstScore == null) {
//                    if(getAggregationType()==CombinationType.WeightedSum) {
                    continue;
                }
                
                if (secondScore != null && firstScore == null) {
//                    if(getAggregationType()==CombinationType.WeightedSum) {
                    finalScore = (double) secondScore;
                    sim.set(instance, candidate, finalScore);
                }
                
//                    else {
//                        finalScore = (double)secondScore;
//                    }
                System.out.println(instance + " score second " + secondScore);
//                }                
            }

        }
//        for(T firstDime : sim.getFirstDimension()) {
//        System.out.println("instance set: " + firstDime);
//            for(T allCand: sim.getMatches(firstDime)) {
//                System.out.println(allCand);
//            }
//        }    
        return sim;
    }
}
