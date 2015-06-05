/**
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of
							Mannheim (t2k@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
