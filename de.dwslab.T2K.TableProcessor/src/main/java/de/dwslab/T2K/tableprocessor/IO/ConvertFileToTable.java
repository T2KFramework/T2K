package de.dwslab.T2K.tableprocessor.IO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import au.com.bytecode.opencsv.CSVReader;
import de.dwslab.T2K.tableprocessor.ColumnType;
import de.dwslab.T2K.tableprocessor.ColumnTypeGuesser;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.tableprocessor.model.TableColumnBuilder;
import de.dwslab.T2K.tableprocessor.model.TableMapping;
import de.dwslab.T2K.tableprocessor.model.TableColumn.ColumnDataType;
import de.dwslab.T2K.units.UnitParser;
import de.dwslab.T2K.units.Unit;
import de.dwslab.T2K.util.Variables;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.concurrent.Producer;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.data.string.LanguageEncoder;
import de.uni_mannheim.informatik.dws.t2k.normalisation.StringNormalizer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

/**
 * @author petar
 *
 */
public class ConvertFileToTable {

    private ColumnTypeGuesser typeGuesser;
    private Boolean cleanHeader = true;
    private boolean useUnitDetection = false;
    private int spanningCellThreshold = 1;

    public void setUseUnitDetection(boolean useUnitDetection) {
        this.useUnitDetection = useUnitDetection;

//        if (useUnitDetection) {
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//            System.out.println("!!!!!!!DON'T USE UNITS IN MULTIPLE THREADS!!!!!!");
//            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//        }
    }

    public boolean isUseUnitDetection() {
        return useUnitDetection;
    }

    public void setCleanHeader(Boolean clean) {
        cleanHeader = clean;
    }
    private char overrideDelimiter = 0;

    public void setOverrideDelimiter(char delimiter) {
        overrideDelimiter = delimiter;
    }

    public ConvertFileToTable() {
        typeGuesser = new ColumnTypeGuesser();
    }

