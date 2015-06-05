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
package de.dwslab.T2K.similarity.functions;

import java.util.List;

import de.dwslab.T2K.matching.ValueRange;

/**
 * super class for all similarity functions
 * @author Oliver
 *
 * @param <T>
 */
public abstract class SimilarityFunction<T> {

    /**
     * calculates the similarity of first and second
     * @param first
     * @param second
     * @return
     */
	public abstract Double calculate(T first, T second);
	
	/**
	 * sets the value range for this similarity function 
	 * @param range
	 */
	public void setValueRange(ValueRange range) {
	    
	}
	
	/**
	 * creates the signature of the given value
	 * @param value
	 * @param threshold
	 * @return
	 */
	public List<T> createSignature(T value) {
//	    List<T> l = new ArrayList<>(1);
//	    l.add(value);
//	    return l;
	    return null;
	}
}
