package de.dwslab.T2K.matching.blocking;

import java.util.Collection;

/**
 * super class for all blocking implementations
 * @author Oliver
 *
 * @param <T>
 */
public abstract class Blocking<T> {

    public abstract Collection<T> getCandidates(T instance, Collection<T> candidates);
    
}
