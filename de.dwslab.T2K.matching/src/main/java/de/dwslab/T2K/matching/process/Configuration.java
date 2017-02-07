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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 *
 * @author domi
 */
public class Configuration {
    
    private Map<Parameter, Object> config;
    private Map<String, Object> aliases;
    
    public Configuration(Map<Parameter, Object> config, Map<String, Object> aliases) {
        this.config = config;
        this.aliases = aliases;
        
        if(this.aliases==null) {
            this.aliases = new HashMap<String, Object>();
        }
    }
    
    public Configuration clone() {
        Configuration c = new Configuration(new HashMap<Parameter, Object>(), aliases);
        
        for(Entry<Parameter, Object> e : config.entrySet()) {
            c.getConfig().put(e.getKey(), e.getValue());
        }
        
        return c;
    }

    /**
     * @return the config
     */
    public Map<Parameter, Object> getConfig() {
        return config;
    }
    
    public Object getValue(Parameter p) {
        Object value = config.get(p);
        
        if(value==null) {
            return p.getDefaultValue();
        }
        else {
            return value;
        }
    }
    
    public Configuration mergeWith(Configuration conf) {
        getConfig().putAll(conf.getConfig());
        return this;
    }
    
    protected String resolveValue(Parameter p) {
        if(aliases.containsValue(config.get(p))) {
            for(String s : aliases.keySet()) {
                if(aliases.get(s).equals(config.get(p))) {
                    return s;
                }
            }
        }
        
        return config.get(p).toString();
    }
    
    public void writeConfiguration(String file) throws IOException {
        File f = new File(file);
        BufferedWriter write = new BufferedWriter(new FileWriter(f));
        for(Parameter name : config.keySet()) {
//            if(alias.containsValue(config.get(name))) {
//                for(String s : alias.keySet()) {
//                    if(alias.get(s).equals(config.get(name))) {
//                        write.append(name.getName()+","+s+"\n");
//                    }
//                }
//            }
//            else {
//                write.append(name.getName()+","+config.get(name).toString()+"\n");
//            }
            write.append(name.getName()+","+resolveValue(name)+"\n");
        }
        write.flush();
        write.close();        
    }
    
    public static Configuration readConfiguration(String file, List<Parameter> parameters, Map<String, Object> aliases) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Map<Parameter, Object> values = new HashMap<>();
        
        File f = new File(file);
        BufferedReader reader = new BufferedReader(new FileReader(f));
        
        String line = null;
        
        while((line = reader.readLine()) != null) {
            String[] s = line.split(",");
            
            for(Parameter p : parameters) {
                if(p.getName().equals(s[0])) {
                    Object v;
                    
                    if(s[1].matches("-?\\d+\\.\\d+")) {
                        v = Double.parseDouble(s[1]);
                    } else if(s[1].matches("-?\\d+")) {
                        v = Integer.parseInt(s[1]);
                    } else if(s[1].toLowerCase().matches("true|false")) {
                        v = Boolean.parseBoolean(s[1]);
                    } else {
                        if(aliases.containsKey(s[1])) {
                            v = aliases.get(s[1]);
                        } else {
                            String cls = s[1];
                            
                            if(s[1].contains("@")) {
                                cls = s[1].substring(0,s[1].indexOf("@"));
                            }
                            
                            Class<?> parsedClass = Class.forName(cls);
                            v = parsedClass.newInstance();
                        }
                    }
                    
                    if(v!=null) {
                        values.put(p, v);
                    }
                }
            }
        }
        
        reader.close();
        
        return new Configuration(values, aliases);
    }
    
    public String print() {
        StringBuilder sb = new StringBuilder();
        
        for(Entry<Parameter, Object> p: getConfig().entrySet()) {
            //sb.append(String.format("%s: %s %n", p.getKey().getName(), p.getValue()));
            sb.append(String.format("%s: %s %n", p.getKey().getName(), resolveValue(p.getKey())));
        }
        
        return sb.toString();
    }
    
    public String[] getValues() {
        List<String> values = new LinkedList<>();
        
        // we must sort the parameters so they always appear in the same order!
        List<Entry<Parameter, Object>> entries  = new ArrayList<>(getConfig().entrySet());
        Collections.sort(entries, new Comparator<Entry<Parameter, Object>>() {

            @Override
            public int compare(Entry<Parameter, Object> o1,
                    Entry<Parameter, Object> o2) {
                return o1.getKey().getName().compareTo(o2.getKey().getName());
            }
        });
        
        //for(Entry<Parameter, Object> p: getConfig().entrySet()) {
        for(Entry<Parameter, Object> p: entries) {
            //values.add(p.getValue().toString());
            values.add(resolveValue(p.getKey()));
        }
        return values.toArray(new String[0]);
    }
    
    public String[] getParameterNames() {
        List<String> values = new LinkedList<>();
        
        // we must sort the parameters so they always appear in the same order!
        List<Entry<Parameter, Object>> entries  = new ArrayList<>(getConfig().entrySet());
        Collections.sort(entries, new Comparator<Entry<Parameter, Object>>() {

            @Override
            public int compare(Entry<Parameter, Object> o1,
                    Entry<Parameter, Object> o2) {
                return o1.getKey().getName().compareTo(o2.getKey().getName());
            }
        });
        
        //for(Entry<Parameter, Object> p: getConfig().entrySet()) {
        for(Entry<Parameter, Object> p: entries) {
            values.add(p.getKey().getName().toString());
        }
        return values.toArray(new String[0]);
    }

    @Override
    public boolean equals(Object o) {
        return hashCode()==o.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.config);
        return hash;
    }
    
    
}
