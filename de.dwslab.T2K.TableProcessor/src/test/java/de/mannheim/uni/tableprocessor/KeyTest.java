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
        
        Table t1 = read.readWebTable("in/webtable.csv");
        Assert.assertEquals(0,t1.getKeyIndex());
//        
//        Table t4 = read.readLODTable("in/smallTests/dbpedia/dbpedia.csv");
//        Assert.assertEquals(1,t4.getKeyIndex());
        
//         Table t2 = read.readWebTable("in/web/21585935_0_294037497010176843.csv");
//         Assert.assertEquals(1,t2.getKeyIndex());
        
    }
    
}
