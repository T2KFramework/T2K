package de.dwslab.T2K.similarity.functions.set;

import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;

/**
 * Set-based similarity function that returns the maximum similarity between any two elements of both sets
 * @author Oliver
 *
 * @param <T>
 */
public class MaxSimilarity<T> extends ComplexSetSimilarity<T> {

    @Override
    protected Double aggregateSimilarity(SimilarityMatrix<T> matrix) {
        return matrix.getMaxValue();
    }

}
