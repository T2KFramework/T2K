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
package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author domi
 */
public class NEMatchingAdapter extends MatchingAdapter<TableRow> {

    private int columnIndex;

    @Override
    public Object getLabel(TableRow instance) {
        if(instance.getURI() != null &&instance.getURI().toString().contains("dbpedia.org")) {
            return instance.getKey();
        }
        return instance.getTable().getColumns().get(columnIndex).getValues().get(instance.getRowIndex());
    }

    @Override
    public boolean isMultiValued(TableRow instance) {
        if(instance.getURI() != null && instance.getURI().toString().contains("dbpedia.org")) {
            return instance.getKey() instanceof List;
        }
        return instance.getTable().getColumns().get(columnIndex).getValues().get(instance.getRowIndex()) instanceof List;
    }

    @Override
    public Collection getLabels(TableRow instance) {
        if (isMultiValued(instance)) {
            if(instance.getURI() != null && instance.getURI().toString().contains("dbpedia.org")) {
                return (List)instance.getKey();
            }
            return (List) instance.getTable().getColumns().get(columnIndex).getValues().get(instance.getRowIndex());
        } else {
            return super.getLabels(instance);
        }
    }

    @Override
    public Object getType(TableRow instance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getTokens(TableRow instance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the columnIndex
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * @param columnIndex the columnIndex to set
     */
    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

}
