package de.dwslab.T2K.matching.similarity.signatures;

import java.util.Collection;
import java.util.List;

import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.utils.data.Pair;

public class JaccardPrefixFiltering<T extends Comparable<T>> extends PrefixFiltering<T> {

    @Override
    protected int getPrefixLength(Collection<T> signature, double threshold,
            SimilarityFunction<T> sim) {
        return signature.size() - (int)Math.ceil(threshold * signature.size()) + 1;
    }

}
