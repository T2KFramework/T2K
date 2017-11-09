package de.dwslab.T2K.index.dbpedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.apache.lucene.index.IndexWriter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.io.DefaultIndex;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.uni_mannheim.informatik.dws.t2k.normalisation.StringNormalizer;

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
                    Table t = r.readKGTable(f.getAbsolutePath());
                    //Table t = r.readLODTable(f.getAbsolutePath());
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
