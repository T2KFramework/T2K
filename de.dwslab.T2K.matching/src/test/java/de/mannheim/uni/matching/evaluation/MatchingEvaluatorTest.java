package de.mannheim.uni.matching.evaluation;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.mannheim.uni.matching.TestInstance;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.evaluation.MatchingEvaluator;
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
