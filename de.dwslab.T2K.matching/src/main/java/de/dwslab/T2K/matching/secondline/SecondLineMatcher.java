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
