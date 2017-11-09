package de.dwslab.T2K.similarity.functions.set;

import java.util.Collection;

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.math.DoubleSet;

/**
 * The first set (left side) is important here. A similarity of 1 is reached if
 * each element of the first set has a corresponding element in the second set
 * (right side) with an inner similarity of 1
 * 
 * @author Oliver
 * 
 * @param <T>
 */
public class LeftSideCoverage<T extends Comparable<T>> extends
        ComplexSetSimilarity<T> {

    @Override
    protected Double aggregateSimilarity(SimilarityMatrix<T> matrix) {
        if(matrix.getFirstDimension().size()==0 || matrix.getSecondDimension().size()==0) {
            return 0.0;
        }
        
        SimilarityMatrix<T> best = Matcher
                .selectBestStableCandidateForEachInstance(true, matrix);
        Collection<Double> scores = best.getRowSums();

        // best only contains matched pairs, so we have to divide by the
        // dimension of the initial matrix to get the correct average
        return DoubleSet.sum(scores)
                / (double) matrix.getFirstDimension().size();
    }

}
