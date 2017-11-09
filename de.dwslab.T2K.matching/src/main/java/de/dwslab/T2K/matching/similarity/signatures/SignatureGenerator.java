package de.dwslab.T2K.matching.similarity.signatures;

import java.util.List;

public abstract class SignatureGenerator<T> {

    public abstract List<T> createSignatures(T value);
    
}
