package de.dwslab.T2K.similarity.functions.set;

import java.util.Collection;

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.MatchingPair;
import de.dwslab.T2K.matching.adapters.IdentityAdapter;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcher;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.measures.SimpleSimilarityMeasure;

/**
 * super class for set-based similarity functions that compare the elements of the sets using an inner similarity function
 * @author Oliver
 *
 * @param <T>
 */
public abstract class ComplexSetSimilarity<T> {

    public Double calculate(Collection<T> first, Collection<T> second, SimilarityFunction<T> innerFunction) {
        SimilarityMatrix<T> matrix = calculateSimilarity(first, second, innerFunction);
        
        return aggregateSimilarity(matrix);
    }

    private LabelBasedMatcher<T> lbl = new LabelBasedMatcher<T>();
    public LabelBasedMatcher<T> getMatcher() {
        return lbl;
    }
    
    protected SimilarityMatrix<T> calculateSimilarity(Collection<T> first,
            Collection<T> second, SimilarityFunction<T> function) {
        
        lbl.setRunInParallel(true);
        lbl.setSimilarityMeasure(new SimpleSimilarityMeasure<T>(function, null));
        SimilarityMatrix<T> sim = lbl.match(first, second, new IdentityAdapter<T>());
        
        return sim;
    }
    
    protected abstract Double aggregateSimilarity(SimilarityMatrix<T> matrix);
}
