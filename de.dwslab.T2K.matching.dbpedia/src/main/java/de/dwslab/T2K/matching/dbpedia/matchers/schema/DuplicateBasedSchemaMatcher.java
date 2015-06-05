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
package de.dwslab.T2K.matching.dbpedia.matchers.schema;

import java.util.Collection;
import java.util.Map;

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnTableLabelAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnToCellHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowToCellHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.settings.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.Similarities;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.query.Func;
import de.dwslab.T2K.utils.query.Q;
import de.dwslab.T2K.utils.timer.Timer;

import java.util.List;

/**
 * Schema Matcher that matches properties based on existing similarity matrices
 * uses a duplicate-based method with voting
 * @author Oliver
 *
 */
public class DuplicateBasedSchemaMatcher extends PartialMatcher<TableColumn> {

    public DuplicateBasedSchemaMatcher(Similarities similarities,
            MatchingParameters matchingParameters, Timer rootTimer,
            GoldStandard goldStandard, MatchingLogger logger) {
        super(similarities, matchingParameters, rootTimer, goldStandard, logger);
    }
    
    private SimilarityMatrix<TableRow> candidateSimilarity;
    public SimilarityMatrix<TableRow> getCandidateSimilarity() {
        return candidateSimilarity;
    }
    public void setCandidateSimilarity(
            SimilarityMatrix<TableRow> candidateSimilarity) {
        this.candidateSimilarity = candidateSimilarity;
    }
    
    private SimilarityMatrix<TableCell> valueSimilarity;
    public SimilarityMatrix<TableCell> getValueSimilarity() {
        return valueSimilarity;
    }
    public void setValueSimilarity(SimilarityMatrix<TableCell> valueSimilarity) {
        this.valueSimilarity = valueSimilarity;
    }
    
    private SimilarityMatrix<TableColumn> labelSimilarity;
    public SimilarityMatrix<TableColumn> getLabelSimilarity() {
        return labelSimilarity;
    }
    public void setLabelSimilarity(SimilarityMatrix<TableColumn> labelSimilarity) {
        this.labelSimilarity = labelSimilarity;
    }
    
    private Table finalClass;
    public Table getFinalClass() {
        return finalClass;
    }
    public void setFinalClass(Table finalClass) {
        this.finalClass = finalClass;
    }
    
    private SimilarityMatrix<Table> classSimilarity;
    public SimilarityMatrix<Table> getClassSimilarity() {
        return classSimilarity;
    }
    public void setClassSimilarity(SimilarityMatrix<Table> classSimilarity) {
        this.classSimilarity = classSimilarity;
    }
    
    private int numCandidatesPerInstance;
    public int getNumCandidatesPerInstance() {
        return numCandidatesPerInstance;
    }
    public void setNumCandidatesPerInstance(int numCandidatesPerInstance) {
        this.numCandidatesPerInstance = numCandidatesPerInstance;
    }
    
    private double candidateThreshold;
    public double getCandidateThreshold() {
        return candidateThreshold;
    }
    public void setCandidateThreshold(double candidateThreshold) {
        this.candidateThreshold = candidateThreshold;
    }
    
    private double valueThreshold;
    public double getValueThreshold() {
        return valueThreshold;
    }
    public void setValueThreshold(double valueThreshold) {
        this.valueThreshold = valueThreshold;
    }
    
    private int numVotesPerInstance;
    public int getNumVotesPerInstance() {
        return numVotesPerInstance;
    }
    public void setNumVotesPerInstance(int numVotesPerInstance) {
        this.numVotesPerInstance = numVotesPerInstance;
    }
    
    private int numResults;
    public int getNumResults() {
        return numResults;
    }
    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }
    
    private double finalThreshold;
    public double getFinalThreshold() {
        return finalThreshold;
    }
    public void setFinalThreshold(double finalThreshold) {
        this.finalThreshold = finalThreshold;
    }
    private int keyWeight;
    public int getKeyWeight() {
        return keyWeight;
    }
    public void setKeyWeight(int keyWeight) {
        this.keyWeight = keyWeight;
    }

    
    
    @Override
    public SimilarityMatrix<TableColumn> match(MatchingData data) {
        Timer t = Timer.getNamed("Property matching", getRootTimer());

        /*
         * for each instance, we have several candidates (and we don't know which one is correct)
         * so for the property matching, we use the best or the top K candidates
         */
        //TODO try best candidate per class
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Property matching 1/4 ...");
        }
        Timer tm = Timer.getNamed("Choose best candidates for property voting", t);
        SimilarityMatrix<TableRow> cand = null;
        
        cand = Matcher.selectedTopKCandidatesForEachInstance(getNumCandidatesPerInstance(), getCandidateSimilarity());
