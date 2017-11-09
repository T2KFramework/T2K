package de.dwslab.T2K.matching.dbpedia.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;

public class TableColumnCache {

    private Map<TableColumn, Collection<TableCell>> cache;
    
    private static TableColumnCache singleton = new TableColumnCache();
    
    public static TableColumnCache get() {
//        if(singleton==null) {
//            singleton = new TableColumnCache();
//        }
        
        return singleton;
    }
    
    protected TableColumnCache() {
        cache = new ConcurrentHashMap<TableColumn, Collection<TableCell>>();
    }
    
    public void set(TableColumn column, Collection<TableCell> cells) {
        cache.put(column, cells);
    }
    
    public Collection<TableCell> get(TableColumn column) {
        return cache.get(column);
    }
    
    public void removeTable(Table t) {
        for(TableColumn tc : t.getColumns()) {
            cache.remove(tc);
        }
    }
}
