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
