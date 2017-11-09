
package de.dwslab.T2K.test;

import au.com.bytecode.opencsv.CSVReader;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.util.Variables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;



/**
 *
 * @author domi
 */
public class KeyHeaderDetectionCounter {

    public static void main(String args[]) throws FileNotFoundException, IOException {
        if (args[0].equals("keyHeaderDetection")) {
            Variables.keyUniqueness = Double.parseDouble(args[4]);

            Map<String, Integer> keys = new HashMap<>();
            //Pipeline pipeline = Pipeline.getPipelineFromConfigFile("domi", "searchJoins.conf");
            File directory = new File(args[1]);
            for (File f : directory.listFiles()) {
                if (f.isDirectory()) {
                    continue;
                }

                TableReader readT = new TableReader();
                Table t = readT.readWebTable(f.getAbsolutePath());
                if (t == null) {
                    continue;
                }

                int keyColumn = 0;
                for (TableColumn c : t.getColumns()) {
                    if (c.isKey()) {
                        System.out.println("name: " + f.getName() + " data type: " + c.getDataType() + " column name: " + c.getHeader());
                        break;
                    }
                    keyColumn++;
                }
                if (t.isHasKey()) {
                    keys.put(f.getName(), keyColumn);
                } else {
                    keys.put(f.getName(), -1);
                }
            }
            int correctKeys = 0, correctHeaders = 0, counterKey = 0, counterHeader = 0;
            CSVReader read = new CSVReader(new BufferedReader(new FileReader(args[2])),';');
            List<String[]> results = read.readAll();
            List<String> alreadySeen = new ArrayList();
            int counter =0;
            for (String[] line : results) {
                counter++;
                if(counter%100==0) {
                    System.out.println(counter);
                }
                if (line[0].equals("Name")) {
                    continue;
                }
                String name = line[0].replace("tar.gz", "csv");
                if (!keys.containsKey(name)) {
                    continue;
                }
                if (alreadySeen.contains(name)) {
                    continue;
                } else {
                    alreadySeen.add(name);
                }
                int keyPosition = -5, header = -5;
                boolean hasKey = true, hasHeader = true;
                if (line[2].isEmpty() || line[2].equals("NULL")) {
                    if (args[3].contains("empty")) {
                        keyPosition = -1;
                        counterKey++;
                    } else {
                        hasKey = false;
                    }
                } else if (!line[2].contains(",")) {
                    keyPosition = Integer.parseInt(line[2]);
                    counterKey++;
                } else {
                    hasKey = false;
                }
                if (line[1].isEmpty() || line[1].equals("NULL")) {
                    if (args[3].contains("empty")) {
                        header = -1;
                        counterHeader++;
                    } else {
                        hasHeader = false;
                    }
                } else if (!line[1].contains(",")) {
                    header = Integer.parseInt(line[1]);
                    counterHeader++;
                } else {
                    hasHeader = false;
                }                
                //System.out.println("name " + name + " score: " + keys.get(name) + " key pos: " + keyPosition + " detected key pos: " + keys.get(name) + "  header pos " + header);

                if (hasKey && keys.get(name) == keyPosition) {
                    correctKeys++;
                }
                else {
                    System.out.println("name: " +name + " key pos: " + keyPosition + " detected key pos: " + keys.get(name));
                }

                if (hasHeader && header == 0) {
                    correctHeaders++;
                }
            }
            System.out.println("#keys: " + counterKey + " #header: " + counterHeader + " correct keys: " + correctKeys + " correct headers: " + correctHeaders);
        }
    }
}
