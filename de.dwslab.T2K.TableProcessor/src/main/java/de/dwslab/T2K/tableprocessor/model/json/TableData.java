package de.dwslab.T2K.tableprocessor.model.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;

public class TableData {

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    public enum HeaderPosition {
        FIRST_ROW
    }

    public enum TableType {
        RELATION
    }

    public enum TableOrientation {
        HORIZONTAL, VERTICAL
    }

    private String[][] relation;
    private String pageTitle;
    private String title;
    private String url;
    private boolean hasHeader;
    private HeaderPosition headerPosition;
    private TableType tableType;
    private int tableNum;
    private String s3Link;
    private int recordEndOffset;
    private int recordOffset;
    private TableOrientation tableOrientation;
    private String TableContextTimeStampBeforeTable;
    private String textBeforeTable;
    private String textAfterTable;
    private boolean hasKeyColumn;
    private int keyColumnIndex;
    private int headerRowIndex;
    private String path;

    public String[][] getRelation() {
        return relation;
    }

    public void setRelation(String[][] relation) {
        this.relation = relation;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public HeaderPosition getHeaderPosition() {
        return headerPosition;
    }

    public void setHeaderPosition(HeaderPosition headerPosition) {
        this.headerPosition = headerPosition;
    }

    public TableType getTableType() {
        return tableType;
    }

    public void setTableType(TableType tableType) {
        this.tableType = tableType;
    }

    public int getTableNum() {
        return tableNum;
    }

    public void setTableNum(int tableNum) {
        this.tableNum = tableNum;
    }

    public String getS3Link() {
        return s3Link;
    }

    public void setS3Link(String s3Link) {
        this.s3Link = s3Link;
    }

    public int getRecordEndOffset() {
        return recordEndOffset;
    }

    public void setRecordEndOffset(int recordEndOffset) {
        this.recordEndOffset = recordEndOffset;
    }

    public int getRecordOffset() {
        return recordOffset;
    }

    public void setRecordOffset(int recordOffset) {
        this.recordOffset = recordOffset;
    }

    public TableOrientation getTableOrientation() {
        return tableOrientation;
    }

    public void setTableOrientation(TableOrientation tableOrientation) {
        this.tableOrientation = tableOrientation;
    }

    public String getTableContextTimeStampBeforeTable() {
        return TableContextTimeStampBeforeTable;
    }

    public void setTableContextTimeStampBeforeTable(
            String tableContextTimeStampBeforeTable) {
        TableContextTimeStampBeforeTable = tableContextTimeStampBeforeTable;
    }

    public String getTextBeforeTable() {
        return textBeforeTable;
    }

    public void setTextBeforeTable(String textBeforeTable) {
        this.textBeforeTable = textBeforeTable;
    }

    public String getTextAfterTable() {
        return textAfterTable;
    }

    public void setTextAfterTable(String textAfterTable) {
        this.textAfterTable = textAfterTable;
    }

    public boolean isHasKeyColumn() {
        return hasKeyColumn;
    }

    public void setHasKeyColumn(boolean hasKeyColumn) {
        this.hasKeyColumn = hasKeyColumn;
    }

    public int getKeyColumnIndex() {
        return keyColumnIndex;
    }

    public void setKeyColumnIndex(int keyColumnIndex) {
        this.keyColumnIndex = keyColumnIndex;
    }

    public int getHeaderRowIndex() {
        return headerRowIndex;
    }

    public void setHeaderRowIndex(int headerRowIndex) {
        this.headerRowIndex = headerRowIndex;
    }

    public int getNumberOfHeaderRows() {
        if (tableType != TableType.RELATION
                || tableOrientation != TableOrientation.HORIZONTAL
                || !hasHeader || headerPosition != HeaderPosition.FIRST_ROW) {
            return 0;
        } else {
            return 1;
        }
    }

    public String[] getColumnHeaders() {
        String[] headers = null;
        headers = new String[relation.length];
    
        for (int col = 0; col < relation.length; col++) {
            headers[col] = relation[col][0];
        }
        return headers;
        
//        if (tableType != TableType.RELATION
//                || tableOrientation != TableOrientation.HORIZONTAL
//                || !hasHeader) {
//            return null;
//        }
//
//        headers = null;
//
//        if(headerPosition!=null) {
//            switch (headerPosition) {
//            case FIRST_ROW:
//                headers = new String[relation.length];
//    
//                for (int col = 0; col < relation.length; col++) {
//                    headers[col] = relation[col][0];
//                }
//                break;
//            default:
//    
//            }
//        }
//
//        return headers;
    }
    
    public static TableData fromJson(File file) throws IOException {
        Gson gson = new Gson();
        
        FileReader reader = new FileReader(file);
        // get the data from the JSON source
        TableData data = gson.fromJson(reader, TableData.class);
        data.setPath(file.getPath());
        reader.close();
        
        return data;
    }
    
    public void transposeRelation() {
        int colNum = 0;
        
        for(int i = 0; i < relation.length; i++) {
            colNum = Math.max(colNum, relation[i].length);
        }
        
        String[][] newRelation = new String[colNum][relation.length];
        
        for(int i = 0; i < relation.length; i++) {
            for(int j = 0; j < colNum; j++) {
                
                if(j < relation[i].length) {
                    newRelation[j][i] = relation[i][j];
                }
                
            }
        }
        
        relation = newRelation;
    }
}
