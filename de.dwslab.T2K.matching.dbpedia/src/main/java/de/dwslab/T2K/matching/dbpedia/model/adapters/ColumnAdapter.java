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
package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.tableprocessor.model.TableColumn;


/**
 *
 * @author domi
 */
public class ColumnAdapter extends EvaluationAdapter<TableColumn>{

    @Override
    public Object getUniqueIdentifier(TableColumn instance) {        
        int index = -1;
        for(int i=0; i< instance.getTable().getColumns().size();i++) {
            if(instance.equals(instance.getTable().getColumns().get(i))) {
                index = i;
                break;
            }
        }
        return index;
        //return instance.getHeader().toLowerCase().replace(".", "");
    }
    
}
