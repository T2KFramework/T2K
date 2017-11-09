/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.tableprocessor;

import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 *
 * @author domi
 */
public class ValueTest extends TestCase {

    public void testValues() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        TableReader read = new TableReader();
        read.setUseUnitDetection(true);
//        Table t1 = read.readWebTable("in/web/11688006_0_8123036130090004213.csv");
//        
//        //header is not counted as value!
//        Object value = t1.getColumns().get(3).getValues().get(5);
//        //check if value was transformed into base unit
//        Assert.assertEquals(8.0394E13,(double)value);
//        Table t2 = read.readWebTable("in/web/24036779_0_5608105867560183058.csv");
//        value = t2.getColumns().get(6).getValues().get(4);
//        Assert.assertEquals("no (abolished by parliament in 1972[57])", (String)value);
//        Table t3 = read.readWebTable("in/web/86297395_0_6919201319699354263.csv");
//        value = t3.getColumns().get(6).getValues().get(59);
//        Assert.assertEquals("yes (alternative service available", (String)value);
//        Table t4 = read.readLODTable("in/dbpedia/Country.csv");
//        value = t4.getColumns().get(2).getValues().get(1);
//        Assert.assertEquals(1.57989274730496E9, (Double)value);
        
        Table t1 = read.readWebTable("in/smallTests/web/webtable.csv");
        Object value = t1.getColumns().get(2).getValues().get(0);
        Assert.assertEquals(9.59696E12,(Double) value);
        
        Table t4 = read.readLODTable("in/smallTests/dbpedia/dbpedia.csv");
        value = t4.getColumns().get(2).getValues().get(0);
        Assert.assertEquals(3721432.0, (Double)value);
        
    }
}
