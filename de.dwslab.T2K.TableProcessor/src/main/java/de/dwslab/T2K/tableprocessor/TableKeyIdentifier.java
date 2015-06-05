/**
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of
							Mannheim (t2k@dwslab.de)
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
package de.dwslab.T2K.tableprocessor;

import java.util.ArrayList;
import java.util.List;

import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.util.Variables;

import java.util.regex.Pattern;

/**
 * Detects the key column of the table.
 *
 * @author petar
 *
 */
public class TableKeyIdentifier {
    
    public void indenfityLODKeys(Table table) {
        identifyKeys(table);
    }

    public void identifyKeys(Table table) {
        TableColumn key = null;
        int keyColumnIndex = -1;
        List<Double> columnUniqueness = new ArrayList<>(table.getColumns().size());
        // check if there is a common key
        for (TableColumn column : table.getColumns()) {

            if (Pattern.compile("([^#]*#)?([a-z]{1,9})?label$").matcher(column.getHeader()).matches() && column.getDataType() == TableColumn.ColumnDataType.string) {
                key = column;
                break;
            }

            if (Pattern.compile("([^#]*#)?([a-z]{1,9})?name$").matcher(column.getHeader()).matches() && column.getDataType() == TableColumn.ColumnDataType.string) {
                key = column;
                break;

            }

            if (Pattern.compile("([^#]*#)?([a-z]{1,9})?title$").matcher(column.getHeader()).matches() && column.getDataType() == TableColumn.ColumnDataType.string) {
                key = column;
                break;

            }
        }
        
        if(key !=null) {            
            keyColumnIndex = table.getColumns().indexOf(key);
            if (table.getColumns().get(keyColumnIndex).getColumnUniqnessRank() >= Variables.keyUniqueness && 
                    table.getColumns().get(keyColumnIndex).getColumnStatistic().getAverageValueLength() > 3.5 && 
                    table.getColumns().get(keyColumnIndex).getColumnStatistic().getAverageValueLength() <= 200) {
                table.getColumns().get(keyColumnIndex).setKey(true);
                table.setHasKey(true);
                return;
            }
            //the found key does not fit the requirements, see if another column does
            key = null;
        }

        for (TableColumn column : table.getColumns()) {
            columnUniqueness.add(column.getColumnUniqnessRank());
        }
        
        if (columnUniqueness.isEmpty()) {
            return;
        }
        double maxCount = -1;
        int maxColumn = -1;

        for (int i = 0; i < columnUniqueness.size(); i++) {
            if (columnUniqueness.get(i) > maxCount && table.getColumns().get(i).getDataType() == TableColumn.ColumnDataType.string
                    && table.getColumns().get(i).getColumnStatistic().getAverageValueLength() > 3.5
                    && table.getColumns().get(i).getColumnStatistic().getAverageValueLength() <= 200) {
                maxCount = (Double) columnUniqueness.get(i);
                maxColumn = i;
            }
        }

        if (key == null) {
            if (maxColumn == -1) {
                table.setHasKey(false);
                return;
            }
            key = table.getColumns().get(maxColumn);
        }
        keyColumnIndex = table.getColumns().indexOf(key);

        if (columnUniqueness.get(keyColumnIndex) < Variables.keyUniqueness) {
            table.setHasKey(false);
            return;
        }

        table.getColumns().get(keyColumnIndex).setKey(true);
        table.setHasKey(true);
    }
}
