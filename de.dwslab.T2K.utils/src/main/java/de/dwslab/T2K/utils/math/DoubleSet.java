package de.dwslab.T2K.utils.math;

import java.util.Collection;

public class DoubleSet {

    public static Double average(Collection<Double> values) {
        Double sum = null;
        
        for(Double d : values) {
            if(sum==null) {
                sum = d;
            } else
            {
                sum += d;
            }
        }
        
        return sum / (double)values.size();
    }
    
    public static Double sum(Collection<Double> values) {
        Double sum = null;
        
        for(Double d : values) {
            if(sum==null) {
                sum = d;
            } else
            {
                sum += d;
            }
        }
        
        return sum;
    }
}
