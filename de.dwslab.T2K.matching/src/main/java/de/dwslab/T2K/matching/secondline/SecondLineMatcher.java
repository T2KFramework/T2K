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
package de.dwslab.T2K.matching.secondline;

import de.dwslab.T2K.matching.AbstractMatcher;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrixFactory;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrixFactory;

/**
 * super class for all second-line matchers (those that take similarity matrices as input and output another similarity matrix)
 * @author Oliver
 *
 */
public abstract class SecondLineMatcher extends AbstractMatcher {

	SimilarityMatrixFactory similarityMatrixFactory;
	/**
	 * returns the similarity matrix factory that is used to create the similarity matrix containing the matching result
	 */
	public SimilarityMatrixFactory getSimilarityMatrixFactory() {
		return similarityMatrixFactory;
	}
	/**
	 * sets the similarity matrix factory that is used to create the similarity matrix containing the matching result
	 */
	public void setSimilarityMatrixFactory(SimilarityMatrixFactory factory) {
		similarityMatrixFactory = factory;
	}
	
	public SecondLineMatcher()
	{
		setSimilarityMatrixFactory(new SparseSimilarityMatrixFactory());
	}
	
}
