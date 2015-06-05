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

import de.dwslab.T2K.matching.secondline.ConflictResolution;
import de.dwslab.T2K.matching.secondline.ParentOneToOneConstraint;
import de.dwslab.T2K.similarity.matrix.ArrayBasedSimilarityMatrixFactory;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.mannheim.uni.matching.TestEntity;
import de.mannheim.uni.matching.TestEntityHierarchyAdapter;
import de.mannheim.uni.matching.TestInstance;
import junit.framework.TestCase;

public class ParentOneToOneConstraintTest extends TestCase {

    public void testMatch() {
        TestEntityHierarchyAdapter hierarchy = new TestEntityHierarchyAdapter();
        
        TestEntity inst1 = new TestEntity(1);
        TestInstance inst11 = new TestInstance(11, "aa");
        TestInstance inst12 = new TestInstance(12, "ab");
        inst1.getParts().add(inst11);
        inst1.getParts().add(inst12);
        
        TestEntity inst2 = new TestEntity(2);
        TestInstance inst21 = new TestInstance(21, "ba");
        TestInstance inst22 = new TestInstance(22, "bb");
        inst2.getParts().add(inst21);
        inst2.getParts().add(inst22);
        
        TestEntity cand1 = new TestEntity(11);
        TestInstance cand11 = new TestInstance(111, "aa");
        TestInstance cand12 = new TestInstance(112, "ab");
        cand1.getParts().add(cand11);
        cand1.getParts().add(cand12);
        
        TestEntity cand2 = new TestEntity(22);
        TestInstance cand21 = new TestInstance(221, "ba");
        TestInstance cand22 = new TestInstance(222, "bb");
        cand2.getParts().add(cand21);
        cand2.getParts().add(cand22);
        
        SimilarityMatrix<TestEntity> entitySimilarities = new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(2, 2);
        entitySimilarities.set(inst1, cand1, 1.0);
        entitySimilarities.set(inst1, cand2, 0.5);
        entitySimilarities.set(inst2, cand1, 1.0/3.0);
        entitySimilarities.set(inst2, cand2, 1.0);
        
        SimilarityMatrix<TestInstance> instanceSimilarities = new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(4, 4);
        instanceSimilarities.set(inst11, cand11, 1.0);
        instanceSimilarities.set(inst11, cand12, 0.5);
        instanceSimilarities.set(inst11, cand21, 0.5);
        instanceSimilarities.set(inst11, cand22, 0.0);
        
        instanceSimilarities.set(inst12, cand11, 0.5);
        instanceSimilarities.set(inst12, cand12, 1.0);
        instanceSimilarities.set(inst12, cand21, 0.0);
        instanceSimilarities.set(inst12, cand22, 0.5);
        
        instanceSimilarities.set(inst21, cand11, 0.5);
        instanceSimilarities.set(inst21, cand12, 0.0);
        instanceSimilarities.set(inst21, cand21, 1.0);
        instanceSimilarities.set(inst21, cand22, 0.5);
        
        instanceSimilarities.set(inst22, cand11, 0.0);
        instanceSimilarities.set(inst22, cand12, 0.5);
        instanceSimilarities.set(inst22, cand21, 0.5);
        instanceSimilarities.set(inst22, cand22, 1.0);
        
        for(int i = 0; i < 1000; i++) {
            ParentOneToOneConstraint<TestEntity, TestInstance> m = new ParentOneToOneConstraint<TestEntity, TestInstance>(ConflictResolution.Maximum);
            SimilarityMatrix<TestInstance> sim = m.match(entitySimilarities, instanceSimilarities, hierarchy);
            
            assertEquals(1.0, sim.get(inst11, cand11));
            assertEquals(null, sim.get(inst11, cand12));
            assertEquals(0.5, sim.get(inst11, cand21));
            assertEquals(null, sim.get(inst11, cand22));
            
            assertEquals(null, sim.get(inst12, cand11));
            assertEquals(1.0, sim.get(inst12, cand12));
            assertEquals(null, sim.get(inst12, cand21));
            assertEquals(0.5, sim.get(inst12, cand22));
            
            assertEquals(0.5, sim.get(inst21, cand11));
            assertEquals(null, sim.get(inst21, cand12));
            assertEquals(1.0, sim.get(inst21, cand21));
            assertEquals(null, sim.get(inst21, cand22));
            
            assertEquals(null, sim.get(inst22, cand11));
            assertEquals(0.5, sim.get(inst22, cand12));
            assertEquals(null, sim.get(inst22, cand21));
            assertEquals(1.0, sim.get(inst22, cand22));
        }
    }
    
}
