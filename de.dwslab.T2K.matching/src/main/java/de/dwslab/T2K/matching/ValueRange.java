package de.dwslab.T2K.matching;

public class ValueRange {

    private Object minValue;
    public Object getMinValue() {
        return minValue;
    }
    private Object maxValue;
    public Object getMaxValue() {
        return maxValue;
    }
    public ValueRange(Object minValue, Object maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
}
