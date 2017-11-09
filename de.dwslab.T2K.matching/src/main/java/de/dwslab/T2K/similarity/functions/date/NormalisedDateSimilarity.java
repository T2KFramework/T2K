package de.dwslab.T2K.similarity.functions.date;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;

import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;

public class NormalisedDateSimilarity extends SimilarityFunction<Date> {

    private Date minDate = null;
    public void setMinDate(Date minDate) {
        this.minDate = minDate;
    }
    public Date getMinDate() {
        return minDate;
    }
    
    private Date maxDate = null;
    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }
    public Date getMaxDate() {
        return maxDate;
    }
    
    private int dateRange = 0;
    public int getDateRange() {
        return dateRange;
    }
    
    @Override
    public void setValueRange(ValueRange range) {
        setMinDate((Date)range.getMinValue());
        setMaxDate((Date)range.getMaxValue());
        calcDateRange();
    }
    
    private void calcDateRange() {
        if(minDate!=null && maxDate!=null) {
            dateRange = Math.abs(Days.daysBetween(new DateTime(getMaxDate()), new DateTime(getMinDate())).getDays());
        }
    }
    
    @Override
    public Double calculate(Date first, Date second) {
        int days = Math.abs(Days.daysBetween(new DateTime(first), new DateTime(second)).getDays());
        
        return Math.max(1.0 - ((double)days / (double)getDateRange()),0.0);
    }

}
