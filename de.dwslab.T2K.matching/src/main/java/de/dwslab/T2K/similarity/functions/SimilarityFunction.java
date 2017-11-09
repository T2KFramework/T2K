package de.dwslab.T2K.similarity.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.dwslab.T2K.matching.ValueRange;

/**
 * super class for all similarity functions
 * @author Oliver
 *
 * @param <T>
 */
public abstract class SimilarityFunction<T> {

    /**
     * calculates the similarity of first and second
     * @param first
     * @param second
     * @return
     */
	public abstract Double calculate(T first, T second);
	
	/**
	 * sets the value range for this similarity function 
	 * @param range
	 */
	public void setValueRange(ValueRange range) {
	    
	}
	
	/**
	 * creates the signature of the given value
	 * @param value
	 * @param threshold
	 * @return
	 */
	public List<T> createSignature(T value) {
//	    List<T> l = new ArrayList<>(1);
//	    l.add(value);
//	    return l;
	    return null;
	}
}
