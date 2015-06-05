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
package de.dwslab.T2K.matching.dbpedia.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;

import de.dwslab.T2K.index.io.DefaultIndex;
import de.dwslab.T2K.matching.dbpedia.components.CandidateSelectionComponent;
import de.dwslab.T2K.matching.dbpedia.components.IterativeComponent;
import de.dwslab.T2K.matching.dbpedia.components.Preprocessing;
import de.dwslab.T2K.matching.dbpedia.components.PropertyBasedClassRefinementComponent;
import de.dwslab.T2K.matching.dbpedia.components.ValueBasedComponent;
import de.dwslab.T2K.matching.dbpedia.components.WebtableToDBpediaMatchingProcess;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingScoreAggregator;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.settings.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingParameters;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.optimization.Optimizer;
import de.dwslab.T2K.matching.optimization.genetic.GeneticAlgorithm;
import de.dwslab.T2K.matching.optimization.simple.AllPossibilities;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.process.ParameterRange;
import de.dwslab.T2K.similarity.functions.QuadraticSimilarityDecorator;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.date.NormalisedDateSimilarity;
import de.dwslab.T2K.similarity.functions.date.WeightedDatePartSimilarity;
import de.dwslab.T2K.similarity.functions.numeric.DeviationSimilarity;
import de.dwslab.T2K.similarity.functions.numeric.NormalisedNumericSimilarity;
import de.dwslab.T2K.similarity.functions.set.LeftSideCoverage;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.functions.string.GeneralisedStringJaccard;
import de.dwslab.T2K.similarity.functions.string.JaccardSimilarity;
import de.dwslab.T2K.similarity.functions.string.LevenshteinSimilarity;
import de.dwslab.T2K.utils.data.ValueAggregator;
import de.dwslab.T2K.utils.timer.Timer;

import java.util.Arrays;

public class mainOptimise {

    public static Long getTime(Class<?> cl) {
        try {
            String rn = cl.getName().replace('.', '/') + ".class";
            JarURLConnection j = (JarURLConnection) ClassLoader.getSystemResource(rn).openConnection();
            return j.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
        } catch (Exception e) {
            return null;
        }
    }
    
