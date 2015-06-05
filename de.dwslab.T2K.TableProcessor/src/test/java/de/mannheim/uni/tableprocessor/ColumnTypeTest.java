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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mannheim.uni.tableprocessor;

import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.tableprocessor.model.TableColumn.ColumnDataType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 *
 * @author domi
 */
public class ColumnTypeTest extends TestCase {
    
    public void testColumnType() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        
        TableReader read = new TableReader();
        //Table t1 = read.readWebTable("in/web/11688006_0_8123036130090004213.csv");
        Table t1 = read.readWebTable("in/webtable.csv");
        
        Assert.assertEquals(ColumnDataType.string, t1.getColumns().get(0).getDataType());
        Assert.assertEquals(ColumnDataType.numeric, t1.getColumns().get(1).getDataType());
        //Assert.assertEquals(ColumnDataType.unit, t1.getColumns().get(2).getDataType());
        Assert.assertEquals(ColumnDataType.numeric, t1.getColumns().get(2).getDataType());
        
        //large tables
//        Assert.assertEquals(ColumnDataType.unit, t1.getColumns().get(3).getDataType());
//        Assert.assertEquals("Area", t1.getColumns().get(3).getBaseUnit().getName());
        //should be Population Desnity but contains (sq. km.) in the header -> detected as area
//        Assert.assertEquals(ColumnDataType.unit, t1.getColumns().get(4).getDataType());
//        Assert.assertEquals("Area", t1.getColumns().get(4).getBaseUnit().getName());
//        Assert.assertEquals(ColumnDataType.unit, t1.getColumns().get(5).getDataType());
//        Assert.assertEquals("Area", t1.getColumns().get(5).getBaseUnit().getName());
//        Assert.assertEquals(ColumnDataType.unit, t1.getColumns().get(6).getDataType());
//        Assert.assertEquals("Area", t1.getColumns().get(6).getBaseUnit().getName());
        
//        Table t2 = read.readWebTable("in/web/24036779_0_5608105867560183058.csv");
//        Assert.assertEquals(ColumnDataType.string, t2.getColumns().get(0).getDataType());        
//        Assert.assertEquals(ColumnDataType.numeric, t2.getColumns().get(1).getDataType());
//        Assert.assertEquals(ColumnDataType.unit, t2.getColumns().get(2).getDataType());
//        Assert.assertEquals(ColumnDataType.unit, t2.getColumns().get(3).getDataType());
//        Assert.assertEquals(ColumnDataType.numeric, t2.getColumns().get(4).getDataType());
//        Assert.assertEquals(ColumnDataType.string, t2.getColumns().get(5).getDataType());
//        Assert.assertEquals(ColumnDataType.string, t2.getColumns().get(6).getDataType());
//        
//        Table t3 = read.readWebTable("in/web/86297395_0_6919201319699354263.csv");
//        Assert.assertEquals(ColumnDataType.string, t3.getColumns().get(0).getDataType());        
//        Assert.assertEquals(ColumnDataType.numeric, t3.getColumns().get(1).getDataType());
//        Assert.assertEquals(ColumnDataType.unit, t3.getColumns().get(2).getDataType());
//        Assert.assertEquals(ColumnDataType.unit, t3.getColumns().get(3).getDataType());
//        Assert.assertEquals(ColumnDataType.numeric, t3.getColumns().get(4).getDataType());
//        Assert.assertEquals(ColumnDataType.string, t3.getColumns().get(5).getDataType());
//        Assert.assertEquals(ColumnDataType.string, t3.getColumns().get(6).getDataType());
        
//        Table t4 = read.readLODTable("in/dbpedia/Country.csv");   
        Table t4 = read.readLODTable("in/dbpedia.csv"); 
        Assert.assertEquals(ColumnDataType.link, t4.getColumns().get(0).getDataType());        
        Assert.assertEquals(ColumnDataType.string, t4.getColumns().get(1).getDataType());
        Assert.assertEquals(ColumnDataType.numeric, t4.getColumns().get(2).getDataType());
        //Assert.assertEquals(ColumnDataType.unit, t4.getColumns().get(3).getDataType());
        Assert.assertEquals(ColumnDataType.numeric, t4.getColumns().get(3).getDataType());
        //test whether the date is recognized correctly when using the dataytpes given in DBpedia
//        Assert.assertEquals(ColumnDataType.date, t4.getColumns().get(12).getDataType());        
         
    }    
}
