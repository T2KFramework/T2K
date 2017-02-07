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
package de.dwslab.T2K.similarity.functions;

import de.dwslab.T2K.matching.ValueRange;

public class SimilarityFunctionDecorator<T> extends SimilarityFunction<T> {

    private SimilarityFunction<T> innerFunction;
    protected SimilarityFunction<T> getInnerFunction() {
        return innerFunction;
    }
    
    public SimilarityFunctionDecorator(SimilarityFunction<T> innerFunction) {
        this.innerFunction = innerFunction;
    }
    
    @Override
    public Double calculate(T first, T second) {
        return innerFunction.calculate(first, second);
    }
    
    @Override
    public void setValueRange(ValueRange range) {
        innerFunction.setValueRange(range);
    }
    
}
