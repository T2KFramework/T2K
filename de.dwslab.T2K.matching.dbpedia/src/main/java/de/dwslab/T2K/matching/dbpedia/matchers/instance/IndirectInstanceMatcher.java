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
import de.dwslab.T2K.matching.blocking.SimpleBlokcing;
import de.dwslab.T2K.matching.blocking.TypeBasedBlocking;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.AlwaysMatchSimilarityFunction;
import de.dwslab.T2K.matching.dbpedia.algorithm.InterTableComponent;
import de.dwslab.T2K.matching.dbpedia.algorithm.InterTableComponentForInst;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author dritze
 */
public class IndirectInstanceMatcher {

    private SimilarityMatrix<TableRow> overallLabel = new SparseSimilarityMatrix<>(0, 0);
    private SimilarityMatrix<TableRow> overallValue = new SparseSimilarityMatrix<>(0, 0);
    private Set<TableColumn> overlappingProps;

    public SimilarityMatrix<TableRow> computeIndirectMappings(MatchingParameters matchingParameters, Timer rootTimer,
            MatchingData data, Configuration config, MatchingResult result, GoldStandard gs, Matchers matchers, MatchingLogger log, EvaluationParameters evalParams, Map<Table, SimilarityMatrix<TableRow>> mapped, InterTableComponent itc) {

        Map<Table, MatchingData> matchingData = itc.getMatchingDataPerTable();
        Map<Table, List<SimilarityMatrix<TableRow>>> dbpediaPropsPerTable = new HashMap<>();

        List<TableRow> unmappedRows = new ArrayList<>();

        //check in test table!
        for (TableRow tr : data.getWebtableRowSet()) {
            boolean noCorFound = true;
            for (Correspondence<TableRow> c : result.getInstanceMappings()) {
                if (tr.equals(c.getFirst())) {
                    //System.out.println("domi mapped row: " + tr + " to " + c.getSecond());
                    noCorFound = false;
                }
            }
            if (noCorFound) {
                //System.out.println("domi mapped row not: " + tr);
                unmappedRows.add(tr);
            }
        }
        // System.out.println("size unmapped:  " + data.getWebtable().getHeader() + " - " + unmappedRows.size() + " - " + result.getInstanceMappings().size()
        //         + " - " + data.getWebtable().getKey().getValues().size());
        //Collection<TableRow> unmappedRows = data.getWebtableRowSet();

        Map<TableRow, Map<String, Integer>> possibleMappings = new HashMap<>();
        Map<TableRow, List<String>> mappingsToTables = new HashMap<>();

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

        overlappingProps = new HashSet<>();

        Map<String, List<String>> possibleLables = new TreeMap<>();
        Map<String, List<String>> possibleLablesAll = new HashMap<>();

        Map<String, List<String>> labels = itc.getLabelsPerURI();

        for (Table otherTab : mapped.keySet()) {
            MatchingData dt = matchingData.get(otherTab);
            Collection<TableRow> possilbeRows = new ArrayList<>();
            for (TableRow tr : dt.getWebtableRowSet()) {
                //also exclude rows that are already mapped to other rows in our table via DBpedia
                if (otherTab.getMapping().getMappedInstances().containsKey(tr.getRowIndex())) {
                    possilbeRows.add(tr);
                }
            }

            if (otherTab.getHeader().contains("1350433107026_1350497272807_318.arc4727466956293543108#6899771_4_6843721927200132921.csv")) {
                System.out.println("table got from map");
                System.out.println("table got from map key: " + otherTab.getKeyIndex());
                for (TableRow r : possilbeRows) {
                    System.out.println("table from map rows: " + r);
                }
            }

            //not sure? try with all rows!
//            List<TableRow> possilbeRows = new ArrayList<>();
//            Collection<TableRow> overlappingRows = mapped.get(otherTab).getSecondDimension();
//            for (TableRow r2 : dt.getWebtableRowSet()) {
//                if (!overlappingRows.contains(r2) && otherTab.getMapping().getMappedInstances().keySet().contains(r2.getRowIndex())) {
//                    possilbeRows.add(r2);
//                }
//            }
            LabelBasedMatcherWithFiltering<TableRow> labelMatching = new LabelBasedMatcherWithFiltering<>();
            labelMatching.setBlocking(new IdentityBlocking<TableRow>());
//            SimpleBlokcing sb = new SimpleBlokcing();
//            sb.setAdapter(new TableRowMatchingStopWordAdapter());
//            labelMatching.setBlocking(sb);

            // calculate scores based on column/label string similarity
            StringSimilarityMeasure<TableRow> colMeasure = new StringSimilarityMeasure<>();
            colMeasure.setSimilarityFunction((SimilarityFunction<String>) config.getValue(PAR_LABEL_SIMILARITY));
            //try without set!
            //colMeasure.setSetSimilarity(new MaxSimilarity());
            labelMatching.setSimilarityMeasure(colMeasure);

            labelMatching.setCollectMatchingInfo(matchingParameters.isCollectMatchingInfo());
            labelMatching.setRunInParallel(true);

            TableRowMatchingStopWordAdapter tsa = new TableRowMatchingStopWordAdapter();
            tsa.setCount(itc.getLabelsCount());

            SimilarityMatrix<TableRow> candidateMatrix = labelMatching.match(unmappedRows, possilbeRows, tsa);
            candidateMatrix.pruneWithNullEqualOrBelow(0.5);
 //           System.out.println(candidateMatrix.getFirstDimension() + " vs. " + candidateMatrix.getSecondDimension());

            SimilarityMatrix<TableRow> valueMatrix = new SparseSimilarityMatrix<>(unmappedRows.size(), otherTab.getKey().getValues().size());

            for (TableRow r : possilbeRows) {
                for (TableRow s : unmappedRows) {
                    if (tsa.getLabel(s).toString().contains(tsa.getLabel(r).toString())) {
                        if (candidateMatrix.get(s, r) == null && !tsa.getLabel(r).toString().isEmpty() && !tsa.getLabel(r).toString().equals(" ") &&
                                tsa.getLabel(r).toString().length()>4) {
                            //try with less like 0.5
                            candidateMatrix.set(s, r, 0.75);
                        }
                    }
//                    if(s.getKey() instanceof List) {
//                        List l = (List<Object>) s.getKey();
//                        if(l.get(0).toString().contains(r.getKey().toString())) {
//                            candidateMatrix.set(s,r,0.5);
//                        }
//                    }
//                    else {
//                        if(s.getKey().toString().contains(r.getKey().toString())) {
//                            candidateMatrix.set(s,r,0.5);
//                        }
//                    }
                }
                for (int i : otherTab.getMapping().getMappedInstances().keySet()) {
                    if (i == r.getRowIndex()) {
                        String key = otherTab.getMapping().getMappedInstances().get(i).getFirst();
                        if (possibleLablesAll.containsKey(key)) {
                            List<String> current = possibleLablesAll.get(key);
                            current.add(r.getKey().toString());
                            possibleLablesAll.put(key, current);
                        } else {
                            List<String> l = new ArrayList<>();
                            l.add(r.getKey().toString());
                            possibleLablesAll.put(key, l);
                        }
                    }
                }
            }

            for (TableRow t1 : candidateMatrix.getFirstDimension()) {
                for (TableRow t2 : candidateMatrix.getMatches(t1)) {
                    // System.out.println("matches! " + t1 + " - " + t2);
                    if (otherTab.getHeader().contains("1350433107026_1350497272807_318.arc4727466956293543108#6899771_4_6843721927200132921.csv")) {
                        System.out.println("table matches");
                    }
                    if (t1.getRowIndex() == 236) {
                        System.out.println("test " + t1 + "\t" + t2 + "\t" + t2.getTable().getMapping().getMappedInstances().get(t2.getRowIndex()).getFirst()
                                + "\t" + t2.getTable().getHeader());
                    }

                    //for (TableRow r : possilbeRows) {
                    int index = t2.getRowIndex();
                    for (int i : otherTab.getMapping().getMappedInstances().keySet()) {
                        if (i == index) {
                            String key = t1.getRowIndex() + "\t" + t1.getKey() + "\t" + otherTab.getMapping().getMappedInstances().get(i).getFirst();
                            if (possibleLables.containsKey(key)) {
                                List<String> current = possibleLables.get(key);
                                current.add(t2.getKey().toString());
                                possibleLables.put(key, current);
                            } else {
                                List<String> l = new ArrayList<>();
                                l.add(t2.getKey().toString());
                                possibleLables.put(key, l);
                            }
                        }
                    }
                    //}

                    Double simValue = 0.0;
                    int counter = 0;
                    for (Correspondence<TableColumn> tc : result.getPropertyMappings()) {
                        String mappedUri = tc.getSecond().getURI();
                        for (Integer columnIndex : t2.getTable().getMapping().getMappedProperties().keySet()) {
                            String otherMappedUri = t2.getTable().getMapping().getMappedProperties().get(columnIndex).getFirst();
                            if (mappedUri.equals(otherMappedUri)) {
                                overlappingProps.add(tc.getSecond());
                                counter++;
                                Object first = tc.getFirst().getValues().get(t1.getRowIndex());
                                Object second = t2.getTable().getColumns().get(columnIndex).getValues().get(t2.getRowIndex());                                
                                if (tc.getFirst().getDataType() != t2.getTable().getColumns().get(columnIndex).getDataType()) {
                                    continue;
                                }
                                if (first == null || second == null) {
                                    continue;
                                }
                                // System.out.println("value comp: " + t1 + "\t" + t2 + "\t" +first + "\t" + second + "\t" + tc.getSecond().getURI());
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
                                if (currentValue != null) {
                                    //currentValue = tc.getSimilarity() * currentValue;
                                    if (currentValue > 0.5) {
                                        simValue += currentValue;
                                        //TODO: best idea to just add the values without any different weight? z.B. Spalten date,year wird beides gezaehlt!
                                        //normalerweise: property score as weight!
                                        double uniqueness = tc.getSecond().getNumberOfUniqueValues()/tc.getSecond().getValues().size();
                                        System.out.println("set value indirect: " + t1 + " - " + t2 + " - " + tc.getSecond() + " - "+ uniqueness +" - " + simValue);
                                        valueMatrix.set(t1, t2, simValue);
                                    }
                                }
                            }
                        }
                    }
                    if (counter > 1 && simValue > 0) {
                        valueMatrix.set(t1, t2, simValue / counter);
                    }

//                    Integer index1 = t2.getRowIndex();
//                    if (t2.getTable().getMapping().getMappedInstances().get(index1) != null) {
//
//                        //System.out.println("index1 not null " + index1 + " - " + t2);
//                        String mappedUri = t2.getTable().getMapping().getMappedInstances().get(index1).getFirst();
//                        boolean alreadyUsedMapping = false;
//                        for (Correspondence<TableRow> instanceMappings : result.getInstanceMappings()) {
//                            if (instanceMappings.getSecond().getURI().toString().equals(mappedUri)) {
//                                alreadyUsedMapping = true;
//                            }
//                        }
//                        if (alreadyUsedMapping) {
//                            continue;
//                        }
                    //System.out.println("mapped URI index1" + mappedUri);
                    //System.out.println("mappedUri + size " + mappedUri + " - " + data.getUriMap().get(mappedUri).size());
//                        for (TableRow dbpedia : data.getUriMap().get(mappedUri)) {
//                            if (candidateMatrix.get(t1, t2) != null) {
//                                //dbpediaCandidates.set(t1, dbpedia, candidateMatrix.get(t1, t2));
//                            }
//                        }
//                        if (mappingsToTables.containsKey(t1)) {
//                            mappingsToTables.get(t1).add(t2.getTable().toString());
//                        } else {
//                            List<String> ids = new ArrayList<>();
//                            ids.add(t2.getTable().toString());
//                            mappingsToTables.put(t1, ids);
//                        }
//                        Map<String, Integer> counts = possibleMappings.get(t1);
//                        Map<String, Integer> countsValue = possibleMappingsHeader.get(t1);
//
//                        if (counts.containsKey(mappedUri)) {
//                            Integer current = counts.get(mappedUri);
//                            current++;
//                            counts.put(mappedUri, current);
//                        } else {
//                            counts.put(mappedUri, 1);
//                        }
//
//                        if (valueMatrix.get(t1, t2) != null) {
//                            if (countsValue.containsKey(mappedUri)) {
//                                Integer current = countsValue.get(mappedUri);
//                                current++;
//                                countsValue.put(mappedUri, current);
//                            } else {
//                                countsValue.put(mappedUri, 1);
//                            }
//                        }
//                    }
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
                                System.out.println("set value: " + tr1 + " - " + dbpedia.getURI() + valueMatrix.get(tr1, t2));
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
//                        boolean alreadyUsedMapping = false;
//                        for (Correspondence<TableRow> instanceMappings : result.getInstanceMappings()) {
//                            if (instanceMappings.getSecond().getURI().toString().equals(mappedUri)) {
//                                alreadyUsedMapping = true;
//                            }
//                        }
//                        if (alreadyUsedMapping) {
//                            continue;
//                        }
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

                                double all = 0;
                                double mine = 0;
                                if (dbpedia.getURI() == null) {
                                    System.out.println("dbpedia null");
                                    continue;
                                }
                                if (labels == null || labels.get(dbpedia.getURI().toString()) == null) {
                                    labelBasedDBpedia.set(tr1, dbpedia, candidateMatrix.get(tr1, t2));
                                    System.out.println("set label: " + tr1 + " - " + t2 + " - " +dbpedia.getURI() + candidateMatrix.get(tr1, t2));
                                    // System.out.println("not found in labels! " + dbpedia.getURI().toString());
                                    continue;
                                }
                                for (String t : labels.get(dbpedia.getURI().toString())) {
                                    if (t2.getKey().toString().equals(t)) {
                                        mine++;
                                    } else {
                                        all++;
                                    }
                                }
                                if (mine / all > 0.2) {
                                    labelBasedDBpedia.set(tr1, dbpedia, candidateMatrix.get(tr1, t2));
                                    System.out.println("set label: " + tr1 + " - " + dbpedia.getURI() + candidateMatrix.get(tr1, t2));
                                }
                            }
                        }
                    }
                }
            }

            if (candidateMatrix.getFirstDimension().size() > 0 || valueMatrix.getFirstDimension().size() > 0) {
                System.out.println("XXX added matrices");
                List<SimilarityMatrix<TableRow>> list = new ArrayList<>();
                list.add(labelBasedDBpedia);
                list.add(valueBasedDBpedia);
                dbpediaPropsPerTable.put(otherTab, list);
            }

