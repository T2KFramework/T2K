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
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
