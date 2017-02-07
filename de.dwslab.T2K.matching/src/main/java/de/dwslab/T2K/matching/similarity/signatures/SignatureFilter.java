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
package de.dwslab.T2K.matching.similarity.signatures;

import java.util.Collection;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.utils.data.Pair;

public abstract class SignatureFilter<T> {

    /***
     * Filters the two lists that are provided and returns only those pairs that reach the threshold with the given similarity function
     * @param values
     * @param candidates
     */
    public abstract Collection<Pair<T, T>> filterCandidates(Collection<T> values, Collection<T> candidates, SimilarityFunction sim, MatchingAdapter<T> adapter, double threshold);
    
}