//            Map<SimilarityMatrix, MatrixStats> stats = new HashMap<>();
//            Map<TableRow, List<TableRow>> possibleMatchesByRow = new HashMap<>();
//            for (TableRow r : candidateMatrix.getFirstDimension()) {
//                List<TableRow> l = new ArrayList<>();
//                l.addAll(candidateMatrix.getMatches(r));
//                possibleMatchesByRow.put(r, l);
//            }
//
//            stats.put(valueMatrix, new MatrixStats(valueMatrix, data, possibleMatchesByRow));
//            stats.put(candidateMatrix, new MatrixStats(candidateMatrix, data, possibleMatchesByRow));
//            //combine the matrices?
//            CombineNonOverlapping<TableRow> combine = new CombineNonOverlapping<>();
//            combine.setFirstWeight(stats.get(candidateMatrix).getHerfindahlIndex());
//            combine.setSecondWeight(stats.get(valueMatrix).getHerfindahlIndex());
//            combine.setAggregationType(CombinationType.Sum);
//            SimilarityMatrix<TableRow> combinedMatrix = combine.match(candidateMatrix.copy(), valueMatrix.copy());
//
//            SimilarityMatrix<TableRow> dbpediaCandidates = new SparseSimilarityMatrix<>(unmappedRows.size(), candidateMatrix.getSecondDimension().size());
//
//            for (TableRow r1 : combinedMatrix.getFirstDimension()) {
//                for (TableRow r2 : combinedMatrix.getMatches(r1)) {
//                    if (r2.getTable().getMapping().getMappedInstances().get(r2.getRowIndex()) != null) {
//                        String mappedUri = r2.getTable().getMapping().getMappedInstances().get(r2.getRowIndex()).getFirst();
//                        dbpediaCandidates.set(r1, data.getUriMap().get(mappedUri).get(0), candidateMatrix.get(r1, r2));
//                    }
//                }
//            }
//            if (!dbpediaCandidates.getFirstDimension().isEmpty()) {
//                dbpediaPropsPerTable.put(otherTab, dbpediaCandidates);
//            }
        }
        for (String s : possibleLables.keySet()) {
            Integer i = Integer.parseInt(s.split("\t")[0]);
            String uri = s.split("\t")[2];
            boolean found = false;
            for (Object gsIndex : gs.getInstanceGoldStandard().keySet()) {
                if (Integer.parseInt(gsIndex.toString()) == i) {
                    found = true;
                    if (uri.equals(gs.getInstanceGoldStandard().get(gsIndex).toString())) {
                        System.out.println("possible labels: " + s + "\t" + possibleLables.get(s) + "\t1");
                    } else {
                        System.out.println("possible labels: " + s + "\t" + possibleLables.get(s) + "\t0");
                    }
                }
            }
            if (!found) {
                System.out.println("possible labels: " + s + "\t" + possibleLables.get(s) + "\t7");
            }
        }

        for (String s : possibleLablesAll.keySet()) {
            System.out.println("possible labels all: " + s + "\t" + possibleLablesAll.get(s));
        }

        overallLabel = new SparseSimilarityMatrix<>(unmappedRows.size(), data.getDbpediaRowSet().size());
        overallValue = new SparseSimilarityMatrix<>(unmappedRows.size(), data.getDbpediaRowSet().size());
        Map<TableRow, Integer> labelColumnCount = new HashMap<>();
        Map<TableRow, Integer> valueColumnCount = new HashMap<>();

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
                    if (getOverallLabel().get(r1, r2) != null) {
                        Double score = getOverallLabel().get(r1, r2);
                        //       if (score < label.get(r1, r2)) {
                        //try out    
                        score += label.get(r1, r2);

                        getOverallLabel().set(r1, r2, score);
                        //        }

                    } else {
                        getOverallLabel().set(r1, r2, label.get(r1, r2));
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
                    if (getOverallValue().get(r1, r2) != null) {
                        Double score = getOverallValue().get(r1, r2);
                        //     if (score < value.get(r1, r2)) {
                        //try out
                        score += value.get(r1, r2);
                        getOverallValue().set(r1, r2, score);

                    } else {
                        getOverallValue().set(r1, r2, value.get(r1, r2));
                    }
                }
            }
        }

        //try without normalization!
        for (TableRow r1 : getOverallLabel().getFirstDimension()) {
            for (TableRow r2 : getOverallLabel().getMatches(r1)) {
                if (labelColumnCount.get(r1) != null) {
                    int occurence = labelColumnCount.get(r1);
                    System.out.println("label unmapped inst" + r1.getTable().getHeader() + " - " + r1 + " - " + r2.getURI() + " - " + getOverallLabel().get(r1, r2) + " - " + occurence + " - " + getOverallLabel().get(r1, r2) / occurence);
                    getOverallLabel().set(r1, r2, getOverallLabel().get(r1, r2) / occurence);
                }
            }
        }

        for (TableRow r1 : getOverallValue().getFirstDimension()) {
            for (TableRow r2 : getOverallValue().getMatches(r1)) {
                if (valueColumnCount.get(r1) != null) {
                    boolean found = false;
                    int occurence = valueColumnCount.get(r1);
                    for (Object o : gs.getInstanceGoldStandard().keySet()) {
                        if (r2.getURI().equals(gs.getInstanceGoldStandard().get(o))) {
                            System.out.println("value unmapped inst" + r1.getTable().getHeader() + " - " + r1 + " - " + r2.getURI() + " - " + getOverallValue().get(r1, r2) + " - " + occurence + " - " + getOverallValue().get(r1, r2) / occurence
                                    + "\t1");
                            found = true;
                        }

                    }
                    if (!found) {
                        System.out.println("value unmapped inst" + r1.getTable().getHeader() + " - " + r1 + " - " + r2.getURI() + " - " + getOverallValue().get(r1, r2) + " - " + occurence + " - " + getOverallValue().get(r1, r2) / occurence
                                + "\t0");
                    }

                    getOverallValue().set(r1, r2, getOverallValue().get(r1, r2) / occurence);
                }
            }
        }

        overallLabel.pruneWithNullEqualOrBelow(0.0);

        Map<SimilarityMatrix, MatrixStats> stats = new HashMap<>();
        Map<TableRow, List<TableRow>> possibleMatchesByRow = new HashMap<>();
        for (TableRow r : getOverallLabel().getFirstDimension()) {
            List<TableRow> l = new ArrayList<>();
            l.addAll(getOverallLabel().getMatches(r));
            possibleMatchesByRow.put(r, l);
        }
        for (TableRow r : getOverallValue().getFirstDimension()) {
            List<TableRow> l = new ArrayList<>();
            l.addAll(getOverallValue().getMatches(r));
            possibleMatchesByRow.put(r, l);
        }

        stats.put(getOverallValue(), new MatrixStats(getOverallValue(), data, possibleMatchesByRow));
        stats.put(getOverallLabel(), new MatrixStats(getOverallLabel(), data, possibleMatchesByRow));
        //combine the matrices?
        CombineNonOverlapping<TableRow> combine = new CombineNonOverlapping<>();
        //combine.setFirstWeight(stats.get(getOverallValue()).getHerfindahlIndex());

        SimilarityMatrix y2 = getOverallValue().copy();
        //TODO: normalizedHerf
        y2.multiplyScalar(stats.get(getOverallValue()).getHerfindahlIndex());
        SimilarityMatrix x2 = getOverallLabel().copy();
        x2.multiplyScalar(stats.get(getOverallLabel()).getHerfindahlIndex());

        //System.out.println("XXX value herf: " + stats.get(propsBetweenTables).getHerfindahlIndex());
        //System.out.println("XXX label herf: " + stats.get(labelSimilarityUnmapped).getHerfindahlIndex());
        //combine.setSecondWeight(stats.get(getOverallLabel()).getHerfindahlIndex());
        combine.setAggregationType(CombinationType.Sum);
        SimilarityMatrix<TableRow> overall1 = combine.match(y2, x2);
        SparseSimilarityMatrix<TableRow> overall = (SparseSimilarityMatrix) overall1;

