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

/**
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of
 * Mannheim (t2k@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *
 *
 * /**
 *
 * @author dritze
 */
public class AttributeBaselineFromTable {

    public static void main(String args[]) throws FileNotFoundException, IOException {

        //tables args[0]
        File tableDir = new File(args[0]);

        MatchingParameters mp = new MatchingParameters();
        MatchingData md = new MatchingData();
        md.loadDBpedia(args[3], mp);

        Map<Double, List<Integer>> occurenceJaccard = new HashMap();
        List<Double> allSimsJaccard = new ArrayList<>();

        Map<Double, Integer> occurenceEquals = new HashMap();

        Map<Double, Integer> occurenceLeven = new HashMap();
        List<Double> allSimsLeven = new ArrayList<>();

        int FP = 0, FN = 0, TP = 0, TN = 0, all = 0;
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
           // ep.setEquivalentPropertiesLocation(args[1] + "/equivalentProperties.tsv");
            ep.setEquivalentPropertiesLocation("empty");
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
                if (tc.isKey()) {
                    continue;
                }
                Integer columnIndex = t.getColumns().indexOf(tc);
                boolean hasCorres = false;
                System.out.println("index; " + columnIndex);

                double max = 0.0;
                String uriBest = "";
                for (TableColumn tc2 : md.getDbpediaColSet()) {
                    String propName = tc2.getHeader().toString();
                    propName = propName.replaceAll("([A-Z]+)", "\\_$1").toLowerCase();
                    propName = propName.replace("_", " ");

                    JaccardSimilarity js = new JaccardSimilarity();
                    Double value = js.calculate(propName.toLowerCase(), tc.getHeader().toString().toLowerCase());

                    if (value > max) {
                        max = value;
                        uriBest = tc2.getURI();
                    }

                    //LevenshteinSimilarity ls = new LevenshteinSimilarity();
                    //Double lsScore = ls.calculate(propName.toLowerCase(), tc.getHeader().toString().toLowerCase());
                }

                if (gs.getPropertyGoldStandard().get(columnIndex) != null) {
                    all++;
                    if (gs.getPropertyGoldStandard().get(columnIndex).equals(uriBest)) {
                        TP++;
                        if (occurenceJaccard.get(max) != null) {
                            occurenceJaccard.get(max).add(1);
                        } else {
                            List<Integer> list = new ArrayList<>();
                            list.add(1);
                            occurenceJaccard.put(max, list);
                        }
                    } else {
                        FP++;
                        if (occurenceJaccard.get(max) != null) {
                            occurenceJaccard.get(max).add(0);
                        } else {
                            List<Integer> list = new ArrayList<>();
                            list.add(0);
                            occurenceJaccard.put(max, list);
                        }
                    }
                } else {
                    FP++;
                    if (occurenceJaccard.get(max) != null) {
                        occurenceJaccard.get(max).add(0);
                    } else {
                        List<Integer> list = new ArrayList<>();
                        list.add(0);
                        occurenceJaccard.put(max, list);
                    }
                }
            }
        }

        System.out.println(TP);
        System.out.println(FP);
        System.out.println(all);
        for(Double d : occurenceJaccard.keySet()) {
            System.out.println(d + " --- " + occurenceJaccard.get(d));
        }

    }
}
