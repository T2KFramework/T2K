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
package de.dwslab.T2K.matching.firstline;


import de.dwslab.T2K.matching.AbstractMatcher;
import de.dwslab.T2K.matching.blocking.Blocking;
import de.dwslab.T2K.matching.blocking.IdentityBlocking;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrixFactory;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrixFactory;
import de.dwslab.T2K.similarity.measures.SimilarityMeasure;

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
