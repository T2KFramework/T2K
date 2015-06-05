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
package de.dwslab.T2K.tableprocessor.statistics;

import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author domi
 */
public class Statistics {

    public static void main(String args[]) throws UnsupportedEncodingException, FileNotFoundException, IOException {

        String fileDir = args[0];
        File dir = new File(fileDir);
        BufferedWriter write = new BufferedWriter(new FileWriter("sizes.csv"));

        for (File f : dir.listFiles()) {
            TableReader read = new TableReader();
            Table t = read.readWebTable(f.getAbsolutePath());
            int size;
            if (t.getKey() == null) {
                size = -1;
            } else {
                size = t.getKey().getValues().size();
            }

            write.write(f.getName() + ";" + size + "\n");
        }
        write.flush();
        write.close();
    }
}
