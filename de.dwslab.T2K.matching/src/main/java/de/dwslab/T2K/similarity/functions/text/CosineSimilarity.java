
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
