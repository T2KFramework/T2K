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

import java.util.Collection;

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.math.DoubleSet;

/**
 * The first set (left side) is important here. A similarity of 1 is reached if
 * each element of the first set has a corresponding element in the second set
 * (right side) with an inner similarity of 1
 * 
 * @author Oliver
 * 
 * @param <T>
 */
public class LeftSideCoverage<T extends Comparable<T>> extends
        ComplexSetSimilarity<T> {

    @Override
    protected Double aggregateSimilarity(SimilarityMatrix<T> matrix) {
        SimilarityMatrix<T> best = Matcher
                .selectBestStableCandidateForEachInstance(true, matrix);
        Collection<Double> scores = best.getRowSums();

        // best only contains matched pairs, so we have to divide by the
        // dimension of the initial matrix to get the correct average
        return DoubleSet.sum(scores)
                / (double) matrix.getFirstDimension().size();
    }

}
