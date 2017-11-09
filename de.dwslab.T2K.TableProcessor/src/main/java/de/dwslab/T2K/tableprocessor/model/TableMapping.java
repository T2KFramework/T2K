package de.dwslab.T2K.tableprocessor.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.dwslab.T2K.tableprocessor.IO.ListHandler;
import de.dwslab.T2K.tableprocessor.model.TableColumn.ColumnDataType;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.query.Func;
import de.dwslab.T2K.utils.query.Q;

public class TableMapping {

    public static final String SOURCE = "#source";
    public static final String CLASS = "#class";
    public static final String CLASS_CONFIDENCE = "#classConf";
    public static final String NUM_HEADER_ROWS = "#numHeaderRows";
    public static final String PROPERTIES = "#properties";
    public static final String PROPERTY_SCORES = "#propertyScores";
    public static final String INSTANCES = "#instances";
    public static final String INSTANCE_SCORES = "#instanceScores";
    public static final String KEY_COLUMN = "#keyColumn";
    public static final String DATA_TYPES = "#dataTypes";

    public static final String[] VALID_ANNOTATIONS = new String[] { SOURCE, CLASS, CLASS_CONFIDENCE, NUM_HEADER_ROWS, PROPERTIES, PROPERTY_SCORES, INSTANCES, INSTANCE_SCORES, KEY_COLUMN, DATA_TYPES };
    
    private String URI;
    private String tableName;
    private int numHeaderRows;
    private Pair<String, Double> mappedClass;
    private HashMap<Integer, Pair<String, Double>> mappedProperties = new HashMap<>();
    private HashMap<Integer, Pair<String, Double>> mappedInstances = new HashMap<>();
    private int keyIndex;
    private HashMap<Integer, ColumnDataType> dataTypes = new HashMap<>();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getNumHeaderRows() {
        return numHeaderRows;
    }

    public void setNumHeaderRows(int numHeaderRows) {
        this.numHeaderRows = numHeaderRows;
    }

    public Pair<String, Double> getMappedClass() {
        return mappedClass;
    }

    public void setMappedClass(Pair<String, Double> mappedClass) {
        this.mappedClass = mappedClass;
    }

    public HashMap<Integer, Pair<String, Double>> getMappedProperties() {
        return mappedProperties;
    }

    public void setMappedProperties(
            HashMap<Integer, Pair<String, Double>> mappedProperties) {
        this.mappedProperties = mappedProperties;
    }

    public HashMap<Integer, Pair<String, Double>> getMappedInstances() {
        return mappedInstances;
    }

