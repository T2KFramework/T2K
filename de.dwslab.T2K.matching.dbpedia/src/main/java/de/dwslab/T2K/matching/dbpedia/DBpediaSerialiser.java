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
package de.dwslab.T2K.matching.dbpedia;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class DBpediaSerialiser {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        TableReader r = new TableReader();
        r.setUseUnitDetection(Boolean.parseBoolean(args[0]));
        
        ArrayList<Table> tables = new ArrayList<Table>();
        
        List<File> files = new LinkedList<File>();
        
        if(new File(args[1]).isDirectory()) {
            for(File f : new File(args[1]).listFiles()) {
                files.add(f);
            }
        } else {
            files.add(new File(args[1]));
        }
        
        for(File f : files) {
            try {
                System.out.println(f.getName());
                Table t = r.readLODTable(f.getAbsolutePath());
                tables.add(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
//        ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(args[2]));
//        oout.writeObject(tables);
//        oout.close();
        Kryo kryo = new Kryo();

        Output output = new Output(new FileOutputStream(args[2]));
        kryo.writeObject(output, tables);
        output.close();
        
        System.out.println("done.");
        
    }
    
}
