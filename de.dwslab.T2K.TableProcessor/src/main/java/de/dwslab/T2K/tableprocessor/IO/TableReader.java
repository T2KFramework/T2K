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
package de.dwslab.T2K.tableprocessor.IO;

import de.dwslab.T2K.tableprocessor.TableKeyIdentifier;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.util.Variables;

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

    private void prepareTable(Table table) {
        if (table != null) {
            for (TableColumn c : table.getColumns()) {
                c.setDataSource(table.getHeader());
            }            
        }
    }
}
