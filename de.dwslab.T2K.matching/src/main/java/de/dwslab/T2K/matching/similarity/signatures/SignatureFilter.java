package de.dwslab.T2K.matching.similarity.signatures;

import java.util.Collection;
import java.util.List;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.utils.data.Pair;

public abstract class SignatureFilter<T> {

    /***
     * Filters the two lists that are provided and returns only those pairs that reach the threshold with the given similarity function
     * @param values
     * @param candidates
     */
    public abstract Collection<Pair<T, T>> filterCandidates(Collection<T> values, Collection<T> candidates, SimilarityFunction sim, MatchingAdapter<T> adapter, double threshold);
    
}
