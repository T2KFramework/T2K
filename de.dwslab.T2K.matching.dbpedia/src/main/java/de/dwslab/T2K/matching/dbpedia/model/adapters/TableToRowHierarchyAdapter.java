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
import java.util.HashSet;

import de.dwslab.T2K.matching.MatchingHierarchyAdapater;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.TableRowCache;
import de.dwslab.T2K.tableprocessor.model.Table;

public class TableToRowHierarchyAdapter extends
        MatchingHierarchyAdapater<Table, TableRow> {

    @Override
    public Collection<TableRow> getParts(Table instance) {
        int numRows = 0;

        if (instance.getKey() != null) {
            
            TableRowCache cache = TableRowCache.get();
            
            Collection<TableRow> rows = cache.get(instance);

            if(rows==null) {
                
                synchronized (instance) {
                    if(rows == null) {
                        rows = new HashSet<TableRow>(numRows);
                        
                        //for (int i = 0; i < numRows; i++) {
                        for(Integer i : instance.getKey().getValues().keySet()) {
                            TableRow row = new TableRow(instance, i,instance.getNumHeaderRows()+i);
                            
                            rows.add(row);
                        }
                        
                        cache.set(instance, rows);
                    }
                }
            }

            return rows;
        } else {
            return new HashSet<TableRow>();
        }
    }

}
