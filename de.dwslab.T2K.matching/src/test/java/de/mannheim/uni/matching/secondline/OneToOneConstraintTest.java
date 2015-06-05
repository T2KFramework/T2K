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
import de.dwslab.T2K.matching.secondline.OneToOneConstraint;
import de.dwslab.T2K.similarity.matrix.ArrayBasedSimilarityMatrixFactory;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.mannheim.uni.matching.TestInstance;
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
