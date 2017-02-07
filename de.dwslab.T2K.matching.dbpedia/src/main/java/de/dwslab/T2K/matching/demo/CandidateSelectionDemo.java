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
package de.dwslab.T2K.matching.demo;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.dwslab.T2K.index.io.DefaultIndex;
import de.dwslab.T2K.matching.dbpedia.Preprocessing;
import de.dwslab.T2K.matching.dbpedia.algorithm.CandidateSelectionComponent;
import de.dwslab.T2K.matching.dbpedia.algorithm.KeyIndex;
import de.dwslab.T2K.matching.dbpedia.algorithm.Matchers;
import de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess;
import de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.tableType;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.model.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.similarity.functions.QuadraticSimilarityDecorator;
import de.dwslab.T2K.similarity.functions.date.WeightedDatePartSimilarity;
import de.dwslab.T2K.similarity.functions.numeric.DeviationSimilarity;
import de.dwslab.T2K.similarity.functions.numeric.NormalisedNumericSimilarity;
import de.dwslab.T2K.similarity.functions.string.GeneralisedStringJaccard;
import de.dwslab.T2K.similarity.functions.string.LevenshteinSimilarity;
import de.dwslab.T2K.utils.timer.Timer;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandidateSelectionDemo {

    @Parameter(names = "-config")
    public String configFile;
    
    @Parameter(names = "-dbpedia")
    public String dbpedia;
    
    @Parameter(names = "-index")
    public String index;
    
    @Parameter(names = "-empty")
    public String empty;
    
    public String webtable;
    
    @Parameter
    public List<String> params;
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        CandidateSelectionDemo demo = new CandidateSelectionDemo();
        
        JCommander cmd = new JCommander(demo, args);
        
        if(demo.params.size()==0) {
            cmd.usage();
            return;
        }
        
        demo.webtable = demo.params.get(0);
        
        demo.runDemo();
    }
    
    public void runDemo() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        CandidateSelectionComponent csc = new CandidateSelectionComponent();
        
        Map<String, Object> aliases = new HashMap<>();
        aliases.put("QuadraticDeviation", new QuadraticSimilarityDecorator<Double>(new DeviationSimilarity()));
        aliases.put("QuadraticNormalized", new QuadraticSimilarityDecorator<Double>(new NormalisedNumericSimilarity()));
        aliases.put("GeneralizedLevenshtein", new GeneralisedStringJaccard(new LevenshteinSimilarity(), 0.5, 0.5));
        aliases.put("WeightedDate", new WeightedDatePartSimilarity(1, 3, 5));
        aliases.put("lodtable", WebtableToDBpediaMatchingProcess.tableType.lodtable);
        aliases.put("webtable", WebtableToDBpediaMatchingProcess.tableType.webtable);
        
        Configuration conf = Configuration.readConfiguration(configFile, csc.getParams(), aliases);
        
        Preprocessing.loadSurfaceForms(empty);
        Preprocessing.loadRedirects(empty);
        
        MatchingParameters mp = new MatchingParameters();
        mp.setUseUnitDetection(true);
        mp.setCollectMatchingInfo(true);
        
        MatchingData data = new MatchingData();
        data.loadDBpedia(dbpedia, mp);
        data.loadWebTable(webtable, new Timer("load table"), mp, tableType.webtable);
        
        MatchingLogger log = new MatchingLogger();
        log.prepareLog();
        
        EvaluationParameters eval = new EvaluationParameters();
        eval.setClassGoldStandardLocation(empty);
        eval.setClassHierarchyLocation(empty);
        eval.setCorrectedInstancesLocation(empty);
        eval.setEquivalentPropertiesLocation(empty);
        eval.setInstanceGoldStandardLocation(empty);
        eval.setPropertyGoldStandardLocation(empty);
        eval.setPropertyRangeGoldstandardLocation(empty);
        eval.setPropertyRangesLocation(empty);

        GoldStandard gs = new GoldStandard();
        gs.initialise(webtable, data.getWebtable(), eval);
        
        Similarities sim = new Similarities();
        Matchers matchers = new Matchers(sim, mp, new Timer(""), gs, log);
        
        csc.initialise(matchers, data, eval, null, log, mp, sim, new Timer("a"));
        
        //KeyIndex idx = createIndex(data);
        KeyIndex idx = new KeyIndex();
        idx.setLuceneIndex(new DefaultIndex(index));
        csc.setKeyIndex(idx);
        csc.setWebTableName(webtable);
        matchers.getCandidateSelectionMatcher().setKeyIndex(idx);
        matchers.getCandidateRefinementMatcher().setKeyIndex(idx);
        
        csc.run(conf);
        
        
        System.out.println(csc.getClassSimilarity().getOutput());
    }
    
//    private KeyIndex createIndex(MatchingData data) {
//        KeyIndex keyIndex = new KeyIndex();
//        System.out.println("No index provided, creating in-memory index");
//
//        IIndex index = new InMemoryIndex();
//        DBpediaIndexer indexer = new DBpediaIndexer();
//
//        for (Table t : data.getDbpediaTables()) {
//            System.out.println("indexing " + t.getHeader());
//            indexer.indexInstances(index, t);
//        }
//
//        keyIndex.setLuceneIndex(index);
//        
//        return keyIndex;
//    }
}
