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
import de.dwslab.T2K.similarity.functions.date.WeightedDatePartSimilarity;
import junit.framework.TestCase;

public class WeightedDatePartSimilarityTest extends TestCase {

    public void testCalculate() {
        WeightedDatePartSimilarity sim = new WeightedDatePartSimilarity();
        sim.setDayWeight(0.1);
        sim.setMonthWeight(0.2);
        sim.setYearWeight(0.7);
        sim.setValueRange(new ValueRange(new Date(-900,0,1), new Date(100,6,31)));
        
        Date d1 = new Date(100, 0, 1);
        Date d2 = new Date(100, 6, 1);
        Date d3 = new Date(100, 6, 31);
//        Date d4 = new Date(-900, 0, 1);
        
        assertEquals(1, sim.calculate(d1, d1).intValue());
        assertEquals(9, (int)(sim.calculate(d1, d2)*10));
        assertEquals(8, (int)(sim.calculate(d1, d3)*10));
//        assertEquals(475, (int)(sim.calculate(d1, d4)*1000));
    }
    
}
