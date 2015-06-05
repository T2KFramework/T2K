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
package de.dwslab.T2K.matching.similarity.signatures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wcohen.ss.api.Token;
import com.wcohen.ss.api.Tokenizer;
import com.wcohen.ss.tokens.NGramTokenizer;
import com.wcohen.ss.tokens.SimpleTokenizer;

public class NGramsSignatureGenerator extends SignatureGenerator<String> {

    private int n;
    public int getN() {
        return n;
    }
    public void setN(int n) {
        this.n = n;
    }
    
    public NGramsSignatureGenerator(int n) {
        setN(n);
    }
    
    @Override
    public List<String> createSignatures(String value) {
        Tokenizer tok = new NGramTokenizer(getN(), getN(), false, new SimpleTokenizer(false, false));
        List<String> tokens = new ArrayList<>();
        for(Token t : tok.tokenize(value)) {
            if(!t.getValue().contains("^")) {
                tokens.add(t.getValue());
            }
        }
        Collections.sort(tokens);
        return tokens;
    }
    
}
