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
package de.dwslab.T2K.similarity.measures;

import java.util.ArrayList;
import java.util.Collection;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.utils.data.Pair;

/**
 * super class for all similarity measures.
 * @author Oliver
 *
 */
public abstract class SimilarityMeasure<T> {

    @SuppressWarnings("unchecked")
    public Double calculate(T first, T second, MatchingAdapter<T> adapter, ValueRange range) {

        if (adapter.isMultiValued(first) || adapter.isMultiValued(second)) {
            return calculateMultiValued(adapter.getLabels(first),
                    adapter.getLabels(second), range);
        } else {
            return calculateSingleValued(adapter.getLabel(first),
                    adapter.getLabel(second), range);
        }

    }

    protected abstract Double calculateSingleValued(Object first, Object second, ValueRange range);

    protected abstract Double calculateMultiValued(Collection<Object> first,
            Collection<Object> second, ValueRange range);
    
    public Collection<Pair<T, T>> applyFilter(Collection<T> values, Collection<T> candidates, MatchingAdapter<T> adapter, double threshold) {
        Collection<Pair<T, T>> result = new ArrayList<Pair<T,T>>();
        
        for(T t : values) {
            for(T t2 : candidates) {
                result.add(new Pair<T, T>(t, t2));
            }
        }
        
        return result;
    }
}
