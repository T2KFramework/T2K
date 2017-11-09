package de.mannheim.uni.similarity.functions.string;

import de.dwslab.T2K.similarity.functions.string.JaccardOnNGramsSimilarity;
import junit.framework.TestCase;

public class JaccardOnNGramsSimilarityTest extends TestCase {

    public void testCalculateStringString() {
        JaccardOnNGramsSimilarity j = new JaccardOnNGramsSimilarity(2);
        
        assertEquals(1.0, j.calculate("11", "11"));
        assertEquals(0.0, j.calculate("11", "22"));
        assertEquals(0.3, Math.round(j.calculate("11 22", "11 33") * 10) / 10.0);
    }

}
