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
package de.mannheim.uni.similarity.functions.date;

import java.util.Date;

import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.similarity.functions.date.NormalisedDateSimilarity;
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
