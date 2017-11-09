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
                            TableRow row;
                            if(instance.getKey().getValues().keySet().contains(0) && instance.getNumHeaderRows()>0) {
                                row = new TableRow(instance, i,instance.getNumHeaderRows()+i); 
                            }
                            else {
                                row = new TableRow(instance, i,i);
                            }   
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
