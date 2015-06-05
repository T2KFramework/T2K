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
