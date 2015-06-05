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
package de.mannheim.uni.matching.firstline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import de.dwslab.T2K.matching.firstline.ValueBasedMatcher;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.functions.string.LevenshteinSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.mannheim.uni.matching.TestEntity;
import de.mannheim.uni.matching.TestEntityHierarchyAdapter;
import de.mannheim.uni.matching.TestInstance;
import de.mannheim.uni.matching.TestInstanceMatchingAdapter;
import de.mannheim.uni.matching.TestInstanceMatchingAdapterWithList;
import junit.framework.TestCase;

public class TestValueBasedMatcher extends TestCase {

	public void testMatch() {
		ValueBasedMatcher<TestEntity, TestInstance> m = new ValueBasedMatcher<TestEntity, TestInstance>();
		m.setSimilarityMeasure(new StringSimilarityMeasure(new LevenshteinSimilarity(), null));
		
		m.setRunInParallel(true);
		
		TestInstanceMatchingAdapter adapter = new TestInstanceMatchingAdapter();
		TestEntityHierarchyAdapter hierarchy = new TestEntityHierarchyAdapter();
		
		Collection<TestEntity> instancesToMatch = new LinkedList<TestEntity>();
		Collection<TestEntity> candidates = new LinkedList<TestEntity>();
		
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
		
		instancesToMatch.add(inst1);
		instancesToMatch.add(inst2);
		candidates.add(cand1);
		candidates.add(cand2);
		
		SimilarityMatrix<TestInstance> similarity = m.match(instancesToMatch, candidates, hierarchy, adapter);
		
		assertEquals(1.0, similarity.get(inst11, cand11));
		assertEquals(0.5, similarity.get(inst11, cand12));
		assertEquals(0.5, similarity.get(inst11, cand21));
		assertEquals(0.0, similarity.get(inst11, cand22));
		
		assertEquals(0.5, similarity.get(inst12, cand11));
		assertEquals(1.0, similarity.get(inst12, cand12));
		assertEquals(0.0, similarity.get(inst12, cand21));
		assertEquals(0.5, similarity.get(inst12, cand22));
		
		assertEquals(0.5, similarity.get(inst21, cand11));
		assertEquals(0.0, similarity.get(inst21, cand12));
		assertEquals(1.0, similarity.get(inst21, cand21));
		assertEquals(0.5, similarity.get(inst21, cand22));
		
		assertEquals(0.0, similarity.get(inst22, cand11));
		assertEquals(0.5, similarity.get(inst22, cand12));
		assertEquals(0.5, similarity.get(inst22, cand21));
		assertEquals(1.0, similarity.get(inst22, cand22));
	}
	
	public void testMatchWithList() {
	       ValueBasedMatcher<TestEntity, TestInstance> m = new ValueBasedMatcher<TestEntity, TestInstance>();
	        m.setSimilarityMeasure(new StringSimilarityMeasure(new LevenshteinSimilarity(), new MaxSimilarity<TestInstance>()));
	        
	        TestInstanceMatchingAdapterWithList adapter = new TestInstanceMatchingAdapterWithList();
	        TestEntityHierarchyAdapter hierarchy = new TestEntityHierarchyAdapter();
	        
	        Collection<TestEntity> instancesToMatch = new LinkedList<TestEntity>();
	        Collection<TestEntity> candidates = new LinkedList<TestEntity>();
	        
	        /*
	         * instances:
	         * 1   aa      ab      {aa111|aa112}
	         * 
	         * candidates:
	         * 11  aa      ab      {aa111|aa110}
	         * 
	         */
	        
	        TestEntity inst1 = new TestEntity(1);
	        TestInstance inst11 = new TestInstance(11, "aa");
	        ArrayList<String> list11 = new ArrayList<String>();
	        list11.add("aa111");
	        list11.add("aa112");
	        inst11.setList(list11);
	        inst1.getParts().add(inst11);
	        
	        TestEntity cand1 = new TestEntity(11);
	        TestInstance cand11 = new TestInstance(111, "aa");
	        ArrayList<String> listc11 = new ArrayList<String>();
	        listc11.add("aa111");
	        listc11.add("aa110");
	        cand11.setList(listc11);
	        cand1.getParts().add(cand11);
	        
	        instancesToMatch.add(inst1);
	        candidates.add(cand1);
	        
	        SimilarityMatrix<TestInstance> similarity = m.match(instancesToMatch, candidates, hierarchy, adapter);
	        
	        assertEquals(1.0, similarity.get(inst11, cand11));
	}

}
