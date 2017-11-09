package de.dwslab.T2K.matching.dbpedia.baselines;

import de.dwslab.T2K.matching.dbpedia.model.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.similarity.functions.string.JaccardSimilarity;
import de.dwslab.T2K.similarity.functions.string.LevenshteinSimilarity;
import de.dwslab.T2K.tableprocessor.IO.JsonTableParser;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.TableKeyIdentifier;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.



/**
 *
 * @author dritze
 */


public class AttributeBaseline {
    
    public static void main(String args[]) throws FileNotFoundException, IOException {
        
        //tables args[0]
        File tableDir = new File(args[0]);

        MatchingParameters mp = new MatchingParameters();
        MatchingData md = new MatchingData();
        md.loadDBpedia(args[3], mp);

        Map<Double, Integer> occurenceJaccard = new HashMap();
        List<Double> allSimsJaccard = new ArrayList<>();

        Map<Double, Integer> occurenceEquals = new HashMap();

        Map<Double, Integer> occurenceLeven = new HashMap();
        List<Double> allSimsLeven = new ArrayList<>();

        List<String> nothingFOundJaccard = new ArrayList<>();

        for (File f : tableDir.listFiles()) {
            boolean nothingFound = true;
            TableReader tr = new TableReader();
            Table t = null;
            if (f.getName().endsWith(".csv")) {
                t = tr.readWebTable(f.getAbsolutePath());
                if (t != null) {
                    t.setFullPath(f.getAbsolutePath());
                    tr.prepareTable(t);
                    //identify the key
                    TableKeyIdentifier keyIdentifier = new TableKeyIdentifier();
                    keyIdentifier.identifyKeys(t);
                }
            } else {
                JsonTableParser jp = new JsonTableParser();
                t = jp.parseJson(f);
            }
            if (!t.isHasKey()) {
                continue;
            }

            GoldStandard gs = new GoldStandard();
            EvaluationParameters ep = new EvaluationParameters();
            ep.setCorrectedInstancesLocation(args[1] + "/correctedInstances.tsv");
            ep.setEquivalentPropertiesLocation(args[1] + "/equivalentProperties.tsv");
            ep.setPropertyRangeGoldstandardLocation(args[1] + "/propertyRanges.tsv");
            ep.setClassHierarchyLocation(args[1] + "/superclasses.tsv");
            ep.setPropertyGoldStandardLocation(args[2]);
            ep.loadCanoniser();

            System.out.println("lcoation: " + ep.getPropertyGoldStandardLocation());
            System.out.println("file: " + f.getName());
            String filePath = new File(ep.getPropertyGoldStandardLocation(), f.getName()).getAbsolutePath();
            System.out.println("file path: " + filePath);
            gs.initialise(f.getName(), t, ep);
            
            //System.out.println(gs.getInstanceGoldStandard());
            for (TableColumn tc : t.getColumns()) {
                if(tc.isKey()) {
                    continue;
                }
                Integer columnIndex = t.getColumns().indexOf(tc);    
                System.out.println("index; " +columnIndex);
                if (gs.getPropertyGoldStandard().get(columnIndex) != null) {
                    System.out.println("not null");
                    String uri = gs.getPropertyGoldStandard().get(columnIndex).toString();
                    String propName = "";
                    for(TableColumn tc2 : md.getDbpediaColSet()) {
                        if(tc2.getURI().equals(uri)) {
                            propName = tc2.getHeader().toString();
                            propName = propName.replaceAll("([A-Z]+)","\\_$1").toLowerCase(); 
                            propName = propName.replace("_", " ");
                            break;
                        }
                    }
                    if (uri != null && !propName.isEmpty()) {

                        JaccardSimilarity js = new JaccardSimilarity();
                        Double value = js.calculate(propName.toLowerCase(), tc.getHeader().toString().toLowerCase());
                        
                       // if (f.getName().contains("21333456_2_1886495893795687264")) {
                            System.out.println(propName +" vs " +tc.getHeader()+ " = " +value);
                        //}

                        if (value > 0) {
                            nothingFound = false;
                        }

                        if (occurenceJaccard.containsKey(value)) {
                            int current = occurenceJaccard.get(value);
                            current++;
                            occurenceJaccard.put(value, current);
                        } else {
                            occurenceJaccard.put(value, 1);
                        }
                        allSimsJaccard.add(value);

                        if (propName.toLowerCase().equals(tc.getHeader().toString().toLowerCase())) {
                            if (occurenceEquals.containsKey(1.0)) {
                                int current = occurenceEquals.get(1.0);
                                current++;
                                occurenceEquals.put(1.0, current);
                            } else {
                                occurenceEquals.put(1.0, 1);
                            }
                        }

                        LevenshteinSimilarity ls = new LevenshteinSimilarity();
                        Double lsScore = ls.calculate(propName.toLowerCase(), tc.getHeader().toString().toLowerCase());

                        if (occurenceLeven.containsKey(lsScore)) {
                            int current = occurenceLeven.get(lsScore);
                            current++;
                            occurenceLeven.put(lsScore, current);
                        } else {
                            occurenceLeven.put(lsScore, 1);
                        }
                        allSimsLeven.add(lsScore);

                        //System.out.println("jacc: " +rows.get(0).getKeyCell().toString() + "\t" + t.getKey().getValues().get(i).toString() + "\t" + value);
                    }
                }
            }
            if (nothingFound) {
                nothingFOundJaccard.add(f.getName());
            }
        }

        System.out.println(occurenceJaccard);
        System.out.println(occurenceEquals);
        System.out.println(occurenceLeven);
        System.out.println(nothingFOundJaccard);

    }
}
