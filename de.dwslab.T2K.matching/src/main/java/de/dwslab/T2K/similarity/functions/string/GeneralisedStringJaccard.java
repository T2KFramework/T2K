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

import com.wcohen.ss.api.Token;
import com.wcohen.ss.tokens.SimpleTokenizer;

import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.GeneralisedJaccard;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrixFactory;

public class GeneralisedStringJaccard extends SimilarityFunction<String> {

    private SimilarityFunction<String> innerFunction;
    public SimilarityFunction<String> getInnerFunction() {
        return innerFunction;
    }
    public void setInnerFunction(SimilarityFunction<String> innerFunction) {
        this.innerFunction = innerFunction;
    }
    
    private double innerThreshold;
    public double getInnerThreshold() {
        return innerThreshold;
    }
    public void setInnerThreshold(double innerThreshold) {
        this.innerThreshold = innerThreshold;
    }
    
    private double JaccardThreshold;
    public double getJaccardThreshold() {
        return JaccardThreshold;
    }
    public void setJaccardThreshold(double jaccardThreshold) {
        JaccardThreshold = jaccardThreshold;
    }
    
    public GeneralisedStringJaccard(SimilarityFunction<String> innerSimilarityFunction, double innerSimilarityThreshold, double jaccardThreshold) {
        setInnerFunction(innerSimilarityFunction);
        setInnerThreshold(innerSimilarityThreshold);
        setJaccardThreshold(jaccardThreshold);
    }
    
    @Override
    public Double calculate(String first, String second) {
        
        // split strings into tokens
        SimpleTokenizer tok = new SimpleTokenizer(true, true);
        
        List<String> f = new LinkedList<>();
        List<String> s = new LinkedList<>();
        
        if(first!=null) {
            for(Token t : tok.tokenize(first)) {
                f.add(t.getValue());
            }
        }
        
        if(second!=null) {
            for(Token t : tok.tokenize(second)) {
                s.add(t.getValue());
            }
        }
        
        // run Set-based similarity function
        GeneralisedJaccard<String> j = new GeneralisedJaccard<>();
        //j.getMatcher().setSimilarityMatrixFactory(new ArrayBasedSimilarityMatrixFactory());
        j.getMatcher().setSimilarityMatrixFactory(new SparseSimilarityMatrixFactory());
        j.setInnerThreshold(getInnerThreshold());
        double sim = j.calculate(f, s, innerFunction);
        
        return sim >= getJaccardThreshold() ? sim : 0.0;
    }

    @Override
    public List<String> createSignature(String value) {
        List<String> sig = new LinkedList<>();
        
        if(value!=null) {
            for(Token t : new SimpleTokenizer(true, true).tokenize(value)) {
                sig.add(t.getValue());
            }
        }
        
        return sig;
    }

}
