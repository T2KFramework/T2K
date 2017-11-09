package de.dwslab.T2K.tableprocessor.model;

import de.dwslab.T2K.tableprocessor.model.json.TableData;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Table implements Serializable, Comparable<Table> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private boolean hasKey;
    private String fullPath;
    private String source;
    private List<TableColumn> columns;
    private String header;
    public int nmNulls;
    private int numHeaderRows;
    private transient int totalNumOfRows;
    private transient TableColumn keyColumn;
    private transient int keyIndex = -1;
    private transient int wikiIDCol = -1;    
    
    private transient String contextTimestamp;
    private transient String contextBeforeTable;
    private transient String contextAfterTable;
    private transient String pageTitle;
    private transient String tableTitle;
    private transient TableMapping mapping;
    private transient TableData data;

    @Override
    public int hashCode() {
        return fullPath.hashCode();
    }

    public int getNmNulls() {
        return nmNulls;
    }

    public void setNmNulls(int nmNulls) {
        this.nmNulls = nmNulls;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    public String getSource() {
        return source;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    public TableColumn getColumn(String header) {
        for (TableColumn c : columns) {
            if (c.getHeader().equals(header)) {
                return c;
            }
        }
        return null;
    }

    public void setColumns(List<TableColumn> columns) {
        this.columns = columns;
    }

    public void addColumn(TableColumn column) {
        this.columns.add(column);
    }

    public void deleteColumn(TableColumn column) {
        this.columns.remove(column);
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        if (header.contains("\\")) {
            header = header.substring(header.lastIndexOf("\\") + 1,
                    header.length());
        }
        if (header.contains("/")) {
            header = header.substring(header.lastIndexOf("/") + 1,
                    header.length());
        }
        this.header = header;
    }

    public String getContextAfterTable() {
        return contextAfterTable;
    }
    public void setContextAfterTable(String contextAfterTable) {
        this.contextAfterTable = contextAfterTable;
    }
    
    public String getContextBeforeTable() {
        return contextBeforeTable;
    }
    public void setContextBeforeTable(String contextBeforeTable) {
        this.contextBeforeTable = contextBeforeTable;
    }
    
    public String getContextTimestamp() {
        return contextTimestamp;
    }
    public void setContextTimestamp(String contextTimestamp) {
        this.contextTimestamp = contextTimestamp;
    }
    
    public Table() {
        columns = new LinkedList<>();
        header = "";
        hasKey = true;
        nmNulls = 0;        
    }

    public boolean isHasKey() {
        return hasKey;
    }

    public StringBuilder printHeader() {
        StringBuilder sb = new StringBuilder();
        for (TableColumn c : getColumns()) {
            sb.append(padString(c.getHeader().toString(), 30));
            sb.append(" | ");
        }
        sb.append("\n");
        return sb;
    }

    public StringBuilder printTypes() {
        StringBuilder sb = new StringBuilder();
        for (TableColumn c : getColumns()) {
            sb.append(padString(c.getDataType().toString(), 30));
            sb.append(" | ");
        }
        sb.append("\n");
        return sb;
    }

    public StringBuilder printRow(int row) {
        StringBuilder sb = new StringBuilder();
        for (TableColumn c : getColumns()) {
            String value = "";

            if (c.getValues().get(row) != null) {
                value = c.getValues().get(row).toString();
            }

            sb.append(padString(value, 30));
            sb.append(" | ");
        }
        sb.append("\n");
        return sb;
    }

    public String printTable() {
        StringBuilder sb = new StringBuilder();

//        for(TableColumn c : getColumns()) {
//            sb.append(padString(c.getHeader(), 30));
//            sb.append(" | ");
//        }
//        sb.append("\n");
        sb.append(printHeader());

//        for(TableColumn c : getColumns()) {
//            sb.append(padString(c.getDataType().toString(), 30));
//            sb.append(" | ");
//        }
//        sb.append("\n");
        sb.append(printTypes());

        for(TableColumn tc : getColumns()) {
            sb.append("---------------------------------");
        }
        sb.append("\n");
        
//        if (!isHasKey() || getKey().getValues() == null) {
//            return sb.toString();
//        }
        //for (int row : getKey().getValues().keySet()) {
        for(int row = 0; row < getTotalNumOfRows(); row++) {
//            for(TableColumn c : getColumns()) {
//                String value = "";
//                
//                if(c.getValues().get(row)!=null) {
//                    value = c.getValues().get(row).toString();
//                }
//                
//                sb.append(padString(value, 30));
//                sb.append(" | ");
//            }
//            sb.append("\n");
            sb.append(printRow(row));
        }

        return sb.toString();
    }

    protected String padString(String s, int n) {
        if (s.length() > n) {
            s = s.substring(0, n);
        }
        return String.format("%1$-" + n + "s", s);
    }

    public TableColumn getKey() {
        if (!hasKey) {
            return null;
        } else if (keyColumn != null) {
            return keyColumn;
        } else {
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).isKey()) {
                    keyIndex = i;
                    keyColumn = columns.get(i);
                    return keyColumn;
                }
            }
        }
        return null;
    }
    
    public void setKey(TableColumn d, Integer i) {
        System.out.println("set key to : " +i);
        this.keyColumn = d;
        this.keyIndex = i;
    }

    public int getKeyIndex() {
        if (!hasKey) {
            return -1;
        } else if (keyIndex > -1) {
            return keyIndex;
        } else {
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).isKey()) {
                    keyIndex = i;
                    keyColumn = columns.get(i);
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return getHeader();
    }

//    @Override
//    public int compareTo(Table o) {
//        return Integer.compare(hashCode, o.hashCode);
//    }
    @Override
    public int compareTo(Table o) {
        return getHeader().compareTo(o.getHeader());
    }

    /**
     * @return the numHeaderRows
     */
    public int getNumHeaderRows() {
        return numHeaderRows;
    }

    /**
     * @param numHeaderRows the numHeaderRows to set
     */
    public void setNumHeaderRows(int numHeaderRows) {
        this.numHeaderRows = numHeaderRows;
    }

    /**
     * @return the totalNumOfRows
     */
    public int getTotalNumOfRows() {
        return totalNumOfRows;
    }

    /**
     * @param totalNumOfRows the totalNumOfRows to set
     */
    public void setTotalNumOfRows(int totalNumOfRows) {
        this.totalNumOfRows = totalNumOfRows;
    }

    /**
     * @return the pageTitle
     */
    public String getPageTitle() {
        return pageTitle;
    }

    /**
     * @param pageTitle the pageTitle to set
     */
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    /**
     * @return the tableTitle
     */
    public String getTableTitle() {
        return tableTitle;
    }

    /**
     * @param tableTitle the tableTitle to set
     */
    public void setTableTitle(String tableTitle) {
        this.tableTitle = tableTitle;
    }

    /**
     * @return the wikiIDCol
     */
    public int getWikiIDCol() {
        return wikiIDCol;
    }

    /**
     * @param wikiIDCol the wikiIDCol to set
     */
    public void setWikiIDCol(int wikiIDCol) {
        this.wikiIDCol = wikiIDCol;
    }

    /**
     * @return the mapping
     */
    public TableMapping getMapping() {
        return mapping;
    }

    /**
     * @param mapping the mapping to set
     */
    public void setMapping(TableMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * @return the data
     */
    public TableData getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(TableData data) {
        this.data = data;
    }
}
