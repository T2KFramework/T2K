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
package de.dwslab.T2K.matching.optimization.genetic;

import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.MatchingComponent;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.process.ParameterRange;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author domi
 */
public class Individual implements Comparable<Object> {

    private Configuration config;
    private double score = -1.0;
    private MatchingComponent m;

    public Individual(ParameterRange ranges, MatchingComponent m) {
        Map<Parameter, Object> assignedValues = new HashMap<>();
//        for(Parameter p : ranges.getRanges().keySet()) {
//            System.out.println("param: " + p.getName() + " list: " + ranges.getRanges().get(p));
//        }
        
        for (Parameter p : ranges.getRanges().keySet()) {
            if (m.getParameters().contains(p)) {
                int index = (int) Math.round(Math.random() * (ranges.getRanges().get(p).size()-1));
                assignedValues.put(p, ranges.getRanges().get(p).get(index));
            }
        }
        this.m = m;
        this.config = new Configuration(assignedValues, ranges.getAliases());
        score = computeScore();
    }

    /**
     * Create an individual with a fixed configuration.
     *
     */
    public Individual(Configuration config, MatchingComponent m) {
        this.config = config;
        this.m = m;
        score = computeScore();
    }

    private double computeScore() {
        return m.evaluate(config);
    }

    /**
     * Compares two individuals by means of their scores.
     *
     * @param o
     * @return
     */
    public int compareTo(Object o) {
        if (o instanceof Individual) {
            if (this.score - ((Individual) o).getScore() == 0) {
                return 0;
            }
            if (this.score - ((Individual) o).getScore() < 0) {
                return -1;
            }
        }
        return 1;
    }

    public double getScore() {
        return this.score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    /**
     * @return the config
     */
    public Configuration getConfig() {
        return config;
    }
}
