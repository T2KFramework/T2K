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
package de.dwslab.T2K.similarity.functions.set;

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;

public class GeneralisedJaccard<T extends Comparable<T>> extends ComplexSetSimilarity<T> {

    private double innerThreshold = 0.0;
    public double getInnerThreshold() {
        return innerThreshold;
    }
    public void setInnerThreshold(double innerThreshold) {
        this.innerThreshold = innerThreshold;
    }
    
    @Override
    protected Double aggregateSimilarity(SimilarityMatrix<T> matrix) {

        matrix.prune(getInnerThreshold());
        
        double firstLength = matrix.getFirstDimension().size();
        double secondLength = matrix.getSecondDimension().size();
        
        matrix = Matcher.selectBestStableCandidateForEachInstance(true, matrix);
        
        double fuzzyMatching = matrix.getSum();
        
        return fuzzyMatching / (firstLength + secondLength - fuzzyMatching);
    }

}
