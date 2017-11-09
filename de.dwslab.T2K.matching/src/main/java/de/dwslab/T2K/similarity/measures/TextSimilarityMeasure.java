package de.dwslab.T2K.similarity.measures;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.ComplexSetSimilarity;
import java.util.Collection;

/**
 *
 * @author domi
 */
public class TextSimilarityMeasure<T> extends SimilarityMeasure<T> {

    private SimilarityFunction<double[]> similarityFunction;

    public SimilarityFunction<double[]> getSimilarityFunction() {
        return similarityFunction;
    }

    public void setSimilarityFunction(
            SimilarityFunction<double[]> similarityFunction) {
        this.similarityFunction = similarityFunction;
    }

    public TextSimilarityMeasure(){}
    
    public TextSimilarityMeasure(SimilarityFunction<double[]> similarityFunction) {
        setSimilarityFunction(similarityFunction);
    }

    @Override
    protected Double calculateSingleValued(Object first, Object second, ValueRange range) {
        return similarityFunction.calculate((double[])first, (double[])second);
    }

    @Override
    protected Double calculateMultiValued(Collection<Object> first, Collection<Object> second, ValueRange range) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @SuppressWarnings("unchecked")
    @Override
    public Double calculate(T first, T second, MatchingAdapter<T> adapter, ValueRange range) {
        return calculateSingleValued(first, second, range);
    }

}
