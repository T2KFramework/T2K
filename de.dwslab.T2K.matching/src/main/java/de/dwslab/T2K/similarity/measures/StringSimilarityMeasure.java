package de.dwslab.T2K.similarity.measures;

import java.util.Collection;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.ComplexSetSimilarity;
import de.dwslab.T2K.utils.data.Pair;

/**
 * A similarity measure for Strings
 * @author Oliver
 *
 */
@SuppressWarnings("rawtypes")
public class StringSimilarityMeasure<T> extends SimilarityMeasure<T> {

    private SimilarityFunction<String> similarityFunction;
    public SimilarityFunction<String> getSimilarityFunction() {
        return similarityFunction;
    }
    public void setSimilarityFunction(
            SimilarityFunction<String> similarityFunction) {
        this.similarityFunction = similarityFunction;
    }

    private ComplexSetSimilarity setSimilarity;
    public ComplexSetSimilarity getSetSimilarity() {
        return setSimilarity;
    }
    public void setSetSimilarity(ComplexSetSimilarity setSimilarity) {
        this.setSimilarity = setSimilarity;
    }
    
    private SignatureFilter<T> signatureFilter;
    public SignatureFilter<T> getSignatureFilter() {
        return signatureFilter;
    }
    public void setSignatureFilter(SignatureFilter<T> signatureFilter) {
        this.signatureFilter = signatureFilter;
    }
    
    public StringSimilarityMeasure() {
    }

    public StringSimilarityMeasure(SimilarityFunction<String> similarityFunction, ComplexSetSimilarity setSimilarity) {
        setSimilarityFunction(similarityFunction);
        setSetSimilarity(setSimilarity);
    }

    @Override
    protected Double calculateSingleValued(Object first, Object second, ValueRange range) {
        return getSimilarityFunction().calculate(first.toString(), second.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Double calculateMultiValued(Collection<Object> first,
            Collection<Object> second, ValueRange range) {
        return getSetSimilarity().calculate(first, second, getSimilarityFunction());
    }

    @Override
    public Collection<Pair<T, T>> applyFilter(Collection<T> values,
            Collection<T> candidates, MatchingAdapter<T> adapter, double threshold) {
        if(getSignatureFilter()==null) {
            return super.applyFilter(values, candidates, adapter, threshold);
        } else {
            return getSignatureFilter().filterCandidates(values, candidates, getSimilarityFunction(), adapter, threshold);
        }
    }
}
