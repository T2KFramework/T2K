package de.mannheim.uni.tableprocessor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import junit.framework.TestCase;

public class TableReaderTest extends TestCase {

    public void testReadWebTable() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        TableReader r = new TableReader();
        
        String path = "in/smallTests/web/webtable.csv";
        
        // Test without unit detection
        r.setUseUnitDetection(false);
        Table t = r.readWebTable(path);
        
        assertEquals("china", t.getColumns().get(0).getValues().get(0));
        assertEquals("india", t.getColumns().get(0).getValues().get(1));
        assertEquals("united states of america", t.getColumns().get(0).getValues().get(2));
        assertEquals("indonesia", t.getColumns().get(0).getValues().get(3));
        assertEquals("brazil", t.getColumns().get(0).getValues().get(4));
        
        System.out.println(t.getColumns().get(1).getValues().get(0).getClass());
        
        assertEquals(1339190000.0, t.getColumns().get(1).getValues().get(0));
        assertEquals(1184639000.0, t.getColumns().get(1).getValues().get(1));
        assertEquals(309975000.0, t.getColumns().get(1).getValues().get(2));
        assertEquals(234181400.0, t.getColumns().get(1).getValues().get(3));
        assertEquals(193364000.0, t.getColumns().get(1).getValues().get(4));
        
        assertEquals(9596960.0, t.getColumns().get(2).getValues().get(0));
        assertEquals(3287590.0, t.getColumns().get(2).getValues().get(1));
        assertEquals(9629091.0, t.getColumns().get(2).getValues().get(2));
        assertEquals(1919440.0, t.getColumns().get(2).getValues().get(3));
        assertEquals(8511965.0, t.getColumns().get(2).getValues().get(4));
        
        // Test with unit detection
        r.setUseUnitDetection(true);
        t = r.readWebTable(path);
        assertEquals("china", t.getColumns().get(0).getValues().get(0));
        assertEquals("india", t.getColumns().get(0).getValues().get(1));
        assertEquals("united states of america", t.getColumns().get(0).getValues().get(2));
        assertEquals("indonesia", t.getColumns().get(0).getValues().get(3));
        assertEquals("brazil", t.getColumns().get(0).getValues().get(4));
        
        assertEquals(1339190000.0, t.getColumns().get(1).getValues().get(0));
        assertEquals(1184639000.0, t.getColumns().get(1).getValues().get(1));
        assertEquals(309975000.0, t.getColumns().get(1).getValues().get(2));
        assertEquals(234181400.0, t.getColumns().get(1).getValues().get(3));
        assertEquals(193364000.0, t.getColumns().get(1).getValues().get(4));
        
        assertEquals(9596960000000.0, t.getColumns().get(2).getValues().get(0));
        assertEquals(3287590000000.0, t.getColumns().get(2).getValues().get(1));
        assertEquals(9629091000000.0, t.getColumns().get(2).getValues().get(2));
        assertEquals(1919440000000.0, t.getColumns().get(2).getValues().get(3));
        assertEquals(8511965000000.0, t.getColumns().get(2).getValues().get(4));
    }

    public void testReadWebTable2() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        TableReader r = new TableReader();
        
        String path = "in/web/24036779_0_5608105867560183058.csv";
        
        // Test without unit detection
        r.setUseUnitDetection(false);
        Table t = r.readWebTable(path);
        
        int numValues = t.getColumns().get(2).getValues().size();
        
        assertEquals(83, numValues);
    }
    
    public void testReadLODTable() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        TableReader r = new TableReader();
        
        Table t = r.readLODTable("in/dbpedia/Athlete_small.csv");
        
        for(TableColumn c : t.getColumns()) {
            assertFalse(c.getHeader().toString().endsWith("\""));
            assertFalse(c.getURI().endsWith("\""));
        }
    }

}
