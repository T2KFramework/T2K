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
package de.dwslab.T2K.matching.experiments;

//package de.mannheim.uni.matching.experiments;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.JarURLConnection;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//import de.mannheim.uni.index.io.DefaultIndex;
//import de.mannheim.uni.matching.dbpedia.Preprocessing;
//import de.mannheim.uni.matching.dbpedia.main;
//import de.mannheim.uni.matching.dbpedia.algorithm.CandidateSelectionComponent;
//import de.mannheim.uni.matching.dbpedia.algorithm.Matchers;
//import de.mannheim.uni.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess;
//import de.mannheim.uni.matching.dbpedia.logging.MatchingLogger;
//import de.mannheim.uni.matching.dbpedia.model.EvaluationParameters;
//import de.mannheim.uni.matching.dbpedia.model.GoldStandard;
//import de.mannheim.uni.matching.dbpedia.model.MatchingData;
//import de.mannheim.uni.matching.dbpedia.model.MatchingParameters;
//import de.mannheim.uni.matching.dbpedia.model.MatchingResult;
//import de.mannheim.uni.matching.dbpedia.model.Similarities;
//import de.mannheim.uni.matching.optimization.Optimizer;
//import de.mannheim.uni.matching.optimization.genetic.GeneticAlgorithm;
//import de.mannheim.uni.matching.process.Configuration;
//import de.mannheim.uni.matching.process.Parameter;
//import de.mannheim.uni.matching.process.ParameterRange;
//import de.mannheim.uni.similarity.functions.QuadraticSimilarityDecorator;
//import de.mannheim.uni.similarity.functions.date.WeightedDatePartSimilarity;
//import de.mannheim.uni.similarity.functions.numeric.DeviationSimilarity;
//import de.mannheim.uni.similarity.functions.numeric.NormalisedNumericSimilarity;
//import de.mannheim.uni.similarity.functions.string.GeneralisedStringJaccard;
//import de.mannheim.uni.similarity.functions.string.LevenshteinSimilarity;
//import de.mannheim.uni.utils.timer.Timer;
//
//public class CandidateSelection extends CandidateSelectionComponent {
//
//    public static Long getTime(Class<?> cl) {
//        try {
//            String rn = cl.getName().replace('.', '/') + ".class";
//            JarURLConnection j = (JarURLConnection) ClassLoader.getSystemResource(rn).openConnection();
//            return j.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
//        } catch (Exception e) {
//            return null;
//        }
//    }
//    
//    public static void main(String[] args) {
//
//        String dbp = args[0];
//        String idx = args[1];
//        Boolean useUnits = Boolean.parseBoolean(args[2]);
//        String inst = args[3];
//        String prop = args[4];
//        String equiv = args[5];
//        String cls = args[6];
//        String web = args[7];
//        String surface = args[8];
//        boolean verbose = Boolean.parseBoolean(args[9]);
//
//        String paramRange = args[10];
//
//        Timer mainTimer = new Timer("Matching");
//        
//        //WebtableToDBpediaMatcher m = new WebtableToDBpediaMatcher();
//        WebtableToDBpediaMatchingProcess m = new WebtableToDBpediaMatchingProcess();
//        Timer tdbp = new Timer("Load DBpedia", mainTimer);
//        
//        MatchingParameters loadDbpParams = new MatchingParameters();
//        loadDbpParams.setUseUnitDetection(useUnits);
//        m.setMatchingParameters(loadDbpParams);
//        
//        if (new File(idx).exists()) {
//            m.setLuceneIndex(new DefaultIndex(idx));
//        }
//        
//        m.loadDBpedia(dbp);
//        tdbp.stop();
//
//        MatchingParameters params = new MatchingParameters();
//        params.setUseUnitDetection(useUnits);
//        params.setCollectMatchingInfo(verbose);
//        EvaluationParameters eval = new EvaluationParameters();
//
//        eval.setInstanceGoldStandardLocation(inst);
//        eval.setPropertyGoldStandardLocation(prop);
//        eval.setEquivalentPropertiesLocation(equiv);
//        eval.setCorrectedInstancesLocation(new File(new File(equiv).getParent(), "correctedInstances.tsv").getAbsolutePath());
//        eval.setClassHierarchyLocation(new File(new File(equiv).getParent(), "superclasses.tsv").getAbsolutePath());
//        eval.setClassGoldStandardLocation(cls);
//        
//        m.setMatchingParameters(params);
//        m.setEvaluationParameters(eval);
//        
//        Collection<MatchingResult> results = new LinkedList<MatchingResult>();
//
//        Optimizer op = new Optimizer(new GeneticAlgorithm());
//        
//        ParameterRange range = new ParameterRange();
//        Map<String, Object> aliases = new HashMap<>();
//        aliases.put("QuadraticDeviation", new  QuadraticSimilarityDecorator<Double>(new DeviationSimilarity()));
//        aliases.put("QuadraticNormalized", new  QuadraticSimilarityDecorator<Double>(new NormalisedNumericSimilarity()));
//        aliases.put("GeneralizedLevenshtein", new GeneralisedStringJaccard(new LevenshteinSimilarity(), 0.5, 0.5));
//        aliases.put("WeightedDate", new WeightedDatePartSimilarity(0, 0, 1));
//        
//        range.initialize(paramRange, m, aliases);
//        
//        Timer t = Timer.getNamed("Candidate Selection Experiment build #" + getTime(main.class), null);
//        m.setParentTimer(t);
//        
//        Preprocessing.setSurfaceFormPath(surface);
//        
//        CandidateSelectionComponent comp = new CandidateSelectionComponent();
//
//        
//        if(new File(web).isDirectory()) {
//            ArrayList<String> files = new ArrayList<>();
//            
//            for (File f : new File(web).listFiles()) {
//                
//                files.add(f.getAbsolutePath());
//                
//
//                
//                op.optimize(comp, range);
//                
////                range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE, Arrays.asList(new String[] { f.getAbsolutePath() }));
////                Configuration c = op.optimize(m, range);
////                c.writeConfiguration(f.getName() + ".best.config");
////                System.out.println(c.print());
////                results.add(m.getResult());
//            }
//            
////            range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE, Arrays.asList(new String[] { f.getAbsolutePath() }));
//            ArrayList<List<String>> tblRange = new ArrayList<>();
//            tblRange.add(files); // we must wrap the list of tables in another list so the whole list is passed to the run() method
//            range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE, tblRange);
//            Configuration c = op.optimize(m.getCandidateSelectionComponent(), range);
//            //Configuration c = op.optimize(m, range);
//            try {
//                c.writeConfiguration("candidateselection.best.config");
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            System.out.println(c.print());
//            results.add(m.getResult());
//        } else {
//            //Configuration c = m.optimize(op, web, range);
//            //Configuration c = m.optimizeComponents(op, web, range);
//            range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE, Arrays.asList(new String[] { web }));
//            //Configuration c = op.optimize(m, range);
//            Configuration c = op.optimize(m.getCandidateSelectionComponent(), range);
//            try {
//                c.writeConfiguration(new File(web).getName() + ".best.config");
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            System.out.println(c.print());
//            results.add(m.getResult());
//        }
//        t.stop();
//        
//        
//        
//        
//    }
//    
//    private int maxRecall = 0;
//    
//    @Override
//    public List<Parameter> getParameters() {
//        List<Parameter> lst = super.getParameters();
//        lst.add(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE);
//        return lst;
//    }
//    
//    @Override
//    public void run(Configuration config) {
//
//        if(config.getValue(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE) instanceof List) {
//            result = new MatchingResult();
//            
//            List<String> tbls = (List<String>)config.getValue(PAR_WEBTABLE);
//            
//            MatchingResult r = null;
//            
//            for(String t : tbls) {
//                config.getConfig().put(PAR_WEBTABLE, t);
//                
//                r = runStandalone(config);
//                
//                result.getEvaluation().merge(r.getEvaluation());
//            }
//            
//            // put the list back into the config, in case the optimisation algorithm needs to re-use it
//            config.getConfig().put(PAR_WEBTABLE, tbls);
//        } else {
//            result = runStandalone(config);
//        }
//        
//        super.run(config);
//    }
//    
//    protected void runStandalone(Configuration config) {
//        Similarities sim = new Similarities();
//        GoldStandard g = new GoldStandard();
//        MatchingLogger logger = new MatchingLogger();
//        MatchingData data = new MatchingData();
//        
//        String webtable = (String)config.getValue(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE);
//        Timer t = getRootTimer();
//        data.loadWebTable(webtable, t, params);
//        
//        g.initialise(f.getName(), data.getWebtable(), eval);
//        
//        Matchers mat = new Matchers(sim, params, t, g, logger);
//        
//        comp.initialise(mat, data, eval, g, logger, params, sim, t);
//    }
//}
