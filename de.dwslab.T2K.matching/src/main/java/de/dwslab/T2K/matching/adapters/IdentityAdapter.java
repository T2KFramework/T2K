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
    public Object getType(T instance) {
        return instance.getClass();
    }
    
    @Override
    public Object getTokens(T instance) {
        return instance;
    }

}
