package de.dwslab.T2K.matching.dbpedia;

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
import de.dwslab.T2K.matching.dbpedia.algorithm.CandidateSelectionComponent;
import de.dwslab.T2K.matching.dbpedia.algorithm.IterativeComponent;
import de.dwslab.T2K.matching.dbpedia.algorithm.PropertyBasedClassRefinementComponent;
import de.dwslab.T2K.matching.dbpedia.algorithm.ValueBasedComponent;
import de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.PAR_WEBTABLE;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingScoreAggregator;
import de.dwslab.T2K.matching.dbpedia.model.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
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
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.data.ValueAggregator;
import de.dwslab.T2K.utils.query.Func;
import de.dwslab.T2K.utils.query.Q;
import de.dwslab.T2K.utils.timer.Timer;
import java.io.BufferedWriter;
import java.io.FileWriter;

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
        if (build != null) {
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
            final WebtableToDBpediaMatchingProcess m = new WebtableToDBpediaMatchingProcess();
            Timer tdbp = new Timer("Load DBpedia", mainTimer);

            final MatchingParameters loadDbpParams = new MatchingParameters();
            loadDbpParams.setUseUnitDetection(useUnits);
            m.setMatchingParameters(loadDbpParams);

            if (new File(idx).exists()) {
                m.setLuceneIndex(new DefaultIndex(idx));
            }

            m.loadDBpedia(dbp);
            tdbp.stop();

            m.getData().loadEquivalentProperties(eqProps);

            final MatchingParameters params = new MatchingParameters();
            params.setUseUnitDetection(useUnits);
            params.setCollectMatchingInfo(verbose);
            final EvaluationParameters eval = new EvaluationParameters();

            eval.setInstanceGoldStandardLocation(inst);
            eval.setPropertyGoldStandardLocation(prop);
            eval.setEquivalentPropertiesLocation(equiv);
            eval.setCorrectedInstancesLocation(new File(new File(equiv).getParent(), "correctedInstances.tsv").getAbsolutePath());
            eval.setClassHierarchyLocation(new File(new File(equiv).getParent(), "superclasses.tsv").getAbsolutePath());
            eval.setClassGoldStandardLocation(cls);
            eval.setPropertyRangesLocation(new File(new File(equiv).getParent(), "propertyRanges.tsv").getAbsolutePath());
            eval.setPropertyRangeGoldstandardLocation(propRange);
            eval.loadCanoniser();

            m.setMatchingParameters(params);
            m.setEvaluationParameters(eval);

            Collection<MatchingResult> results = new LinkedList<MatchingResult>();

            final Optimizer op = new Optimizer(new GeneticAlgorithm());

            ParameterRange range = new ParameterRange();
            Map<String, Object> aliases = new HashMap<>();
            aliases.put("QuadraticDeviation", new QuadraticSimilarityDecorator<Double>(new DeviationSimilarity()));
            aliases.put("QuadraticNormalized", new QuadraticSimilarityDecorator<Double>(new NormalisedNumericSimilarity()));
            aliases.put("GeneralizedLevenshtein", new GeneralisedStringJaccard(new LevenshteinSimilarity(), 0.5, 0.5));
            aliases.put("WeightedDate", new WeightedDatePartSimilarity(0, 0, 1));

            range.initialize(paramRange, m, aliases);

            Timer t = Timer.getNamed("Matching v3 build #" + getTime(main.class), null);
            m.setParentTimer(t);

            Preprocessing.loadSurfaceForms(surface);
            Preprocessing.loadRedirects(redirects);

            boolean singleOpti = false;

            if (singleOpti) {
                Collection<File> files = Arrays.asList(new File(web).listFiles());

                try {
                    final ParameterRange copiedRange = range.clone();
                    //final WebtableToDBpediaMatchingProcess copiedM = m.clone();
                    //new Parallel<File>().foreach(Arrays.asList(new File(web).listFiles()), new Consumer<File>() {
                    new Parallel<File>().foreach(files, new Consumer<File>() {
                        @Override
                        public void execute(File parameter) {
                            try {
                                ParameterRange rangeSingle = copiedRange.clone();
                                rangeSingle.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE, Arrays.asList(new String[]{parameter.getAbsolutePath()}));
                                List<WebtableToDBpediaMatchingProcess.tableType> tt = new ArrayList<>();
                                tt.add(WebtableToDBpediaMatchingProcess.tableType.valueOf("jsonWebTable"));
                                rangeSingle.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_TABLE_TYPE, tt);

                                //WebtableToDBpediaMatchingProcess copiedSingle = copiedM.clone();
                                WebtableToDBpediaMatchingProcess p = new WebtableToDBpediaMatchingProcess();
                                p.setMatchingParameters(loadDbpParams);
                                p.setData(m.getData().shallowCopy());
                                p.setMatchingParameters(params);
                                p.setEvaluationParameters(eval);
                                p.setLuceneIndex(m.getKeyIndex().getLuceneIndex());
                                if (m.getUriIndex() != null) {
                                    p.setUriIndex(m.getUriIndex());
                                }
                                p.setParentTimer(new Timer(parameter.getAbsolutePath()));

                                Configuration c = new Optimizer(new GeneticAlgorithm()).optimize(p, rangeSingle);
                                c.writeConfiguration(parameter.getName() + ".best.config");
                                MatchingResult mr = p.getBestResult(c);
                                
                                writeResult(parameter,mr);
                                System.out.println(c.print());
                                //results.add(m.getResult());

                            } catch (Exception e) {
                                System.err.println(String.format("Matching Table %s failed!", parameter.getName()));
                                e.printStackTrace();
                            }
                        }
                    }, mainTimer, "");

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (new File(web).isDirectory()) {
//                ArrayList<String> files = new ArrayList<>();
//
//                for (File f : new File(web).listFiles()) {
//
//                    files.add(f.getAbsolutePath());
//
//                }

//                range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE, Arrays.asList(new String[] { f.getAbsolutePath() }));
                //ArrayList<List<String>> tblRange = new ArrayList<>();
                //tblRange.add(files); // we must wrap the list of tables in another list so the whole list is passed to the run() method
                range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE, Arrays.asList(new String[]{web}));
                List<WebtableToDBpediaMatchingProcess.tableType> tt = new ArrayList<>();
                tt.add(WebtableToDBpediaMatchingProcess.tableType.valueOf(tableType));
                range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_TABLE_TYPE, tt);
                System.out.println("initialized ---" + t.getDuration());
                Configuration c = op.optimize(m, range);
                c.writeConfiguration("overall.best.config");
                System.out.println(c.print());
                results.add(m.getResult());
            } else {
                //Configuration c = m.optimize(op, web, range);
                //Configuration c = m.optimizeComponents(op, web, range);
                range.getRanges().put(WebtableToDBpediaMatchingProcess.PAR_WEBTABLE, Arrays.asList(new String[]{web}));
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

    private static synchronized void writeResult(File parameter,MatchingResult mr) {
        try {
        BufferedWriter writeBestResults = new BufferedWriter(new FileWriter(new File("bestConfigRes.txt"), true));
        if(mr.getEvaluation().getInstanceResult()==null) {
            writeBestResults.write(parameter.getName() + "\tNO\tNO\tNO\tNO\tNO\tNO\t" + mr.getEvaluation().getClassResult().getCorrect() + "\t"
                + mr.getEvaluation().getClassResult().getInputSetSize() + "\t" + mr.getEvaluation().getClassResult().getReferenceSetSize()
                + "\n");
        }
        else {
        writeBestResults.write(parameter.getName() + "\t" + mr.getEvaluation().getInstanceResult().getCorrect() + "\t"
                + mr.getEvaluation().getInstanceResult().getInputSetSize() + "\t" + mr.getEvaluation().getInstanceResult().getReferenceSetSize()
                + "\t" + mr.getEvaluation().getPropertyResult().getCorrect() + "\t"
                + mr.getEvaluation().getPropertyResult().getInputSetSize() + "\t" + mr.getEvaluation().getPropertyResult().getReferenceSetSize()
                + "\t" + mr.getEvaluation().getClassResult().getCorrect() + "\t"
                + mr.getEvaluation().getClassResult().getInputSetSize() + "\t" + mr.getEvaluation().getClassResult().getReferenceSetSize()
                + "\n");
        }
        writeBestResults.flush();
        writeBestResults.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
