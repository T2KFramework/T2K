package de.dwslab.T2K.matching.dbpedia.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.dwslab.T2K.tableprocessor.model.Table;

public class TableRowCache {

    private Map<Table, Collection<TableRow>> cache;
    
    private static TableRowCache singleton = new TableRowCache();
    
    public static TableRowCache get() {
//        if(singleton==null) {
//            singleton = new TableRowCache();
//        }
        return singleton;
    }
    
    protected TableRowCache() {
        cache = new ConcurrentHashMap<Table, Collection<TableRow>>(10, 0.9f, 1);
    }
    
    public Collection<TableRow> get(Table t) {
        return cache.get(t);
    }
    
    public void set(Table t, Collection<TableRow> rows) {
        cache.put(t, rows);
    }
    
    public void removeTable(Table t) {
        cache.remove(t);
    }
    
}
