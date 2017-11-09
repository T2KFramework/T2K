package de.mannheim.uni.similarity.functions.string;

import de.dwslab.T2K.similarity.functions.string.LevenshteinSimilarity;
import de.dwslab.T2K.similarity.functions.string.GeneralisedStringJaccard;
import junit.framework.TestCase;

public class GeneralisedJaccardTest extends TestCase {

    public void testCalculate() {
        
        GeneralisedStringJaccard j = new GeneralisedStringJaccard(new LevenshteinSimilarity(), 0.0, 0.0);
        
        String s1, s2;
        
        s1 = "aa cc";
        s2 = "aa bb";
        
        assertEquals(33, (int)(j.calculate(s1, s2)*100));
        
        s1 = "nba mcgrady";
        s2 = "macgrady nba";
        
        assertEquals(88, (int)(j.calculate(s1, s2)*100));
        
        s1 = "nba wnba mcgrady";
        s2 = "macgrady nba";
        
        assertEquals(60, (int)(j.calculate(s1, s2)*100));
    }
    
}
