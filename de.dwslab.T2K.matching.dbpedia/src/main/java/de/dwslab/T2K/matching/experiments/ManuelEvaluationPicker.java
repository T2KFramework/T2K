
package de.dwslab.T2K.matching.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author domi
 */
public class ManuelEvaluationPicker {
    
    public static void main(String args[]) throws FileNotFoundException, IOException {
        
        BufferedReader readExamples = new BufferedReader(new FileReader(new File("C:\\Users\\domi\\WebTables\\ErrorAnalysis\\InstanceFP.tsv"))); 
        BufferedReader readClassCount = new BufferedReader(new FileReader(new File("C:\\Users\\domi\\WebTables\\ErrorAnalysis\\InstanceFPClasses.tsv"))); 
        
        Map<String,Integer> counts = new HashMap<>();
        
        String line = readClassCount.readLine();
        while(line != null) {
            String[] cont =  line.split("\t");
            counts.put(cont[0], Integer.parseInt(cont[3]));
            line = readClassCount.readLine();
        }
        
        Map<String,List<String>> examples = new HashMap<>();
        
        line = readExamples.readLine();
        while(line != null) {
            String[] cont =  line.split("\t");
            String className = cont[11].split("---")[0];
            if(examples.containsKey(className)) {
                examples.get(className).add(line);
            }
            else {
                List<String> l = new ArrayList<>();
                l.add(line);
                examples.put(className, l);
            }
            line = readExamples.readLine();
        }
        
        List<String> outputLines = new ArrayList<>();
        for(String className : counts.keySet()) {
            int number = counts.get(className);
            
        }
    }
}
