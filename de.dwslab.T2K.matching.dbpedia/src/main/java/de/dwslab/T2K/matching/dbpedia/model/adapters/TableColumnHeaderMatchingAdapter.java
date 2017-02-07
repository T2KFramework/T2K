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
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author domi
 */
public class TableColumnHeaderMatchingAdapter extends MatchingAdapter<TableColumn> {

    @Override
    public Object getLabel(TableColumn instance) {
        if (!instance.getHeaderList().isEmpty()) {
            List<String> allTokens = new ArrayList<>();
            for (Object s : instance.getHeaderList()) {
                String[] tokens = StringUtils.splitByCharacterTypeCamelCase(s.toString().replaceAll("\\(.*\\)", "").replace("_label", "").replace(".", ""));
                String header = "";
                for (String t : tokens) {
                    header = header + t.toLowerCase() + " ";
                }
                header = header.trim();
                allTokens.add(header);
            }
            return allTokens;
        } else {
            String[] tokens = StringUtils.splitByCharacterTypeCamelCase(instance.getHeader().toString().replaceAll("\\(.*\\)", "").replace("_label", "").replace(".", ""));
            String header = "";
            for (String s : tokens) {
                header = header + s.toLowerCase() + " ";
            }
            header = header.trim();
            return header;
        }
    }

    @Override
    public Object getType(TableColumn instance) {
        return instance.getDataType();
    }

    @Override
    public Object getTokens(TableColumn instance) {
        return null;
    }

    @Override
    public boolean isMultiValued(TableColumn instance) {
        return instance.getHeaderList().isEmpty();
    }
}
