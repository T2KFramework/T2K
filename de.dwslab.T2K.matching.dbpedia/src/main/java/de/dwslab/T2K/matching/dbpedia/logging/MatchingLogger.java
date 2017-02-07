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

import de.dwslab.T2K.tableprocessor.model.Table;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class MatchingLogger {
    private StringBuilder matchingLog = null;
    public StringBuilder getMatchingLog() {
        return matchingLog;
    }
    
    public void logData(String data) {
        matchingLog.append(data);
        matchingLog.append("\n\n\n");
    }
    
    public void prepareLog() {
        matchingLog = new StringBuilder();
        if(!new File("log/").exists()) {
            new File("log/").mkdir();
        }
    }
    
    public void writeLog(Table webtable) {
        try {
            FileUtils.writeStringToFile(new File("log/" + webtable.getHeader() + ".log"), matchingLog.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
