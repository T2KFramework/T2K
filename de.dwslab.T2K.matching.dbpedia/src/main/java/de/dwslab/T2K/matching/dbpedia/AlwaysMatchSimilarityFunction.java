/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.dbpedia;

import de.dwslab.T2K.similarity.functions.SimilarityFunction;

/**
 *
 * @author domi
 */
public class AlwaysMatchSimilarityFunction extends SimilarityFunction<String>{

    @Override
    public Double calculate(String first, String second) {
        return 1.0;
    }
    
    
    
}
