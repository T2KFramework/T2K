package de.dwslab.T2K.tableprocessor.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.dwslab.T2K.util.Variables;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a column of a Table. Holds the references to all the values of the column.
 * 
 * No longer compatible to the original SearchJoins Project, but now uses less than one third of the memory previously needed.
 * @author Oliver
 *
 */
public class TableColumn
        implements java.io.Serializable, Comparable<TableColumn> {

    /**
     *
     */
    private static final long serialVersionUID = 7927975071509939076L;

    /**
     * @return the columnStatistic
     */
    public Statistic getColumnStatistic() {
        //if(this.dataType == ColumnDataType.numeric && this.getURI()!=null && this.getURI().contains("dbpedia")) {
        if(this.isKey || (this.getURI()!=null && this.getURI().contains("dbpedia"))) {
            Statistic s = new Statistic();
            TableColumnBuilder tcb = new TableColumnBuilder(this);
            tcb.computeStatistics(s);
            this.setColumnStatistic(s);
        }
        return columnStatistic;
    }

    /**
     * @param columnStatistic the columnStatistic to set
     */
    public void setColumnStatistic(Statistic columnStatistic) {
        this.columnStatistic = columnStatistic;
    }

    /**
     * @return the headerList
     */
    public List getHeaderList() {
        return headerList;
    }

    /**
     * @param headerList the headerList to set
     */
    public void setHeaderList(List headerList) {
        this.headerList = headerList;
    }

    public static enum ColumnDataType {
        numeric, string, coordinate, date, link, bool, unknown, unit, list, reference
    };

    /*
     * Column meta data
     */
    private String header;
    private ColumnDataType dataType;
    private String dataSource;
    private Table table;
    private String URI;
    private boolean isKey;
    private int numRows;
    private Statistic columnStatistic;
    private transient List<String> headerList = new ArrayList<>();
    private transient boolean isWikiID;
    private transient TableColumn equivBefore;
    
    /*
     * Column values
     */
    Map<Integer, Object> values;
    
    
    /**
     * @return the table that contains this column
     */
    public Table getTable() {
        return table;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isKey() {
        return isKey;
    }

    public void setKey(boolean isKey) {
        this.isKey = isKey;
    }

    public Object getHeader() {
        return header;
    }

    public void setHeader(Object header) {
        this.header = header.toString();
    }

    public String getURI() {
        return URI;
    }
    
    protected void setURI(String URI) {
        this.URI = URI;
    }
    
    public ColumnDataType getDataType() {
        return dataType;
    }

    public void setDataType(ColumnDataType dataType) {
        this.dataType = dataType;
    }

    public Map<Integer, Object> getValues() {
        return values;
    }
    
    public int getNumRows() {
        return numRows;
    }
    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public TableColumn(Table table) {
        values = new TreeMap<Integer, Object>();
        header = "";
        dataType = ColumnDataType.unknown;
        this.table = table; 
        this.columnStatistic = new Statistic();
    }

    /**
     * The uniqueness rank of a column is the fraction of unique values minus
     * the fraction of null values.
     *
     * @return
     */
    public double getColumnUniqnessRank() {
        //TODO check if this calculation is still correct!
        int uniqueValues = getNumberOfUniqueValues();
        //System.out.println("unique value: " + this.getTable().getColumns().indexOf(this) + " - " + uniqueValues);
        int totalValues = getValues().size();
        
//        double rank = (double) ((double) (uniqueValues) / (double) totalValues);
//        
//        int numberOfNullValues = totalValues - numRows;
//        
//        rank = rank - ((double) ((double) numberOfNullValues / (double) totalValues));
//
//        return rank;
        
        //the whole time it was like this:
//        double rank1 = (double)uniqueValues / (double)numRows;
//        int numNulls = numRows - totalValues;
//        double rank2 = (double)numNulls / (double)numRows;
        
        double rank1 = (double)uniqueValues / (double)getTable().getTotalNumOfRows();
        int numNulls = getTable().getTotalNumOfRows() - totalValues;
        //System.out.println("num nulls value: " + this.getTable().getColumns().indexOf(this) + " - " + getTable().getTotalNumOfRows() + " - " + totalValues);
        double rank2 = (double)numNulls / (double)getTable().getTotalNumOfRows();        
        return rank1 - rank2;
    }

    public int getNumberOfUniqueValues() {
        
        HashSet<Object> values = new HashSet<>();
        
        for(Object o : getValues().values()) {
            values.add(o);
        }
        
        return values.size();
    }
    
    @Override
    public String toString() {
        return header.toString();
    }
    
    @Override
    public int compareTo(TableColumn o) {
//        int comp = getTable().getHeader().compareTo(o.getTable().getHeader());
//        
//        if(comp==0) {
//            comp = Integer.compare(getTable().getColumns().indexOf(this), o.getTable().getColumns().indexOf(o));
//            
//            if(comp==0 && getURI()!=null && o.getURI()!=null) {
//                comp = getURI().compareTo(o.getURI());
//            }
//        }
//        
//        return comp;
        String me = getTable().getHeader() + getTable().getColumns().indexOf(this) + getURI()+ "";
        String other = o.getTable().getHeader() + o.getTable().getColumns().indexOf(o) + o.getURI()+  "";
        return me.compareTo(other);
    }

//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 29 * hash + Objects.hashCode(this.URI);
//        return hash;
//    }
//    
//    @Override
//    public boolean equals(Object obj) {
//        TableColumn tc = (TableColumn)obj;
//        return (tc.getURI() == null ? this.getURI() == null : tc.getURI().equals(this.getURI()));
//    }
    
//  too slow...
//    @Override
//    public int hashCode() {
//        return new HashCodeBuilder().append(dataSource.hashCode()).append(header.hashCode()).append(table.hashCode()).append(table.getColumns().indexOf(this)).toHashCode();
//    }
//    
//    @Override
//    public boolean equals(Object obj) {
//        return hashCode()==obj.hashCode();
//    }

    /**
     * @return the equivBefore
     */
    public TableColumn getEquivBefore() {
        return equivBefore;
    }

    /**
     * @param equivBefore the equivBefore to set
     */
    public void setEquivBefore(TableColumn equivBefore) {
        this.equivBefore = equivBefore;
    }
}
