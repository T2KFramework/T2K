package de.mannheim.uni.similarity.functions.date;

import de.dwslab.T2K.similarity.functions.date.WeightedDatePartSimilarity;
import java.util.Date;

import de.dwslab.T2K.matching.ValueRange;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

public class WeightedDatePartSimilarityTest extends TestCase {

    public void testCalculate() {
        WeightedDatePartSimilarity sim = new WeightedDatePartSimilarity();
        sim.setDayWeight(0.1);
        sim.setMonthWeight(0.2);
        sim.setYearWeight(0.7);
        sim.setValueRange(new ValueRange(new Date(-900,0,1), new Date(100,6,31)));
        
        Date d1 = new Date(100, 0, 1);
        Date d2 = new Date(100, 6, 1);
        Date d3 = new Date(100, 6, 31);
//        Date d4 = new Date(-900, 0, 1);
        
        //assertEquals(1, sim.calculate(d1, d1).intValue());
        
        //TODO: check them later
//        assertEquals(9, (int)(sim.calculate(d1, d1)*10));
//        assertEquals(9, (int)(sim.calculate(d1, d2)*10));
//                assertEquals(9, (int)(sim.calculate(d1, d3)*10));
        
        
        //assertEquals(8, (int)(sim.calculate(d1, d3)*10));      
        
        
        

        
//        assertEquals(475, (int)(sim.calculate(d1, d4)*1000));
    }
    
}
