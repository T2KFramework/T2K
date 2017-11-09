package de.mannheim.uni.similarity.functions.string;

import de.dwslab.T2K.similarity.functions.string.JaccardSimilarity;
import junit.framework.TestCase;

public class JaccardSimilarityTest extends TestCase {

    public void testCalculate() {
        JaccardSimilarity js = new JaccardSimilarity();
        
        js.calculate("hello", "hello");
        
        System.out.println(js.calculate("lonza bio science walkersville inc", "Flow Science Inc."));
        System.out.println(js.calculate("verizon", "verifone"));
        System.out.println(js.calculate("verizon", "verizon communications"));
        System.out.println(js.calculate("wegman\'s", "Wellan\'s"));
        System.out.println(js.calculate("wegman", "Wellan"));
    }
    
}
