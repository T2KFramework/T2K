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
