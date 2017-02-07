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
package de.dwslab.T2K.tableprocessor.model;

import de.dwslab.T2K.normalisation.StringNormalizer;
import de.dwslab.T2K.tableprocessor.ColumnType;
import de.dwslab.T2K.tableprocessor.IO.parsers.DateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.dwslab.T2K.tableprocessor.model.TableColumn.ColumnDataType;
import de.dwslab.T2K.units.UnitParser;
import de.dwslab.T2K.units.Unit;
import de.dwslab.T2K.util.Variables;
import java.text.NumberFormat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

public class TableColumnBuilder {

    private TableColumn column;
    private Map<Integer, ColumnDataType> types;
    private Map<Integer, Unit> units;
    private int numRows;
    private Unit unitFromHeader;

    public TableColumnBuilder(Table table) {
        column = new TableColumn(table);
        types = new HashMap<>();
        numRows = 0;
        units = new HashMap<>();
    }

    public void setHeader(String header) {
        column.setHeader(header);
    }

    public String getHeader() {
        return column.getHeader().toString();
    }

    public void setDataSource(String dataSource) {
        column.setDataSource(dataSource);
    }

    public void setUri(String uri) {
        column.setURI(uri);
    }

    public void setDataType(ColumnDataType type) {
        column.setDataType(type);
    }

    public ColumnDataType getDataType() {
        return column.getDataType();
    }

    public Unit getUnitFromHeader() {
        return unitFromHeader;
    }

    public void setUnitFromHeader(Unit unitFromHeader) {
        this.unitFromHeader = unitFromHeader;
    }

    public void addValue(int rowIndex, Object value, ColumnType type) {
        synchronized (this) {
            if (value instanceof List) {
                boolean allEmpty = true;
                for (String s : (List<String>) value) {
                    if (!s.equalsIgnoreCase(StringNormalizer.nullValue) && !s.isEmpty()) {
                        allEmpty = false;
                        break;
                    }
                }
                if (!allEmpty) {
                    // add the value
                    column.values.put(rowIndex, value);
                    // add the type
                    types.put(rowIndex, type.getType());
                    units.put(rowIndex, type.getUnit());
                }
            } else {
                String stringValue = (String) value;
                if (!stringValue.equalsIgnoreCase(StringNormalizer.nullValue) && !stringValue.isEmpty()) {
                    // add the value
                    column.values.put(rowIndex, value);
                    // add the type
                    types.put(rowIndex, type.getType());
                    units.put(rowIndex, type.getUnit());
                }
            }
            numRows++;
        }
    }

    protected Map<ColumnDataType, Integer> getColumnValueTypes() {
        Map<ColumnDataType, Integer> counts = new HashMap<>();

        for (ColumnDataType t : types.values()) {
            Integer cnt = counts.get(t);

            if (cnt == null) {
                cnt = 0;
            }

            counts.put(t, cnt + 1);
        }

        return counts;
    }

    public TableColumn getColumn() {
        return column;
    }

