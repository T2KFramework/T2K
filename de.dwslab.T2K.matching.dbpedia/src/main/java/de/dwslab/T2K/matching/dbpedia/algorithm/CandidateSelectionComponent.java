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
package de.dwslab.T2K.matching.dbpedia.algorithm;

import au.com.bytecode.opencsv.CSVWriter;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.WebJaccardSimilarity;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateSelectionMatcher;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.similarity.signatures.JaccardPrefixFiltering;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;

public class CandidateSelectionComponent extends WebtableToDBpediaMatchingComponent {

    private KeyIndex keyIndex;
    public KeyIndex getKeyIndex() {
        return keyIndex;
    }
    public void setKeyIndex(KeyIndex keyIndex) {
        this.keyIndex = keyIndex;
    }
    
    private EvaluationResult initialInstanceResult;
    public EvaluationResult getInitialInstanceResult() {
        return initialInstanceResult;
    }
    
    private EvaluationResult prunedInstanceResult;
    public EvaluationResult getPrunedInstanceResult() {
        return prunedInstanceResult;
    }   
    
    private EvaluationResult initialClassResult;
    public EvaluationResult getInitialClassResult() {
        return initialClassResult;
    }
    
    Collection<Correspondence<TableRow>> initialInstanceMappings;
    
    public int getMaxCorrectCanddiates() {
        int numCorrect = 0;
        for(Correspondence<TableRow> cor : initialInstanceMappings) {
            if(isInCandidateList(cor.getCorrectValue())) {
                numCorrect++;
            }
        }
        return numCorrect;
    }
    
    @Override
    public double evaluate(Configuration config) {
        super.evaluate(config);
        // evaluation based on candidate list
        // good if all correct candidates are in the list (position doesn't matter)
        // good if the candidate lists are as small as possible
        
        // i.e. number of correct candidates / number of total candidates
        int numCorrect = 0;
        for(Correspondence<TableRow> cor : initialInstanceMappings) {
            if(isInCandidateList(cor.getCorrectValue())) {
                numCorrect++;
            }
        }
        
        //double result= (double)numCorrect / (double)refinedCandidateSimilarity.getSecondDimension().size();
        //double result= (double)numCorrect - 1.0/(double)refinedCandidateSimilarity.getSecondDimension().size();
        double result = (double)numCorrect / (double)initialInstanceResult.getInputSetSize(); // = max. recall
        //double prec = refinedInstanceResult.getPrecision();
        
        //double result = 2 * maxRecall * prec / (maxRecall + prec);
        //double result = numCorrect;
        
        System.out.println(String.format("%d / %d correct candidates = %.4f", numCorrect, initialCandidateSimilarity.getSecondDimension().size(), result));
        System.out.println(config.print());
        
        try {
            File f = new File("CandidateSelectionComponent.csv");
            
            boolean writeHeaders = !f.exists();
            
            CSVWriter w = new CSVWriter(new BufferedWriter(new FileWriter(f, true)));
            
            if(writeHeaders) {
                String[] headers = new String[] {"webtable","baseline prec","baseline recall","baseline f1","pruned prec","pruned recall","pruned f1","refined prec","refined recall","refined f1","max recall"};
                headers = (String[])ArrayUtils.addAll(headers, config.getParameterNames());
                w.writeNext(headers);
            }

            String[] values = new String[] {
                    getWebTableName(),
                    Double.toString(initialInstanceResult.getPrecision()),
                    Double.toString(initialInstanceResult.getRecall()),
                    Double.toString(initialInstanceResult.getF1Score()),
                    Double.toString(prunedInstanceResult.getPrecision()),
                    Double.toString(prunedInstanceResult.getRecall()),
                    Double.toString(prunedInstanceResult.getF1Score()),
                    Double.toString(result)
            };
            
            values = (String[])ArrayUtils.addAll(values, config.getValues());
                    
            w.writeNext(values);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    protected boolean isInCandidateList(Object key) {

        for (TableRow r : initialCandidateSimilarity.getSecondDimension()) {
            if(key instanceof List) {
                for(Object s : (List)key) {
                    if(r.getURI().equals(s)) {
                        return true;
                    }
                }
            }
            if (r.getURI().equals(key)) {
                return true;
            }
        }

        return false;
    }
    
    private SimilarityMatrix<TableRow> initialCandidateSimilarity;
    private SimilarityMatrix<TableRow> prunedCandidateSimilarity;
    private SimilarityMatrix<Table> classSimilarity;
    
    public SimilarityMatrix<Table> getClassSimilarity() {
        return classSimilarity;
    }
    
    public static final Parameter PAR_INITIAL_SIMILARITY_FUNCTION = new Parameter("CandidateSelection.InitialSimilarityFunction", new WebJaccardSimilarity());
    public static final Parameter PAR_INITIAL_K = new Parameter("CandidateSelection.InitialK", 500);
    public static final Parameter PAR_INTIAL_EDIT_DIST = new Parameter("CandidateSelection.InitialEditDistance", 0);
    public static final Parameter PAR_INITIAL_THRESHOLD = new Parameter("CandidateSelection.InitialThreshold", 0.5);
    public static final Parameter PAR_INITIAL_STRING_FILTERING = new Parameter("CandidateSelection.InitialStringFiltering", new JaccardPrefixFiltering<>());
    
    protected static final List<Parameter> params;
    public static List<Parameter> getParams() {
        return params;
    }
    
    static {
        ArrayList<Parameter> l = new ArrayList<>();
        l.add(PAR_INITIAL_SIMILARITY_FUNCTION);
        l.add(PAR_INITIAL_K);
        l.add(PAR_INTIAL_EDIT_DIST);
        l.add(PAR_INITIAL_STRING_FILTERING);
        l.add(PAR_INITIAL_THRESHOLD);
        params = l;
    }
    
    public CandidateSelectionComponent() {
        setParameters(params);
    }
    
    @SuppressWarnings("unchecked")
    protected void initialiseParameters(Configuration config) {
        
        CandidateSelectionMatcher candsel = getMatchers().getCandidateSelectionMatcher();
        candsel.setLabelSimilarityFunction((SimilarityFunction<String>)config.getValue(PAR_INITIAL_SIMILARITY_FUNCTION));
        candsel.setSelectK((Integer)config.getValue(PAR_INITIAL_K));
        candsel.setMaxEditDist((Integer)config.getValue(PAR_INTIAL_EDIT_DIST));
        candsel.setSimilarityThreshold((Double)config.getValue(PAR_INITIAL_THRESHOLD));
        candsel.setStringFilter((SignatureFilter<TableRow>)config.getValue(PAR_INITIAL_STRING_FILTERING));
        
    }
    
    @Override
    public void run(Configuration config) {
        initialiseParameters(config);
        
        System.out.println("Candidate Selection Component");
        
        MatchingResult logResult = new MatchingResult();
        
        // Candidate Selection
        setInitialCandidateSimilarity(getMatchers().getCandidateSelectionMatcher().match(getData()));
        
        if(getGoldStandard()!=null) {
            // we re-use the set of candidates created during candidate selection, so we can't adjust the gold standard earlier
            getGoldStandard().adjustToSubset(getKeyIndex().getLuceneBlocking().getCandidateMap().keySet(), getData().getDbpediaPropUris());
        }
        mapInstances(getInitialCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), logResult, "baseline");
        
        initialInstanceResult = logResult.getEvaluation().getInstanceResult();
        initialInstanceMappings = logResult.getInstanceMappings();
        //addIntermediaResult("instances", "[01] baseline", instanceBaseLine);
        
        // Class Matching
        // current approach is to use class matching to reduce the number of wrong candidates, so we only consider the majority class
        getMatchers().getClassMatcher().setCandidateSimilarity(getInitialCandidateSimilarity());
        classSimilarity = getMatchers().getClassMatcher().match(getData());        
        mapClasses(classSimilarity, logResult, false);
        initialClassResult = logResult.getEvaluation().getClassResult();
        
        
        //TEST!!!!
        mapInstances(getInitialCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), logResult, "not majority");       
        //updateCandidatesBasedOnClasses();        
        //mapInstances(prunedCandidateSimilarity, getMatchingParameters().isCollectMatchingInfo(), logResult, "pruned");
        
        prunedInstanceResult = logResult.getEvaluation().getInstanceResult();
        //addIntermediaResult("instances", "[02] pruned", logResult.getEvaluation().getInstanceResult());
        
        // candidate refinement
        getMatchingParameters().setFastJoinDelta(0.5); // = edit distance threshold
        getMatchingParameters().setFastJoinTau(0.5); // = jaccard threshold
 //       getMatchers().getCandidateRefinementMatcher().setClassSimilarity(classSimilarity);
        
        
        //TEST!!!
        //getMatchers().getCandidateRefinementMatcher().setInitialCandidateSimilarity(prunedCandidateSimilarity);
   //     getMatchers().getCandidateRefinementMatcher().setInitialCandidateSimilarity(getInitialCandidateSimilarity());
        
    }