    public static void main(String[] args) throws IOException {

        Long build = getTime(main.class);
        if(build!=null) {
            System.out.println(String.format("dbpedia.matching Version 3 Build #%s", DateFormatUtils.format(build, "yyyy-MM-dd HH:mm:ss")));
        }
        
        if (args.length < 9) {

            System.out
                    .println("Usage: <DBpedia directory> <DBpedia index> <use units> <instance goldstandard> <property goldstandard> <propertyRange goldstandard> <equivalent properties> <class goldstandard> <webtable directory> <surface form file> <redirects file> <verbose> <Param ranges> <table type>");

        } else {
            boolean end = false;

            String dbp = args[0];
            String idx = args[1];
            Boolean useUnits = Boolean.parseBoolean(args[2]);
            String inst = args[3];
            String prop = args[4];
            String propRange = args[5];
            String equiv = args[6];
            String cls = args[7];
            String web = args[8];
            String surface = args[9];
            String redirects = args[10];
            boolean verbose = Boolean.parseBoolean(args[11]);
            String paramRange = args[12];
            String tableType = args[13];
            String eqProps = args[14];
                    
            Timer mainTimer = new Timer("Matching");
            
            //WebtableToDBpediaMatcher m = new WebtableToDBpediaMatcher();
            WebtableToDBpediaMatchingProcess m = new WebtableToDBpediaMatchingProcess();
            Timer tdbp = new Timer("Load DBpedia", mainTimer);
            
            MatchingParameters loadDbpParams = new MatchingParameters();
            loadDbpParams.setUseUnitDetection(useUnits);
            m.setMatchingParameters(loadDbpParams);
            
            if (new File(idx).exists()) {
                m.setLuceneIndex(new DefaultIndex(idx));
            }
            
            m.loadDBpedia(dbp);
            tdbp.stop();
            
            m.getData().loadEquivalentProperties(eqProps);

            MatchingParameters params = new MatchingParameters();
            params.setUseUnitDetection(useUnits);
            params.setCollectMatchingInfo(verbose);
            EvaluationParameters eval = new EvaluationParameters();

            eval.setInstanceGoldStandardLocation(inst);
            eval.setPropertyGoldStandardLocation(prop);
            eval.setEquivalentPropertiesLocation(equiv);
            eval.setCorrectedInstancesLocation(new File(new File(equiv).getParent(), "correctedInstances.tsv").getAbsolutePath());
            eval.setClassHierarchyLocation(new File(new File(equiv).getParent(), "superclasses.tsv").getAbsolutePath());
            eval.setClassGoldStandardLocation(cls);
            eval.setPropertyRangesLocation(new File(new File(equiv).getParent(), "propertyRanges.tsv").getAbsolutePath());
            eval.setPropertyRangeGoldstandardLocation(propRange);
            
            m.setMatchingParameters(params);
            m.setEvaluationParameters(eval);
            
            Collection<MatchingResult> results = new LinkedList<MatchingResult>();

            Optimizer op = new Optimizer(new GeneticAlgorithm());
            
            ParameterRange range = new ParameterRange();
            Map<String, Object> aliases = new HashMap<>();
            aliases.put("QuadraticDeviation", new  QuadraticSimilarityDecorator<Double>(new DeviationSimilarity()));
            aliases.put("QuadraticNormalized", new  QuadraticSimilarityDecorator<Double>(new NormalisedNumericSimilarity()));
            aliases.put("GeneralizedLevenshtein", new GeneralisedStringJaccard(new LevenshteinSimilarity(), 0.5, 0.5));
            aliases.put("WeightedDate", new WeightedDatePartSimilarity(0, 0, 1));
            
            range.initialize(paramRange, m, aliases);
//
//            try {
//                //range.initialize(paramRange);
//                Map<Parameter, List> ranges = new HashMap<>();
//                List<Double> thresholds = new ArrayList<>();
//                for(int i = 0; i<10; i++) {
//                    thresholds.add((double)i/10.0);
//                }
//                ranges.put(CandidateSelectionComponent.PAR_INITIAL_THRESHOLD, thresholds);                
//                ranges.put(CandidateSelectionComponent.PAR_INITIAL_K, new ArrayList(Arrays.asList(10,50,100,200,500,1000)));              
//                ranges.put(CandidateSelectionComponent.PAR_INITIAL_SIMILARITY_FUNCTION, new ArrayList(Arrays.asList(new LevenshteinSimilarity(), new WebJaccardSimilarity(), new GeneralisedStringJaccard(new LevenshteinSimilarity(), 0.5, 0.5))));
//                ranges.put(CandidateSelectionComponent.PAR_INTIAL_EDIT_DIST,new ArrayList(Arrays.asList(0,1,2)));
//                ranges.put(CandidateSelectionComponent.PAR_REFINEMENT_EDIT_DIST,new ArrayList(Arrays.asList(0,1,2)));
//                ranges.put(CandidateSelectionComponent.PAR_REFINEMENT_K, new ArrayList(Arrays.asList(10,50,100,200,500,1000)));  
//                ranges.put(CandidateSelectionComponent.PAR_REFINEMENT_SIMILARITY_FUNCTION, new ArrayList(Arrays.asList(new LevenshteinSimilarity(), new WebJaccardSimilarity(), new GeneralisedStringJaccard(new LevenshteinSimilarity(), 0.5, 0.5))));
//
//                ranges.put(CandidateSelectionComponent.PAR_REFINEMENT_THRESHOLD, thresholds);  
//                
//                ranges.put(ValueBasedComponent.PAR_DATE_SIMILARITY, new ArrayList(Arrays.asList(new WeightedDatePartSimilarity(0, 0, 1), new NormalisedDateSimilarity())));
//                ranges.put(ValueBasedComponent.PAR_NUMERIC_SIMILARITY, new ArrayList(Arrays.asList(new  QuadraticSimilarityDecorator<Double>(new DeviationSimilarity()), 
//                        new  QuadraticSimilarityDecorator<Double>(new NormalisedNumericSimilarity()), new NormalisedNumericSimilarity(), new DeviationSimilarity())));
//                ranges.put(ValueBasedComponent.PAR_STRING_SIMILARITY, new ArrayList(Arrays.asList(new LevenshteinSimilarity(), new WebJaccardSimilarity(), new GeneralisedStringJaccard(new LevenshteinSimilarity(), 0.5, 0.5))));
//                
//                List<Double> threshold2 = new ArrayList<>();
//                for(int i = 0; i<10; i++) {
//                    threshold2.add((double)i/100.0);
//                }
//                
//                ranges.put(PropertyBasedClassRefinementComponent.PAR_PROP_CANDIDATE_THRESHOLD, thresholds);  
//                ranges.put(PropertyBasedClassRefinementComponent.PAR_PROP_FINAL_THRESHOLD, threshold2);  
//                ranges.put(PropertyBasedClassRefinementComponent.PAR_PROP_VALUE_THRESHOLD, thresholds);  
//                ranges.put(PropertyBasedClassRefinementComponent.PAR_PROP_NUM_CANDIDATES, new ArrayList(Arrays.asList(1,2,3)));
//                ranges.put(PropertyBasedClassRefinementComponent.PAR_PROP_NUM_RESULTS, new ArrayList(Arrays.asList(1,2,3)));
//                ranges.put(PropertyBasedClassRefinementComponent.PAR_PROP_NUM_VOTES, new ArrayList(Arrays.asList(1,2,3)));
//                
//                ranges.put(IterativeComponent.PAR_INST_FINAL_THRESHOLD, thresholds);
//                
//                range.setRanges(ranges);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return;
//            }            
            Timer t = Timer.getNamed("Matching v3 build #" + getTime(main.class), null);
            m.setParentTimer(t);
            
            //Preprocessing.setSurfaceFormPath(surface);
            //Preprocessing.setRedirectsPath(redirects);
            Preprocessing.loadSurfaceForms(surface);
            Preprocessing.loadRedirects(redirects);
            
            if(new File(web).isDirectory()) {
                ArrayList<String> files = new ArrayList<>();
                
                for (File f : new File(web).listFiles()) {
                    
                    files.add(f.getAbsolutePath());
                    
//                    range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE, Arrays.asList(new String[] { f.getAbsolutePath() }));
//                    Configuration c = op.optimize(m, range);
//                    c.writeConfiguration(f.getName() + ".best.config");
//                    System.out.println(c.print());
//                    results.add(m.getResult());
                }
                
//                range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE, Arrays.asList(new String[] { f.getAbsolutePath() }));
                ArrayList<List<String>> tblRange = new ArrayList<>();
                tblRange.add(files); // we must wrap the list of tables in another list so the whole list is passed to the run() method
                range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE, tblRange);
                List<WebtableToDBpediaMatchingProcess.tableType> tt = new ArrayList<>();
                tt.add(WebtableToDBpediaMatchingProcess.tableType.valueOf(tableType));
                range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_TABLE_TYPE,tt);
                Configuration c = op.optimize(m, range);
                c.writeConfiguration("overall.best.config");
                System.out.println(c.print());
                results.add(m.getResult());
            } else {
                //Configuration c = m.optimize(op, web, range);
                //Configuration c = m.optimizeComponents(op, web, range);
                range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE, Arrays.asList(new String[] { web }));
                Configuration c = op.optimize(m, range);
                c.writeConfiguration(new File(web).getName() + ".best.config");
                System.out.println(c.print());
                results.add(m.getResult());
            }
            t.stop();

            mainTimer.stop();
            System.err.println(mainTimer.toString());
        }
    }
}
