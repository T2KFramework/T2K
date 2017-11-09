/** Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.context.StopWordRemover;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dritze
 */
public class TableRowMatchingStopWordAdapter extends MatchingAdapter<TableRow> {

    private Map<String, Integer> count = new HashMap<>();

    @Override
    public Object getLabel(TableRow instance) {

//        int all = 0;
//        for (String s : count.keySet()) {
//            all += count.get(s);
//        }

        String key;
        if (instance.getKey() instanceof List) {
            key = ((List) instance.getKey()).get(0).toString();
        } else {
            key = instance.getKey().toString();
        }
        String newLabel = "";
        key = key.replaceAll("\\[.*\\]", "");
        key = key.replaceAll(",.*", "");
        String[] cellContent = key.split("\\s");
        for (String s : cellContent) {
//            if (count.get(s) != null) {
//                int countLabel = count.get(s);
//                if (countLabel / all > 0.1) {
//                    continue;
//                }
//            }
            if(s.startsWith("?") || s.startsWith("/")) {
                continue;
            }
            if (!StopWordRemover.isStopWord(s) && !s.equals("-")) {
                newLabel += s + " ";
            }
        }
        newLabel = newLabel.trim();
        if (newLabel.length() == 1) {
            return "";
        }
        //System.out.println("row adapter: "+ newLabel + " for " + instance);
        return newLabel;
    }

    @Override
    public boolean isMultiValued(TableRow instance) {
        return false;
        //return instance.getKey() instanceof List;
    }

    @Override
    public Collection getLabels(TableRow instance) {
        if (isMultiValued(instance)) {
            List l = (List) instance.getKey();
            List finalList = new ArrayList<>();
            for (Object o : l) {
                String newLabel = "";
                String[] cellContent = o.toString().split("\\s");
                for (String s : cellContent) {
                    if (!StopWordRemover.isStopWord(s) && !s.equals("-")) {
                        newLabel += s + " ";
                    }
                }
                newLabel = newLabel.trim();
                finalList.add(newLabel);
            }
            //return (List) instance.getKey();
            return finalList;
        } else {
            return super.getLabels(instance);
        }
    }

    @Override
    public Object getType(TableRow instance) {
        return instance.getTable().getHeader();
    }

    @Override
    public Object getTokens(TableRow instance) {
        List<String> tokens = new ArrayList<>();
        for (TableCell tc : instance.getCells()) {
            String[] cellContent = tc.getValue().toString().split("\\s");
            for (String s : cellContent) {
                if (!StopWordRemover.isStopWord(s)) {
                    tokens.add(s);
                }
            }

        }
        return tokens;
    }

    /**
     * @return the count
     */
    public Map<String, Integer> getCount() {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(Map<String, Integer> count) {
        this.count = count;
    }

}
