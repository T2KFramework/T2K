package de.dwslab.T2K.matching.dbpedia.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.dwslab.T2K.tableprocessor.model.Table;

public class TableCell implements Comparable<TableCell> {

    private Table table;
    private int rowIndex;
    private int columnIndex;
    private int hashCode;
    
    private Object value;
    private Object type;

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
        
        value = getTable().getColumns().get(getColumnIndex()).getValues()
                .get(getRowIndex());
        type = getTable().getColumns().get(getColumnIndex()).getDataType();
    }

    public Object getValue() {
//        return getTable().getColumns().get(getColumnIndex()).getValues()
//                .get(getRowIndex());
        return value;
    }

    public Object getType() {
        //return getTable().getColumns().get(getColumnIndex()).getDataType();
        return type;
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
        
//        int comp = getTable().getHeader().compareTo(o.getTable().getHeader());
//        
//        if(comp==0) {
//            comp = Integer.compare(getColumnIndex(),o.getColumnIndex());
//            
//            if(comp==0) {
//                comp = Integer.compare(getRowIndex(), o.getRowIndex());
//            }
//        }
//        
//        return comp;
        
        String me = String.format("%s.%s.%s", getTable().getHeader(), getColumnIndex(), getRowIndex());
        String other = String.format("%s.%s.%s", o.getTable().getHeader(), o.getColumnIndex(), o.getRowIndex());
        
        return me.compareTo(other);
    }

}
