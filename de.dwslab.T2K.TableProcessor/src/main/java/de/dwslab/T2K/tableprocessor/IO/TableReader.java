package de.dwslab.T2K.tableprocessor.IO;

import de.dwslab.T2K.tableprocessor.TableKeyIdentifier;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.tableprocessor.model.json.TableData;
import de.dwslab.T2K.util.Variables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Class to read a table (LOD or web) from an archive (.gz) or a CSV file.
 * During the process, the datatypes for the columns are
 * determined as well as the key column and the headers of each column. 
 * It is supposed that the cells are separated by "," otherwise the 
 * variable "delimiter" in the class "Variables" need to be changed.
 * If the cell values should be normalized, set "normalizeValues" in "Variables"
 * to true. When the values are normalized, the stopswords are also removed (more can
 * be added in a custom list in the class "Variables").
 * 
 * @author domi
 */
public class TableReader {

    private ConvertFileToTable conv;
    public void setUseUnitDetection(boolean useUnitDetection) {
        conv.setUseUnitDetection(useUnitDetection);
        Variables.useUnitDetection =useUnitDetection;
    }
    public boolean isUseUnitDetection() {
        return conv.isUseUnitDetection();
    }
    public void setSpanningCellThreshold(int threshold) {
        conv.setSpanningCellThreshold(threshold);
    }
    
    public TableReader() {
        conv = new ConvertFileToTable();
    }

    public Table readWebTable(String path) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        Table t = conv.readWebTable(path);
        if(t!=null) {
            t.setFullPath(path);
            prepareTable(t);
            //identify the key
            TableKeyIdentifier keyIdentifier = new TableKeyIdentifier();
            keyIdentifier.identifyKeys(t);
            t.setPageTitle(t.getHeader().replaceAll("\\.html.*", ""));
            t.setSource(t.getHeader().replaceAll("\\.html.*", ""));
            t.setContextAfterTable("");
            t.setContextBeforeTable("");
            t.setTableTitle("");
        }
        return t;
    }
    
    public Table readWebTableFromJson(String path) {
        JsonTableParser p = new JsonTableParser();
        Table t = p.parseJson(new File(path));
        if(t!=null) {
            t.setFullPath(path);
            prepareTable(t);
            //identify the key
//            TableKeyIdentifier keyIdentifier = new TableKeyIdentifier();
//            keyIdentifier.identifyKeys(t);
        }
        return t;
    }
    
//    public Table readWebTableFromJson(TableData data, String path) {
//        JsonTableParser p = new JsonTableParser();
//        Table t = p.parseJson(data, path);
//        if(t!=null) {
//            t.setFullPath(path);
//            prepareTable(t);
//            //identify the key
////            TableKeyIdentifier keyIdentifier = new TableKeyIdentifier();
////            keyIdentifier.identifyKeys(t);
//        }
//        return t;
//    }
    
    public Table readKGTable(String path) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        Table t = conv.readKGTable(path);
        if(t!=null) {
            t.setFullPath(path);
            prepareTable(t);
            //identify the key
            TableKeyIdentifier keyIdentifier = new TableKeyIdentifier();
            keyIdentifier.indenfityLODKeys(t);
            for(TableColumn c : t.getColumns()) {
                if(c.getHeader().toString().toLowerCase().contains("wikiid")) {
                    System.out.println("wiki id " + t.getColumns().indexOf(c));
                    t.setWikiIDCol(t.getColumns().indexOf(c));
                }
            }
        }
        return t;
    }
    
    public Table readLODTable(String path) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        Table t = conv.readLODTable(path);
        if(t!=null) {
            t.setFullPath(path);
            prepareTable(t);
            //identify the key
            TableKeyIdentifier keyIdentifier = new TableKeyIdentifier();
            keyIdentifier.indenfityLODKeys(t);
        }
        return t;
    }

    public void prepareTable(Table table) {
        if (table != null) {
            for (TableColumn c : table.getColumns()) {
                c.setDataSource(table.getHeader());
            }            
        }
    }
    
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        TableReader tr = new TableReader();
        Table t = tr.readLODTable(args[0]);
        System.out.println(t.printTable());
    }
}
