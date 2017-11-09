/** Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package de.dwslab.T2K.matching.dbpedia.matchers.instance;

import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.matching.blocking.IdentityBlocking;
import de.dwslab.T2K.matching.blocking.TypeBasedBlocking;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.AlwaysMatchSimilarityFunction;
import de.dwslab.T2K.matching.dbpedia.algorithm.InterTableComponent;
import de.dwslab.T2K.matching.dbpedia.algorithm.Matchers;
import de.dwslab.T2K.matching.dbpedia.algorithm.MatrixStats;
import de.dwslab.T2K.matching.dbpedia.algorithm.ValueBasedComponent;
import de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_LABEL_SIMILARITY;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_PROP_CANDIDATE_THRESHOLD;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_PROP_NUM_CANDIDATES;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_PROP_NUM_RESULTS;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_PROP_NUM_VOTES;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.PAR_PROP_VALUE_THRESHOLD;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.DuplicateBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.LabelBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.model.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingPair;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.CandidateAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.DBpediaPropertyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowMatchingStopWordAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.context.StopWordRemover;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcher;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcherWithFiltering;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.Combine;
import de.dwslab.T2K.matching.secondline.CombineNonOverlapping;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.date.WeightedDatePartSimilarity;
import de.dwslab.T2K.similarity.functions.numeric.DeviationSimilarity;
import de.dwslab.T2K.similarity.functions.set.ComplexSetSimilarity;
import de.dwslab.T2K.similarity.functions.set.GeneralisedJaccard;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.functions.string.GeneralisedStringJaccard;
import de.dwslab.T2K.similarity.functions.string.LevenshteinSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.similarity.measures.TypeBasedSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.timer.Timer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author dritze
 */
public class IndirectInstanceMatcherMapped {

    private SparseSimilarityMatrix<TableRow> overall1;

