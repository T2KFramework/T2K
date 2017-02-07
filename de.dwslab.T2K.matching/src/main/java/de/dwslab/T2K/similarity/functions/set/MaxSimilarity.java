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
package de.dwslab.T2K.similarity.functions.set;

import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;

/**
 * Set-based similarity function that returns the maximum similarity between any two elements of both sets
 * @author Oliver
 *
 * @param <T>
 */
public class MaxSimilarity<T> extends ComplexSetSimilarity<T> {

    @Override
    protected Double aggregateSimilarity(SimilarityMatrix<T> matrix) {
        return matrix.getMaxValue();
    }

}
