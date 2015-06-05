/**
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of
							Mannheim (t2k@dwslab.de)
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
package de.dwslab.T2K.matching.dbpedia.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import au.com.bytecode.opencsv.CSVReader;
import de.dwslab.T2K.matching.adapters.IdentityAdapter;
import de.dwslab.T2K.matching.correspondences.Correspondence;

public class CorrespondenceCollector {

    public Collection<Correspondence<String>> collectCorrespondences(String directory, Boolean filterCorrect) {
        Collection<Correspondence<String>> results = new LinkedList<Correspondence<String>>();
        
        for(File f : new File(directory).listFiles()) {
        
            try {
                CSVReader r = new CSVReader(new FileReader(f.getAbsolutePath()));
                
                String[] values = r.readNext();
                
                while((values = r.readNext()) != null) {
                    Correspondence<String> c = new Correspondence<String>(values[0], values[1], Double.parseDouble(values[2]));
                    c.setCorrect(Boolean.parseBoolean(values[3]));
                    c.setCorrectValue(values[4]);
                    c.setSource(f.getName());
                    
                    if(filterCorrect==null || filterCorrect==c.isCorrect()) {
                        results.add(c);
                    }
                }
                
                r.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return results;
    }
    
    public static void main(String[] args) {
        CorrespondenceCollector col = new CorrespondenceCollector();
        
        Boolean b = null;
        
        if(args.length>2) {
            b = Boolean.parseBoolean(args[2]);
        }
        
        Collection<Correspondence<String>> correspondeces = col.collectCorrespondences(args[0], b);
        
        Correspondence.writeCollection(correspondeces, args[1], new IdentityAdapter<String>(), new IdentityAdapter<String>());
    }
}