    public Map<TableRow, List<MatchingPair>> computeIndirectMappings(MatchingParameters matchingParameters, Timer rootTimer,
            MatchingData data, Configuration config, MatchingResult result, GoldStandard gs, Matchers matchers, MatchingLogger log, EvaluationParameters evalParams, Map<Table, SimilarityMatrix<TableRow>> mapped, InterTableComponent itc, List<TableRow> mappedNotIndirect) {

        Map<Table, MatchingData> matchingData = itc.getMatchingDataPerTable();
        Map<Table, List<SimilarityMatrix<TableRow>>> dbpediaPropsPerTable = new HashMap<>();
        

        List<TableRow> unmappedRows = new ArrayList<>();

        Map<TableRow, List<MatchingPair>> generatedPairs = new HashMap<>();

        for (TableRow tr : mappedNotIndirect) {
            unmappedRows.add(tr);
            List<MatchingPair> l = new ArrayList<>();
            generatedPairs.put(tr, l);
        }

        Map<TableRow, Map<String, Integer>> possibleMappings = new HashMap<>();

        //try out with all columns
        for (TableRow unmappedCol : unmappedRows) {
            Map<String, Integer> countsPerCol = new HashMap<>();
            possibleMappings.put(unmappedCol, countsPerCol);
        }

        Map<TableRow, Map<String, Integer>> possibleMappingsCosine = new HashMap<>();
        for (TableRow unmappedCol : unmappedRows) {
            Map<String, Integer> countsPerCol = new HashMap<>();
            possibleMappingsCosine.put(unmappedCol, countsPerCol);
        }

        Map<TableRow, Map<String, Integer>> possibleMappingsHeader = new HashMap<>();
        for (TableRow unmappedCol : unmappedRows) {
            Map<String, Integer> countsPerCol = new HashMap<>();
            possibleMappingsHeader.put(unmappedCol, countsPerCol);
        }

        int overlappingProps = 0;

        for (Table otherTab : mapped.keySet()) {
            MatchingData dt = matchingData.get(otherTab);
            List<TableRow> possilbeRows = new ArrayList<>();

            //TODO: all are possible? otherwise the ones that will fit are excluded?            
            //also only take rows that have a correspondence?!
            for (TableRow r2 : dt.getWebtableRowSet()) {
                for (Integer i : otherTab.getMapping().getMappedInstances().keySet()) {
                    if (r2.getRowIndex() == i) {
                        possilbeRows.add(r2);
                    }
                }
            }

            //can we somehow only use the fist label if the label is a list?
            LabelBasedMatcherWithFiltering<TableRow> labelMatching = new LabelBasedMatcherWithFiltering<>();
            labelMatching.setBlocking(new IdentityBlocking<TableRow>());

            // calculate scores based on column/label string similarity
            StringSimilarityMeasure<TableRow> colMeasure = new StringSimilarityMeasure<>();
            colMeasure.setSimilarityFunction((SimilarityFunction<String>) config.getValue(PAR_LABEL_SIMILARITY));
            colMeasure.setSetSimilarity(new MaxSimilarity());
            labelMatching.setSimilarityMeasure(colMeasure);

            labelMatching.setCollectMatchingInfo(matchingParameters.isCollectMatchingInfo());
            labelMatching.setRunInParallel(true);

            TableRowMatchingStopWordAdapter tsa = new TableRowMatchingStopWordAdapter();
            SimilarityMatrix<TableRow> candidateMatrix = labelMatching.match(unmappedRows, possilbeRows, tsa);
            candidateMatrix.pruneWithNullEqualOrBelow(0.5);
            // System.out.println(candidateMatrix.getFirstDimension() + " vs. " + candidateMatrix.getSecondDimension());

            SimilarityMatrix<TableRow> valueMatrix = new SparseSimilarityMatrix<>(unmappedRows.size(), otherTab.getKey().getValues().size());

            for (TableRow t1 : candidateMatrix.getFirstDimension()) {
                for (TableRow t2 : candidateMatrix.getMatches(t1)) {
                    // System.out.println("matches row matched! " + t1 + " - " + t2);

                    Double simValue = 0.0;
                    int counter = 0;

//                    List<Correspondence> allPropMappings = new ArrayList<>();
//                    for (Correspondence<TableColumn> tc : result.getPropertyMappings()) {  
//                        List<String> equivURIs = gs.getPropertyCanoniser().backwardsCanoniseResource(tc.getSecond().getURI());
//                        for(String s : equivURIs) {
//                            for(TableColumn tx : data.getDbpediaColSet()) {
//                                if(s.equals(tx.getURI())) {
//                                    Correspondence c = new Correspondence(tc.getFirst(), tx, 0.5);
//                                    allPropMappings.add(c);
//                                }
//                            }
//                        }
//                    }
                    // for (Correspondence<TableColumn> tc : allPropMappings) { 
                    for (Correspondence<TableColumn> tc : result.getPropertyMappings()) {
                        String mappedUri = tc.getSecond().getURI();
                        for (Integer columnIndex : t2.getTable().getMapping().getMappedProperties().keySet()) {
                            String otherMappedUri = t2.getTable().getMapping().getMappedProperties().get(columnIndex).getFirst();
                            if (mappedUri.equals(otherMappedUri)) {
                                overlappingProps++;
                                counter++;
                                Object first = tc.getFirst().getValues().get(t1.getRowIndex());
                                Object second = t2.getTable().getColumns().get(columnIndex).getValues().get(t2.getRowIndex());
                                if (tc.getFirst().getDataType() != t2.getTable().getColumns().get(columnIndex).getDataType()) {
                                    continue;
                                }
                                if (first == null || second == null) {
                                    continue;
                                }
                                Double currentValue = null;
                                boolean list1 = false;
                                boolean list2 = false;
                                if (first instanceof List) {
                                    list1 = true;
                                }
                                if (second instanceof List) {
                                    list2 = true;
                                }
                                if (tc.getFirst().getDataType() == TableColumn.ColumnDataType.string) {
                                    GeneralisedStringJaccard gsj = new GeneralisedStringJaccard(new LevenshteinSimilarity(), 0.5, 0.5);
                                    if (!list1 && !list2) {
                                        currentValue = gsj.calculate(first.toString(), second.toString());
                                    } else {
                                        ComplexSetSimilarity<String> css = new MaxSimilarity<>();
                                        if (list1 && list2) {
                                            currentValue = css.calculate((List) first, (List) second, gsj);
                                        } else if (list1) {
                                            List<String> l2 = new ArrayList<>();
                                            l2.add(second.toString());
                                            currentValue = css.calculate((List) first, l2, gsj);
                                        } else if (list2) {
                                            List<String> l2 = new ArrayList<>();
                                            l2.add(first.toString());
                                            currentValue = css.calculate(l2, (List) second, gsj);
                                        }
                                    }
                                }
                                if (tc.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
                                    DeviationSimilarity ds = new DeviationSimilarity();
                                    if (!list1 && !list2) {
                                        Double d1 = (Double) first;
                                        Double d2 = (Double) second;
                                        currentValue = ds.calculate(d1, d2);
                                    } else {
                                        ComplexSetSimilarity<Double> css = new MaxSimilarity<>();
                                        if (list1 && list2) {
                                            currentValue = css.calculate((List) first, (List) second, ds);
                                        } else if (list1) {
                                            List<Double> l2 = new ArrayList<>();
                                            l2.add((Double) second);
                                            currentValue = css.calculate((List) first, l2, ds);
                                        } else if (list2) {
                                            List<Double> l2 = new ArrayList<>();
                                            l2.add((Double) first);
                                            currentValue = css.calculate(l2, (List) second, ds);
                                        }
                                    }
                                }
                                if (tc.getFirst().getDataType() == TableColumn.ColumnDataType.date) {
                                    WeightedDatePartSimilarity wds = new WeightedDatePartSimilarity(1, 3, 5);
                                    ValueRange vr = new ValueRange(tc.getFirst().getColumnStatistic().getMinimalValue(), tc.getFirst().getColumnStatistic().getMaximalValue());
                                    wds.setValueRange(vr);
                                    if (!list1 && !list2) {
                                        currentValue = wds.calculate((Date) first, (Date) second);
                                    } else {
                                        ComplexSetSimilarity<Date> css = new MaxSimilarity<>();
                                        if (list1 && list2) {
                                            currentValue = css.calculate((List) first, (List) second, wds);
                                        } else if (list1) {
                                            List<Date> l2 = new ArrayList<>();
                                            l2.add((Date) second);
                                            currentValue = css.calculate((List) first, l2, wds);
                                        } else if (list2) {
                                            List<Date> l2 = new ArrayList<>();
                                            l2.add((Date) first);
                                            currentValue = css.calculate(l2, (List) second, wds);
                                        }
                                    }
                                }

                                if (currentValue != null && currentValue > 0.5) {
                                    simValue += currentValue;
                                    //System.out.println("set value indirect row matched: " + t1 + " - " + t2 + " - " + tc.getFirst() + " - " + simValue);
                                    valueMatrix.set(t1, t2, simValue);
                                }
                            }
                        }
                    }
                    if (counter > 1 && simValue > 0) {
                        valueMatrix.set(t1, t2, simValue / counter);
                    }
                }
            }

            SimilarityMatrix<TableRow> valueBasedDBpedia = new SparseSimilarityMatrix<>(valueMatrix.getFirstDimension().size(),
                    valueMatrix.getSecondDimension().size());

            SimilarityMatrix<TableRow> labelBasedDBpedia = new SparseSimilarityMatrix<>(candidateMatrix.getFirstDimension().size(),
                    candidateMatrix.getSecondDimension().size());

            for (TableRow tr1 : valueMatrix.getFirstDimension()) {
                for (TableRow t2 : valueMatrix.getMatches(tr1)) {
                    Integer index1 = t2.getRowIndex();
                    if (t2.getTable().getMapping().getMappedInstances().get(index1) != null) {

                        String mappedUri = t2.getTable().getMapping().getMappedInstances().get(index1).getFirst();
                        //System.out.println("why? " + t2.getTable() + " - " + tr1 + " - " + t2 + " - " + mappedUri);

                        for (Pair p : t2.getTable().getMapping().getMappedInstances().values()) {
                            String key;
                            if (t2.getKey() instanceof List) {
                                key = ((List) t2.getKey()).get(0).toString().toLowerCase();
                            } else {
                                key = t2.getKey().toString().toLowerCase();
                            }
                            if (p.getFirst().toString().toLowerCase().contains(key)) {
                                mappedUri = p.getFirst().toString();
                            }
                        }

                        for (TableRow dbpedia : data.getUriMap().get(mappedUri)) {
                            if (valueMatrix.get(tr1, t2) != null) {
                                valueBasedDBpedia.set(tr1, dbpedia, valueMatrix.get(tr1, t2));
                                // System.out.println("set value row matched: " + tr1 + " - " + dbpedia.getURI() + valueMatrix.get(tr1, t2));
                            }
                        }
                    }
                }
            }

            for (TableRow tr1 : candidateMatrix.getFirstDimension()) {
                for (TableRow t2 : candidateMatrix.getMatches(tr1)) {
                    Integer index1 = t2.getRowIndex();
                    if (t2.getTable().getMapping().getMappedInstances().get(index1) != null) {

                        String mappedUri = t2.getTable().getMapping().getMappedInstances().get(index1).getFirst();

                        for (Pair p : t2.getTable().getMapping().getMappedInstances().values()) {
                            String key;
                            if (t2.getKey() instanceof List) {
                                key = ((List) t2.getKey()).get(0).toString().toLowerCase();
                            } else {
                                key = t2.getKey().toString().toLowerCase();
                            }
                            if (p.getFirst().toString().toLowerCase().contains(key)) {
                                mappedUri = p.getFirst().toString();
                            }
                        }

                        for (TableRow dbpedia : data.getUriMap().get(mappedUri)) {
                            if (candidateMatrix.get(tr1, t2) != null) {
                                labelBasedDBpedia.set(tr1, dbpedia, candidateMatrix.get(tr1, t2));
                                // System.out.println("set label row matched: " + tr1 + " - " + dbpedia.getURI() + candidateMatrix.get(tr1, t2));
                            }
                        }
                    }
                }
            }

            if (candidateMatrix.getFirstDimension().size() > 0 || valueMatrix.getFirstDimension().size() > 0) {
                candidateMatrix.pruneWithNullEqualOrBelow(0.0);
                valueMatrix.pruneWithNullEqualOrBelow(0.0);
                //System.out.println("XXX added matrices");
                List<SimilarityMatrix<TableRow>> list = new ArrayList<>();
                list.add(labelBasedDBpedia);
                list.add(valueBasedDBpedia);
                dbpediaPropsPerTable.put(otherTab, list);
            }
        }

        SimilarityMatrix<TableRow> overallLabel = new SparseSimilarityMatrix<>(unmappedRows.size(), data.getDbpediaRowSet().size());
        SimilarityMatrix<TableRow> overallValue = new SparseSimilarityMatrix<>(unmappedRows.size(), data.getDbpediaRowSet().size());
        Map<TableRow, Integer> labelColumnCount = new HashMap<>();
        Map<TableRow, Integer> valueColumnCount = new HashMap<>();
        Map<String, Integer> labelColumnPairCount = new HashMap<>();
        Map<String, Integer> valueColumnPairCount = new HashMap<>();

        for (Table t : dbpediaPropsPerTable.keySet()) {
            SimilarityMatrix<TableRow> label = dbpediaPropsPerTable.get(t).get(0);
            SimilarityMatrix<TableRow> value = dbpediaPropsPerTable.get(t).get(1);

            for (TableRow r1 : label.getFirstDimension()) {

                if (labelColumnCount.containsKey(r1)) {
                    Integer i = labelColumnCount.get(r1);
                    i++;
                    labelColumnCount.put(r1, i);
                } else {
                    labelColumnCount.put(r1, 1);
                }
                for (TableRow r2 : label.getMatches(r1)) {

                    if (labelColumnPairCount.containsKey(r1 + "\t" + r2)) {
                        Integer i = labelColumnPairCount.get(r1 + "\t" + r2);
                        i++;
                        labelColumnPairCount.put(r1 + "\t" + r2, i);
                    } else {
                        labelColumnPairCount.put(r1 + "\t" + r2, 1);
                    }

                    if (overallLabel.get(r1, r2) != null) {
                        Double score = overallLabel.get(r1, r2);
                        score += label.get(r1, r2);
                        overallLabel.set(r1, r2, score);
                    } else {
                        overallLabel.set(r1, r2, label.get(r1, r2));
                    }
                }
            }

            for (TableRow r1 : value.getFirstDimension()) {
                if (valueColumnCount.containsKey(r1)) {
                    Integer i = valueColumnCount.get(r1);
                    i++;
                    valueColumnCount.put(r1, i);
                } else {
                    valueColumnCount.put(r1, 1);
                }
                for (TableRow r2 : value.getMatches(r1)) {
                    if (valueColumnPairCount.containsKey(r1 + "\t" + r2)) {
                        Integer i = valueColumnPairCount.get(r1 + "\t" + r2);
                        i++;
                        valueColumnPairCount.put(r1 + "\t" + r2, i);
                    } else {
                        valueColumnPairCount.put(r1 + "\t" + r2, 1);
                    }

                    if (overallValue.get(r1, r2) != null) {
                        Double score = overallValue.get(r1, r2);
                        score += value.get(r1, r2);
                        overallValue.set(r1, r2, score);
                    } else {
                        overallValue.set(r1, r2, value.get(r1, r2));
                    }
                }
            }
        }

//        for (TableRow r1 : overallLabel.getFirstDimension()) {
//            for (TableRow r2 : overallLabel.getMatches(r1)) {
//                if (labelColumnCount.get(r1) != null) {
//                    int occurence = labelColumnCount.get(r1);
//                    System.out.println("label row matched" + r1 + " - " + r2.getURI()  + " - " +overallLabel.get(r1, r2) + " - " +occurence + " - " +overallLabel.get(r1, r2) / occurence);
//                    overallLabel.set(r1, r2, overallLabel.get(r1, r2) / occurence);
//                }
//            }
//        }
//
//        for (TableRow r1 : overallValue.getFirstDimension()) {
//            for (TableRow r2 : overallValue.getMatches(r1)) {
//                if (valueColumnCount.get(r1) != null) {
//                    int occurence = valueColumnCount.get(r1);
//                    System.out.println("value row matched" + r1 + " - " + r2.getURI() + " - " +overallValue.get(r1, r2) + " - " +occurence+ " - " +overallValue.get(r1, r2) / occurence);
//                    overallValue.set(r1, r2, overallValue.get(r1, r2) / occurence);
//                }
//            }
//        }
        overallLabel.pruneWithNullEqualOrBelow(0.0);
        overallValue.pruneWithNullEqualOrBelow(0.0);

        for (TableRow r1 : overallLabel.getFirstDimension()) {
            for (TableRow r2 : overallLabel.getMatches(r1)) {
                if (labelColumnCount.get(r1) != null) {
                    int occurence = labelColumnCount.get(r1);
                    //System.out.println("label mapped row matched" + r1 + " - " + r2 + " - " + overallLabel.get(r1, r2) + " - " + occurence + " - " + overallLabel.get(r1, r2) / occurence);
                    overallLabel.set(r1, r2, overallLabel.get(r1, r2) / occurence);

                    MatchingPair mp = new MatchingPair();
                    mp.setMatchingPair(new Pair(r1, r2));
                    mp.setLabelScore(overallLabel.get(r1, r2));
                    mp.setOccurenceFirstLabel(occurence);
                    mp.setOtherPairsLabel(overallLabel.getMatches(r1).size());
                    mp.setOverallNumbTables(dbpediaPropsPerTable.size());
                    mp.setOccurencePairLabel(labelColumnPairCount.get(r1 + "\t" + r2));
                    //System.out.println("gen pairs label " + r1 + " - " + mp.getMatchingPair().getSecond());
                    generatedPairs.get(r1).add(mp);
                }
            }
        }

        for (TableRow r1 : overallValue.getFirstDimension()) {
            for (TableRow r2 : overallValue.getMatches(r1)) {
                if (valueColumnCount.get(r1) != null) {
                    int occurence = valueColumnCount.get(r1);
                    //System.out.println("value mapped row matched" + r1 + " - " + r2 + " - " + overallValue.get(r1, r2) + " - " + occurence + " - " + overallValue.get(r1, r2) / occurence);
                    overallValue.set(r1, r2, overallValue.get(r1, r2) / occurence);

                    if (generatedPairs.containsKey(r1)) {
                        boolean alreadyExisting = false;
                        for (MatchingPair p : generatedPairs.get(r1)) {
                            if (p.getMatchingPair().getSecond().equals(r2)) {
                                p.setValueScore(overallValue.get(r1, r2));
                                p.setOccurenceFirstValue(occurence);
                                p.setOtherPairsValue(overallValue.getMatches(r1).size());
                                p.setOccurencePairValue(valueColumnPairCount.get(r1 + "\t" + r2));
                                alreadyExisting = true;
                            }
                        }
                        if (!alreadyExisting) {
                            MatchingPair mp = new MatchingPair();
                            mp.setMatchingPair(new Pair(r1, r2));
                            mp.setValueScore(overallValue.get(r1, r2));
                            mp.setOccurenceFirstValue(occurence);
                            mp.setOtherPairsValue(overallValue.getMatches(r1).size());
                            mp.setOverallNumbTables(dbpediaPropsPerTable.size());
                            mp.setOccurencePairValue(valueColumnPairCount.get(r1 + "\t" + r2));
                            //System.out.println("gen pairs value1 " + r1 + " - " + mp.getMatchingPair().getSecond());
                            generatedPairs.get(r1).add(mp);
                        }
                    } else {
                        MatchingPair mp = new MatchingPair();
                        mp.setMatchingPair(new Pair(r1, r2));
                        mp.setValueScore(overallValue.get(r1, r2));
                        mp.setOccurenceFirstValue(occurence);
                        mp.setOtherPairsValue(overallValue.getMatches(r1).size());
                        mp.setOverallNumbTables(dbpediaPropsPerTable.size());
                        mp.setOccurencePairValue(valueColumnPairCount.get(r1 + "\t" + r2));
                        //System.out.println("gen pairs value2 " + r1 + " - " + mp.getMatchingPair().getSecond());
                        generatedPairs.get(r1).add(mp);
                    }
                }
            }
        }

        Map<SimilarityMatrix, MatrixStats> stats = new HashMap<>();
        Map<TableRow, List<TableRow>> possibleMatchesByRow = new HashMap<>();
        for (TableRow r : overallLabel.getFirstDimension()) {
            List<TableRow> l = new ArrayList<>();
            l.addAll(overallLabel.getMatches(r));
            possibleMatchesByRow.put(r, l);
        }
        for (TableRow r : overallValue.getFirstDimension()) {
            List<TableRow> l = new ArrayList<>();
            l.addAll(overallValue.getMatches(r));
            possibleMatchesByRow.put(r, l);
        }

        stats.put(overallValue, new MatrixStats(overallValue, data, possibleMatchesByRow));
        stats.put(overallLabel, new MatrixStats(overallLabel, data, possibleMatchesByRow));
        //combine the matrices

        CombineNonOverlapping<TableRow> combine = new CombineNonOverlapping<>();
        //combine.setFirstWeight(stats.get(getOverallValue()).getHerfindahlIndex());

        SimilarityMatrix y2 = overallValue.copy();
        //TODO: normalizedHerf
        y2.multiplyScalar(stats.get(overallValue).getNormalizedHerinfahl());
        SimilarityMatrix x2 = overallLabel.copy();
        x2.multiplyScalar(stats.get(overallLabel).getNormalizedHerinfahl());
//        
//        combine.setFirstWeight(stats.get(overallValue).getHerfindahlIndex());
//        combine.setSecondWeight(stats.get(overallLabel).getHerfindahlIndex());
        combine.setAggregationType(CombinationType.Sum);
        overall1 = (SparseSimilarityMatrix)combine.match(y2, x2);

        //better way instead of normalizing?
        //overall1.normalize();
        for (TableRow tx : getOverall1().getFirstDimension()) {
            for (MatchingPair<TableRow> mp : generatedPairs.get(tx)) {
                //System.out.println("gen pairs final " + mp.getMatchingPair().getFirst() + " - " + mp.getMatchingPair().getSecond());
                mp.setFinalScore(getOverall1().get(mp.getMatchingPair().getFirst(), mp.getMatchingPair().getSecond()));
            }
        }

        for (TableRow tc : generatedPairs.keySet()) {
            Map<String, List<MatchingPair>> uniqueIdenToPairs = new HashMap<>();
            List<MatchingPair> toRemove = new ArrayList<>();
            for (MatchingPair<TableRow> mp : generatedPairs.get(tc)) {
                String uniqueString = mp.getMatchingPair().getSecond().getURI().toString();
                if (uniqueIdenToPairs.containsKey(uniqueString)) {
                    uniqueIdenToPairs.get(uniqueString).add(mp);
                } else {
                    List<MatchingPair> l = new ArrayList<>();
                    l.add(mp);
                    uniqueIdenToPairs.put(uniqueString, l);
                }
            }

            for (String unique : uniqueIdenToPairs.keySet()) {
                MatchingPair best = null;
                double maxScore = -1.0;
                for (MatchingPair mp : uniqueIdenToPairs.get(unique)) {
                    if (mp.getFinalScore() > maxScore) {
                        maxScore = mp.getFinalScore();
                        best = mp;
                    }
                }
                for (MatchingPair mp : uniqueIdenToPairs.get(unique)) {
                    if (!mp.equals(best)) {
                        toRemove.add(mp);
                    }
                }
            }
            for (MatchingPair mp : toRemove) {
                generatedPairs.get(tc).remove(mp);
            }
        }

//        SparseSimilarityMatrix<TableRow> overall2 = new SparseSimilarityMatrix<>(overall.getFirstDimension().size(),
//        overall.getSecondDimension().size());
//
//        for (TableRow r1 : overall.getFirstDimension()) {
//            TableRow best = overall.getBestPair(r1);            
//            overall2.set(r1, best, overall.get(r1, best));
//            boolean inGS = false;
//            for (Object o : gs.getInstanceGoldStandard().keySet()) {
//                Integer i = Integer.parseInt(o.toString());
//                //double diffToAvg = countR2.get(r1.toString() + "\t" + best.toString()) - averageCounts;
//                if (i == r1.getRowIndex()) {
//                    inGS = true;
//                    if (best.getURI().equals(gs.getInstanceGoldStandard().get(o))) {                        
//                        System.out.println("final overall DBpedia: " + r1.getTable().getHeader() + "\t" + r1 + "\t" + best.getURI() + "\t" + overall.get(r1, best) + "\t" +
//                                overallValue.get(r1, best) + "\t" + overallLabel.get(r1, best) + "\t" + labelColumnCount.get(r1) + "\t" + valueColumnCount.get(r1)
//                                + "\t" + dbpediaPropsPerTable.size() + "\t" + overlappingProps + "\t"+ result.getPropertyMappings().size() + "\tCORRECT");
//                    } else {
//                        System.out.println("final overall DBpedia: " + r1.getTable().getHeader() + "\t" + r1 + "\t" + best.getURI() + "\t" + overall.get(r1, best) + "\t" +
//                                overallValue.get(r1, best) + "\t" + overallLabel.get(r1, best) + "\t" + labelColumnCount.get(r1) + "\t" + valueColumnCount.get(r1)
//                                + "\t" + dbpediaPropsPerTable.size() + "\t" + overlappingProps + "\t"+ result.getPropertyMappings().size()+ "\tINCORRECT");
//                    }
//                }
//            }
//            if (!inGS) {
//                System.out.println("final overall DBpedia: " + r1.getTable().getHeader() + "\t" + r1 + "\t" + best.getURI() + "\t" + overall.get(r1, best) + "\t" +
//                                overallValue.get(r1, best) + "\t" + overallLabel.get(r1, best) + "\t" + labelColumnCount.get(r1) + "\t" + valueColumnCount.get(r1)
//                                + "\t" + dbpediaPropsPerTable.size() + "\t" + overlappingProps + "\t"+ result.getPropertyMappings().size()+ "\tINCORRECT2");
//            }
//        }    
        //usually possibleMappings!
        for (TableRow s : possibleMappings.keySet()) {
            boolean foundCorrect = false, valueSim = false;
            if (!possibleMappings.get(s).isEmpty()) {
                for (Object o : gs.getInstanceGoldStandard().keySet()) {
                    Integer i = Integer.parseInt(o.toString());
                    if (i == s.getRowIndex()) {
                        for (String possible : possibleMappings.get(s).keySet()) {
                            if (possible.equals(gs.getInstanceGoldStandard().get(o))) {
                                //                        System.out.println("possible instance match index: " + s.getTable() + "\t" + s + "\t" + possibleMappings.get(s) + "\tCORRECT");
                                foundCorrect = true;
                            }
                        }
                    }
                }
                if (!foundCorrect) {
                    //           System.out.println("possible instance match index: " + s.getTable() + "\t" + s + "\t" + possibleMappings.get(s) + "\tFALSE");
                }
            }
        }

        for (TableRow s : possibleMappingsHeader.keySet()) {
            boolean foundCorrect = false, valueSim = false;
            if (!possibleMappingsHeader.get(s).isEmpty()) {
                for (Object o : gs.getInstanceGoldStandard().keySet()) {
                    Integer i = Integer.parseInt(o.toString());
                    if (i == s.getRowIndex()) {
                        for (String possible : possibleMappingsHeader.get(s).keySet()) {
                            if (possible.equals(gs.getInstanceGoldStandard().get(o))) {
                                //                         System.out.println("possible instance match index value: " + s.getTable() + "\t" + s + "\t" + possibleMappingsHeader.get(s) + "\tCORRECT");
                                foundCorrect = true;
                            }
                        }
                    }
                }
                if (!foundCorrect) {
                    //              System.out.println("possible instance match index value: " + s.getTable() + "\t" + s + "\t" + possibleMappingsHeader.get(s) + "\tFALSE");
                }
            }
        }

        return generatedPairs;
    }

    /**
     * @return the overall1
     */
    public SparseSimilarityMatrix<TableRow> getOverall1() {
        return overall1;
    }

    /**
     * @param overall1 the overall1 to set
     */
    public void setOverall1(SparseSimilarityMatrix<TableRow> overall1) {
        this.overall1 = overall1;
    }

}
