/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mannheim.uni.matching.optimization;

import de.dwslab.T2K.matching.optimization.simple.AllPossibilities;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.MatchingComponent;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.process.ParameterRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author domi
 */
public class AllPossibilitiesTest extends TestCase {
    
    public void testRecursion() {
        ParameterRange allRanges = new ParameterRange();
        Map<Parameter, List> ranges = new HashMap<>();
        ranges.put(new Parameter("abc"), new ArrayList<>(Arrays.asList(-1.0,-2.0,-3.0)));
        ranges.put(new Parameter("def"), new ArrayList<>(Arrays.asList(-5.0,-6.0)));
        ranges.put(new Parameter("ghi"), new ArrayList<>(Arrays.asList("hallo", "oli")));
        
        allRanges.setRanges(ranges);
        
        MatchingComponent c = new MatchingComponent() {

            @Override
            public void run(Configuration config) {
                
            }
            @Override
            public double evaluate(Configuration config){
                System.out.println(config.print());
                return -1.0;
            }
        };
        
        c.setParameters(new ArrayList<>(Arrays.asList(new Parameter("abc"), new Parameter("def"),new Parameter("ghi"))));
        
        AllPossibilities a = new AllPossibilities();
        a.run(allRanges, c);
        
    }
    
}
