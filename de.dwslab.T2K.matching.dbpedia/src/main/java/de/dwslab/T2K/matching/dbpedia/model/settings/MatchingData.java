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
package de.dwslab.T2K.matching.dbpedia.model.settings;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.components.Preprocessing;
import de.dwslab.T2K.matching.dbpedia.components.WebtableToDBpediaMatchingProcess;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowUriMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableToRowHierarchyAdapter;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.IO.TableWriter;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.tableprocessor.model.TableColumn.ColumnDataType;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.timer.Timer;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * contains all data that is used by the matching algorithm
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
    
    private Map<String, TableRow> candidateMap;
    public Map<String, TableRow> getCandidateMap() {
        return candidateMap;
    }
    public void setCandidateMap(Map<String, TableRow> candidateMap) {
        this.candidateMap = candidateMap;  
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
    
    
    public MatchingData shallowCopy() {
        MatchingData d = new MatchingData();
        d.setCandidateMap(candidateMap);
        d.setDbpediaColSet(dbpediaColSet);
        d.setDbpediaPropUris(dbpediaPropUris);
        d.setDbpediaRowSet(dbpediaRowSet);
        d.setDbpediaTables(dbpediaTables);
        d.setEquivalentProperties(equivalentProperties);
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
                    dbpediaColSet.addAll(t.getColumns());
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
            } catch(Exception e) {
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
                dbpediaRowSet.addAll(rowAdapter.getParts(t));
                dbpediaColSet.addAll(t.getColumns());
            }
        }

        HashSet<String> dbpediaPropUris = new HashSet<String>();
        setDbpediaPropUris(dbpediaPropUris);
        
        for(Table t : dbpediaTables) {
            List<String> colsToRemove = new LinkedList<String>();
            for(TableColumn c : t.getColumns()) {
                if(c.getHeader().endsWith("_label")) {
                    colsToRemove.add(c.getHeader().substring(0, c.getHeader().indexOf("_label")));
                }
            }
            
            Iterator<TableColumn> it = t.getColumns().iterator();
            while(it.hasNext()) {
                TableColumn c = it.next();
                
                if(colsToRemove.contains(c.getHeader())) {
                    //System.out.println("removed " + c.getHeader());
                    it.remove();
                    dbpediaColSet.remove(c);
                }
                
                if(c.getDataType()==ColumnDataType.list) {
                    c.setDataType(ColumnDataType.string);
                }
                
                dbpediaPropUris.add(c.getURI());
            }
        }
        
        initialiseLookup(dbpediaRowSet);
        
        System.out.println("Loading DBpedia ... done");
        System.err.println("Loading DBpedia ... done");
    }

    public void initialiseLookup(Collection<TableRow> candidates) {
        if(candidateMap==null) {
            candidateMap = new HashMap<String, TableRow>();
    
            MatchingAdapter<TableRow> keyAdapter = new TableRowUriMatchingAdapter(); 
            
            // add all candidates to a hashmap indexed with their labels
            for (TableRow c : candidates) {
                if (c != null) {
                    Object label = keyAdapter.getLabel(c);
                    
                    if (label != null) {
                        candidateMap.put(label.toString(), c);
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
                        getEquivalentProperties().get(values[1].replace("<", "").replace(">", "")).add(new Pair(dbpediaProps.get(values[0].replace("<", "").replace(">", "")),1.0));
                }
                else {
                    List<Pair<TableColumn, Double>> l = new ArrayList();
                        l.add(new Pair(dbpediaProps.get(values[0].replace("<", "").replace(">", "")),1.0));
                        getEquivalentProperties().put(values[1].replace("<", "").replace(">", ""), l);
                    }
                }
                line = read.readLine();
            }
            }catch(Exception e) {
                e.printStackTrace();
            }
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
            if(type==WebtableToDBpediaMatchingProcess.tableType.webtable){
                webtable = r.readWebTable(webtablePath);
            }
            else {
                webtable = r.readLODTable(webtablePath);
            }
            if(webtable!=null) {
                setWebtable(webtable);
    
                for(TableColumn c : webtable.getColumns()) {
                    if(c.getDataType()==ColumnDataType.list) {
                        c.setDataType(ColumnDataType.string);
                    }
                }
                
                Preprocessing.addSurfaceForms(webtable, false, this);
                Preprocessing.addRedirects(webtable, false, this);
                            
                Collection<TableRow> webtableRowSet = rowAdapter.getParts(webtable);
                setWebtableRowSet(webtableRowSet);
                Collection<Table> webTables = new ArrayList<Table>();
                setWebTables(webTables);
                webTables.add(webtable);
    
                if (params.isCollectMatchingInfo()) {
                    System.out.println(webtable.printTable());
                    TableWriter w = new TableWriter();
                    w.writeTable(webtable, "preprocessed_table.csv",false);
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
}
