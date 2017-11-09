/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.dbpedia.algorithm;

import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.io.DefaultIndex;
import de.dwslab.T2K.matching.blocking.IdentityBlocking;
import de.dwslab.T2K.matching.blocking.TypeBasedBlocking;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.AlwaysMatchSimilarityFunction;
import de.dwslab.T2K.matching.dbpedia.Preprocessing;
import static de.dwslab.T2K.matching.dbpedia.algorithm.CandidateSelectionComponent.PAR_INITIAL_K;
import static de.dwslab.T2K.matching.dbpedia.algorithm.CandidateSelectionComponent.PAR_INITIAL_SIMILARITY_FUNCTION;
import static de.dwslab.T2K.matching.dbpedia.algorithm.CandidateSelectionComponent.PAR_INITIAL_STRING_FILTERING;
import static de.dwslab.T2K.matching.dbpedia.algorithm.CandidateSelectionComponent.PAR_INITIAL_THRESHOLD;
import static de.dwslab.T2K.matching.dbpedia.algorithm.CandidateSelectionComponent.PAR_INTIAL_EDIT_DIST;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_PROP_CANDIDATE_THRESHOLD;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_PROP_NUM_CANDIDATES;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_PROP_NUM_RESULTS;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_PROP_NUM_VOTES;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_PROP_VALUE_THRESHOLD;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_TABLE_TYPE;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateSelectionMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.DuplicateBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.LabelBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.TableCache;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.tableToTable.WebTableIndexEntry;
import de.dwslab.T2K.matching.dbpedia.tableToTable.WebTableValueIndex;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcherWithFiltering;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.tableprocessor.model.TableMapping;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.timer.Timer;
import de.uni_mannheim.informatik.dws.t2k.webtables.parsers.JsonTableParser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author dritze
 */
public class InterTableComponent {

    //new blocking: return a list with the tableRows of all tables that can be found in the tables
    private List<String> dbpediaClass;
    private MatchingParameters params;
    private Timer parentTimer;
    private List<TableColumn> possibleCols;
    private int numTabes = 1000;
    private Map<String, Integer> classes;
    private Map<String, Integer> classesWithOverlap;
    private Map<Table, MatchingData> matchingDataPerTable;
    private List<TableRow> mappedRows;
    private Map<String, Integer> headerCount;
    private IIndex tableIndex;
    private double minimalOverlap;
    private GoldStandard gs;
    private Set<String> foundInCorpus;
    private Map<Table, SimilarityMatrix<TableRow>> matricesAll;
    private MatchingData matchingDataOri;
    private Map<String,List<String>> labelsPerURI;
    private Map<String,List<String>> labelsURIWithTableName;
    private Map<String,Integer> labelsCount;

