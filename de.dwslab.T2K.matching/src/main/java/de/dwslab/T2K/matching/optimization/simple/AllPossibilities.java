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
package de.dwslab.T2K.matching.optimization.simple;

import de.dwslab.T2K.matching.optimization.OptimizationAlgorithm;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.MatchingComponent;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.process.ParameterRange;
import de.dwslab.T2K.utils.query.Func;
import de.dwslab.T2K.utils.query.Q;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author domi
 */
public class AllPossibilities implements OptimizationAlgorithm {

    private ParameterRange ranges;
    private MatchingComponent m;
    private Map<Configuration, Double> configs;
    private Map<Integer, Parameter> numberToParam;
    private Map<Parameter, Integer> paramToNumber;
    private Map<Parameter,List> rangesToOptimize;
    
    public AllPossibilities() {
    }
    
    @Override
    public Configuration run(ParameterRange ranges, MatchingComponent m) {
        this.ranges = ranges;
        this.m = m;
        
        rangesToOptimize = new HashMap<>();
        numberToParam = new TreeMap<>();
        paramToNumber = new HashMap<>();
        configs = new HashMap<>();
        int number = 0;
        for (Parameter p1 : ranges.getRanges().keySet()) {
            if (m.getParameters().contains(p1)) { 
                rangesToOptimize.put(p1, ranges.getRanges().get(p1));
                numberToParam.put(number, p1);
                paramToNumber.put(p1, number);
                number++;
            }
        }
        List<Integer> start = new ArrayList<>();
        for(Parameter p : rangesToOptimize.keySet()) {
            start.add(0);
        }
        recursive(start);
        return(Q.max(configs.entrySet(), new Func<Double, Map.Entry<Configuration, Double>>() {

            @Override
            public Double invoke(Map.Entry<Configuration, Double> in) {
                return in.getValue();
            }
        }).getKey());
    }
    
    /**
     * Try out all possible possibilites of parameter assignments (configurations).
     * 
     * @param indices 
     */
    private void recursive(List<Integer> indices) {
        //compute score for current config 
        Map<Parameter, Object> assigned = new HashMap<>();
        //create the current configuration and compute the score
        for(Parameter p : rangesToOptimize.keySet()) {
            assigned.put(p, rangesToOptimize.get(p).get(indices.get(paramToNumber.get(p))));
        }
        Configuration config = new Configuration(assigned, ranges.getAliases());
        if(!configs.containsKey(config)) {
            configs.put(config, m.evaluate(config));  
        }
        //recursive call to get other possibilities
        for(int i : numberToParam.keySet()) {
            if(indices.get(i)<rangesToOptimize.get(numberToParam.get(i)).size()-1) {                
                List<Integer> cpy = new ArrayList<>(indices);
                cpy.set(i, Integer.valueOf(cpy.get(i)+1));
                recursive(cpy);
            }
        }
    }
}