//        Map<String, Integer> countR2 = new HashMap<>();
//        for (Table t : dbpediaPropsPerTable.keySet()) {
//            SimilarityMatrix<TableRow> matrix = dbpediaPropsPerTable.get(t);
//            for (TableRow r1 : matrix.getFirstDimension()) {
//                for (TableRow r2 : matrix.getMatches(r1)) {
//                    if (countR2.containsKey(r1.toString() + "\t" + r2.toString())) {
//                        Integer current = countR2.get(r1.toString() + "\t" + r2.toString());
//                        current++;
//                        countR2.put(r1.toString() + "\t" + r2.toString(), current);
//                    } else {
//                        countR2.put(r1.toString() + "\t" + r2.toString(), 1);
//                    }
//
//                    if (overall.get(r1, r2) != null) {
//                        Double score = overall.get(r1, r2);
//                        score += matrix.get(r1, r2);
//                        overall.set(r1, r2, score);
//                    } else {
//                        overall.set(r1, r2, matrix.get(r1, r2));
//                    }
//                }
//            }
//        }
//        overall.makeRowStochastic();
//
//        double averageCounts = 0.0;
//        Map<String, Integer> allCounts = new HashMap<>();
//
//        for (String s : countR2.keySet()) {
//            averageCounts += countR2.get(s);
//            String row = s.split("\t")[0];
//            if (!allCounts.containsKey(row)) {
//                allCounts.put(row, countR2.get(s));
//            } else {
//                Integer i = allCounts.get(row);
//                i += countR2.get(s);
//                allCounts.put(row, i);
//            }
//
//        }
//        averageCounts = averageCounts / countR2.size();

            //TODO: don't use the class in the gs but of the table
