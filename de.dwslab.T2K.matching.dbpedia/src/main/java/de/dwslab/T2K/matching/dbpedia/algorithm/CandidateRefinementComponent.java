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
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateRefinementMatcher;
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

/**
 *
 * @author domi
 */
public class CandidateRefinementComponent extends WebtableToDBpediaMatchingComponent {
    
     private KeyIndex keyIndex;
    public KeyIndex getKeyIndex() {
        return keyIndex;
    }
    public void setKeyIndex(KeyIndex keyIndex) {
        this.keyIndex = keyIndex;
    }
    private EvaluationResult refinedInstanceResult;
    public EvaluationResult getRefinedInstanceResult() {
        return refinedInstanceResult;
    }
    
    private Collection<Correspondence<TableRow>> refinedInstanceMappings;
    
    public int getMaxCorrectCanddiates() {
        int numCorrect = 0;
        for(Correspondence<TableRow> cor : refinedInstanceMappings) {
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
        for(Correspondence<TableRow> cor : refinedInstanceMappings) {
            if(isInCandidateList(cor.getCorrectValue())) {
                numCorrect++;
            }
        }
        
        //double result= (double)numCorrect / (double)refinedCandidateSimilarity.getSecondDimension().size();
        //double result= (double)numCorrect - 1.0/(double)refinedCandidateSimilarity.getSecondDimension().size();
        double result = (double)numCorrect / (double)refinedInstanceResult.getInputSetSize(); // = max. recall
        //double prec = refinedInstanceResult.getPrecision();
        
        //double result = 2 * maxRecall * prec / (maxRecall + prec);
        //double result = numCorrect;
        
        System.out.println(String.format("%d / %d correct candidates = %.4f", numCorrect, refinedCandidateSimilarity.getSecondDimension().size(), result));
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
                    Double.toString(refinedInstanceResult.getPrecision()),
                    Double.toString(refinedInstanceResult.getRecall()),
                    Double.toString(refinedInstanceResult.getF1Score()),
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

        for (TableRow r : refinedCandidateSimilarity.getSecondDimension()) {
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
    
    private SimilarityMatrix<TableRow> refinedCandidateSimilarity;
    
    public SimilarityMatrix<TableRow> getCandidateSimilarity() {
        return refinedCandidateSimilarity;
    }
    
    private SimilarityMatrix<Table> classSimilarity;
    
    private SimilarityMatrix<TableRow> initialCandidateSimilarity;
    
    public static final Parameter PAR_REFINEMENT_SIMILARITY_FUNCTION = new Parameter("CandidateSelection.RefinementSimilarityFunction", new WebJaccardSimilarity());
    public static final Parameter PAR_REFINEMENT_K = new Parameter("CandidateSelection.RefinementK", 20);
    public static final Parameter PAR_REFINEMENT_EDIT_DIST = new Parameter("CandidateSelection.RefinementEditDistance", 2);
    public static final Parameter PAR_REFINEMENT_THRESHOLD = new Parameter("CandidateSelection.RefinementThreshold", 0.2);
    public static final Parameter PAR_REFINEMENT_STRING_FILTERING = new Parameter("CandidateSelection.RefinementStringFiltering", new JaccardPrefixFiltering<>());
    
    public static final Parameter PAR_LOG_CANDIDATE_SELECTION = new Parameter("CandidateSelection.VerboseLogging", false);
    
    protected static final List<Parameter> params;
    public static List<Parameter> getParams() {
        return params;
    }
    
    static {
        ArrayList<Parameter> l = new ArrayList<>();
        l.add(PAR_REFINEMENT_SIMILARITY_FUNCTION);
        l.add(PAR_REFINEMENT_K);
        l.add(PAR_REFINEMENT_EDIT_DIST);
        l.add(PAR_REFINEMENT_THRESHOLD);
        l.add(PAR_REFINEMENT_STRING_FILTERING);
        l.add(PAR_LOG_CANDIDATE_SELECTION);
        params = l;
    }
    
    public CandidateRefinementComponent() {
        setParameters(params);
    }
    
    @SuppressWarnings("unchecked")
    protected void initialiseParameters(Configuration config) {
                
        CandidateRefinementMatcher candref = getMatchers().getCandidateRefinementMatcher();
        candref.setLabelSimilarityFunction((SimilarityFunction<String>)config.getValue(PAR_REFINEMENT_SIMILARITY_FUNCTION));
        candref.setSelectK((Integer)config.getValue(PAR_REFINEMENT_K));
        candref.setMaxEditDist((Integer)config.getValue(PAR_REFINEMENT_EDIT_DIST));
        candref.setSimilarityThreshold((Double)config.getValue(PAR_REFINEMENT_THRESHOLD));
        candref.setStringFilter((SignatureFilter<TableRow>)config.getValue(PAR_REFINEMENT_STRING_FILTERING));
    }
    
    @Override
    public void run(Configuration config) {
        initialiseParameters(config);
        
        System.out.println("Candidate Refinement Component");
        
        MatchingResult logResult = new MatchingResult();
                
        getMatchers().getCandidateRefinementMatcher().setInitialCandidateSimilarity(getInitialCandidateSimilarity());
        getMatchers().getCandidateRefinementMatcher().setClassSimilarity(getClassSimilarity());
        
        refinedCandidateSimilarity = getMatchers().getCandidateRefinementMatcher().match(getData());
        
        mapInstances(refinedCandidateSimilarity, getMatchingParameters().isCollectMatchingInfo(), logResult, "refined");
        //addIntermediaResult("instances", "[03] refined", logResult.getEvaluation().getInstanceResult());
        refinedInstanceResult = logResult.getEvaluation().getInstanceResult();
        refinedInstanceMappings = logResult.getInstanceMappings();
        
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Final instance correspondences:");
            for(Correspondence<TableRow> c : refinedInstanceMappings) {
                
                System.out.println(String.format("%s -> %s", c.getFirst().getKey(), c.getSecond().getKey()));
                
            }
        }
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
     * @return the classSimilarity
     */
    public SimilarityMatrix<Table> getClassSimilarity() {
        return classSimilarity;
    }

    /**
     * @param classSimilarity the classSimilarity to set
     */
    public void setClassSimilarity(SimilarityMatrix<Table> classSimilarity) {
        this.classSimilarity = classSimilarity;
    }
    
}
