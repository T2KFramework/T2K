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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.dwslab.T2K.tableprocessor.model.Table;

public class TableRow implements Comparable<TableRow> {

    private Table table;
    private int rowIndex;
    private int hashCode;
    private int rowIndexInFile;

    private Collection<TableCell> cells;
    
    public Table getTable() {
        return table;
    }

    protected void setTable(Table table) {
        this.table = table;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    protected void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public TableRow(Table t, int row, int rowIndexInFile) {
        setTable(t);
        setRowIndex(row);
        setRowIndexInFile(rowIndexInFile);
        hashCode = new HashCodeBuilder().append(getTable().hashCode())
                .append(getRowIndex()).toHashCode();
    }

    public Object getKey() {
        return getTable().getKey().getValues().get(getRowIndex());
    }

    public TableCell getKeyCell() {
        for(TableCell c : getCells()) {
            if(c!=null && getTable()!=null) {
                if(c.getColumnIndex() == getTable().getKeyIndex()) {
                    return c;
                }
            }
        }
        return null;
    }
    
    public Collection<TableCell> getCells() {
        // this method is called very frequently and in parallel during value-based matching
        if(cells==null) {
            
            // so we must be careful with blocking
            // hence we first check if there is a cached version, and only if not, we synchronise and create the collection
            synchronized (this) {
            
                // re-check: if two threads checked the first condition while cells was null, both want to create it, but we allow it only once
                if(cells==null) {
                    
                    Set<TableCell> cellsTmp = new HashSet<TableCell>(getTable().getColumns().size());
                    
                    TableCellCache cache = TableCellCache.get();
                    
                    for (int i = 0; i < getTable().getColumns().size(); i++) {
                        if (getTable().getColumns().get(i).getValues().containsKey(getRowIndex())) {
                            TableCell cell = cache
                                    .getOrCreate(getTable(), getRowIndex(), i);
                            cellsTmp.add(cell);
                        }
                    }
                    
                    // do not set cells before the loop has ended as otherwise another thread could return the set while it's being filled
                    cells = cellsTmp;
                }
                
            } // synchronized
            
        }

        return cells;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return "" + getKey();
    }

    public Object getURI() {
        return getTable().getColumns().get(0).getValues().get(getRowIndex());
    }

    public int compareTo(TableRow o) {
        
        String me = getTable().getHeader() + getKey() + "";
        String other = o.getTable().getHeader() + o.getKey() + "";
        
        return me.compareTo(other);
    }

    /**
     * @return the rowIndexInFile
     */
    public int getRowIndexInFile() {
        return rowIndexInFile;
    }

    /**
     * @param rowIndexInFile the rowIndexInFile to set
     */
    public void setRowIndexInFile(int rowIndexInFile) {
        this.rowIndexInFile = rowIndexInFile;
    }
}
