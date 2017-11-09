package de.dwslab.T2K.tableprocessor.IO;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;

import de.dwslab.T2K.tableprocessor.ColumnType;
import de.dwslab.T2K.tableprocessor.ColumnTypeGuesser;
import de.dwslab.T2K.tableprocessor.TableKeyIdentifier;
import de.dwslab.T2K.tableprocessor.model.json.TableData.TableOrientation;
import de.dwslab.T2K.tableprocessor.model.json.TableData.TableType;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumnBuilder;
import de.dwslab.T2K.tableprocessor.model.TableMapping;
import de.dwslab.T2K.tableprocessor.model.json.AnnotatedTable;
import de.dwslab.T2K.tableprocessor.model.json.TableData;
import de.dwslab.T2K.units.UnitParser;
import de.dwslab.T2K.units.Unit;
import de.dwslab.T2K.util.Variables;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.data.Pair;
import de.uni_mannheim.informatik.dws.t2k.normalisation.StringNormalizer;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;

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

    public static void main(String[] args) throws UnsupportedEncodingException {
        TableReader tr = new TableReader();

        Table t = tr.readWebTableFromJson(args[0]);

//        System.out.println(t.getHeader());
        System.out.println(t.printTable());
    }

    public Table parseJson(File file) {
        Reader fr;
        Table t = null;
        try {

            Charset inputCharset = Charset.forName("ISO-8859-1");
            fr = new InputStreamReader(new FileInputStream(file), inputCharset);

            //fr = new FileInputStream(file);
            //fr = new FileReader(file);
            t = parseJson(fr, file.getName());

            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return t;
    }

    public Table parseJson(Reader reader, String fileName) throws IOException {
        Gson gson = new Gson();

        String json = IOUtils.toString(reader);

        // get the data from the JSON source
        TableData data = gson.fromJson(json, TableData.class);
        TableMapping tm = null;

        // check if any data was parsed ... if the file used the schema with mappings, data will not have any contents
        // but as no exception is thrown, we have to check attributes of data for null ...
        if (data.getRelation() == null) {

            AnnotatedTable moreData = gson.fromJson(json, AnnotatedTable.class);

            try {
                data = moreData.getTable();
                tm = moreData.getMapping();
            } catch (Exception e) {
                System.out.println("mapping: " + moreData.getMapping());
                System.out.println("table: " + moreData.getTable());
                System.out.println(e + " of " + fileName);
            }
        }

        return parseJson(data, fileName, tm);
    }

    public Table parseJson(TableData data, String fileName, TableMapping tm) {
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
        if (tm != null) {
            t.setMapping(tm);
            t.setData(data);
        }

        System.out.println("table length: " + data.getRelation().length);
        System.out.println("table cols: " + data.getRelation()[0].length);

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
        try {
            keyIdentifier.identifyKeys(t);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            b.setUri(table.getHeader() + "-" + table.getColumns().indexOf(b.getColumn()));

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
        if (value == null || value.equalsIgnoreCase(StringNormalizer.nullValue) || value.isEmpty()) {
            if(row==0){}
            else {
                return;
            }
        }
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

        if (row == 0 || (!value.equalsIgnoreCase(StringNormalizer.nullValue) && !value.isEmpty())) {
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
