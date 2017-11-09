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
