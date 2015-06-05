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
package de.dwslab.T2K.similarity.functions.numeric;

import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;

public class NormalisedNumericSimilarity extends SimilarityFunction<Double> {

    private Double min;
    public Double getMin() {
        return min;
    }
    public void setMin(Double min) {
        this.min = min;
    }
    
    private Double max;
    public Double getMax() {
        return max;
    }
    public void setMax(Double max) {
        this.max = max;
    }
    
    private Double range;
    public Double getRange() {
        return range;
    }
    public void setRange(Double range) {
        this.range = range;
    }
    
    @Override
    public void setValueRange(ValueRange range) {
        setMax((Double)range.getMaxValue());
        setMin((Double)range.getMinValue());
        
        if(getMin()!=null && getMax()!=null) {
            setRange(getMax() - getMin());
        }
    }
    
    @Override
    public Double calculate(Double first, Double second) {
        
        Double diff = Math.abs(first-second);
        
        if(diff>range) {
            return 0.0;
        } else {
            return diff / range;
        }
    }

}
