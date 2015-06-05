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
package de.dwslab.T2K.matching.correspondences;

import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;

import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;

/**
 * Generates correspondences based on a similarity matrix
 * @author Oliver
 *
 */
public class CorrespondenceGenerator {

	public <T extends Comparable<T>> Collection<Correspondence<T>> generateCorrespondences(SimilarityMatrix<T> similarities, double similarityThreshold) {
		
		Collection<Correspondence<T>> correspondences = new LinkedList<Correspondence<T>>();
		
        // order all items so that we get consistent results in cases where the score is equal 
        TreeSet<T> dimension = new TreeSet<T>();
        dimension.addAll(similarities.getFirstDimension());
		for(T first : dimension) {
			
		    
	        // order all items so that we get consistent results in cases where the score is equal 
	        TreeSet<T> matches = new TreeSet<T>();
	        matches.addAll(similarities.getMatches(first));
			for(T second : matches) {
				
				double similarity = similarities.get(first, second);
				
				if(similarity>similarityThreshold) {
					
					correspondences.add(new Correspondence<T>(first, second, similarity));
					
				}
				
			}
		}
		
		return correspondences;
	}
	
}
