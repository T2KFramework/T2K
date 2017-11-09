package de.dwslab.T2K.similarity.functions.string;

import com.wcohen.ss.Levenstein;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;

/**
 * Calculates the Levenshtein similarity of two strings
 *
 * @author Oliver
 *
 */
public class LevenshteinSimilarity extends SimilarityFunction<String> {

    public Double calculate(String first, String second) {

        if (first == null || second == null) {
            return null;
        } else {
            
            Levenstein l = new Levenstein();
            double score = l.score(first, second);
            score = score / Math.max(first.length(), second.length());
            if (score < 0) {
                score = score * -1;
            }
            score = 1 - score;
            return score;
        }
    }
}
