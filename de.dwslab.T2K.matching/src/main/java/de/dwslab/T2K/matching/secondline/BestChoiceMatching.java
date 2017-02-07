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
package de.dwslab.T2K.matching.secondline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;

/**
 * Greedy approach that selects the best stable match for each instance of the first dimension
 * @author Oliver
 *
 */
public class BestChoiceMatching extends SecondLineMatcher {

    private boolean forceOneToOneMapping = true;
    public boolean isForceOneToOneMapping() {
        return forceOneToOneMapping;
    }
    public void setForceOneToOneMapping(boolean forceOneToOneMapping) {
        this.forceOneToOneMapping = forceOneToOneMapping;
    }
    
    public <T extends Comparable<T>> SimilarityMatrix<T> match(SimilarityMatrix<T> input) {
        
        SimilarityMatrix<T> sim = getSimilarityMatrixFactory().createSimilarityMatrix(input.getFirstDimension().size(), input.getSecondDimension().size());
        
        Set<T> alreadyMatched = new HashSet<T>();
        
        // order all items so that we get consistent results in cases where the score is equal 
        //TreeSet<T> dimension = new TreeSet<T>();
        //dimension.addAll(input.getFirstDimension());
        ArrayList<T>  dimension = new ArrayList<>(input.getFirstDimension());
        Collections.sort(dimension);
        for(T instance : dimension) {
            
            double max = 0.0;
            T best = null;
            //TreeSet<T> matches = new TreeSet<T>();
            //matches.addAll(input.getMatches(instance));
            ArrayList<T> matches = new ArrayList<>(input.getMatches(instance));
            Collections.sort(matches);
            // determine best match
            for(T candidate : matches) {
                
                if(!alreadyMatched.contains(candidate) && input.get(instance, candidate)>max) {
                    max = input.get(instance, candidate);
                    best = candidate;
                }
                
            }
            
            // make sure instance is also the best match for candidate (i.e. is this a stable pair)
            for(T instance2 : input.getFirstDimension()) {
                
                //TODO this line may be a bottleneck (don't know why yet...)
                if(instance2!=instance && !alreadyMatched.contains(instance2) && input.get(instance2, best)!=null && input.get(instance2, best)>max) {
                    best = null;
                    break;
                }
                
            }
            
            // if we found a stable pair
            if(best!=null) {
                sim.set(instance, best, max);
                
                if(isForceOneToOneMapping()) {
                    alreadyMatched.add(instance);
                    alreadyMatched.add(best);
                }
            }
        }
        
        return sim;
    }

}
