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
package de.dwslab.T2K.tableprocessor.IO;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import de.dwslab.T2K.normalisation.StringNormalizer;

import de.dwslab.T2K.tableprocessor.ColumnType;
import de.dwslab.T2K.tableprocessor.ColumnTypeGuesser;
import de.dwslab.T2K.tableprocessor.TableKeyIdentifier;
import de.dwslab.T2K.tableprocessor.model.json.TableData.TableOrientation;
import de.dwslab.T2K.tableprocessor.model.json.TableData.TableType;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumnBuilder;
import de.dwslab.T2K.tableprocessor.model.json.TableData;
import de.dwslab.T2K.units.UnitParser;
import de.dwslab.T2K.units.Unit;
import de.dwslab.T2K.util.Variables;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.data.Pair;

/**
 * Parses a table from the WDCv2 JSON data format
 *
 * @author Oliver
 *
 */
public class JsonTableParser {

    private boolean cleanHeader = true;

    public boolean isCleanHeader() {
        return cleanHeader;
    }

    public void setCleanHeader(boolean cleanHeader) {
        this.cleanHeader = cleanHeader;
    }
    private ColumnTypeGuesser typeGuesser = new ColumnTypeGuesser();

    public static void main(String[] args) {
        TableReader tr = new TableReader();

        Table t = tr.readWebTableFromJson(args[0]);

//        System.out.println(t.getHeader());
//        System.out.println(t.printTable());
    }

    public Table parseJson(File file) {
        FileReader fr;
        Table t = null;
        try {
            fr = new FileReader(file);

            t = parseJson(fr, file.getName());

            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return t;
    }

    public Table parseJson(Reader reader, String fileName) {
        Gson gson = new Gson();

        // get the data from the JSON source
        TableData data = gson.fromJson(reader, TableData.class);

        return parseJson(data, fileName);
    }

    public Table parseJson(TableData data, String fileName) {
        UnitParser.readInUnits();

        if (data.getTableType() != TableType.RELATION) {
            //return null;
        }

        if (data.getTableOrientation() == TableOrientation.VERTICAL) {
            // flip table
  //          data.transposeRelation();
            //return null;
        }

        // create a new table
        Table t = new Table();

        t.setSource(data.getUrl());
        t.setContextTimestamp(data.getTableContextTimeStampBeforeTable());
        t.setContextBeforeTable(data.getTextBeforeTable());
        t.setContextAfterTable(data.getTextAfterTable());
        t.setHeader(fileName);
        t.setPageTitle(data.getPageTitle());
        t.setTableTitle(data.getTitle());

        // determine the total number of rows
        int ttlNumRows = 0;
        for (int col = 0; col < data.getRelation().length; col++) {
            ttlNumRows = Math.max(ttlNumRows, data.getRelation()[col].length);
        }
        t.setTotalNumOfRows(ttlNumRows);

        // create the table columns
        List<Pair<TableColumnBuilder, String[]>> columns = getColumnData(data, t);

        final int numRowsToSkip = data.getNumberOfHeaderRows();
        //final int numRowsToSkip = 1;
        t.setNumHeaderRows(numRowsToSkip);

        // fill the columns with values
        new Parallel<Pair<TableColumnBuilder, String[]>>(1).tryForeach(columns, new Consumer<Pair<TableColumnBuilder, String[]>>() {
            @Override
            public void execute(Pair<TableColumnBuilder, String[]> parameter) {

                for (int row = numRowsToSkip; row < parameter.getSecond().length; row++) {

                    String value = parameter.getSecond()[row];

                    processValue(parameter.getFirst(), value, row);

                }

            }
        });

        // complete the column construction (decide final data type, etc.)
        for (Pair<TableColumnBuilder, String[]> p : columns) {
            p.getFirst().buildColumn();
        }
        
        TableKeyIdentifier keyIdentifier = new TableKeyIdentifier();
        keyIdentifier.identifyKeys(t);
        
//        if(data.getTableType() != TableType.RELATION || data.getTableOrientation() == TableOrientation.VERTICAL) {
//            TableKeyIdentifier keyIdentifier = new TableKeyIdentifier();
//            keyIdentifier.identifyKeys(t);
//        }
//        else if (data.getKeyColumnIndex() != -1) {
//            t.getColumns().get(data.getKeyColumnIndex()).setKey(true);
//            t.setHasKey(true);
//        }
//        else {
//            t.setHasKey(false);
//        }

        return t;
    }

    protected List<Pair<TableColumnBuilder, String[]>> getColumnData(TableData data, Table table) {
        String[] columnNames = data.getColumnHeaders();

        List<Pair<TableColumnBuilder, String[]>> columns = new LinkedList<>();

        for (int colIdx = 0; colIdx < data.getRelation().length; colIdx++) {
            String columnName = null;

            if (columnNames != null && columnNames.length > colIdx) {
                columnName = columnNames[colIdx];
            } else {
                columnName = "";
            }

            TableColumnBuilder b = new TableColumnBuilder(table);

            // set the header
            String header = columnName;
            if (isCleanHeader()) {
                header = StringNormalizer.normaliseHeader(columnName);
            }
            b.setHeader(header);

            // parse units from headers
            Unit unit = UnitParser.parseUnitFromHeader(columnName);
            b.setUnitFromHeader(unit);

            columns.add(new Pair<TableColumnBuilder, String[]>(b, data.getRelation()[colIdx]));
            table.getColumns().add(b.getColumn());
        }
        //table.setNumHeaderRows(1);

        return columns;
    }

    protected void processValue(TableColumnBuilder b, String value, int row) {
        if (value.contains("&mdash")) {
            value = value.replace("&mdash", "-");
        }

        // handle lists
        if (ListHandler.checkIfList(value)) {
            //valueType = ColumnDataType.list;
            // split values, normalise, determine type, ...
        } else {
            // normalise the value
            if (Variables.normalizeValues) {
                value = StringNormalizer.normaliseValue(value, false);
            }
        }

        if (!value.equalsIgnoreCase(StringNormalizer.nullValue) && !value.isEmpty()) {
            // guess the type 
            ColumnType valueType = typeGuesser.guessTypeForValue(value, b.getUnitFromHeader());

            if (ListHandler.checkIfList(value)) {
                List<String> columnValues = Arrays.asList(ListHandler.splitList(value));
                b.addValue(row, columnValues, valueType);
            } else {
                b.addValue(row, value, valueType);
            }
        }
    }
}
