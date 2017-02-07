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
package de.mannheim.uni.matching;

import java.util.Collection;

import de.dwslab.T2K.matching.MatchingAdapter;

public class TestInstanceMatchingAdapterWithList extends MatchingAdapter<TestInstance> {

    @Override
    public Object getLabel(TestInstance instance) {
        return instance.getLabel();
    }

    @Override
    public Object getType(TestInstance instance) {
        return instance.getType();
    }
    
    @Override
    public boolean isMultiValued(TestInstance instance) {
        return true;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Collection getLabels(TestInstance instance) {
        return instance.getList();
    }

    @Override
    public Object getTokens(TestInstance instance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
