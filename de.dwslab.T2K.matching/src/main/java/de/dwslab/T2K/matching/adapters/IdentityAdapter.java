package de.dwslab.T2K.matching.adapters;

import de.dwslab.T2K.matching.MatchingAdapter;

/**
 * A matching adapter that returns the instance that was passed as the value to be matched
 * @author Oliver
 *
 * @param <T>
 */
public class IdentityAdapter<T> extends MatchingAdapter<T> {

    @Override
    public Object getLabel(T instance) {
        return instance;
    }
    
    @Override
    public Object getTokens(T instance) {
        return instance;
    }

    @Override
    public Object getType(T instance) {
        return instance.getClass();
    }

}
