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
package de.dwslab.T2K.matching.dbpedia.model.adapters;

import java.util.Collection;
import java.util.List;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.utils.query.Func;
import de.dwslab.T2K.utils.query.Q;

public class TableCellMatchingAdapter extends MatchingAdapter<TableCell> {

	@Override
	public Object getLabel(TableCell instance) {
		return instance.getValue();
	}

	@Override
	public Object getType(TableCell instance) {
		return instance.getType();
	}

	@Override
	public boolean isMultiValued(TableCell instance) {
	    return instance.getValue() instanceof List;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public Collection getLabels(TableCell instance) {
	    if(isMultiValued(instance)) {
//	        return (Collection)instance.getValue();
	        //TODO the list should not contain null values, but it does!
	        return Q.where((Collection)instance.getValue(), new Func<Boolean, Object>() {

                @Override
                public Boolean invoke(Object in) {
                    return in != null;
                }
	            
            });
	    } else {
	        return super.getLabels(instance);
	    }
	}
}