//         Map<String, String> superClassMap = new HashMap<>();
//            Collection<String[]> superclasses = CSVUtils.readCSV(
//                    data.get.getClassHierarchyLocation(), "\t");
//            for (String[] s : superclasses) {
//                superClassMap.put(
//                        s[0].replace("http://dbpedia.org/ontology/", "")
//                        .toLowerCase(),
//                        s[1].replace("http://dbpedia.org/ontology/", "")
//                        .toLowerCase());
//            }

        for (TableRow tr1 : overall.getFirstDimension()) {
            for (TableRow tr2 : overall.getMatches(tr1)) {
                List<Object> possible = gs.getClassGoldStandard().get(tr1.getTable().getHeader().replace(".json", ""));
                //otherwise exception and exclude the tables with no class
                if (possible == null || possible.isEmpty()) {
                    continue;
                }
                System.out.println("possible classes: " + possible + " vs " + tr2.getTable().getHeader().replace(".csv.gz", "").toLowerCase());
                if (!possible.contains(tr2.getTable().getHeader().replace(".csv.gz", "").toLowerCase())) {
                    overall.set(tr1, tr2, null);
                    System.out.println("set to null: " + tr1 + "\t" + tr2);
                }
            }
        }

        SparseSimilarityMatrix<TableRow> overall2 = new SparseSimilarityMatrix<>(overall.getFirstDimension().size(),
                overall.getSecondDimension().size());

        for (TableRow r1 : overall.getFirstDimension()) {
            if (overall.getMatches(r1).isEmpty()) {
                continue;
            }
            TableRow best = overall.getBestPair(r1);
            overall2.set(r1, best, overall.get(r1, best));
            boolean inGS = false;
            for (Object o : gs.getInstanceGoldStandard().keySet()) {
                Integer i = Integer.parseInt(o.toString());
                //double diffToAvg = countR2.get(r1.toString() + "\t" + best.toString()) - averageCounts;
                if (i == r1.getRowIndex()) {
                    inGS = true;
                    if (best.getURI().equals(gs.getInstanceGoldStandard().get(o))) {
                        System.out.println("final overall DBpedia: " + r1.getTable().getHeader() + "\t" + r1 + "\t" + best.getURI() + "\t" + overall.get(r1, best) + "\t"
                                + getOverallValue().get(r1, best) + "\t" + getOverallLabel().get(r1, best) + "\t" + labelColumnCount.get(r1) + "\t" + valueColumnCount.get(r1)
                                + "\t" + dbpediaPropsPerTable.size() + "\t" + getOverlappingProps() + "\t" + result.getPropertyMappings().size() + "\tCORRECT");
                    } else {
                        System.out.println("final overall DBpedia: " + r1.getTable().getHeader() + "\t" + r1 + "\t" + best.getURI() + "\t" + overall.get(r1, best) + "\t"
                                + getOverallValue().get(r1, best) + "\t" + getOverallLabel().get(r1, best) + "\t" + labelColumnCount.get(r1) + "\t" + valueColumnCount.get(r1)
                                + "\t" + dbpediaPropsPerTable.size() + "\t" + getOverlappingProps() + "\t" + result.getPropertyMappings().size() + "\tINCORRECT");
                    }
                }
            }
            if (!inGS) {
                System.out.println("final overall DBpedia: " + r1.getTable().getHeader() + "\t" + r1 + "\t" + best.getURI() + "\t" + overall.get(r1, best) + "\t"
                        + getOverallValue().get(r1, best) + "\t" + getOverallLabel().get(r1, best) + "\t" + labelColumnCount.get(r1) + "\t" + valueColumnCount.get(r1)
                        + "\t" + dbpediaPropsPerTable.size() + "\t" + getOverlappingProps() + "\t" + result.getPropertyMappings().size() + "\tINCORRECT2");
            }
        }

