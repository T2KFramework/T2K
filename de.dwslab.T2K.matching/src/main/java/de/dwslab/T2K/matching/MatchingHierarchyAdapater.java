package de.dwslab.T2K.matching;

import java.util.Collection;

/**
 * Adapter that returns the parts of a given composite
 * @author Oliver
 *
 * @param <T>
 * @param <U>
 */
public abstract class MatchingHierarchyAdapater<T, U> {

	public abstract Collection<U> getParts(T instance);
	
	public ValueRange getValueRange(T instance, U value) {
	    return new ValueRange(null, null);
	}
	
}
