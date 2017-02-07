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
package de.dwslab.T2K.index.dbpedia;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.io.DefaultIndex;
import de.dwslab.T2K.normalisation.StringNormalizer;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.index.IndexWriter;


public class DBpediaIndexer {

	   public void indexInstances(IIndex index, Table t)
	    {
	       if(!t.isHasKey()) {
	           System.out.println("No key!");
	           return;
	       } else {
	           System.out.println(String.format("Key is [%d] %s", t.getColumns().indexOf(t.getKey()), t.getKey().getHeader()));
	       }
	       
	        IndexWriter writer = index.getIndexWriter();
	        
	        long cnt=0;
	        
	        for(int i : t.getKey().getValues().keySet())
	        {
	            if(t.getColumns().get(0).getValues().containsKey(i)
	                    && t.getKey().getValues().containsKey(i)) {
	            
    	            DBpediaIndexEntry e = new DBpediaIndexEntry();
    	            e.setUri(t.getColumns().get(0).getValues().get(i).toString()); // URI is always the first column
                    
    	            String label = t.getKey().getValues().get(i).toString();
                    
    	            // removes artifacts from the CSV format
    	            String labelClean = StringNormalizer.normaliseValue(label,false);
    	            
    	            // normalises the value to improve lookup results
    	            label = StringNormalizer.normalise(labelClean, true);
    	                                
    	            //System.out.println(String.format("before: %s \t cleaned: %s \t after: %s", t.getKey().getValues().get(i).toString(), labelClean, label));
    	            
    	            e.setLabel(label);
    	            e.setClass_label(t.getHeader().replace(".csv", ""));
    	            
    	            try {
    	                writer.addDocument(e.createDocument());
    	            } catch (IOException e1) {
    	                e1.printStackTrace();
    	            }
    	            
    	            cnt++;
    	            
    	            if(cnt%100000==0)
    	            {
    	                System.out.println("Indexed " + cnt + " items.");
    	            }
	            }
	        }
	        
	        System.out.println("Indexed " + cnt + " items.");
	        
	        index.closeIndexWriter();
	    }
	   
	   public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
        DBpediaIndexer indexer = new DBpediaIndexer();
        TableReader r = new TableReader();
        r.setUseUnitDetection(true);
        
        DefaultIndex idx = new DefaultIndex(args[0]);
        
        if(new File(args[1]).isDirectory()) {
        
            System.out.println("Processing directory");
            
            ArrayList<Table> tables = new ArrayList<Table>(new File(args[1]).list().length);
            
            for(File f : new File(args[1]).listFiles()) {
                try {
                    System.out.println(f.getName());
                    Table t = r.readLODTable(f.getAbsolutePath());
                    tables.add(t);
                    indexer.indexInstances(idx, t);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
//            ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(args[2]));
//            oout.writeObject(tables);
//            oout.close();
//            
            Kryo kryo = new Kryo();

            Output output = new Output(new FileOutputStream(args[2]));
            kryo.writeObject(output, tables);
            output.close();
        
        } else {
            
//            ObjectInputStream oin = new ObjectInputStream(new FileInputStream(args[1]));
//            ArrayList<Table> tables = (ArrayList<Table>)oin.readObject();
//            oin.close();
            
            System.out.println("Loading serialised data");
            
            Kryo kryo = new Kryo();
            
            Input input = new Input(new FileInputStream(args[1]));
            ArrayList<Table> tables = kryo.readObject(input, ArrayList.class);
            input.close();
            
            for(Table t : tables) {
                System.out.println(t.getHeader());
                indexer.indexInstances(idx, t);
            }
            
        }
        
        System.out.println("done.");
    }
}
