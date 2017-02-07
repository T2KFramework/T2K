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

/**
 * returns a matrix with the same dimensions as the second input matrix. in this matrix, every value is assigned to only one other value from each element of the second dimension of the first matrix.
 * @author Oliver
 *
 * @param <TFirst>
 * @param <TSecond>
 */
public class ParentOneToOneConstraint<TFirst, TSecond> extends HierarchyMatcher<TFirst, TSecond> {

    @Override
    protected String getProgressMessage() {
        return "ParentOneToOneConstraint";
    }

    private ConflictResolution conflictResolution;
    public ConflictResolution getConflictResolution() {
        return conflictResolution;
    }
    public void setConflictResolution(ConflictResolution conflictResolution) {
        this.conflictResolution = conflictResolution;
    }
    
    public ParentOneToOneConstraint(ConflictResolution conflictResolution) {
        setConflictResolution(conflictResolution);
    }
    
    private SimilarityMatrix<TSecond> result;
    
    public SimilarityMatrix<TSecond> match(SimilarityMatrix<TFirst> first,
            SimilarityMatrix<TSecond> second,
            MatchingHierarchyAdapater<TFirst, TSecond> hierarchy) {

        initialise(first, second, hierarchy);

        result = getSimilarityMatrixFactory().createSimilarityMatrix(
                second.getFirstDimension().size(),
                second.getSecondDimension().size());

        enumerateFirst();

        return result;
    }
    
    @Override
    protected void enumerateSecond(TFirst instance, TFirst candidate,
            Collection<TSecond> firstParts, Collection<TSecond> secondParts,
            Double firstSimilarity, Collection<Double> secondScores) {
        // for each of the instances, list all values
        for (TSecond firstValue : firstParts) {

            if(isCollectMatchingInfo()) {
                setLabel(firstValue, instance);
            }
            
            TSecond best = null;
            double bestScore;
            
            switch (getConflictResolution()) {
            case Maximum:
                bestScore=Double.MIN_VALUE;
                break;
            case Minimum:
                bestScore = Double.MAX_VALUE;
            default:
                bestScore=0.0;
                break;
            }
            
            // and do the same for all candidates
            for (TSecond secondValue : getSecondMatrix().getMatches(firstValue)) {

                if (secondParts.contains(secondValue)) {

                    // Collect the scores from the second matrix
                    double secondSimilarity = getSecondMatrix().get(firstValue,
                            secondValue);

                    switch (getConflictResolution()) {
                    case Maximum:
                        if(secondSimilarity>bestScore) {
                            // get the candidate value with the best score
                            best = secondValue;
                            bestScore = secondSimilarity;
                        }
                        break;
                    case Minimum:
                        if(secondSimilarity<bestScore) {
                            // get the candidate value with the best score
                            best = secondValue;
                            bestScore = secondSimilarity;
                        }
                        break;
                    default:
                        break;
                    }
                    
                }
            }
            
            if(best!=null) {
                
                // keep only the best candidate value
                synchronized (result) {
                    result.set(firstValue, best, bestScore);
                }
                
                
                if(isCollectMatchingInfo()) {
                    setLabel(best, candidate);
                }
                
            }
        }
    }
}
