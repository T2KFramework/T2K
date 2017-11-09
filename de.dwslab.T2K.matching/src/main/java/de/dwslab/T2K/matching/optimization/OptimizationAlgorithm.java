/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.optimization;

import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.MatchingComponent;
import de.dwslab.T2K.matching.process.ParameterRange;

/**
 *
 * @author domi
 */
public interface OptimizationAlgorithm {
    
    public Configuration run(ParameterRange ranges, MatchingComponent m);
    
}
