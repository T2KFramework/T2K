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
package de.dwslab.T2K.matching.dbpedia.properties;

import java.util.Collection;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import de.dwslab.T2K.utils.io.CSVUtils;

import java.util.ArrayList;
import java.util.List;

public class Canoniser {

    private Collection<List<String>> equivalentResources;
    public Collection<List<String>> getEquivalentResources() {
        return equivalentResources;
    }
    
    public void loadEquivalentResources(String fileName) {
        Collection<String[]> lines = CSVUtils.readCSV(fileName, "\t");
        
        equivalentResources = new LinkedList<List<String>>();
        
        for(String[] line : lines) {
            List<String> equivalent = new ArrayList<String>();
            
            for(String prop : line) {
                equivalent.add(prop);
            }
            
            equivalentResources.add(equivalent);
        }
    }
    
    public String canoniseResource(String resource) {
        for(List<String> set : equivalentResources) {            
            if(set.contains(resource)) {                
                return set.get(0);                
            }            
        }        
        return resource;
    }    
}
