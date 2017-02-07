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
package de.dwslab.T2K.matching.dbpedia.model;

import au.com.bytecode.opencsv.CSVWriter;
import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.pageResolving.WebPageEntry;
import de.dwslab.T2K.index.pageResolving.WebPageIndex;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.tableType;
import de.dwslab.T2K.matching.dbpedia.model.adapters.CandidateAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.ClassAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.ColumnAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.DBpediaClassAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.DBpediaInstanceAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.DBpediaPropertyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableToRowHierarchyAdapter;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.evaluation.MatchingEvaluator;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.tableprocessor.model.TableMapping;
import de.dwslab.T2K.tableprocessor.model.json.AnnotatedTable;
import de.dwslab.T2K.tableprocessor.model.json.TableData;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.query.Func;
import de.dwslab.T2K.utils.query.Q;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class MatchingResult {

    private Table webtable;

    public Table getWebtable() {
        return webtable;
    }
    private IIndex uriIndex;

    public void setWebtable(Table webtable) {
        this.webtable = webtable;
    }
    Collection<Correspondence<TableRow>> instanceMappings;
    Collection<Correspondence<TableColumn>> propertyMappings;
    Collection<Correspondence<TableColumn>> propertyRangeMappings;
    Collection<Correspondence<Table>> classMappings;
    Map<Correspondence<TableRow>, Integer> instanceErrors;
    private Map<Integer, Integer> instanceFNErrors;
    MatchingEvaluation evaluation;
    private MatchingData matchingData;
    private Similarities similarities;

    public void setMatchingData(MatchingData matchingData) {
        this.matchingData = matchingData;
    }

    public MatchingData getMatchingData() {
        return matchingData;
    }
    private GoldStandard goldStandard;

    public MatchingResult() {
        evaluation = new MatchingEvaluation(null, 0, null, null, null, null);
    }

    public void setGoldStandard(GoldStandard goldStandard) {
        this.goldStandard = goldStandard;
    }

    public GoldStandard getGoldStandard() {
        return goldStandard;
    }

    public Collection<Correspondence<TableRow>> getInstanceMappings() {
        return instanceMappings;
    }

    public Collection<Correspondence<TableColumn>> getPropertyMappings() {
        return propertyMappings;
    }

    public Collection<Correspondence<Table>> getClassMappings() {
        return classMappings;
    }

    public void setInstanceMappings(
            Collection<Correspondence<TableRow>> instanceMappings) {
        this.instanceMappings = instanceMappings;
    }

    public void setPropertyMappings(
            Collection<Correspondence<TableColumn>> propertyMappings) {
        this.propertyMappings = propertyMappings;
    }

    public void setPropertyRangeMappings(
            Collection<Correspondence<TableColumn>> propertyRangeMappings) {
        this.propertyRangeMappings = propertyRangeMappings;
    }

    public Collection<Correspondence<TableColumn>> getPropertyRangeMappings() {
        return propertyRangeMappings;
    }

    public void setClassMappings(Collection<Correspondence<Table>> classMappings) {
        this.classMappings = classMappings;
    }

    public Map<Correspondence<TableRow>, Integer> getInstanceErrors() {
        return instanceErrors;
    }

    public void setInstanceErrors(
            Map<Correspondence<TableRow>, Integer> instanceErrors) {
        this.instanceErrors = instanceErrors;
    }

    public MatchingEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(MatchingEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    /**
     * @return the uriIndex
     */
    public IIndex getUriIndex() {
        return uriIndex;
    }

    /**
     * @param uriIndex the uriIndex to set
     */
    public void setUriIndex(IIndex uriIndex) {
        this.uriIndex = uriIndex;
    }

    /**
     * @return the similarities
     */
    public Similarities getSimilarities() {
        return similarities;
    }

    /**
     * @param similarities the similarities to set
     */
    public void setSimilarities(Similarities similarities) {
        this.similarities = similarities;
    }

    /**
     * @return the instanceFNErrors
     */
    public Map<Integer, Integer> getInstanceFNErrors() {
        return instanceFNErrors;
    }

    /**
     * @param instanceFNErrors the instanceFNErrors to set
     */
    public void setInstanceFNErrors(Map<Integer, Integer> instanceFNErrors) {
        this.instanceFNErrors = instanceFNErrors;
    }

    private class InstanceData {

        public String key;
        public String mappedTo;
        public Double similarity;
        public Boolean isCorrect;
        public String correctValue;
        public String reason;
        public String table;
        public String classificationPrecision;
        public String classificationRecall;

        public String[] toStringArray() {
            if (isCorrect) {

                if (mappedTo == null || mappedTo.equals("")) {
                    classificationPrecision = "TN";
                    classificationRecall = "TN";
                } else {
                    classificationPrecision = "TP";
                    classificationRecall = "TP";
                }

            } else {

                if (mappedTo == null || mappedTo.equals("")) {
                    classificationPrecision = "FN";
                    classificationRecall = "FN";
                } else {
                    classificationPrecision = "FP";
                    classificationRecall = "FN";
                }

            }

            return new String[]{key, mappedTo, Double.toString(similarity),
                Boolean.toString(isCorrect), correctValue, reason, table,
                classificationPrecision, classificationRecall};
        }
    }
    public static final int REASON_NOT_IN_DATA = 1;
    public static final int REASON_NOT_IN_CANDIDATES = 2;

    public Collection<String[]> formatInstancesForCSV() {
        Collection<String[]> col = new ArrayList<String[]>(
                getInstanceMappings().size());
        HashSet<String> mappedUris = new HashSet<String>();
        HashSet<String> writtenKeys = new HashSet<String>();

        InstanceData values = null;

        // all mapped rows (True Positives & False Positives)
        for (Correspondence<TableRow> rowC : getInstanceMappings()) {

            // String[] values = new String[7];
            values = new InstanceData();
            values.key = rowC.getFirst().getKey().toString();
            values.mappedTo = rowC.getSecond().getURI().toString();
            // values[2] = Double.toString(rowC.getSimilarity());
            values.similarity = rowC.getSimilarity();
            // values[3] = Boolean.toString(rowC.isCorrect());
            values.isCorrect = rowC.isCorrect();
            if (rowC.getCorrectValue() != null) {
                values.correctValue = rowC.getCorrectValue().toString();

                mappedUris.add(rowC.getSecond().getURI().toString());
            }
            if (getInstanceErrors() != null
                    && getInstanceErrors().containsKey(rowC)) {
                switch (getInstanceErrors().get(rowC)) {
                    case REASON_NOT_IN_DATA:
                        values.reason = "correct value not in data";
                        break;
                    case REASON_NOT_IN_CANDIDATES:
                        values.reason = "correct value not in candidates";
                        break;
                    default:
                        values.reason = "other";
                        break;
                }
            }
            values.table = getWebtable().getHeader();

            writtenKeys.add(values.key);
            col.add(values.toStringArray());
        }

        // all mappings in GS that were not found (False Negatives)
        for (Entry<Object, Object> e : getGoldStandard()
                .getInstanceGoldStandard().entrySet()) {
            String key = "";

            // if (!mappedUris.contains(e.getValue())) {
            Object keyObject = webtable
                    .getKey()
                    .getValues()
                    .get(Integer.parseInt(e.getKey().toString()));
            if (keyObject != null) {
                key = webtable
                        .getKey()
                        .getValues()
                        .get(Integer.parseInt(e.getKey().toString())).toString();
            }
            // String key =
            // webtable.getKey().getValues().get(Integer.parseInt(e.getKey().toString())).toString();
            // String key_2 =
            // webtable.getKey().getValues().get(Integer.parseInt(e.getKey().toString())
            // + webtable.getNumHeaderRows()).toString();
            if (!writtenKeys.contains(key)) {
                values = new InstanceData();
                // values.key = e.getKey().toString();
                values.key = key;
                values.mappedTo = "";
                values.similarity = 0.0;
                values.isCorrect = false;
                values.correctValue = e.getValue().toString();
                if (getInstanceFNErrors().containsKey(Integer.parseInt(e.getKey().toString()))) {
                    switch (getInstanceFNErrors().get(Integer.parseInt(e.getKey().toString()))) {
                        case REASON_NOT_IN_DATA:
                            values.reason = "correct value not in data";
                            break;
                        case REASON_NOT_IN_CANDIDATES:
                            values.reason = "correct value not in candidates";
                            break;
                        default:
                            values.reason = "other";
                            break;
                    }
                }
                values.table = getWebtable().getHeader();
                writtenKeys.add(values.key);
                col.add(values.toStringArray());
            }
        }

        // all rows that were not mapped and are not in GS (True Negatives)
        TableToRowHierarchyAdapter rowAdapter = new TableToRowHierarchyAdapter();
        for (final TableRow row : rowAdapter.getParts(getWebtable())) {

            Collection<Correspondence<TableRow>> mappings = Q.where(
                    getInstanceMappings(),
                    new Func<Boolean, Correspondence<TableRow>>() {
                @Override
                public Boolean invoke(Correspondence<TableRow> in) {
                    return in.getFirst() == row;
                }
            });

            // if there is no mapping and the GS does not contain the key, we
            // have a TN
            if (mappings.size() == 0
                    && !getGoldStandard().getInstanceGoldStandard()
                    .containsKey(row.getKey())) {

                values = new InstanceData();
                values.key = row.getKey().toString();
                values.mappedTo = "";
                values.similarity = 0.0;
                values.isCorrect = true;
                values.correctValue = "";
                values.reason = "";
                values.table = getWebtable().getHeader();
                col.add(values.toStringArray());
            }

        }

        return col;
    }

    public Collection<String[]> formatPropertyRangesForCSV() {
        Collection<String[]> col = new ArrayList<String[]>(
                getPropertyRangeMappings().size());
        HashSet<String> mappedUris = new HashSet<String>();

        for (Correspondence<TableColumn> rowC : getPropertyRangeMappings()) {

            String[] values = new String[5];
            values[0] = rowC.getFirst().toString();
            values[1] = rowC.getSecond().toString();
            values[2] = Boolean.toString(rowC.isCorrect());
            if (rowC.getCorrectValue() != null) {
                values[3] = rowC.getCorrectValue().toString();
                mappedUris.add(rowC.getSecond().toString());
            }
            values[4] = getWebtable().getHeader();

            col.add(values);
        }
        if(getGoldStandard() != null) {
        for (Entry<Object, Object> e : getGoldStandard()
                .getPropertyRangeGoldStandard().entrySet()) {
            if (!mappedUris.contains(e.getValue())) {
                String[] values = new String[6];
                values[0] = e.getKey().toString();
                values[1] = "";
                values[2] = "false";
                values[3] = e.getValue().toString();
                values[4] = getWebtable().getHeader();
                values[5] = "not mapped";

                col.add(values);
            }
        }
        }

        return col;
    }

    public String[] getPropertiesCSVHeader() {
        return PropertyData.getCsvHeader();
    }

    private static class PropertyData {

        public static String[] getCsvHeader() {
            String[] values = new String[34];
            int i = 0;
            values[i++] = "header";
            values[i++] = "mapped URI";
            values[i++] = "#values";
            values[i++] = "#distinct values";
            values[i++] = "min value";
            values[i++] = "max value";
            values[i++] = "average";
            values[i++] = "variance";
            values[i++] = "std dev";
            values[i++] = "skewness";
            values[i++] = "kurtosis";
            values[i++] = "mapped column #values";
            values[i++] = "mapped column #distinct values";
            values[i++] = "mapped column min value";
            values[i++] = "mapped column max value";
            values[i++] = "mapped column average";
            values[i++] = "mapped column variance";
            values[i++] = "mapped column std dev";
            values[i++] = "mapped column skewness";
            values[i++] = "mapped column kurtosis";
            values[i++] = "similarity";
            values[i++] = "correct";
            values[i++] = "correct URI";
            values[i++] = "correct #values";
            values[i++] = "correct #distinct values";
            values[i++] = "correct min value";
            values[i++] = "correct max value";
            values[i++] = "correct average";
            values[i++] = "correct variance";
            values[i++] = "correct std dev";
            values[i++] = "correct skewness";
            values[i++] = "correct kurtosis";
            values[i++] = "web table";
            values[i++] = "data type";

            return values;
        }
        public String header;
        public String mappedTo;
        public Integer numValues;
        public Integer numDistinctValues;
        public Object min;
        public Object max;
        public Double average;
        public Double variance;
        public Double stddev;
        public Double skewness;
        public Double kurtosis;
        public Integer mappedNumValues;
        public Integer mappedDistinctValues;
        public Object mappedMin;
        public Object mappedMax;
        public Double mappedAverage;
        public Double mappedVariance;
        public Double mappedStddev;
        public Double mappedSkewness;
        public Double mappedKurtosis;
        public Double similarity;
        public Boolean isCorrect;
        public String correctUri;
        public Integer correctNumValues;
        public Integer correctDistinctValues;
        public Object correctMin;
        public Object correctMax;
        public Double correctAverage;
        public Double correctVariance;
        public Double correctStddev;
        public Double correctSkewness;
        public Double correctKurtosis;
        public String table;
        public String dataType;
        public String classificationPrecision;
        public String classificationRecall;

        public void setColumnStatistics(TableColumn col) {
            numValues = col.getValues().size();
            numDistinctValues = (int) col.getColumnStatistic()
                    .getDistinctValues();
            min = col.getColumnStatistic().getMinimalValue();
            max = col.getColumnStatistic().getMaximalValue();
            average = col.getColumnStatistic().getAverage();
            variance = col.getColumnStatistic().getVariance();
            stddev = col.getColumnStatistic().getStandardDeviation();
            skewness = col.getColumnStatistic().getSkewness();
            kurtosis = col.getColumnStatistic().getKurtosis();
        }

        public void setMappedColumnStatistics(TableColumn col) {
            mappedNumValues = col.getValues().size();
            mappedDistinctValues = (int) col.getColumnStatistic()
                    .getDistinctValues();
            mappedMin = col.getColumnStatistic().getMinimalValue();
            mappedMax = col.getColumnStatistic().getMaximalValue();
            mappedAverage = col.getColumnStatistic().getAverage();
            mappedVariance = col.getColumnStatistic().getVariance();
            mappedStddev = col.getColumnStatistic().getStandardDeviation();
            mappedSkewness = col.getColumnStatistic().getSkewness();
            mappedKurtosis = col.getColumnStatistic().getKurtosis();
        }

        public void setCorrectColumnStatistics(TableColumn col) {
            correctNumValues = col.getValues().size();
            correctDistinctValues = (int) col.getColumnStatistic()
                    .getDistinctValues();
            correctMin = col.getColumnStatistic().getMinimalValue();
            correctMax = col.getColumnStatistic().getMaximalValue();
            correctAverage = col.getColumnStatistic().getAverage();
            correctVariance = col.getColumnStatistic().getVariance();
            correctStddev = col.getColumnStatistic().getStandardDeviation();
            correctSkewness = col.getColumnStatistic().getSkewness();
            correctKurtosis = col.getColumnStatistic().getKurtosis();
        }

        public String[] toStringArray() {
            if (isCorrect) {

                if (mappedTo == null || mappedTo.equals("")) {
                    classificationPrecision = "TN";
                    classificationRecall = "TN";
                } else {
                    classificationPrecision = "TP";
                    classificationRecall = "TP";
                }

            } else {

                if (mappedTo == null || mappedTo.equals("")) {
                    classificationPrecision = "FN";
                    classificationRecall = "FN";
                } else {
                    classificationPrecision = "FP";
                    classificationRecall = "FN";
                }
            }

            return new String[]{header, mappedTo, numValues + "",
                numDistinctValues + "", min == null ? "" : min.toString(),
                max == null ? "" : max.toString(), average + "",
                variance + "", stddev + "", skewness + "", kurtosis + "",
                mappedNumValues + "", mappedDistinctValues + "",
                mappedMin == null ? "" : mappedMin.toString(),
                mappedMax == null ? "" : mappedMax.toString(),
                mappedAverage + "", mappedVariance + "", mappedStddev + "",
                mappedSkewness + "", mappedKurtosis + "", similarity + "",
                Boolean.toString(isCorrect), correctUri,
                correctNumValues + "", correctDistinctValues + "",
                correctMin == null ? "" : correctMin.toString(),
                correctMax == null ? "" : correctMax.toString(),
                correctAverage + "", correctVariance + "",
                correctStddev + "", correctSkewness + "",
                correctKurtosis + "", table, dataType,
                classificationPrecision, classificationRecall};
        }
    }

    public Collection<String[]> formatPropertiesForCSV() {
        Collection<String[]> col = new ArrayList<String[]>(
                getPropertyMappings().size());
        HashSet<String> mappedUris = new HashSet<String>();
        HashSet<String> mappedCols = new HashSet<String>();

        PropertyData values = null;

//        String cls;
//        Table correctCls = null;
//        if (classMappings.iterator().hasNext()) {
//            // determine class to get statistical properties of the correct
//            // columns
//            Object obj = classMappings.iterator().next().getCorrectValue();
//            if (obj != null) {
//                cls = obj.toString();
//                for (Table t : getMatchingData().getDbpediaTables()) {
//                    if (t.getHeader().replace(".csv", "").equals(cls)) {
//                        correctCls = t;
//                        break;
//                    }
//                }
//            }
//        }
//        if (classMappings.iterator().hasNext()) {
//            // determine mapped class to get statistical properties of the
//            // mapped columns
//            Object obj = classMappings.iterator().next().getCorrectValue();
//            if (obj != null) {
//                cls = obj.toString();
//                Table mappedCls = null;
//                for (Table t : getMatchingData().getDbpediaTables()) {
//                    if (t.getHeader().replace(".csv", "").equals(cls)) {
//                        mappedCls = t;
//                        break;
//                    }
//                }
//            }
//        }

        // all mapped columns (True Positives & False Positives)
        for (Correspondence<TableColumn> colC : getPropertyMappings()) {

            // String[] values = new String[34];
            values = new PropertyData();
            values.header = colC.getFirst().getHeader().toString();
            mappedCols.add(colC.getFirst().getHeader().toString());
            values.mappedTo = colC.getSecond().getURI();
            // values[2] = Double.toString(colC.getFirst().getValues().size());
            // values.numValues = colC.getFirst().getValues().size();
            // //values[3] =
            // Double.toString(colC.getFirst().getColumnStatistic().getDistinctValues());
            // values.numDistinctValues = (int)
            // colC.getFirst().getColumnStatistic().getDistinctValues();
            // // if (colC.getFirst().getColumnStatistic().getMinimalValue() !=
            // null) {
            // // values[4] =
            // colC.getFirst().getColumnStatistic().getMinimalValue().toString();
            // // }
            // values.min =
            // colC.getFirst().getColumnStatistic().getMinimalValue();
            // // if (colC.getFirst().getColumnStatistic().getMaximalValue() !=
            // null) {
            // // values[5] =
            // colC.getFirst().getColumnStatistic().getMaximalValue().toString();
            // // }
            // values.max =
            // colC.getFirst().getColumnStatistic().getMaximalValue();
            // //values[6] =
            // Double.toString(colC.getFirst().getColumnStatistic().getAverage());
            // values.average =
            // colC.getFirst().getColumnStatistic().getAverage();
            // //values[7] =
            // Double.toString(colC.getFirst().getColumnStatistic().getVariance());
            // values.variance =
            // colC.getFirst().getColumnStatistic().getVariance();
            // //values[8] =
            // Double.toString(colC.getFirst().getColumnStatistic().getStandardDeviation());
            // values.stddev =
            // colC.getFirst().getColumnStatistic().getStandardDeviation();
            // //values[9] =
            // Double.toString(colC.getFirst().getColumnStatistic().getSkewness());
            // values.skewness =
            // colC.getFirst().getColumnStatistic().getSkewness();
            // //values[10] =
            // Double.toString(colC.getFirst().getColumnStatistic().getKurtosis());
            // values.kurtosis =
            // colC.getFirst().getColumnStatistic().getKurtosis();
            values.setColumnStatistics(colC.getFirst());

            // //values[11] =
            // Double.toString(colC.getSecond().getValues().size());
            // values.mappedNumValues = colC.getSecond().getValues().size();
            // //values[12] =
            // Double.toString(colC.getSecond().getColumnStatistic().getDistinctValues());
            // values.mappedDistinctValues = (int)
            // colC.getSecond().getColumnStatistic().getDistinctValues();
            // // if (colC.getSecond().getColumnStatistic().getMinimalValue() !=
            // null) {
            // // values[13] =
            // colC.getSecond().getColumnStatistic().getMinimalValue().toString();
            // // }
            // values.mappedMin =
            // colC.getSecond().getColumnStatistic().getMinimalValue();
            // // if (colC.getSecond().getColumnStatistic().getMaximalValue() !=
            // null) {
            // // values[14] =
            // colC.getSecond().getColumnStatistic().getMaximalValue().toString();
            // // }
            // values.mappedMax =
            // colC.getSecond().getColumnStatistic().getMaximalValue();
            // //values[15] =
            // Double.toString(colC.getSecond().getColumnStatistic().getAverage());
            // values.mappedAverage =
            // colC.getSecond().getColumnStatistic().getAverage();
            // //values[16] =
            // Double.toString(colC.getSecond().getColumnStatistic().getVariance());
            // values.mappedVariance =
            // colC.getSecond().getColumnStatistic().getVariance();
            // //values[17] =
            // Double.toString(colC.getSecond().getColumnStatistic().getStandardDeviation());
            // values.mappedStddev =
            // colC.getSecond().getColumnStatistic().getStandardDeviation();
            // //values[18] =
            // Double.toString(colC.getSecond().getColumnStatistic().getSkewness());
            // values.mappedSkewness =
            // colC.getSecond().getColumnStatistic().getSkewness();
            // //values[19] =
            // Double.toString(colC.getSecond().getColumnStatistic().getKurtosis());
            // values.mappedKurtosis =
            // colC.getSecond().getColumnStatistic().getKurtosis();
            values.setMappedColumnStatistics(colC.getSecond());

            // values[20] = Double.toString(colC.getSimilarity());
            values.similarity = colC.getSimilarity();
            // values[21] = Boolean.toString(colC.isCorrect());
            values.isCorrect = colC.isCorrect();
            values.dataType = colC.getFirst().getDataType().toString();

            if (colC.getCorrectValue() != null) {
                values.correctUri = colC.getCorrectValue().toString();
                if (colC.getCorrectValue() instanceof List) {
                    for (Object o : (List) colC.getCorrectValue()) {
                        mappedUris.add(o.toString());
                    }
                } else {
                    mappedUris.add(colC.getCorrectValue().toString());
                }

//                if (correctCls != null) {
//                    TableColumn correctCol = null;
//                    for (TableColumn c : correctCls.getColumns()) {
//                        if (c.getURI().equals(colC.getCorrectValue())) {
//                            correctCol = c;
//                            break;
//                        }
//                    }

//                    if (correctCol != null) {
                // //values[23] =
                // Double.toString(correctCol.getValues().size());
                // values.correctNumValues =
                // correctCol.getValues().size();
                // //values[24] =
                // Double.toString(correctCol.getColumnStatistic().getDistinctValues());
                // values.correctDistinctValues = (int)
                // correctCol.getColumnStatistic().getDistinctValues();
                // // if
                // (correctCol.getColumnStatistic().getMinimalValue() !=
                // null) {
                // // values[25] =
                // correctCol.getColumnStatistic().getMinimalValue().toString();
                // // }
                // values.correctMin =
                // correctCol.getColumnStatistic().getMinimalValue();
                // // if
                // (correctCol.getColumnStatistic().getMaximalValue() !=
                // null) {
                // // values[26] =
                // correctCol.getColumnStatistic().getMaximalValue().toString();
                // // }
                // values.correctMax =
                // correctCol.getColumnStatistic().getMaximalValue();
                // //values[27] =
                // Double.toString(correctCol.getColumnStatistic().getAverage());
                // values.correctAverage =
                // correctCol.getColumnStatistic().getAverage();
                // //values[28] =
                // Double.toString(correctCol.getColumnStatistic().getVariance());
                // values.correctVariance =
                // correctCol.getColumnStatistic().getVariance();
                // //values[29] =
                // Double.toString(correctCol.getColumnStatistic().getStandardDeviation());
                // values.correctStddev =
                // correctCol.getColumnStatistic().getStandardDeviation();
                // //values[30] =
                // Double.toString(correctCol.getColumnStatistic().getSkewness());
                // values.correctSkewness =
                // correctCol.getColumnStatistic().getSkewness();
                // //values[31] =
                // Double.toString(correctCol.getColumnStatistic().getKurtosis());
                // values.correctKurtosis =
                // correctCol.getColumnStatistic().getKurtosis();
                // //values[33] = correctCol.getDataType().toString();
//                        values.setCorrectColumnStatistics(correctCol);

//                    }
//                }
            }
            values.table = getWebtable().getHeader();

            col.add(values.toStringArray());
        }

        if (getGoldStandard() != null) {


            // all mappings in GS that were not found (False Negatives)
            for (Entry<Object, Object> e : getGoldStandard()
                    .getPropertyGoldStandard().entrySet()) {
                if (!mappedUris.contains(e.getValue())) {
                    // String[] values = new String[34];
                    values = new PropertyData();
                    values.header = e.getKey().toString();
                    values.mappedTo = "";
                    // values[2] = "";

                    mappedCols.add(e.getKey().toString());

                    TableColumn origCol = null;
                    for (TableColumn c : getWebtable().getColumns()) {
                        if (c.getHeader().equals(e.getKey().toString())) {
                            origCol = c;
                            break;
                        }
                    }
                    if (origCol != null) {
                        // values[2] = Double.toString(origCol.getValues().size());
                        // values.numValues = origCol.getValues().size();
                        // //values[3] =
                        // Double.toString(origCol.getColumnStatistic().getDistinctValues());
                        // values.numDistinctValues = (int)
                        // origCol.getColumnStatistic().getDistinctValues();
                        // // if (origCol.getColumnStatistic().getMinimalValue() !=
                        // null) {
                        // // values[4] =
                        // origCol.getColumnStatistic().getMinimalValue().toString();
                        // // }
                        // values.min =
                        // origCol.getColumnStatistic().getMinimalValue();
                        // // if (origCol.getColumnStatistic().getMaximalValue() !=
                        // null) {
                        // // values[5] =
                        // origCol.getColumnStatistic().getMaximalValue().toString();
                        // // }
                        // values.max =
                        // origCol.getColumnStatistic().getMaximalValue();
                        // //values[6] =
                        // Double.toString(origCol.getColumnStatistic().getAverage());
                        // values.average =
                        // origCol.getColumnStatistic().getAverage();
                        // //values[7] =
                        // Double.toString(origCol.getColumnStatistic().getVariance());
                        // values.variance =
                        // origCol.getColumnStatistic().getVariance();
                        // values[8] =
                        // Double.toString(origCol.getColumnStatistic().getStandardDeviation());
                        // values[9] =
                        // Double.toString(origCol.getColumnStatistic().getSkewness());
                        // values[10] =
                        // Double.toString(origCol.getColumnStatistic().getKurtosis());
                        values.setColumnStatistics(origCol);
                        values.dataType = origCol.getDataType().toString();
                    }

                    // values[21] = "false";
                    values.isCorrect = false;
                    // values[22] = e.getValue().toString();
                    values.correctUri = e.getValue().toString();

//                if (correctCls != null) {
//                    TableColumn correctCol = null;
//                    for (TableColumn c : correctCls.getColumns()) {
//                        if (c.getURI().equals(e.getValue().toString())) {
//                            correctCol = c;
//                            break;
//                        }
//                    }
//
//                    if (correctCol != null) {
                    // values[23] =
                    // Double.toString(correctCol.getValues().size());
                    // values[24] =
                    // Double.toString(correctCol.getColumnStatistic().getDistinctValues());
                    // if (correctCol.getColumnStatistic().getMinimalValue()
                    // != null) {
                    // values[25] =
                    // correctCol.getColumnStatistic().getMinimalValue().toString();
                    // }
                    // if (correctCol.getColumnStatistic().getMaximalValue()
                    // != null) {
                    // values[26] =
                    // correctCol.getColumnStatistic().getMaximalValue().toString();
                    // }
                    // values[27] =
                    // Double.toString(correctCol.getColumnStatistic().getAverage());
                    // values[28] =
                    // Double.toString(correctCol.getColumnStatistic().getVariance());
                    // values[29] =
                    // Double.toString(correctCol.getColumnStatistic().getStandardDeviation());
                    // values[30] =
                    // Double.toString(correctCol.getColumnStatistic().getSkewness());
                    // values[31] =
                    // Double.toString(correctCol.getColumnStatistic().getKurtosis());
                    // values[33] = correctCol.getDataType().toString();
//                        values.setCorrectColumnStatistics(correctCol);
//                    }
//                }

                    values.table = getWebtable().getHeader();
                    col.add(values.toStringArray());
                }
            }
        }
        if (getWebtable() != null) {
            // all rows that were not mapped and are not in GS (True Negatives)
            for (TableColumn c : getWebtable().getColumns()) {
                if (!mappedCols.contains(c.getHeader())) {
                    // String[] values = new String[34];
                    values = new PropertyData();
                    values.header = c.getHeader().toString();
                    values.mappedTo = "";
                    // values[2] = Double.toString(c.getValues().size());
                    // values[3] =
                    // Double.toString(c.getColumnStatistic().getDistinctValues());
                    // if (c.getColumnStatistic().getMinimalValue() != null) {
                    // values[4] =
                    // c.getColumnStatistic().getMinimalValue().toString();
                    // }
                    // if (c.getColumnStatistic().getMaximalValue() != null) {
                    // values[5] =
                    // c.getColumnStatistic().getMaximalValue().toString();
                    // }
                    // values[6] =
                    // Double.toString(c.getColumnStatistic().getAverage());
                    // values[7] =
                    // Double.toString(c.getColumnStatistic().getVariance());
                    // values[8] =
                    // Double.toString(c.getColumnStatistic().getStandardDeviation());
                    // values[9] =
                    // Double.toString(c.getColumnStatistic().getSkewness());
                    // values[10] =
                    // Double.toString(c.getColumnStatistic().getKurtosis());
                    values.setColumnStatistics(c);

                    // values[21] = "unmapped";
                    // values[22] = "";
                    // values[33] = c.getDataType().toString();
                    values.isCorrect = true;
                    values.correctUri = "";
                    values.dataType = c.getDataType().toString();

                    values.table = getWebtable().getHeader();
                    col.add(values.toStringArray());
                }
            }
        }

        return col;
    }

    public void write(tableType type) {

        if (!new File("mappings/").exists()) {
            new File("mappings/").mkdir();
        }

        boolean hasInstanceMapping = false;
        for (Correspondence<TableRow> tabC : getInstanceMappings()) {
            if (tabC.getSecond() != null
                    && !tabC.getSecond().getURI().toString().isEmpty()) {
                hasInstanceMapping = true;
                break;
            }
        }
        if (!hasInstanceMapping) {
            return;
        }

        if (getWebtable() != null) {

            if (type == tableType.webtable || type == tableType.lodtable) {
                // Set<Correspondence<TableColumn>> filtered = new HashSet<>();
                //
                // for (Correspondence<TableColumn> tabC :
                // getPropertyMappings()) {
                // if
                // (tabC.getSecond().toString().contains("rdf-syntax-ns#type")
                // || tabC.getFirst().toString().contains("rdf-syntax-ns#type")
                // || tabC.getFirst().toString().contains("core#prefLabel")
                // || tabC.getFirst().toString().contains("owl#sameAs")
                // || tabC.getSecond().toString().contains("owl#sameAs")
                // || tabC.getFirst().toString().equals("URI")
                // || tabC.getSecond().toString().equals("URI") ||
                // tabC.getFirst().toString().contains("rdf-schema#comment")
                // || tabC.getSecond().toString().contains("rdf-schema#comment")
                // || tabC.getFirst().toString().contains("thumbnail") ||
                // tabC.getSecond().toString().contains("thumbnail")
                // || tabC.getFirst().toString().contains("rdf-schema#label") ||
                // tabC.getSecond().toString().contains("rdf-schema#label")
                // || tabC.getFirst().toString().contains("rdf-syntax-ns#label")
                // ||
                // tabC.getSecond().toString().contains("rdf-syntax-ns#label"))
                // {
                // continue;
                // }
                // else {
                // filtered.add(tabC);
                // }
                // }
                // if(filtered.isEmpty()) {
                // return;
                // }
                CSVWriter w = null;

                String name = getWebtable().getHeader().replace(".csv", "")
                        .replace(".gz", "").replace(".tar", "");

                // if (!new File("instances/").exists()) {
                // new File("instances/").mkdir();
                // }
                //
                // // write instance mappings
                // try {
                // w = new CSVWriter(new FileWriter("instances/" + name +
                // ".csv"));
                //
                // String[] header = new String[7];
                // header[0] = "webtable key";
                // header[1] = "dbpedia uri";
                // header[2] = "similarity";
                // header[3] = "correct";
                // header[4] = "correct value";
                // header[5] = "error reason";
                // header[6] = "source";
                // w.writeNext(header);
                //
                // for (String[] values : formatInstancesForCSV()) {
                // w.writeNext(values);
                // }
                //
                // w.close();
                // } catch (IOException e) {
                // e.printStackTrace();
                // }
                //
                // if (!new File("properties/").exists()) {
                // new File("properties/").mkdir();
                // }
                //
                // // write property mappings
                // try {
                // w = new CSVWriter(new FileWriter("properties/" + name +
                // ".csv"));
                //
                // String[] header = new String[5];
                // header[0] = "webtable column";
                // header[1] = "dbpedia property uri";
                // header[2] = "similarity";
                // header[3] = "correct";
                // header[4] = "correct value";
                // w.writeNext(header);
                //
                // for (Correspondence<TableColumn> colC :
                // getPropertyMappings()) {
                //
                // String[] values = new String[5];
                // values[0] = colC.getFirst().getHeader().toString();
                // values[1] = colC.getSecond().getURI().toString();
                // values[2] = Double.toString(colC.getSimilarity());
                // values[3] = Boolean.toString(colC.isCorrect());
                // if (colC.getCorrectValue() != null) {
                // values[4] = colC.getCorrectValue().toString();
                // }
                //
                // w.writeNext(values);
                // }
                //
                // w.close();
                // } catch (IOException e) {
                // e.printStackTrace();
                // }
                //
                // if (!new File("classes/").exists()) {
                // new File("classes/").mkdir();
                // }
                //
                // // write class mappings
                // try {
                // w = new CSVWriter(new FileWriter("classes/" + name +
                // ".csv"));
                //
                // String[] header = new String[5];
                // header[0] = "webtable name";
                // header[1] = "dbpedia class uri";
                // header[2] = "similarity";
                // header[3] = "correct";
                // header[4] = "correct value";
                // w.writeNext(header);
                //
                // for (Correspondence<Table> tabC : getClassMappings()) {
                //
                // String[] values = new String[5];
                // values[0] = tabC.getFirst().getHeader();
                // values[1] = tabC.getSecond().getHeader();
                // values[2] = Double.toString(tabC.getSimilarity());
                // values[3] = Boolean.toString(tabC.isCorrect());
                // if (tabC.getCorrectValue() != null) {
                // values[4] = tabC.getCorrectValue().toString();
                // }
                //
                // w.writeNext(values);
                // }
                //
                // w.close();
                //
                // } catch (IOException e) {
                // e.printStackTrace();
                // }
                // write output file with table + correspondences
                try {
                    BufferedWriter write = new BufferedWriter(new FileWriter(
                            "mappings/" + name + ".csv"));
                    if (uriIndex != null) {
                        String shortName = name;
                        if (name.contains("#")) {
                            shortName = name.split("#")[1];
                        }
                        WebPageIndex resIndex = new WebPageIndex(uriIndex,
                                WebPageEntry.ID_FIELD);
                        List<WebPageEntry> matches = resIndex.search(shortName);
                        if (matches.size() > 0) {
                            write.write("#source=" + matches.get(0).getUri()
                                    + "\n");
                        }

                        if (matches.isEmpty()) {
                            write.write("#source=\n");
                        }
                    } else {
                        write.write("#source=" + getWebtable().getSource()
                                + "\n");
                    }
                    for (Correspondence<Table> tabC : getClassMappings()) {
                        write.write("#class=http://dbpedia.org/ontology/"
                                + tabC.getSecond().getHeader()
                                .replace(".csv", "").replace(".gz", "")
                                + "\n");
                    }

                    for (Correspondence<Table> tabC : getClassMappings()) {
                        write.write("#classConf="
                                + Double.toString(tabC.getSimilarity()) + "\n");
                    }

                    write.write("#numHeaderRows=" + webtable.getNumHeaderRows()
                            + "\n");

                    Map<String, List<Correspondence<TableColumn>>> propHeaderCorres = new HashMap<>();
                    for (Correspondence<TableColumn> tabC : getPropertyMappings()) {
                        if (propHeaderCorres.containsKey(tabC.getFirst()
                                .getHeader())) {
                            propHeaderCorres.get(tabC.getFirst().getHeader())
                                    .add(tabC);
                        } else {
                            List<Correspondence<TableColumn>> list = new ArrayList<>();
                            list.add(tabC);
                            propHeaderCorres.put(tabC.getFirst().getHeader().toString(),
                                    list);
                        }
                    }

                    String props = "";
                    String propsCorres = "";
                    int i = 0;
                    Set<Correspondence<TableColumn>> checkedCorres = new HashSet<>();
                    for (TableColumn tc : webtable.getColumns()) {
                        boolean found = false;
                        String columnHeader = tc.getHeader().toString();
                        for (Correspondence<TableColumn> tabC : getPropertyMappings()) {
                            if (checkedCorres.contains(tabC)) {
                                continue;
                            }
                            if (tabC.getFirst().getHeader()
                                    .equals(columnHeader)) {
                                if (i == 0) {
                                    props += "#properties={";
                                    propsCorres += "#propertyScores={";
                                }
                                // comment if only 1:1 mappings used
                                if (propHeaderCorres.get(
                                        tabC.getFirst().getHeader()).size() > 1) {
                                    for (Correspondence<TableColumn> cor : propHeaderCorres
                                            .get(tabC.getFirst().getHeader())) {
                                        props += cor.getSecond().getURI() + ";";
                                        propsCorres += Double.toString(cor
                                                .getSimilarity()) + ";";
                                        checkedCorres.add(cor);
                                    }
                                } else {
                                    props += tabC.getSecond().getURI();
                                    propsCorres += Double.toString(tabC
                                            .getSimilarity());
                                }

                                if (i == webtable.getColumns().size() - 1) {
                                    props += "}";
                                    propsCorres += "}";
                                } else {
                                    props += "|";
                                    propsCorres += "|";
                                }
                                if (props.contains(";|")) {
                                    props = props.replace(";|", "|");
                                }
                                if (props.contains(";}")) {
                                    props = props.replace(";}", "}");
                                }
                                if (propsCorres.contains(";|")) {
                                    propsCorres = propsCorres
                                            .replace(";|", "|");
                                }
                                if (propsCorres.contains(";}")) {
                                    propsCorres = propsCorres
                                            .replace(";}", "}");
                                }
                                i++;
                                found = true;
                                break;
                                //
                                // if (i == 0) {
                                // props +=
                                // "#properties={http://dbpedia.org/ontology/" +
                                // tabC.getSecond().getHeader().replace("_label",
                                // "") + "|";
                                // propsCorres += "#propertyScores={" +
                                // Double.toString(tabC.getSimilarity()) + "|";
                                // } else if (i == webtable.getColumns().size()
                                // - 1) {
                                // props += "http://dbpedia.org/ontology/" +
                                // tabC.getSecond().getHeader().replace("_label",
                                // "") + "}";
                                // propsCorres +=
                                // Double.toString(tabC.getSimilarity()) + "}";
                                // } else {
                                // props += "http://dbpedia.org/ontology/" +
                                // tabC.getSecond().getHeader().replace("_label",
                                // "") + "|";
                                // propsCorres +=
                                // Double.toString(tabC.getSimilarity()) + "|";
                                // }
                                // i++;
                                // found = true;
                                // break;
                            }
                        }
                        if (!found) {
                            if (i == 0) {
                                props += "#properties={|";
                                propsCorres += "#propertyScores={|";
                            } else if (i == webtable.getColumns().size() - 1) {
                                props += "}";
                                propsCorres += "}";
                            } else {
                                props += "|";
                                propsCorres += "|";
                            }
                            i++;
                        }
                    }

                    write.write(props + "\n");
                    write.write(propsCorres + "\n");

                    String inst = "";
                    String instCorres = "";

                    for (int rowNum = 0; rowNum < webtable.getTotalNumOfRows(); rowNum++) {
                        // for (Integer rowNum :
                        // webtable.getKey().getValues().keySet()) {
                        boolean found = false;
                        // Collections.sort(getInstanceMappings());
                        for (Correspondence<TableRow> tabC : getInstanceMappings()) {
                            if (tabC.getFirst().getRowIndex() == rowNum) {
                                if (rowNum == 0) {
                                    inst += "#instances={"
                                            + tabC.getSecond().getURI() + "|";
                                    instCorres += "#instanceScores={"
                                            + Double.toString(tabC
                                            .getSimilarity()) + "|";
                                    // avoid problems with tables only
                                    // containing 1 row
                                    if (rowNum == webtable.getTotalNumOfRows() - 1) {
                                        inst += "}";
                                        instCorres += "}";
                                    }
                                } else if (rowNum == webtable
                                        .getTotalNumOfRows() - 1) {
                                    inst += tabC.getSecond().getURI() + "}";
                                    instCorres += Double.toString(tabC
                                            .getSimilarity()) + "}";
                                } else {
                                    inst += tabC.getSecond().getURI() + "|";
                                    instCorres += Double.toString(tabC
                                            .getSimilarity()) + "|";
                                }
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            if (rowNum == 0) {
                                inst += "#instances={|";
                                instCorres += "#instanceScores={|";
                                if (rowNum == webtable.getTotalNumOfRows() - 1) {
                                    inst += "}";
                                    instCorres += "}";
                                }
                            } else if (rowNum == webtable.getTotalNumOfRows() - 1) {
                                inst += "}";
                                instCorres += "}";
                            } else {
                                inst += "|";
                                instCorres += "|";
                            }
                        }
                    }
                    write.write(inst + "\n");
                    write.write(instCorres + "\n");

                    write.write("#keyColumn="
                            + Integer.toString(webtable.getKeyIndex()) + "\n");

                    i = 0;
                    String datatypes = "";
                    for (TableColumn tc : webtable.getColumns()) {
                        if (i == 0) {
                            datatypes += "#dataTypes={";
                        }
                        datatypes += tc.getDataType();
                        if (i == webtable.getColumns().size() - 1) {
                            datatypes += "}";
                        } else {
                            datatypes += "|";
                        }
                        i++;
                    }
                    write.write(datatypes + "\n");

                    // w.write(String.format("%s=%s\n", DATA_TYPES,
                    // ListHandler.formatList(types)));
                    write.flush();
                    write.close();

                    BufferedWriter writeTable = new BufferedWriter(
                            new FileWriter("mappings/" + name + ".csv", true));

                    Reader read;
                    if (webtable.getFullPath().endsWith(".gz")) {
                        read = new InputStreamReader(new GZIPInputStream(
                                new FileInputStream(webtable.getFullPath())),
                                "UTF-8");
                    } else {
                        read = new InputStreamReader(new FileInputStream(
                                webtable.getFullPath()), "UTF-8");
                    }
                    BufferedReader readTable = new BufferedReader(read);

                    String line = readTable.readLine();

                    while (line != null) {
                        writeTable.write(line + "\n");
                        line = readTable.readLine();
                    }
                    readTable.close();
                    writeTable.flush();
                    writeTable.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (type == tableType.jsonWebTable) {
                try {

                    String name = getWebtable().getHeader()
                            .replace(".json", "");

                    // create the Mapping
                    TableMapping tm = TableMapping.fromTable(getWebtable());
                    Correspondence<Table> cls = classMappings.iterator().next();
                    if (cls == null) {
                        return;
                    }
                    tm.setMappedClass(new Pair<String, Double>(String.format(
                            "http://dbpedia.org/ontology/%s",
                            cls.getSecond().getHeader().replace(".csv", "")
                            .replace(".gz", "")), cls.getSimilarity()));
                    for (Correspondence<TableColumn> cor : getPropertyMappings()) {
                        int colIdx = getWebtable().getColumns().indexOf(
                                cor.getFirst());
                        Pair<String, Double> match = new Pair<String, Double>(
                                cor.getSecond().getURI(), cor.getSimilarity());
                        tm.getMappedProperties().put(colIdx, match);
                    }
                    for (Correspondence<TableRow> cor : getInstanceMappings()) {
                        Pair<String, Double> match = new Pair<String, Double>(
                                cor.getSecond().getURI().toString(),
                                cor.getSimilarity());
                        tm.getMappedInstances().put(
                                cor.getFirst().getRowIndex(), match);
                    }

                    // load the original table data
                    TableData tbl = TableData.fromJson(new File(getWebtable()
                            .getFullPath()));

                    // write the table and the mapping
                    AnnotatedTable atbl = new AnnotatedTable();
                    atbl.setTable(tbl);
                    atbl.setMapping(tm);
                    atbl.writeJson(new File("mappings/" + name + ".json"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public EvaluationResult evaluateClass(EvaluationParameters p,
            Table webtable, GoldStandard gold, boolean verbose) {
//        Map<Object, List<Object>> goldStandard = new HashMap<Object, List<Object>>();

        try {
            // read all superclasses to allow a correct match when a superclass
            // was matched
//            Map<String, String> superClassMap = new HashMap<>();
//            Collection<String[]> superclasses = CSVUtils.readCSV(
//                    p.getClassHierarchyLocation(), "\t");
//            for (String[] s : superclasses) {
//                superClassMap.put(
//                        s[0].replace("http://dbpedia.org/ontology/", "")
//                        .toLowerCase(),
//                        s[1].replace("http://dbpedia.org/ontology/", "")
//                        .toLowerCase());
//            }
//
//            Collection<String[]> corres = CSVUtils.readCSV(p
//                    .getClassGoldStandardLocation());
//            String webHeader;
//            if (webtable.getHeader().contains(".csv")) {
//                webHeader = webtable.getHeader().split("\\.csv")[0];
//            } else {
//                webHeader = webtable.getHeader().split("\\.")[0];
//            }
//
//            for (String[] s : corres) {
//                String dbpHeader;
//                if (s[0].contains(".csv")) {
//                    dbpHeader = s[0].split("\\.csv")[0];
//                } else {
//                    dbpHeader = s[0].split("\\.")[0];
//                }
//                // System.out.println("web: " + webHeader + " dbpedia " +
//                // dbpHeader);
//
//                List<Object> allClasses = new ArrayList<>();
//                if (webHeader != null && dbpHeader != null
//                        && webHeader.equals(dbpHeader)) {
//                    if (s[1].toLowerCase().contains(" ")) {
//                        s[1] = s[1].toLowerCase().replace(" ", "");
//                    }
//                    allClasses.add(s[1].toLowerCase());
//                    if (superClassMap.containsKey(s[1].toLowerCase())) {
//                        if ((s[1]).contains(" ")) {
//                            s[1] = s[1].toLowerCase().replace(" ", "");
//                        }
//                        allClasses.add(superClassMap.get(s[1].toLowerCase())
//                                .toLowerCase());
//                    }
//                    goldStandard.put(dbpHeader, allClasses);
//                }
//            }

            MatchingEvaluator eval = new MatchingEvaluator();
            eval.setVerbose(verbose);
            EvaluationAdapter<Table> evalRow = new ClassAdapter();
            EvaluationAdapter<Table> evalInstance = new DBpediaClassAdapter();

            EvaluationResult results = eval.evaluateMatching(
                    getClassMappings(), gold.getClassGoldStandard(), 1, evalRow, evalInstance);

            System.out.println("class prec: " + results.getPrecision()
                    + " rec: " + results.getRecall() + " f-mea: "
                    + results.getF1Score());

            getEvaluation().setClassResult(results);

            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return new EvaluationResult(0, 0, 0, 0);
        }
    }

    public EvaluationResult evaluateInstances(boolean verbose,
            GoldStandard gold, Table webtable, String name) {
        MatchingEvaluator eval = new MatchingEvaluator();
        eval.setVerbose(verbose);
        EvaluationAdapter<TableRow> evalRow = new CandidateAdapter();
        EvaluationAdapter<TableRow> evalInstance = new DBpediaInstanceAdapter();
        Map<Object, List<Object>> tmpList = new HashMap<>();

        double keyCounter = 0.0;
        for (Object o : gold.getInstanceGoldStandard().keySet()) {
            List<Object> tmp = new ArrayList<>();
            tmp.add(gold.getInstanceGoldStandard().get(o));
            tmpList.put(o, tmp);

            if (webtable.isHasKey()) {
                for (Object keyO : webtable.getKey().getValues().values()) {
                    Integer rowNumb = Integer.parseInt(o.toString());
                    if (keyO.equals(webtable.getKey().getValues().get(rowNumb))) {
                        keyCounter++;
                    }
                }
            }
        }
        if (keyCounter > (double) gold.getInstanceGoldStandard().size() * 0.9) {
            getEvaluation().setCorrectKey(1);
        }

        if (gold == null || webtable.getKey() == null) {
            return new EvaluationResult(0, 0, 0, 0);
        }
        EvaluationResult results = eval.evaluateMatching(getInstanceMappings(),
                tmpList, webtable.getKey().getValues().size(), evalRow,
                evalInstance);

        System.out.println(String.format(
                "[%s] instance precision: %.4f recall: %.4f f-measure: %.4f",
                name, results.getPrecision(), results.getRecall(),
                results.getF1Score()));

        getEvaluation().setInstanceResult(results);

        return results;
    }

    public EvaluationResult evaluateProperties(boolean verbose,
            GoldStandard gold, Table webtable) {
        MatchingEvaluator eval = new MatchingEvaluator();
        eval.setVerbose(verbose);
        EvaluationAdapter<TableColumn> evalRow = new ColumnAdapter();
        EvaluationAdapter<TableColumn> evalInstance = new DBpediaPropertyAdapter(
                gold.getPropertyCanoniser());

        if (gold == null || webtable.getColumns() == null) {
            return new EvaluationResult(0, 0, 0, 0);
        }

        Map<Object, List<Object>> tmpList = new HashMap<>();

        for (Object o : gold.getPropertyGoldStandard().keySet()) {
            List<Object> tmp = new ArrayList<>();
            // System.out.println("gold o: " + o + " --- " +
            // gold.getPropertyGoldStandard().get(o));
            if (gold.getPropertyGoldStandard().get(o).toString()
                    .contains("rdf-syntax-ns#type")
                    || o.toString().contains("rdf-syntax-ns#type")
                    || gold.getPropertyGoldStandard().get(o).toString()
                    .contains("rdf-syntax-ns#label")
                    || o.toString().contains("rdf-syntax-ns#label")
                    || o.toString().contains("isPreferredMeaningOf")
                    || o.toString().contains("core#prefLabel")
                    || o.toString().contains("owl#sameAs")
                    || gold.getPropertyGoldStandard().get(o).toString()
                    .contains("owl#sameAs")
                    || gold.getPropertyGoldStandard().get(o).toString()
                    .equals("URI")
                    || o.toString().equals("URI")
                    || o.toString().contains("thumbnail")
                    || gold.getPropertyGoldStandard().get(o).toString()
                    .contains("thumbnail")
                    || o.toString().contains("rdf-schema#label")
                    || gold.getPropertyGoldStandard().get(o).toString()
                    .contains("rdf-schema#label")
                    || o.toString().contains("longName")
                    || gold.getPropertyGoldStandard().get(o).toString()
                    .contains("longName")
                    || o.toString().contains("dbpedia.org/ontology/type")
                    || gold.getPropertyGoldStandard().get(o).toString()
                    .contains("dbpedia.org/ontology/type")) {
                continue;
            }
            tmp.add(gold.getPropertyGoldStandard().get(o));
            tmpList.put(o, tmp);
        }
        Collection<Correspondence<TableColumn>> newPropMappings = new HashSet<>();

        for (Correspondence c : getPropertyMappings()) {
            // System.out.println("c: " + c.getFirst() + " --- " +
            // c.getSecond());
            if (c.getSecond().toString().contains("rdf-syntax-ns#type")
                    || c.getFirst().toString().contains("rdf-syntax-ns#type")
                    || c.getFirst().toString().contains("isPreferredMeaningOf")
                    || c.getFirst().toString().contains("core#prefLabel")
                    || c.getFirst().toString().contains("owl#sameAs")
                    || c.getSecond().toString().contains("owl#sameAs")
                    || c.getFirst().toString().equals("URI")
                    || c.getSecond().toString().equals("URI")
                    || c.getFirst().toString().contains("rdf-schema#comment")
                    || c.getSecond().toString().contains("rdf-schema#comment")
                    || c.getFirst().toString().contains("thumbnail")
                    || c.getSecond().toString().contains("thumbnail")
                    || c.getFirst().toString().contains("rdf-schema#label")
                    || c.getSecond().toString().contains("rdf-schema#label")
                    || c.getFirst().toString().contains("longName")
                    || c.getSecond().toString().contains("longName")
                    || c.getFirst().toString()
                    .contains("dbpedia.org/ontology/type")
                    || c.getSecond().toString()
                    .contains("dbpedia.org/ontology/type")
                    || c.getFirst().toString().contains("rdf-syntax-ns#label")
                    || c.getSecond().toString().contains("rdf-syntax-ns#label")) {
                continue;
            }
            newPropMappings.add(c);
        }

        setPropertyMappings(newPropMappings);

        EvaluationResult results = eval.evaluateMatching(newPropMappings,
                tmpList, webtable.getColumns().size(), evalRow, evalInstance);

        System.out.println("Property prec: " + results.getPrecision()
                + " rec: " + results.getRecall() + " f-mea: "
                + results.getF1Score());

        getEvaluation().setPropertyResult(results);

        return results;
    }

    public EvaluationResult evaluatePropertyRanges(boolean verbose,
            GoldStandard gold, Table webtable) {
        if (gold == null) {
            return new EvaluationResult(0, 0, 0, 0);
        }
        MatchingEvaluator eval = new MatchingEvaluator();
        eval.setVerbose(verbose);
        EvaluationAdapter<TableColumn> evalRow = new ColumnAdapter();
        EvaluationAdapter<TableColumn> evalInstance = new DBpediaPropertyAdapter(
                gold.getPropertyRangeCanoniser());

        Collection<Correspondence<TableColumn>> objectPropertyMappings = new ArrayList<>();

        // Correspondence<TableColumn> classCorres = null;
        // add the class of the table as property range for the key column
        // for (TableColumn c : webtable.getColumns()) {
        // if (c.isKey()) {
        // if (getClassMappings() != null &&
        // getClassMappings().iterator().hasNext()) {
        // Correspondence<Table> detectedClass =
        // getClassMappings().iterator().next();
        // String dbpediaClass =
        // detectedClass.getSecond().toString().replace(".csv", "");
        // dbpediaClass = "http://dbpedia.org/ontology/" + dbpediaClass;
        // TableColumn tc = new TableColumn(detectedClass.getSecond());
        // tc.setHeader(dbpediaClass);
        // classCorres = new Correspondence(c, tc,
        // detectedClass.getSimilarity());
        // System.out.println("detected class for key column: " +
        // evalRow.getUniqueIdentifier(c) + " === " + dbpediaClass);
        // }
        // }
        // }
        for (Correspondence<TableColumn> c : getPropertyMappings()) {
            if (c.getFirst().isKey()) {
                continue;
            }

            // only check the object properties for the range evaluation!
            for (Object o : gold.getPropertyRangeGoldStandard().keySet()) {
                String currentKey = (String) o;
                // if(classCorres != null &&
                // evalRow.getUniqueIdentifier(classCorres.getFirst()).equals(currentKey))
                // {
                // objectPropertyMappings.add(classCorres);
                // }
                if (evalRow.getUniqueIdentifier(c.getFirst())
                        .equals(currentKey)) {
                    objectPropertyMappings.add(c);
                }
            }
        }
        setPropertyRangeMappings(objectPropertyMappings);

        Map<Object, List<Object>> tmpList = new HashMap<>();

        for (Object o : gold.getPropertyRangeGoldStandard().keySet()) {
            List<Object> tmp = new ArrayList<>();
            tmp.add(gold.getPropertyRangeGoldStandard().get(o));
            tmpList.put(o, tmp);
        }

        EvaluationResult results = eval.evaluateMatching(
                objectPropertyMappings, tmpList, gold
                .getPropertyRangeGoldStandard().size(), evalRow,
                evalInstance);

        System.out.println("PropertyRange prec: " + results.getPrecision()
                + " rec: " + results.getRecall() + " f-mea: "
                + results.getF1Score());

        getEvaluation().setPropertyRangeResult(results);

        return results;
    }
}
