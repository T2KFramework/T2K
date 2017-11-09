/** Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package de.dwslab.T2K.test;

import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableMapping;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author dritze
 */


public class CheckMapping {
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        File f = new File("/home/dritze/2012Mappings/allTables/1346981172155_1347001877976_1497.arc2187851125028841241#45866238_0_8607256803539805057.csv");
        TableReader r = new TableReader();
        Table t = r.readWebTable(f.getAbsolutePath());
        TableMapping tm = new TableMapping();
        tm.readMapping(f.getAbsolutePath());
        for (Integer numCorres : tm.getMappedInstances().keySet()) {
            System.out.println(numCorres + " - " +tm.getMappedInstances().get(numCorres).getFirst());
        }
        for(Integer i : t.getKey().getValues().keySet()) {
            System.out.println(i + " - " + t.getKey().getValues().get(i));
        }
    }
    
}
