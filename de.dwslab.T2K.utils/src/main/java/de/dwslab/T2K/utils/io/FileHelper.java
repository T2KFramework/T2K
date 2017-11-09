package de.dwslab.T2K.utils.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

public class FileHelper {

    public static long countLines(String path) {
        
        long lines=0;
        
        try {
            BufferedReader r = new BufferedReader(getReader(path));
            
            
            while(r.readLine()!=null)
            {
                lines++;
            }
            
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
            lines=-1;
        }
        
        return lines;
    }
    
    public static Reader getReader(String path) throws FileNotFoundException, IOException {
        if (path.endsWith(".gz")) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(path));
            return new InputStreamReader(gzip, "UTF-8");
        } else {
            return new InputStreamReader(new FileInputStream(path), "UTF-8");
        }
    }
}
