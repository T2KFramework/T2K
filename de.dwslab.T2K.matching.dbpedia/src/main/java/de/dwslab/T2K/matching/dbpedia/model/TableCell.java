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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.dwslab.T2K.tableprocessor.model.Table;

public class TableCell implements Comparable<TableCell> {

    private Table table;
    private int rowIndex;
    private int columnIndex;
    private int hashCode;

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

    public int getColumnIndex() {
        return columnIndex;
    }

    protected void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public TableCell(Table t, int row, int column) {
        setTable(t);
        setRowIndex(row);
        setColumnIndex(column);
        hashCode = new HashCodeBuilder().append(getTable().hashCode())
                .append(getRowIndex()).append(getColumnIndex()).toHashCode();
    }

    public Object getValue() {
        return getTable().getColumns().get(getColumnIndex()).getValues()
                .get(getRowIndex());
    }

    public Object getType() {
        return getTable().getColumns().get(getColumnIndex()).getDataType();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null) {
            return false;
        } else {
            return obj.hashCode() == hashCode();
        }
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return "" + getValue();
    }

    public int compareTo(TableCell o) {
        if(o==null) {
            return -1;
        }
        
        String me = String.format("%s.%s.%s", getTable().getHeader(), getColumnIndex(), getRowIndex());
        String other = String.format("%s.%s.%s", o.getTable().getHeader(), o.getColumnIndex(), o.getRowIndex());
        
        return me.compareTo(other);
    }

}
