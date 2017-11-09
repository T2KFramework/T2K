package de.dwslab.T2K.matching.blocking;

import java.util.Collection;

/**
 * Blocking that returns all possible candidates (= no blocking)
 * @author Oliver
 *
 * @param <T>
 */
public class IdentityBlocking<T> extends Blocking<T> {

	public Collection<T> getCandidates(T instance,
			Collection<T> candidates) {
		return candidates;
	}

}
