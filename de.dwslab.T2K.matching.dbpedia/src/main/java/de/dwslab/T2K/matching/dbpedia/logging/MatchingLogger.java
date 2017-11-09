package de.dwslab.T2K.matching.dbpedia.logging;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import de.dwslab.T2K.tableprocessor.model.Table;

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
