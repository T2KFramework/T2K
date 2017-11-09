/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.similarity.functions.numeric;

import de.dwslab.T2K.similarity.functions.SimilarityFunction;

/**
 *
 * @author domi
 */
public class DeviationSimilarity extends SimilarityFunction<Double>{

    @Override
    public Double calculate(Double first, Double second) {                
        if(first==null || second == null) {
            return null;
        }
        if(first.equals(second)) {
            return 1.0;
        }
        else {
            return 0.5*Math.min(Math.abs(first),Math.abs(second))/Math.max(Math.abs(first),Math.abs(second));
        }
    }    
}
