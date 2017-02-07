/*
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
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
package de.dwslab.T2K.matching.blocking;

import java.util.Collection;

import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;

/**
 * Re-uses a blocking that was produced by a previous matcher by only considering the non-zero pairs of a similarity matrix
 * @author Oliver
 *
 */
public class SecondLineBlocking<T> extends Blocking<T> {

	private SimilarityMatrix<T> similarityMatrix;
	public SimilarityMatrix<T> getSimilarityMatrix() {
		return similarityMatrix;
	}
	public void setSimilarityMatrix(SimilarityMatrix<T> similarityMatrix) {
		this.similarityMatrix = similarityMatrix;
	}
	
	public SecondLineBlocking(SimilarityMatrix<T> similarityMatrix)
	{
		this.similarityMatrix = similarityMatrix;
	}
	
	public Collection<T> getCandidates(T instance, Collection<T> candidates) {
		return getSimilarityMatrix().getMatches(instance);
	}

}
