package de.dwslab.T2K.matching.firstline;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import de.dwslab.T2K.matching.AbstractMatcher;
import de.dwslab.T2K.matching.blocking.Blocking;
import de.dwslab.T2K.matching.blocking.IdentityBlocking;
import de.dwslab.T2K.similarity.matrix.FastSparseSimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrixFactory;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrixFactory;
import de.dwslab.T2K.similarity.measures.SimilarityMeasure;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.concurrent.Task;
import de.dwslab.T2K.utils.data.Triple;

/**
 * super class for all first-line matchers (those that take data as input and output a similarity matrix)
 * @author Oliver
 *
 * @param <T>
 */
public class FirstLineMatcher<T> extends AbstractMatcher {

    private double similarityThreshold = 0.0;
    
    /**
     * returns the minimum similarity threshold
     * @return
     */
    public double getSimilarityThreshold() {
        return similarityThreshold;
    }
    
    /**
     * sets the minimum similarity threshold
     * @param similarityThreshold
     */
    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
    
	private Blocking<T> blocking;
	/**
	 * returns the blocking instance that is used during matching
	 */
	public Blocking<T> getBlocking() {
		return blocking;
	}
	/**
	 * sets the blocking instance that is used during matching
	 */
	public void setBlocking(Blocking<T> blocking) {
		this.blocking = blocking;
	}
	
	SimilarityMeasure measure;
	/**
	 * returns the similarity measure instance that is used during matching
	 */
	public SimilarityMeasure getSimilarityMeasure() {
		return measure;
	}
	/**
	 * sets the similarity measure instance that is used during matching
	 */
	public void setSimilarityMeasure(SimilarityMeasure measure) {
		this.measure = measure;
	}
	
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
	
	public FirstLineMatcher()
	{
		setBlocking(new IdentityBlocking<T>());
		setSimilarityMatrixFactory(new SparseSimilarityMatrixFactory());
	}
}
