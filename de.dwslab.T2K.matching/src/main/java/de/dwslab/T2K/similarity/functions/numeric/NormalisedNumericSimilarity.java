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
