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
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author domi
 */
public class TableRowTokenMatchingAdapter extends MatchingAdapter<TableRow> {

    @Override
    public Object getLabel(TableRow instance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getType(TableRow instance) {
        return instance.getTable().getHeader();
    }

    @Override
    public Object getTokens(TableRow instance) {
        List<String> tokens = new ArrayList<>();
        for (TableCell tc : instance.getCells()) {
            String[] tokenStrings = tc.getValue().toString().split("\\s");
            tokens.addAll(Arrays.asList(tokenStrings));
        }
        String[] tokenStrings = instance.getTable().getPageTitle().split("\\s");
        tokens.addAll(Arrays.asList(tokenStrings));

        tokenStrings = instance.getTable().getTableTitle().split("\\s");
        tokens.addAll(Arrays.asList(tokenStrings));
//
//        tokenStrings = instance.getTable().getContextBeforeTable().split("\\s");
//        tokens.addAll(Arrays.asList(tokenStrings));
//
//        tokenStrings = instance.getTable().getContextAfterTable().split("\\s");
//        tokens.addAll(Arrays.asList(tokenStrings));

        return tokens;
    }

}
