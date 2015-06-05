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
package de.dwslab.T2K.similarity.functions.string;

import java.util.LinkedList;
import java.util.List;

import com.wcohen.ss.Jaccard;
import com.wcohen.ss.api.Token;
import com.wcohen.ss.tokens.SimpleTokenizer;

import de.dwslab.T2K.similarity.functions.SimilarityFunction;

/**
 * Calculates the Jaccard similarity of two strings based on word tokens
 * @author Oliver
 *
 */
public class JaccardSimilarity extends SimilarityFunction<String> {

    @Override
    public Double calculate(String first, String second) {
        if(first == null || second == null) {
            return null;
        }
        
        Jaccard j = new Jaccard(new SimpleTokenizer(true, true));
        return j.score(first, second);
    }
    
    @Override
    public List<String> createSignature(String value) {
        List<String> sig = new LinkedList<>();
        
        for(Token t : new SimpleTokenizer(true, true).tokenize(value)) {
            sig.add(t.getValue());
        }
        
        return sig;
    }

}
