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
package de.dwslab.T2K.matching.dbpedia;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.blocking.TypeBasedBlocking;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import java.util.Collection;

public class LoggingTypeBasedBlocking extends TypeBasedBlocking<TableCell> {

    public LoggingTypeBasedBlocking(MatchingAdapter<TableCell> adapter) {
        super(adapter);
    }

    @Override
    public Collection<TableCell> getCandidates(TableCell instance,
            Collection<TableCell> candidates) {
        Collection<TableCell> col = super.getCandidates(instance, candidates);
        
        StringBuilder sb = new StringBuilder();
        
        String key = instance.getTable().getKey().getValues().get(instance.getRowIndex()).toString();
        String value = instance.getValue() + "";
        
        sb.append(key + ": " + value + " -> ");
        
        for(TableCell c : col) {
            String column = c.getTable().getColumns().get(c.getColumnIndex()).getHeader().toString();
            String v = c.getValue() + "";
            sb.append(column+"="+v+"; ");
        }
        
        System.out.println(sb.toString());
        
        return col;
    }
}
