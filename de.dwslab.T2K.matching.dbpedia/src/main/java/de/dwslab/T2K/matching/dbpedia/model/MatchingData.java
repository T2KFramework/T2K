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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.Preprocessing;
import de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess;
import de.dwslab.T2K.matching.dbpedia.model.adapters.DBpediaInstanceAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowUriMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableToRowHierarchyAdapter;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.IO.TableWriter;
import de.dwslab.T2K.tableprocessor.model.Statistic;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.tableprocessor.model.TableColumn.ColumnDataType;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.io.CSVUtils;
import de.dwslab.T2K.utils.timer.Timer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

/**
 * contains all data that is used by the matching algorithm
 *
 * @author Oliver
 *
 */
public class MatchingData {

    private Collection<Table> dbpediaTables = null;

    public Collection<Table> getDbpediaTables() {
        return dbpediaTables;
    }

    public void setDbpediaTables(Collection<Table> dbpediaTables) {
        this.dbpediaTables = dbpediaTables;
    }
    private Collection<TableRow> dbpediaRowSet = null;

    public Collection<TableRow> getDbpediaRowSet() {
        return dbpediaRowSet;
    }

    public void setDbpediaRowSet(Collection<TableRow> dbpediaRowSet) {
        this.dbpediaRowSet = dbpediaRowSet;
    }
    private Collection<TableColumn> dbpediaColSet = null;

    public Collection<TableColumn> getDbpediaColSet() {
        return dbpediaColSet;
    }

    public void setDbpediaColSet(Collection<TableColumn> dbpediaColSet) {
        this.dbpediaColSet = dbpediaColSet;
    }
    private Collection<TableColumn> dbpediaColSetWithAdds = null;

    public Collection<TableColumn> getDbpediaColSetWithAdds() {
        return dbpediaColSetWithAdds;
    }

    public void setDbpediaColSetWithAdds(Collection<TableColumn> dbpediaColSetWithAdds) {
        this.dbpediaColSetWithAdds = dbpediaColSetWithAdds;
    }

    private Set<String> dbpediaPropUris = null;

    public Set<String> getDbpediaPropUris() {
        return dbpediaPropUris;
    }

    public void setDbpediaPropUris(Set<String> dbpediaPropUris) {
        this.dbpediaPropUris = dbpediaPropUris;
    }
    private Table webtable = null;

    public Table getWebtable() {
        return webtable;
    }

    public void setWebtable(Table webtable) {
        this.webtable = webtable;
    }
    private Collection<TableRow> webtableRowSet = null;

    public Collection<TableRow> getWebtableRowSet() {
        return webtableRowSet;
    }

    public void setWebtableRowSet(Collection<TableRow> webtableRowSet) {
        this.webtableRowSet = webtableRowSet;
    }
    private Collection<Table> webTables = null;

    public Collection<Table> getWebTables() {
        return webTables;
    }

    public void setWebTables(Collection<Table> webTables) {
        this.webTables = webTables;
    }
    private Map<String, List<TableRow>> candidateMap;

    public Map<String, List<TableRow>> getCandidateMap() {
        return candidateMap;
    }

    public void setCandidateMap(Map<String, List<TableRow>> candidateMap) {
        this.candidateMap = candidateMap;
    }
    private Map<String, List<TableRow>> uriMap;

    public Map<String, List<TableRow>> getUriMap() {
        return uriMap;
    }

    public void setUriMap(Map<String, List<TableRow>> uriMap) {
        this.uriMap = uriMap;
    }
    private Map<String, List<Pair<TableColumn, Double>>> equivalentProperties;

    /**
     * @return the equivalentProperties
     */
    public Map<String, List<Pair<TableColumn, Double>>> getEquivalentProperties() {
        return equivalentProperties;
    }

    /**
     * @param aEquivalentProperties the equivalentProperties to set
     */
    public void setEquivalentProperties(Map<String, List<Pair<TableColumn, Double>>> aEquivalentProperties) {
        equivalentProperties = aEquivalentProperties;
    }
    private Map<Integer, String> dictionaryMap = new HashMap();
    private Map<String, List<String>> overallTermMap = new HashMap<>();
    private Map<String, Integer> documentTermCount = new HashMap();
    private Map<String, Map<String, Double>> vectors = new HashMap();
    private Map<String, Map<String, Double>> classDocuments = new HashMap();
    private Map<String, Integer> documentClassTermCount = new HashMap();
    private Map<String, List<String>> NEPropertyRanges = new HashMap();
    private Map<String, Map<String, Double>> classCococ = new HashMap<>();

