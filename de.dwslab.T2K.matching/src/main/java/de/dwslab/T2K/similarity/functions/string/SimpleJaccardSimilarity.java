package de.dwslab.T2K.similarity.functions.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.wcohen.ss.Jaccard;
import com.wcohen.ss.api.Token;
import com.wcohen.ss.tokens.SimpleTokenizer;

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
