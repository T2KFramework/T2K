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

import com.wcohen.ss.Jaccard;
import com.wcohen.ss.tokens.NGramTokenizer;
import com.wcohen.ss.tokens.SimpleTokenizer;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;


public class JaccardOnNGramsSimilarity extends SimilarityFunction<String> {

    private int gramSize = 3;
    
    public JaccardOnNGramsSimilarity(int n) {
        gramSize = n;
    }
    
    @Override
    public Double calculate(String first, String second) {
        if(first == null || second == null) {
            return null;
        }
        
        NGramTokenizer tok = new NGramTokenizer(gramSize, gramSize, false, SimpleTokenizer.DEFAULT_TOKENIZER);
        Jaccard j = new Jaccard(tok);
        return j.score(first, second);
    }

}
