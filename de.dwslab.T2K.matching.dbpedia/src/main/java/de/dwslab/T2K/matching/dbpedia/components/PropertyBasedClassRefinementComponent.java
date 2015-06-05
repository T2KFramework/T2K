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
import de.dwslab.T2K.matching.dbpedia.matchers.schema.DuplicateBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnTableLabelAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableToRowHierarchyAdapter;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.CombineHierarchy;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.timer.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PropertyBasedClassRefinementComponent extends WebtableToDBpediaMatchingComponent {

    /**
     * @return the PAR_PROP_LABELS_WEIGHT
     */
    public static Parameter getPAR_PROP_LABELS_WEIGHT() {
        return PAR_PROP_LABELS_WEIGHT;
    }

    /**
     * @param aPAR_PROP_LABELS_WEIGHT the PAR_PROP_LABELS_WEIGHT to set
     */
    public static void setPAR_PROP_LABELS_WEIGHT(Parameter aPAR_PROP_LABELS_WEIGHT) {
        PAR_PROP_LABELS_WEIGHT = aPAR_PROP_LABELS_WEIGHT;
    }

    /**
     * @return the PAR_PROP_EQUIVALENCES_WEIGHT
     */
    public static Parameter getPAR_PROP_EQUIVALENCES_WEIGHT() {
        return PAR_PROP_EQUIVALENCES_WEIGHT;
    }

    /**
     * @param aPAR_PROP_EQUIVALENCES_WEIGHT the PAR_PROP_EQUIVALENCES_WEIGHT to
     * set
     */
    public static void setPAR_PROP_EQUIVALENCES_WEIGHT(Parameter aPAR_PROP_EQUIVALENCES_WEIGHT) {
        PAR_PROP_EQUIVALENCES_WEIGHT = aPAR_PROP_EQUIVALENCES_WEIGHT;
    }

    private EvaluationResult propertyResult;

    public EvaluationResult getPropertyResult() {
        return propertyResult;
    }

    private EvaluationResult classResult;

    public EvaluationResult getClassResult() {
        return classResult;
    }

    private EvaluationResult instanceResult;

    public EvaluationResult getInstanceResult() {
        return instanceResult;
    }

    public double evaluate(Configuration config) {
        super.evaluate(config);
        return classResult.getF1Score();
    }

    private SimilarityMatrix<TableColumn> propertySimilarity;

    public SimilarityMatrix<TableColumn> getPropertySimilarity() {
        return propertySimilarity;
    }

    private SimilarityMatrix<Table> classSimilarity;

    public SimilarityMatrix<Table> getClassSimilarity() {
        return classSimilarity;
    }

    private Table finalClass;

    public Table getFinalClass() {
        return finalClass;
    }

    private SimilarityMatrix<TableRow> candidateSimilarity;

    public SimilarityMatrix<TableRow> getCandidateSimilarity() {
        return candidateSimilarity;
    }
      //default
//    public static final Parameter PAR_PROP_NUM_CANDIDATES = new Parameter("Iterative.Property.Num_Cand", 2);
//    public static final Parameter PAR_PROP_CANDIDATE_THRESHOLD = new Parameter("Iterative.Property.Cand_threshold", 0.0);
//    public static final Parameter PAR_PROP_VALUE_THRESHOLD = new Parameter("Iterative.Property.Value_threshold", 0.0);
//    public static final Parameter PAR_PROP_NUM_VOTES = new Parameter("Iterative.Property.Num_Votes", 1);
//    public static final Parameter PAR_PROP_NUM_RESULTS = new Parameter("Iterative.Property.Num_Results", 2);
//    public static final Parameter PAR_PROP_FINAL_THRESHOLD = new Parameter("Iterative.Property.Final_threshold", 0.05);

    public static final Parameter PAR_PROP_NUM_CANDIDATES = new Parameter("Iterative.Property.Num_Cand", 2);
    public static final Parameter PAR_PROP_CANDIDATE_THRESHOLD = new Parameter("Iterative.Property.Cand_threshold", 0.1);
    public static final Parameter PAR_PROP_VALUE_THRESHOLD = new Parameter("Iterative.Property.Value_threshold", 0.6);
    public static final Parameter PAR_PROP_NUM_VOTES = new Parameter("Iterative.Property.Num_Votes", 2);
    public static final Parameter PAR_PROP_NUM_RESULTS = new Parameter("Iterative.Property.Num_Results", 2);
    public static final Parameter PAR_PROP_FINAL_THRESHOLD = new Parameter("Iterative.Property.Final_threshold", 0.0);
    public static final Parameter PAR_PROP_KEY_WEIGHT = new Parameter("Iterative.Property.Key_weight", 5);
    private static Parameter PAR_PROP_LABELS_WEIGHT = new Parameter("Iterative.Property.Labels_weight", 0.0);
    private static Parameter PAR_PROP_EQUIVALENCES_WEIGHT = new Parameter("Iterative.Property.Equivalences_weight", 0.0);
    private static Parameter PAR_PROP_VALUES_WEIGHT = new Parameter("Iterative.Property.Values_weight",1.0);

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
        params.add(PAR_PROP_KEY_WEIGHT);
        params.add(getPAR_PROP_LABELS_WEIGHT());
        params.add(getPAR_PROP_EQUIVALENCES_WEIGHT());
        params.add(PAR_PROP_VALUES_WEIGHT);
    }

    public PropertyBasedClassRefinementComponent() {
        setParameters(params);
    }

    protected void initialiseParameters(Configuration config) {
        DuplicateBasedSchemaMatcher prop = getMatchers().getPropertyMatcher();

        prop.setNumCandidatesPerInstance((Integer) config.getValue(PAR_PROP_NUM_CANDIDATES));
        prop.setCandidateThreshold((Double) config.getValue(PAR_PROP_CANDIDATE_THRESHOLD));
        prop.setValueThreshold((Double) config.getValue(PAR_PROP_VALUE_THRESHOLD));
        prop.setNumVotesPerInstance((Integer) config.getValue(PAR_PROP_NUM_VOTES));
        prop.setNumResults((Integer) config.getValue(PAR_PROP_NUM_RESULTS));
        prop.setFinalThreshold((Double) config.getValue(PAR_PROP_FINAL_THRESHOLD));
        prop.setKeyWeight((Integer) config.getValue(PAR_PROP_KEY_WEIGHT));
    }

    @Override
    public void run(Configuration config) {
        initialiseParameters(config);

        System.out.println("Property-based Class Refinement Component");

        MatchingResult logResult = new MatchingResult();

        getMatchers().getPropertyMatcher().setCandidateSimilarity(getSimilarities().getCandidateSimilarity());
        getMatchers().getPropertyMatcher().setClassSimilarity(getSimilarities().getClassSimilarity());
        getMatchers().getPropertyMatcher().setFinalClass(getSimilarities().getFinalClass());
        getMatchers().getPropertyMatcher().setLabelSimilarity(getSimilarities().getLabelSimilarity());
        getMatchers().getPropertyMatcher().setValueSimilarity(getSimilarities().getValueSimilarity());
        propertySimilarity = getMatchers().getPropertyMatcher().match(getData());
        //mapProperties(propertySimilarity, getWebTableName(), false, logResult);
        //propertyResult = logResult.getEvaluation().getPropertyResult();

        // Property-based Class Matching
        getMatchers().getPropertyBasedClassMatcher().setClassSimilarity(getSimilarities().getClassSimilarity());
        getMatchers().getPropertyBasedClassMatcher().setPropertySimilarity(propertySimilarity);
        classSimilarity = getMatchers().getPropertyBasedClassMatcher().match(getData());
        getSimilarities().setClassSimilarity(classSimilarity);
        finalClass = getMatchers().getPropertyBasedClassMatcher().getFinalClass();
        candidateSimilarity = updateCandidatesBasedOnClasses();

        // re-compute property similarity based on filtered candidates
        logResult = new MatchingResult();

        getMatchers().getPropertyMatcher().setCandidateSimilarity(candidateSimilarity);
        getMatchers().getPropertyMatcher().setClassSimilarity(classSimilarity);
        getMatchers().getPropertyMatcher().setFinalClass(finalClass);
        getMatchers().getPropertyMatcher().setLabelSimilarity(getSimilarities().getLabelSimilarity());
        getMatchers().getPropertyMatcher().setValueSimilarity(getSimilarities().getValueSimilarity());
        propertySimilarity = getMatchers().getPropertyMatcher().match(getData());

        double wLbl = (double) config.getValue(PAR_PROP_LABELS_WEIGHT);
        double wEqiv = (double) config.getValue(PAR_PROP_EQUIVALENCES_WEIGHT);
        double wProp = (double) config.getValue(PAR_PROP_VALUES_WEIGHT);
        double sum = wLbl + wEqiv + wProp;
        wLbl /= sum;
        wEqiv /= sum;
        wProp /= sum;

        propertySimilarity.multiplyScalar(wProp);
        
        System.out.println("before: "+propertySimilarity.getOutput2(null, null, new TableColumnMatchingAdapter(), new TableColumnTableLabelAdapter()));

        if (wLbl > 0.0) {
            SimilarityMatrix<TableColumn> lbl = getSimilarities().getLabelSimilarity().copy();
            lbl.multiplyScalar(wLbl);
            propertySimilarity = Matcher.addSimilarity(propertySimilarity, lbl);
        }
        
        SimilarityMatrix<TableColumn> equiv = getEquivalentPropertiesSimilarity();        
        if (wEqiv > 0.0 && equiv != null && !equiv.getFirstDimension().isEmpty() && !equiv.getSecondDimension().isEmpty()) {
            equiv.multiplyScalar(wEqiv);
            propertySimilarity = Matcher.addSimilarity(propertySimilarity, equiv);
        }
        

        propertySimilarity.makeStochastic();

        mapClasses(classSimilarity, logResult, false);
        classResult = logResult.getEvaluation().getClassResult();

        mapInstances(candidateSimilarity, false, logResult, "");
        instanceResult = logResult.getEvaluation().getInstanceResult();
        
        mapProperties(propertySimilarity, getWebTableName(), false, logResult);
        propertyResult = logResult.getEvaluation().getPropertyResult();
    }

    public void applyLabelReward() {
        //add the similarity of the label (value similarity + value similarity * label similarity)
        SimilarityMatrix<TableColumn> labelReward = Matcher.multiplySimilarity(getPropertySimilarity(), getSimilarities().getLabelSimilarity());
        propertySimilarity = Matcher.averageSimilarity(getPropertySimilarity(), labelReward);
        
    }

    public SimilarityMatrix<TableColumn> getEquivalentPropertiesSimilarity() {
        Map<String, List<Pair<TableColumn, Double>>> equivalentProperties = getData().getEquivalentProperties();
        //key: web table column, values: DBpedia properties + confidence vlaue
        
        if (equivalentProperties != null) {
            SimilarityMatrix<TableColumn> predefinedMappings = new SparseSimilarityMatrix<>(0, 0);

            // iterate web table columns
            //for(TableColumn tc : getWebtable().getColumns()) {
            for (TableColumn tc : getPropertySimilarity().getFirstDimension()) {
                for(TableColumn tc2 : getPropertySimilarity().getSecondDimension()) {
                    predefinedMappings.set(tc, tc2, 0.0);
                }
                // iterate mapped dbpedia properties
                if (equivalentProperties.containsKey(tc.getURI())) {
                    for (Pair<TableColumn, Double> dbpediaProp : equivalentProperties.get(tc.getURI())) {
                        predefinedMappings.set(tc, dbpediaProp.getFirst(), dbpediaProp.getSecond());
                    }
                }
            }

            return predefinedMappings;
        } else {
            return null;
        }
    }

    /**
     * removes candidates that do not belong to the majority class
     */
    protected SimilarityMatrix<TableRow> updateCandidatesBasedOnClasses() {
        Timer t = Timer.getNamed("Update Candidates based on Classes", getRootTimer());
        int before = getSimilarities().getCandidateSimilarity().getNumberOfNonZeroElements();

        // multiply new class similarities with candidate similarities (sets all candidates with wrong class to 0)
        CombineHierarchy<Table, TableRow> c = new CombineHierarchy<Table, TableRow>();
        c.setAggregationType(CombinationType.Multiply);
        Timer tm = Timer.getNamed("Multiply Class with Candidate similarities", t);
        SimilarityMatrix<TableRow> combined = c.match(getClassSimilarity(), getSimilarities().getCandidateSimilarity(), new TableToRowHierarchyAdapter());
        tm.stop();

        combined.normalize();

        //getSimilarities().setInitialCandidateSimilarity(combined);
        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Majority Class decision:");
            System.out.println(getSimilarities().getClassSimilarity().getOutput());
            System.out.println(String.format("Remove candiates with wrong class: removed %d/%d", before - combined.getNumberOfNonZeroElements(), before));
        }
        t.stop();

        return combined;
    }
}
