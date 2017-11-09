package de.dwslab.T2K.similarity.functions.date;

import java.util.Date;

import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import java.util.Calendar;

public class WeightedDatePartSimilarity extends SimilarityFunction<Date> {

    private double dayWeight;
    public double getDayWeight() {
        return dayWeight;
    }
    public void setDayWeight(double dayWeight) {
        this.dayWeight = dayWeight;
    }
    
    private double monthWeight;
    public double getMonthWeight() {
        return monthWeight;
    }
    public void setMonthWeight(double monthWeight) {
        this.monthWeight = monthWeight;
    }
    
    private double yearWeight;
    public double getYearWeight() {
        return yearWeight;
    }
    public void setYearWeight(double yearWeight) {
        this.yearWeight = yearWeight;
    }
    
    @Override
    public void setValueRange(ValueRange range) {
        if(range.getMinValue()!=null && range.getMaxValue()!=null) {
            yearRange = ((Date)range.getMinValue()).getYear()-((Date)range.getMaxValue()).getYear();
        }
    }
    
    public WeightedDatePartSimilarity() {
        
    }
    
    public WeightedDatePartSimilarity(double dayWeight, double monthWeight, double yearWeight) {
        this.dayWeight = dayWeight;
        this.monthWeight = monthWeight;
        this.yearWeight = yearWeight;
    }
    
    private int yearRange = 0;
    
    @Override
    public Double calculate(Date first, Date second) {
        
        Calendar calFirst = Calendar.getInstance();
        calFirst.setTime(first);
        Calendar calSecond = Calendar.getInstance();
        calSecond.setTime(second);
        
        //double reduction = 0.1;
        if(calFirst.get(Calendar.DAY_OF_YEAR) == 1 || calSecond.get(Calendar.DAY_OF_YEAR) == 1) {
            double yearSim = 1.0 - ((double)Math.abs(calFirst.get(Calendar.YEAR) - calSecond.get(Calendar.YEAR)) / (double)Math.abs(yearRange));
            //double value = yearSim*getYearWeight();
            //System.out.println("reduced value: " + value);
            return yearSim;
        }
        
        int days = Math.abs(calFirst.get(Calendar.DAY_OF_MONTH) - calSecond.get(Calendar.DAY_OF_MONTH));
        int months = Math.abs(calFirst.get(Calendar.MONTH) - calSecond.get(Calendar.MONTH));
        
        double daySim = (31.0 - days) / 31.0;
        double monthSim = (12.0 - months) / 12.0;
        //double yearSim = new DeviationSimilarity().calculate((double)first.getYear()+1900, (double)second.getYear()+1900);
        double yearSim = 1.0 - ((double)Math.abs(calFirst.get(Calendar.YEAR) - calSecond.get(Calendar.YEAR)) / (double)Math.abs(yearRange));
        
//        if(yearSim!=1.0) {
//            yearSim *= 0.0;
//        }
        
        daySim = getDayWeight()*daySim;
        monthSim = getMonthWeight()*monthSim;
        yearSim = getYearWeight()*yearSim;
        double value = daySim + monthSim + yearSim;
        value = value/(getDayWeight()+getMonthWeight()+getYearWeight());
        return value;
    }

}
