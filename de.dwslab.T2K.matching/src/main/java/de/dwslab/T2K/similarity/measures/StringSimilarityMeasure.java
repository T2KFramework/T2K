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

import java.util.Collection;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.ComplexSetSimilarity;
import de.dwslab.T2K.utils.data.Pair;

/**
 * A similarity measure for Strings
 * @author Oliver
 *
 */
@SuppressWarnings("rawtypes")
public class StringSimilarityMeasure<T> extends SimilarityMeasure<T> {

    private SimilarityFunction<String> similarityFunction;
    public SimilarityFunction<String> getSimilarityFunction() {
        return similarityFunction;
    }
    public void setSimilarityFunction(
            SimilarityFunction<String> similarityFunction) {
        this.similarityFunction = similarityFunction;
    }

    private ComplexSetSimilarity setSimilarity;
    public ComplexSetSimilarity getSetSimilarity() {
        return setSimilarity;
    }
    public void setSetSimilarity(ComplexSetSimilarity setSimilarity) {
        this.setSimilarity = setSimilarity;
    }
    
    private SignatureFilter<T> signatureFilter;
    public SignatureFilter<T> getSignatureFilter() {
        return signatureFilter;
    }
    public void setSignatureFilter(SignatureFilter<T> signatureFilter) {
        this.signatureFilter = signatureFilter;
    }
    
    public StringSimilarityMeasure() {
    }

    public StringSimilarityMeasure(SimilarityFunction<String> similarityFunction, ComplexSetSimilarity setSimilarity) {
        setSimilarityFunction(similarityFunction);
        setSetSimilarity(setSimilarity);
    }

    @Override
    protected Double calculateSingleValued(Object first, Object second, ValueRange range) {
        return getSimilarityFunction().calculate(first.toString(), second.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Double calculateMultiValued(Collection<Object> first,
            Collection<Object> second, ValueRange range) {
        return getSetSimilarity().calculate(first, second, getSimilarityFunction());
    }

    @Override
    public Collection<Pair<T, T>> applyFilter(Collection<T> values,
            Collection<T> candidates, MatchingAdapter<T> adapter, double threshold) {
        if(getSignatureFilter()==null) {
            return super.applyFilter(values, candidates, adapter, threshold);
        } else {
            return getSignatureFilter().filterCandidates(values, candidates, getSimilarityFunction(), adapter, threshold);
        }
    }
}
