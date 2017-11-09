package de.dwslab.T2K.matching.dbpedia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;

public class DBpediaSerialiser {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        TableReader r = new TableReader();
        r.setUseUnitDetection(Boolean.parseBoolean(args[0]));
        
        ArrayList<Table> tables = new ArrayList<Table>();
        
        List<File> files = new LinkedList<File>();
        
        if(new File(args[1]).isDirectory()) {
            for(File f : new File(args[1]).listFiles()) {
                files.add(f);
            }
        } else {
            files.add(new File(args[1]));
        }
        
        for(File f : files) {
            try {
                System.out.println(f.getName());
                Table t = r.readLODTable(f.getAbsolutePath());
                tables.add(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
//        ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(args[2]));
//        oout.writeObject(tables);
//        oout.close();
        Kryo kryo = new Kryo();

        Output output = new Output(new FileOutputStream(args[2]));
        kryo.writeObject(output, tables);
        output.close();
        
        System.out.println("done.");
        
    }
    
}
