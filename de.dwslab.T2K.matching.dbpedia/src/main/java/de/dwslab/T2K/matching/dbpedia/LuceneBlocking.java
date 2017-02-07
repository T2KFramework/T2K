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
package de.dwslab.T2K.matching.dbpedia;

import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.dbpedia.DBPediaInstanceIndex;
import de.dwslab.T2K.index.dbpedia.DBpediaIndexEntry;
import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.blocking.Blocking;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/***
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
            
            if(getLabelAdapter().isMultiValued(instance)) {
                matches = idx.searchMany(getLabelAdapter().getLabels(instance));
            } else {
                matches = idx.search(getLabelAdapter().getLabel(instance).toString());
            }

            // get all candidates by looking up the search results in the
            // hashmap
            for (DBpediaIndexEntry e : matches) {
                List<T> obj = candidateMap.get(e.getUri());
                if (e.getUri() != null && obj!=null && !obj.isEmpty()) {
                    //result.add(candidateMap.get(e.getUri()));
                    result.addAll(obj);
                }
            }
        }

        return result;
    }

}
