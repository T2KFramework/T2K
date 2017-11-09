package de.dwslab.T2K.similarity.functions.set;

import com.wcohen.ss.tokens.SimpleTokenizer;

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.secondline.ConflictResolution;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;

public class GeneralisedJaccard<T extends Comparable<T>> extends ComplexSetSimilarity<T> {

    private double innerThreshold = 0.0;
    public double getInnerThreshold() {
        return innerThreshold;
    }
    public void setInnerThreshold(double innerThreshold) {
        this.innerThreshold = innerThreshold;
    }
    
    @Override
    protected Double aggregateSimilarity(SimilarityMatrix<T> matrix) {

        matrix.prune(getInnerThreshold());
        
        double firstLength = matrix.getFirstDimension().size();
        double secondLength = matrix.getSecondDimension().size();
        
        matrix = Matcher.selectBestStableCandidateForEachInstance(true, matrix);
        
        double fuzzyMatching = matrix.getSum();
        
        return fuzzyMatching / (firstLength + secondLength - fuzzyMatching);
    }

}