    /**
     * Call this method after all values have been added to the column to decide
     * the final data type and unit and to convert the string values into the
     * correct data types.
     */
    public void buildColumn() {
        column.setNumRows(numRows);

        Map<ColumnDataType, Integer> columnValueTypes = getColumnValueTypes();

        //determine majority of datatypes
        if (columnValueTypes.size() > 0) {
            int maxCount = columnValueTypes.values().iterator().next();
            ColumnDataType type = columnValueTypes.keySet().iterator().next();
            for (Entry<ColumnDataType, Integer> entry : columnValueTypes.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    type = entry.getKey();
                }
            }
            //WHY?
            // check if it is boolean if it contains different values than
            // binary
//            if (type == ColumnDataType.bool) {
//                for (Object val : column.getValues().values()) {
//                    if (!val.equals("0") && !val.equals("1")
//                            && !val.toString().toLowerCase().equals("true")
//                            && !val.toString().toLowerCase().equals("false")) {
//                        type = ColumnDataType.numeric;
//                    }
//                }
//            }
            // set the final column data type
            if (type == ColumnDataType.unit) {
                setDataType(ColumnDataType.numeric);
            } else {
                setDataType(type);
            }
            // convert the strings into the correct data type
            typeValues();
            Statistic s = new Statistic();
            computeStatistics(s);
            column.setColumnStatistic(s);
        }
    }

    private void computeStatistics(Statistic s) {
        switch (column.getDataType()) {
            case string:
                Set<String> allValues = new HashSet<>();
                for (Object o : column.getValues().values()) {
                    if (o instanceof List) {
                        for (Object innerObject : (List) o) {
                            allValues.add(innerObject.toString());
                        }
                    } else {
                        allValues.add(o.toString());
                    }
                }
                double sum = 0.0;
                for (String value : allValues) {
                    sum += value.length();
                }
                s.setAverageValueLength((double) sum / allValues.size());
                break;
            case numeric:
                SynchronizedDescriptiveStatistics statistics = new SynchronizedDescriptiveStatistics();
                statistics.setWindowSize(-1);
                for (Object o : column.getValues().values()) {
                    if (o instanceof List) {
                        for (Object innerObject : (List) o) {
                            statistics.addValue((double) innerObject);
                        }
                    } else {
                        statistics.addValue((double) o);
                    }
                }
                Set<Double> distinctValues = new HashSet<>();
                for (double value : statistics.getValues()) {
                    distinctValues.add(value);
                }
                s.setVariance(statistics.getVariance());
                s.setSkewness(statistics.getSkewness());
                s.setKurtosis(statistics.getKurtosis());
                s.setDistinctValues((double) distinctValues.size());
                s.setStandardDeviation(statistics.getStandardDeviation());
                s.setAverage(statistics.getMean());
                s.setMaximalValue(statistics.getMax());
                s.setMinimalValue(statistics.getMin());
                break;
            case date:
                Date minimal = new Date(0);
                for (Object object : column.getValues().values()) {
                    if (object instanceof List) {
                        for (Object listObject : (List) object) {
                            if (((Date) listObject).before(minimal)) {
                                minimal = (Date) listObject;
                            }
                        }
                    } else {
                        Date d;
                        try {
                            d = (Date) object;
                        } catch (Exception e) {
                            continue;
                        }
                        if (d.before(minimal)) {
                            minimal = (Date) object;
                        }
                    }
                }
                s.setMinimalValue(minimal);

                Date maximal = new Date(0, 0, 1);
                for (Object object : column.getValues().values()) {
                    if (object instanceof List) {
                        for (Object listObject : (List) object) {
                            if (((Date) listObject).after(maximal)) {
                                maximal = (Date) listObject;
                            }
                        }
                    } else {
                        try {
                            if (((Date) object).after(maximal)) {
                                maximal = (Date) object;
                            }
                        } catch (ClassCastException ce) {
                        }
                    }
                }
                s.setMaximalValue(maximal);
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("rawtypes")
    public void typeValues() {
        Set<Integer> removeValues = new HashSet<>();
        for (Entry<Integer, Object> entry : column.getValues().entrySet()) {

            //handle lists by typing all values in the list
            if (entry.getValue() instanceof List) {
                List typedValues = new ArrayList();
                List untypedValues = (List) entry.getValue();

                for (Object untyped : untypedValues) {
                    Object typed = null;

                    try {
                        typed = typeValue(untyped, units.get(entry.getKey()));
                    } catch (Exception e) {
                    }

                    if (typed != null) {
                        typedValues.add(typed);
                    }
                }

                if (typedValues.size() > 0) {
                    column.getValues().put(entry.getKey(), typedValues);
                } else {
                    removeValues.add(entry.getKey());
                }
            } else {
                try {
                    Object typedValue = null;
                    typedValue = typeValue(entry.getValue(), units.get(entry.getKey()));
                    if (typedValue != null) {
                        column.getValues().put(entry.getKey(), typedValue);
                    } else {
                        // remove the value if it could not be typed
                        removeValues.add(entry.getKey());
                    }
                } catch (Exception e) {
                    removeValues.add(entry.getKey());
                }
            }
        }
        //remove all values that cannot be typed at all
        for (int i : removeValues) {
            column.getValues().remove(i);
        }
    }

    private Object typeValue(Object entry, Unit unit) throws ParseException {
        Object typedValue = null;
        switch (getDataType()) {
            case string:
                typedValue = entry.toString();
                break;
            case date:
                typedValue = DateUtil.parse(entry.toString());
                break;
            case numeric:
                //TODO: how to handle nummers with commas (German style)
                if (Variables.useUnitDetection && unit != null) {
                    typedValue = UnitParser.transformUnit(entry.toString(), unit);

                } else {
                    String value = entry.toString().replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
                    NumberFormat format = NumberFormat.getInstance(Locale.US);
                    Number number = format.parse(value);
                    typedValue = number.doubleValue();
                }
                break;
            case bool:
                typedValue = Boolean.parseBoolean(entry.toString());
                break;
            case coordinate:
                typedValue = entry.toString();
                break;
            case link:
                typedValue = entry.toString();
            default:
                break;
        }
        return typedValue;
    }
}
