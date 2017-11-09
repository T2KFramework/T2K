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
 * limitations under the License.
 */
package de.dwslab.T2K.matching.dbpedia.matchers.schema;

import de.dwslab.T2K.matching.blocking.IdentityBlocking;
import de.dwslab.T2K.matching.blocking.TypeBasedBlocking;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.AlwaysMatchSimilarityFunction;
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
import de.dwslab.T2K.matching.dbpedia.model.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingPair;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.DBpediaPropertyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.context.StopWordRemover;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcher;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.CombineNonOverlapping;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.timer.Timer;
import de.uni_mannheim.informatik.dws.t2k.normalisation.StringNormalizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
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
public class IndirectSchemaMatcherMapped {

    public Map<TableColumn, List<MatchingPair>> computeIndirectMappings(MatchingParameters matchingParameters, Timer rootTimer,
            MatchingData data, Configuration config, MatchingResult result, GoldStandard gs, Matchers matchers, MatchingLogger log, EvaluationParameters evalParams, List<TableColumn> unmappedCols, Map<Table, SimilarityMatrix<TableRow>> mapped) {

        SimilarityMatrix<TableColumn> indirectCombined = new SparseSimilarityMatrix<>(0, 0);

        SimilarityMatrix<TableColumn> labelSimilarityUnmapped = new SparseSimilarityMatrix<>(0, 0);

        DBpediaPropertyAdapter evalInstance = new DBpediaPropertyAdapter(gs.getPropertyCanoniser());

        Map<TableColumn, Map<String, Integer>> possibleMappings = new HashMap<>();

        Map<String, Integer> propHeaderCount = new HashMap<>();

        Map<Table, List<SimilarityMatrix<TableColumn>>> dbpediaPropsPerTable = new HashMap<>();

        try {
            File f = new File("listHeaderProperty.csv");
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line = read.readLine();
            while (line != null) {
                String[] content = line.split("\t");
                propHeaderCount.put(content[0], Integer.parseInt(content[1]));
                line = read.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<TableColumn, List<MatchingPair>> generatedPairs = new HashMap<>();

        unmappedCols = new ArrayList<>();

        for (TableColumn tr : data.getWebtable().getColumns()) {
            boolean corFound = false;
            for (Correspondence<TableColumn> c : result.getPropertyMappings()) {
                if (tr.equals(c.getFirst())) {
                    corFound = true;
                }
            }
            if (corFound) {
                unmappedCols.add(tr);
                List<MatchingPair> l = new ArrayList<>();
                generatedPairs.put(tr, l);
            }
        }
        System.out.println("domi mapped cols size: " + unmappedCols.size());

        for (TableColumn unmappedCol : unmappedCols) {
            Map<String, Integer> countsPerCol = new HashMap<>();
            possibleMappings.put(unmappedCol, countsPerCol);
        }

        Map<TableColumn, Map<String, Integer>> possibleMappingsCosine = new HashMap<>();
        for (TableColumn unmappedCol : unmappedCols) {
            Map<String, Integer> countsPerCol = new HashMap<>();
            possibleMappingsCosine.put(unmappedCol, countsPerCol);
        }

        Map<TableColumn, Map<String, Integer>> possibleMappingsHeader = new HashMap<>();
        for (TableColumn unmappedCol : unmappedCols) {
            Map<String, Integer> countsPerCol = new HashMap<>();
            possibleMappingsHeader.put(unmappedCol, countsPerCol);
        }

        Set<String> possibleCols = new HashSet<>();
        for (Table otherTab : mapped.keySet()) {
            SimilarityMatrix<TableRow> overlappingRows = mapped.get(otherTab);

            for (TableColumn tc : otherTab.getColumns()) {
                possibleCols.add(tc.getURI());
            }
            //System.out.println("matching: " + otherTab);

            LabelBasedMatcher<TableColumn> labelMatching = new LabelBasedMatcher<>();
            labelMatching.setBlocking(new IdentityBlocking<TableColumn>());

            // calculate scores based on column/label string similarity
            StringSimilarityMeasure<TableColumn> colMeasure = new StringSimilarityMeasure<>();
            colMeasure.setSimilarityFunction((SimilarityFunction<String>) config.getValue(PAR_LABEL_SIMILARITY));
            colMeasure.setSetSimilarity(new MaxSimilarity());
            labelMatching.setSimilarityMeasure(colMeasure);

            labelMatching.setCollectMatchingInfo(matchingParameters.isCollectMatchingInfo());
            labelMatching.setSimilarityThreshold(0.5);

            labelSimilarityUnmapped = labelMatching.match(unmappedCols, otherTab.getColumns(), new TableColumnMatchingAdapter());

            SimilarityMatrix<TableColumn> labelSimIndirect = new SparseSimilarityMatrix<>(labelSimilarityUnmapped.getFirstDimension().size(), labelSimilarityUnmapped.getSecondDimension().size());

            for (TableColumn t1 : labelSimilarityUnmapped.getFirstDimension()) {
                for (TableColumn t2 : labelSimilarityUnmapped.getMatches(t1)) {
                    if (t1.getDataType() != t2.getDataType()) {
                        continue;
                    }
                    Integer index = t2.getTable().getColumns().indexOf(t2);

                    if (t2.getTable().getMapping().getMappedProperties().get(index) != null
                            && t2.getTable().getMapping().getMappedProperties().get(index).getFirst().contains("dbpedia.org") && !t2.getTable().getMapping().getMappedProperties().get(index).getFirst().contains("rdf-")) {

                        TableColumn mappedDBpediaProp = null;
                        String mappedUri = t2.getTable().getMapping().getMappedProperties().get(index).getFirst();
                        for (TableColumn tc : data.getDbpediaColSet()) {
                            if (tc.getURI().equals(mappedUri)) {
                                mappedDBpediaProp = tc;
                            }
                        }

                        if (mappedDBpediaProp == null || mappedDBpediaProp.getDataType() != t1.getDataType()) {
                            continue;
                        }

                        for (TableColumn tc : data.getDbpediaColSet()) {
                            if (evalInstance.getUniqueIdentifier(tc).equals(evalInstance.getUniqueIdentifier(mappedDBpediaProp))) {
                                if (tc.getDataType() == t1.getDataType()) {
                                    String reolvedURI = evalInstance.getUniqueIdentifier(tc).toString();
                                    Map<String, Integer> counts = possibleMappingsHeader.get(t1);
                                    if (counts.containsKey(reolvedURI)) {
                                        Integer current = counts.get(reolvedURI);
                                        current++;
                                        counts.put(reolvedURI, current);
                                    } else {
                                        counts.put(reolvedURI, 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Similarities sim = new Similarities();
            sim.setInitialCandidateSimilarity(mapped.get(otherTab));
            sim.setCandidateSimilarity(mapped.get(otherTab));
            SimilarityMatrix newClassSim = new SparseSimilarityMatrix(1, 1);
            //  for (TableColumn colInWTs : cols) {
            newClassSim.set(data.getWebtable(), otherTab, 1.0);
            //}
            sim.setClassSimilarity(newClassSim);

            ValueBasedComponent valueBased = new ValueBasedComponent();
            valueBased.initialise(matchers, data, evalParams, gs, log, matchingParameters, sim, rootTimer);
            Set<TableRow> allPossibleRows = new HashSet<>();
            for (TableRow t2 : mapped.get(otherTab).getSecondDimension()) {
                allPossibleRows.add(t2);
            }
            valueBased.setOtherRows(allPossibleRows);
            valueBased.setWebTableName(data.getWebtable().getHeader());
            valueBased.run(config);
            sim.setValueSimilarity(valueBased.getValueSimilarity());
            //System.out.println(valueBased.getValueSimilarity().getFirstDimension().size() + " - " + valueBased.getValueSimilarity().getSecondDimension().size());

            DuplicateBasedSchemaMatcher dp = new DuplicateBasedSchemaMatcher(sim, matchingParameters, rootTimer, gs, log);
            dp.setValueSimilarity(sim.getValueSimilarity());
            dp.setCandidateSimilarity(sim.getCandidateSimilarity());
            dp.setClassSimilarity(sim.getClassSimilarity());
            dp.setFinalClass(otherTab);
            dp.setNumCandidatesPerInstance((int) config.getValue(PAR_PROP_NUM_CANDIDATES));
            dp.setCandidateThreshold((double) config.getValue(PAR_PROP_CANDIDATE_THRESHOLD));
            dp.setValueThreshold((double) config.getValue(PAR_PROP_VALUE_THRESHOLD));
            dp.setNumVotesPerInstance((int) config.getValue(PAR_PROP_NUM_VOTES));

            LabelBasedSchemaMatcher lbs2 = new LabelBasedSchemaMatcher(sim, matchingParameters, rootTimer, gs, log);
            lbs2.setLabelSimilarity((SimilarityFunction<String>) new AlwaysMatchSimilarityFunction());
            lbs2.setBlocking(new TypeBasedBlocking<>(new TableColumnMatchingAdapter()));
            lbs2.setOtherColSet(otherTab.getColumns());
            lbs2.setWtColSet(unmappedCols);
            SimilarityMatrix<TableColumn> allLabelSimilarity2 = lbs2.match(data);

            dp.setLabelSimilarity(allLabelSimilarity2);
            dp.setNumResults((int) config.getValue(PAR_PROP_NUM_RESULTS));
            dp.setFinalThreshold(0.0);

            SimilarityMatrix<TableColumn> propsBetweenTables = dp.match(data);
            propsBetweenTables.pruneWithNullEqualOrBelow(0.0);

            SimilarityMatrix<TableColumn> valueBasedDBpedia = new SparseSimilarityMatrix<>(propsBetweenTables.getFirstDimension().size(), propsBetweenTables.getSecondDimension().size());

            for (TableColumn r1 : propsBetweenTables.getFirstDimension()) {
                for (TableColumn r2 : propsBetweenTables.getMatches(r1)) {
                    if (r2.getTable().getMapping().getMappedProperties().get(r2.getTable().getColumns().indexOf(r2)) != null) {
                        String mappedUri = r2.getTable().getMapping().getMappedProperties().get(r2.getTable().getColumns().indexOf(r2)).getFirst();
                        System.out.println("XXX mappedURI " + mappedUri);
                        TableColumn mappedDBpediaProp = null;
                        for (TableColumn tc : data.getDbpediaColSet()) {
                            if (tc.getURI().equals(mappedUri)) {
                                mappedDBpediaProp = tc;
                            }
                        }
                        System.out.println("XXX set matrix " + r1 + " - " + mappedDBpediaProp);
                        if (mappedDBpediaProp != null) {
                            valueBasedDBpedia.set(r1, mappedDBpediaProp, propsBetweenTables.get(r1, r2));
                        }
                    }
                }
            }

            SimilarityMatrix<TableColumn> labelBasedDBpedia = new SparseSimilarityMatrix<>(labelSimilarityUnmapped.getFirstDimension().size(), labelSimilarityUnmapped.getSecondDimension().size());

            for (TableColumn r1 : labelSimilarityUnmapped.getFirstDimension()) {
                for (TableColumn r2 : labelSimilarityUnmapped.getMatches(r1)) {
                    if (r2.getTable().getMapping().getMappedProperties().get(r2.getTable().getColumns().indexOf(r2)) != null) {
                        String mappedUri = r2.getTable().getMapping().getMappedProperties().get(r2.getTable().getColumns().indexOf(r2)).getFirst();
                        //System.out.println("XXX mappedURI " + mappedUri);
                        TableColumn mappedDBpediaProp = null;
                        for (TableColumn tc : data.getDbpediaColSet()) {
                            if (tc.getURI().equals(mappedUri)) {
                                mappedDBpediaProp = tc;
                            }
                        }
                        //System.out.println("XXX set matrix " + r1 + " - " + mappedDBpediaProp);
                        if (mappedDBpediaProp != null) {
                            labelBasedDBpedia.set(r1, mappedDBpediaProp, labelSimilarityUnmapped.get(r1, r2));
                        }
                    }
                }
            }

            if (labelBasedDBpedia.getFirstDimension().size() > 0 || valueBasedDBpedia.getFirstDimension().size() > 0) {
                System.out.println("XXX added matrices");
                List<SimilarityMatrix<TableColumn>> list = new ArrayList<>();
                list.add(labelBasedDBpedia);
                list.add(valueBasedDBpedia);
                dbpediaPropsPerTable.put(otherTab, list);
            }

            for (TableColumn c1 : propsBetweenTables.getFirstDimension()) {
//                                System.out.println("c1: " + c1.getValues());
                for (TableColumn c2 : propsBetweenTables.getMatches(c1)) {
//                                    System.out.println("c2: " + c1.getValues() + " mappings: " + c2.getTable().getMapping().getMappedProperties() + " table: "
//                                    + c2.getTable());
                    if (c1.getDataType() != c2.getDataType()) {
                        continue;
                    }
                    if (c2.getTable().getMapping() != null
                            && c2.getTable().getMapping().getMappedProperties() != null
                            && c2.getTable().getMapping().getMappedProperties().get(c2.getTable().getColumns().indexOf(c2)) != null) {

                        TableColumn mappedDBpediaProp = null;
                        String mappedUri = c2.getTable().getMapping().getMappedProperties().get(c2.getTable().getColumns().indexOf(c2)).getFirst();
                        for (TableColumn tc : data.getDbpediaColSet()) {
                            if (tc.getURI().equals(mappedUri)) {
                                mappedDBpediaProp = tc;
                            }
                        }
                        if (mappedDBpediaProp == null || mappedDBpediaProp.getDataType() != c1.getDataType()) {
                            continue;
                        }
                        for (TableColumn tc : data.getDbpediaColSet()) {
                            if (evalInstance.getUniqueIdentifier(tc).equals(evalInstance.getUniqueIdentifier(mappedDBpediaProp))) {
                                if (tc.getDataType() == c1.getDataType()) {
                                    Map<String, Integer> counts = possibleMappings.get(c1);
                                    String reolvedURI = evalInstance.getUniqueIdentifier(tc).toString();
                                    if (counts.containsKey(reolvedURI)) {
                                        Integer current = counts.get(reolvedURI);
                                        current++;
                                        counts.put(reolvedURI, current);
                                    } else {
                                        counts.put(reolvedURI, 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SimilarityMatrix<TableColumn> simCosine = new SparseSimilarityMatrix(unmappedCols.size(), otherTab.getColumns().size());

            for (TableColumn c1 : unmappedCols) {
                if (c1.getDataType() == TableColumn.ColumnDataType.string) {
                    for (TableColumn c2 : otherTab.getColumns()) {
                        if (c2.getDataType() != TableColumn.ColumnDataType.string) {
                            continue;
                        }
                        Map<String, Integer> countsValuesDBpedia = new TreeMap<>();
                        for (Object o : c2.getValues().values()) {
                            if (o instanceof List) {
                                List<String> list = (List<String>) o;
                                try {
                                    for (String value : list) {
                                        if (value.contains(" ") || value.contains("_")) {
                                            String[] splittedValues;
                                            if (value.contains(" ")) {
                                                splittedValues = value.split("\\s");
                                            } else {
                                                splittedValues = value.split("_");
                                            }
                                            for (String s : splittedValues) {
                                                s = s.replace("(", "");
                                                if (!StopWordRemover.isStopWord(s)) {
                                                    if (countsValuesDBpedia.containsKey(s)) {
                                                        Integer current = countsValuesDBpedia.get(s);
                                                        current++;
                                                        countsValuesDBpedia.put(s, current);
                                                    } else {
                                                        countsValuesDBpedia.put(s, 1);
                                                    }
                                                }
                                            }
                                        } else {
                                            value = StringNormalizer.normaliseValue(value, true);
                                            if (countsValuesDBpedia.containsKey(value)) {
                                                Integer current = countsValuesDBpedia.get(value);
                                                current++;
                                                countsValuesDBpedia.put(value, current);
                                            } else {
                                                countsValuesDBpedia.put(value, 1);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            } else if (o instanceof String) {
                                String value = (String) o;
                                value = StringNormalizer.normaliseValue(value, true);
                                if (value.contains(" ") || value.contains("_")) {
                                    String[] splittedValues;
                                    if (value.contains(" ")) {
                                        splittedValues = value.split("\\s");
                                    } else {
                                        splittedValues = value.split("_");
                                    }
                                    for (String s : splittedValues) {
                                        if (!StopWordRemover.isStopWord(s)) {
                                            if (countsValuesDBpedia.containsKey(s)) {
                                                Integer current = countsValuesDBpedia.get(s);
                                                current++;
                                                countsValuesDBpedia.put(s, current);
                                            } else {
                                                countsValuesDBpedia.put(s, 1);
                                            }
                                        }
                                    }
                                } else {
                                    if (countsValuesDBpedia.containsKey(value)) {
                                        Integer current = countsValuesDBpedia.get(value);
                                        current++;
                                        countsValuesDBpedia.put(value, current);
                                    } else {
                                        countsValuesDBpedia.put(value, 1);
                                    }
                                }
                            }
                        }
                        //System.out.println("count values: " + countsValues);

                        double sum = 0.0;

                        Map<String, Integer> countsValuesWT = new TreeMap<>();

                        //System.out.println("values table: " + cor.getFirst().getValues().values());
                        for (Object o : c1.getValues().values()) {
                            //System.out.println(o);
                            if (o instanceof List) {
                                List<String> list = (List<String>) o;
                                for (String s : list) {
                                    if (s.contains(",")) {
                                        s = s.split(",")[0];
                                        s = s.trim();
                                    }
                                    if (s.contains("/")) {
                                        s = s.split("/")[0];
                                        s = s.trim();
                                    }
                                    if (s.contains(" ")) {
                                        String[] splittedValues = s.split("\\s");
                                        for (String t : splittedValues) {
                                            if (!StopWordRemover.isStopWord(t)) {
                                                if (countsValuesDBpedia.containsKey(t)) {
                                                    //sum += countsValuesDBpedia.get(t);
                                                    if (countsValuesWT.containsKey(t)) {
                                                        Integer current = countsValuesWT.get(t);
                                                        current++;
                                                        countsValuesWT.put(t, current);
                                                    } else {
                                                        countsValuesWT.put(t, 1);
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        if (countsValuesDBpedia.containsKey(s)) {
                                            //sum += countsValuesDBpedia.get(s);
                                            if (countsValuesWT.containsKey(s)) {
                                                Integer current = countsValuesWT.get(s);
                                                current++;
                                                countsValuesWT.put(s, current);
                                            } else {
                                                countsValuesWT.put(s, 1);
                                            }
                                        }
                                    }
                                }
                            } else {
                                String s = (String) o;
                                if (s.contains(",")) {
                                    s = s.split(",")[0];
                                    s = s.trim();
                                }
                                if (s.contains("/")) {
                                    s = s.split("/")[0];
                                    s = s.trim();
                                }
                                s = s.trim();
                                if (s.contains(" ")) {
                                    String[] splittedValues = s.split("\\s");
                                    for (String t : splittedValues) {
                                        if (!StopWordRemover.isStopWord(t)) {
                                            if (countsValuesDBpedia.containsKey(t)) {
                                                //sum += countsValuesDBpedia.get(t);
                                                if (countsValuesWT.containsKey(t)) {
                                                    Integer current = countsValuesWT.get(t);
                                                    current++;
                                                    countsValuesWT.put(t, current);
                                                } else {
                                                    countsValuesWT.put(t, 1);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (countsValuesDBpedia.containsKey(s)) {
                                        //sum += countsValuesDBpedia.get(s);
                                        if (countsValuesWT.containsKey(s)) {
                                            Integer current = countsValuesWT.get(s);
                                            current++;
                                            countsValuesWT.put(s, current);
                                        } else {
                                            countsValuesWT.put(s, 1);
                                        }
                                    }
                                }
                            }
                        }
                        double score = WebTableToDBpediaComponentProcess.calculateCosineSimilarity(countsValuesDBpedia, countsValuesWT);
                        simCosine.set(c1, c2, score);
                    }
                }
            }
            simCosine.pruneWithNullEqualOrBelow(0.5);
            for (TableColumn c1 : simCosine.getFirstDimension()) {
                for (TableColumn c2 : simCosine.getMatches(c1)) {
                    if (c2.getTable().getMapping() != null
                            && c2.getTable().getMapping().getMappedProperties() != null
                            && c2.getTable().getMapping().getMappedProperties().get(c2.getTable().getColumns().indexOf(c2)) != null) {
                        //String mappedDbpediaProp = c2.getTable().getMapping().getMappedProperties().get(c2.getTable().getColumns().indexOf(c2)).getFirst();

                        TableColumn mappedDBpediaProp = null;
                        String mappedUri = c2.getTable().getMapping().getMappedProperties().get(c2.getTable().getColumns().indexOf(c2)).getFirst();
                        for (TableColumn tc : data.getDbpediaColSet()) {
                            if (tc.getURI().equals(mappedUri)) {
                                mappedDBpediaProp = tc;
                            }
                        }
                        if (mappedDBpediaProp == null || mappedDBpediaProp.getDataType() != c1.getDataType()) {
                            continue;
                        }
                        for (TableColumn tc : data.getDbpediaColSet()) {
                            if (evalInstance.getUniqueIdentifier(tc).equals(evalInstance.getUniqueIdentifier(mappedDBpediaProp))) {
                                String resolvedURI = evalInstance.getUniqueIdentifier(mappedDBpediaProp).toString();
                                if (tc.getDataType() == c1.getDataType()) {
                                    Map<String, Integer> counts = possibleMappingsCosine.get(c1);
                                    if (counts.containsKey(resolvedURI)) {
                                        Integer current = counts.get(resolvedURI);
                                        current++;
                                        counts.put(resolvedURI, current);
                                    } else {
                                        counts.put(resolvedURI, 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SimilarityMatrix<TableColumn> overallLabel = new SparseSimilarityMatrix<>(unmappedCols.size(), data.getDbpediaColSet().size());
        SimilarityMatrix<TableColumn> overallValue = new SparseSimilarityMatrix<>(unmappedCols.size(), data.getDbpediaColSet().size());
        Map<TableColumn, Integer> labelColumnCount = new HashMap<>();
        Map<String, Integer> labelColumnPairCount = new HashMap<>();
        Map<TableColumn, Integer> valueColumnCount = new HashMap<>();
        Map<String, Integer> valueColumnPairCount = new HashMap<>();

        for (Table t : dbpediaPropsPerTable.keySet()) {
            SimilarityMatrix<TableColumn> label = dbpediaPropsPerTable.get(t).get(0);
            SimilarityMatrix<TableColumn> value = dbpediaPropsPerTable.get(t).get(1);

            for (TableColumn r1 : label.getFirstDimension()) {
                if (r1.isKey()) {
                    continue;
                }
                if (labelColumnCount.containsKey(r1)) {
                    Integer i = labelColumnCount.get(r1);
                    i++;
                    labelColumnCount.put(r1, i);
                } else {
                    labelColumnCount.put(r1, 1);
                }
                for (TableColumn r2 : label.getMatches(r1)) {
                    //might be good to see if we can get some better results if we allow different datatypes
                    if (r1.getDataType() != r2.getDataType()) {
                        continue;
                    }

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

            for (TableColumn r1 : value.getFirstDimension()) {
                if (r1.isKey()) {
                    continue;
                }
                if (valueColumnCount.containsKey(r1)) {
                    Integer i = valueColumnCount.get(r1);
                    i++;
                    valueColumnCount.put(r1, i);
                } else {
                    valueColumnCount.put(r1, 1);
                }
                for (TableColumn r2 : value.getMatches(r1)) {
                    //might be good to see if we can get some better results if we allow different datatypes
                    if (r1.getDataType() != r2.getDataType()) {
                        continue;
                    }

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

        for (TableColumn r1 : overallLabel.getFirstDimension()) {
            for (TableColumn r2 : overallLabel.getMatches(r1)) {
                if (labelColumnCount.get(r1) != null) {
                    int occurence = labelColumnCount.get(r1);
                    System.out.println("label mapped " + r1 + " - " + r2 + " - " + overallLabel.get(r1, r2) + " - " + occurence + " - " + overallLabel.get(r1, r2) / occurence);
                    overallLabel.set(r1, r2, overallLabel.get(r1, r2) / occurence);

                    MatchingPair mp = new MatchingPair();
                    mp.setMatchingPair(new Pair(r1, r2));
                    mp.setLabelScore(overallLabel.get(r1, r2));
                    mp.setOccurenceFirstLabel(occurence);
                    mp.setOtherPairsLabel(overallLabel.getMatches(r1).size());
                    mp.setOverallNumbTables(dbpediaPropsPerTable.size());
                    mp.setOccurencePairLabel(labelColumnPairCount.get(r1 + "\t" + r2));
                    generatedPairs.get(r1).add(mp);
                }
            }
        }

        for (TableColumn r1 : overallValue.getFirstDimension()) {
            for (TableColumn r2 : overallValue.getMatches(r1)) {
                if (valueColumnCount.get(r1) != null) {
                    int occurence = valueColumnCount.get(r1);
                    System.out.println("value mapped " + r1 + " - " + r2 + " - " + overallValue.get(r1, r2) + " - " + occurence + " - " + overallValue.get(r1, r2) / occurence);
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
                        generatedPairs.get(r1).add(mp);
                    }
                }
            }
        }

        Map<SimilarityMatrix, MatrixStats> stats = new HashMap<>();
        Map<TableColumn, List<TableColumn>> possibleMatchesByRow = new HashMap<>();
        for (TableColumn r : overallLabel.getFirstDimension()) {
            List<TableColumn> l = new ArrayList<>();
            l.addAll(overallLabel.getMatches(r));
            possibleMatchesByRow.put(r, l);
        }
        for (TableColumn r : overallValue.getFirstDimension()) {
            List<TableColumn> l = new ArrayList<>();
            l.addAll(overallValue.getMatches(r));
            possibleMatchesByRow.put(r, l);
        }
//            //System.out.println("XXX value matrix: " + propsBetweenTables.getSecondDimension().size());
//            //System.out.println("XXX label matrix: " + labelSimilarityUnmapped.getSecondDimension().size());
//
        stats.put(overallValue, new MatrixStats(overallValue, data, possibleMatchesByRow));
        stats.put(overallLabel, new MatrixStats(overallLabel, data, possibleMatchesByRow));
        //combine the matrices?
        CombineNonOverlapping<TableColumn> combine = new CombineNonOverlapping<>();
        combine.setFirstWeight(stats.get(overallValue).getMean());
        //System.out.println("XXX value herf: " + stats.get(propsBetweenTables).getHerfindahlIndex());
        //System.out.println("XXX label herf: " + stats.get(labelSimilarityUnmapped).getHerfindahlIndex());
        combine.setSecondWeight(stats.get(overallLabel).getMean());
        combine.setAggregationType(CombinationType.Sum);
        SimilarityMatrix<TableColumn> overall1 = combine.match(overallValue.copy(), overallLabel.copy());

        for (TableColumn tx : overall1.getFirstDimension()) {
            for (MatchingPair<TableColumn> mp : generatedPairs.get(tx)) {
                mp.setFinalScore(overall1.get(mp.getMatchingPair().getFirst(), mp.getMatchingPair().getSecond()));
            }
        }

        for (TableColumn tc : generatedPairs.keySet()) {
            Map<String, List<MatchingPair>> uniqueIdenToPairs = new HashMap<>();
            List<MatchingPair> toRemove = new ArrayList<>();
            for (MatchingPair<TableColumn> mp : generatedPairs.get(tc)) {
                String uniqueString = evalInstance.getUniqueIdentifier(mp.getMatchingPair().getSecond()).toString();
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

        //SparseSimilarityMatrix<TableColumn> overall = (SparseSimilarityMatrix) overall1;
//        SparseSimilarityMatrix<TableColumn> overall = new SparseSimilarityMatrix(unmappedCols.size(), data.getDbpediaColSet().size());
//        Map<String, Integer> countR2 = new HashMap<>();
//        for (Table t : dbpediaPropsPerTable.keySet()) {
//            SimilarityMatrix<TableColumn> matrix = dbpediaPropsPerTable.get(t);
//            //System.out.println("XXX first dimension " + matrix.getFirstDimension());
//            for (TableColumn r1 : matrix.getFirstDimension()) {
//                if (r1.isKey()) {
//                    continue;
//                }
//                //System.out.println("XXX r1 " + r1);
//                for (TableColumn r2 : matrix.getMatches(r1)) {
//                    //might be good to see if we can get some better results if we allow different datatypes
//                    if (r1.getDataType() != r2.getDataType()) {
//                        continue;
//                    }
//                    //System.out.println("XXX r2 " + r2);
//                    if (countR2.containsKey(r1.getTable().getColumns().indexOf(r1) + "\t" + r2.getURI())) {
//                        Integer current = countR2.get(r1.getTable().getColumns().indexOf(r1) + "\t" + r2.getURI());
//                        current++;
//                        countR2.put(r1.getTable().getColumns().indexOf(r1) + "\t" + r2.getURI(), current);
//                    } else {
//                        countR2.put(r1.getTable().getColumns().indexOf(r1) + "\t" + r2.getURI(), 1);
//                    }
//
//                    if (overall.get(r1, r2) != null) {
//                        Double score = overall.get(r1, r2);
//                        score += matrix.get(r1, r2);
//                        overall.set(r1, r2, score);
//                        //System.out.println("XXX set score " + r1 + " - " + r2 + " - " + matrix.get(r1, r2));
//                    } else {
//                        overall.set(r1, r2, matrix.get(r1, r2));
//                        //System.out.println("XXX set new " + r1 + " - " + r2 + " - " + matrix.get(r1, r2));
//                    }
//                }
//            }
//        }
//
//        for (TableColumn r1 : overall.getFirstDimension()) {
//            for (TableColumn r2 : overall.getMatches(r1)) {
//                int occurence = countR2.get(r1.getTable().getColumns().indexOf(r1) + "\t" + r2.getURI());
//                overall.set(r1, r2, overall.get(r1, r2) / occurence);
//            }
//        }
        //overall.makeRowStochastic();
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
        //only take the best property? or take all?
////        SparseSimilarityMatrix<TableColumn> overall2 = new SparseSimilarityMatrix<>(overall.getFirstDimension().size(),
////                overall.getSecondDimension().size());
        //TODO: add the score of the correspondence
//        for (TableColumn r1 : overall.getFirstDimension()) {
//            //for (TableRow r2 : overall.getMatches(r1)) {
//            TableColumn best = overall.getBestPair(r1);
//            if (overallValue.get(r1, best) == null || best.getURI().equals("http://dbpedia.org/ontology/internationally")) {
//                continue;
//            }
//            if (overall.get(r1, best) < 1.0) {
//                System.out.println("too low: " + overall.get(r1, best));
//                continue;
//            }
//            double rel = (double) valueColumnCount.get(r1) / (double) dbpediaPropsPerTable.size();
//            if (overall.get(r1, best) == 1.0 && rel < 1) {
//                System.out.println("relation too low: " + overall.get(r1, best) + " - " + rel + " - " + valueColumnCount.get(r1) + " - " + (double) dbpediaPropsPerTable.size());
//                continue;
//            }
//            overall2.set(r1, best, overall.get(r1, best));
//            //System.out.println("XXX best " + r1 + " - " + best);
//            boolean inGS = false;
//            for (Object o : gs.getPropertyGoldStandard().keySet()) {
//                Integer i = Integer.parseInt(o.toString());
//                if (i == r1.getTable().getColumns().indexOf(r1)) {
//                    inGS = true;
//                    if (evalInstance.getUniqueIdentifier(best).equals(gs.getPropertyGoldStandard().get(o))) {
//                        System.out.println("final overall DBpedia prop: " + r1.getTable().getHeader() + "\t" + r1 + "\t" + evalInstance.getUniqueIdentifier(best) + "\t" + overall.get(r1, best) + "\t" + overallValue.get(r1, best) + "\t"
//                                + overallLabel.get(r1, best) + "\t" + labelColumnCount.get(r1) + "\t" + valueColumnCount.get(r1)
//                                + "\t" + dbpediaPropsPerTable.size() + "\t" + "\t" + propHeaderCount.get(evalInstance.getUniqueIdentifier(best).toString()) + "\tCORRECT");
//                    } else {
//                        System.out.println("final overall DBpedia prop: " + r1.getTable().getHeader() + "\t" + r1 + "\t" + evalInstance.getUniqueIdentifier(best) + "\t" + overall.get(r1, best) + "\t" + overallValue.get(r1, best) + "\t"
//                                + overallLabel.get(r1, best) + "\t" + labelColumnCount.get(r1) + "\t" + valueColumnCount.get(r1)
//                                + "\t" + dbpediaPropsPerTable.size() + "\t" + "\t" + propHeaderCount.get(evalInstance.getUniqueIdentifier(best).toString()) + "\tINCORRECT");
//                    }
//                }
//            }
//            if (!inGS) {
//                System.out.println("final overall DBpedia prop: " + r1.getTable().getHeader() + "\t" + r1 + "\t" + evalInstance.getUniqueIdentifier(best) + "\t" + overall.get(r1, best) + "\t" + overallValue.get(r1, best) + "\t"
//                        + overallLabel.get(r1, best) + "\t" + labelColumnCount.get(r1) + "\t" + valueColumnCount.get(r1)
//                        + "\t" + dbpediaPropsPerTable.size() + "\t" + "\t" + propHeaderCount.get(evalInstance.getUniqueIdentifier(best).toString()) + "\tINCORRECT2");
//            }
//        }
        StringBuilder b = new StringBuilder();
        //iterate thorugh all columns of the table
        for (TableColumn tc1 : unmappedCols) {
            String columnStats = "";
            columnStats += "stats t2t: " + tc1.getTable() + "\t" + tc1.getHeader() + "\t";
            Map<String, Integer> finalCountAll = new HashMap<>();
            Map<String, Integer> finalCountValFocus = new HashMap<>();
            Map<String, Integer> finalCountValFocusMajority = new HashMap<>();

            if (possibleMappings.get(tc1) != null && !possibleMappings.get(tc1).isEmpty()) {
                columnStats += possibleMappings.get(tc1) + "\t";
                for (String s : possibleMappings.get(tc1).keySet()) {
                    if (finalCountAll.containsKey(s)) {
                        int cur = finalCountAll.get(s);
                        cur += possibleMappings.get(tc1).get(s);
                        finalCountAll.put(s, cur);
                    } else {
                        finalCountAll.put(s, possibleMappings.get(tc1).get(s));
                    }
                    if (finalCountValFocus.containsKey(s)) {
                        int cur = finalCountValFocus.get(s);
                        cur += possibleMappings.get(tc1).get(s);
                        finalCountValFocus.put(s, cur);
                    } else {
                        finalCountValFocus.put(s, possibleMappings.get(tc1).get(s));
                    }
                    finalCountValFocusMajority.put(s, 1);
                }
            } else {
                columnStats += "0\t";
            }

            if (possibleMappingsCosine.get(tc1) != null && !possibleMappingsCosine.get(tc1).isEmpty()) {
                columnStats += possibleMappingsCosine.get(tc1) + "\t";
                for (String s : possibleMappingsCosine.get(tc1).keySet()) {
                    if (finalCountAll.containsKey(s)) {
                        int cur = finalCountAll.get(s);
                        cur += possibleMappingsCosine.get(tc1).get(s);
                        finalCountAll.put(s, cur);
                    } else {
                        finalCountAll.put(s, possibleMappingsCosine.get(tc1).get(s));
                    }
                    if (!finalCountValFocus.isEmpty()) {
                        if (finalCountValFocus.containsKey(s)) {
                            int cur = finalCountValFocus.get(s);
                            cur += possibleMappingsCosine.get(tc1).get(s);
                            finalCountValFocus.put(s, cur);
                        } else {
                            finalCountValFocus.put(s, possibleMappingsCosine.get(tc1).get(s));
                        }
                        if (!finalCountValFocusMajority.containsKey(s)) {
                            finalCountValFocusMajority.put(s, 1);
                        } else {
                            finalCountValFocusMajority.put(s, 2);
                        }
                    }
                }
            } else {
                columnStats += "0\t";
            }
            boolean foudnInHeader = false;
            if (possibleMappingsHeader.get(tc1) != null && !possibleMappingsHeader.get(tc1).isEmpty()) {

                columnStats += possibleMappingsHeader.get(tc1) + "\t";
                for (String s : possibleMappingsHeader.get(tc1).keySet()) {
                    if (finalCountAll.containsKey(s)) {
                        int cur = finalCountAll.get(s);
                        cur += possibleMappingsHeader.get(tc1).get(s);
                        finalCountAll.put(s, cur);
                    } else {
                        finalCountAll.put(s, possibleMappingsHeader.get(tc1).get(s));
                    }
                    if (!finalCountValFocus.isEmpty()) {
                        if (finalCountValFocus.containsKey(s)) {
                            int cur = finalCountValFocus.get(s);
                            cur += possibleMappingsHeader.get(tc1).get(s);
                            finalCountValFocus.put(s, cur);
                        } else {
                            finalCountValFocus.put(s, possibleMappingsHeader.get(tc1).get(s));
                        }
                        if (tc1.getHeader().toString().toLowerCase().contains(s.replace("http://dbpedia.org/ontology/", "").toLowerCase())
                                || s.replace("http://dbpedia.org/ontology/", "").toLowerCase().contains(tc1.getHeader().toString().toLowerCase())) {
                            foudnInHeader = true;
                        }

                        if (!finalCountValFocusMajority.containsKey(s)) {
                            finalCountValFocusMajority.put(s, 1);
                        } else if (finalCountValFocusMajority.get(s) == 1) {
                            finalCountValFocusMajority.put(s, 2);
                        } else {
                            finalCountValFocusMajority.put(s, 3);
                        }

                    }
                }
            } else {
                columnStats += "0\t";
            }
            int maxForProp = -1;
            String bestProp = "nan";
            for (String s : finalCountAll.keySet()) {
                if (finalCountAll.get(s) > maxForProp) {
                    maxForProp = finalCountAll.get(s);
                    bestProp = s;
                }
            }

            columnStats += bestProp + "\t";
            columnStats += maxForProp + "\t";
            columnStats += finalCountAll.size() + "\t";

            maxForProp = -1;
            bestProp = "nan";
            for (String s : finalCountValFocus.keySet()) {
                if (finalCountValFocus.get(s) > maxForProp) {
                    maxForProp = finalCountValFocus.get(s);
                    bestProp = s;
                }
            }

            maxForProp = -1;
            bestProp = "nan";
            for (String s : finalCountValFocusMajority.keySet()) {
                if (finalCountValFocusMajority.get(s) > maxForProp) {
                    maxForProp = finalCountValFocusMajority.get(s);
                    bestProp = s;
                }
            }

            if (result.getClassMappings() != null && result.getClassMappings().iterator().hasNext()) {
                Table dbpediaMappedTable = result.getClassMappings().iterator().next().getSecond();
                for (TableColumn tc2 : dbpediaMappedTable.getColumns()) {
                    if (evalInstance.getUniqueIdentifier(tc2).equals(bestProp) || tc2.getURI().equals(bestProp)) {
                        if ((propHeaderCount.get(tc2.getURI()) != null && propHeaderCount.get(tc2.getURI()) > 9)
                                || (propHeaderCount.get(evalInstance.getUniqueIdentifier(tc2).toString()) != null && propHeaderCount.get(evalInstance.getUniqueIdentifier(tc2).toString()) > 9)) {
                            if (!foudnInHeader) {
                                //System.out.println("filtered freq file: " + tc1 + " - " + tc2);
                                continue;
                            }
                        }
                        System.out.println("set matrix! " + tc1 + " - " + tc2 + " - " + (double) maxForProp);
                        indirectCombined.set(tc1, tc2, (double) maxForProp);
                    }
                }
            }

            columnStats += bestProp + "\t";
            columnStats += maxForProp + "\t";
            columnStats += finalCountValFocus.size() + "\t";

            boolean found = false;
            for (Object o1 : gs.getPropertyGoldStandard().keySet()) {
                int indexWT = (int) o1;
                if (indexWT == tc1.getTable().getColumns().indexOf(tc1)) {
                    columnStats += gs.getPropertyGoldStandard().get(o1) + "\t";
                    found = true;
                }
            }
            if (!found) {
                columnStats += "0\t";
            }
            //doesn't care at the moment
//                        found = false;
//                        boolean overlap = false;
//                        for (Correspondence<TableColumn> cor : result.getPropertyMappings()) {
//                            if (cor.getFirst().equals(tc1)) {
//                                columnStats += cor.getSecond().getURI() + "\t";
//                                found = true;
//                                if (possibleMappingsCosine.get(tc1).containsKey(cor.getSecond().getURI())
//                                        || possibleMappingsHeader.get(tc1).containsKey(cor.getSecond().getURI())
//                                        || possibleMappings.get(tc1).containsKey(cor.getSecond().getURI())) {
//                                    overlap = true;
//                                }
//                            }
//                        }
//                        if (!found) {
//                            columnStats += "0\t";
//                        }
//                        columnStats += mapped.size() + "\t";
//                        if (overlap) {
//                            columnStats += "over\t";
//                        } else {
//                            columnStats += "nover\t";
//                        }

            System.out.println(columnStats);
        }

        for (TableColumn tc1
                : possibleMappingsCosine.keySet()) {
            if (possibleMappingsCosine.get(tc1) == null || possibleMappingsCosine.get(tc1).isEmpty()) {
                continue;
            }
            System.out.println("final candidates cosine: " + tc1.getTable() + " - " + tc1 + " - " + possibleMappingsCosine.get(tc1));

            for (Object o1 : gs.getPropertyGoldStandard().keySet()) {
                int indexWT = (int) o1;
                if (indexWT == tc1.getTable().getColumns().indexOf(tc1)) {
                    System.out.println("final candidates cosine correct: " + gs.getPropertyGoldStandard().get(o1));
                }
            }
            for (Correspondence<TableColumn> cor : result.getPropertyMappings()) {
                if (cor.getFirst().equals(tc1)) {
                    System.out.println("final candidates cosine before: " + tc1.getTable() + " - " + tc1 + " - " + cor.getSecond().getURI());
                }
            }
        }

        for (TableColumn tc1
                : possibleMappings.keySet()) {
            if (possibleMappings.get(tc1) == null || possibleMappings.get(tc1).isEmpty()) {
                continue;
            }
            System.out.println("final candidates: " + tc1.getTable() + " - " + tc1 + " - " + possibleMappings.get(tc1));
            for (Object o1 : gs.getPropertyGoldStandard().keySet()) {
                int indexWT = (int) o1;
                if (indexWT == tc1.getTable().getColumns().indexOf(tc1)) {
                    System.out.println("final candidates correct: " + tc1.getTable() + " - " + tc1 + " - " + gs.getPropertyGoldStandard().get(o1));
                }
            }
            for (Correspondence<TableColumn> cor : result.getPropertyMappings()) {
                if (cor.getFirst().equals(tc1)) {
                    System.out.println("final candidates before: " + tc1.getTable() + " - " + tc1 + " - " + cor.getSecond().getURI());
                }
            }
        }

        for (TableColumn tc1
                : possibleMappingsHeader.keySet()) {
            if (possibleMappingsHeader == null || possibleMappingsHeader.get(tc1).isEmpty()) {
                continue;
            }
            System.out.println("final candidates header: " + tc1.getTable() + " - " + tc1 + " - " + possibleMappingsHeader.get(tc1));
            for (Object o1 : gs.getPropertyGoldStandard().keySet()) {
                int indexWT = (int) o1;
                if (indexWT == tc1.getTable().getColumns().indexOf(tc1)) {
                    System.out.println("final candidates header correct: " + tc1.getTable() + " - " + tc1 + " - " + gs.getPropertyGoldStandard().get(o1));
                }
            }
            for (Correspondence<TableColumn> cor : result.getPropertyMappings()) {
                if (cor.getFirst().equals(tc1)) {
                    System.out.println("final candidates header before: " + tc1.getTable() + " - " + tc1 + " - " + cor.getSecond().getURI());
                }
            }
        }

        System.out.println(
                "final candidates # mapped: " + mapped.size());
        System.out.println(
                "possible cols: " + possibleCols);

        indirectCombined.normalize();
        System.out.println("domi before return " + overall1.getFirstDimension().size() + " - " + overall1.getSecondDimension().size());
        for (TableColumn c : overall1.getFirstDimension()) {
            for (TableColumn d : overall1.getMatches(c)) {
                System.out.println("domi dimensions: " + c + " - " + d.getURI() + " - " + overall1.get(c, d));
            }
        }
        return generatedPairs;
    }

}