    public void setMappedInstances(
            HashMap<Integer, Pair<String, Double>> mappedInstances) {
        this.mappedInstances = mappedInstances;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(int keyIndex) {
        this.keyIndex = keyIndex;
    }

    public HashMap<Integer, ColumnDataType> getDataTypes() {
        return dataTypes;
    }
    
    public void setDataTypes(HashMap<Integer, ColumnDataType> dataTypes) {
        this.dataTypes = dataTypes;
    }
    
    public String getURI() {
        return URI;
    }

    public void setURI(String uRI) {
        URI = uRI;
    }

    public static TableMapping fromTable(Table table) {
        TableMapping tm = new TableMapping();
        tm.setURI(table.getSource());
        tm.setTableName(table.getHeader());
        tm.setNumHeaderRows(table.getNumHeaderRows());
        tm.setKeyIndex(table.getColumns().indexOf(table.getKey()));
        for(int i = 0; i < table.getColumns().size(); i++) {
            tm.getDataTypes().put(i, table.getColumns().get(i).getDataType());
        }
        return tm;
    }
    
    public static TableMapping read(String fileName) throws IOException {
        TableMapping m = new TableMapping();
        m.readMapping(fileName);
        return m;
    }

    public void readMapping(String fileName) throws IOException {
        readMapping(new FileInputStream(fileName), fileName);
    }

    public void readMapping(InputStream is, String fileName) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));

        setTableName(new File(fileName).getName());

        String line = null;

        while ((line = r.readLine()) != null) {

            if (line.startsWith("#")) {
                parseMetadata(line);
            } else {
                break;
            }
        }

        r.close();
    }

    private void parseMetadata(String line) {
        //String[] parts = line.split("=");
        String[] parts = new String[] { "", "" };
        for(String s : VALID_ANNOTATIONS) {
            if(line.startsWith(s+"=")) {
                parts[0] = s;
                
                if(line.length()==s.length()+1) {
                    parts[1] = "";
                } else {
                    parts[1] = line.substring(s.length()+1);
                }
                
                break;
            }
        }

        if(parts[1].equals("")) {
            return;
        }
        
        if (parts[0].equals(SOURCE) && parts.length > 1) {
            if(parts.length>1) {
                setURI(parts[1]);
            }
            else {
                setURI("");
            }            
        } else if (parts[0].equals(CLASS)) {
            Pair<String, Double> p = getMappedClass();

            String cls = parts[1].replace(".gz", "");

            if (p == null) {
                p = new Pair<String, Double>(cls, 0.0);
            } else {
                p = new Pair<String, Double>(cls, p.getSecond());

            }

            setMappedClass(p);
        } else if (parts[0].equals(CLASS_CONFIDENCE)) {
            Pair<String, Double> p = getMappedClass();

            Double conf = Double.parseDouble(parts[1]);

            if (p == null) {
                p = new Pair<String, Double>("", conf);
            } else {
                p = new Pair<String, Double>(p.getFirst(), conf);

            }

            setMappedClass(p);
        } else if (parts[0].equals(NUM_HEADER_ROWS)) {
            Integer num = Integer.parseInt(parts[1]);
            setNumHeaderRows(num);
        } else if (parts[0].equals(PROPERTIES)) {
            if (parts[1].length() > 2) {
                String data = parts[1].substring(1, parts[1].length() - 1);
                String[] values = data.split("\\|");

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null && !values[i].equals("")) {
                        Pair<String, Double> p = getMappedProperties().get(i);

                        if (p == null) {
                            p = new Pair<String, Double>(values[i], 0.0);
                        } else {
                            p = new Pair<String, Double>(values[i],
                                    p.getSecond());
                        }

                        getMappedProperties().put(i, p);
                    }
                }
            }
        } else if (parts[0].equals(PROPERTY_SCORES)) {
            if (parts[1].length() > 2) {
                String data = parts[1].substring(1, parts[1].length() - 1);
                String[] values = data.split("\\|");

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null && !values[i].equals("")) {
                        Pair<String, Double> p = getMappedProperties().get(i);

                        try {
                            Double score = Double.parseDouble(values[i]);

                            if (p == null) {
                                p = new Pair<String, Double>("", score);
                            } else {
                                p = new Pair<String, Double>(p.getFirst(),
                                        score);
                            }

                            getMappedProperties().put(i, p);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } else if ((parts[0].equals(INSTANCES))) {
            if (parts[1].length() > 2) {
                String data = parts[1].substring(1, parts[1].length() - 1);
                String[] values = data.split("\\|");

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null && !values[i].equals("")) {
                        Pair<String, Double> p = getMappedInstances().get(i);

                        if (p == null) {
                            p = new Pair<String, Double>(values[i], 0.0);
                        } else {
                            p = new Pair<String, Double>(values[i],
                                    p.getSecond());
                        }

                        getMappedInstances().put(i, p);
                    }
                }
            }
        } else if (parts[0].equals(INSTANCE_SCORES)) {
            if (parts[1].length() > 2) {
                String data = parts[1].substring(1, parts[1].length() - 1);
                String[] values = data.split("\\|");

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null && !values[i].equals("")) {
                        Pair<String, Double> p = getMappedInstances().get(i);

                        try {
                            Double score = Double.parseDouble(values[i]);

                            if (p == null) {
                                p = new Pair<String, Double>("", score);
                            } else {
                                p = new Pair<String, Double>(p.getFirst(),
                                        score);
                            }

                            getMappedInstances().put(i, p);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } else if (parts[0].equals(KEY_COLUMN)) {
            int idx = Integer.parseInt(parts[1]);
            setKeyIndex(idx);
        }  else if (parts[0].equals(DATA_TYPES)) {
            if (parts[1].length() > 2) {
                String[] values = ListHandler.splitList(parts[1]);

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null && !values[i].equals("")) {
                        getDataTypes().put(i, ColumnDataType.valueOf(values[i]));
                    }
                }
            }
        }
    }

    public void write(String fileName, int numberOfRows,int numberOfColumns) throws IOException {
        BufferedWriter w = new BufferedWriter(
                new FileWriter(new File(fileName)));

        write(w, numberOfRows, numberOfColumns);
        
        w.close();
    }
    
    public void write(Writer w, int numberOfRows, int numberOfColumns) throws IOException {
        w.write(String.format("%s=%s\n", SOURCE, getURI()));
        w.write(String.format("%s=%s\n", CLASS, getMappedClass().getFirst()));
        w.write(String.format("%s=%s\n", CLASS_CONFIDENCE,
                Double.toString(getMappedClass().getSecond())));
        w.write(String.format("%s=%d\n", KEY_COLUMN, getKeyIndex()));
        w.write(String.format("%s=%d\n", NUM_HEADER_ROWS, getNumHeaderRows()));

        if (getMappedProperties() != null && getMappedProperties().size() > 0) {
            StringBuilder sbUris = new StringBuilder();
            StringBuilder sbConf = new StringBuilder();

            sbUris.append("{");
            sbConf.append("{");
            for (int i = 0; i <= numberOfColumns; i++) {

                if (i != 0) {
                    sbUris.append("|");
                    sbConf.append("|");
                }

                Pair<String, Double> mapping = getMappedProperties().get(i);
                if (mapping != null) {
                    sbUris.append(mapping.getFirst());
                    sbConf.append(mapping.getSecond().toString());
                }

            }
            sbUris.append("}");
            sbConf.append("}");

            w.write(String.format("%s=%s\n", PROPERTIES, sbUris.toString()));
            w.write(String.format("%s=%s\n", PROPERTY_SCORES, sbConf.toString()));
        }

        if (getMappedInstances() != null && getMappedInstances().size() > 0) {
            StringBuilder sbUris = new StringBuilder();
            StringBuilder sbConf = new StringBuilder();
          
            sbUris.append("{");
            sbConf.append("{");
            for (int i = 0; i <= numberOfRows; i++) {

                if (i != 0) {
                    sbUris.append("|");
                    sbConf.append("|");
                }

                Pair<String, Double> mapping = getMappedInstances().get(i);
                if (mapping != null) {
                    sbUris.append(mapping.getFirst());
                    sbConf.append(mapping.getSecond().toString());
                }

            }
            sbUris.append("}");
            sbConf.append("}");

            w.write(String.format("%s=%s\n", INSTANCES, sbUris.toString()));
            w.write(String.format("%s=%s\n", INSTANCE_SCORES, sbConf.toString()));
        }
        
        List<String> types = new LinkedList<>();
        for(int i = 0; i < numberOfColumns; i++) {
            types.add(getDataTypes().get(i).toString());
        }
        w.write(String.format("%s=%s\n", DATA_TYPES, ListHandler.formatList(types)));
    }
}
