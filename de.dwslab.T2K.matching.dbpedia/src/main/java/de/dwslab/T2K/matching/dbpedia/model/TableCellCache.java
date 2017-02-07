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
package de.dwslab.T2K.matching.dbpedia.model;

import de.dwslab.T2K.tableprocessor.model.Table;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TableCellCache {

	/*
	 * Caches the cells generated for candidates, hence there are a few tables
	 * with a few rows each, but always containing all columns So we use a
	 * HashMap for the tables, a HashMap for the rows and an Array for the
	 * columns
	 */

	private Map<Table, Map<Integer, TableCell[]>> cache;

	protected Map<Table, Map<Integer, TableCell[]>> getCache() {
		return cache;
	}

	protected Map<Table, Map<Integer, TableCell[]>> createTableMap() {
		//return new HashMap<Table, Map<Integer, TableCell[]>>();
	    return new ConcurrentHashMap<Table, Map<Integer, TableCell[]>>(10, 0.9f, 1);
	}

	protected Map<Integer, TableCell[]> createRowMap() {
		//return new HashMap<Integer, TableCell[]>();
	    return new ConcurrentHashMap<>(10, 0.9f, 1);
	}

	protected TableCell[] createCellArray(Table t) {
		return new TableCell[t.getColumns().size()];
	}

	protected TableCellCache() {
		cache = createTableMap();
	}

	private static TableCellCache singleton;

	public static TableCellCache get() {
		if (singleton == null) {
			singleton = new TableCellCache();
		}

		return singleton;
	}

	public TableCell get(Table t, int row, int col) {
	    Map<Integer, TableCell[]> rowMap = getCache().get(t);
	    
		if (rowMap!=null) {

		    TableCell[] cellCollection = rowMap.get(row);
		    
			if (cellCollection!=null) {
				return cellCollection[col];
			}
		}

		return null;
	}

	public TableCell[] getCells(Table t, int row) {
        Map<Integer, TableCell[]> rowMap = getCache().get(t);
        
        if (rowMap!=null) {

            TableCell[] cellCollection = rowMap.get(row);
            
            return cellCollection;
        }
        
        return null;
	}
	
	public TableCell getOrCreate(Table t, int row, int col) {

	    Map<Integer, TableCell[]> rowMap = getCache().get(t);
	    
		if (rowMap!=null) {

		    TableCell[] cellCollection = rowMap.get(row);
		    
			if (cellCollection!=null) {
				if (cellCollection[col] != null) {
					return cellCollection[col];
				}

			}
		}

		TableCell c = new TableCell(t, row, col);
		set(c);
		return c;
	}

	public void set(TableCell c) {
	    synchronized (c.getTable()) {
    		Map<Integer, TableCell[]> rowMap = getCache().get(c.getTable());;
    
    		if (rowMap==null) {
    			rowMap = createRowMap();
    			getCache().put(c.getTable(), rowMap);
    		}
    
    		TableCell[] cellCollection = rowMap.get(c.getRowIndex());
    
    		if (cellCollection==null) {
    			cellCollection = createCellArray(c.getTable());
    			rowMap.put(c.getRowIndex(), cellCollection);
    		}
    
    		cellCollection[c.getColumnIndex()] = c;
        }
	}
	
	public void removeTable(Table t) {
	    getCache().remove(t);
	}
}
