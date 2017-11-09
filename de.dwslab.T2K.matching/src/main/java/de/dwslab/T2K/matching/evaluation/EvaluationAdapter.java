package de.dwslab.T2K.matching.evaluation;

/**
 * Provides access to a unique identifier for all instances to be used during evaluation
 * @author Oliver
 *
 * @param <T>
 */
public abstract class EvaluationAdapter<T> {

	public abstract Object getUniqueIdentifier(T instance);
	
}
