package de.dwslab.T2K.tableprocessor.model.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.dwslab.T2K.tableprocessor.model.TableMapping;

public class AnnotatedTable {

    private TableData table;
    private TableMapping mapping;
    
    public TableData getTable() {
        return table;
    }
    public void setTable(TableData table) {
        this.table = table;
    }
    
    public TableMapping getMapping() {
        return mapping;
    }
    public void setMapping(TableMapping mapping) {
        this.mapping = mapping;
    }
    
    public void writeJson(File file) throws IOException {
        Gson gson = new Gson();
        BufferedWriter w = new BufferedWriter(new FileWriter(file));
        w.write(gson.toJson(this));
        w.close();
    }
    
    public static AnnotatedTable fromJson(File file) throws JsonSyntaxException, IOException {
        Gson gson = new Gson();
        return gson.fromJson(FileUtils.readFileToString(file), AnnotatedTable.class);
    }
}
