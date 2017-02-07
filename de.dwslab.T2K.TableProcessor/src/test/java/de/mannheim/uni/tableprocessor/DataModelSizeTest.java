package de.mannheim.uni.tableprocessor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import objectexplorer.MemoryMeasurer;
import objectexplorer.ObjectGraphMeasurer;
import objectexplorer.ObjectGraphMeasurer.Footprint;
import junit.framework.TestCase;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;

public class DataModelSizeTest extends TestCase {

    public void testSizes() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        
        TableReader r = new TableReader();
        
        Table t = r.readLODTable("resources/dbpedia/Country.csv.gz");
        
        int numElements = 0;
        
        Object[][] columnOriented = new Object[t.getColumns().size()][];
        String[][] columnString = new String[t.getColumns().size()][];
        
        int col = 0;
        for(TableColumn c : t.getColumns()) {
            Object[] columnData = new Object[t.getTotalNumOfRows()];
            String[] stringData = new String[t.getTotalNumOfRows()];
            
            for(int idx : c.getValues().keySet()) {
                columnData[idx] = c.getValues().get(idx);
                stringData[idx] = c.getValues().get(idx).toString();
                numElements++;
            }
            
            columnString[col] = stringData;
            columnOriented[col++] = columnData;
            
        }
        
        System.out.println(String.format("Table %s has %,d elements", t.getHeader(), numElements));
//        
//        measure(t, "Table");
//        measureObjectArray(columnOriented, "ColumnArray");
//        measureObjectArray(columnString, "StringArray");
    }
    
    private static void measure(Object obj, String name) {
        long memory = MemoryMeasurer.measureBytes(obj);

        System.out.println(String.format("%s Memory Size: %,d", name, memory));
        
        Footprint footprint = ObjectGraphMeasurer.measure(obj);
        System.out.println(String.format("%s Graph Footprint: \n\tObjects: %,d\n\tReferences %,d", name, footprint.getObjects(), footprint.getReferences()));
    }
    
    private static void measureObjectArray(Object[][] obj, String name) {
        long memory = MemoryMeasurer.measureBytes(obj);
        
        
//        System.out.println(String.format("%s x%d", name, obj.length));
//        for(int col = 0; col < obj.length; col++) {
//            Object[] o = obj[col];
//            
//            if(o!=null) {
//                System.out.println(String.format(" [%d] x%d", col, o.length));
//            } else {
//                System.out.println(String.format(" [%d] null", col));
//            }
//        }
        

        System.out.println(String.format("%s Memory Size: %,d", name, memory));
        
        Footprint footprint = ObjectGraphMeasurer.measure(obj);
        System.out.println(String.format("%s Graph Footprint: \n\tObjects: %,d\n\tReferences %,d", name, footprint.getObjects(), footprint.getReferences()));
    }
    
}
