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
public class InterTableComponentForInst {

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

    public Map<Table, SimilarityMatrix<TableRow>> computeCandidates(Collection<TableRow> instancesToMatch, MatchingResult res) {

        possibleCols = new ArrayList<>();
        matchingDataPerTable = new HashMap<>();
        headerCount = new HashMap<>();
        foundInCorpus = new HashSet<>();
        Table query = null;

        //TODO? if rows are matched to the same instance -> no need to compare it
        String searchString = "";

//        Set<TableRow> toRemove = new HashSet<>();
//        for(TableRow t : instancesToMatch) {
//            toRemove.add(t)
//        }
        for (TableRow row : instancesToMatch) {
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
        SimilarityMatrix<TableRow> mapped = null;

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
            MatchingData dt = new MatchingData();
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

            for (Object o : getGs().getInstanceGoldStandard().keySet()) {
                String URI = getGs().getInstanceGoldStandard().get(o).toString();
                Collection<Correspondence<TableRow>> corres = res.getInstanceMappings();
                boolean detected = false;
                for (Correspondence<TableRow> c : corres) {
                    if (o.toString().equals(String.valueOf(c.getFirst().getRowIndex()))) {
                        detected = true;
                        break;
                    }
                }
                if (detected) {
                    continue;
                }
                for (Pair<String, Double> p : tm.getMappedInstances().values()) {
                    if (p.getFirst().equals(URI)) {
                        foundInCorpus.add(URI);
                        for (Pair pair : tm.getMappedInstances().values()) {
                            System.out.println("found in corpus! " + URI + " - " + f.getAbsolutePath() + " . " + pair.getFirst());
                        }
                        break;
                    }
                }
            }

            //Table t = dt.loadWebTable(f.getAbsolutePath(), timer, params, (de.mannheim.uni.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.tableType) WebtableToDBpediaMatchingProcess.tableType.jsonWebTable, true);
            Table t = dt.loadWebTable(f.getAbsolutePath(), timer, params, (de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.tableType) WebtableToDBpediaMatchingProcess.tableType.webtable, true);
            matchingDataPerTable.put(t, dt);

            if(f.getName().equals("1350433107026_1350497272807_318.arc4727466956293543108#6899771_4_6843721927200132921.csv")) {
                System.out.println("table loaded");
            }
            
            if (!t.isHasKey()) {
                continue;
            }

            int last = -1, diff = 0;
            HashMap<Integer, Pair<String, Double>> newInstanceMappings = new HashMap();
            for (Integer index : t.getKey().getValues().keySet()) {
                //   System.out.println("index: " + index + " last " + last + " - " + diff + " - " + t.getKey().getValues().get(index));
                if (index - last - 1 > 0 || diff > 0) {
                    if (index - last - 1 > 0) {
                        diff += index - last - 1;
                    }
                    if (tm.getMappedInstances().get(index) == null || tm.getMappedInstances().get(index).getFirst() == null) {
                        //        System.out.println("continue");
                        last = index;
                        continue;
                    }
                    newInstanceMappings.put(index + diff, tm.getMappedInstances().get(index));
                    //     System.out.println("new: " + tm.getMappedInstances().get(index).getFirst());
                } else {
                    if (tm.getMappedInstances().get(index) == null || tm.getMappedInstances().get(index).getFirst() == null) {
                        //          System.out.println("continue");
                        last = index;
                        continue;
                    }
                    newInstanceMappings.put(index, tm.getMappedInstances().get(index));
                    //      System.out.println("old");
                }
                last = index;
            }

            tm.setMappedInstances(newInstanceMappings);
            t.setMapping(tm);
            
            if(f.getName().equals("1350433107026_1350497272807_318.arc4727466956293543108#6899771_4_6843721927200132921.csv")) {
                System.out.println("table put in map");
            }
            
            mapped = new SparseSimilarityMatrix<>(0,0);
            matrices.put(t, mapped);
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

}
