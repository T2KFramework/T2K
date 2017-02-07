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
package de.dwslab.T2K.matching.dbpedia.model;

import de.dwslab.T2K.matching.dbpedia.properties.Canoniser;
import de.dwslab.T2K.normalisation.StringNormalizer;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.utils.data.link.DBpediaURIEncoder;
import de.dwslab.T2K.utils.io.CSVUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * represents the gold standard for instances, properties and classes
 *
 * @author Oliver
 *
 */
public class GoldStandard {

    private Map<Object, Object> instanceGoldStandard;

    public Map<Object, Object> getInstanceGoldStandard() {
        return instanceGoldStandard;
    }
    private Canoniser instanceCanoniser;

    public Canoniser getInstanceCanoniser() {
        return instanceCanoniser;
    }

    public void setInstanceCanoniser(Canoniser instanceCanoniser) {
        this.instanceCanoniser = instanceCanoniser;
    }
    private Map<Object, Object> propertyGoldStandard;

    public Map<Object, Object> getPropertyGoldStandard() {
        return propertyGoldStandard;
    }

    public void setPropertyGoldStandard(Map<Object, Object> propertyGoldStandard) {
        this.propertyGoldStandard = propertyGoldStandard;
    }
    private Map<Object, Object> propertyRangeGoldStandard;

    public Map<Object, Object> getPropertyRangeGoldStandard() {
        return propertyRangeGoldStandard;
    }

    public void setPropertyRangeGoldStandard(Map<Object, Object> propertyRangeGoldStandard) {
        this.propertyRangeGoldStandard = propertyRangeGoldStandard;
    }
    private Canoniser propertyRangeCanoniser;

    public Canoniser getPropertyRangeCanoniser() {
        return propertyRangeCanoniser;
    }

    public void setPropertyRangeCanoniser(Canoniser propertyRangeCanoniser) {
        this.propertyRangeCanoniser = propertyRangeCanoniser;
    }
    private Canoniser propertyCanoniser;

    public Canoniser getPropertyCanoniser() {
        return propertyCanoniser;
    }

    public void setPropertyCanoniser(Canoniser propertyCanoniser) {
        this.propertyCanoniser = propertyCanoniser;
    }
    private Map<String, String> classHierarchy;

    public Map<String, String> getClassHierarchy() {
        return classHierarchy;
    }

    public void setClassHierarchy(Map<String, String> classHierarchy) {
        this.classHierarchy = classHierarchy;
    }
    private Map<Object, List<Object>> classGoldStandard = new HashMap<>();

