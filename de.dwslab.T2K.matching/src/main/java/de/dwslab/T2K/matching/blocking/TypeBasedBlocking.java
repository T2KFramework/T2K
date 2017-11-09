package de.dwslab.T2K.matching.blocking;

import java.util.Collection;

import de.dwslab.T2K.matching.MatchingAdapter;
import java.util.ArrayList;

/**
 * A blocking based on the types of the instances as provided by the MatchingAdapter.
 * Only instances with the same type are compared.
 * @author Oliver
 *
 * @param <T>
 */
public class TypeBasedBlocking<T> extends Blocking<T> {
	
	private MatchingAdapter<T> adapter;
	protected MatchingAdapter<T> getAdapter() {
		return adapter;
	}
	protected void setAdapter(MatchingAdapter<T> adapter) {
		this.adapter = adapter;
	}
	
	public TypeBasedBlocking(MatchingAdapter<T> adapter)
	{
		setAdapter(adapter);
	}
	
	public Collection<T> getCandidates(T instance, Collection<T> candidates) {
            Collection<T> results = new ArrayList<T>(candidates.size());
            
            for(T cand : candidates) {
                if(instance==null || cand==null) {
                    continue;
                }
                
                Object instanceType = getAdapter().getType(instance);
                Object candidateType = getAdapter().getType(cand);
                
                if(instanceType == candidateType)
                {
                    results.add(cand);
                }
            }

            return results;
	}
}
