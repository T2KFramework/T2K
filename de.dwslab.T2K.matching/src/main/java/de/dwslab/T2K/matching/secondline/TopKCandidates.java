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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;

/**
 * keeps only the top K similarity values for each instance (of the first dimension) in the resulting similarity matrix
 * @author Oliver
 *
 */
public class TopKCandidates extends SecondLineMatcher {

    public <T extends Comparable<T>> SimilarityMatrix<T> match(final SimilarityMatrix<T> similarities, final int k) {
        
        final SimilarityMatrix<T> sim = getSimilarityMatrixFactory().createSimilarityMatrix(similarities.getFirstDimension().size(), similarities.getSecondDimension().size());

        try {
            new Parallel<T>(isRunInParallel() ? 0 : 1).foreach(similarities.getFirstDimension(), new Consumer<T>() {

                @Override
                public void execute(T parameter) {
                    setTopKForInstance(parameter, similarities, sim, k);
                }
            }, "TopKCandidates");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return sim;
    }
    
    protected <T extends Comparable<T>> void setTopKForInstance(final T instance, final SimilarityMatrix<T> similarities, SimilarityMatrix<T> sim, int k) {
        // we need the treeset first to put the elements in a stable natural ordering, otherwise the sort is unpredictable for elements with the same score
        //TreeSet<T> matches = new TreeSet<T>();
        //matches.addAll(similarities.getMatches(instance));
        
        // now put the (pre-) ordered elements in a list that we can sort by score
        LinkedList<T> top = new LinkedList<T>();
        //top.addAll(matches);
        top.addAll(similarities.getMatches(instance));
        
        // sort the list
        Collections.sort(top, new Comparator<T>() {

            public int compare(T o1, T o2) {
                int i = -Double.compare(similarities.get(instance, o1), similarities.get(instance, o2));
                
                if(i==0) {
                    return Integer.compare(o1.hashCode(), o2.hashCode());
                } else {
                    return i;
                }
                //return -Double.compare(similarities.get(instance, o1), similarities.get(instance, o2));
            }
        });
        
        k = Math.min(k, top.size());
        
        // and take the top k elements
        synchronized (sim) {
            for(int i = 0; i < k; i++) {
                sim.set(instance, top.get(i), similarities.get(instance, top.get(i)));
            }
        }
    }
    
}
