/**
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of
							Mannheim (t2k@dwslab.de)
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
package de.mannheim.uni.matching.dbpedia;


import de.dwslab.T2K.matching.dbpedia.model.settings.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingParameters;
import junit.framework.TestCase;

public class WebtableToDBpediaMatcherTest extends TestCase {

//    public void testMatchWebTable() {
//        WebtableToDBpediaMatcher m = new WebtableToDBpediaMatcher();
//
//        m.setUseUnitDetection(true);
//        m.setForcePropertyOneToOneMapping(true);
//        // m.setCollectMatchingInfo(true);
//        // m.setFastJoinPath("C:/Users/Oliver/SkyDrive/dws/tools/fastjoin/win32/FastJoin.exe");
//
//        //m.loadDBpedia("in/dbpedia");
//        m.loadDBpedia("in/country_film_units.bin");
//        m.setRunParallel(false);
//
//        m.setInstanceGoldStandardLocation("test/goldstandard/instance/");
//        m.setPropertyGoldStandardLocation("test/goldstandard/property");
//        m.setEquivalentPropertiesLocation("test/goldstandard/equivalentProperties.tsv");
//        m.setClassGoldStandardLocation("test/goldstandard/classes.csv");
//
//        m.matchWebTable("in/web/11278409_0_3742771475298785475.csv");
//        // m.matchWebTable("in/web/24036779_0_5608105867560183058.csv");
//    }
    
    // public void testMatchWebTableErrors() {
    // WebtableToDBpediaMatcher m = new WebtableToDBpediaMatcher();
    //
    // m.setUseUnitDetection(false);
    // m.setForcePropertyOneToOneMapping(true);
    // m.setCollectMatchingInfo(true);
    //
    // m.loadDBpedia("in/dbpedia");
    //
    // m.setInstanceGoldStandardLocation("test/goldstandard/instance/");
    // m.setPropertyGoldStandardLocation("test/goldstandard/property");
    // m.setEquivalentPropertiesLocation("test/goldstandard/equivalentProperties.tsv");
    // m.setClassGoldStandardLocation("test/goldstandard/classes.csv");
    //
    // m.matchWebTable("in/web/24036779_0_5608105867560183058_errors.csv");
    // }

//     public void testMatchWebTableSimple() {
//     WebtableToDBpediaMatcher m = new WebtableToDBpediaMatcher();
//    
//     MatchingParameters p = new MatchingParameters();
//     p.setUseUnitDetection(false);
//     p.setForcePropertyOneToOneMapping(false);
//     p.setCollectMatchingInfo(true);
//    m.setMatchingParameters(p);
//     //m.loadDBpedia("in/dbpedia");
//     //m.loadDBpedia("in/country_film_units.bin");
//    m.loadDBpedia("in/all_kryo.bin");
//     //m.loadDBpedia("in/all_with_units.bin");
//    
//     EvaluationParameters ep = new EvaluationParameters();
//     ep.setInstanceGoldStandardLocation("test/goldstandard/instance/");
//     ep.setPropertyGoldStandardLocation("test/goldstandard/property");
//     ep.setEquivalentPropertiesLocation("test/goldstandard/equivalentProperties.tsv");
//     ep.setClassGoldStandardLocation("test/goldstandard/classes.csv");
//    m.setEvaluationParameters(ep);
//     
//     m.matchWebTable("in/web/simple.csv");
//     }

}
