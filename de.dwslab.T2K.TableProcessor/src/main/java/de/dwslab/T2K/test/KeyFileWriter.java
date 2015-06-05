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
package de.dwslab.T2K.test;

import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.util.Variables;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author domi
 */
public class KeyFileWriter {

    public static void main(String args[]) throws FileNotFoundException, IOException {
        Variables.keyUniqueness = 0.8;

        //Map<String, Integer> keys = new HashMap<>();
        File output = new File("keyFile.csv");
        BufferedWriter write = new BufferedWriter(new FileWriter(output));
        //Pipeline pipeline = Pipeline.getPipelineFromConfigFile("domi", "searchJoins.conf");
        File directory = new File("C:\\Users\\domi\\Downloads\\allInstances");
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                continue;
            }

            TableReader readT = new TableReader();
            Table t = readT.readWebTable(f.getAbsolutePath());
            if (t == null) {
                continue;
            }
            write.write(f.getName()+","+t.getKeyIndex()+"\n");
        }
        write.flush();
        write.close();
    }
}
