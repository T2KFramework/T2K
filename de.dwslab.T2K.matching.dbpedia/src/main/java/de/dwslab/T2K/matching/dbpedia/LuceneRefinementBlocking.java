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
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/***
 * Searches for matching candidates in a lucene index
 * 
 * @author Oliver
 * 
 */
public class LuceneRefinementBlocking<T> extends Blocking<T> {

    private IIndex index;

    protected void setIndex(IIndex index) {
        this.index = index;
        
        initialiseIndex();
    }

    protected IIndex getIndex() {
        return index;
    }

    private boolean isVerbose = false;
    public void setVerbose(boolean isVerbose) {
        this.isVerbose = isVerbose;
    }
    public boolean isVerbose() {
        return isVerbose;
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
    
    private SimilarityMatrix<T> candidateSimilarities;
    public SimilarityMatrix<T> getCandidateSimilarities() {
        return candidateSimilarities;
    }
    protected void setCandidateSimilarities(
            SimilarityMatrix<T> candidateSimilarities) {
        this.candidateSimilarities = candidateSimilarities;
    }
    
    private Collection<String> refinementClasses;
    public Collection<String> getRefinementClasses() {
        return refinementClasses;
    }
    protected void setRefinementClasses(Collection<String> refinementClasses) {
        this.refinementClasses = refinementClasses;
    }
    
    private DBPediaInstanceIndex idx;
    
    public LuceneRefinementBlocking(IIndex index, MatchingAdapter<T> labelAdapter, MatchingAdapter<T> keyAdapter, SimilarityMatrix<T> candidateSimilarities, Map<String, List<T>> candidateMap, Collection<String> refinementClasses) {
        setIndex(index);
        setAdapter(labelAdapter);
        setKeyAdapter(keyAdapter);
        setCandidateSimilarities(candidateSimilarities);
        this.candidateMap = candidateMap;
//        System.out.println("ref classes: " +refinementClasses);
        setRefinementClasses(refinementClasses);
        
        initialiseIndex();
    }

    protected void initialiseIndex() {
        idx = new DBPediaInstanceIndex(getIndex(), DBpediaIndexEntry.LABEL_FIELD);
        idx.setNumRetrievedDocsFromIndex(100);
        idx.setRemoveBrackets(false);
        idx.setMaxEditDistance(2);        
        if(getRefinementClasses() != null) {
            idx.setFilterField(DBpediaIndexEntry.CLASS_LABEL_FIELD);
            idx.setFilterValues(getRefinementClasses());
        }
        
        //idx.setVerbose(isVerbose());
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
    
    
    public Collection<T> getCandidates(T instance, Collection<T> candidates) {
        long start, init=0, search=0, lookup=0, compare=0;
        start = System.currentTimeMillis();
        Object lbl = getLabelAdapter().getLabel(instance);

        Collection<T> result = new HashSet<T>();

        // add all candidates from the similarity matrix that have a similarity > 0
        if(getCandidateSimilarities() != null) {
            for(T cand : getCandidateSimilarities().getMatchesAboveThreshold(instance, 0.0)) {
                result.add(cand);
            }
        }
        
        if (lbl != null) {
            //initialiseLookup(candidates);

            init = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();
        
            // search the lucene index for the instance label
            List<DBpediaIndexEntry> matches = null; //idx.search(lbl.toString());
            if(getLabelAdapter().isMultiValued(instance)) {
                matches = idx.searchMany(getLabelAdapter().getLabels(instance));
//                System.out.println("search many : " +getLabelAdapter().getLabels(instance));
            } else {
                matches = idx.search(getLabelAdapter().getLabel(instance).toString());
//                System.out.println("search single : " +getLabelAdapter().getLabel(instance).toString());
            }
            
            search = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();
            
            // get all candidates by looking up the search results in the
            // hashmap
            int newCandidates = 0;
            for (DBpediaIndexEntry e : matches) {
                if (e.getUri() != null  && candidateMap.containsKey(e.getUri())) {
                    List<T> cand = candidateMap.get(e.getUri());
                    
                    lookup += System.currentTimeMillis() - start;
                    start = System.currentTimeMillis();
                    
                    for(T candTest : cand) {
                    // add a candidate only if it is not already included in the similarity matrix
                    if(!getCandidateSimilarities().getSecondDimension().contains(candTest)) {
                        result.add(candTest);
                        newCandidates++;
                        }
                    }
                    
                    compare += System.currentTimeMillis() - start;
                    start = System.currentTimeMillis();
                }
            }
            
            if(isVerbose()) {
                System.out.println("Refinement: added " + newCandidates + " new candidates for " + lbl + String.format(" (init: %d\tsearch: %d\tlookup: %d\tcompare: %d", init, search, lookup, compare));
            }
        }
        return result;
    }

}
