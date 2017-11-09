/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        Map<Parameter, Object> assignedValues = new HashMap();
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
