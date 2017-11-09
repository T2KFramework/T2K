package de.dwslab.T2K.similarity.measures;

import java.util.ArrayList;
import java.util.Collection;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.utils.data.Pair;

/**
 * super class for all similarity measures.
 * @author Oliver
 *
 */
public abstract class SimilarityMeasure<T> {

    @SuppressWarnings("unchecked")
    public Double calculate(T first, T second, MatchingAdapter<T> adapter, ValueRange range) {
                
        if (adapter.isMultiValued(first) || adapter.isMultiValued(second)) {
            return calculateMultiValued(adapter.getLabels(first),
                    adapter.getLabels(second), range);
        } else {
            return calculateSingleValued(adapter.getLabel(first),
                    adapter.getLabel(second), range);
        }

    }

    protected abstract Double calculateSingleValued(Object first, Object second, ValueRange range);

    protected abstract Double calculateMultiValued(Collection<Object> first,
            Collection<Object> second, ValueRange range);
    
    public Collection<Pair<T, T>> applyFilter(Collection<T> values, Collection<T> candidates, MatchingAdapter<T> adapter, double threshold) {
        Collection<Pair<T, T>> result = new ArrayList<Pair<T,T>>(values.size() * candidates.size());
        //Collection<Pair<T, T>> result = new LinkedList<Pair<T,T>>();
        
        for(T t : values) {
            for(T t2 : candidates) {
                result.add(new Pair<T, T>(t, t2));
            }
        }
        
        return result;
    }
}
