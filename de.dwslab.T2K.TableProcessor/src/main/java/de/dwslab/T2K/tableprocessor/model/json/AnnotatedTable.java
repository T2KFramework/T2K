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
package de.dwslab.T2K.tableprocessor.model.json;

import java.io.BufferedWriter;
import java.io.File;
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
