package de.dwslab.T2K.matching.dbpedia.logging;

///** Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package de.mannheim.uni.matching.dbpedia.logging;
//
//import com.google.common.primitives.Doubles;
//import de.mannheim.uni.matching.correspondences.Correspondence;
//import static de.mannheim.uni.matching.dbpedia.algorithm.WebTableToDBpediaComponentProcess.calculateCosineSimilarity;
//import de.mannheim.uni.matching.dbpedia.model.GoldStandard;
//import de.mannheim.uni.matching.dbpedia.model.MatchingResult;
//import de.mannheim.uni.matching.dbpedia.model.TableRow;
//import de.mannheim.uni.matching.dbpedia.model.adapters.DBpediaPropertyAdapter;
//import de.mannheim.uni.matching.dbpedia.model.adapters.conext.StopWordRemover;
//import de.mannheim.uni.matching.evaluation.EvaluationAdapter;
//import de.mannheim.uni.similarity.matrix.SimilarityMatrix;
//import de.mannheim.uni.tableprocessor.model.Table;
//import de.mannheim.uni.tableprocessor.model.TableColumn;
//import de.uni_mannheim.informatik.dws.t2k.normalisation.StringNormalizer;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.TreeMap;
//import org.apache.commons.math3.stat.inference.TTest;
//
///**
// *
// * @author dritze
// */
//public class PropertyCorrespondenceWriter {
//
//    public void writeCorrespondencesWithFeatures(GoldStandard gs, MatchingResult result, SimilarityMatrix<TableColumn> props, 
//            SimilarityMatrix<TableColumn> labelSimilarity, SimilarityMatrix<TableColumn> props) {
//        EvaluationAdapter<TableColumn> evalInstance = new DBpediaPropertyAdapter(gs.getPropertyCanoniser());
//
//        for (Correspondence<TableColumn> cor : result.getPropertyMappings()) {
//
//            if (!cor.isCorrect()) {
//                String checkIncorrect = "";
//                checkIncorrect += cor.getFirst().getTable().getHeader() + "\t";
//                checkIncorrect += "0\t";
//                checkIncorrect += cor.getFirst().getDataType() + "\t";
//                checkIncorrect += cor.getFirst().getHeader().toString() + "\t";
//                checkIncorrect += cor.getFirst().getValues().get(cor.getFirst().getTable().getColumns().indexOf(cor.getFirst())) + "\t";
//                checkIncorrect += evalInstance.getUniqueIdentifier(cor.getSecond()) + "\t";
//                checkIncorrect += cor.getSecond().getValues().get(cor.getSecond().getTable().getColumns().indexOf(cor.getSecond())) + "\t";
//
//                Double propValueToWrite = null;
//                if (props.get(cor.getFirst(), cor.getSecond()) == null) {
//                    for (TableColumn t : props.getMatches(cor.getFirst())) {
//                        if (t.getURI().equals(cor.getSecond().getURI())) {
//                            propValueToWrite = props.get(cor.getFirst(), t);
//                        }
//                    }
//                } else {
//                    propValueToWrite = props.get(cor.getFirst(), cor.getSecond());
//                }
//                checkIncorrect += propValueToWrite + "\t";
//                checkIncorrect += stats.get(props).getMean() + "\t";
//
//                Double labelValueToWrite = null;
//                if (labelSimilarity.get(cor.getFirst(), cor.getSecond()) == null) {
//                    for (TableColumn t : labelSimilarity.getMatches(cor.getFirst())) {
//                        if (t.getURI().equals(cor.getSecond().getURI())) {
//                            labelValueToWrite = labelSimilarity.get(cor.getFirst(), t);
//                        }
//                    }
//                } else {
//                    labelValueToWrite = labelSimilarity.get(cor.getFirst(), cor.getSecond());
//                }
//                checkIncorrect += labelValueToWrite + "\t";
//
//                checkIncorrect += stats.get(labelSimilarity).getMean() + "\t";
//                checkIncorrect += kurt.get(cor.getFirst(), cor.getSecond()) + "\t";
//                checkIncorrect += stats.get(kurt).getMean() + "\t";
//                checkIncorrect += freqDT.get(cor.getFirst(), cor.getSecond()) + "\t";
//                checkIncorrect += classStatsMatrix.get(cor.getFirst(), cor.getSecond()) + "\t";
//                checkIncorrect += propStatsMatrix.get(cor.getFirst(), cor.getSecond()) + "\t";
//                checkIncorrect += countDT.get(cor.getFirst(), cor.getSecond()) + "\t";
//                checkIncorrect += propertySim.get(cor.getFirst(), cor.getSecond()) + "\t";
//                checkIncorrect += cor.getFirst().getColumnStatistic().getKurtosis() + "\t";
//
////                        checkIncorrect += cor.getFirst().getColumnStatistic().getMinimalValue() + "\t";
////                        checkIncorrect += cor.getFirst().getColumnStatistic().getMaximalValue() + "\t";
////                        checkIncorrect += cor.getFirst().getColumnStatistic().getAverage() + "\t";
////                        checkIncorrect += cor.getFirst().getColumnStatistic().getStandardDeviation() + "\t";
////                        checkIncorrect += cor.getFirst().getColumnStatistic().getVariance() + "\t";
//                if (cor.getSecond().getEquivBefore() != null) {
////                            System.out.println("equi used: " + cor.getSecond().getEquivBefore().getURI() + " class: " + cor.getSecond().getTable() + " table ID " + cor.getFirst().getTable().getHeader());
////                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getMinimalValue() + "\t";
////                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getMaximalValue() + "\t";
////                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getAverage() + "\t";
////                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getStandardDeviation() + "\t";
////                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getVariance() + "\t";
////                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getLowerPercentile() + "\t";
////                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getUpperPercentile() + "\t";
//                    if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
//                        if ((double) cor.getSecond().getEquivBefore().getColumnStatistic().getMinimalValue() <= cor.getFirst().getColumnStatistic().getAverage()
//                                && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getEquivBefore().getColumnStatistic().getMaximalValue()) {
//                            checkIncorrect += "1\t";
//                        } else {
//                            checkIncorrect += "-1.0\t";
//                        }
//                        if ((double) cor.getSecond().getEquivBefore().getColumnStatistic().getLowerPercentile() <= cor.getFirst().getColumnStatistic().getAverage()
//                                && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getEquivBefore().getColumnStatistic().getUpperPercentile()) {
//                            checkIncorrect += "1\t";
//                        } else {
//                            checkIncorrect += "-1.0\t";
//                        }
//                    } else {
//                        checkIncorrect += "0\t";
//                        checkIncorrect += "0\t";
//                    }
//                } else {
////                            Correspondence<Table> decidedClass = result.getClassMappings().iterator().next();
////                            TableColumn propertyUsed = decidedClass.getSecond().getColumn(cor.getSecond().getHeader().toString() + " table ID " + cor.getFirst().getTable().getHeader());
////                            System.out.println("no equi used: " + cor.getSecond().getURI() + " class: " + cor.getSecond().getTable());
//////                            checkIncorrect += cor.getSecond().getColumnStatistic().getMinimalValue() + "\t";
//////                            checkIncorrect += cor.getSecond().getColumnStatistic().getMaximalValue() + "\t";
//////                            checkIncorrect += cor.getSecond().getColumnStatistic().getAverage() + "\t";
//////                            checkIncorrect += cor.getSecond().getColumnStatistic().getStandardDeviation() + "\t";
//////                            checkIncorrect += cor.getSecond().getColumnStatistic().getVariance() + "\t";
//////                            checkIncorrect += cor.getSecond().getColumnStatistic().getLowerPercentile() + "\t";
//////                            checkIncorrect += cor.getSecond().getColumnStatistic().getUpperPercentile() + "\t";
//                    if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
//                        if ((double) cor.getSecond().getColumnStatistic().getMinimalValue() <= cor.getFirst().getColumnStatistic().getAverage()
//                                && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getColumnStatistic().getMaximalValue()) {
//                            checkIncorrect += "1\t";
//                        } else {
//                            checkIncorrect += "-1.0\t";
//                        }
//                        if ((double) cor.getSecond().getColumnStatistic().getLowerPercentile() <= cor.getFirst().getColumnStatistic().getAverage()
//                                && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getColumnStatistic().getUpperPercentile()) {
//                            checkIncorrect += "1\t";
//                        } else {
//                            checkIncorrect += "-1.0\t";
//                        }
//                    } else {
//                        checkIncorrect += "0\t";
//                        checkIncorrect += "0\t";
//                    }
//                }
//                if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
//                    TTest ttest = new TTest();
//                    List<Double> tableValues = new ArrayList<>();
//                    for (Object o : cor.getFirst().getValues().values()) {
//                        if (o instanceof List) {
//                            List<Double> list = (List<Double>) o;
//                            tableValues.addAll(list);
//                        } else {
//                            Double value = (double) o;
//                            tableValues.add(value);
//                        }
//                    }
//                    double[] valuesTable = Doubles.toArray(tableValues);
//
//                    List<Double> propValues = new ArrayList<>();
//                    for (Object o : cor.getSecond().getValues().values()) {
//                        if (o instanceof List) {
//                            List<Double> list = (List<Double>) o;
//                            propValues.addAll(list);
//                        } else {
//                            Double value = (double) o;
//                            propValues.add(value);
//                        }
//                    }
//                    double[] valuesProp = Doubles.toArray(propValues);
//                    try {
//                        checkIncorrect += ttest.t(valuesTable, valuesProp) + "\t";
//                    } catch (Exception e) {
//                        checkIncorrect += "-5\t";
//                    }
//                    double countIntTable = 0;
//                    double countIntDBpedia = 0;
//                    for (Object o : cor.getSecond().getValues().values()) {
//                        try {
//                            if (!(o instanceof List) && (o instanceof Double)) {
//                                if ((double) o % 1 == 0) {
//                                    countIntDBpedia++;
//                                }
//                            }
//                        } catch (Exception e) {
//                            System.out.println("error 1" + e);
//                        }
//                    }
//                    for (Object o : cor.getFirst().getValues().values()) {
//                        try {
//                            if (!(o instanceof List) && (o instanceof Double)) {
//                                if ((double) o % 1 == 0) {
//                                    countIntTable++;
//                                }
//                            }
//                        } catch (Exception e) {
//                            System.out.println("error 2" + e);
//                        }
//                    }
//                    countIntDBpedia = countIntDBpedia / (double) cor.getSecond().getValues().size();
//                    countIntTable = countIntTable / (double) cor.getFirst().getValues().size();
////                            if(countIntDBpedia > 0.9 && countIntTable < 0.9) {
////                                checkIncorrect += "0\t";
////                            }
////                            else {
////                                checkIncorrect += "1\t";
////                            }
//                    double diff = countIntDBpedia - countIntTable;
//                    checkIncorrect += diff + "\t";
//                    System.out.println("counts: " + countIntDBpedia + "  vs. " + countIntTable + " sizes " + cor.getSecond().getValues().size() + " vs. " + cor.getFirst().getValues().size());
//                } else {
//                    checkIncorrect += "-5\t";
////                            checkIncorrect += "-8\t";
//                    checkIncorrect += "0\t";
//                }
//
//                double distValueDBpedia = (double) cor.getSecond().getColumnStatistic().getDistinctValues() / (double) cor.getSecond().getValues().size();
//                double distValueDBpediaList = (double) cor.getSecond().getColumnStatistic().getDistinctValuesWithoutLists() / (double) cor.getSecond().getValues().size();
//                double distValueWT = (double) cor.getFirst().getColumnStatistic().getDistinctValuesWithoutLists() / (double) cor.getFirst().getValues().size();
//                double diff1, diff2;
//                diff1 = distValueDBpedia - distValueWT;
//                diff2 = distValueDBpediaList - distValueWT;
//                checkIncorrect += diff1 + "\t";
//                checkIncorrect += diff2 + "\t";
//
//                if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.string) {
//
//                    Collection<Object> values1 = cor.getSecond().getValues().values();
//                    Collection<Object> allValues = new HashSet<>();
//
//                    if (cor.getSecond().getEquivBefore() != null) {
//                        //System.out.println("equiv props: " + cor.getSecond().getURI() + " -- " + cor.getSecond().getEquivBefore().getURI());
//                        Collection<Object> values2 = cor.getSecond().getEquivBefore().getValues().values();
//                        allValues.addAll(values2);
//                    } else {
//                        allValues.addAll(values1);
//                    }
//
//                    Map<String, Integer> countsValuesDBpedia = new TreeMap<>();
//                    for (Object o : allValues) {
//                        if (o instanceof List) {
//                            List<String> list = (List<String>) o;
//                            try {
//                                for (String value : list) {
//                                    if (value.contains(" ") || value.contains("_")) {
//                                        String[] splittedValues;
//                                        if (value.contains(" ")) {
//                                            splittedValues = value.split("\\s");
//                                        } else {
//                                            splittedValues = value.split("_");
//                                        }
//                                        for (String s : splittedValues) {
//                                            s = s.replace("(", "");
//                                            if (!StopWordRemover.isStopWord(s)) {
//                                                if (countsValuesDBpedia.containsKey(s)) {
//                                                    Integer current = countsValuesDBpedia.get(s);
//                                                    current++;
//                                                    countsValuesDBpedia.put(s, current);
//                                                } else {
//                                                    countsValuesDBpedia.put(s, 1);
//                                                }
//                                            }
//                                        }
//                                    } else {
//                                        value = StringNormalizer.normaliseValue(value, true);
//                                        if (countsValuesDBpedia.containsKey(value)) {
//                                            Integer current = countsValuesDBpedia.get(value);
//                                            current++;
//                                            countsValuesDBpedia.put(value, current);
//                                        } else {
//                                            countsValuesDBpedia.put(value, 1);
//                                        }
//                                    }
//                                }
//                            } catch (Exception e) {
//                            }
//                        } else if (o instanceof String) {
//                            String value = (String) o;
//                            value = StringNormalizer.normaliseValue(value, true);
//                            if (value.contains(" ") || value.contains("_")) {
//                                String[] splittedValues;
//                                if (value.contains(" ")) {
//                                    splittedValues = value.split("\\s");
//                                } else {
//                                    splittedValues = value.split("_");
//                                }
//                                for (String s : splittedValues) {
//                                    if (!StopWordRemover.isStopWord(s)) {
//                                        if (countsValuesDBpedia.containsKey(s)) {
//                                            Integer current = countsValuesDBpedia.get(s);
//                                            current++;
//                                            countsValuesDBpedia.put(s, current);
//                                        } else {
//                                            countsValuesDBpedia.put(s, 1);
//                                        }
//                                    }
//                                }
//                            } else {
//                                if (countsValuesDBpedia.containsKey(value)) {
//                                    Integer current = countsValuesDBpedia.get(value);
//                                    current++;
//                                    countsValuesDBpedia.put(value, current);
//                                } else {
//                                    countsValuesDBpedia.put(value, 1);
//                                }
//                            }
//                        }
//                    }
//                    //System.out.println("count values: " + countsValues);
//
//                    double sum = 0.0;
//
//                    Map<String, Integer> countsValuesWT = new TreeMap<>();
//
//                    //System.out.println("values table: " + cor.getFirst().getValues().values());
//                    for (Object o : cor.getFirst().getValues().values()) {
//                        //System.out.println(o);
//                        if (o instanceof List) {
//                            List<String> list = (List<String>) o;
//                            for (String s : list) {
//                                if (s.contains(",")) {
//                                    s = s.split(",")[0];
//                                    s = s.trim();
//                                }
//                                if (s.contains("/")) {
//                                    s = s.split("/")[0];
//                                    s = s.trim();
//                                }
//                                if (s.contains(" ")) {
//                                    String[] splittedValues = s.split("\\s");
//                                    for (String t : splittedValues) {
//                                        if (!StopWordRemover.isStopWord(t)) {
//                                            if (countsValuesDBpedia.containsKey(t)) {
//                                                //sum += countsValuesDBpedia.get(t);
//                                                if (countsValuesWT.containsKey(t)) {
//                                                    Integer current = countsValuesWT.get(t);
//                                                    current++;
//                                                    countsValuesWT.put(t, current);
//                                                } else {
//                                                    countsValuesWT.put(t, 1);
//                                                }
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    if (countsValuesDBpedia.containsKey(s)) {
//                                        //sum += countsValuesDBpedia.get(s);
//                                        if (countsValuesWT.containsKey(s)) {
//                                            Integer current = countsValuesWT.get(s);
//                                            current++;
//                                            countsValuesWT.put(s, current);
//                                        } else {
//                                            countsValuesWT.put(s, 1);
//                                        }
//                                    }
//                                }
//                            }
//                        } else {
//                            String s = (String) o;
//                            if (s.contains(",")) {
//                                s = s.split(",")[0];
//                                s = s.trim();
//                            }
//                            if (s.contains("/")) {
//                                s = s.split("/")[0];
//                                s = s.trim();
//                            }
//                            s = s.trim();
//                            if (s.contains(" ")) {
//                                String[] splittedValues = s.split("\\s");
//                                for (String t : splittedValues) {
//                                    if (!StopWordRemover.isStopWord(t)) {
//                                        if (countsValuesDBpedia.containsKey(t)) {
//                                            //sum += countsValuesDBpedia.get(t);
//                                            if (countsValuesWT.containsKey(t)) {
//                                                Integer current = countsValuesWT.get(t);
//                                                current++;
//                                                countsValuesWT.put(t, current);
//                                            } else {
//                                                countsValuesWT.put(t, 1);
//                                            }
//                                        }
//                                    }
//                                }
//                            } else {
//                                if (countsValuesDBpedia.containsKey(s)) {
//                                    //sum += countsValuesDBpedia.get(s);
//                                    if (countsValuesWT.containsKey(s)) {
//                                        Integer current = countsValuesWT.get(s);
//                                        current++;
//                                        countsValuesWT.put(s, current);
//                                    } else {
//                                        countsValuesWT.put(s, 1);
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    //System.out.println("calculate cosine: " + cor.getSecond().getURI() + " of class " + cor.getSecond().getTable().getHeader() + " with " + cor.getFirst().getHeader());
//                    checkIncorrect += calculateCosineSimilarity(countsValuesDBpedia, countsValuesWT) + "\t";
//
////                            sum = 0.0;
////
////                            try {
////                                //SoftballLeague_currentSeason    {2015 Men's Softball World Championship=1, 2013 NCAA Division III Softball Championship=1, 2013 NCAA Division II Softball Championship=1}
////                                BufferedReader read = new BufferedReader(new FileReader(new File("/backup-local-home/dritze/newGS/recognizer/DBpedia/dbpediaStatsString.csv")));
////                                String wholeLine = read.readLine();
////                                String className = "";
////                                for (Correspondence<Table> corTable : result.getClassMappings()) {
////                                    className = corTable.getSecond().getHeader();
////                                    className = className.replace(".tar.gz", "");
////                                    className = className.replace(".csv", "");
////                                    className = className.replace(".gz", "");
////                                }
////                                Set<String> urisToCheck = new HashSet<>();
////                                Canoniser c = new Canoniser();
////                                Collection<List<String>> equiProps = c.loadEquivalentResourcesExternal(getEvaluationParameters().getEquivalentPropertiesLocation());
////                                for (List l : equiProps) {
////                                    if (l.contains(cor.getSecond().getURI())) {
////                                        //System.out.println("uris to check! " + l);
////                                        urisToCheck.addAll(l);
////                                    }
////                                }
////
////                                //System.out.println("class prop name second: " + className + "_" + cor.getSecond().getURI().replace("http://dbpedia.org/ontology/", ""));
////                                //System.out.println("class prop name equi: " + className + "_" + evalInstance.getUniqueIdentifier(cor.getSecond()).toString().replace("http://dbpedia.org/ontology/", ""));
////                                Set<String> lineWithProp = new HashSet<>();
////                                while (wholeLine != null) {
////                                    if (!urisToCheck.isEmpty()) {
////                                        //System.out.println("not empty");
////                                        for (String s : urisToCheck) {
////                                            System.out.println("check " + s);
////                                            if (wholeLine.startsWith(className + "_" + s.replace("http://dbpedia.org/ontology/", "") + "\t")
////                                                    || wholeLine.startsWith(className + "_" + s.replace("http://dbpedia.org/ontology/", "") + "_label\t")) {
////                                                lineWithProp.add(wholeLine);
////                                                //    System.out.println("found");
////                                            }
////                                        }
////                                    } else {
////                                        if (wholeLine.startsWith(className + "_" + cor.getSecond().getURI().replace("http://dbpedia.org/ontology/", "") + "\t")
////                                                || wholeLine.startsWith(className + "_" + cor.getSecond().getURI().replace("http://dbpedia.org/ontology/", "") + "_label\t")) {
////                                            lineWithProp.add(wholeLine);
////                                            // System.out.println("found single: " + cor.getSecond().getURI());
////                                            break;
////                                        }
////                                        //continue;
////                                    }
////
//////                                    if (wholeLine.startsWith(className + "_" + evalInstance.getUniqueIdentifier(cor.getSecond()).toString().replace("http://dbpedia.org/ontology/", ""))) {
//////                                        secondLineWithProp = wholeLine;
//////                                        //break;
//////                                    }
////                                    wholeLine = read.readLine();
////                                }
////                                //System.out.println("line with prop " +lineWithProp);
////                                if (!lineWithProp.isEmpty()) {
////                                    for (String check : lineWithProp) {
////                                        String[] countArray = new String[0];
////                                        String[] countArray2 = new String[0];
////                                        String[] line = check.split("\t");
////                                        countArray = line[1].split(",");
////                                        Map<String, Integer> counts = new HashMap<>();
////                                        for (String s : countArray) {
////                                            s = s.replace("}", "");
////                                            s = s.replace("{", "");
////                                            String[] entry = s.split("=");
////                                            counts.put(StringNormalizer.normaliseValue(entry[0], true), Integer.parseInt(entry[1]));
////                                            //System.out.println("entry: " + StringNormalizer.normaliseValue(entry[0], true));
////                                        }
////                                        for (String s : countArray2) {
////                                            s = s.replace("}", "");
////                                            s = s.replace("{", "");
////                                            String[] entry = s.split("=");
////                                            counts.put(StringNormalizer.normaliseValue(entry[0], true), Integer.parseInt(entry[1]));
////                                            //System.out.println(StringNormalizer.normaliseValue(entry[0],true));
////                                        }
////                                        for (Object o : cor.getFirst().getValues().values()) {
////                                            //System.out.println(o);
////                                            if (o instanceof List) {
////                                                List<String> list = (List<String>) o;
////                                                for (String s : list) {
////                                                    if (counts.containsKey(s)) {
////                                                        sum += counts.get(s);
////                                                    }
////                                                    break;
////                                                }
////                                            } else {
////                                                String s = (String) o;
////                                                if (s.contains(",")) {
////                                                    s = s.split(",")[0];
////                                                    s = s.trim();
////                                                }
////                                                if (s.contains("/")) {
////                                                    s = s.split("/")[0];
////                                                    s = s.trim();
////                                                }
////                                                if (counts.containsKey(s)) {
////                                                    sum += counts.get(s);
////                                                }
////                                            }
////                                        }
////                                    }
////                                }
////                            } catch (Exception e) {
////                                e.printStackTrace();
////                            }
////                            checkIncorrect += sum + "\t";
//                } else {
////                            checkIncorrect += -1.0 + "\t";
////                            checkIncorrect += -1.0 + "\t";
//                    checkIncorrect += "0\t";
//                }
//
//                //checkIncorrect += cor.getFirst().getValues().size() + "\t";
//                if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.date) {
//                    TTest ttest = new TTest();
//                    List<Date> tableValues = new ArrayList<>();
//                    for (Object o : cor.getFirst().getValues().values()) {
//                        if (o instanceof List) {
//                            List<Date> list = (List<Date>) o;
//                            tableValues.addAll(list);
//                        } else {
//                            Date value = (Date) o;
//                            tableValues.add(value);
//                        }
//                    }
//                    List<Date> dbpediaValues = new ArrayList<>();
//                    for (Object o : cor.getSecond().getValues().values()) {
//                        if (o instanceof List) {
//                            List<Date> list = (List<Date>) o;
//                            dbpediaValues.addAll(list);
//                        } else {
//                            Date value = (Date) o;
//                            dbpediaValues.add(value);
//                        }
//                    }
//                    Collections.sort(tableValues);
//                    Date median;
//                    int middle = tableValues.size() / 2;
//                    median = tableValues.get(middle);
//
//                    Collections.sort(dbpediaValues);
//                    Date minDate = dbpediaValues.get(0);
//                    Date maxDate = dbpediaValues.get(dbpediaValues.size() - 1);
//
//                    if (minDate.before(median) && median.before(maxDate)) {
//                        checkIncorrect += "1.0\t";
//                    } else {
//                        checkIncorrect += "0.0\t";
//                    }
//
//                } else {
//                    checkIncorrect += "-5\t";
//                }
//
//                Object correctKey = cor.getCorrectValue() + "\t";
//                if (correctKey != null) {
//                    checkIncorrect += correctKey + "\t";
//                    checkIncorrect += "correctExists\t";
//                } else {
//                    checkIncorrect += "correctNotExists\t";
//                }
//
//                List<TableColumn> correctCols = new ArrayList<>();
//                for (TableColumn cols : getData().getDbpediaColSet()) {
//                    if (cols.getURI().equals(correctKey)) {
//                        correctCols.add(cols);
//                    }
//                }
//
////                        for(TableColumn col : correctCols) {
////                            if(freqAll.getMatches(cor.getFirst()).contains(col)) {
////                                checkIncorrect += "in freq all: " +freqAll.get(cor.getFirst(),col)+ "\t";
////                            }
////                        }
//                for (Correspondence<Table> corTable : result.getClassMappings()) {
//                    //checkIncorrect += corTable.getSecond() + "---" + corTable.isCorrect() + "\t";
//                    checkIncorrect += corTable.isCorrect() + "\t";
//                }
//                int countInst = 0;
//                for (Correspondence<TableRow> corProp : result.getInstanceMappings()) {
//                    //checkIncorrect += corProp.getFirst() + "---" + corProp.getSecond() + "---" + corProp.isCorrect() + "\t";
//                    countInst++;
//                }
//                checkIncorrect += (double) countInst / (double) result.getInstanceMappings().size();
//
//                System.out.println("propProp check property: " + checkIncorrect);
//            } else {
//                String correct = "";
//                correct += cor.getFirst().getTable().getHeader() + "\t";
//                correct += "1\t";
//                correct += cor.getFirst().getDataType() + "\t";
//                correct += cor.getFirst().getHeader().toString() + "\t";
//                correct += cor.getFirst().getValues().get(cor.getFirst().getTable().getColumns().indexOf(cor.getFirst())) + "\t";
//                correct += evalInstance.getUniqueIdentifier(cor.getSecond()) + "\t";
//                correct += cor.getSecond().getValues().get(cor.getSecond().getTable().getColumns().indexOf(cor.getSecond())) + "\t";
//
//                Double propValueToWrite = null;
//                if (props.get(cor.getFirst(), cor.getSecond()) == null) {
//                    for (TableColumn t : props.getMatches(cor.getFirst())) {
//                        if (t.getURI().equals(cor.getSecond().getURI())) {
//                            propValueToWrite = props.get(cor.getFirst(), t);
//                        }
//                    }
//                } else {
//                    propValueToWrite = props.get(cor.getFirst(), cor.getSecond());
//                }
//                correct += propValueToWrite + "\t";
//                correct += stats.get(props).getMean() + "\t";
//
//                Double labelValueToWrite = null;
//                if (labelSimilarity.get(cor.getFirst(), cor.getSecond()) == null) {
//                    for (TableColumn t : labelSimilarity.getMatches(cor.getFirst())) {
//                        if (t.getURI().equals(cor.getSecond().getURI())) {
//                            labelValueToWrite = labelSimilarity.get(cor.getFirst(), t);
//                        }
//                    }
//                } else {
//                    labelValueToWrite = labelSimilarity.get(cor.getFirst(), cor.getSecond());
//                }
//                correct += labelValueToWrite + "\t";
//
//                // System.out.println("label score: " + cor.getFirst() + " . " + cor.getSecond().getURI() + " of class " + cor.getSecond().getTable().getHeader());
//                for (TableColumn o : labelSimilarity.getFirstDimension()) {
//                    for (TableColumn t : labelSimilarity.getMatches(o)) {
//                        //System.out.println("score in label: " + o.getHeader() + " with " + t.getURI() + " from " + t.getTable().getHeader() + " score: " + props.get(o, t));
//                    }
//                }
//
//                correct += stats.get(labelSimilarity).getMean() + "\t";
//                correct += kurt.get(cor.getFirst(), cor.getSecond()) + "\t";
//                correct += stats.get(kurt).getMean() + "\t";
//                correct += freqDT.get(cor.getFirst(), cor.getSecond()) + "\t";
//                correct += classStatsMatrix.get(cor.getFirst(), cor.getSecond()) + "\t";
//                correct += propStatsMatrix.get(cor.getFirst(), cor.getSecond()) + "\t";
//                correct += countDT.get(cor.getFirst(), cor.getSecond()) + "\t";
//                correct += propertySim.get(cor.getFirst(), cor.getSecond()) + "\t";
//                correct += cor.getFirst().getColumnStatistic().getKurtosis() + "\t";
//
////                        correct += cor.getFirst().getColumnStatistic().getMinimalValue() + "\t";
////                        correct += cor.getFirst().getColumnStatistic().getMaximalValue() + "\t";
////                        correct += cor.getFirst().getColumnStatistic().getAverage() + "\t";
////                        correct += cor.getFirst().getColumnStatistic().getStandardDeviation() + "\t";
////                        correct += cor.getFirst().getColumnStatistic().getVariance() + "\t";
////
//                if (cor.getSecond().getEquivBefore() != null) {
////                            System.out.println("equi used: " + cor.getSecond().getEquivBefore().getURI() + " class: " + cor.getSecond().getTable() + " table ID " + cor.getFirst().getTable().getHeader());
////                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getMinimalValue() + "\t";
////                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getMaximalValue() + "\t";
////                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getAverage() + "\t";
////                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getStandardDeviation() + "\t";
////                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getVariance() + "\t";
////                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getLowerPercentile() + "\t";
////                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getUpperPercentile() + "\t";
//                    if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
//                        if ((double) cor.getSecond().getEquivBefore().getColumnStatistic().getMinimalValue() <= cor.getFirst().getColumnStatistic().getAverage()
//                                && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getEquivBefore().getColumnStatistic().getMaximalValue()) {
//                            correct += "1\t";
//                        } else {
//                            correct += "-1.0\t";
//                        }
//                        if ((double) cor.getSecond().getEquivBefore().getColumnStatistic().getLowerPercentile() <= cor.getFirst().getColumnStatistic().getAverage()
//                                && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getEquivBefore().getColumnStatistic().getUpperPercentile()) {
//                            correct += "1\t";
//                        } else {
//                            correct += "-1.0\t";
//                        }
//                    } else {
//                        correct += "0\t";
//                        correct += "0\t";
//                    }
//                } else {
////                            System.out.println("equi not used: " + cor.getSecond().getURI() + " class: " + cor.getSecond().getTable() + " table ID " + cor.getFirst().getTable().getHeader());
////                            Correspondence<Table> decidedClass = result.getClassMappings().iterator().next();
////                            TableColumn propertyUsed = decidedClass.getSecond().getColumn(cor.getSecond().getHeader().toString());
////                            correct += cor.getSecond().getColumnStatistic().getMinimalValue() + "\t";
////                            correct += cor.getSecond().getColumnStatistic().getMaximalValue() + "\t";
////                            correct += cor.getSecond().getColumnStatistic().getAverage() + "\t";
////                            correct += cor.getSecond().getColumnStatistic().getStandardDeviation() + "\t";
////                            correct += cor.getSecond().getColumnStatistic().getVariance() + "\t";
////                            correct += cor.getSecond().getColumnStatistic().getLowerPercentile() + "\t";
////                            correct += cor.getSecond().getColumnStatistic().getUpperPercentile() + "\t";
//                    if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
//                        if ((double) cor.getSecond().getColumnStatistic().getMinimalValue() <= cor.getFirst().getColumnStatistic().getAverage()
//                                && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getColumnStatistic().getMaximalValue()) {
//                            correct += "1\t";
//                        } else {
//                            correct += "-1.0\t";
//                        }
//                        if ((double) cor.getSecond().getColumnStatistic().getLowerPercentile() <= cor.getFirst().getColumnStatistic().getAverage()
//                                && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getColumnStatistic().getUpperPercentile()) {
//                            correct += "1\t";
//                        } else {
//                            correct += "-1.0\t";
//                        }
//                    } else {
//                        correct += "0\t";
//                        correct += "0\t";
//                    }
//                }
//
//                if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
//                    TTest ttest = new TTest();
//                    List<Double> tableValues = new ArrayList<>();
//                    for (Object o : cor.getFirst().getValues().values()) {
//                        if (o instanceof List) {
//                            List<Double> list = (List<Double>) o;
//                            tableValues.addAll(list);
//                        } else {
//                            Double value = (double) o;
//                            tableValues.add(value);
//                        }
//                    }
//                    double[] valuesTable = Doubles.toArray(tableValues);
//
//                    List<Double> propValues = new ArrayList<>();
//                    for (Object o : cor.getSecond().getValues().values()) {
//                        if (o instanceof List) {
//                            List<Double> list = (List<Double>) o;
//                            propValues.addAll(list);
//                        } else {
//                            Double value = (double) o;
//                            propValues.add(value);
//                        }
//                    }
//                    double[] valuesProp = Doubles.toArray(propValues);
//                    try {
//                        correct += ttest.t(valuesTable, valuesProp) + "\t";
//                    } catch (Exception e) {
//                        correct += "-5\t";
//                    }
//                    double countIntTable = 0;
//                    double countIntDBpedia = 0;
//                    for (Object o : cor.getSecond().getValues().values()) {
//                        try {
//                            if (!(o instanceof List) && (o instanceof Double)) {
//                                if ((double) o % 1 == 0) {
//                                    countIntDBpedia++;
//                                }
//                            }
//                        } catch (Exception e) {
//                        }
//                    }
//                    for (Object o : cor.getFirst().getValues().values()) {
//                        try {
//                            if (!(o instanceof List) && (o instanceof Double)) {
//                                if ((double) o % 1 == 0) {
//                                    countIntTable++;
//                                }
//                            }
//                        } catch (Exception e) {
//                        }
//                    }
//                    countIntDBpedia = countIntDBpedia / (double) cor.getSecond().getValues().size();
//                    countIntTable = countIntTable / (double) cor.getFirst().getValues().size();
//                    double diff = countIntDBpedia - countIntTable;
//                    correct += diff + "\t";
////                            if(countIntDBpedia > 0.9 && countIntTable < 0.9) {
////                                correct += "0\t";
////                            }
////                            else {
////                                correct += "1\t";
////                            }
//                } else {
//                    correct += "-5\t";
////                            correct += "-8\t";
//                    correct += "0\t";
//                }
//
//                double distValueDBpedia = (double) cor.getSecond().getColumnStatistic().getDistinctValues() / (double) cor.getSecond().getValues().size();
//                double distValueDBpediaList = (double) cor.getSecond().getColumnStatistic().getDistinctValuesWithoutLists() / (double) cor.getSecond().getValues().size();
//                double distValueWT = (double) cor.getFirst().getColumnStatistic().getDistinctValues() / (double) cor.getFirst().getValues().size();
//                double diff1, diff2;
//                diff1 = distValueDBpedia - distValueWT;
//                diff2 = distValueDBpediaList - distValueWT;
//                correct += diff1 + "\t";
//                correct += diff2 + "\t";
//
//                if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.string) {
//                    Collection<Object> values1 = cor.getSecond().getValues().values();
//                    Collection<Object> allValues = new HashSet<>();
//
//                    if (cor.getSecond().getEquivBefore() != null) {
//                        //System.out.println("equiv props: " + cor.getSecond().getURI() + " -- " + cor.getSecond().getEquivBefore().getURI());
//                        Collection<Object> values2 = cor.getSecond().getEquivBefore().getValues().values();
//                        allValues.addAll(values2);
//                    } else {
//                        allValues.addAll(values1);
//                    }
//
//                    Map<String, Integer> countsValuesDBpedia = new TreeMap<>();
//                    for (Object o : allValues) {
//                        if (o instanceof List) {
//                            List<String> list = (List<String>) o;
//                            try {
//                                for (String value : list) {
//                                    if (value.contains(" ") || value.contains("_")) {
//                                        String[] splittedValues;
//                                        if (value.contains(" ")) {
//                                            splittedValues = value.split("\\s");
//                                        } else {
//                                            splittedValues = value.split("_");
//                                        }
//                                        for (String s : splittedValues) {
//                                            s = StringNormalizer.normaliseValue(s, true);
//                                            s = s.replace("(", "");
//                                            if (!StopWordRemover.isStopWord(s)) {
//                                                if (countsValuesDBpedia.containsKey(s)) {
//                                                    Integer current = countsValuesDBpedia.get(s);
//                                                    current++;
//                                                    countsValuesDBpedia.put(s, current);
//                                                } else {
//                                                    countsValuesDBpedia.put(s, 1);
//                                                }
//                                            }
//                                        }
//                                    } else {
//                                        value = StringNormalizer.normaliseValue(value, true);
//                                        if (countsValuesDBpedia.containsKey(value)) {
//                                            Integer current = countsValuesDBpedia.get(value);
//                                            current++;
//                                            countsValuesDBpedia.put(value, current);
//                                        } else {
//                                            countsValuesDBpedia.put(value, 1);
//                                        }
//                                    }
//                                }
//                            } catch (Exception e) {
//                            }
//                        } else if (o instanceof String) {
//                            String value = (String) o;
//                            value = StringNormalizer.normaliseValue(value, true);
//                            if (value.contains(" ") || value.contains("_")) {
//                                String[] splittedValues;
//                                if (value.contains(" ")) {
//                                    splittedValues = value.split("\\s");
//                                } else {
//                                    splittedValues = value.split("_");
//                                }
//                                for (String s : splittedValues) {
//                                    s = s.replace("(", "");
//                                    if (!StopWordRemover.isStopWord(s)) {
//                                        if (countsValuesDBpedia.containsKey(s)) {
//                                            Integer current = countsValuesDBpedia.get(s);
//                                            current++;
//                                            countsValuesDBpedia.put(s, current);
//                                        } else {
//                                            countsValuesDBpedia.put(s, 1);
//                                        }
//                                    }
//                                }
//                            } else {
//                                if (countsValuesDBpedia.containsKey(value)) {
//                                    Integer current = countsValuesDBpedia.get(value);
//                                    current++;
//                                    countsValuesDBpedia.put(value, current);
//                                } else {
//                                    countsValuesDBpedia.put(value, 1);
//                                }
//                            }
//                        }
//                    }
//                    //System.out.println("count values: " + countsValues);
//
//                    double sum = 0.0;
//
//                    Map<String, Integer> countsValuesWT = new TreeMap<>();
//
//                    //System.out.println("values table: " + cor.getFirst().getValues().values());
//                    for (Object o : cor.getFirst().getValues().values()) {
//                        //System.out.println(o);
//                        if (o instanceof List) {
//                            List<String> list = (List<String>) o;
//                            for (String s : list) {
//                                if (s.contains(",")) {
//                                    s = s.split(",")[0];
//                                    s = s.trim();
//                                }
//                                if (s.contains("/")) {
//                                    s = s.split("/")[0];
//                                    s = s.trim();
//                                }
//                                if (s.contains("_")) {
//                                    s = s.split("_")[0];
//                                    s = s.trim();
//                                }
//                                s = s.replace("(", "");
//                                if (s.contains(" ")) {
//                                    String[] splittedValues = s.split("\\s");
//                                    for (String t : splittedValues) {
//                                        if (!StopWordRemover.isStopWord(t)) {
//                                            if (countsValuesDBpedia.containsKey(t)) {
//                                                //sum += countsValuesDBpedia.get(t);
//                                                if (countsValuesWT.containsKey(t)) {
//                                                    Integer current = countsValuesWT.get(t);
//                                                    current++;
//                                                    countsValuesWT.put(t, current);
//                                                } else {
//                                                    countsValuesWT.put(t, 1);
//                                                }
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    if (countsValuesDBpedia.containsKey(s)) {
//                                        //sum += countsValuesDBpedia.get(s);
//                                        if (countsValuesWT.containsKey(s)) {
//                                            Integer current = countsValuesWT.get(s);
//                                            current++;
//                                            countsValuesWT.put(s, current);
//                                        } else {
//                                            countsValuesWT.put(s, 1);
//                                        }
//                                    }
//                                }
//                            }
//                        } else {
//                            String s = (String) o;
//                            if (s.contains(",")) {
//                                s = s.split(",")[0];
//                                s = s.trim();
//                            }
//                            if (s.contains("/")) {
//                                s = s.split("/")[0];
//                                s = s.trim();
//                            }
//                            s = s.trim();
//                            if (s.contains(" ")) {
//                                String[] splittedValues = s.split("\\s");
//                                for (String t : splittedValues) {
//                                    if (!StopWordRemover.isStopWord(t)) {
//                                        if (countsValuesDBpedia.containsKey(t)) {
//                                            //sum += countsValuesDBpedia.get(t);
//                                            if (countsValuesWT.containsKey(t)) {
//                                                Integer current = countsValuesWT.get(t);
//                                                current++;
//                                                countsValuesWT.put(t, current);
//                                            } else {
//                                                countsValuesWT.put(t, 1);
//                                            }
//                                        }
//                                    }
//                                }
//                            } else {
//                                if (countsValuesDBpedia.containsKey(s)) {
//                                    //sum += countsValuesDBpedia.get(s);
//                                    if (countsValuesWT.containsKey(s)) {
//                                        Integer current = countsValuesWT.get(s);
//                                        current++;
//                                        countsValuesWT.put(s, current);
//                                    } else {
//                                        countsValuesWT.put(s, 1);
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    //System.out.println("calculate cosine: " + cor.getSecond().getURI() + " of class " + cor.getSecond().getTable().getHeader() + " with " + cor.getFirst().getHeader());
//                    correct += calculateCosineSimilarity(countsValuesDBpedia, countsValuesWT) + "\t";
////
////                            sum = 0.0;
////                            try {
////                                //SoftballLeague_currentSeason    {2015 Men's Softball World Championship=1, 2013 NCAA Division III Softball Championship=1, 2013 NCAA Division II Softball Championship=1}
////                                BufferedReader read = new BufferedReader(new FileReader(new File("/backup-local-home/dritze/newGS/recognizer/DBpedia/dbpediaStatsString.csv")));
////                                String wholeLine = read.readLine();
////                                String className = "";
////                                for (Correspondence<Table> corTable : result.getClassMappings()) {
////                                    className = corTable.getSecond().getHeader();
////                                    className = className.replace(".tar.gz", "");
////                                    className = className.replace(".csv", "");
////                                    className = className.replace(".gz", "");
////                                }
////                                Set<String> urisToCheck = new HashSet<>();
////                                Canoniser c = new Canoniser();
////                                Collection<List<String>> equiProps = c.loadEquivalentResourcesExternal(getEvaluationParameters().getEquivalentPropertiesLocation());
////                                for (List l : equiProps) {
////                                    if (l.contains(cor.getSecond().getURI())) {
////                                        //System.out.println("uris to check! " + l);
////                                        urisToCheck.addAll(l);
////                                    }
////                                }
////
////                                //System.out.println("class prop name second: " + className + "_" + cor.getSecond().getURI().replace("http://dbpedia.org/ontology/", ""));
////                                //System.out.println("class prop name equi: " + className + "_" + evalInstance.getUniqueIdentifier(cor.getSecond()).toString().replace("http://dbpedia.org/ontology/", ""));
////                                Set<String> lineWithProp = new HashSet<>();
////                                while (wholeLine != null) {
////                                    if (!urisToCheck.isEmpty()) {
////                                        //System.out.println("not empty");
////                                        for (String s : urisToCheck) {
////                                            System.out.println("check " + s);
////                                            if (wholeLine.startsWith(className + "_" + s.replace("http://dbpedia.org/ontology/", "") + "\t")
////                                                    || wholeLine.startsWith(className + "_" + s.replace("http://dbpedia.org/ontology/", "") + "_label\t")) {
////                                                lineWithProp.add(wholeLine);
////                                                //    System.out.println("found");
////                                            }
////                                        }
////                                    } else {
////                                        if (wholeLine.startsWith(className + "_" + cor.getSecond().getURI().replace("http://dbpedia.org/ontology/", "") + "\t")
////                                                || wholeLine.startsWith(className + "_" + cor.getSecond().getURI().replace("http://dbpedia.org/ontology/", "") + "_label\t")) {
////                                            lineWithProp.add(wholeLine);
////                                            // System.out.println("found single: " + cor.getSecond().getURI());
////                                            break;
////                                        }
////                                        //continue;
////                                    }
////
//////                                    if (wholeLine.startsWith(className + "_" + evalInstance.getUniqueIdentifier(cor.getSecond()).toString().replace("http://dbpedia.org/ontology/", ""))) {
//////                                        secondLineWithProp = wholeLine;
//////                                        //break;
//////                                    }
////                                    wholeLine = read.readLine();
////                                }
////                                //System.out.println("line with prop " +lineWithProp);
////                                if (!lineWithProp.isEmpty()) {
////                                    for (String check : lineWithProp) {
////                                        String[] countArray = new String[0];
////                                        String[] countArray2 = new String[0];
////                                        String[] line = check.split("\t");
////                                        countArray = line[1].split(",");
////                                        Map<String, Integer> counts = new HashMap<>();
////                                        for (String s : countArray) {
////                                            s = s.replace("}", "");
////                                            s = s.replace("{", "");
////                                            String[] entry = s.split("=");
////                                            counts.put(StringNormalizer.normaliseValue(entry[0], true), Integer.parseInt(entry[1]));
////                                            //System.out.println("entry: " + StringNormalizer.normaliseValue(entry[0], true));
////                                        }
////                                        for (String s : countArray2) {
////                                            s = s.replace("}", "");
////                                            s = s.replace("{", "");
////                                            String[] entry = s.split("=");
////                                            counts.put(StringNormalizer.normaliseValue(entry[0], true), Integer.parseInt(entry[1]));
////                                            //System.out.println(StringNormalizer.normaliseValue(entry[0],true));
////                                        }
////                                        for (Object o : cor.getFirst().getValues().values()) {
////                                            //System.out.println(o);
////                                            if (o instanceof List) {
////                                                List<String> list = (List<String>) o;
////                                                for (String s : list) {
////                                                    if (counts.containsKey(s)) {
////                                                        sum += counts.get(s);
////                                                    }
////                                                    break;
////                                                }
////                                            } else {
////                                                String s = (String) o;
////                                                if (s.contains(",")) {
////                                                    s = s.split(",")[0];
////                                                    s = s.trim();
////                                                }
////                                                if (s.contains("/")) {
////                                                    s = s.split("/")[0];
////                                                    s = s.trim();
////                                                }
////                                                if (counts.containsKey(s)) {
////                                                    sum += counts.get(s);
////                                                }
////                                            }
////                                        }
////                                    }
////                                }
////                            } catch (Exception e) {
////                                e.printStackTrace();
////                            }
////                            correct += sum + "\t";
//                } else {
////                            correct += -1.0 + "\t";
////                            correct += -1.0 + "\t";
//                    correct += "0\t";
//                }
////                        correct += cor.getFirst().getValues().size() + "\t";
//
//                if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.date) {
//                    TTest ttest = new TTest();
//                    List<Date> tableValues = new ArrayList<>();
//                    for (Object o : cor.getFirst().getValues().values()) {
//                        if (o instanceof List) {
//                            List<Date> list = (List<Date>) o;
//                            tableValues.addAll(list);
//                        } else {
//                            Date value = (Date) o;
//                            tableValues.add(value);
//                        }
//                    }
//                    List<Date> dbpediaValues = new ArrayList<>();
//                    for (Object o : cor.getSecond().getValues().values()) {
//                        if (o instanceof List) {
//                            List<Date> list = (List<Date>) o;
//                            dbpediaValues.addAll(list);
//                        } else {
//                            Date value = (Date) o;
//                            dbpediaValues.add(value);
//                        }
//                    }
//                    Collections.sort(tableValues);
//                    Date median;
//                    int middle = tableValues.size() / 2;
//                    median = tableValues.get(middle);
//
//                    Collections.sort(dbpediaValues);
//                    Date minDate = dbpediaValues.get(0);
//                    Date maxDate = dbpediaValues.get(dbpediaValues.size() - 1);
//
//                    if (minDate.before(median) && median.before(maxDate)) {
//                        correct += "1.0\t";
//                    } else {
//                        correct += "0.0\t";
//                    }
//
//                } else {
//                    correct += "-5\t";
//                }
//
//                Object correctKey = cor.getCorrectValue() + "\t";
//                correct += correctKey;
//                System.out.println("propProp correct property " + correct);
//            }
//
//        }
//
//        for (Map.Entry<Object, Object> e : getGoldStandard().getPropertyGoldStandard().entrySet()) {
//            String checkIncorrect = "";
//            SimilarityMatrix<TableRow> sim = similarities.getInitialCandidateSimilarity();
//            for (TableColumn tr : getData().getWebtable().getColumns()) {
//                if (!tr.getTable().getHeader().equals(getData().getWebtable().getHeader())) {
//                    continue;
//                }
//                boolean foundInValueCandidates = false, detected = false, foundFreq = false, allowed = false;
//                if (tr.getTable().getColumns().indexOf(tr) == Integer.parseInt(e.getKey().toString())) {
//                    checkIncorrect += tr.getTable().getHeader() + "\t";
//                    checkIncorrect += tr.getHeader() + "\t";
//                    checkIncorrect += tr.getValues() + "\t";
//                    for (Correspondence<Table> corTable : result.getClassMappings()) {
//                        //checkIncorrect += corTable.getSecond() + "---" + corTable.isCorrect() + "\t";
//                        checkIncorrect += corTable.isCorrect() + "\t";
//                        checkIncorrect += corTable.getSecond().getHeader() + "\t";
//                    }
//                    if (result.getClassMappings().isEmpty()) {
//                        checkIncorrect += "tableFiltered\t";
//                        checkIncorrect += "tableFiltered\t";
//                    }
////                            int countInst = 0;
////                            for (Correspondence<TableRow> corInst : result.getInstanceMappings()) {
////                                //checkIncorrect += corProp.getFirst() + "---" + corProp.getSecond() + "---" + corProp.isCorrect() + "\t";
////                                countInst++;
////                            }
//                    if (!result.getPropertyMappings().isEmpty()) {
//                        for (Correspondence<TableColumn> tc : result.getPropertyMappings()) {
//                            if (tc.getSecond().getURI().equals(e.getValue().toString())) {
//                                continue;
//                            }
//                            checkIncorrect += tc.getSecond().getURI() + "---";
//                        }
//                        checkIncorrect += "\t";
//                    } else {
//                        checkIncorrect += "noOther\t";
//                    }
//                    checkIncorrect += (double) result.getInstanceMappings().size() / (double) getData().getWebtable().getKey().getNumRows() + "\t";
//                    checkIncorrect += e.getValue() + "\t";
//                    if (allowedCols.contains(e.getValue().toString())) {
//                        checkIncorrect += "allowed\t";
//                    } else {
//                        checkIncorrect += "notAllowed\t";
//                    }
//                    for (TableColumn valueCandidates : propertySim.getMatches(tr)) {
//                        if (e.getValue().equals(evalInstance.getUniqueIdentifier(valueCandidates))) {
//                            checkIncorrect += propertySim.get(tr, valueCandidates) + "\t";
//                            foundInValueCandidates = true;
//                        }
//                    }
//                    for (TableColumn valueCandidates : labelSimilarity.getMatches(tr)) {
//                        if (e.getValue().equals(evalInstance.getUniqueIdentifier(valueCandidates))) {
//                            checkIncorrect += labelSimilarity.get(tr, valueCandidates) + "\t";
//                            foundInValueCandidates = true;
//                        }
//                    }
////                            for (TableColumn valueCandidates : freqAll.getMatches(tr)) {
////                                if (e.getValue().equals(evalInstance.getUniqueIdentifier(valueCandidates))) {
////                                    checkIncorrect += freqAll.get(tr, valueCandidates) + "\t";
////                                    foundFreq = true;
////                                }
////                            }
//
//                    for (Correspondence<TableColumn> cor : result.getPropertyMappings()) {
//                        if (cor.getFirst().equals(tr) && evalInstance.getUniqueIdentifier(cor.getSecond()).equals(e.getValue())) {
//                            detected = true;
//                        } else {
//                            if (cor.getFirst().equals(tr) && !evalInstance.getUniqueIdentifier(cor.getSecond()).equals(e.getValue())) {
//                                checkIncorrect += cor.getSecond().getURI() + "\t";
//                                checkIncorrect += cor.getSimilarity() + "\t";
//                            }
//                        }
//                    }
//
//                    if (foundInValueCandidates && !detected) {
//                        checkIncorrect += "wrongChoice\t";
//                    } else if (!detected && !foundInValueCandidates && !foundFreq) {
//                        checkIncorrect += "noCandidateAtAll\t";
//                    } else if (!detected && !foundInValueCandidates && foundFreq) {
//                        checkIncorrect += "onlyFreq\t";
//                    }
//                    checkIncorrect += kurti + "\t";
//                    if (!detected) {
//                        System.out.println("propProp property FN: " + checkIncorrect);
//                    }
//                }
//            }
//        }
//    }
//
//}
