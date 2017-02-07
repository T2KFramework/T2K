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

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import java.util.Collection;

/**
 *
 * @author domi
 */
public class TextSimilarityMeasure<T> extends SimilarityMeasure<T> {

    private SimilarityFunction<double[]> similarityFunction;

    public SimilarityFunction<double[]> getSimilarityFunction() {
        return similarityFunction;
    }

    public void setSimilarityFunction(
            SimilarityFunction<double[]> similarityFunction) {
        this.similarityFunction = similarityFunction;
    }

    public TextSimilarityMeasure(){}
    
    public TextSimilarityMeasure(SimilarityFunction<double[]> similarityFunction) {
        setSimilarityFunction(similarityFunction);
    }

    @Override
    protected Double calculateSingleValued(Object first, Object second, ValueRange range) {
        return similarityFunction.calculate((double[])first, (double[])second);
    }

    @Override
    protected Double calculateMultiValued(Collection<Object> first, Collection<Object> second, ValueRange range) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @SuppressWarnings("unchecked")
    @Override
    public Double calculate(T first, T second, MatchingAdapter<T> adapter, ValueRange range) {
        return calculateSingleValued(first, second, range);
    }

}
