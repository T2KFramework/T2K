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
