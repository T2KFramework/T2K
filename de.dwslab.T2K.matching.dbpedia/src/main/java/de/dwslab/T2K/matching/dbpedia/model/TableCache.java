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
package de.dwslab.T2K.matching.dbpedia.model;

import de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.utils.timer.Timer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author dritze
 */
public class TableCache {

    private static TableCache singleton;

    public static TableCache get() {
        if (singleton == null) {
            singleton = new TableCache();
        }

        return singleton;
    }

    protected TableCache() {
        cache = createTableMap();
    }

    private Map<String, MatchingData> cache;

    protected Map<String, MatchingData> getCache() {
        return cache;
    }

    protected Map<String, MatchingData> createTableMap() {
        //return new HashMap<Table, Map<Integer, TableCell[]>>();
        return new ConcurrentHashMap<>(10, 0.9f, 1);
    }

    public MatchingData getOrCreate(String s) {

        MatchingData rowMap = getCache().get(s);

        if (rowMap != null) {            
            return rowMap;
        }

        MatchingData dt = new MatchingData();
        dt.loadWebTable(s, new MatchingParameters(), (de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.tableType) WebtableToDBpediaMatchingProcess.tableType.webtable, true);
        getCache().put(s, dt);
        return dt;
    }

}
