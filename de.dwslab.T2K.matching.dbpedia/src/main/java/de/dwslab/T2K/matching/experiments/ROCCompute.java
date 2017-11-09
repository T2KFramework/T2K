
package de.dwslab.T2K.matching.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author domi
 */
public class ROCCompute {
    
    public static void main(String args []) throws FileNotFoundException, IOException {
        
        File f = new File("C:\\Users\\domi\\WebTables\\Improvements\\rocWithNoFreqNew.csv");
        
        BufferedReader read = new BufferedReader(new FileReader(f));
        
        //double FP = 219.0;
        //double FP = 218.0;
       //double TP = 316.0;
//        double TP = 340.0;
//        double FP = 197.0;
        
        double TP = 339.0;
        double FP = 196.0;
        
        String line =read.readLine();
        double TPValue = 0.0;
        double FPValue = 0.0;
        while(line != null){
            Integer correct = Integer.parseInt(line.split("\t")[0]);
            if(correct == 1) {
                TPValue++;
            }
            else {
                FPValue++;
            }
            double TPRate = TPValue / TP;
            double FPRate = FPValue / FP;
            System.out.println(FPRate + "\t" + TPRate);
            line = read.readLine();
        }
        
    }
    
}