//        if((getMatchingParameters().getPropertyMatchingFlags() & MatchingParameters.PROPERTY_TOP_K_CANDIDATES) != 0) {
//            cand = Matcher.selectedTopKCandidatesForEachInstance(2, getCandidateSimilarity());
//        } else {
//            cand = Matcher.selectBestCandidateForEachInstance(ConflictResolution.Maximum, getCandidateSimilarity());
//        }
        tm.stop();
        
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Property matching 1/4 ... done.");
        }
        
        /*
         * apply a threshold to the selected candidates (best or top K)
         */
//        if((getMatchingParameters().getPropertyMatchingFlags() & MatchingParameters.PROPERTY_CANDIDATE_PRUNING) != 0) {
//            cand.prune(getMatchingParameters().getPropertyCandidateThreshold());
//        }
        cand.prune(getCandidateThreshold());
        
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Candidates for duplicate-based schema matching (properties):");
            
            // group rows by table
            Map<Table, Collection<TableRow>> grouped = Q.group(cand.getSecondDimension(), new Func<Table, TableRow>() {

                public Table invoke(TableRow in) {
                    return in.getTable();
                }
                
            });
            
            for(Table tab : grouped.keySet()) {
                
                System.out.println(tab.getHeader());
                System.out.print(tab.printHeader());
                System.out.print(tab.printTypes());
                
                for(TableRow r : grouped.get(tab)) {
                    System.out.print(tab.printRow(r.getRowIndex()));
                }
                
            }
        }
        
        /*
         * combine candidate scores with instance similarities
         * this removes all similarities of candidates that were excluded in the previous step
         */
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Property matching 2/4 ...");
        }
        tm = Timer.getNamed("CombineHierarchy: weight value-based similarities with candidate similarities", t);
        SimilarityMatrix<TableCell> weightedInst = Matcher.multiplyParentSimilarity(cand, getValueSimilarity(), new TableRowToCellHierarchyAdapter());
        tm.stop();
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Property matching 2/4 ... done.");
        }
        
        
        /*
         * apply a threshold on the combined value scores
         * alternative: could apply threshold on value scores before combining
         */
//        if((getMatchingParameters().getPropertyMatchingFlags() & MatchingParameters.PROPERTY_VALUE_PRUNING)!=0) {
//            weightedInst.prune(getMatchingParameters().getPropertyValueThreshold());
//        }
        weightedInst.prune(getValueThreshold());
        
        /*
         * voting: every value votes for a corresponding candidate value => 1:1 mapping on cell level
         * each attribute has been compared with multiple other attributes. Now, each instance votes for a certain mapping, 
         * i.e. only the best or the top K similarities per attribute value are kept 
         */
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Property matching 3/4 ...");
        }
        tm = Timer.getNamed("Vote for value mappings", t);
        SimilarityMatrix<TableCell> voted = null;
//        if((getMatchingParameters().getPropertyMatchingFlags() & MatchingParameters.PROPERTY_TOP_K_VOTE)!=0) {
//            voted = Matcher.selectedTopKCandidatesForEachInstance(2, weightedInst);
//        } else {
//            voted = Matcher.selectBestCandidateForEachInstance(ConflictResolution.Maximum, weightedInst);
//        }
        voted = Matcher.selectedTopKCandidatesForEachInstance(getNumVotesPerInstance(), weightedInst);
        tm.stop();
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Property matching 3/4 ... done.");
        }
        
        /*
         * aggregate votes by summing up the score of each vote
         */
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Property matching 4/4 ...");
        }
        tm = Timer.getNamed("Aggregate property votes", t);
        SimilarityMatrix<TableColumn> aggregatedVotes = Matcher.sumChildrenSimilarity(voted, getLabelSimilarity(), new TableColumnToCellHierarchyAdapter());
        
        // remove all similarities with the key columns on the RHS, as they will be set later on (an we don't want any additional mappings to the keys)
        for(Table tab : data.getDbpediaTables()) {
            
            for(TableColumn c : data.getWebtable().getColumns()) {
                
                if(tab.getKey()!=null) {
                    aggregatedVotes.set(c, tab.getKey(), 0.0);
                }
                
            }
            
        }
        
        if(getMatchingParameters().isCollectMatchingInfo())
        {
            System.out.println("votes for property mappings");
            //System.out.println(instSchemaSim.getOutput());
            System.out.println(aggregatedVotes.getOutput2(null, null, new TableColumnMatchingAdapter(), new TableColumnTableLabelAdapter()));
        }
        
        // normalise by diving the sum by the number of summed values (in case there are only few values in dbpedia)
