package de.dwslab.T2K.matching.dbpedia.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.dwslab.T2K.tableprocessor.model.Table;

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