    /**
     * Get a reader for the table. The path can either point to a gz archive or
     * a csv file.
     *
     * @param path
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Reader getReader(String path) throws FileNotFoundException, IOException {
        if (path.endsWith(".gz")) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(path));
            return new InputStreamReader(gzip, "UTF-8");
        } else {
            return new InputStreamReader(new FileInputStream(path), "UTF-8");
        }
    }

    public Table readKGTable(String path) {
        if (Variables.useUnitDetection) {
            UnitParser.readInUnits();
        }
        // create new table
        final Table table = new Table();
        // take care of the header of the table
        table.setHeader(path);
        final List<TableColumnBuilder> colBuilders = new LinkedList<>();

        try {
            String[] columnNames;

            final BufferedReader in = new BufferedReader(getReader(path));

            // read the property names
            String fileLine = in.readLine();
            Variables.delimiter = "\t";
            columnNames = fileLine.split(Variables.delimiter);

            final Unit[] columnUnits = new Unit[columnNames.length];

            // process all properties (=columns)
            int i = 0;
            for (String columnName : columnNames) {

                // replace trailing " for the last column
                columnName = columnName.replace("\"", "");
                String columnUri = "http://woo/" + columnName;

                // create the ColumnBuilder
                TableColumnBuilder b = new TableColumnBuilder(table);
                b.setHeader(columnName);
                b.setUri(columnUri);
                b.setDataType(ColumnDataType.unknown);
                columnUnits[i] = UnitParser.parseUnitFromHeader(columnName);
                colBuilders.add(b);
                // add the column to the table
                table.getColumns().add(b.getColumn());
                i++;
            }

            table.setNumHeaderRows(1);

            // start processing the table contents
            final int rowIndex = 0;

            new Parallel<Pair<Integer, String[]>>().producerConsumer(new Producer<Pair<Integer, String[]>>() {
                @Override
                public void execute() {
                    int row = rowIndex;
                    String[] values;

                    try {
                        String fileLine;
                        while ((fileLine = in.readLine()) != null) {

                            // handle the column splitting
                            fileLine = fileLine.substring(1, fileLine.length() - 1);
                            values = fileLine.split(Variables.delimiter);
                            produce(new Pair<>(row++, values));
                        }
                        table.setTotalNumOfRows(row);
                    } catch (IOException e) {
                    }
                }
            }, new Consumer<Pair<Integer, String[]>>() {
                @Override
                public void execute(Pair<Integer, String[]> parameter) {

                    int rowIndex = parameter.getFirst();

                    // iterate all columns
                    int columnIndex = 0;
                    for (String columnValue : parameter.getSecond()) {
                        TableColumnBuilder b = null;

                        if (columnIndex >= colBuilders.size()) {
                            return;
                        } else {
                            b = colBuilders.get(columnIndex);
                        }
                        ColumnType valueType = new ColumnType(b.getDataType(), null);

                        if (!columnValue.equalsIgnoreCase(StringNormalizer.nullValue) && !columnValue.isEmpty()) {

                            // guess the type if not already set
                            boolean list = false;
                            if (table.getColumns().get(columnIndex).getDataType() == ColumnDataType.unknown) {

                                if (columnValue.contains("^^<http")) {
                                    String[] content = columnValue.split("\\^\\^");
                                    switch (content[1]) {
                                        case "<http://www.w3.org/2001/XMLSchema#boolean>":
                                            valueType = new ColumnType(ColumnDataType.bool, columnUnits[columnIndex]);
                                            break;
                                        case "<http://www.w3.org/2001/XMLSchema#double>":
                                        case "<http://www.w3.org/2001/XMLSchema#integer>":
                                            valueType = new ColumnType(ColumnDataType.numeric, columnUnits[columnIndex]);
                                            break;
                                    }
                                    columnValue = content[0];
                                } else {
                                    // detect the unit and guess the type
                                    //TODO: use the determined datatype? e.g. for populationDensity?
                                    valueType = typeGuesser.guessTypeForValue(columnValue, columnUnits[columnIndex]);
                                }

                            }
                            if (checkIfList(columnValue)) {
                                List<String> columnValues;
                                columnValue = columnValue.replace("{", "");
                                columnValue = columnValue.replace("}", "");
                                columnValues = Arrays.asList(columnValue.split("\\|"));
                                b.addValue(rowIndex, columnValues, valueType);
                                list = true;
                            }

                            if (!list) {
                                // add the new value
                                b.addValue(rowIndex, columnValue, valueType);
                            }
                        }
                        columnIndex++;
                    }
                }
            });

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // finalise the column building
        for (TableColumnBuilder b : colBuilders) {
            b.buildColumn();
        }

        return table;
    }

    /**
     *
     * read as LOD table expecting the DBpedia-to-tables CSV format.
     *
     * @param path
     * @return
     */
    public Table readLODTable(String path) {
        if (Variables.useUnitDetection) {
            UnitParser.readInUnits();
        }
        // create new table
        final Table table = new Table();
        // take care of the header of the table
        table.setHeader(path);
        final List<TableColumnBuilder> colBuilders = new LinkedList<>();

        try {
            String[] columnNames;
            String[] columntypes;
            String[] columnURIs;

            final BufferedReader in = new BufferedReader(getReader(path));

            // read the property names
            String fileLine = in.readLine();
            columnNames = fileLine.split(Variables.delimiter);

            // skip annotations
            boolean isMetaData = columnNames[0].startsWith("#");
            // if the current line starts with #, check for valid annotations
            while (isMetaData) {
                isMetaData = false;

                // check all valid annotations
                for (String s : TableMapping.VALID_ANNOTATIONS) {
                    if (columnNames[0].startsWith(s)) {
                        isMetaData = true;
                        break;
                    }
                }

                // if the current line is an annotation, read the next line and start over
                if (isMetaData) {
                    fileLine = in.readLine();
                    columnNames = fileLine.split(Variables.delimiter);
                    isMetaData = columnNames[0].startsWith("#");
                }
            }

            // read the property URIs
            columnURIs = in.readLine().split(Variables.delimiter);

            // read the datatypes
            fileLine = in.readLine();
            columntypes = fileLine.split(Variables.delimiter);

            final Unit[] columnUnits = new Unit[columnNames.length];

            // process all properties (=columns)
            int i = 0;
            for (String columnName : columnNames) {

                // replace trailing " for the last column
                columntypes[i] = columntypes[i].replace("\"", "");
                columnURIs[i] = columnURIs[i].replace("\"", "");
                columnName = columnName.replace("\"", "");

                // create the ColumnBuilder
                TableColumnBuilder b = new TableColumnBuilder(table);
                b.setHeader(columnName);
                b.setUri(columnURIs[i]);

                // set the type if it's a primitive
                //TODO what about other primitive types?
                String datatype = columntypes[i];
                switch (datatype) {
                    case "XMLSchema#date":
                    case "XMLSchema#gYear":
                        b.setDataType(ColumnDataType.date);
                        break;
                    case "XMLSchema#double":
                    case "XMLSchema#float":
                    case "XMLSchema#nonNegativeInteger":
                    case "XMLSchema#positiveInteger":
                    case "XMLSchema#integer":
                    case "XMLSchema#negativeInteger":
                        b.setDataType(ColumnDataType.numeric);
                        break;
                    case "XMLSchema#string":
                        b.setDataType(ColumnDataType.string);
                        break;
                    case "XMLSchema#boolean":
                        b.setDataType(ColumnDataType.bool);
                        break;
                    default:
                        b.setDataType(ColumnDataType.unknown);
                }

                columnUnits[i] = UnitParser.parseUnitFromHeader(columnName);

                colBuilders.add(b);
                // add the column to the table
                table.getColumns().add(b.getColumn());
                i++;
            }

            // skip the last header
            fileLine = in.readLine();

            table.setNumHeaderRows(4);

            // start processing the table contents
//            long start = System.currentTimeMillis();
            // the absolute row number in the file (offset of 4!)
            final int rowIndex = 0;

            new Parallel<Pair<Integer, String[]>>().producerConsumer(new Producer<Pair<Integer, String[]>>() {
                @Override
                public void execute() {
                    int row = rowIndex;
                    String[] values;

                    try {
                        String fileLine;
                        while ((fileLine = in.readLine()) != null) {

                            // handle the column splitting
                            //fileLine = fileLine.substring(1, fileLine.length());
                            fileLine = fileLine.substring(1, fileLine.length() - 1);
                            values = fileLine.split(Variables.delimiter);
                            produce(new Pair<>(row++, values));
                        }
                        table.setTotalNumOfRows(row);
                    } catch (IOException e) {
                    }
                }
            }, new Consumer<Pair<Integer, String[]>>() {
                @Override
                public void execute(Pair<Integer, String[]> parameter) {

                    int rowIndex = parameter.getFirst();

                    // iterate all columns
                    int columnIndex = 0;
                    for (String columnValue : parameter.getSecond()) {
                        TableColumnBuilder b = null;

                        if (columnIndex >= colBuilders.size()) {
                            return;
                        } else {
                            b = colBuilders.get(columnIndex);
                        }
                        ColumnType valueType = new ColumnType(b.getDataType(), null);

                        if (!columnValue.equalsIgnoreCase(StringNormalizer.nullValue)) {

                            // guess the type if not already set
                            boolean list = false;
                            if (table.getColumns().get(columnIndex).getDataType() == ColumnDataType.unknown) {

                                // detect the unit and guess the type
                                //TODO: use the determined datatype? e.g. for populationDensity?
                                valueType = typeGuesser.guessTypeForValue(columnValue, columnUnits[columnIndex]);

                            }
                            if (checkIfList(columnValue)) {
                                List<String> columnValues;
                                columnValue = columnValue.replace("{", "");
                                columnValue = columnValue.replace("}", "");
                                columnValues = Arrays.asList(columnValue.split("\\|"));
                                b.addValue(rowIndex, columnValues, valueType);
                                list = true;
                            }

                            if (!list) {
                                // add the new value
                                b.addValue(rowIndex, columnValue, valueType);
                            }
                        }
                        columnIndex++;
                    }
                }
            });

//            while ((fileLine = in.readLine()) != null) {
//
//                // handle the column splitting
//                fileLine = fileLine.substring(1, fileLine.length() - 1);
//                nextLine = fileLine.split(Variables.delimiter);
//
//                // report progress
//                if (rowIndex % 1000 == 0) {
//                    System.out.println(rowIndex + " rows were read in " + DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss.S"));
//                }
//
//                // iterate all columns
//                int columnIndex = 0;
//                for (String columnValue : nextLine) {
//
//                    TableColumnBuilder b = colBuilders.get(columnIndex);
//                    ColumnType valueType = new ColumnType(b.getDataType(), null);
//
//                    if (!columnValue.equalsIgnoreCase(Variables.nullValue)) {
//
//                        // normalise the value
////                        if (Variables.normalizeValues) {
////                            String columnValueNormalized = StringNormalizer.clearString(columnValue, true);
////                            columnValueNormalized = StringNormalizer.simpleStringNormalization(columnValueNormalized, true);
////                            columnValue = columnValueNormalized;
////                        }
//
//                        // handle lists
////                        if (checkIfList(columnValue)) {
////                            // lists are currently not handeled, so just take the first value
////                            //columnValue = columnValue.split("\\|")[0];
////                            //columnValue = columnValue.replace("{", "");
////                            table.getColumns().get(columnIndex).setDataType(ColumnDataType.list);
////                        }
//
//                        // guess the type if not already set
//                        boolean list = false;
//                        if (table.getColumns().get(columnIndex).getDataType() == ColumnDataType.unknown) {
//
//                            // detect the unit and guess the type
//                            //TODO: use the determined datatype? e.g. for populationDensity?
//                            valueType = typeGuesser.guessTypeForValue(columnValue, b.getHeader());
//
//                        }
//                        if (checkIfList(columnValue)) {
//                            List<String> columnValues;
//                            columnValue = columnValue.replace("{", "");
//                            columnValue = columnValue.replace("}", "");
//                            columnValues = Arrays.asList(columnValue.split("\\|"));
//                            b.addValue(rowIndex, columnValues, valueType);
//                            list = true;
//                        }
//
//                        if (!list) {
//                            // add the new value
//                            b.addValue(rowIndex, columnValue, valueType);
//                        }
//                    }
//                    columnIndex++;
//                }
//                rowIndex++;
//            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // finalise the column building
        for (TableColumnBuilder b : colBuilders) {
            b.buildColumn();
        }

        return table;
    }

    /**
     *
     *
     * @param tablePath
     * @return
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Table readWebTable(String tablePath) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        if (Variables.useUnitDetection) {
            UnitParser.readInUnits();
        }
        // create new table
        final Table table = new Table();
        Table result = table;
        // take care of the header of the table
        table.setHeader(tablePath);

        final List<TableColumnBuilder> colBuilders = new LinkedList<>();

        try {
            Reader r = new InputStreamReader(new FileInputStream(tablePath), "UTF-8");
            final CSVReader reader;

            // create reader
            if (overrideDelimiter == 0) {
                reader = new CSVReader(r);
            } else {
                reader = new CSVReader(r, overrideDelimiter);
            }

            // read headers
            String[] columnNames = reader.readNext();

            if (columnNames == null) {
                reader.close();
                return null;
            }

            // skip annotations
            //currently not used
            boolean isMetaData = columnNames[0].startsWith("#");
            // if the current line starts with #, check for valid annotations
            while (isMetaData) {
                isMetaData = false;

                // check all valid annotations
                for (String s : TableMapping.VALID_ANNOTATIONS) {
                    if (columnNames[0].startsWith(s)) {
                        isMetaData = true;
                        break;
                    }
                }

                // if the current line is an annotation, read the next line and start over
                if (isMetaData) {
                    columnNames = reader.readNext();
                    isMetaData = columnNames[0].startsWith("#");
                }
            }
            final Unit[] columnUnits = new Unit[columnNames.length];

            //set the header for each column (take the first row!)
            int colIdx = 0;
            for (String columnName : columnNames) {
                TableColumnBuilder b = new TableColumnBuilder(table);

                // set the header
                String header = columnName;
                if (cleanHeader) {
                    header = StringNormalizer.normaliseHeader(columnName);
                }
                b.setHeader(header);

                // parse units from headers
                columnUnits[colIdx++] = UnitParser.parseUnitFromHeader(columnName);

                colBuilders.add(b);
                table.getColumns().add(b.getColumn());
                b.setUri(table.getHeader() + "-" + table.getColumns().indexOf(b.getColumn()));
            }
            table.setNumHeaderRows(1);

            final int rowNumber = 0;

            // read the file sequentially, but process the rows in parallel (processing is much slower than reading)
            new Parallel<Pair<Integer, String[]>>().producerConsumer(new Producer<Pair<Integer, String[]>>() {
                @Override
                public void execute() {
                    int row = rowNumber;
                    String[] values;

                    try {
                        while ((values = reader.readNext()) != null) {
                            produce(new Pair<>(row++, values));
//                            System.out.println(row);
//                            for (String v : values) {
//                                System.out.print(v + "\t");
//                            }
//                            System.out.println();
                        }
                        table.setTotalNumOfRows(row);
                    } catch (IOException e) {
                        System.out.println("EXCEPTION for " + row + "\t" + e.getMessage());
                    }
                }
            }, new Consumer<Pair<Integer, String[]>>() {
                @Override
                public void execute(Pair<Integer, String[]> parameter) {
                    int rowNumber = parameter.getFirst();
                    String[] values = parameter.getSecond();

//                    System.out.println("now in exevute " +rowNumber);
//                    for (String v : values) {
//                        System.out.print(v + "\t");
//                    }
//                    System.out.println();

                    int columnIndex = 0;

                    String last = null;
                    int duplicateCount = 0;

                    // check for spanning cells
                    for (String columnValue : values) {

                        if (columnValue != null && columnValue.equals(last)) {
                            duplicateCount++;
                        }
                        last = columnValue;
                    }

                    if (duplicateCount > getSpanningCellThreshold() && getSpanningCellThreshold() > 0) {
                        // this row contains spanning cells, so we ignore it
                        return;
                    }

                    for (String columnValue : values) {

                        if (columnIndex < table.getColumns().size()) {

                            TableColumnBuilder b = colBuilders.get(columnIndex);

                            LanguageEncoder le = new LanguageEncoder();
                            //TODO: replace all HTML encodings
                            columnValue = le.encodeString(columnValue);                           
                            	

//                            if (columnValue.contains("&quot;")) {
//                                columnValue = columnValue.replace("&quot;", "");
//                            }

                            // handle lists
                            if (checkIfList(columnValue)) {
                                //valueType = ColumnDataType.list;
                                // split values, normalise, determine type, ...
                            } else {
                                // normalise the value
                                if (Variables.normalizeValues) {
                                    columnValue = normaliseValue(columnValue);
                                }
                            }

                            // guess the type 
                            ColumnType valueType = typeGuesser.guessTypeForValue(columnValue, columnUnits[columnIndex]);

                            if (checkIfList(columnValue)) {
                                List<String> columnValues;
                                columnValue = columnValue.replace("{", "");
                                columnValue = columnValue.replace("}", "");
                                columnValues = Arrays.asList(columnValue.split("\\|"));
                                b.addValue(rowNumber, columnValues, valueType);
                            } else {
                                //System.out.println("read table: " + rowNumber + "\t" + columnValue + "\t" + valueType);
                                b.addValue(rowNumber, columnValue, valueType);
                            }
                        }
                        columnIndex++;
                    }
                }
            });

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }

        for (TableColumnBuilder b : colBuilders) {
            b.buildColumn();
        }

        return result;
    }

    protected String normaliseValue(String value) {
        //String columnValueNormalized = StringNormalizer.simpleStringNormalization(value, true);
        //String columnValueNormalized = StringNormalizer.simpleStringNormalization(value, false);
        String columnValueNormalized = StringNormalizer.normaliseValue(value, false);
        return columnValueNormalized;
    }
    private static Pattern listPattern = Pattern.compile("^\\{.+\\|.+\\}$");

    private boolean checkIfList(String columnValue) {
        //if (columnValue.matches("^\\{.+\\|.+\\}$")) {
        if (listPattern.matcher(columnValue).matches()) {
            return true;
        }
        return false;
    }

    /**
     * @return the spanningCellThreshold
     */
    public int getSpanningCellThreshold() {
        return spanningCellThreshold;
    }

    /**
     * @param spanningCellThreshold the spanningCellThreshold to set
     */
    public void setSpanningCellThreshold(int spanningCellThreshold) {
        this.spanningCellThreshold = spanningCellThreshold;
    }
}
