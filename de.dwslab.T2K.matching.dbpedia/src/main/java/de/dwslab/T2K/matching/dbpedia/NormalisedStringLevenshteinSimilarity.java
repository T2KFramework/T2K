package de.dwslab.T2K.matching.dbpedia;

import de.dwslab.T2K.similarity.functions.string.LevenshteinSimilarity;
import de.uni_mannheim.informatik.dws.t2k.normalisation.StringNormalizer;

public class NormalisedStringLevenshteinSimilarity extends LevenshteinSimilarity {

    @Override
    public Double calculate(String first, String second) {

        first = StringNormalizer.normaliseValue(first, true);
        second = StringNormalizer.normaliseValue(second, true);
        
        return super.calculate(first, second);
    }
    
}