//        SimilarityMatrix<TableColumn> countedVotes = Matcher.countChildrenSimilarity(voted, getSimilarities().getPropertySimilarity(), new TableColumnToCellHierarchyAdapter());
//        countedVotes.invert();
//        SimilarityMatrix<TableColumn> averageSimilarity = Matcher.multiplySimilarity(aggregatedVotes, countedVotes);
        
        tm.stop();
        
        if(getMatchingParameters().isCollectMatchingInfo())
        {
            System.out.println("Property matching 4/4 ... done.");
            System.out.println("votes (normalised) for property mappings");
            //System.out.println(instSchemaSim.getOutput());
            System.out.println(aggregatedVotes.getOutput2(null, null, new TableColumnMatchingAdapter(), new TableColumnTableLabelAdapter()));
        }
        getLogger().logData("votes for property mappings:\n" + aggregatedVotes.getOutput2(null, null, new TableColumnMatchingAdapter(), new TableColumnTableLabelAdapter()));
        
        /*
         * normalise the summed scores with the number of instances
         */
        aggregatedVotes.normalize(getCandidateSimilarity().getFirstDimension().size());
        
        // and multiply with average similarity: columns with few very good matches are boosted, columns with many bad matches are lowered
//        aggregatedVotes = Matcher.multiplySimilarity(aggregatedVotes, averageSimilarity);
        
        /*
         * Finally, select the properties that received the most votes
         */
        SimilarityMatrix<TableColumn> properties = null;
        
//        if((getMatchingParameters().getPropertyMatchingFlags() & MatchingParameters.PROPERTY_SELECT_TOP_K)!=0) {
//            properties = Matcher.selectedTopKCandidatesForEachInstance(2, aggregatedVotes);
//        } else {
//            //properties = Matcher.selectBestCandidateForEachInstance(ConflictResolution.Maximum, properties);
//            BestChoiceMatching b = new BestChoiceMatching();
//            b.setForceOneToOneMapping(true);
//            properties = b.match(aggregatedVotes);
//        }
        properties = Matcher.selectedTopKCandidatesForEachInstance(getNumResults(), aggregatedVotes);
        

        
        /*
         * apply a threshold to the selected properties
         */
//        if((getMatchingParameters().getPropertyMatchingFlags() & MatchingParameters.PROPERTY_FINAL_PRUNING)!=0) {
//            //final threshold: at least x% of all candidates must have voted for a property, otherwise it's excluded
//            properties.prune(getMatchingParameters().getPropertyFinalThreshold());
//        }
        properties.prune(getFinalThreshold());
        
        /*
         * make sure all keys on the right-hand side are mapped to the table key (until a final class decision is made)
         * also, we boost the score (=importance) of the key property
         */
        makeKeysMatch(properties, data.getWebtable());
        
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("column scores with key boosting");
            //System.out.println(properties.getOutput());
            System.out.println(properties.getOutput2(null, null, new TableColumnMatchingAdapter(), new TableColumnTableLabelAdapter()));
        }
        
        /*
         * we adjust all scores so that the matrix sums to 1. 
         * With this we can directly multiply it in the next step and need no further normalisation
         */
        tm = Timer.getNamed("Make property similarity matrix stochastic", t);
        properties.makeStochastic();
        tm.stop();
        
        /*
         * add values for the key properties of the super classes
         */
        addSuperClassKeyMappings(properties, data.getWebtable());
        
        //getSimilarities().setPropertySimilarity(properties);
        
        getLogger().logData("property matching:\n" + properties.getOutput2(null, null, new TableColumnMatchingAdapter(), new TableColumnTableLabelAdapter()));
        
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Property matching");
            System.out.println(properties.getOutput2(null, null, new TableColumnMatchingAdapter(), new TableColumnTableLabelAdapter()));
        }
        t.stop();
        
        return properties;
    }
    

    
    protected void addSuperClassKeyMappings(SimilarityMatrix<TableColumn> sim, Table webtable) {
        // once the final class decision has been made, we must make sure that the keys of the super classes are not ignored
        TableColumn wtKey = webtable.getKey();
        
        if(getFinalClass()!=null) {
            double keySim = sim.get(wtKey, getFinalClass().getKey());
            
            // set the super class keys to the same value as the chosen class key, as the cannot both occur, the values still add up to 1
            for(Table matchingTable : getClassSimilarity().getMatchesAboveThreshold(webtable, 0.0)) {
                sim.set(wtKey, matchingTable.getKey(), keySim);
            }
        }
    }    
    //private double keyWeight = 2.0;
    
    protected void makeKeysMatch(SimilarityMatrix<TableColumn> sim, Table webtable) {
        // as long as we did not make a final class decision, we make sure that the key of each class is mapped to the table key
        
        TableColumn wtKey = webtable.getKey();
        
        if(getFinalClass()==null) {
            // set the matching score of all key columns to 5
            for(Table matchingTable : getClassSimilarity().getMatchesAboveThreshold(webtable, 0.0)) {
                sim.set(wtKey, matchingTable.getKey(),(double)keyWeight);
            }
        } else {
            sim.set(wtKey, getFinalClass().getKey(),(double)keyWeight);
        }
    }
}
