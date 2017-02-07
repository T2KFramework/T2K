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
package de.dwslab.T2K.similarity.measures;

import java.util.Collection;
import java.util.Map;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.ComplexSetSimilarity;
import de.dwslab.T2K.utils.data.Pair;

import java.util.HashMap;

/**
 * A similarity measure that uses different similarity functions based on the type of the instances that are compared
 * @author Oliver
 *
 */
@SuppressWarnings("rawtypes")
public class TypeBasedSimilarityMeasure<T> extends SimilarityMeasure<T> {

    public TypeBasedSimilarityMeasure() {
        similarityFunctions = new HashMap<Object, SimilarityFunction>();
        setSimilarities = new HashMap<Object, ComplexSetSimilarity>();
        signatureFilters = new HashMap<>();
        similarityFunctionsForSets = new HashMap<>();
    }

    private Map<Object, SimilarityFunction> similarityFunctions;

    public Map<Object, SimilarityFunction> getSimilarityFunctions() {
        return similarityFunctions;
    }

    private Map<Object, SimilarityFunction> similarityFunctionsForSets;
    
    /**
     * returns the similarity functions used during set comparisons
     * @return
     */
    public Map<Object, SimilarityFunction> getSimilarityFunctionsForSets() {
        return similarityFunctionsForSets;
    }
    
    private Map<Object, ComplexSetSimilarity> setSimilarities;

    public Map<Object, ComplexSetSimilarity> getSetSimilarities() {
        return setSimilarities;
    }

    private Map<Object, SignatureFilter<T>> signatureFilters;
    
    public Map<Object, SignatureFilter<T>> getSignatureFilters() {
        return signatureFilters;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Double calculate(T first, T second, MatchingAdapter<T> adapter, ValueRange range) {

        Double similarity = null;

        if (adapter.getType(first).equals(adapter.getType(second))) {

            SimilarityFunction fun = similarityFunctions.get(adapter
                    .getType(first));
            if(fun!=null) {
                fun.setValueRange(range);
            }
            
            if (adapter.isMultiValued(first) || adapter.isMultiValued(second)) {

                ComplexSetSimilarity set = setSimilarities.get(adapter
                        .getType(first));
                
                SimilarityFunction fs = similarityFunctionsForSets.get(adapter.getType(first));
                if(fs!=null) {
                    fun = fs;
                }

                if (set != null) {
                    similarity = set.calculate(adapter.getLabels(first),
                            adapter.getLabels(second), fun);
                }

            } else {
                if (fun != null) {
                    similarity = fun.calculate(adapter.getLabel(first),
                            adapter.getLabel(second));
                }
            }
        }

        return similarity;
    }

    @Override
    protected Double calculateSingleValued(Object first, Object second, ValueRange range) {
        return null;
    }

    @Override
    protected Double calculateMultiValued(Collection<Object> first,
            Collection<Object> second, ValueRange range) {
        return null;
    }

    @Override
    public Collection<Pair<T, T>> applyFilter(Collection<T> values,
            Collection<T> candidates, MatchingAdapter<T> adapter, double threshold) {
        
        T first = values.iterator().next();
        Object type = adapter.getType(first);
        SignatureFilter<T> filter = getSignatureFilters().get(type);
        SimilarityFunction sim = getSimilarityFunctions().get(type);
        
        if(filter==null) {
            return super.applyFilter(values, candidates, adapter, threshold);
        } else {
            return filter.filterCandidates(values, candidates, sim, adapter, threshold);
        }
    }
}
