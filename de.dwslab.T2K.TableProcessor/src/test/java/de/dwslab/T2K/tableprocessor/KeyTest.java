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
public class KeyTest extends TestCase {
    
    public void testKeyColumn() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        TableReader read = new TableReader();
//        Table t1 = read.readWebTable("in/web/11688006_0_8123036130090004213.csv");
//        Assert.assertEquals(1,t1.getKeyIndex());
//        Table t2 = read.readWebTable("in/web/24036779_0_5608105867560183058.csv");
//        Assert.assertEquals(0,t2.getKeyIndex());
//        Table t3 = read.readWebTable("in/web/86297395_0_6919201319699354263.csv");
//        Assert.assertEquals(0,t3.getKeyIndex());
//        Table t4 = read.readLODTable("in/dbpedia/Country.csv");
//        Assert.assertEquals(1,t4.getKeyIndex());
        
        Table t1 = read.readWebTable("in/smallTests/web/webtable.csv");
        Assert.assertEquals(0,t1.getKeyIndex());
//        
//        Table t4 = read.readLODTable("in/smallTests/dbpedia/dbpedia.csv");
//        Assert.assertEquals(1,t4.getKeyIndex());
        
//         Table t2 = read.readWebTable("in/web/21585935_0_294037497010176843.csv");
//         Assert.assertEquals(1,t2.getKeyIndex());
        
    }
    
}
