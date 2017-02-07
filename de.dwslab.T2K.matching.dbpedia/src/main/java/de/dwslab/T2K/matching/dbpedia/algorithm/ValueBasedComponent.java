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

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.dbpedia.AlwaysMatchSimilarityFunction;
import de.dwslab.T2K.matching.dbpedia.WebJaccardSimilarity;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.ValueBasedInstanceMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.LabelBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnToCellHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowToCellHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableToColumnMatchingAdapter;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.secondline.Aggregate;
import de.dwslab.T2K.matching.secondline.AggregationType;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.CombineHierarchy;
import de.dwslab.T2K.matching.similarity.signatures.JaccardPrefixFiltering;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.IdentitySimilarity;
import de.dwslab.T2K.similarity.functions.QuadraticSimilarityDecorator;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.date.WeightedDatePartSimilarity;
import de.dwslab.T2K.similarity.functions.numeric.DeviationSimilarity;
import de.dwslab.T2K.similarity.functions.set.ComplexSetSimilarity;
import de.dwslab.T2K.similarity.functions.set.LeftSideCoverage;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.timer.Timer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ValueBasedComponent extends WebtableToDBpediaMatchingComponent {
    
    private SimilarityMatrix<TableCell> valueSimilarity;
    public SimilarityMatrix<TableCell> getValueSimilarity() {
        return valueSimilarity;
    }
    
    private SimilarityMatrix<TableColumn> labelSimilarity;
    public SimilarityMatrix<TableColumn> getLabelSimilarity() {
        return labelSimilarity;
    }
    
    public double evaluate(Configuration config) {
        super.evaluate(config);
        //TODO we can't use IterativeComponent here, as ValueSimilarities are not set yet ...
        IterativeComponent it = new IterativeComponent();
        it.initialise(getMatchers(), getData(), getEvaluationParameters(), getGoldStandard(), getLogger(), getMatchingParameters(), getSimilarities(), getRootTimer());
        
        //Map<Parameter, Object> m = new HashMap<>();
        //TODO fill map with default parameters...
        //Configuration c = new Configuration(m);
        //it.run(config);
        
        //return (it.getPropertyResult().getF1Score() + it.getInstanceResult().getF1Score()) / 2.0;
        return 0.0;
    }
  
    public static final Parameter PAR_NUMERIC_SIMILARITY = new Parameter("ValueBasedInstanceMatcher.NumericSimilarity", new  QuadraticSimilarityDecorator<Double>(new DeviationSimilarity()));
    public static final Parameter PAR_NUMERIC_SET_SIMILARITY = new Parameter("ValueBasedInstanceMatcher.NumericSetSimilarity", new MaxSimilarity<Double>());
    
    public static final Parameter PAR_STRING_SIMILARITY = new Parameter("ValueBasedInstanceMatcher.StringSimilarity", new WebJaccardSimilarity());
    public static final Parameter PAR_STRING_SET_SIMILARITY = new Parameter("ValueBasedInstanceMatcher.StringSetSimilarity", new MaxSimilarity<String>());
    public static final Parameter PAR_STRING_FILTERING = new Parameter("ValueBasedInstanceMatcher.StringFiltering", new JaccardPrefixFiltering<>());
    public static final Parameter PAR_STRING_SIMILARITY_FOR_SETS = new Parameter("ValueBasedInstanceMatcher.StringSimilarityForSets", null);
    
    public static final Parameter PAR_DATE_SIMILARITY = new Parameter("ValueBasedInstanceMatcher.DateSimilarity", new WeightedDatePartSimilarity(0, 0, 1));
    public static final Parameter PAR_DATE_SET_SIMILARITY = new Parameter("ValueBasedInstanceMatcher.DateSetSimilarity", new MaxSimilarity<Double>());

    public static final Parameter PAR_LINK_SIMILARITY = new Parameter("ValueBasedInstanceMatcher.LinkSimilarity", new IdentitySimilarity<String>());
    public static final Parameter PAR_LINK_SET_SIMILARITY = new Parameter("ValueBasedInstanceMatcher.LinkSetSimilarity", new LeftSideCoverage<String>());
    
    public static final Parameter PAR_VALUE_SIMILARITY_THRESHOLD = new Parameter("ValueBasedInstanceMatcher.SimilarityThreshold", 0.0);
    public static final Parameter PAR_RUN_LABEL_MATCHING = new Parameter("ValueBasedInstanceMatcher.RunLabelMatching",true);
    public static final Parameter PAR_LABEL_SIMILARITY = new Parameter("ValueBasedIntanceMatcher.LabelSimilarity", new AlwaysMatchSimilarityFunction());
    
    //public static final Parameter PAR_VALUE_KEY_WEIGHT = new Parameter("ValueBasedInstanceMatcherKeyWeight", 0.9);
    double PAR_VALUE_KEY_WEIGHT = 2.0/3.0;
    
    private static final List<Parameter> params;
    public static List<Parameter> getParams() {
        return params;
    }
    
    static {
        params = new ArrayList<>();
        params.add(PAR_NUMERIC_SIMILARITY);
        params.add(PAR_NUMERIC_SET_SIMILARITY);
        params.add(PAR_STRING_SIMILARITY);
        params.add(PAR_STRING_SET_SIMILARITY);
        params.add(PAR_STRING_FILTERING);
        params.add(PAR_STRING_SIMILARITY_FOR_SETS);
        params.add(PAR_DATE_SIMILARITY);
        params.add(PAR_DATE_SET_SIMILARITY);
        params.add(PAR_LINK_SIMILARITY);
        params.add(PAR_LINK_SET_SIMILARITY);
        params.add(PAR_VALUE_SIMILARITY_THRESHOLD);
        //params.add(PAR_VALUE_KEY_WEIGHT);
        params.add(PAR_RUN_LABEL_MATCHING);
        params.add(PAR_LABEL_SIMILARITY);
    }
    
    public ValueBasedComponent() {
        setParameters(params);
    }
    
    @SuppressWarnings("unchecked")
    protected void initialiseParameters(Configuration config) {
        ValueBasedInstanceMatcher vm = getMatchers().getValueBasedInstanceMatcher();
        
        vm.setStringSimilarityFunction((SimilarityFunction<String>) config.getValue(PAR_STRING_SIMILARITY));
        vm.setStringFilter((SignatureFilter<TableCell>) config.getValue(PAR_STRING_FILTERING));
        vm.setStringSetSimilarity((ComplexSetSimilarity<String>) config.getValue(PAR_STRING_SET_SIMILARITY));
        vm.setStringSimilarityFunctionForSets((SimilarityFunction<String>)config.getValue(PAR_STRING_SIMILARITY_FOR_SETS));
        vm.setNumericSimilarityFunction((SimilarityFunction<Double>) config.getValue(PAR_NUMERIC_SIMILARITY));
        vm.setNumericSetSimilarity((ComplexSetSimilarity<Double>) config.getValue(PAR_NUMERIC_SET_SIMILARITY));
        vm.setDateSimilarityFunction((SimilarityFunction<Date>) config.getValue(PAR_DATE_SIMILARITY));
        vm.setDateSetSimilarity((ComplexSetSimilarity<Date>) config.getValue(PAR_DATE_SET_SIMILARITY));
        vm.setLinkSimilarityFunction((SimilarityFunction<String>) config.getValue(PAR_LINK_SIMILARITY));
        vm.setLinkSetSimilarityFunction((ComplexSetSimilarity<String>) config.getValue(PAR_LINK_SET_SIMILARITY));
        vm.setSimilarityThreshold((Double) config.getValue(PAR_VALUE_SIMILARITY_THRESHOLD));
        vm.setKeyWeight(PAR_VALUE_KEY_WEIGHT);
        
        LabelBasedSchemaMatcher lm = getMatchers().getLabelBasedSchemaMatcher();
        lm.setLabelSimilarity((SimilarityFunction<String>)config.getValue(PAR_LABEL_SIMILARITY));
    }
    
    @Override
    public void run(Configuration config) {
        initialiseParameters(config);
        
        System.out.println("Value-based Component");
        
        // Value-based instance matching
        getMatchers().getValueBasedInstanceMatcher().setCandidateSimilarity(getSimilarities().getCandidateSimilarity());
        ValueBasedInstanceMatcher m = getMatchers().getValueBasedInstanceMatcher();
        valueSimilarity = m.match(getData());
        
        
        if((boolean)config.getValue(PAR_RUN_LABEL_MATCHING)) {
            // Label-based schema matching
            getMatchers().getLabelBasedSchemaMatcher().setClassSimilarity(getSimilarities().getClassSimilarity());
            labelSimilarity = getMatchers().getLabelBasedSchemaMatcher().match(getData());
            reduceLabelSimilarity();
            calculatePropertyOverall();              
        }
        else {
            //needed?
            //simplifiedLabelMatching();
        }
    }
    
    protected void reduceLabelSimilarity() {
        Timer t = Timer.getNamed("Reduce Label similarity", getRootTimer());
        // remove similarities that can be excluded from class mapping
        CombineHierarchy<Table, TableColumn> c = new CombineHierarchy<Table, TableColumn>();
        c.setAggregationType(CombinationType.Multiply);
        //getSimilarities().setLabelSimilarity(c.match(getSimilarities().getClassSimilarity(), getSimilarities().getLabelSimilarity().copy(), new TableToColumnMatchingAdapter()));
        labelSimilarity = c.match(getSimilarities().getClassSimilarity(), labelSimilarity, new TableToColumnMatchingAdapter());
        
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Pruned label similarity:");
            System.out.println(labelSimilarity.getOutput());
        }
        t.stop();
    }

    protected void simplifiedLabelMatching() {
    	Timer tim = Timer.getNamed("Create Column-Column Matrix", getRootTimer());
    	
    	labelSimilarity = new SparseSimilarityMatrix<>(0, 0);
    	
    	Table wt = getData().getWebtable();
    	
    	for(TableColumn c : wt.getColumns()) {
    		
    		for(Table t : getSimilarities().getClassSimilarity().getMatches(wt)) {
    			
    			for(TableColumn c2 : t.getColumns()) {
    				
    				labelSimilarity.set(c, c2, 1.0);
    				
    			}
    			
    		}
    		
    	}
    	
    	tim.stop();
    }

    /**
     * This method only prints out aggregated scores for logging, it does not influence the matching process!
     */
    protected void calculatePropertyOverall() {
        if(getMatchingParameters().isCollectMatchingInfo()) {
            
            Timer t = Timer.getNamed("Calculate property overall scores", getRootTimer());
            
            // remove the similarities of all candidates that have been excluded, but do not change the value similarities
            SimilarityMatrix<TableCell> remainingValueSim = Matcher.multiplyParentSimilarity(getSimilarities().getCandidateSimilarity().copy().makeBinary(0.0), getValueSimilarity(), new TableRowToCellHierarchyAdapter());
            
            // calculate the score for the best mapping for each column
            // aggregate combined instance similarities to columns
            Aggregate<TableColumn, TableCell> a = new Aggregate<TableColumn, TableCell>();
            a.setAggregationType(AggregationType.Sum);
            a.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
            Timer tm = Timer.getNamed("Aggregate value-based similarities", t);
            SimilarityMatrix<TableColumn> instSchemaSim = a.match(getLabelSimilarity(), remainingValueSim, new TableColumnToCellHierarchyAdapter());
            tm.stop();
    //        instSchemaSim.normalize();
    //        makeKeysMatch(instSchemaSim);
    
            //SimilarityMatrix<TableColumn> propSim = instSchemaSim;
            
    //        BestChoiceMatching constraintMatcher = new BestChoiceMatching();
    //        constraintMatcher.setForceOneToOneMapping(isForcePropertyOneToOneMapping());
    //        tm = Timer.getNamed("BestChoice: properties overall", t);
    //        SimilarityMatrix<TableColumn> properties = constraintMatcher.match(propSim);
    //        tm.stop();
    
            getLogger().logData("Column-aggregated value similarities (initial):\n" + instSchemaSim.getOutput());
        
            //System.out.println(a.getLog());
            
            System.out.println("Column-aggregated value similarities (initial)");
            System.out.println(instSchemaSim.getOutput());
            
            System.out.println(a.getLog().toString());
//            
//            System.out.println("Detailed aggregation matrices (sums):");
//            
//            for(TableColumn webCol : instSchemaSim.getFirstDimension()) {
//                for(TableColumn dbpCol : instSchemaSim.getMatches(webCol)) {
//                    CombineHierarchy<TableColumn, TableCell> c = new CombineHierarchy<TableColumn, TableCell>();
//                    c.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
//                    c.setAggregationType(CombinationType.Multiply);
//                    
//                    SimilarityMatrix<TableColumn> p = new SparseSimilarityMatrixFactory().createSimilarityMatrix(1, 1);
//                    p.set(webCol, dbpCol, 1.0);
//                    
//                    SimilarityMatrix<TableCell> m = c.match(p, getSimilarities().getInstanceSimilarity(), new TableColumnToCellHierarchyAdapter());
//                    
//                    System.out.println();
//                    System.out.println(String.format("%s <-> %s:", webCol.getHeader(), dbpCol.getHeader()));
//                    System.out.println(m.getOutput2(null, null, new TableCellMatchingAdapter(), new TableCellLabelAdapter()));
//                }
//            }
//            t.stop();
            
            t.stop();
        }
    }
}
