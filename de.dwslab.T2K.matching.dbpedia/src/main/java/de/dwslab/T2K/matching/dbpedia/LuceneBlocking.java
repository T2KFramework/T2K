package de.dwslab.T2K.matching.dbpedia;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.dbpedia.DBPediaInstanceIndex;
import de.dwslab.T2K.index.dbpedia.DBpediaIndexEntry;
import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.blocking.Blocking;

/**
 * *
 * Searches for matching candidates in a lucene index
 *
 * @author Oliver
 *
 */
public class LuceneBlocking<T> extends Blocking<T> {

    private IIndex index;

    protected void setIndex(IIndex index) {
        this.index = index;
        initialiseIndex();
    }

    protected IIndex getIndex() {
        return index;
    }
    private MatchingAdapter<T> labelAdapter;

    protected MatchingAdapter<T> getLabelAdapter() {
        return labelAdapter;
    }

    protected void setAdapter(MatchingAdapter<T> adapter) {
        this.labelAdapter = adapter;
    }
    private MatchingAdapter<T> keyAdapter;

    protected MatchingAdapter<T> getKeyAdapter() {
        return keyAdapter;
    }

    protected void setKeyAdapter(MatchingAdapter<T> keyAdapter) {
        this.keyAdapter = keyAdapter;
    }
    private DBPediaInstanceIndex idx;

    public LuceneBlocking(IIndex index, MatchingAdapter<T> labelAdapter, MatchingAdapter<T> keyAdapter) {
        setIndex(index);
        setAdapter(labelAdapter);
        setKeyAdapter(keyAdapter);
        initialiseIndex();
    }

    private void initialiseIndex() {
        idx = new DBPediaInstanceIndex(getIndex(), DBpediaIndexEntry.LABEL_FIELD);
        idx.setNumRetrievedDocsFromIndex(100);
        idx.setRemoveBrackets(true);
        //idx.setMaxEditDistance(0);
        idx.setMaxEditDistance(2);
    }

    public void setVerbose(boolean verbose) {
        idx.setVerbose(verbose);
    }

    public void setNumDocuments(int numDoc) {
        idx.setNumRetrievedDocsFromIndex(numDoc);
    }

    public void setMaxEditDistance(int dist) {
        idx.setMaxEditDistance(dist);
    }
    private Map<String, List<T>> candidateMap;

    public Map<String, List<T>> getCandidateMap() {
        return candidateMap;
    }

    public void setCandidateMap(Map<String, List<T>> candidateMap) {
        this.candidateMap = candidateMap;
    }

//    public void initialiseLookup(Collection<T> candidates) {
//        if(candidateMap==null) {
//            candidateMap = new HashMap<String, T>();
//    
//            // add all candidates to a hashmap indexed with their labels
//            for (T c : candidates) {
//                if (c != null) {
//                    Object label = keyAdapter.getLabel(c);
//                    
//                    if (label != null) {
//                        candidateMap.put(label.toString(), c);
//                    }
//                }
//            }
//        }
//    }
    public Collection<T> getCandidates(T instance, Collection<T> candidates) {
        Object lbl = getLabelAdapter().getLabel(instance);

        Collection<T> result = new LinkedList<T>();

        if (lbl != null) {
            //initialiseLookup(candidates);

            // search the lucene index for the instance label
            List<DBpediaIndexEntry> matches = null;

            if (getLabelAdapter().isMultiValued(instance)) {
                matches = idx.searchMany(getLabelAdapter().getLabels(instance));
            } else {
                String keyValue = getLabelAdapter().getLabel(instance).toString();
                keyValue = keyValue.replace("[", "");
                keyValue = keyValue.replace("]", "");                
                keyValue = keyValue.replaceAll("\\d+", "");
                keyValue = keyValue.trim();
                if (!keyValue.isEmpty()) {
                    matches = idx.search(keyValue);
                }
            }

            // get all candidates by looking up the search results in the
            // hashmap
            if (matches != null) {
                for (DBpediaIndexEntry e : matches) {
                    List<T> obj = candidateMap.get(e.getUri());                    
                    if (e.getUri() != null && obj != null && !obj.isEmpty()) {
                        //result.add(candidateMap.get(e.getUri()));
                        result.addAll(obj);
                    }
                }
            }
        }

        return result;
    }
}
