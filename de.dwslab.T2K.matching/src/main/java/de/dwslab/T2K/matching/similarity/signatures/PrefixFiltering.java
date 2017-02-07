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
package de.dwslab.T2K.matching.similarity.signatures;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.utils.data.Pair;

public abstract class PrefixFiltering<T extends Comparable<T>> extends SignatureFilter<T> {

    protected abstract int getPrefixLength(Collection<T> signature, double threshold, SimilarityFunction<T> sim);
    
    @Override
    public Collection<Pair<T, T>> filterCandidates(Collection<T> values,
            Collection<T> candidates, SimilarityFunction sim, MatchingAdapter<T> adapter, double threshold) {
        Map<Object, Set<T>> index = new HashMap<>();
        
        
        // Generate the signatures for the candidates and add them to inverted index
        for(T t : candidates) {
            
            // candidate could be multi-valued: generate the signatures for all possible values
            for(Object val : adapter.getLabels(t)) {
                List sig = sim.createSignature(val);

                if(sig!=null) {
                    // sort the signatures
                    Collections.sort(sig);
                    
                    // get the prefix
                    //int prefix = sim.getPrefixLength(sig, threshold);
                    int prefix = getPrefixLength(sig, threshold, sim);
                    while(sig.size()>prefix) {
                        sig.remove(sig.size()-1);
                    }
                    
                    for(Object s : sig) {
                        Set<T> instances = index.get(s);
                        
                        if(instances==null) {
                            instances = new HashSet<>();
                            index.put(s, instances);
                        }
                        
                        instances.add(t);
                    }
                }
            }
        }
        
        List<Pair<T,T>> result = new LinkedList<>();
        
        // Generate the signatures for the values and look them up in the inverted index
        for(T t : values) {
            //List sig = sim.createSignature(t);
            
            // value could be multi-valued: generate the signatures for all possible values
            for(Object val : adapter.getLabels(t)) {
                List sig = sim.createSignature(val);
                
                if(sig!=null) {
                    Set<T> matches = new HashSet<>();
                    
                    for(Object s : sig) {
                        Set<T> cand = index.get(s);
                        
                        if(cand!=null) {
                            matches.addAll(cand);
                        }
                    }
                    
                    for(T m : matches) {
                        result.add(new Pair<>(t, m));
                    }
                }
            }
        }
        
        return result;
    }

    
    
}
