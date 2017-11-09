package de.mannheim.uni.similarity.functions.set;

import de.dwslab.T2K.similarity.functions.set.LeftSideCoverage;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import de.dwslab.T2K.similarity.functions.IdentitySimilarity;
import de.dwslab.T2K.similarity.functions.string.JaccardSimilarity;
import de.dwslab.T2K.similarity.functions.string.LevenshteinSimilarity;
import junit.framework.TestCase;

public class TestSetSimilarities extends TestCase {

    private Collection<String> col1;
    private Collection<String> col2;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        col1 = new ArrayList<String>();
        col2 = new ArrayList<String>();
        
        col1.add("match");
        col1.add("all");
        col1.add("a");
        col1.add("b");
        
        col2.add("mach");
        col2.add("match");
        col2.add("none");
        col2.add("all");
    }
    
    public void testMaxSimilarity() {
        MaxSimilarity<String> sim = new MaxSimilarity<String>();
        Double result = sim.calculate(col1, col2, new LevenshteinSimilarity());
        
        assertEquals(1.0, result);
    }
    
    public void testLeftSideCoverage() {
        LeftSideCoverage<String> sim = new LeftSideCoverage<String>();
        Double result = sim.calculate(col1, col2, new JaccardSimilarity());
        
        assertEquals(0.5, result);
        
        Collection<String> colEmpty = new ArrayList<String>();
        result = sim.calculate(colEmpty, col2, new JaccardSimilarity());
        
        assertEquals(0.0, result);
        
        Collection<String> col3 = new ArrayList<>();
        //col3.add(null);
        col3.add("http://umbel.org/umbel/rc/HockeyTeam");
        col3.add("http://umbel.org/umbel#RefConcept");
        col3.add("http://www.w3.org/2002/07/owl#Class");
        col3.add("http://www.w3.org/2002/07/owl#NamedIndividual");
        //col3.add(null);
        
        result = sim.calculate(col3, colEmpty, new IdentitySimilarity<String>());
        assertEquals(0.0, result);
        
        result = sim.calculate(col3, col3, new IdentitySimilarity<String>());
        assertEquals(1.0, result);
        
        Collection<String> col4 = Arrays.asList("Cole\u00E7\u00E3o|Esquema de conceitos|Cole\u00E7\u00E3o Ordenada".split("\\|"));
        result = sim.calculate(col4, col4, new IdentitySimilarity<String>());
        assertEquals(1.0, result);
    }
    
}
