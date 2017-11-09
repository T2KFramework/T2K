
package de.dwslab.T2K.matching.experiments;

import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.netty.handler.queue.BufferedWriteHandler;

/**
 *
 * @author domi
 */
public class LabelExtraction {
    
    public static void main(String args[]) throws IOException {
        
        File dir = new File(args[0]);
        Map<String, List<String>> labelToRow = new HashMap<>();
        
        for(File f : dir.listFiles()) {
            TableReader reader = new TableReader();
            Table t = reader.readWebTableFromJson(f.getAbsolutePath());
            for(Integer i : t.getKey().getValues().keySet()) {
                String keyValue = t.getKey().getValues().get(i).toString();
                String id = t.getHeader()+"_"+i;
                if(labelToRow.containsKey(keyValue)) {
                    labelToRow.get(keyValue).add(id);
                }
                else {
                    List l = new ArrayList();
                    l.add(id);
                    labelToRow.put(keyValue, l);
                }
            }
        }
        BufferedWriter write = new BufferedWriter(new FileWriter(new File("labels.csv")));
        for(String s : labelToRow.keySet()) {
            write.write(s+"\t"+labelToRow.get(s)+"\n");
        } 
        write.flush();
        write.close();
    }
    
}
