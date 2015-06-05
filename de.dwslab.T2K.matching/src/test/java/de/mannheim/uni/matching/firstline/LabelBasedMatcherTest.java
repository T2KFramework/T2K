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

import java.util.Collection;
import java.util.LinkedList;

import de.dwslab.T2K.matching.firstline.LabelBasedMatcher;
import de.dwslab.T2K.similarity.functions.string.LevenshteinSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.mannheim.uni.matching.TestInstance;
import de.mannheim.uni.matching.TestInstanceMatchingAdapter;
import junit.framework.TestCase;

public class LabelBasedMatcherTest extends TestCase {
	
	public void testMatch() {
		
		LabelBasedMatcher<TestInstance> m = new LabelBasedMatcher<TestInstance>();
		m.setSimilarityMeasure(new StringSimilarityMeasure(new LevenshteinSimilarity(), null));
		
		Collection<TestInstance> instances = new LinkedList<TestInstance>();
		Collection<TestInstance> candidates = new LinkedList<TestInstance>();
		
		TestInstanceMatchingAdapter adapter = new TestInstanceMatchingAdapter();
		
		TestInstance inst1 = new TestInstance(1, "aa");
		TestInstance inst2 = new TestInstance(2, "ab");
		TestInstance inst3 = new TestInstance(3, "bb");
		instances.add(inst1);
		instances.add(inst2);
		instances.add(inst3);
		
		TestInstance cand1 = new TestInstance(11, "aa");
		TestInstance cand2 = new TestInstance(22, "ab");
		TestInstance cand3 = new TestInstance(33, "bb");
		candidates.add(cand1);
		candidates.add(cand2);
		candidates.add(cand3);
		
		SimilarityMatrix<TestInstance> similarities = m.match(instances, candidates, adapter);
		
		assertEquals(1.0, similarities.get(inst1, cand1));
		assertEquals(0.5, similarities.get(inst1, cand2));
		assertEquals(0.0, similarities.get(inst1, cand3));
		
		assertEquals(1.0, similarities.get(inst2, cand2));
		assertEquals(0.5, similarities.get(inst2, cand1));
		assertEquals(0.5, similarities.get(inst2, cand3));
		
		assertEquals(1.0, similarities.get(inst3, cand3));
		assertEquals(0.5, similarities.get(inst3, cand2));
		assertEquals(0.0, similarities.get(inst3, cand1));
	}

}
