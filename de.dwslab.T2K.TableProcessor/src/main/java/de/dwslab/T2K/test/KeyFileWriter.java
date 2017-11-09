/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.test;

import au.com.bytecode.opencsv.CSVReader;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.util.Variables;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author domi
 */
public class KeyFileWriter {

    public static void main(String args[]) throws FileNotFoundException, IOException {
        Variables.keyUniqueness = 0.8;

        Map<String, Integer> keys = new HashMap<>();
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
