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

//package de.mannheim.uni.matching.dbpedia;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map.Entry;
//
//import au.com.bytecode.opencsv.CSVWriter;
//import de.mannheim.uni.index.io.DefaultIndex;
//import de.mannheim.uni.matching.blocking.IdentityBlocking;
//import de.mannheim.uni.matching.dbpedia.algorithm.EvaluationParameters;
//import de.mannheim.uni.matching.dbpedia.algorithm.MatchingParameters;
//import de.mannheim.uni.matching.dbpedia.algorithm.Similarities;
//import de.mannheim.uni.matching.dbpedia.algorithm.WebtableToDBpediaMatcher;
//import de.mannheim.uni.matching.dbpedia.model.MatchingEvaluation;
//import de.mannheim.uni.matching.dbpedia.model.MatchingResult;
//import de.mannheim.uni.matching.dbpedia.model.TableCell;
//import de.mannheim.uni.matching.dbpedia.model.TableRow;
//import de.mannheim.uni.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
//import de.mannheim.uni.matching.dbpedia.model.adapters.TableColumnToCellHierarchyAdapter;
//import de.mannheim.uni.matching.dbpedia.model.adapters.TableColumnUriMatchingAdapter;
//import de.mannheim.uni.matching.dbpedia.model.adapters.TableRowToCellHierarchyAdapter;
//import de.mannheim.uni.matching.dbpedia.model.adapters.TableToRowHierarchyAdapter;
//import de.mannheim.uni.matching.evaluation.EvaluationResult;
//import de.mannheim.uni.matching.firstline.FastJoinMatcher;
//import de.mannheim.uni.matching.firstline.LabelBasedMatcher;
//import de.mannheim.uni.matching.firstline.FastJoinMatcher.FastJoinMeasure;
//import de.mannheim.uni.matching.secondline.Aggregate;
//import de.mannheim.uni.matching.secondline.CombinationType;
//import de.mannheim.uni.matching.secondline.Combine;
//import de.mannheim.uni.matching.secondline.CombineHierarchy;
//import de.mannheim.uni.matching.secondline.AggregationType;
//import de.mannheim.uni.matching.secondline.OneToOneConstraint;
//import de.mannheim.uni.matching.secondline.ConflictResolution;
//import de.mannheim.uni.matching.secondline.ParentOneToOneConstraint;
//import de.mannheim.uni.similarity.matrix.ArrayBasedSimilarityMatrixFactory;
//import de.mannheim.uni.similarity.matrix.SimilarityMatrix;
//import de.mannheim.uni.similarity.matrix.SparseSimilarityMatrixFactory;
//import de.mannheim.uni.similarity.measures.StringSimilarityMeasure;
//import de.mannheim.uni.tableprocessor.model.Table;
//import de.mannheim.uni.tableprocessor.model.TableColumn;
//import de.mannheim.uni.utils.data.ValueAggregator;
//
///**
// * this class is used to test different combinations of the steps included in WebtableToDBpediaMatcher
// * @author Oliver
// *
// */
//public class StepwiseWebtableToDBpediaMatcher extends WebtableToDBpediaMatcher {
//
//    SimilarityMatrix<TableRow> instanceToRow;
//    SimilarityMatrix<TableRow> candidateInstanceToRow;
//    
//    SimilarityMatrix<TableColumn> instanceToCol;
//    SimilarityMatrix<TableColumn> labelInstanceToCol;
//    
//    SimilarityMatrix<TableCell> keyWeightedInstance;
//    SimilarityMatrix<TableCell> headerWeightedInstance;
//    
//    SimilarityMatrix<TableColumn> keyWeightedInstanceToCol;
//    SimilarityMatrix<TableRow> headerWeightedInstanceToRow;
//    
//    SimilarityMatrix<TableColumn> labelKeyWeightedInstanceToCol;
//    SimilarityMatrix<TableRow> candidateHeaderWeightedInstanceToRow;
//    
//    
//    public HashMap<String, List<EvaluationResult>> runMatchWebTable(String webtablePath) {
//            HashMap<String, List<EvaluationResult>> results = new LinkedHashMap<String, List<EvaluationResult>>();
//            List<EvaluationResult> list;
//
//            // reset candidate similarity in case this is not the first run for this
//            // instance
//            //setCandidateSimilarity(null);
//            //setPropertySimilarity(null);
//            setSimilarities(new Similarities());
//            instanceToRow = null;
//            candidateInstanceToRow = null;
//            instanceToCol = null;
//            labelInstanceToCol = null;
//            keyWeightedInstance = null;
//            headerWeightedInstance = null;
//            keyWeightedInstanceToCol = null;
//            headerWeightedInstanceToRow = null;
//            labelKeyWeightedInstanceToCol = null;
//            candidateHeaderWeightedInstanceToRow = null;
//
//            String webTableName = new File(webtablePath).getName();
//
//            loadWebTable(webtablePath);
//            
//            initialiseEvaluation(webTableName);
//            
//            list = new ArrayList<EvaluationResult>();
////            for(int i = 10; i>0;i--) {
////                double d = (double) i / 10.0;
////                
////                setFastJoinDelta(d);
////                setFastJoinTau(d);
////                
////                runCandidateSelection();
////                EvaluationResult instanceBaseLine = evaluateInstances(getInitialCandidateSimilarity(), webTableName, false,
////                    null);
////                list.add(instanceBaseLine);
////            }
////            results.put("baseline", list);
//            results.put("baseline", runStringMatchingTests());
//
//            
//            
//            runInitialClassMatching();
//            runClassMatching();
//            //updateCandidatesBasedOnClasses();
//            
//            runInstanceMatching();
//            runLabelMatching();
//
//            
//            String s = "";
//            
//            SimilarityMatrix<TableColumn> propSim = null;
//            SimilarityMatrix<TableRow> candSim = null;
//            int maxThreshold = 6;
//            
//            for(int i = 1; i <= 4; i++) {
//            
//                if(i==2) {
//                    updateCandidatesBasedOnClasses();
//                    
//                    runInstanceMatching();
//                    runLabelMatching();
//                } else if(i==3) {
//                    getMatchingParameters().setFastJoinDelta(0.5); // = edit distance threshold
//                    getMatchingParameters().setFastJoinTau(0.3); // = jaccard threshold
//                    runCandidateRefinement();
//                    
//                    runInstanceMatching();
//                    runLabelMatching();
//                }
////                
////                instanceToRow = null;
////                candidateInstanceToRow = null;
////                instanceToCol = null;
////                labelInstanceToCol = null;
////                keyWeightedInstance = null;
////                headerWeightedInstance = null;
////                keyWeightedInstanceToCol = null;
////                headerWeightedInstanceToRow = null;
////                labelKeyWeightedInstanceToCol = null;
////                candidateHeaderWeightedInstanceToRow = null;
////                
////                // (I-A) apply similarity threshold to key/label similarities (=improved baseline)
////                list = new ArrayList<EvaluationResult>();
////                for(int j = 0; j < 10; j++) {
////                    double d = (double)j / 10;
////                    list.add(applyKeyThreshold(d));
////                }
////                results.put(i + " I-A cand", list);
////                
////                // (I-B) aggregate instance similarities per candidate and apply threshold
////                list = new ArrayList<EvaluationResult>();
////                for(int j = 0; j < 10; j++) {
////                    double d = (double)j / 10;
////                    list.add(aggregateInstanceToRow(d, AggregationType.Average));
////                }
////                results.put(i + " I-B inst:avg>row", list);
////                
////                // (I-C) combine aggregated instance similarities and candidate similarities (=label similarities) and apply threshold
////                list = new ArrayList<EvaluationResult>();
////                for(int j = 0; j < 10; j++) {
////                    double d = (double)j / 10;
////                    list.add(keyAndAggregatedInstance(d, AggregationType.Average, CombinationType.WeightedSum, 0.75, 0.25));
////                }
////                results.put(i + " I-C cand*a + inst:avg>row*b", list);
////                // --> instance similarities don't really make a difference, most important are candidate scores
////                
////                
////                // (I-D) weight candidate similarities with class similarities and apply threshold
////                list = new ArrayList<EvaluationResult>();
////                for(int j = 0; j < 10; j++) {
////                    double d = (double)j / 10;
////                    list.add(applyThresholdClass(d, CombinationType.Multiply));
////                }
////                results.put(i + " I-D cand*cls", list);
////                // --> still achieves the best results
////                
////                
////                // (P-A) apply threshold to header/label similarity of columns/properties (=improved baseline)
////                list = new ArrayList<EvaluationResult>();
////                for(int j = 0; j < 10; j++) {
////                    double d = (double)j / 10;
////                    list.add(applyHeaderThreshold(d));
////                }
////                results.put(i + " P-A lbl", list);
////                
////                // (P-B) aggregate instance similarities per property and apply threshold
////                list = new ArrayList<EvaluationResult>();
////                for(int j = 0; j < 10; j++) {
////                    double d = (double)j / 10;
////                    list.add(aggregateInstanceToCol(d, AggregationType.Average));
////                }
////                results.put(i + " P-B inst:avg>col", list);
////                
////                // (P-C) combine aggregated instance similarities and property similarities (=label similarities) and apply threshold
////                list = new ArrayList<EvaluationResult>();
////                for(int j = 0; j < 10; j++) {
////                    double d = (double)j / 10;
////                    list.add(headerAndAggregatedInstance(d, AggregationType.Average, CombinationType.WeightedSum, 0.75, 0.25));
////                }
////                results.put(i + " P-C [75,25] lbl*a + inst:avg>col*b", list);
////
////                
////    
////                
////                // (P-B based on I-C) aggregate candidate-instance similarities (I-C) per property and apply threshold
////                list = new ArrayList<EvaluationResult>();
////                for(int j = 0; j < 10; j++) {
////                    double d = (double)j / 10;
////                    list.add(aggregateKeyWeightedInstance(d, CombinationType.WeightedSum));
////                }
////                results.put(i + " P-B+/I-C [75,25](cand*a + inst*b):avg>col", list);
////
////    
////                // (P-C based on I-C) combine aggregated candidate-instance similarities and property similarities (=label similarities) and apply threshold
////                list = new ArrayList<EvaluationResult>();
////                for(int j = 0; j < 10; j++) {
////                    double d = (double)j / 10;
////                    list.add(headerAndAggregateKeyWeightedInstance(d, AggregationType.Average, CombinationType.WeightedSum, 0.75, 0.25));
////                }
////                results.put(i + " P-C+/I-C [75,25]lbl*a + (cand*.75 + inst*.25):avg>col*b", list);
////
////                
////                
////                // (I-B based on P-C) aggregate property-instance similarities (P-C) per candidate and apply threshold (should filter out unmapped columns)
////                list = new ArrayList<EvaluationResult>();
////                for(int j = 0; j < 10; j++) {
////                    double d = (double)j / 10;
////                    list.add(aggregateHeaderWeightedInstance(d, CombinationType.WeightedSum));
////                }
////                results.put(i + " I-B+/P-C [75,25](lbl*a + inst*b):avg>row", list);
////                
////                // (I-C based on P-C)
////                list = new ArrayList<EvaluationResult>();
////                for(int j = 0; j < 10; j++) {
////                    double d = (double)j / 10;
////                    list.add(keyAndAggregatedHeaderWeightedInstance(d, AggregationType.Average, CombinationType.WeightedSum, 0.75, 0.25));
////                }
////                results.put(i + " I-C+/P-C [75,25]cand*a + (lbl*.75 + inst*.25):avg>row", list);
////                
////                // (I-D based on I-C/P-C) include class scores
////                list = new ArrayList<EvaluationResult>();
////                for(int j = 0; j < 10; j++) {
////                    double d = (double)j / 10;
////                    list.add(classKeyAndAggregatedHeaderWeightedInstance(d));
////                }
////                results.put(i + " I-D+/I-C/P-C cls * ...", list);
////            
////                
////                    SimilarityMatrix<TableColumn> tmp = getPropertySimilarity();
////                    setPropertySimilarity(propSim);
////                    propSim = tmp;
////                
////                    SimilarityMatrix<TableRow> tmpC = getCandidateSimilarity();
////                    setCandidateSimilarity(candSim);
////                    candSim = tmpC;
//                
//                instanceToRow = null;
//                candidateInstanceToRow = null;
//                instanceToCol = null;
//                labelInstanceToCol = null;
//                keyWeightedInstance = null;
//                headerWeightedInstance = null;
//                keyWeightedInstanceToCol = null;
//                headerWeightedInstanceToRow = null;
//                labelKeyWeightedInstanceToCol = null;
//                candidateHeaderWeightedInstanceToRow = null;
//                
//              // (I-A) apply similarity threshold to key/label similarities (=improved baseline)
//              list = new ArrayList<EvaluationResult>();
//              for(int j = 0; j < maxThreshold; j++) {
//                  double d = (double)j / 10;
//                  list.add(applyKeyThreshold(d));
//              }
//              results.put(i + " I-A cand", list);
//              
//              // (I-B) aggregate instance similarities per candidate and apply threshold
//              list = new ArrayList<EvaluationResult>();
//              for(int j = 0; j < maxThreshold; j++) {
//                  double d = (double)j / 10;
//                  list.add(aggregateInstanceToRow(d, AggregationType.Average));
//              }
//              results.put(i + " I-B inst:avg>row", list);
//              
//              // (I-C) combine aggregated instance similarities and candidate similarities (=label similarities) and apply threshold
//              list = new ArrayList<EvaluationResult>();
//              for(int j = 0; j < maxThreshold; j++) {
//                  double d = (double)j / 10;
//                  list.add(keyAndAggregatedInstance(d, AggregationType.Average, CombinationType.WeightedSum, 0.75, 0.25));
//              }
//              results.put(i + " I-C cand*a + inst:avg>row*b", list);
//              // --> instance similarities don't really make a difference, most important are candidate scores
//              
//              
//              // (I-D) weight candidate similarities with class similarities and apply threshold
//              list = new ArrayList<EvaluationResult>();
//              for(int j = 0; j < maxThreshold; j++) {
//                  double d = (double)j / 10;
//                  list.add(applyThresholdClass(d, CombinationType.Multiply));
//              }
//              results.put(i + " I-D cand*cls", list);
//              // --> still achieves the best results
//              
//              
//              // (P-A) apply threshold to header/label similarity of columns/properties (=improved baseline)
//              list = new ArrayList<EvaluationResult>();
//              for(int j = 0; j < maxThreshold; j++) {
//                  double d = (double)j / 10;
//                  list.add(applyHeaderThreshold(d));
//              }
//              results.put(i + " P-A lbl", list);
//              
//              // (P-B) aggregate instance similarities per property and apply threshold
//              list = new ArrayList<EvaluationResult>();
//              for(int j = 0; j < maxThreshold; j++) {
//                  double d = (double)j / 10;
//                  list.add(aggregateInstanceToCol(d, AggregationType.Average));
//              }
//              results.put(i + " P-B inst:avg>col", list);
//                
//                list = new ArrayList<EvaluationResult>();
//                for(int j = 0; j < maxThreshold; j++) {
//                    double d = (double)j / 10;
//                    list.add(headerAndAggregatedInstance(d, AggregationType.Average, CombinationType.Multiply, 0.75, 0.25));
//                }
//                results.put(i + " P-C2 lbl * inst:avg>col", list);
//                list = new ArrayList<EvaluationResult>();
//                for(int j = 0; j < maxThreshold; j++) {
//                    double d = (double)j / 10;
//                    list.add(aggregateKeyWeightedInstance(d, CombinationType.Multiply));
//                }
//                results.put(i + " P-B2+/I-C [75,25](cand * inst*b):avg>col", list);
//                list = new ArrayList<EvaluationResult>();
//                for(int j = 0; j < maxThreshold; j++) {
//                    double d = (double)j / 10;
//                    list.add(headerAndAggregateKeyWeightedInstance(d, AggregationType.Average, CombinationType.Multiply, 0.75, 0.25));
//                }
//                results.put(i + " P-C2+/I-C [75,25]lbl * (cand * inst):avg>col", list);
//                list = new ArrayList<EvaluationResult>();
//                for(int j = 0; j < maxThreshold; j++) {
//                    double d = (double)j / 10;
//                    list.add(keyAndAggregatedInstance(d, AggregationType.Average, CombinationType.Multiply, 0.75, 0.25));
//                }
//                results.put(i + " I-C2 cand * inst:avg>row", list);
//                list = new ArrayList<EvaluationResult>();
//                for(int j = 0; j < maxThreshold; j++) {
//                    double d = (double)j / 10;
//                    list.add(aggregateHeaderWeightedInstance(d, CombinationType.Multiply));
//                }
//                results.put(i + " I-B2+/P-C [75,25](lbl * inst):avg>row", list);
//                list = new ArrayList<EvaluationResult>();
//                for(int j = 0; j < maxThreshold; j++) {
//                    double d = (double)j / 10;
//                    list.add(keyAndAggregatedHeaderWeightedInstance(d, AggregationType.Average, CombinationType.Multiply, 0.75, 0.25));
//                }
//                results.put(i + " I-C2+/P-C [75,25]cand * (lbl * inst):avg>row", list);
//                list = new ArrayList<EvaluationResult>();
//                for(int j = 0; j < maxThreshold; j++) {
//                    double d = (double)j / 10;
//                    list.add(classKeyAndAggregatedHeaderWeightedInstance(d));
//                }
//                results.put(i + " I-D+/I-C/P-C cls * ...", list);
//                
////                    tmp = getPropertySimilarity();
////                    setPropertySimilarity(propSim);
////                    propSim = tmp;
////                
////                    tmpC = getCandidateSimilarity();
////                    setCandidateSimilarity(candSim);
////                    candSim = tmpC;
//            }
//            
//            System.out.println("*************************************************");
//            System.out.println("Finished iterations");
//            System.out.println("*************************************************");
//            System.out.println(getWebtable().getHeader());
//            System.out.println("*************************************************");
//            
//            System.out
//                    .println("                    \tPrecision\tRecall\t    F1\t#correct/#mappings/#reference\n");
//            
//            for(Entry<String, List<EvaluationResult>> e : results.entrySet()) {
//
//                List<EvaluationResult> lst = e.getValue();
//
//                for(EvaluationResult r : lst) {
//                    System.out.println(String.format(
//                            "%-20s:\t     %.2f\t  %.2f\t  %.2f\t%d/%d/%d", e.getKey(),
//                            r.getPrecision(), r.getRecall(), r.getF1Score(),
//                            r.getCorrect(), r.getInputSetSize(), r.getReferenceSetSize()));
//                
//                }
//            }
//
//            return results;
//    }
//    
//    private ArrayList<EvaluationResult> runStringMatchingTests() {
//        ArrayList<EvaluationResult> list = new ArrayList<EvaluationResult>();
//        EvaluationResult instanceBaseLine = null;
//        
//        getMatchingParameters().setFastJoinMeasure(FastJoinMeasure.FCOSINE);
//        
//        getMatchingParameters().setFastJoinDelta(0.1);
//        getMatchingParameters().setFastJoinTau(0.1);
//        runCandidateSelection();
//        instanceBaseLine = evaluateInstances(getSimilarities().getInitialCandidateSimilarity(), getWebtable().getHeader(), false, null);
//        list.add(instanceBaseLine);
//        
//        getMatchingParameters().setFastJoinDelta(0.5);
//        getMatchingParameters().setFastJoinTau(0.5);
//        runCandidateSelection();
//        instanceBaseLine = evaluateInstances(getSimilarities().getInitialCandidateSimilarity(), getWebtable().getHeader(), false, null);
//        list.add(instanceBaseLine);
//        
//        getMatchingParameters().setFastJoinDelta(1);
//        getMatchingParameters().setFastJoinTau(1);
//        runCandidateSelection();
//        instanceBaseLine = evaluateInstances(getSimilarities().getInitialCandidateSimilarity(), getWebtable().getHeader(), false, null);
//        list.add(instanceBaseLine);
//        
//        getMatchingParameters().setFastJoinMeasure(FastJoinMeasure.FDICE);
//        
//        getMatchingParameters().setFastJoinDelta(0.1);
//        getMatchingParameters().setFastJoinTau(0.1);
//        runCandidateSelection();
//        instanceBaseLine = evaluateInstances(getSimilarities().getInitialCandidateSimilarity(), getWebtable().getHeader(), false, null);
//        list.add(instanceBaseLine);
//        
//        getMatchingParameters().setFastJoinDelta(0.5);
//        getMatchingParameters().setFastJoinTau(0.5);
//        runCandidateSelection();
//        instanceBaseLine = evaluateInstances(getSimilarities().getInitialCandidateSimilarity(), getWebtable().getHeader(), false, null);
//        list.add(instanceBaseLine);
//        
//        getMatchingParameters().setFastJoinDelta(1);
//        getMatchingParameters().setFastJoinTau(1);
//        runCandidateSelection();
//        instanceBaseLine = evaluateInstances(getSimilarities().getInitialCandidateSimilarity(), getWebtable().getHeader(), false, null);
//        list.add(instanceBaseLine);
//        
//        getMatchingParameters().setFastJoinMeasure(FastJoinMeasure.FJACCARD);
//        
//        getMatchingParameters().setFastJoinDelta(0.1);
//        getMatchingParameters().setFastJoinTau(0.1);
//        runCandidateSelection();
//        instanceBaseLine = evaluateInstances(getSimilarities().getInitialCandidateSimilarity(), getWebtable().getHeader(), false, null);
//        list.add(instanceBaseLine);
//        
//        getMatchingParameters().setFastJoinDelta(0.5);
//        getMatchingParameters().setFastJoinTau(0.5);
//        runCandidateSelection();
//        instanceBaseLine = evaluateInstances(getSimilarities().getInitialCandidateSimilarity(), getWebtable().getHeader(), false, null);
//        list.add(instanceBaseLine);
//        
//        getMatchingParameters().setFastJoinDelta(1);
//        getMatchingParameters().setFastJoinTau(1);
//        runCandidateSelection();
//        instanceBaseLine = evaluateInstances(getSimilarities().getInitialCandidateSimilarity(), getWebtable().getHeader(), false, null);
//        list.add(instanceBaseLine);
//        
//        return list;
//    }
//    
//    private void updateCandidatesBasedOnClasses() {
//        OneToOneConstraint one = new OneToOneConstraint(ConflictResolution.Maximum);
//        SimilarityMatrix<Table> sim = one.match(getSimilarities().getClassSimilarity());
//        
//        CombineHierarchy<Table, TableRow> c = new CombineHierarchy<Table, TableRow>();
//        c.setAggregationType(CombinationType.Multiply);
//        SimilarityMatrix<TableRow> combined = c.match(sim, getSimilarities().getInitialCandidateSimilarity(), new TableToRowHierarchyAdapter());
//        
//        combined.normalize();
//        
//        getSimilarities().setInitialCandidateSimilarity(combined);
//    }
//    
//    private EvaluationResult applyKeyThreshold(double threshold) {
//        SimilarityMatrix<TableRow> sim = getSimilarities().getCandidateSimilarity().copy();
//        
//        if(threshold>0) {
//            sim.prune(threshold);
//        }
//        
//        return evaluateInstances(sim, getWebtable().getHeader(), false, null);
//    }
//    
//    private EvaluationResult applyThresholdClass(double threshold, CombinationType at) {
//        // combine class and candidate similarities
//        CombineHierarchy<Table, TableRow> c = new CombineHierarchy<Table, TableRow>();
//        c.setAggregationType(at);
//        c.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
//        c.setSimilarityMatrixFactory(new SparseSimilarityMatrixFactory());
//        SimilarityMatrix<TableRow> result = c.match(getSimilarities().getClassSimilarity(), getSimilarities().getCandidateSimilarity(), new TableToRowHierarchyAdapter());
//        
//        if(threshold>0) {
//            result.prune(threshold);
//        }
//        
//        return evaluateInstances(result, getWebtable().getHeader(), false, null);
//    }
//    
//    private EvaluationResult applyHeaderThreshold(double threshold) {
//        SimilarityMatrix<TableColumn> sim = getSimilarities().getPropertySimilarity().copy();
//        
//        if(threshold>0) {
//            sim.prune(threshold);
//        }
//        
//        return evaluateProperties(sim, getWebtable().getHeader(), false, null);
//    }
//    
//    /**
//     * aggregate instance similarities to property scores and apply a threshold
//     * @param threshold
//     * @return
//     */
//    private EvaluationResult aggregateInstanceToCol(double threshold, AggregationType at) {
//        if(instanceToCol==null) {
//            
//            //TODO prune here
//            
//         // constraint: one value of one instance can only be mapped to one value of every candidate
//            ParentOneToOneConstraint<TableRow, TableCell> c = new ParentOneToOneConstraint<TableRow, TableCell>(ConflictResolution.Maximum);
//            c.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
//            SimilarityMatrix<TableCell> inst = c.match(getSimilarities().getCandidateSimilarity(), getSimilarities().getInstanceSimilarity(), new TableRowToCellHierarchyAdapter());
//            
//            Aggregate<TableColumn, TableCell> a = new Aggregate<TableColumn, TableCell>();
//            a.setAggregationType(at);
//            a.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
//            instanceToCol = a.match(getSimilarities().getPropertySimilarity(), inst, new TableColumnToCellHierarchyAdapter());
//        }
//        SimilarityMatrix<TableColumn> result = instanceToCol;
//        
//        if(threshold>0) {
//            result = instanceToCol.copy();
//            result.prune(threshold);
//        }
//        
//        return evaluateProperties(result, getWebtable().getHeader(), false, null);
//    }
//    
//    /**
//     * aggregate instance similarities to candidate scores and apply a threshold
//     * @param threshold
//     * @param at
//     * @return
//     */
//    private EvaluationResult aggregateInstanceToRow(double threshold, AggregationType at) {
//        
//        if(instanceToRow==null) {
//            
//            //TODO prune here!
//            
//            // constraint: one value of one instance can only be mapped to one value of every candidate
//            ParentOneToOneConstraint<TableRow, TableCell> c = new ParentOneToOneConstraint<TableRow, TableCell>(ConflictResolution.Maximum);
//            c.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
//            SimilarityMatrix<TableCell> inst = c.match(getSimilarities().getCandidateSimilarity(), getSimilarities().getInstanceSimilarity(), new TableRowToCellHierarchyAdapter());
//            
//            Aggregate<TableRow, TableCell> a=  new Aggregate<TableRow, TableCell>();
//            a.setAggregationType(at);
//            a.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
//            instanceToRow = a.match(getSimilarities().getCandidateSimilarity(), inst, new TableRowToCellHierarchyAdapter());
//        }
//        
//        SimilarityMatrix<TableRow> result = instanceToRow;
//        
//        if(threshold>0) {
//            result = instanceToRow.copy();
//            result.prune(threshold);
//        }
//        
//        return evaluateInstances(result, getWebtable().getHeader(), false, null);
//    }
//    
//    private EvaluationResult aggregateKeyWeightedInstance(double threshold, CombinationType ct) {
//        
//        if(keyWeightedInstance==null) {
//            CombineHierarchy<TableRow, TableCell> cmb = new CombineHierarchy<TableRow, TableCell>();
//            cmb.setAggregationType(ct);
//            if(ct==CombinationType.WeightedSum) {
//                cmb.setFirstWeight(0.75);
//                cmb.setSecondWeight(0.25);
//            }
//            keyWeightedInstance = cmb.match(getSimilarities().getCandidateSimilarity(), getSimilarities().getInstanceSimilarity(), new TableRowToCellHierarchyAdapter());
//            
//            Aggregate<TableColumn, TableCell> agg = new Aggregate<TableColumn, TableCell>();
//            agg.setAggregationType(AggregationType.Average);
//            keyWeightedInstanceToCol = agg.match(getSimilarities().getPropertySimilarity(), keyWeightedInstance, new TableColumnToCellHierarchyAdapter());
//        }
//        
//        SimilarityMatrix<TableColumn> result = keyWeightedInstanceToCol;
//        
//        if(threshold>0) {
//             result = keyWeightedInstanceToCol.copy();
//             result.prune(threshold);
//        }
//        
//        return evaluateProperties(result, getWebtable().getHeader(), false, null);
//    }
//    
//    private EvaluationResult aggregateHeaderWeightedInstance(double threshold, CombinationType ct) {
//        
//        if(headerWeightedInstance==null) {
//            CombineHierarchy<TableColumn, TableCell> cmb = new CombineHierarchy<TableColumn, TableCell>();
//            cmb.setAggregationType(ct);
//            if(ct==CombinationType.WeightedSum) {
//                cmb.setFirstWeight(0.75);
//                cmb.setSecondWeight(0.25);
//            }
//            headerWeightedInstance = cmb.match(getSimilarities().getPropertySimilarity(), getSimilarities().getInstanceSimilarity(), new TableColumnToCellHierarchyAdapter());
//            
//            Aggregate<TableRow, TableCell> agg = new Aggregate<TableRow, TableCell>();
//            agg.setAggregationType(AggregationType.Average);
//            headerWeightedInstanceToRow = agg.match(getSimilarities().getCandidateSimilarity(), keyWeightedInstance, new TableRowToCellHierarchyAdapter());
//        }
//        
//        SimilarityMatrix<TableRow> result = headerWeightedInstanceToRow;
//        
//        if(threshold>0) {
//             result = headerWeightedInstanceToRow.copy();
//             result.prune(threshold);
//        }
//        
//        return evaluateInstances(result, getWebtable().getHeader(), false, null);
//    }
//    
//    private EvaluationResult keyAndAggregatedInstance(double threshold, AggregationType at, CombinationType ct, double w1, double w2) {
//        if(candidateInstanceToRow==null) {
//            SimilarityMatrix<TableRow> result = instanceToRow.copy();
//        
//            //result.prune(0.5);
//            
//            Combine<TableRow> comb = new Combine<TableRow>();
//            comb.setAggregationType(ct);
//            if(ct==CombinationType.WeightedSum) {
//                comb.setFirstWeight(w1);
//                comb.setSecondWeight(w2);
//            }
//            candidateInstanceToRow = comb.match(getSimilarities().getCandidateSimilarity(), result);
//        }
//        SimilarityMatrix<TableRow> sim = candidateInstanceToRow;
//        
//        if(threshold>0) {
//            sim = candidateInstanceToRow.copy();
//            sim.prune(threshold);
//        }
//        
//        return evaluateInstances(sim, getWebtable().getHeader(), false, null);
//    }
//    
//    
//    private EvaluationResult keyAndAggregatedHeaderWeightedInstance(double threshold, AggregationType at, CombinationType ct, double w1, double w2) {
//        if(candidateHeaderWeightedInstanceToRow==null) {
//            SimilarityMatrix<TableRow> result = headerWeightedInstanceToRow.copy();
//        
//            if(ct!=CombinationType.Multiply) {
//                result.prune(0.5);
//            }
//            
//            Combine<TableRow> comb = new Combine<TableRow>();
//            comb.setAggregationType(ct);
//            if(ct==CombinationType.WeightedSum) {
//                comb.setFirstWeight(w1);
//                comb.setSecondWeight(w2);
//            }
//            candidateHeaderWeightedInstanceToRow = comb.match(getSimilarities().getCandidateSimilarity(), result);
//            
//            getSimilarities().setCandidateSimilarity(candidateHeaderWeightedInstanceToRow);
//        }
//        SimilarityMatrix<TableRow> sim = candidateHeaderWeightedInstanceToRow;
//        
//        if(threshold>0) {
//            sim = candidateHeaderWeightedInstanceToRow.copy();
//            sim.prune(threshold);
//        }
//        
//        return evaluateInstances(sim, getWebtable().getHeader(), false, null);
//    }
//    
//    private EvaluationResult classKeyAndAggregatedHeaderWeightedInstance(double threshold) {
//       
//        // combine class and candidate similarities
//        CombineHierarchy<Table, TableRow> c = new CombineHierarchy<Table, TableRow>();
//        c.setAggregationType(CombinationType.Multiply);
//        c.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
//        SimilarityMatrix<TableRow> sim = c.match(getSimilarities().getClassSimilarity(), candidateHeaderWeightedInstanceToRow, new TableToRowHierarchyAdapter());
//        
//        if(threshold>0) {
//            sim = sim.copy();
//            sim.prune(threshold);
//        }
//        
//        return evaluateInstances(sim, getWebtable().getHeader(), false, null);
//    }
//    
//    /**
//     * combine instance and label scores to get property mappings
//     * @param threshold
//     * @param at
//     * @param ct
//     * @param w1
//     * @param w2
//     * @return
//     */
//    private EvaluationResult headerAndAggregatedInstance(double threshold, AggregationType at, CombinationType ct, double w1, double w2) {
//        
//        if(labelInstanceToCol==null) {
//            SimilarityMatrix<TableColumn> result = instanceToCol;
//            
//            Combine<TableColumn> comb = new Combine<TableColumn>();
//            comb.setAggregationType(ct);
//            if(ct==CombinationType.WeightedSum) {
//                comb.setFirstWeight(w1);
//                comb.setSecondWeight(w2);
//            }
//            labelInstanceToCol = comb.match(getSimilarities().getPropertySimilarity(), result);
//        }
//        
//        SimilarityMatrix<TableColumn> sim = labelInstanceToCol;
//        
//        if(threshold>0) {
//            sim = labelInstanceToCol.copy();
//            sim.prune(threshold);
//        }
//        
//        return evaluateProperties(sim, getWebtable().getHeader(), false, null);
//    }
//    
//    private EvaluationResult headerAndAggregateKeyWeightedInstance(double threshold, AggregationType at, CombinationType ct, double w1, double w2) {
//        
//        if(labelKeyWeightedInstanceToCol==null) {
//            SimilarityMatrix<TableColumn> result = keyWeightedInstanceToCol;
//            
//            if(ct!=CombinationType.Multiply) {
//                result.prune(0.5);
//            }
//            
//            Combine<TableColumn> comb = new Combine<TableColumn>();
//            comb.setAggregationType(ct);
//            if(ct==CombinationType.WeightedSum) {
//                comb.setFirstWeight(w1);
//                comb.setSecondWeight(w2);
//            }
//            labelKeyWeightedInstanceToCol = comb.match(getSimilarities().getPropertySimilarity(), result);
//            
//            
//            SimilarityMatrix<TableColumn> prop = labelKeyWeightedInstanceToCol.copy();
//            if(ct!=CombinationType.Multiply) {
//                prop.prune(0.4);
//            }
//            getSimilarities().setPropertySimilarity(prop);
//        }
//        
//        SimilarityMatrix<TableColumn> sim = labelKeyWeightedInstanceToCol;
//        
//        if(threshold>0) {
//            sim = labelKeyWeightedInstanceToCol.copy();
//            sim.prune(threshold);
//        }
//        
//        return evaluateProperties(sim, getWebtable().getHeader(), false, null);
//    }
//    
//    
//    
//    public static void main(String[] args) {
//        if (args.length < 9) {
//
//            System.out
//                    .println("Usage: <DBpedia directory> <DBpedia index> <use units> <instance goldstandard> <property goldstandard> <equivalent properties> <class goldstandard> <webtable directory> <verbose> [fastjoin path]");
//
//        } else {
//            boolean end = false;
//
//            String dbp = args[0];
//            String idx = args[1];
//            Boolean useUnits = Boolean.parseBoolean(args[2]);
//            String inst = args[3];
//            String prop = args[4];
//            String equiv = args[5];
//            String cls = args[6];
//            String web = args[7];
//            boolean verbose = Boolean.parseBoolean(args[8]);
//
//            String fastjoin = null;
//            if (args.length > 9) {
//                fastjoin = args[9];
//            }
//
//            StepwiseWebtableToDBpediaMatcher m = new StepwiseWebtableToDBpediaMatcher();
//            MatchingParameters loadDbpParams = new MatchingParameters();
//            loadDbpParams.setUseUnitDetection(useUnits);
//            m.setMatchingParameters(loadDbpParams);
//            m.loadDBpedia(dbp);
//
//            while (!end) {
//
//                MatchingParameters params = new MatchingParameters();
//                params.setUseUnitDetection(useUnits);
//                params.setFastJoinPath(fastjoin);
//                params.setCollectMatchingInfo(verbose);
//                EvaluationParameters eval = new EvaluationParameters();
//
//                if (new File(idx).exists()) {
//                    m.setLuceneIndex(new DefaultIndex(idx));
//                }
//
//                eval.setInstanceGoldStandardLocation(inst);
//                eval.setPropertyGoldStandardLocation(prop);
//                eval.setEquivalentPropertiesLocation(equiv);
//                eval.setCorrectedInstancesLocation(new File(new File(equiv).getParent(), "correctedInstances.tsv").getAbsolutePath());
//                eval.setClassHierarchyLocation(new File(new File(equiv).getParent(), "superclasses.tsv").getAbsolutePath());
//                eval.setClassGoldStandardLocation(cls);
//                
//                m.setMatchingParameters(params);
//                m.setEvaluationParameters(eval);
//
//                HashMap<String, List<ValueAggregator>> results = new LinkedHashMap<String, List<ValueAggregator>>();
//                HashMap<String, List<ValueAggregator>> resultsP = new LinkedHashMap<String, List<ValueAggregator>>();
//                HashMap<String, List<ValueAggregator>> resultsR = new LinkedHashMap<String, List<ValueAggregator>>();
//
//                for (File f : new File(web).listFiles()) {
//                    //results.add(m.matchWebTable(f.getAbsolutePath()));
//                    HashMap<String, List<EvaluationResult>> r = m.runMatchWebTable(f.getAbsolutePath());
//                    
//                    for(String s : r.keySet()) {
//                        
//                        // F-Measure
//                        List<ValueAggregator> vaLst = results.get(s);
//                        
//                        if(vaLst==null) {
//                            vaLst = new ArrayList<ValueAggregator>();
//                            results.put(s, vaLst);
//                        }
//                        
//                        Iterator<ValueAggregator> vaIt = vaLst.iterator();
//                        for(EvaluationResult e : r.get(s)) {
//                            
//                            ValueAggregator va;
//                            
//                            if(vaIt!=null && vaIt.hasNext()) {
//                                va = vaIt.next();
//                            } else {
//                                vaIt = null;
//                                va = new ValueAggregator();
//                                vaLst.add(va);
//                            }
//                            
//                            va.AddValue(e.getF1Score());
//                        }
//                        
//                        // Precision
//                        vaLst = resultsP.get(s);
//                        
//                        if(vaLst==null) {
//                            vaLst = new ArrayList<ValueAggregator>();
//                            resultsP.put(s, vaLst);
//                        }
//                        
//                        vaIt = vaLst.iterator();
//                        for(EvaluationResult e : r.get(s)) {
//                            
//                            ValueAggregator va;
//                            
//                            if(vaIt!=null && vaIt.hasNext()) {
//                                va = vaIt.next();
//                            } else {
//                                vaIt = null;
//                                va = new ValueAggregator();
//                                vaLst.add(va);
//                            }
//                            
//                            va.AddValue(e.getPrecision());
//                        }
//                        
//                        
//                        // Recall
//                        vaLst = resultsR.get(s);
//                        
//                        if(vaLst==null) {
//                            vaLst = new ArrayList<ValueAggregator>();
//                            resultsR.put(s, vaLst);
//                        }
//                        
//                        vaIt = vaLst.iterator();
//                        for(EvaluationResult e : r.get(s)) {
//                            
//                            ValueAggregator va;
//                            
//                            if(vaIt!=null && vaIt.hasNext()) {
//                                va = vaIt.next();
//                            } else {
//                                vaIt = null;
//                                va = new ValueAggregator();
//                                vaLst.add(va);
//                            }
//                            
//                            va.AddValue(e.getRecall());
//                        }
//                    }
//                }
//                
//                System.out.println("============================================");
//                System.out.println("F-Measure");
//                System.out.println("============================================");
//                for(Entry<String, List<ValueAggregator>> e : results.entrySet()) {
//                    
//                    System.out.print(String.format("%-20s:", e.getKey()));
//                    
//                    for(ValueAggregator va : e.getValue()) {
//                        
//                        System.out.print(String.format("\t%.2f", va.getAvg()));
//                    }
//                    
//                    System.out.println();
//                }
//                
//                System.out.println("============================================");
//                System.out.println("Precision");
//                System.out.println("============================================");
//                for(Entry<String, List<ValueAggregator>> e : resultsP.entrySet()) {
//                    
//                    System.out.print(String.format("%-20s:", e.getKey()));
//                    
//                    for(ValueAggregator va : e.getValue()) {
//                        
//                        System.out.print(String.format("\t%.2f", va.getAvg()));
//                    }
//                    
//                    System.out.println();
//                }
//                
//                System.out.println("============================================");
//                System.out.println("Recall");
//                System.out.println("============================================");
//                for(Entry<String, List<ValueAggregator>> e : resultsR.entrySet()) {
//                    
//                    System.out.print(String.format("%-20s:", e.getKey()));
//                    
//                    for(ValueAggregator va : e.getValue()) {
//                        
//                        System.out.print(String.format("\t%.2f", va.getAvg()));
//                    }
//                    
//                    System.out.println();
//                }
//                
//                boolean userInputOk = false;
//                do {
//
//                    System.out.println();
//                    System.out.println();
//                    System.out.println();
//                    System.out.println();
//
//                    System.out
//                            .println("run [again] or run [new] task or [quit]?");
//
//                    BufferedReader cin = new BufferedReader(
//                            new InputStreamReader(System.in));
//
//                    String cmd = "";
//                    try {
//                        cmd = cin.readLine();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//
//                    if (cmd.equalsIgnoreCase("quit")) {
//                        end = true;
//                        userInputOk = true;
//                    } else if (cmd.equalsIgnoreCase("new")) {
//                        System.out
//                                .println("Enter: <DBpedia directory> <DBpedia index> <use units> <instance goldstandard> <property goldstandard> <equivalent properties> <class goldstandard> <webtable directory> <verbose> [fastjoin path]");
//
//                        ProcessBuilder pb = new ProcessBuilder("dummy", System
//                                .console().readLine());
//
//                        List<String> values = pb.command();
//
//                        if (values.size() > 8) {
//                            dbp = values.get(1);
//                            idx = values.get(2);
//                            useUnits = Boolean.parseBoolean(values.get(3));
//                            inst = values.get(4);
//                            prop = values.get(5);
//                            equiv = values.get(6);
//                            cls = values.get(7);
//                            web = values.get(8);
//                            verbose = Boolean.parseBoolean(values.get(9));
//
//                            if (values.size() > 10) {
//                                fastjoin = values.get(10);
//                            }
//                        } else {
//                            userInputOk = false;
//                        }
//                    } else if (cmd.equalsIgnoreCase("again")) {
//                        userInputOk = true;
//                    }
//
//                } while (!userInputOk);
//            }
//        }
//    }
//}
