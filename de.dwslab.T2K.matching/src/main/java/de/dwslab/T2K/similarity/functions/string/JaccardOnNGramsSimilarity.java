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
