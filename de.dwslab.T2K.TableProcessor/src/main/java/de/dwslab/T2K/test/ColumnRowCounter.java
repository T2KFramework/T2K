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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author domi
 */
public class ColumnRowCounter {

    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {

        int columns = 0;
        int rows = 0;
        int tables = 0;

        File directory = new File("C:\\Users\\domi\\Downloads\\NonContent");
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                continue;
            }
            tables++;

            BufferedReader read = new BufferedReader(new FileReader(f));
            String line = read.readLine();
            while (line != null) {
                rows++;
                line = read.readLine();
            }
            read.close();

            TableReader readT = new TableReader();
            Table t = readT.readWebTable(f.getAbsolutePath());
            if (t == null) {
                continue;
            }
            columns += t.getColumns().size();
        }


        double average = (double) rows / tables;
        double averageCol = (double) columns / tables;
        System.out.println("avg rows " + average + " # tables " + tables + " avg cols " + averageCol);
    }

    public static void countRows() throws UnsupportedEncodingException, FileNotFoundException, IOException {

        Set<String> names = new HashSet<>();
        File fileList = new File("C:\\Users\\domi\\Dropbox\\Arbeit\\WebTables\\OliVersion\\matching.dbpedia\\2ColumnDimension.txt");

        BufferedReader read = new BufferedReader(new FileReader(fileList));
        String line = read.readLine();
        while (line != null) {
            names.add(line.replace("tar.gz", "csv"));
            line = read.readLine();
        }
        read.close();


        int small = 0;
        int medium = 0;
        int large = 0;

        File directory = new File("C:\\Users\\domi\\Downloads\\allWT");
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                continue;
            }
            if(!names.contains(f.getName())) {
                continue;
            }
            int counter = 0;
            read = new BufferedReader(new FileReader(f));
            line = read.readLine();
            while (line != null) {
                counter++;
                line = read.readLine();
            }
            if (counter < 20) {
                small++;
            } else if (counter < 100) {
                medium++;
            } else {
                large++;
            }
        }

        System.out.println("small " + small + " medium " + medium + " large " + large);
    }
}
