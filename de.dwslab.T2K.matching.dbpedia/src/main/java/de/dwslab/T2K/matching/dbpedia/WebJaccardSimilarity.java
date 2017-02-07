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
package de.dwslab.T2K.matching.dbpedia;

import de.dwslab.T2K.normalisation.StringNormalizer;
import de.dwslab.T2K.similarity.functions.string.JaccardSimilarity;
import java.util.regex.Pattern;


public class WebJaccardSimilarity extends JaccardSimilarity {
//public class WebJaccardSimilarity extends SimpleJaccardSimilarity {

    private static final Pattern companyPattern = Pattern.compile("co\\.|\\sco$");
    private static final Pattern corporationPattern = Pattern.compile("corp\\.|\\scorp$");
    private static final Pattern removePattern = Pattern.compile("inc\\.|s\\.a\\.|\\'s");
    
    
    @Override
    public Double calculate(String first, String second) {
        
        if(first==null || second==null) {
            return null;
        }
        
        String s1 = StringNormalizer.normaliseValue(first, true) + "";
        String s2 = StringNormalizer.normaliseValue(second, true) + "";
        
        //s1 = s1.replace("\'s", "") + "";
        //s2 = s2.replace("\'s", "") + "";
        
        //TODO try to remove co and corp completely
        //s1 =s1.replace("co.", " company ");
        //s1 =s1.replaceAll("\\sco$", " company ");
        s1 = companyPattern.matcher(s1).replaceFirst("company");
        //s2 =s2.replace("co.", " company ");
        //s2 =s2.replaceAll("\\sco$", " company ");
        s2 = companyPattern.matcher(s2).replaceFirst("company");
        
        //s1 =s1.replace("corp.", "corporation");
        //s1 =s1.replaceAll("\\scorp$", "corporation");
        s1 = corporationPattern.matcher(s1).replaceFirst("corporation");
        //s2 =s2.replace("corp.", "corporation");
        //s2 =s2.replaceAll("\\scorp$", "corporation");
        s2 = corporationPattern.matcher(s2).replaceFirst("corporation");
        
        //s1 = s1.replace("inc.", "");
        //s2 = s2.replace("inc.", "");
        
        //s1 = s1.replace("s.a.", "");
        //s2 = s2.replace("s.a.", "");
        
        s1 = removePattern.matcher(s1).replaceFirst("");
        s2 = removePattern.matcher(s2).replaceFirst("");
        
        //return super.calculate(s1, s2);
        return super.calculate(s1, s2);
    }
    
}
