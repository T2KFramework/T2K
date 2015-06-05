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
package de.mannheim.uni.matching.evaluation;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.evaluation.MatchingEvaluator;
import de.mannheim.uni.matching.TestInstance;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class MatchingEvaluatorTest extends TestCase {
	
	class TestInstanceAdapter extends EvaluationAdapter<TestInstance> {

		@Override
		public Object getUniqueIdentifier(TestInstance instance) {
			return instance.getId();
		}
		
	}
	
	public void testEvaluateMatching() {
		
		MatchingEvaluator e = new MatchingEvaluator();
		EvaluationAdapter<TestInstance> a = new TestInstanceAdapter();
		Collection<Correspondence<TestInstance>> correspondences = new LinkedList<Correspondence<TestInstance>>();
		Map<Object, List<Object>> goldStandard = new HashMap<Object, List<Object>>();
		EvaluationResult r = null;
		
		TestInstance left1 = new TestInstance(1);
		TestInstance left2 = new TestInstance(2);
		TestInstance right1 = new TestInstance(11);
		TestInstance right2 = new TestInstance(22);
		List tmp1 = new ArrayList();
                List tmp2 = new ArrayList();
                tmp1.add(11);
                tmp2.add(22);
                
		goldStandard.put(1, tmp1);                
		goldStandard.put(2, tmp2);
		
		/*
		 * Test perfect case
		 */
		System.out.println(goldStandard);
		correspondences.add(new Correspondence<TestInstance>(left1, right1, 1.0));
		correspondences.add(new Correspondence<TestInstance>(left2, right2, 1.0));
		
		r = e.evaluateMatching(correspondences, goldStandard, 2, a, a);
		assertEquals(1.0, r.getPrecision());
		assertEquals(1.0, r.getRecall());
		
		/*
		 * Test 50% case
		 */
		correspondences.clear();
		correspondences.add(new Correspondence<TestInstance>(left1, right1, 1.0));
		correspondences.add(new Correspondence<TestInstance>(left2, right1, 1.0));
		
		r = e.evaluateMatching(correspondences, goldStandard, 2, a, a);
		assertEquals(0.5, r.getPrecision());
		assertEquals(0.5, r.getRecall());
		
		/* 
		 * Test completely wrong case
		 */
		correspondences.clear();
		correspondences.add(new Correspondence<TestInstance>(left1, right2, 1.0));
		correspondences.add(new Correspondence<TestInstance>(left2, right1, 1.0));
		
		r = e.evaluateMatching(correspondences, goldStandard, 2, a, a);
		assertEquals(0.0, r.getPrecision());
		assertEquals(0.0, r.getRecall());
	}

}
