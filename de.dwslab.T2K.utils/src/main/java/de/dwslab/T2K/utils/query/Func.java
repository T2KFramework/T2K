package de.dwslab.T2K.utils.query;

public interface Func<TOut, TIn> {

    TOut invoke(TIn in);
    
}
