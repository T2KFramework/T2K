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
package de.mannheim.uni.similarity.functions.string;

import de.dwslab.T2K.similarity.functions.string.GeneralisedStringJaccard;
import de.dwslab.T2K.similarity.functions.string.LevenshteinSimilarity;
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
