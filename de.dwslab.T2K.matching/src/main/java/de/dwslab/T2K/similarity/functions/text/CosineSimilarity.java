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
package de.dwslab.T2K.similarity.functions.text;

import de.dwslab.T2K.similarity.functions.SimilarityFunction;


/**
 *
 * @author domi
 */
public class CosineSimilarity extends SimilarityFunction<double[]>{
    
    
    @Override
    public Double calculate(double[] first, double[] second) {
        double dotProduct = 0.0;
//        double magnitude1 = 0.0;
//        double magnitude2 = 0.0;
//        double cosineSimilarity = 0.0;
        double countMatches =0.0;
        for (int i = 0; i < first.length; i++) //docVector1 and docVector2 must be of same length
        {
            dotProduct += first[i] * second[i];  //a.b
            if(second[i]>0.0) {
                countMatches++;
            }
//            magnitude1 += Math.pow(first[i], 2);  //(a^2)
//            magnitude2 += Math.pow(second[i], 2); //(b^2)
        }
        dotProduct = dotProduct+1.0-(1.0/countMatches);
        return dotProduct;
        
//        magnitude1 = Math.sqrt(magnitude1);//sqrt(a^2)
//        magnitude2 = Math.sqrt(magnitude2);//sqrt(b^2)
//
//        if (magnitude1 != 0.0 | magnitude2 != 0.0) {
//            cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
//        } else {
//            return 0.0;
//        }
        //return cosineSimilarity;
    }
    
}
