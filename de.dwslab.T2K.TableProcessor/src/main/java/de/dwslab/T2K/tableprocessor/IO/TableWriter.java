/*
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
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
package de.dwslab.T2K.tableprocessor.IO;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import au.com.bytecode.opencsv.CSVWriter;

public class TableWriter {

    public void writeTable(Table t, String fileName, boolean add) throws IOException {
        
        CSVWriter w = new CSVWriter(new FileWriter(fileName, add));
        
        List<String> values = new LinkedList<>();
        
        // write headers
        int numRows = 0;
        for(TableColumn c : t.getColumns()) {
            values.add(c.getHeader().toString());
            numRows = Math.max(numRows, c.getValues().size());
        }
        w.writeNext(values.toArray(new String[values.size()]));
        
        // write values
       //for(Integer i : t.getKey().getValues().keySet()) {
       for(int i = 0; i < numRows; i++) {
            
            values.clear();
            
            for(TableColumn c: t.getColumns()) {
                
                if(c.getValues().containsKey(i)) {
                    
                    values.add(c.getValues().get(i).toString());
                    
                } else {
                    
                    values.add("");
                    
                }
                
            }
            
            w.writeNext(values.toArray(new String[values.size()]));
        }
        
        w.close();
    }
    
}
