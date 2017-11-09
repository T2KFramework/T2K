package de.mannheim.uni.similarity.functions.date;

import de.dwslab.T2K.similarity.functions.date.NormalisedDateSimilarity;
import java.util.Date;

import de.dwslab.T2K.matching.ValueRange;
import junit.framework.TestCase;

public class NormalisedDateSimilarityTest extends TestCase {

    public void testCalculate() {
        NormalisedDateSimilarity sim = new NormalisedDateSimilarity();
        
        Date dMin = new Date(100, 0, 1);
        Date dMax = new Date(100, 11, 31);
        
        ValueRange r = new ValueRange(dMin, dMax);
        sim.setValueRange(r);
        
        Date d1 = new Date(100, 0, 1);
        Date d2 = new Date(100, 11, 31);
        
        Date d3 = new Date(100, 5, 20);
        Date d4 = new Date(100, 5, 21);
        Date d5 = new Date(100, 5, 23);
        
        assertEquals(1.0, sim.calculate(d1, d1));
        assertEquals(0.0, sim.calculate(d1, d2));
        
        assertTrue(sim.calculate(d3, d4)>sim.calculate(d4, d5));
    }
    
}
