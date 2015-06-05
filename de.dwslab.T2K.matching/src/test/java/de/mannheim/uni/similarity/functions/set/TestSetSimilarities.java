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
package de.mannheim.uni.similarity.functions.set;

import java.util.ArrayList;
import java.util.Collection;

import de.dwslab.T2K.similarity.functions.set.LeftSideCoverage;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
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
    }
    
}
