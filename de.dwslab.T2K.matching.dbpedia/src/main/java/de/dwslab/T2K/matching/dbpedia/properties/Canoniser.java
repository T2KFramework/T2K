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
        Collection<String[]> lines = null;
        try {
            lines = CSVUtils.readCSV(fileName, "\t");
        } catch(Exception e) {
            System.out.println("warning no equiv file");
        }
        
        equivalentResources = new LinkedList<List<String>>();
        
        for(String[] line : lines) {
            List<String> equivalent = new ArrayList<String>();
            
            for(String prop : line) {
                equivalent.add(prop);
            }
            
            equivalentResources.add(equivalent);
        }
    }
    
    public Collection<List<String>> loadEquivalentResourcesExternal(String fileName) {
        Collection<String[]> lines = CSVUtils.readCSV(fileName, "\t");
        
        List equivalentResourcesExt = new LinkedList<List<String>>();
        
        for(String[] line : lines) {
            List<String> equivalent = new ArrayList<String>();
            
            for(String prop : line) {
                equivalent.add(prop);
            }
            
            equivalentResourcesExt.add(equivalent);
        }
        return equivalentResourcesExt;
    }
    
    public String canoniseResource(String resource) {
        for(List<String> set : equivalentResources) {            
            if(set.contains(resource)) {                
                return set.get(0);              
            }            
        }        
        return resource;
    }   
    
    public List<String> backwardsCanoniseResource(String resource) {
        for(List<String> set : equivalentResources) {            
            if(set.contains(resource)) {                
                return set;              
            }            
        }        
        return new ArrayList<>();
    } 
    
    public List<String> backwardsCanoniseResourceWithInput(String resource) {
        List<String> l = new ArrayList<>();
        for(List<String> set : equivalentResources) {            
            if(set.contains(resource)) {                
                return set;              
            }            
        }        
        l.add(resource);
        return l;
    } 
}
