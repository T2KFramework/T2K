package de.dwslab.T2K.similarity.functions.date;

import java.util.Date;

public class QuadraticNormalisedDateSimiarity extends NormalisedDateSimilarity {

    @Override
    public Double calculate(Date first, Date second) {
        return Math.pow(super.calculate(first, second),4);
    }
    
}
