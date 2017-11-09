package de.dwslab.T2K.matching.dbpedia;

import de.dwslab.T2K.matching.blocking.Blocking;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author domi
 */
public class DocumentOverlapBlocking<T> extends Blocking<T> {

    private Map<String, List<String>> overallTermMap = new HashMap<>();

    private List<String> allowedClasses = new ArrayList<>();

    private List<TableRow> allowedRows = new ArrayList<>();

    private Map<String, List<T>> candidateMap;

    public Map<String, List<T>> getCandidateMap() {
        return candidateMap;
    }

    public void setCandidateMap(Map<String, List<T>> candidateMap) {
        this.candidateMap = candidateMap;
    }

    @Override
    public Collection<T> getCandidates(T instance, Collection<T> candidates) {
        TableRow tr = (TableRow) instance;
        Collection<T> result = new HashSet<T>();
        if (!allowedRows.contains(tr)) {
            return result;
        }

        List<String> tokens = new ArrayList<>();

        for (TableCell tc : tr.getCells()) {
            String[] tokenStrings = tc.getValue().toString().split("\\s");
            tokens.addAll(Arrays.asList(tokenStrings));
            //tokens.add(tc.getValue().toString());
        }
                
        for (String s : tokens) {
            if (overallTermMap.containsKey(s)) {
                List<String> identies = overallTermMap.get(s);
                for (String singleURI : identies) {

                    List<T> obj = candidateMap.get(singleURI);
                    if (singleURI != null && obj != null && !obj.isEmpty()) {
                        for (T singleObject : obj) {
                            TableRow candRow = (TableRow) singleObject;
                            if (allowedClasses.isEmpty() || allowedClasses.contains(candRow.getTable().getHeader().replace(".csv", "").replace(".gz", ""))) {
                                result.add(singleObject);
                            }
                        }
                    }
                }
            }
        }
        //System.out.println("res: " + result.size());
        return result;
    }

    /**
     * @return the overallTermMap
     */
    public Map<String, List<String>> getOverallTermMap() {
        return overallTermMap;
    }

    /**
     * @param overallTermMap the overallTermMap to set
     */
    public void setOverallTermMap(Map<String, List<String>> overallTermMap) {
        this.overallTermMap = overallTermMap;
    }

    /**
     * @return the allowedClasses
     */
    public List<String> getAllowedClasses() {
        return allowedClasses;
    }

    /**
     * @param allowedClasses the allowedClasses to set
     */
    public void setAllowedClasses(List<String> allowedClasses) {
        this.allowedClasses = allowedClasses;
    }

    /**
     * @return the allowedRows
     */
    public List<TableRow> getAllowedRows() {
        return allowedRows;
    }

    /**
     * @param allowedRows the allowedRows to set
     */
    public void setAllowedRows(List<TableRow> allowedRows) {
        this.allowedRows = allowedRows;
    }

}
