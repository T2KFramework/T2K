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
package de.dwslab.T2K.matching.dbpedia.components;

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.PruningInstanceMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.DuplicateBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnTableLabelAdapter;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.TableColumn;

import java.util.ArrayList;
import java.util.List;

public class IterativeComponent extends WebtableToDBpediaMatchingComponent {

    private EvaluationResult propertyResult;
    public EvaluationResult getPropertyResult() {
        return propertyResult;
    }
    
    private EvaluationResult instanceResult;
    public EvaluationResult getInstanceResult() {
        return instanceResult;
    }
    
    private MatchingResult result;

    public MatchingResult getResult() {
        return result;
    }

    private void setResult(MatchingResult result) {
        this.result = result;
    }
    
    
    public double evaluate(Configuration config) {
        super.evaluate(config);
        return (getPropertyResult().getF1Score() + getInstanceResult().getF1Score()) / 2.0;
    }
    
    private SimilarityMatrix<TableColumn> propertySimilarity;
    public SimilarityMatrix<TableColumn> getPropertySimilarity() {
        return propertySimilarity;
    }
    
    private SimilarityMatrix<TableRow> instanceSimilarity;
    public SimilarityMatrix<TableRow> getInstanceSimilarity() {
        return instanceSimilarity;
    }
    
    public static final Parameter PAR_PROP_NUM_CANDIDATES = PropertyBasedClassRefinementComponent.PAR_PROP_NUM_CANDIDATES;
    public static final Parameter PAR_PROP_CANDIDATE_THRESHOLD =  PropertyBasedClassRefinementComponent.PAR_PROP_CANDIDATE_THRESHOLD;
    public static final Parameter PAR_PROP_VALUE_THRESHOLD = PropertyBasedClassRefinementComponent.PAR_PROP_VALUE_THRESHOLD;
    public static final Parameter PAR_PROP_NUM_VOTES = PropertyBasedClassRefinementComponent.PAR_PROP_NUM_VOTES;
    public static final Parameter PAR_PROP_NUM_RESULTS = PropertyBasedClassRefinementComponent.PAR_PROP_NUM_RESULTS;
    public static final Parameter PAR_PROP_FINAL_THRESHOLD = PropertyBasedClassRefinementComponent.PAR_PROP_FINAL_THRESHOLD;
    public static final Parameter PAR_PROP_KEY_WEIGHT = PropertyBasedClassRefinementComponent.PAR_PROP_KEY_WEIGHT;
    public static final Parameter PAR_INST_FINAL_THRESHOLD = new Parameter("Iterative.Instance.Final_threshold", 0.8); 
    
    
    private static List<Parameter> params;
    public static List<Parameter> getParams() {
        return params;
    }
    
    static {
        params = new ArrayList<>();
        params.add(PAR_PROP_NUM_CANDIDATES);
        params.add(PAR_PROP_CANDIDATE_THRESHOLD);
        params.add(PAR_PROP_VALUE_THRESHOLD);
        params.add(PAR_PROP_NUM_VOTES);
        params.add(PAR_PROP_NUM_RESULTS);
        params.add(PAR_PROP_FINAL_THRESHOLD);
        params.add(PAR_INST_FINAL_THRESHOLD);
    }
    
    public IterativeComponent() {
        setParameters(params);
    }
    
    protected void initialiseParameters(Configuration config) {
        DuplicateBasedSchemaMatcher prop = getMatchers().getPropertyMatcher();
        
        prop.setNumCandidatesPerInstance((Integer) config.getValue(PAR_PROP_NUM_CANDIDATES));
        prop.setCandidateThreshold((Double)config.getValue(PAR_PROP_CANDIDATE_THRESHOLD));
        prop.setValueThreshold((Double)config.getValue(PAR_PROP_VALUE_THRESHOLD));
        prop.setNumVotesPerInstance((Integer)config.getValue(PAR_PROP_NUM_VOTES));
        prop.setNumResults((Integer)config.getValue(PAR_PROP_NUM_RESULTS));
        prop.setFinalThreshold((Double)config.getValue(PAR_PROP_FINAL_THRESHOLD));
        prop.setKeyWeight((Integer)config.getValue(PAR_PROP_KEY_WEIGHT));
        
        PruningInstanceMatcher inst = getMatchers().getInstanceMatcher();
        inst.setSimilarityThreshold((Double)config.getValue(PAR_INST_FINAL_THRESHOLD));
    }
    
    @Override
    public void run(Configuration config) {
        initialiseParameters(config);
        
        System.out.println("Iterative Component");
        
        MatchingResult logResult = new MatchingResult();

        propertySimilarity = getSimilarities().getPropertySimilarity();
        
        if(propertySimilarity==null || propertySimilarity.getMaxValue()==null || propertySimilarity.getMaxValue()==0.0) {
            
            instanceSimilarity = getSimilarities().getCandidateSimilarity();
            mapInstances(instanceSimilarity, getMatchingParameters().isCollectMatchingInfo(), logResult, "");
            
            logResult.getEvaluation().getInstanceResult();
            setResult(logResult);
            return;
        }
        
        // Candidate (Instance) Matching
        getMatchers().getInstanceMatcher().setInitialCandidateSimilarity(getSimilarities().getCandidateSimilarity());
        getMatchers().getInstanceMatcher().setPropertySimilarity(propertySimilarity);
        getMatchers().getInstanceMatcher().setValueSimilarity(getSimilarities().getValueSimilarity());
        instanceSimilarity = getMatchers().getInstanceMatcher().match(getData());
        mapInstances(instanceSimilarity, getMatchingParameters().isCollectMatchingInfo(), logResult, "");
        //addIntermediaResult("instances", String.format("[0%d] iteration %d", iterationNum+4,iterationNum), logResult.getEvaluation().getInstanceResult());
        instanceResult = logResult.getEvaluation().getInstanceResult();
        setResult(logResult);

        // Property Matching
        getMatchers().getPropertyMatcher().setCandidateSimilarity(instanceSimilarity);
        getMatchers().getPropertyMatcher().setClassSimilarity(getSimilarities().getClassSimilarity());
        getMatchers().getPropertyMatcher().setFinalClass(getSimilarities().getFinalClass());
        getMatchers().getPropertyMatcher().setLabelSimilarity(getSimilarities().getLabelSimilarity());
        getMatchers().getPropertyMatcher().setValueSimilarity(getSimilarities().getValueSimilarity());
        SimilarityMatrix<TableColumn> propertySimilarityNew = getMatchers().getPropertyMatcher().match(getData());
        propertySimilarityNew.multiplyScalar(0.5);
        propertySimilarity.multiplyScalar(0.5);
        propertySimilarity = Matcher.addSimilarity(propertySimilarity, propertySimilarityNew);
        propertySimilarity.makeStochastic();
        System.out.println("final: " +propertySimilarity.getOutput2(null, null, new TableColumnMatchingAdapter(), new TableColumnTableLabelAdapter()));
        mapProperties(propertySimilarity, getWebTableName(), getMatchingParameters().isCollectMatchingInfo(), logResult);
        //addIntermediaResult("properties", String.format("[0%d] iteration %d", iterationNum+4,iterationNum), logResult.getEvaluation().getPropertyResult());
        propertyResult = logResult.getEvaluation().getPropertyResult();
        
    }
}
