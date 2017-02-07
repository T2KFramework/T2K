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
package de.dwslab.T2K.matching.dbpedia.logging;

import au.com.bytecode.opencsv.CSVWriter;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author domi
 */
public class MissingValueLogger {

    public static void writeMissingValues(Collection<MatchingResult> results) throws IOException {

        CSVWriter write = new CSVWriter(new FileWriter("missingValues.csv"));
        Collection<String[]> output = new ArrayList<>();

        String[] header = new String[8];
        header[0] = "webtable key";
        header[1] = "DBpedia URI";
        header[2] = "value";
        header[3] = "webtable column";
        header[4] = "DBpedia property";
        header[5] = "webtable";
        header[6] = "correct instance corres";
        header[7] = "correct property corres";
        output.add(header);

        for (MatchingResult r : results) {
            for (Correspondence<Table> c : r.getClassMappings()) {
                Table webTable = c.getFirst();
                Table dbpediaTable = c.getSecond();

                for (Correspondence<TableColumn> prop : r.getPropertyMappings()) {
                    TableColumn col1 = prop.getFirst();
                    TableColumn col2 = prop.getSecond();
                    if (webTable.getColumns().contains(col1) && dbpediaTable.getColumns().contains(col2)) {

                        int colWebIndex = webTable.getColumns().indexOf(col1);
                        int colDBpediaIndex = dbpediaTable.getColumns().indexOf(col2);

                        for (Correspondence<TableRow> instCorres : r.getInstanceMappings()) {
                            TableRow inst1 = instCorres.getFirst();
                            TableRow inst2 = instCorres.getSecond();                            
                            for (TableCell cell1 : inst1.getCells()) {
                                boolean valueFound = false;
                                if (colWebIndex == cell1.getColumnIndex()) {
                                    for (TableCell cell2 : inst2.getCells()) {
                                        if (colDBpediaIndex == cell2.getColumnIndex()) {
                                            valueFound = true;
                                            break;
                                        }
                                    }
                                    if (!valueFound) {
                                        String[] values = new String[8];
                                        values[0] = inst1.getKey().toString();
                                        values[1] = inst2.getURI().toString();
                                        values[2] = cell1.getValue().toString();
                                        values[3] = col1.getHeader().toString();
                                        values[4] = col2.getHeader().toString();
                                        values[5] = webTable.getHeader();
                                        values[6] = ""+instCorres.isCorrect();
                                        values[7] = ""+prop.isCorrect();
                                        output.add(values);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (String[] line : output) {
            write.writeNext(line);
        }
        write.flush();
        write.close();
    }
}
