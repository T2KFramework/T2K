/*
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
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