    public void initialise(String webtableName, Table webtable, EvaluationParameters evaluationParameters) {

        if (webtableName.endsWith(".json")) {
            webtableName = webtableName.replace(".json", ".csv");
        }

        Map<String, String> superClassMap = new HashMap<>();
        Collection<String[]> superclasses = CSVUtils.readCSV(
                evaluationParameters.getClassHierarchyLocation(), "\t");
        for (String[] s : superclasses) {
            superClassMap.put(
                    s[0].replace("http://dbpedia.org/ontology/", "")
                    .toLowerCase(),
                    s[1].replace("http://dbpedia.org/ontology/", "")
                    .toLowerCase());
        }

        Collection<String[]> classCorres = CSVUtils.readCSV(evaluationParameters.getClassGoldStandardLocation());
        String webHeader;
        if (webtable.getHeader().contains(".csv")) {
            webHeader = webtable.getHeader().split("\\.csv")[0];
        } else {
            webHeader = webtable.getHeader().split("\\.")[0];
        }

        for (String[] s : classCorres) {
            String dbpHeader;
            if (s[0].contains(".csv")) {
                dbpHeader = s[0].split("\\.csv")[0];
            } else {
                dbpHeader = s[0].split("\\.")[0];
            }
            // System.out.println("web: " + webHeader + " dbpedia " +
            // dbpHeader);

            List<Object> allClasses = new ArrayList<>();
            if (webHeader != null && dbpHeader != null
                    && webHeader.equals(dbpHeader)) {
                if (s[1].toLowerCase().contains(" ")) {
                    s[1] = s[1].toLowerCase().replace(" ", "");
                }
                allClasses.add(s[1].toLowerCase());
                if (superClassMap.containsKey(s[1].toLowerCase())) {
                    if ((s[1]).contains(" ")) {
                        s[1] = s[1].toLowerCase().replace(" ", "");
                    }
                    allClasses.add(superClassMap.get(s[1].toLowerCase())
                            .toLowerCase());
                }
                System.out.println("GS: " + dbpHeader + "first clsas: " + allClasses.get(0));
                classGoldStandard.put(dbpHeader, allClasses);
            }
        }


        instanceGoldStandard = new HashMap<Object, Object>();

        String filePath = new File(evaluationParameters.getInstanceGoldStandardLocation(),
                webtableName).getAbsolutePath();
        instanceCanoniser = evaluationParameters.getEquivInstanceCanoniser();


        try {
            if (new File(filePath).exists()) {
                Collection<String[]> corres = CSVUtils.readCSV(filePath);
                for (String[] s : corres) {
                    String uri = s[0].replace("/page/", "/resource/");
                    uri = instanceCanoniser.canoniseResource(uri);
                    uri = DBpediaURIEncoder.encodeURIForDBpedia(uri);
                    String key = s[2];
//                    if (Variables.normalizeValues) {
//                        key = StringNormalizer.webStringNormalization(key);                        
//                    }
 //                   System.out.println("GS!!: " +uri);                   
                    instanceGoldStandard.put(key, uri);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("warning! goldstandard missing " + filePath);
        }

        propertyGoldStandard = new HashMap<Object, Object>();

        filePath = new File(evaluationParameters.getPropertyGoldStandardLocation(), webtableName).getAbsolutePath();

        try {
            propertyCanoniser = evaluationParameters.getEquivPropertyCanoniser();

            if (new File(filePath).exists()) {
                Collection<String[]> corres = CSVUtils.readCSV(filePath);

                // handle equivalent properties
                for (String[] s : corres) {
                    String uri = propertyCanoniser.canoniseResource(s[0]);
                    //String header = StringNormalizer.cleanWebHeader(StringNormalizer.simpleStringNormalization(s[1], false));
                    Integer index = Integer.parseInt(s[3]);
                    //???? TODO OR NOT?
                    //propertyGoldStandard.put(s[1].replaceAll("\\.", ""), uri);
                    System.out.println("prop: " + index + " --- " + uri);

                    propertyGoldStandard.put(index, uri);
                }

                // remove key-column mappings
                propertyGoldStandard.remove(webtable.getKeyIndex());
            }
        } catch (Exception e) {
            System.out.println("warning! goldstandard missing " + filePath);
        }

        propertyRangeGoldStandard = new HashMap<Object, Object>();

        filePath = new File(evaluationParameters.getPropertyRangeGoldstandardLocation(), webtableName).getAbsolutePath();

        try {
            propertyRangeCanoniser = new Canoniser();
            propertyRangeCanoniser.loadEquivalentResources(evaluationParameters.getPropertyRangesLocation());

            if (new File(filePath).exists()) {
                Collection<String[]> corres = CSVUtils.readCSV(filePath);

                // handle equivalent properties
                for (String[] s : corres) {
                    String uri = s[0];
                    String header = StringNormalizer.normaliseHeader(s[1]);
                    if (!header.equals(webtable.getKey().getHeader())) {
                        propertyRangeGoldStandard.put(header, uri);
                    }
                }
            }
            // remove key-column mappings not working?
            //propertyRangeGoldStandard.remove(webtable.getKey().getHeader());
        } catch (Exception e) {
            System.out.println("warning! goldstandard missing " + filePath);
        }

        try {
            if (new File(filePath).exists()) {
                Collection<String[]> lines = CSVUtils.readCSV(evaluationParameters.getClassHierarchyLocation(), "\t");

                classHierarchy = new HashMap<String, String>();

                for (String[] line : lines) {
                    classHierarchy.put(line[0].replace("http://dbpedia.org/ontology/", ""), line[1].replace("http://dbpedia.org/ontology/", ""));
                }
            }
        } catch (Exception e) {
            System.out.println("warning! goldstandard missing " + filePath);
        }

    }

    public void adjustToSubset(Set<String> instanceUris, Set<String> propertyUris) {
        Iterator<Entry<Object, Object>> it = getInstanceGoldStandard().entrySet().iterator();

        while (it.hasNext()) {
            Entry<Object, Object> e = it.next();

            //String key = (String)e.getKey();
            String uri = (String) e.getValue();

            if (!instanceUris.contains(uri)) {
                it.remove();
                System.out.println(String.format("Removing instance %s", uri));
            }
        }

        Iterator<Entry<Object, Object>> itP = getPropertyGoldStandard().entrySet().iterator();

        while (itP.hasNext()) {
            Entry<Object, Object> e = itP.next();

            //String key = (String)e.getKey();
            String uri = (String) e.getValue();

            if (!propertyUris.contains(uri)) {
                itP.remove();
                System.out.println(String.format("Removing property %s", uri));
            }
        }
    }

    public static void main(String args[]) throws IOException {
        String folderWithAllTables = args[0];
        String fileWithTables = args[1];
        String outputFolder = args[2];
        splitGoldStandard(folderWithAllTables, fileWithTables, outputFolder);
    }

    private static void splitGoldStandard(String all, String selected, String output) throws IOException {
        File allDir = new File(all);
        File selectedTabs = new File(selected);
        BufferedReader read = new BufferedReader(new FileReader(selectedTabs));
        List<String> tablesNames = new ArrayList<>();
        String line = read.readLine();
        while (line != null) {
            tablesNames.add(line);
            line = read.readLine();
        }
        for (File f : allDir.listFiles()) {
            if (tablesNames.contains(f.getName())) {
                copyFile(f, new File(output + "/" + f.getName()));
            }
        }
    }

    public static void copyFile(File in, File out) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(in).getChannel();
            outChannel = new FileOutputStream(out).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (inChannel != null) {
                    inChannel.close();
                }
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * @return the classGoldStandard
     */
    public Map<Object, List<Object>> getClassGoldStandard() {
        return classGoldStandard;
    }
}
