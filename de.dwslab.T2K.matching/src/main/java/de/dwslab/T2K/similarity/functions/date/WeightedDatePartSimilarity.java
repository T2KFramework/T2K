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
package de.dwslab.T2K.similarity.functions.date;

import java.util.Date;

import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;

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
        
        int days = Math.abs(first.getDate() - second.getDate());
        int months = Math.abs(first.getMonth() - second.getMonth());
        
        double daySim = (31.0 - days) / 31.0;
        double monthSim = (12.0 - months) / 12.0;
        //double yearSim = new DeviationSimilarity().calculate((double)first.getYear()+1900, (double)second.getYear()+1900);
        double yearSim = 1.0 - (double)Math.abs(first.getYear()-second.getYear()) / (double)yearRange;
        
        if(yearSim!=1.0) {
            yearSim *= 0.0;
        }
        
        daySim = getDayWeight()*daySim;
        monthSim = getMonthWeight()*monthSim;
        yearSim = getYearWeight()*yearSim;
        
        return daySim + monthSim + yearSim;
    }

}
