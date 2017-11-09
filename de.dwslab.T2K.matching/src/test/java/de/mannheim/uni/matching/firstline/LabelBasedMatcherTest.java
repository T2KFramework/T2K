package de.mannheim.uni.matching.firstline;

import de.dwslab.T2K.matching.firstline.LabelBasedMatcher;
import java.util.Collection;
import java.util.LinkedList;

import de.mannheim.uni.matching.TestInstance;
import de.mannheim.uni.matching.TestInstanceMatchingAdapter;
import de.dwslab.T2K.similarity.functions.string.LevenshteinSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
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
