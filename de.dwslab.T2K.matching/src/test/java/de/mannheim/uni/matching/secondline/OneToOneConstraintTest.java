package de.mannheim.uni.matching.secondline;

import de.dwslab.T2K.matching.secondline.OneToOneConstraint;
import de.mannheim.uni.matching.TestInstance;
import de.dwslab.T2K.matching.secondline.ConflictResolution;
import de.dwslab.T2K.similarity.matrix.ArrayBasedSimilarityMatrixFactory;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import junit.framework.TestCase;

public class OneToOneConstraintTest extends TestCase {

	public void testMatch() {
		OneToOneConstraint m = new OneToOneConstraint(ConflictResolution.Maximum);
		
		SimilarityMatrix<TestInstance> similarities = new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(2, 2);
		TestInstance inst1 = new TestInstance(1, "aa");
		TestInstance inst2 = new TestInstance(2, "ab");
		TestInstance cand1 = new TestInstance(11, "aa");
		TestInstance cand2 = new TestInstance(22, "ab");
		
		similarities.set(inst1, cand1, 1.0);
		similarities.set(inst1, cand2, 0.5);
		similarities.set(inst2, cand1, 0.5);
		similarities.set(inst2, cand2, 1.0);
		
		SimilarityMatrix<TestInstance> oneToOne = m.match(similarities);
		
		assertEquals(1.0, oneToOne.get(inst1, cand1));
		assertEquals(null, oneToOne.get(inst1, cand2));
		assertEquals(null, oneToOne.get(inst2, cand1));
		assertEquals(1.0, oneToOne.get(inst2, cand2));
	}

}
