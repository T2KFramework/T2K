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
