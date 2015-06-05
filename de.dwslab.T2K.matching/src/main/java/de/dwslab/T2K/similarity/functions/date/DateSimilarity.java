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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.similarity.functions.date;

import de.dwslab.T2K.similarity.functions.SimilarityFunction;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 *
 * @author domi
 */
public class DateSimilarity extends SimilarityFunction<Date> {

    @Override
    public Double calculate(Date first, Date second) {
        //int dateRange = Days.daysBetween(new DateTime(min), new DateTime(max)).getDays();
        int dateDiff = Math.abs(Days.daysBetween(new DateTime(first), new DateTime(second)).getDays());

        //return (double) dateDiff / (double) dateRange;
        double sim = (double)(365-dateDiff)/365.0;
        if(sim > 1) {
            sim = 0.0;
        }
        return sim;
    }
}
