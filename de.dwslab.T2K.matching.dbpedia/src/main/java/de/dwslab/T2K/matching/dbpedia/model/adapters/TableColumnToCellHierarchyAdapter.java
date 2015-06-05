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
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableCellCache;
import de.dwslab.T2K.matching.dbpedia.model.TableColumnCache;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;

public class TableColumnToCellHierarchyAdapter extends MatchingHierarchyAdapater<TableColumn, TableCell> {

	@Override
	public Collection<TableCell> getParts(TableColumn instance) {

	    TableColumnCache colCache = TableColumnCache.get();
	    
        Collection<TableCell> cells = colCache.get(instance);
        
	    if(cells==null) {
	    
	        synchronized (instance) {
                
	            if(cells==null) {
	                
	                HashSet<TableCell> cellsTmp = new HashSet<TableCell>(
	                        instance.getValues().size());

	                int colIndex = instance.getTable().getColumns().indexOf(instance);

	                Table t = instance.getTable();
	                TableCellCache cache = TableCellCache.get();

	                for (Integer key : instance.getValues().keySet()) {

	                    TableCell c = cache.getOrCreate(t, key, colIndex);
	                    cellsTmp.add(c);

	                }

	                colCache.set(instance, cellsTmp);
	                
	                
	                cells = cellsTmp;
	            }
	            
            }

	    }
		
		return cells;
	}

}
