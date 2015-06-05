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
package de.dwslab.T2K.tableprocessor.model;

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
        for(TableColumn c : getColumns()) {
            sb.append(padString(c.getHeader(), 30));
            sb.append(" | ");
        }
        sb.append("\n");
        return sb;
    }
    
    public StringBuilder printTypes() {
        StringBuilder sb = new StringBuilder();
        for(TableColumn c : getColumns()) {
            sb.append(padString(c.getDataType().toString(), 30));
            sb.append(" | ");
        }
        sb.append("\n");
        return sb;
    }
    
    public StringBuilder printRow(int row) {
        StringBuilder sb = new StringBuilder();
        for(TableColumn c : getColumns()) {
            String value = "";
            
            if(c.getValues().get(row)!=null) {
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
        
        if(getKey() == null || getKey().getValues() == null) {
            return sb.toString();
        }
        for(int row : getKey().getValues().keySet()) {
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
        for (TableColumn c : columns) {
            if (c.isKey()) {
                return c;
            }
        }
        return null;
    }

    public int getKeyIndex() {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).isKey()) {
                return i;
            }
        }
        return -1;
    }
        
    @Override
    public String toString() {
        return getHeader();
    }

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
}
