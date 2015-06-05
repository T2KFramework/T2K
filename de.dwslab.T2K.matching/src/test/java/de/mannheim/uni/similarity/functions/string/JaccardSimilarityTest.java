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
