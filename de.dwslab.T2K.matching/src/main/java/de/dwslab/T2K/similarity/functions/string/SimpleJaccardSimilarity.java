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
package de.dwslab.T2K.similarity.functions.string;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.utils.query.Q;

/**
 * Calculates the Jaccard similarity of two strings based on word tokens
 * @author Oliver
 *
 */
public class SimpleJaccardSimilarity extends SimilarityFunction<String> {

    @Override
    public Double calculate(String first, String second) {
        if(first == null || second == null) {
            return null;
        }
        
        List<String> tok1 = Arrays.asList(first.toLowerCase().split(" "));
        List<String> tok2 = Arrays.asList(second.toLowerCase().split(" "));
        
        Collection<String> intersection = Q.intersection(tok1,  tok2);
        Collection<String> union = Q.union(tok1, tok2);
        
        return (double)intersection.size() / (double)union.size();
    }
    
    @Override
    public List<String> createSignature(String value) {
        List<String> sig = new LinkedList<>();
        
        for(String t : value.split(" ")) {
            sig.add(t);
        }
        
        return sig;
    }

}
