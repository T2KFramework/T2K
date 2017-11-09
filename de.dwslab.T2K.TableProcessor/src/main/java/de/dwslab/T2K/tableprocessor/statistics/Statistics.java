/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.tableprocessor.statistics;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.timer.Timer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author domi
 */
public class Statistics {

    public static void main(String args[]) throws UnsupportedEncodingException, FileNotFoundException, IOException, Exception {

        String fileDir = args[0];
        File dir = new File(fileDir);
        int counter = 0;

        final BufferedWriter write  = new BufferedWriter(new FileWriter("sizes.csv"));                       
        
        for (File fh : dir.listFiles()) {
            if (fh.isDirectory()) {

                Collection<File> files = Arrays.asList(fh.listFiles());

                new Parallel<File>().foreach(files, new Consumer<File>() {
                    
                    @Override
                    public void execute(File parameter) {
                         
                        try {
                            
                        CSVReader read = new CSVReader(new BufferedReader(new FileReader(parameter)));
                        
                        List<String[]> cont = read.readAll();

                        if (cont.size() > 0) {
                            synchronized(write) {
                                write.write(parameter.getName() + ";" + cont.size() + ";" + cont.get(0).length + "\n");
                            }
                        }

                        read.close();        
                        
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(Statistics.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(Statistics.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                }, new Timer("stats"), "finished");
                write.flush();
            }
        }
         write.close();
    }

}