    public MatchingData shallowCopy() {
        MatchingData d = new MatchingData();
        d.setCandidateMap(candidateMap);
        d.setDbpediaColSet(dbpediaColSet);
        d.setDbpediaColSetWithAdds(dbpediaColSetWithAdds);
        d.setDbpediaPropUris(dbpediaPropUris);
        d.setDbpediaRowSet(dbpediaRowSet);
        d.setDbpediaTables(dbpediaTables);
        d.setEquivalentProperties(equivalentProperties);
        d.setDictionaryMap(dictionaryMap);
        d.setDocumentTermCount(documentTermCount);
        d.setOverallTermMap(overallTermMap);
        d.setVectors(vectors);
        d.setClassDocuments(classDocuments);
        d.setDocumentClassTermCount(documentClassTermCount);
        d.setNEPropertyRanges(NEPropertyRanges);
        d.setUriMap(uriMap);
        d.setClassCococ(classCococ);
        return d;
    }

    @SuppressWarnings("unchecked")
    public void loadDBpedia(String dbpediaDirectory, MatchingParameters params) {
        System.out.println("Loading DBpedia ... ");
        System.err.println("Loading DBpedia ... ");

        dbpediaTables = new ArrayList<Table>();
        dbpediaRowSet = new ArrayList<TableRow>();
        dbpediaColSet = new ArrayList<TableColumn>();

        TableToRowHierarchyAdapter rowAdapter = new TableToRowHierarchyAdapter();

        // load dbpedia
        File directory = new File(dbpediaDirectory);

        if (directory.isDirectory()) {
            TableReader r = new TableReader();
            r.setUseUnitDetection(params.isUseUnitDetection());

            for (File f : directory.listFiles()) {
                System.out.println("loading " + f.getAbsolutePath());
                Table t;
                try {
                    t = r.readLODTable(f.getPath());
                    Preprocessing p = new Preprocessing();
                    p.handleIncorrectDates(t);
                    dbpediaTables.add(t);
                    dbpediaRowSet.addAll(rowAdapter.getParts(t));
                    for (TableColumn c : t.getColumns()) {
                        if (c.getURI().contains("http://dbpedia.org/property/")) {
                            continue;
                        }
                        dbpediaColSet.add(c);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {

            try {
                Kryo kryo = new Kryo();

                Input input = new Input(new FileInputStream(dbpediaDirectory));
                dbpediaTables = kryo.readObject(input, ArrayList.class);
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            ObjectInputStream oin;
//            try {
//                oin = new ObjectInputStream(new FileInputStream(
//                        dbpediaDirectory));
//                dbpediaTables = (ArrayList<Table>) oin.readObject();
//                oin.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            for (Table t : dbpediaTables) {
                //TODO: used to compute uniqueness of labels                
                writeStats(t);
                dbpediaRowSet.addAll(rowAdapter.getParts(t));
                for (TableColumn c : t.getColumns()) {
                    if (c.getURI().contains("http://dbpedia.org/property/")) {
                        continue;
                    }
                    dbpediaColSet.add(c);
                }
            }
        }
        HashSet<String> dbpediaPropUris = new HashSet<String>();
        setDbpediaPropUris(dbpediaPropUris);

        for (Table t : dbpediaTables) {
            List<String> colsToRemove = new LinkedList<String>();
            for (TableColumn c : t.getColumns()) {
                if (c.getHeader().toString().endsWith("_label")) {
                    colsToRemove.add(c.getHeader().toString().substring(0, c.getHeader().toString().indexOf("_label")));
                }
            }

            Iterator<TableColumn> it = t.getColumns().iterator();
            while (it.hasNext()) {
                TableColumn c = it.next();

                if (colsToRemove.contains(c.getHeader())) {
                    //System.out.println("removed " + c.getHeader());
                    it.remove();
                    dbpediaColSet.remove(c);
                }

                if (c.getDataType() == ColumnDataType.list) {
                    c.setDataType(ColumnDataType.string);
                }

                Statistic s = new Statistic();
                Set<String> allValues = new HashSet<>();
                for (Object o : c.getValues().values()) {
                    if (o instanceof List) {
                        for (Object innerObject : (List) o) {
                            allValues.add(innerObject.toString());
                        }
                    } else {
                        allValues.add(o.toString());
                    }
                }
                double sum = 0.0;
                for (String value : allValues) {
                    sum += value.length();
                }
                s.setAverageValueLength((double) sum / allValues.size());
                c.setColumnStatistic(s);

                dbpediaPropUris.add(c.getURI());
            }
        }
        initialiseLookup(dbpediaRowSet);
        //currently not needed!
        //initializeAbstracts();

//        dbpediaColSetWithAdds = new ArrayList<TableColumn>();
//        for (TableColumn t : dbpediaColSet) {
//            dbpediaColSetWithAdds.add(t.clone());
//        }
//        addDict();
        System.out.println("Loading DBpedia ... done");
        System.err.println("Loading DBpedia ... done");
    }

    public synchronized void writeStats(Table t) {
        BufferedWriter writeStats = null;
        try {
            writeStats = new BufferedWriter(new FileWriter(new File("stats.txt"), true));

            int dateCols = 0, numericCols = 0, stringCols = 0;
            for (TableColumn tc : t.getColumns()) {
                switch (tc.getDataType()) {
                    case date:
                        dateCols++;
                        break;
                    case numeric:
                        numericCols++;
                        break;
                    case string:
                        stringCols++;
                        break;
                }
            }

            writeStats.write(t.getHeader() + "\t" + t.getColumns().size() + "\t" + t.getKey().getValues().keySet().size() + "\t" + dateCols + "\t" + numericCols + "\t" + stringCols + "\n");
//            Map<String, Integer> nameSet = new HashMap<>();            
//            
//            Map<Integer, Object> pairs = t.getColumns().get(t.getKeyIndex()).getValues();
//            for (Integer i : pairs.keySet()) {
//                String name = (String) pairs.get(i);
//                name = name.replace("\\(.*\\)", "");
//                name = name.trim();
//                //other possibility, the name is contained!
//                if (nameSet.containsKey(name)) {
//                    int counter = nameSet.get(name);
//                    counter++;
//                    nameSet.put(name, counter);
//                } else {
//                    nameSet.put(name, 1);
//                }
//            }
//            double countSingle = 0, countMultiple = 0;
//            for (String s : nameSet.keySet()) {
//                if (nameSet.get(s) == 1) {
//                    countSingle++;
//                } else {
//                    countMultiple++;
//                }
//            }
//            double avg = (double) nameSet.size() / (double) t.getKey().getNumRows();
//            double countSingleAmount = countSingle / (double) t.getKey().getNumRows();
//            double countMultipleAmount = countMultiple / (double) t.getKey().getNumRows();
//            writeStats.write(t.getHeader() + "\t" + t.getKey().getNumRows() + "\t" + nameSet.size() + "\t" + avg + "\t" + countSingle + "\t" + countMultiple
//                    + "\t" + countSingleAmount + "\t" + countMultipleAmount + "\n");

            writeStats.flush();
            writeStats.close();
        } catch (Exception e) {
            //          System.out.println(t.getHeader() + " --- " + t.getKeyIndex() + " --- " + t.getColumns().size());
        }
    }

    public void initializeAbstracts() {
        try {

            if (getDictionaryMap().isEmpty()) {

                String filePath = "outputDicLowmaxDF";
                Collection<String[]> corres = CSVUtils.readCSV(filePath, "\t");

                String term;
                int idx;

                for (String[] s : corres) {
                    if (s.length < 2) {
                        continue;
                    }
                    try {
                        term = s[0];
                        //docFreq = Integer.parseInt(s[1]);
                        idx = Integer.parseInt(s[2]);
                        getDocumentTermCount().put(term, Integer.parseInt(s[1]));
                        getDictionaryMap().put(idx, term);
                    } catch (Exception e) {
                        System.out.println(s);
                        e.printStackTrace();
                    }
                }
            }

            if (getOverallTermMap().isEmpty()) {

                Configuration conf = new Configuration();
                FileSystem fs = FileSystem.get(conf);
                String vectorsPath = "outputVectorMaxDF";

                SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(vectorsPath), conf);

                LongWritable key = new LongWritable();
                VectorWritable value = new VectorWritable();
                while (reader.next(key, value)) {
                    NamedVector namedVector = (NamedVector) value.get();
                    Map<String, Double> termMap = new HashMap<>();
                    getVectors().put(namedVector.getName(), termMap);
                    RandomAccessSparseVector vect = (RandomAccessSparseVector) namedVector.getDelegate();
                    //System.out.println("size: " +vect.size());
                    Iterator<Vector.Element> iter = vect.iterateNonZero();
                    while (iter.hasNext()) {
                        Vector.Element e = iter.next();
                        termMap.put(getDictionaryMap().get(e.index()), e.get());
                        if (getOverallTermMap().containsKey(getDictionaryMap().get(e.index()))) {
                            getOverallTermMap().get(getDictionaryMap().get(e.index())).add(namedVector.getName());
                        } else {
                            List<String> newList = new ArrayList<>();
                            newList.add(namedVector.getName());
                            getOverallTermMap().put(getDictionaryMap().get(e.index()), newList);
                        }
                    }
                }
                reader.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeClassDocuments() {
        try {
            if (getClassDocuments().isEmpty()) {
                BufferedReader read = new BufferedReader(new FileReader(new File("classDocuments/termCountPerClass.txt")));
                String line = read.readLine();
                while (line != null) {
                    String[] classInfo = line.split("csv.gz\\t\\{");
                    String className = classInfo[0] + "csv.gz";
                    Map<String, Double> tfIdfsPerTerm = new HashMap<>();
                    String[] terms = classInfo[1].split("\\|");
                    for (String term : terms) {
                        String[] values = term.split("\\t");
                        if (values.length == 2) {
                            tfIdfsPerTerm.put(values[0], Double.parseDouble(values[1]));
                        }
                    }
                    classDocuments.put(className, tfIdfsPerTerm);
                    line = read.readLine();
                }
            }
            if (getDocumentClassTermCount().isEmpty()) {
                String filePath = "classDocuments/docFreq.txt";
                Collection<String[]> corres = CSVUtils.readCSV(filePath, "\t");

                String term;

                for (String[] s : corres) {
                    if (s.length < 1) {
                        continue;
                    }
                    try {
                        term = s[0];
                        getDocumentClassTermCount().put(term, Integer.parseInt(s[1]));
                    } catch (Exception e) {
                        System.out.println(s);
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialiseLookup(Collection<TableRow> candidates) {
        if (candidateMap == null) {

            candidateMap = new HashMap();
            uriMap = new HashMap<>();

            MatchingAdapter<TableRow> keyAdapter = new TableRowUriMatchingAdapter();
            DBpediaInstanceAdapter uriAdapter = new DBpediaInstanceAdapter();

            // add all candidates to a hashmap indexed with their labels
            for (TableRow c : candidates) {
                if (c != null) {

                    Object label = keyAdapter.getLabel(c);
                    Object uri = uriAdapter.getUniqueIdentifier(c);

                    if (label != null) {
                        if (candidateMap.keySet().contains(label.toString())) {
                            candidateMap.get(label.toString()).add(c);
                        } else {
                            List<TableRow> valueList = new ArrayList<>();
                            valueList.add(c);
                            candidateMap.put(label.toString(), valueList);
                        }
                    }
                    if (uri != null) {
                        if (uriMap.keySet().contains(uri.toString())) {
                            uriMap.get(uri.toString()).add(c);
                        } else {
                            List<TableRow> valueList = new ArrayList<>();
                            valueList.add(c);
                            uriMap.put(uri.toString(), valueList);
                        }
                    }
                }
            }
        }
    }

    public void loadEquivalentProperties(String path) {
        //key: web table column, values: DBpedia properties + confidence vlaue
        if (getEquivalentProperties() == null) {
            equivalentProperties = new HashMap<>();
            Map<String, TableColumn> dbpediaProps = new HashMap<>();
            for (TableColumn c : getDbpediaColSet()) {
                dbpediaProps.put(c.getURI(), c);
            }
            try {
                BufferedReader read = new BufferedReader(new FileReader(new File(path)));
                String line = read.readLine();
                while (line != null) {
                    String[] values = line.split(" <http://www.w3.org/2002/07/owl#equivalentProperty> ");
                    if (dbpediaProps.containsKey(values[0].replace("<", "").replace(">", ""))) {
                        if (getEquivalentProperties().containsKey(values[1].replace("<", "").replace(">", ""))) {
                            getEquivalentProperties().get(values[1].replace("<", "").replace(">", "")).add(new Pair(dbpediaProps.get(values[0].replace("<", "").replace(">", "")), 1.0));
                        } else {
                            List<Pair<TableColumn, Double>> l = new ArrayList();
                            l.add(new Pair(dbpediaProps.get(values[0].replace("<", "").replace(">", "")), 1.0));
                            getEquivalentProperties().put(values[1].replace("<", "").replace(">", ""), l);
                        }
                    }
                    line = read.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadClassCococ() {
        File f = new File("classCocosPLDAggregated.tsv");
        Map<String, Map<String, Double>> classCocosCond = new HashMap<>();

        try {
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line = read.readLine();
            while (line != null) {
                String cont[] = line.split("\t");
                String className = cont[0].split(";")[0].replace("http://dbpedia.org/ontology/", "");
                String propURI = cont[0].split(";")[1];
                if (!propURI.contains("dbpedia.org/ontology") || propURI.contains("wikiPageID") || propURI.contains("wikiPageRevisionID")) {
                    line = read.readLine();
                    continue;
                }
                double value = Double.parseDouble(cont[1]);
                if (classCocosCond.containsKey(className)) {
                    classCocosCond.get(className).put(propURI, value);
                } else {
                    Map<String, Double> internalHashMap = new HashMap<>();
                    internalHashMap.put(propURI, value);
                    classCocosCond.put(className, internalHashMap);
                }
                line = read.readLine();
            }
            classCococ = classCocosCond;
        } catch (Exception e) {
            System.out.println("class cocos gone wrong ");
            e.printStackTrace();
        }

    }

    public void loadWebTable(String webtablePath, Timer tim, MatchingParameters params, WebtableToDBpediaMatchingProcess.tableType type) {
        Timer t = Timer.getNamed("loadWebTable", tim);
        TableReader r = new TableReader();
        r.setUseUnitDetection(params.isUseUnitDetection());
        r.setSpanningCellThreshold(params.getMaxSpanningCells());

        TableToRowHierarchyAdapter rowAdapter = new TableToRowHierarchyAdapter();

        try {
            Table webtable;

            switch (type) {
                case webtable:
                    webtable = r.readWebTable(webtablePath);
                    break;
                case jsonWebTable:
                    webtable = r.readWebTableFromJson(webtablePath);
                    break;
                case lodtable:
                    webtable = r.readLODTable(webtablePath);
                    break;
                default:
                    System.out.println("Unknown table type!");
                    webtable = null;
                    break;
            }
//            if(type==WebtableToDBpediaMatchingProcess.tableType.webtable){
//                webtable = r.readWebTable(webtablePath);
//            }
//            else {
//                webtable = r.readLODTable(webtablePath);
//            }

            if (webtable != null) {
                setWebtable(webtable);

                for (TableColumn c : webtable.getColumns()) {
                    if (c.getDataType() == ColumnDataType.list) {
                        c.setDataType(ColumnDataType.string);
                    }
                }

                Preprocessing.addSurfaceForms(webtable, true, this, true);
                Preprocessing.addRedirects(webtable, true, this, true);

                Collection<TableRow> webtableRowSet = rowAdapter.getParts(webtable);
                setWebtableRowSet(webtableRowSet);
                Collection<Table> webTables = new ArrayList<Table>();
                setWebTables(webTables);
                webTables.add(webtable);

                System.out.println(String.format("Matching %s: %,d rows, %,d columns", webtablePath, webtableRowSet.size(), webtable.getColumns().size()));

                if (params.isCollectMatchingInfo()) {
                    System.out.println(webtable.printTable());
                    TableWriter w = new TableWriter();
                    w.writeTable(webtable, "preprocessed_table.csv", false);
                }
            } else {
                setWebtableRowSet(null);
                setWebTables(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setWebtable(null);
            setWebtableRowSet(null);
            setWebTables(null);
        }

        t.stop();
    }

    public void addDict() {
        File f = new File("dict.csv");
        try {
            Map<String, List<String>> additionalNames = new HashMap<>();
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line = read.readLine();
            while (line != null) {
                String[] content = null;
                try {
                    content = line.split("\t");
                } catch (Exception e1) {
                    e1.printStackTrace();
                    System.out.println("no tab! " + line);
                }
                List<String> names = new ArrayList<>();
                for (int i = 1; i < content.length; i++) {
                    if (!content[i].isEmpty()) {
                        names.add(content[i]);
                    }
                }
                if (content[0].contains("dbpedia.org/ontology") && !content[0].contains("wikiPage")) {
                    additionalNames.put(content[0], names);
                }
                line = read.readLine();
            }
            System.out.println(additionalNames.size());

            for (TableColumn d : dbpediaColSet) {
                if (additionalNames.containsKey(d.getURI())) {
                    additionalNames.get(d.getURI()).add(d.getHeader().toString());
                    d.setHeaderList(additionalNames.get(d.getURI()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the dictionaryMap
     */
    public Map<Integer, String> getDictionaryMap() {
        return dictionaryMap;
    }

    /**
     * @return the overallTermMap
     */
    public Map<String, List<String>> getOverallTermMap() {
        return overallTermMap;
    }

    /**
     * @return the documentTermCount
     */
    public Map<String, Integer> getDocumentTermCount() {
        return documentTermCount;
    }

    /**
     * @return the vectors
     */
    public Map<String, Map<String, Double>> getVectors() {
        return vectors;
    }

    /**
     * @param vectors the vectors to set
     */
    public void setVectors(Map<String, Map<String, Double>> vectors) {
        this.vectors = vectors;
    }

    /**
     * @param dictionaryMap the dictionaryMap to set
     */
    public void setDictionaryMap(Map<Integer, String> dictionaryMap) {
        this.dictionaryMap = dictionaryMap;
    }

    /**
     * @param overallTermMap the overallTermMap to set
     */
    public void setOverallTermMap(Map<String, List<String>> overallTermMap) {
        this.overallTermMap = overallTermMap;
    }

    /**
     * @param documentTermCount the documentTermCount to set
     */
    public void setDocumentTermCount(Map<String, Integer> documentTermCount) {
        this.documentTermCount = documentTermCount;
    }

    /**
     * @return the classDocuments
     */
    public Map<String, Map<String, Double>> getClassDocuments() {
        return classDocuments;
    }

    /**
     * @param classDocuments the classDocuments to set
     */
    public void setClassDocuments(Map<String, Map<String, Double>> classDocuments) {
        this.classDocuments = classDocuments;
    }

    /**
     * @return the documentClassTermCount
     */
    public Map<String, Integer> getDocumentClassTermCount() {
        return documentClassTermCount;
    }

    /**
     * @param documentClassTermCount the documentClassTermCount to set
     */
    public void setDocumentClassTermCount(Map<String, Integer> documentClassTermCount) {
        this.documentClassTermCount = documentClassTermCount;
    }

    /**
     * @return the NEPropertyRanges
     */
    public Map<String, List<String>> getNEPropertyRanges() {
        return NEPropertyRanges;
    }

    /**
     * @param NEPropertyRanges the NEPropertyRanges to set
     */
    public void setNEPropertyRanges(Map<String, List<String>> NEPropertyRanges) {
        this.NEPropertyRanges = NEPropertyRanges;
    }

    /**
     * @return the classCococ
     */
    public Map<String, Map<String, Double>> getClassCococ() {
        return classCococ;
    }

    /**
     * @param classCococ the classCococ to set
     */
    public void setClassCococ(Map<String, Map<String, Double>> classCococ) {
        this.classCococ = classCococ;
    }
}
