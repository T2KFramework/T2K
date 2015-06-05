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
package de.dwslab.T2K.matching.dbpedia.model.adapters;

import java.util.ArrayList;
import java.util.Collection;

import de.dwslab.T2K.matching.MatchingHierarchyAdapater;
import de.dwslab.T2K.matching.ValueRange;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.tableprocessor.model.Statistic;

public class TableRowToCellHierarchyAdapter extends
        MatchingHierarchyAdapater<TableRow, TableCell> {

    @Override
    public Collection<TableCell> getParts(TableRow instance) {

        if(instance==null) {
            return new ArrayList<TableCell>();
        } else {
            // get cells
            Collection<TableCell> cells = instance.getCells();
    
            // remove key
//            TableCellCache cache = TableCellCache.get();
//            TableCell c = cache.getOrCreate(instance.getTable(),
//                    instance.getRowIndex(), instance.getTable().getColumns()
//                            .indexOf(instance.getTable().getKey()));
//            cells.remove(c);
    
            return cells;
        }
    }
    
    @Override
    public ValueRange getValueRange(TableRow instance, TableCell value) {
        //HierarchyStatistics stat = super.getStatistics(instance, value);
        
        Statistic s = value.getTable().getColumns().get(value.getColumnIndex()).getColumnStatistic();
        
//        stat.setMaxValue(s.getMaximalValue());
//        stat.setMinValue(s.getMinimalValue());
        
        return new ValueRange(s.getMinimalValue(), s.getMaximalValue());
        
//        return stat;
    }
}
