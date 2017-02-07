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
package de.dwslab.T2K.matching.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author domi
 */
public class ParameterRange {

    private Map<Parameter, List> ranges;
    private Map<String, Object> aliases;

    /**
     * @return the ranges
     */
    public Map<Parameter, List> getRanges() {
        return ranges;
    }

    /**
     * @param ranges the ranges to set
     */
    public void setRanges(Map<Parameter, List> ranges) {
        this.ranges = ranges;
    }

    public Map<String, Object> getAliases() {
        return aliases;
    }
    public void setAliases(Map<String, Object> aliases) {
        this.aliases = aliases;
    }
    
    /**
     * NAME,{a,b,c},TYPE,INNERTYPE
     *
     * @param filePath
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void initialize(String filePath, MatchingComponent m, Map<String, Object> aliases) {
        System.out.println("PATH " +filePath               );
        try {
        File f = new File(filePath);
        Parameter p = null;
        BufferedReader read = new BufferedReader(new FileReader(f));
        String line = read.readLine();
        Map<Parameter, List> ranges = new HashMap<>();
        while (line != null) {
            String name = line.split(",")[0];
            System.out.println("name:  " +name + " paras: " + m.getParameters());
            for (Parameter para : m.getParameters()) {
                if (name.equals(para.getName())) {
                    p = para;
                }
            }
            
            List<Object> values = new ArrayList<>();
            String valuesLine = line.split("\\{")[1];
            valuesLine = valuesLine.split("}")[0];
            String type = line.split("}")[1];
            type = type.replace(",", "");
            for (String s : valuesLine.split(",")) {
                if (type.equals("Integer")) {
                    values.add(Integer.parseInt(s));
                } else if (type.equals("Double")) {
                    values.add(Double.parseDouble(s));
                }    
                else if (type.equals("Boolean")) {
                    values.add(Boolean.parseBoolean(s));    
                } else {
                    try {
                        Class<?> parsedClass = Class.forName(s);
                        values.add(parsedClass.newInstance());
                    } catch (Exception e) {
                        values.add(aliases.get(s));
                    }
                }
            }
            ranges.put(p, values);
            line = read.readLine();
        }
        this.ranges = ranges;
        this.aliases = aliases;
        read.close();
        }catch(Exception e ) {
            e.printStackTrace();
        }
    }
    
    public ParameterRange clone() {
        ParameterRange pr = new ParameterRange();
        
        for(Map.Entry<Parameter, List> e : ranges.entrySet()) {
            pr.getRanges().put(e.getKey(), e.getValue());
        }
        for(Map.Entry<String, Object> e : aliases.entrySet()) {
            pr.getAliases().put(e.getKey(), e.getValue());
        }        
        return pr;
    }
}