    /**
     * @return the initialCandidateSimilarity
     */
    public SimilarityMatrix<TableRow> getInitialCandidateSimilarity() {
        return initialCandidateSimilarity;
    }

    /**
     * @param initialCandidateSimilarity the initialCandidateSimilarity to set
     */
    public void setInitialCandidateSimilarity(SimilarityMatrix<TableRow> initialCandidateSimilarity) {
        this.initialCandidateSimilarity = initialCandidateSimilarity;
    }
    
    /**
     * removes candidates that do not belong to the majority class
     */
//    protected void updateCandidatesBasedOnClasses() {
//        Timer t = Timer.getNamed("Update Candidates based on Classes", getRootTimer());
//        int before = initialCandidateSimilarity.getNumberOfNonZeroElements();
//        
//        // multiply new class similarities with candidate similarities (sets all candidates with wrong class to 0)
//        CombineHierarchy<Table, TableRow> c = new CombineHierarchy<Table, TableRow>();
//        c.setAggregationType(CombinationType.Multiply);
//        Timer tm = Timer.getNamed("Multiply Class with Candidate similarities", t);
//        prunedCandidateSimilarity = c.match(classSimilarity, initialCandidateSimilarity, new TableToRowHierarchyAdapter());
//        tm.stop();
//        
//        prunedCandidateSimilarity.normalize();
//                
//        if(getMatchingParameters().isCollectMatchingInfo()) {
//            System.out.println("Majority Class decision:");
//            System.out.println(prunedCandidateSimilarity.getOutput());
//            System.out.println(String.format("Remove candiates with wrong class: removed %d/%d", before - prunedCandidateSimilarity.getNumberOfNonZeroElements(), before));
//        }
//        t.stop();
//    }
}
