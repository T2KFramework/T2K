/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.similarity.measures;

import com.google.common.reflect.TypeToken;
import java.util.Collection;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.ComplexSetSimilarity;
import de.dwslab.T2K.similarity.functions.set.SetSimilarity;
import de.dwslab.T2K.utils.data.Pair;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author domi
 */
public class SimpleSimilarityMeasure<T> extends SimilarityMeasure<T> {

    private SimilarityFunction<T> similarityFunction;

    public SimilarityFunction<T> getSimilarityFunction() {
        return similarityFunction;
    }

    public void setSimilarityFunction(SimilarityFunction<T> similarityFunction) {
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
    TypeToken<T> tokenOfContainedType;

    public SimpleSimilarityMeasure(SimilarityFunction<T> f, ComplexSetSimilarity s) {
        setSimilarityFunction(f);
        setSetSimilarity(s);
    }


    @Override
    protected Double calculateSingleValued(Object first, Object second, ValueRange range) {
        //System.out.println("sim: " +getSimilarityFunction().toString() + " first " + first.toString() + " second " + second.toString());
        Double value = 0.0;
        try {        
            T object1 = (T) first;
            T object2 = (T) second;
            value = getSimilarityFunction().calculate((T) first, (T) second);
        } catch (Exception e) {
            System.out.println("first: " + first + " --- " + second);
            return 0.0;
        }
        return value;
    }

    @Override
    protected Double calculateMultiValued(Collection<Object> first,
            Collection<Object> second, ValueRange range) {
        return getSetSimilarity().calculate(first, second, getSimilarityFunction());
    }

    @Override
    public Collection<Pair<T, T>> applyFilter(Collection<T> values,
            Collection<T> candidates, MatchingAdapter<T> adapter, double threshold) {
        if (getSignatureFilter() == null) {
            return super.applyFilter(values, candidates, adapter, threshold);
        } else {
            return getSignatureFilter().filterCandidates(values, candidates, getSimilarityFunction(), adapter, threshold);
        }
    }
}