    public Map<Table, SimilarityMatrix<TableRow>> computeCandidates(Collection<TableRow> instancesToMatch, MatchingResult res) {

        possibleCols = new ArrayList<>();
        matchingDataPerTable = new HashMap<>();
        headerCount = new HashMap<>();
        foundInCorpus = new HashSet<>();
        Table query = null;
        matricesAll = new HashMap<>();
        labelsPerURI = new HashMap<>();
        labelsURIWithTableName = new HashMap<>();
        labelsCount = new HashMap<>();

        //TODO? if rows are matched to the same instance -> no need to compare it
        String searchString = "";

        for (TableRow row : instancesToMatch) {
            for(Correspondence<TableRow> c : res.getInstanceMappings()) {
                if(c.getFirst().getRowIndex()==row.getRowIndex()) {
                    continue;
                }
            }
            if (query == null) {
                query = row.getTable();
            }
            Object o = row.getKey();
            String keyVal = "";
            if (o instanceof List) {
                List list = (List) o;
                keyVal = list.get(0).toString();
            } else {
                keyVal = row.getKey().toString();
            }
            keyVal = keyVal.replaceAll("\\(.*\\)", "");
            if (keyVal.length() < 3) {
                continue;
            }
            //System.out.println("key for query: " +keyVal);
            searchString += keyVal + " ";
            if (StringUtils.countMatches(searchString, " ") + StringUtils.countMatches(searchString, "-") > 1022) {
                break;
            }
        }

        WebTableValueIndex di = new WebTableValueIndex(tableIndex, WebTableIndexEntry.ENTITY_LABELS_FIELD);
        di.setVerbose(false);
        di.setNumRetrievedDocsFromIndex(10000);
        di.setSearchExactMatches(false);
        di.setRemoveBrackets(true);
        di.setMaxEditDistance(0);
        List<WebTableIndexEntry> returnedResults = di.search(searchString);
//        
//        if(returnedResults.size()<1000) {
//            di.setNumRetrievedDocsFromIndex(10001);
//            returnedResults = di.search(searchString);
//        }
//        if(returnedResults.size()<1000) {
//            di.setNumRetrievedDocsFromIndex(10002);
//            returnedResults = di.search(searchString);
//        }

        //System.out.println("num res search: " + returnedResults.size() + " class " + getDbpediaClass() + " - " + query.getHeader());
        //System.out.println("instances size: " +instancesToMatch.size() + " - " + query.getHeader());
        Map<Table, SimilarityMatrix<TableRow>> matrices = new HashMap<>();
        SimilarityMatrix<TableRow> mapped = new SparseSimilarityMatrix<>(0,0);

        int i = 0;
        classes = new HashMap<>();
        classesWithOverlap = new HashMap<>();
        for (WebTableIndexEntry wti : returnedResults) {
            if (getClasses().containsKey(wti.getDbpediaClass())) {
                int count = getClasses().get(wti.getDbpediaClass());
                count++;
                getClasses().put(wti.getDbpediaClass(), count);
            } else {
                getClasses().put(wti.getDbpediaClass(), 1);
            }
            i++;
            if (i > numTabes) {
                break;
            }
            File f = new File("/home/dritze/2012Mappings/allTables/" + wti.getTable());
//            System.out.println(f.getName());
            Timer timer = Timer.getNamed("load other", parentTimer);
            TableMapping tm = new TableMapping();
            try {
                tm.readMapping(f.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
                return matrices;
            }

            if (tm.getMappedInstances().isEmpty()) {
                continue;
            }
            boolean overlap = false;
            double overlapping = 0;
            Collection<Correspondence<TableRow>> corres = res.getInstanceMappings();
            for (Pair<String, Double> p : tm.getMappedInstances().values()) {
                for (Correspondence<TableRow> c : corres) {
                    if (c.getSecond().getURI().toString().equals(p.getFirst())) {
                        overlap = true;
                        overlapping++;
                        //break;
                    }
                }
            }

            TableCache cache = TableCache.get();
            MatchingData dt = cache.getOrCreate(f.getAbsolutePath());
            //Table t = dt.loadWebTable(f.getAbsolutePath(), timer, params, (de.mannheim.uni.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.tableType) WebtableToDBpediaMatchingProcess.tableType.jsonWebTable, true);
            Table t = dt.getWebtable();
            
            if (!t.isHasKey()) {
                continue;
            }
            
            getMatchingDataPerTable().put(t, dt);
            
            int last = -1, diff = 0;
            HashMap<Integer, Pair<String, Double>> newInstanceMappings = new HashMap();
            for (Integer index : t.getKey().getValues().keySet()) {
                //System.out.println("index: " + index + " last " + last + " - " + diff + " - " + t.getKey().getValues().get(index));
                if (index - last - 1 > 0 || diff > 0) {
                    if (index - last - 1 > 0) {
                        diff += index - last - 1;
                    }
                    if (tm.getMappedInstances().get(index) == null || tm.getMappedInstances().get(index).getFirst() == null) {
                      //  System.out.println("continue");
                        last = index;
                        continue;
                    }
                    newInstanceMappings.put(index + diff, tm.getMappedInstances().get(index));
               //     System.out.println("new: " + tm.getMappedInstances().get(index).getFirst());
                } else {
                    if (tm.getMappedInstances().get(index) == null || tm.getMappedInstances().get(index).getFirst() == null) {
                  //      System.out.println("continue2");
                        last = index;
                        continue;
                    }
                    newInstanceMappings.put(index, tm.getMappedInstances().get(index));
              //       System.out.println("old");
                }
                last = index;
            }

            tm.setMappedInstances(newInstanceMappings);
            t.setMapping(tm);

            getMatricesAll().put(t, mapped);
            
            if(t.getHeader().contains("1346876860467_1346894357594_193.arc7618711425574255266#4249012_0_6121117076140750740.csv")) {
                for(TableRow tr : dt.getWebtableRowSet()) {
                    System.out.println("tab animal " + tr + " - " + tr.getRowIndex());
                }
                for(Integer it : tm.getMappedInstances().keySet()) {
                    System.out.println("map animal "+ it + " - " + tm.getMappedInstances().get(it).getFirst());
                }
            }
            
            
            for (Object o : getGs().getInstanceGoldStandard().keySet()) {
                Integer rowIndex = Integer.parseInt(o.toString());
                String URI = getGs().getInstanceGoldStandard().get(o).toString();
                Collection<Correspondence<TableRow>> corres2 = res.getInstanceMappings();
                boolean detected = false;
                for (Correspondence<TableRow> c : corres2) {
                    if (rowIndex == c.getFirst().getRowIndex()) {
                        detected = true;
                        break;
                    }
                }
                if (detected) {
                    continue;
                }
                
            }
            
//            for(TableRow tr : dt.getWebtableRowSet()) {
//                String [] lables = tr.getKey().toString().split("\\s");
//                for(String s : lables) {
//                    if(s.isEmpty()) {
//                        continue;
//                    }
//                    if(getLabelsCount().containsKey(s)) {
//                        int count = getLabelsCount().get(s);
//                        count++;
//                        getLabelsCount().put(s, count);
//                    }
//                    else{
//                        getLabelsCount().put(s, 1);
//                    }
//                }
//            }
            
            for (Integer indexTable : tm.getMappedInstances().keySet()) {           
                    Pair<String, Double> p = tm.getMappedInstances().get(indexTable);
                    String URI = p.getFirst();
                    if (p.getFirst().equals(URI)) {
                        foundInCorpus.add(URI);
                        if(getLabelsPerURI().containsKey(URI)) {
                            List<String> l = getLabelsPerURI().get(URI);
                            List<String> l2 = labelsURIWithTableName.get(URI);
                            if(t.getKey().getValues() == null || t.getKey().getValues().get(indexTable) == null) {
                             //   System.out.println("index error! " + getMatchingDataOri().getWebtable().getHeader()+ "\t" +f.getAbsolutePath() + "\t" + indexTable + "\t" + t.getKey().getValues());
                                continue;
                            }
                            l.add(t.getKey().getValues().get(indexTable).toString());
                            l2.add(t.getHeader());
                            getLabelsPerURI().put(URI, l);
                            labelsURIWithTableName.put(URI, l2);
                        }
                        else {
                            List<String> l = new ArrayList<>();
                            List<String> l2 = new ArrayList<>();
                            if(t.getKey().getValues() == null || t.getKey().getValues().get(indexTable) == null) {
                            //    System.out.println("index error! " + f.getAbsolutePath() + "\t" + indexTable + "\t" + t.getKey().getValues());
                                continue;
                            }
                            l.add(t.getKey().getValues().get(indexTable).toString());
                            l2.add(t.getHeader());
                            getLabelsPerURI().put(URI, l);
                            labelsURIWithTableName.put(URI, l2);
                        }
                        //for (Pair pair : tm.getMappedInstances().values()) {
//                         System.out.println("found in corpus! " + URI + "\t" + t.getKey().getValues().get(indexTable) + "\t" +
//                                matchingDataOri.getWebtable().getKey().getValues().get(rowIndex));
                        //}
                        //usually yes
                        //break;
                    }
                }
            
            if (!overlap || overlapping / corres.size() < minimalOverlap) {
                continue;
            }

            if (getClassesWithOverlap().containsKey(wti.getDbpediaClass())) {
                int count = getClassesWithOverlap().get(wti.getDbpediaClass());
                count++;
                getClassesWithOverlap().put(wti.getDbpediaClass(), count);
            } else {
                getClassesWithOverlap().put(wti.getDbpediaClass(), 1);
            }

//            for (Integer numCorres : tm.getMappedInstances().keySet()) {
//                System.out.println("numCorres: " + numCorres + tm.getMappedInstances().get(numCorres).getFirst());
//            }
            for (TableRow toMatch : instancesToMatch) {
                boolean alreadyHasACorrespondences = false;
                for (Correspondence<TableRow> corresInTab : res.getInstanceMappings()) {
//                  //the instance has a correspondence
                    if (toMatch.equals(corresInTab.getFirst())) {
                        //check all correspondences of the table coming from the index
                        for (Integer numCorres : tm.getMappedInstances().keySet()) {
                            Pair corresPair = tm.getMappedInstances().get(numCorres);
                            //they are mapped to the same instance
                            if (corresInTab.getSecond().getURI().toString().equals(corresPair.getFirst())) {
                                alreadyHasACorrespondences = true;
//                                if (t == null) {
//                                    t = dt.loadWebTable(f.getAbsolutePath(), timer, params, (de.mannheim.uni.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.tableType) WebtableToDBpediaMatchingProcess.tableType.webtable, true);
//                                    t.setMapping(tm);
//                                    for (Integer index : t.getMapping().getMappedProperties().keySet()) {
//                                        System.out.println("allMappingsCorpus\t" + t.getColumns().get(index).getHeader() + "\t" + t.getMapping().getMappedProperties().get(index).getFirst() + "\t" + toMatch.getTable().getHeader());
//                                    }
//                                }
                                mapped = new SparseSimilarityMatrix<>(instancesToMatch.size(), dt.getWebtableRowSet().size());

                                //put the tableRow in the matrix that shares the same DBpedia instance
                                for (TableRow check : dt.getWebtableRowSet()) {
                                    if (check.getRowIndex() == numCorres) {
                                        mapped.set(toMatch, check, 1.0);
                                    }
                                }
                            }
                        }
                    }
                }
                //eine row kann zwar in einer aber in anderen Tabellen eine Correspondence haben
            }
            if (mapped != null && mapped.getFirstDimension().size() > 0) {
                //System.out.println("filled in matrix: " + query.getHeader() + " - " +t.getHeader());
                matrices.put(t, mapped);
            }
        }

        return matrices;
    }

    /**
     * @return the dbpediaClass
     */
    public List<String> getDbpediaClass() {
        return dbpediaClass;
    }

    /**
     * @param dbpediaClass the dbpediaClass to set
     */
    public void setDbpediaClass(List<String> dbpediaClass) {
        this.dbpediaClass = dbpediaClass;
    }

    /**
     * @return the params
     */
    public MatchingParameters getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(MatchingParameters params) {
        this.params = params;
    }

    /**
     * @return the parentTimer
     */
    public Timer getParentTimer() {
        return parentTimer;
    }

    /**
     * @param parentTimer the parentTimer to set
     */
    public void setParentTimer(Timer parentTimer) {
        this.parentTimer = parentTimer;
    }

    /**
     * @return the possibleCols
     */
    public List<TableColumn> getPossibleCols() {
        return possibleCols;
    }

    /**
     * @param possibleCols the possibleCols to set
     */
    public void setPossibleCols(List<TableColumn> possibleCols) {
        this.possibleCols = possibleCols;
    }

    /**
     * @return the numTabes
     */
    public int getNumTabes() {
        return numTabes;
    }

    /**
     * @param numTabes the numTabes to set
     */
    public void setNumTabes(int numTabes) {
        this.numTabes = numTabes;
    }

    /**
     * @return the classes
     */
    public Map<String, Integer> getClasses() {
        return classes;
    }

    /**
     * @param classes the classes to set
     */
    public void setClasses(Map<String, Integer> classes) {
        this.classes = classes;
    }

    /**
     * @return the matchingDataPerTable
     */
    public Map<Table, MatchingData> getMatchingDataPerTable() {
        return matchingDataPerTable;
    }

    /**
     * @param matchingDataPerTable the matchingDataPerTable to set
     */
    public void setMatchingDataPerTable(Map<Table, MatchingData> matchingDataPerTable) {
        this.matchingDataPerTable = matchingDataPerTable;
    }

    /**
     * @return the tableIndex
     */
    public IIndex getTableIndex() {
        return tableIndex;
    }

    /**
     * @param tableIndex the tableIndex to set
     */
    public void setTableIndex(IIndex tableIndex) {
        this.tableIndex = tableIndex;
    }

    /**
     * @return the classesWithOverlap
     */
    public Map<String, Integer> getClassesWithOverlap() {
        return classesWithOverlap;
    }

    /**
     * @param classesWithOverlap the classesWithOverlap to set
     */
    public void setClassesWithOverlap(Map<String, Integer> classesWithOverlap) {
        this.classesWithOverlap = classesWithOverlap;
    }

    /**
     * @return the minimalOverlap
     */
    public double getMinimalOverlap() {
        return minimalOverlap;
    }

    /**
     * @param minimalOverlap the minimalOverlap to set
     */
    public void setMinimalOverlap(double minimalOverlap) {
        this.minimalOverlap = minimalOverlap;
    }

    /**
     * @return the gs
     */
    public GoldStandard getGs() {
        return gs;
    }

    /**
     * @param gs the gs to set
     */
    public void setGs(GoldStandard gs) {
        this.gs = gs;
    }

    /**
     * @return the foundInCorpus
     */
    public Set<String> getFoundInCorpus() {
        return foundInCorpus;
    }

    /**
     * @param foundInCorpus the foundInCorpus to set
     */
    public void setFoundInCorpus(Set<String> foundInCorpus) {
        this.foundInCorpus = foundInCorpus;
    }

    /**
     * @return the matricesAll
     */
    public Map<Table, SimilarityMatrix<TableRow>> getMatricesAll() {
        return matricesAll;
    }

    /**
     * @param matricesAll the matricesAll to set
     */
    public void setMatricesAll(Map<Table, SimilarityMatrix<TableRow>> matricesAll) {
        this.matricesAll = matricesAll;
    }

    /**
     * @return the matchingDataOri
     */
    public MatchingData getMatchingDataOri() {
        return matchingDataOri;
    }

    /**
     * @param matchingDataOri the matchingDataOri to set
     */
    public void setMatchingDataOri(MatchingData matchingDataOri) {
        this.matchingDataOri = matchingDataOri;
    }

    /**
     * @return the labelsPerURI
     */
    public Map<String,List<String>> getLabelsPerURI() {
        return labelsPerURI;
    }

    /**
     * @return the labelsURIWithTableName
     */
    public Map<String,List<String>> getLabelsURIWithTableName() {
        return labelsURIWithTableName;
    }

    /**
     * @return the labelsCount
     */
    public Map<String,Integer> getLabelsCount() {
        return labelsCount;
    }

}
