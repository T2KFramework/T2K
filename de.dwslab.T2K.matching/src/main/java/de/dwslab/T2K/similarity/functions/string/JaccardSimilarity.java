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
