/** Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package de.dwslab.T2K.matching.experiments;

import com.google.gson.Gson;
import com.google.gson.internal.bind.TypeAdapters;
import de.dwslab.T2K.index.io.DefaultIndex;
import de.dwslab.T2K.matching.dbpedia.algorithm.InstanceMatchingTask;
import de.dwslab.T2K.matching.dbpedia.model.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author dritze
 */
public class ObjectExtractor {

    public static void main(String args[]) throws FileNotFoundException, IOException {

        final InstanceMatchingTask m = new InstanceMatchingTask();

        final MatchingParameters loadDbpParams = new MatchingParameters();
        loadDbpParams.setUseUnitDetection(true);
        m.setMatchingParameters(loadDbpParams);

        //m.setLuceneIndex(new DefaultIndex(args[1]));
        //m.loadDBpedia(args[0]);

        MatchingData md = m.getData();

        final EvaluationParameters eval = new EvaluationParameters();

        eval.setEquivalentPropertiesLocation("empty");
        eval.setPropertyRangesLocation("empty");
        eval.setInstanceGoldStandardLocation(args[2]);
        eval.setPropertyGoldStandardLocation(args[3]);
        eval.setCorrectedInstancesLocation("empty");
        eval.setClassGoldStandardLocation(args[4]);
        eval.setClassHierarchyLocation("empty");

        eval.loadCanoniser();

        File dir = new File(args[5]);
        for (File f : dir.listFiles()) {
            
            if(f.getName().contains("-ip-")) {
                continue;
            }
            //System.out.println(f.getName());

            BufferedWriter write = new BufferedWriter(new FileWriter(new File("out/"+f.getName().replace(".json", ".csv"))));
            
            TableReader read = new TableReader();
            Table t = read.readWebTableFromJson(f.getAbsolutePath());

            GoldStandard gs = new GoldStandard();
            try {
                gs.initialise(f.getName(), t, eval);
            } catch (Exception e) {
             //   System.out.println("gold prop: " + e.toString());
            }
           // System.out.println(eval.getPropertyRangesLocation());
           // System.out.println(gs.getPropertyGoldStandard().size());
            
            List<Object> classes = gs.getClassGoldStandard().get(t.getHeader().split("\\.")[0]);
            String className = classes.get(0).toString();
            //System.out.println("class name: " +className);
            
            Table DBTable = null;
            
            for(File fz : new File("/data2/web2dbpedia/matching/data/DBpedia").listFiles()) {
                if (fz.getName().split("\\.")[0].toLowerCase().equals(className)) {
                    DBTable = read.readLODTable(fz.getAbsolutePath());
                }
            }
            
            int keyIndex = -1;
            Map<Integer, List<TableColumn>> possibleColumns = new HashMap<>();
            for (Object o : gs.getPropertyGoldStandard().keySet()) {
                int index = Integer.parseInt(o.toString());
                Object uri = gs.getPropertyGoldStandard().get(o);
                if (uri.toString().contains("#label")) {
                    keyIndex = index;
                    continue;
                }
                for (TableColumn tc : DBTable.getColumns()) {
                    //System.out.println(tc.getURI() + "\t" + tc.getHeader());
                    if (tc.getURI().equals(uri.toString()) && !tc.toString().contains("_label")) {                       
                        if (possibleColumns.containsKey(index)) {
                            possibleColumns.get(index).add(tc);
                            //System.out.println("added " + index + "\t" + tc.getURI());
                        } else {
                            List<TableColumn> l = new ArrayList();
                            l.add(tc);
                            possibleColumns.put(index, l);
                        }
                    }
                }
            }

            for (Object index : gs.getInstanceGoldStandard().keySet()) {
                String uri = gs.getInstanceGoldStandard().get(index).toString();
                //System.out.println("row " + index + "\t" + uri);
                Integer rowIndex = Integer.parseInt(index.toString());
                for (Integer colIndex : possibleColumns.keySet()) {
                    for (TableColumn tc : possibleColumns.get(colIndex)) {
                        for (Integer keyDB : tc.getValues().keySet()) {
                            if (tc.getTable().getColumns().get(0).getValues().get(keyDB).toString().equals(uri)) {
                                Object used = tc.getValues().get(keyDB);                
                                if(used == null) {
                                    continue;
                                }
                                boolean objectProp = false;
                                if(used instanceof List) {
                                    List l = (List)used;                                    
                                    if(l.get(0).toString().contains("http://dbpedia.org")) {
                                        objectProp = true;
                                    }
                                }
                                else {
                                    if(used.toString().contains("http://dbpedia.org")) {
                                        objectProp = true;
                                    }
                                }
                                if(objectProp) {
                                    write.write("\"" + rowIndex + "\",\""+ colIndex +"\",\""+ used+"\"\n");
                                }
//                                else {
//                                    System.out.println(uri + "\t" + tc.getURI() + "\tnull");
//                                }
                            }
                        }
                    }
                }

            }
            write.flush();
            write.close();
        }
    }

}
