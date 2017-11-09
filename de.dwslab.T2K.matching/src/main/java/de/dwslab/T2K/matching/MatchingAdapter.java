package de.dwslab.T2K.matching;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adapter that returns the value that should be matched from any instance.
 * Supports multi-valued data.
 * @author Oliver
 *
 * @param <T>
 */
public abstract class MatchingAdapter<T> {

	public abstract Object getLabel(T instance);
	public abstract Object getType(T instance);
        public abstract Object getTokens(T instance);
	
    @SuppressWarnings("rawtypes")
    public Collection getLabels(T instance) {
        ArrayList<Object> lst = new ArrayList<Object>(1);
        lst.add(getLabel(instance));
        return lst;
    }

    public boolean isMultiValued(T instance) {
        return false;
    }
}
