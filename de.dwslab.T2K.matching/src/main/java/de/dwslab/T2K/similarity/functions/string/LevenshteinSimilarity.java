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
package de.dwslab.T2K.similarity.functions.string;

import com.wcohen.ss.Levenstein;

import de.dwslab.T2K.similarity.functions.SimilarityFunction;

/**
 * Calculates the Levenshtein similarity of two strings
 * @author Oliver
 *
 */
public class LevenshteinSimilarity extends SimilarityFunction<String> {

    public Double calculate(String first, String second) {
        if(first==null || second==null) {
            return null;
        } else {
            Levenstein l = new Levenstein();
            double score = l.score(first, second);
            score = score/Math.max(first.length(), second.length());
            if(score<0) {
                score = score*-1;
            }
            score = 1-score;
            return score;
        }
    }
}
