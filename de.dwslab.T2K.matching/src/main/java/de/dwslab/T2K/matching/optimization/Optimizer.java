/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.optimization;

import de.dwslab.T2K.matching.optimization.genetic.GeneticAlgorithm;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.MatchingComponent;
import de.dwslab.T2K.matching.process.ParameterRange;

/**
 *
 * @author domi
 */
public class Optimizer {
    
    private OptimizationAlgorithm algorithm;
    
    public Optimizer(OptimizationAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
    
    public Configuration optimize(MatchingComponent m, ParameterRange parameters) {
        return algorithm.run(parameters, m);
    }    
}