//        for (TableRow r1 : overall.getFirstDimension()) {
//            TableRow best = overall.getBestPair(r1);
//            boolean inGS = false;
//            double diffToAvg = countR2.get(r1.toString() + "\t" + best.toString()) - averageCounts;
//            for (Object o : gs.getInstanceGoldStandard().keySet()) {
//                Integer i = Integer.parseInt(o.toString());
//                if (i == r1.getRowIndex()) {
//                    inGS = true;
//                    if (best.getURI().equals(gs.getInstanceGoldStandard().get(o))) {
//                        System.out.println("final overall DBpedia: " + r1.getTable().getHeader() + "\t" + r1 + "\t" + best + "\t" + overall.get(r1, best) + "\t"
//                                + countR2.get(r1.toString() + "\t" + best.toString()) + "\t" + diffToAvg + "\t" + dbpediaPropsPerTable.size() + "\t" + allCounts.get(r1.toString()) + "\tCORRECT");
//                    } else {
//                        System.out.println("final overall DBpedia: " + r1.getTable().getHeader() + "\t" + r1 + "\t" + best + "\t" + overall.get(r1, best) + "\t"
//                                + countR2.get(r1.toString() + "\t" + best.toString()) + "\t" + diffToAvg + "\t" + dbpediaPropsPerTable.size() + "\t" + allCounts.get(r1.toString()) + "\tINCORRECT");
//                    }
//                }
//            }
//            if (!inGS) {
//                System.out.println("final overall DBpedia: " + r1.getTable().getHeader() + "\t" + r1 + "\t" + best + "\t" + overall.get(r1, best) + "\t"
//                        + countR2.get(r1.toString() + "\t" + best.toString()) + "\t" + diffToAvg + "\t" + dbpediaPropsPerTable.size() + "\t" + allCounts.get(r1.toString()) + "\tINCORRECT2");
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

        return overall2;
    }

    /**
     * @return the overallLabel
     */
    public SimilarityMatrix<TableRow> getOverallLabel() {
        return overallLabel;
    }

    /**
     * @return the overallValue
     */
    public SimilarityMatrix<TableRow> getOverallValue() {
        return overallValue;
    }

    /**
     * @return the overlappingProps
     */
    public Set<TableColumn> getOverlappingProps() {
        return overlappingProps;
    }

}
