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
package de.mannheim.uni.matching.secondline;

import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.Combine;
import de.dwslab.T2K.similarity.matrix.ArrayBasedSimilarityMatrixFactory;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.mannheim.uni.matching.TestInstance;
import junit.framework.TestCase;

public class CombineTest extends TestCase {

    public void testSum() {
        Combine<TestInstance> c = new Combine<TestInstance>();
        
        SimilarityMatrix<TestInstance> first = new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(2, 2);
        TestInstance inst1 = new TestInstance(1, "aa");
        TestInstance inst2 = new TestInstance(2, "ab");
        TestInstance cand1 = new TestInstance(11, "aa");
        TestInstance cand2 = new TestInstance(22, "ab");
        
        first.set(inst1, cand1, 1.0);
        first.set(inst1, cand2, 2.0);
        first.set(inst2, cand1, 1.0);
        first.set(inst2, cand2, 2.0);
        
        SimilarityMatrix<TestInstance> second = new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(2, 2);
        second.set(inst1, cand1, 3.0);
        second.set(inst1, cand2, 4.0);
        second.set(inst2, cand1, 3.0);
        second.set(inst2, cand2, 4.0);
        
        c.setAggregationType(CombinationType.Sum);
        SimilarityMatrix<TestInstance> multiplied = c.match(first, second);
        assertEquals(4.0, multiplied.get(inst1, cand1));
        assertEquals(6.0, multiplied.get(inst1, cand2));
        assertEquals(4.0, multiplied.get(inst2, cand1));
        assertEquals(6.0, multiplied.get(inst2, cand2));
    }
    
    public void testAvg() {
        Combine<TestInstance> c = new Combine<TestInstance>();
        
        SimilarityMatrix<TestInstance> first = new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(2, 2);
        TestInstance inst1 = new TestInstance(1, "aa");
        TestInstance inst2 = new TestInstance(2, "ab");
        TestInstance cand1 = new TestInstance(11, "aa");
        TestInstance cand2 = new TestInstance(22, "ab");
        
        first.set(inst1, cand1, 1.0);
        first.set(inst1, cand2, 2.0);
        first.set(inst2, cand1, 1.0);
        first.set(inst2, cand2, 2.0);
        
        SimilarityMatrix<TestInstance> second = new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(2, 2);
        second.set(inst1, cand1, 3.0);
        second.set(inst1, cand2, 4.0);
        second.set(inst2, cand1, 3.0);
        second.set(inst2, cand2, 4.0);
        
        c.setAggregationType(CombinationType.Average);
        SimilarityMatrix<TestInstance> multiplied = c.match(first, second);
        assertEquals(2.0, multiplied.get(inst1, cand1));
        assertEquals(3.0, multiplied.get(inst1, cand2));
        assertEquals(2.0, multiplied.get(inst2, cand1));
        assertEquals(3.0, multiplied.get(inst2, cand2));
    }
    
    public void testWeightedSum() {
        Combine<TestInstance> c = new Combine<TestInstance>();
        
        SimilarityMatrix<TestInstance> first = new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(2, 2);
        TestInstance inst1 = new TestInstance(1, "aa");
        TestInstance inst2 = new TestInstance(2, "ab");
        TestInstance cand1 = new TestInstance(11, "aa");
        TestInstance cand2 = new TestInstance(22, "ab");
        
        first.set(inst1, cand1, 1.0);
        first.set(inst1, cand2, 2.0);
        first.set(inst2, cand1, 1.0);
        first.set(inst2, cand2, 2.0);
        
        SimilarityMatrix<TestInstance> second = new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(2, 2);
        second.set(inst1, cand1, 3.0);
        second.set(inst1, cand2, 4.0);
        second.set(inst2, cand1, 3.0);
        second.set(inst2, cand2, 4.0);
        
        c.setAggregationType(CombinationType.WeightedSum);
        c.setFirstWeight(2.0);
        c.setSecondWeight(0.5);
        SimilarityMatrix<TestInstance> multiplied = c.match(first, second);
        assertEquals(3.5, multiplied.get(inst1, cand1));
        assertEquals(6.0, multiplied.get(inst1, cand2));
        assertEquals(3.5, multiplied.get(inst2, cand1));
        assertEquals(6.0, multiplied.get(inst2, cand2));
    }
    
    public void testMultiply() {
        Combine<TestInstance> c = new Combine<TestInstance>();
        
        SimilarityMatrix<TestInstance> first = new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(2, 2);
        TestInstance inst1 = new TestInstance(1, "aa");
        TestInstance inst2 = new TestInstance(2, "ab");
        TestInstance cand1 = new TestInstance(11, "aa");
        TestInstance cand2 = new TestInstance(22, "ab");
        
        first.set(inst1, cand1, 1.0);
        first.set(inst1, cand2, 2.0);
        first.set(inst2, cand1, 1.0);
        first.set(inst2, cand2, 2.0);
        
        SimilarityMatrix<TestInstance> second = new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(2, 2);
        second.set(inst1, cand1, 3.0);
        second.set(inst1, cand2, 4.0);
        second.set(inst2, cand1, 3.0);
        second.set(inst2, cand2, 4.0);
        
        c.setAggregationType(CombinationType.Multiply);
        SimilarityMatrix<TestInstance> multiplied = c.match(first, second);
        assertEquals(3.0, multiplied.get(inst1, cand1));
        assertEquals(8.0, multiplied.get(inst1, cand2));
        assertEquals(3.0, multiplied.get(inst2, cand1));
        assertEquals(8.0, multiplied.get(inst2, cand2));
        
    }
}
