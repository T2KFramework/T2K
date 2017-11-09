package de.dwslab.T2K.matching.dbpedia.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;

import au.com.bytecode.opencsv.CSVWriter;
import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.dbpedia.DBpediaIndexer;
import de.dwslab.T2K.index.io.InMemoryIndex;
import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import static de.dwslab.T2K.matching.dbpedia.algorithm.IterativeComponent.PAR_PROP_FINAL_THRESHOLD;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.LinkBasedMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.ContextBasedClassMatcher;
import de.dwslab.T2K.matching.dbpedia.model.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.TableCellCache;
import de.dwslab.T2K.matching.dbpedia.model.TableColumnCache;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.TableRowCache;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.optimization.Optimizer;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.MatchingProcess;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.process.ParameterRange;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.CombineNonOverlapping;
import de.dwslab.T2K.matching.secondline.TopKCandidates;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrixFactory;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrixFactory;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.query.Func;
import de.dwslab.T2K.utils.query.Q;
import de.dwslab.T2K.utils.timer.Timer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class WebtableToDBpediaMatchingProcess extends MatchingProcess {

    /**
     * @return the namedEntity
     */
    public NEComponent getNEComponent() {
        return namedEntity;
    }

    public enum tableType {
        webtable, lodtable, jsonWebTable
    }
    private HashMap<String, HashMap<String, EvaluationResult>> intermediateResults;

    public HashMap<String, HashMap<String, EvaluationResult>> getIntermediateResults() {
        return intermediateResults;
    }
    private long runtime;
    private long tableRuntime;
    private MatchingData data;

    public MatchingData getData() {
        return data;
    }

    public void setData(MatchingData data) {
        this.data = data;
    }
    private Similarities similarities;

    public Similarities getSimilarities() {
        return similarities;
    }

    protected void setSimilarities(Similarities similarities) {
        this.similarities = similarities;
    }
    private KeyIndex keyIndex;

    public KeyIndex getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(KeyIndex keyIndex) {
        this.keyIndex = keyIndex;
    }
    private IIndex uriIndex;

    public IIndex getUriIndex() {
        return uriIndex;
    }

    public void setUriIndex(IIndex index) {
        this.uriIndex = index;
    }
    private Matchers matchers;

    public Matchers getMatchers() {
        return matchers;
    }

    public void setMatchers(Matchers matchers) {
        this.matchers = matchers;
    }
    private EvaluationParameters evaluationParameters;

    public EvaluationParameters getEvaluationParameters() {
        return evaluationParameters;
    }

    public void setEvaluationParameters(
            EvaluationParameters evaluationParameters) {
        this.evaluationParameters = evaluationParameters;
    }
    private MatchingLogger logger;

    public MatchingLogger getLogger() {
        return logger;
    }

    public void setLogger(MatchingLogger logger) {
        this.logger = logger;
    }
    private MatchingParameters matchingParameters;

    public MatchingParameters getMatchingParameters() {
        return matchingParameters;
    }

    public void setMatchingParameters(MatchingParameters matchingParameters) {
        this.matchingParameters = matchingParameters;
    }
    private Timer parentTimer = null;

    public Timer getParentTimer() {
        return parentTimer;
    }

    public void setParentTimer(Timer parentTimer) {
        this.parentTimer = parentTimer;
    }
    private Timer rootTimer = null;

    protected Timer getRootTimer() {
        return rootTimer;
    }

    protected void setRootTimer(Timer rootTimer) {
        this.rootTimer = rootTimer;
    }
    private GoldStandard goldStandard;

    public GoldStandard getGoldStandard() {
        return goldStandard;
    }

    public void setGoldStandard(GoldStandard goldStandard) {
        this.goldStandard = goldStandard;
    }

    protected void initialiseEvaluation(String webtableName) {
        intermediateResults = new HashMap<String, HashMap<String, EvaluationResult>>();

        goldStandard = new GoldStandard();
        goldStandard.initialise(webtableName, getData().getWebtable(), getEvaluationParameters());
    }

    public void loadDBpedia(String dbpediaDirectory) {
        setData(new MatchingData());

        getData().loadDBpedia(dbpediaDirectory, getMatchingParameters());
        prepareLuceneIndex();
    }

    protected void prepareLuceneIndex() {
        if (getKeyIndex() == null || getKeyIndex().getLuceneIndex() == null) {
            keyIndex = new KeyIndex();
            System.out.println("No index provided, creating in-memory index");

            IIndex index = new InMemoryIndex();
            DBpediaIndexer indexer = new DBpediaIndexer();

            for (Table t : getData().getDbpediaTables()) {
                System.out.println("indexing " + t.getHeader());
                indexer.indexInstances(index, t);
            }

            getKeyIndex().setLuceneIndex(index);
        }
    }

    public void setLuceneIndex(IIndex luceneIndex) {
        if (getKeyIndex() == null) {
            keyIndex = new KeyIndex();
        }

        keyIndex.setLuceneIndex(luceneIndex);
    }
    private CandidateSelectionComponent candidateSelection;

    public CandidateSelectionComponent getCandidateSelectionComponent() {
        return candidateSelection;
    }
    private CandidateRefinementComponent candidateRefinement;

    public CandidateRefinementComponent getCandidateRefinementnComponent() {
        return candidateRefinement;
    }
    private CandidateAbstractSelectionComponent candidateAbstractSelection;

    public CandidateAbstractSelectionComponent getCandidateAbstractSelectionComponent() {
        return candidateAbstractSelection;
    }
    private ContextComponent contextComponent;

    public ContextComponent getContextComponent() {
        return contextComponent;
    }
    private ValueBasedComponent valueBased;

    public ValueBasedComponent getValueBasedComponent() {
        return valueBased;
    }
    private PropertyBasedClassRefinementComponent classRefinement;

    public PropertyBasedClassRefinementComponent getClassRefinementComponent() {
        return classRefinement;
    }
    private IterativeComponent iterative;

    public IterativeComponent getIterativeComponent() {
        return iterative;
    }
    private NEComponent namedEntity;

    protected void initialiseComponents() {

        contextComponent = new ContextComponent();
        contextComponent.initialise(matchers, data, evaluationParameters, goldStandard, logger, matchingParameters, similarities, rootTimer);

        candidateSelection = new CandidateSelectionComponent();
        candidateSelection.initialise(getMatchers(), getData(), getEvaluationParameters(), getGoldStandard(), getLogger(), getMatchingParameters(), getSimilarities(), getRootTimer());
        candidateSelection.setKeyIndex(getKeyIndex());
        candidateSelection.setWebTableName(getWebtableName());

        candidateRefinement = new CandidateRefinementComponent();
        candidateRefinement.initialise(matchers, data, evaluationParameters, goldStandard, logger, matchingParameters, similarities, rootTimer);
        candidateRefinement.setKeyIndex(getKeyIndex());
        candidateRefinement.setWebTableName(getWebtableName());

        candidateAbstractSelection = new CandidateAbstractSelectionComponent();
        candidateAbstractSelection.initialise(getMatchers(), getData(), getEvaluationParameters(), getGoldStandard(), getLogger(), getMatchingParameters(), getSimilarities(), getRootTimer());
        candidateAbstractSelection.setWebTableName(getWebtableName());

        valueBased = new ValueBasedComponent();
        valueBased.initialise(getMatchers(), getData(), getEvaluationParameters(), getGoldStandard(), getLogger(), getMatchingParameters(), getSimilarities(), getRootTimer());
        valueBased.setWebTableName(getWebtableName());

        classRefinement = new PropertyBasedClassRefinementComponent();
        classRefinement.initialise(getMatchers(), getData(), getEvaluationParameters(), getGoldStandard(), getLogger(), getMatchingParameters(), getSimilarities(), getRootTimer());
        classRefinement.setWebTableName(getWebtableName());

        namedEntity = new NEComponent();
        namedEntity.initialise(matchers, data, evaluationParameters, goldStandard, logger, matchingParameters, similarities, rootTimer);
        namedEntity.setKeyIndex(getKeyIndex());
        namedEntity.setWebTableName(getWebtable());

        iterative = new IterativeComponent();
        iterative.initialise(getMatchers(), getData(), getEvaluationParameters(), getGoldStandard(), getLogger(), getMatchingParameters(), getSimilarities(), getRootTimer());
        iterative.setWebTableName(getWebtableName());

    }
    public static final Parameter PAR_WEBTABLE = new Parameter("Process.WebTable");
    //public static final Parameter PAR_RUN = new Parameter("Process.Run");
    public static final Parameter PAR_MAPPED_RATIO_FILTER = new Parameter("Process.MappedRatioFilter", 0.5);
    public static final Parameter PAR_EVALUATE = new Parameter("Process.Evaluate", true);
    public static final Parameter PAR_SPANNING_CELL_THRESHOLD = new Parameter("Process.SpanningCellThreshold", 1);
    public static final Parameter PAR_MAX_PARALLEL = new Parameter("Process.MaxParallel", 0);
    public static final Parameter PAR_TABLE_TYPE = new Parameter("Process.TableType", tableType.webtable);
    public static final Parameter PAR_LINK_BASED_INST_THRESHOLD = new Parameter("LinkBasedMatcher.Instance.Sim_thres", 0.0);
    public static final Parameter PAR_MAX_NUM_ROWS = new Parameter("Process.MaxNumberOfRows", 0);
    public static final Parameter PAR_MAX_NUM_COLS = new Parameter("Process.MaxNumberOfCols", 0);
    public static final Parameter PAR_NUM_ITER = new Parameter("Process.NumberOfIterations", 2);
//    public static final Parameter PAR_ABSTRACT_WEIGHT = new Parameter("Process.AbstractWeight", 0.5);
//    public static final Parameter PAR_CONTEXT_WEIGHT = new Parameter("Process.ContextWeight", 0.5);
    public static final Parameter PAR_ABSTRACT_WEIGHT = new Parameter("Process.AbstractWeight", 0.0);
    public static final Parameter PAR_CONTEXT_WEIGHT = new Parameter("Process.ContextWeight", 0.0);
    private String webtable;

    public String getWebtable() {
        return webtable;
    }

    public String getWebtableName() {
        return new File(webtable).getName();
    }

    public MatchingResult matchWebTable(String webtablePath) {

        if (getMatchingParameters().isCollectMatchingInfo()) {
            TableColumnMatchingAdapter a = new TableColumnMatchingAdapter();
            for (TableColumn c : getData().getDbpediaColSet()) {
                System.out.println(String.format("%s: %s - %s", c.getTable().getHeader(), c.getHeader(), a.getLabel(c)));
            }
        }

        Map<Parameter, Object> m = new HashMap<>();
        Configuration c = new Configuration(m, null);
        c.getConfig().put(PAR_WEBTABLE, webtablePath);
        webtable = webtablePath;
        run(c);

        return result;
    }
    private MatchingResult result;

    public MatchingResult getResult() {
        return result;
    }
    private UUID currentRun;

    @Override
    public void run(Configuration config) {

        currentRun = UUID.randomUUID();
        long start = System.currentTimeMillis();

        if (config.getValue(PAR_WEBTABLE) instanceof List) {
            if ((boolean) config.getValue(PAR_EVALUATE)) {
                result = new MatchingResult();
            }

            @SuppressWarnings("unchecked")
            List<String> tbls = (List<String>) config.getValue(PAR_WEBTABLE);

            MatchingResult r = null;

            for (String t : tbls) {
                config.getConfig().put(PAR_WEBTABLE, t);

                r = runSingle(config);

                if ((boolean) config.getValue(PAR_EVALUATE)) {
                    result.getEvaluation().merge(r.getEvaluation());
                }
            }

            // put the list back into the config, in case the optimisation algorithm needs to re-use it
            config.getConfig().put(PAR_WEBTABLE, tbls);
        } else {
            result = runSingle(config);
        }

        runtime = System.currentTimeMillis() - start;
        //return result;
    }
//
//    protected void writeRunResult(Configuration config, MatchingResult result) {
//        try {
//            File f = new File("runs.csv");
//
//            boolean writeHeaders = !f.exists();
//
//            CSVWriter w = new CSVWriter(new BufferedWriter(new FileWriter(f, true)));
//
//            if (writeHeaders) {
//                String[] headers = new String[]{"run", "webtable", "runtime", "instance baseline prec", "instance baseline recall", "instance baseline f1", "max recall", "instance prec", "instance recall", "instance f1", "property prec", "property recall", "property f1", "class precision", "class recall", "class f1"};
//                headers = (String[]) ArrayUtils.addAll(headers, config.getParameterNames());
//                w.writeNext(headers);
//            }
//
//            String[] values = null;
//
//            if (result.getEvaluation().getInstanceBaseLine() == null || result.getEvaluation().getInstanceResult() == null
//                    || result.getEvaluation().getClassResult() == null || result.getEvaluation().getPropertyResult() == null) {
//                values = new String[]{
//                    currentRun.toString(),
//                    getWebtableName(),
//                    Long.toString(tableRuntime)
//                };
//            } else {
//                values = new String[]{
//                    currentRun.toString(),
//                    getWebtableName(),
//                    Long.toString(tableRuntime),
//                    Double.toString(result.getEvaluation().getInstanceBaseLine().getPrecision()),
//                    Double.toString(result.getEvaluation().getInstanceBaseLine().getRecall()),
//                    Double.toString(result.getEvaluation().getInstanceBaseLine().getF1Score()),
//                    Double.toString(result.getEvaluation().getMaxRecall()),
//                    Double.toString(result.getEvaluation().getInstanceResult().getPrecision()),
//                    Double.toString(result.getEvaluation().getInstanceResult().getRecall()),
//                    Double.toString(result.getEvaluation().getInstanceResult().getF1Score()),
//                    Double.toString(result.getEvaluation().getPropertyResult().getPrecision()),
//                    Double.toString(result.getEvaluation().getPropertyResult().getRecall()),
//                    Double.toString(result.getEvaluation().getPropertyResult().getF1Score()),
//                    Double.toString(result.getEvaluation().getClassResult().getPrecision()),
//                    Double.toString(result.getEvaluation().getClassResult().getRecall()),
//                    Double.toString(result.getEvaluation().getClassResult().getF1Score())
//                };
//            }
//
//            values = (String[]) ArrayUtils.addAll(values, config.getValues());
//
//            w.writeNext(values);
//            w.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    protected MatchingResult runSingle(Configuration config) {
        long start = System.currentTimeMillis();
        Timer timer = Timer.getNamed("matchWebTable", getParentTimer());
        setRootTimer(timer);
        MatchingResult result = new MatchingResult();
        result.setUriIndex(uriIndex);

        // initialise logger
        setLogger(new MatchingLogger());
        getLogger().prepareLog();

        // reset candidate similarity in case this is not the first run for this
        // instance
        setSimilarities(new Similarities());

        // load web table
        //String webtablePath = (String)config.getValue(PAR_WEBTABLE);
        //String webtablePath = getWebtable();
        String webtablePath = (String) config.getValue(PAR_WEBTABLE);

        getMatchingParameters().setMaxSpanningCells((Integer) config.getValue(PAR_SPANNING_CELL_THRESHOLD));
        getMatchingParameters().setTableType((tableType) config.getValue(PAR_TABLE_TYPE));

        webtable = webtablePath;
        getData().loadWebTable(webtablePath, getRootTimer(), getMatchingParameters(), (tableType) config.getValue(PAR_TABLE_TYPE));
        String webTableName = new File(webtablePath).getName();
        //System.out.println(webTableName);
        //System.out.println("type: " + config.getValue(PAR_TABLE_TYPE));

        //only for the large corpus where it can happen that we do not have a key!
        if (getData().getWebtable() == null) {
//        if (getData().getWebtable() == null || getData().getWebtable().getKey() == null) {
            getLogger().logData("Cannot load table or no key detected, stopping!");
            // table cannot be loaded
            return result;
        }

        int maxRows = (int) config.getValue(PAR_MAX_NUM_ROWS);
        if (maxRows > 0 && getData().getWebtable().getTotalNumOfRows() > maxRows) {
            getLogger().logData("Skipping table (too many rows)");
            return result;
        }

        int maxCols = (int) config.getValue(PAR_MAX_NUM_COLS);
        if (maxCols > 0 && getData().getWebtable().getColumns().size() > maxCols) {
            getLogger().logData("Skipping table (too many columns)");
            return result;
        }
        
        // load gold standard for web table
        if ((boolean) config.getValue(PAR_EVALUATE)) {
            initialiseEvaluation(webTableName);
        }

        // initialise partial matchers
        setMatchers(new Matchers(getSimilarities(), getMatchingParameters(), getRootTimer(), getGoldStandard(), getLogger()));
        getMatchers().getCandidateSelectionMatcher().setKeyIndex(getKeyIndex());
        getMatchers().getCandidateRefinementMatcher().setKeyIndex(getKeyIndex());
        getMatchers().getNEMatcher().setKeyIndex(getKeyIndex());
        getMatchers().getLinkBasedMatcher().setSimilarityThreshold((double) config.getValue(PAR_LINK_BASED_INST_THRESHOLD));

        // set initial parameters
        getMatchingParameters().setFastJoinDelta(1); // = edit distance threshold
        getMatchingParameters().setFastJoinTau(1); // = jaccard threshold

        initialiseComponents();
        
//        if(!data.getWebtable().isHasKey()) {
//            result.setGoldStandard(getGoldStandard());
//            contextComponent.run(config);
//            SimilarityMatrix<Table> contextMatrix = contextComponent.getClassContextMatrix();
//            classRefinement.mapClasses(contextMatrix, result, getMatchingParameters().isCollectMatchingInfo());
//            return result;
//        }

        getLogger().logData(webTableName + "\n" + getData().getWebtable().printTable());
        System.out.println("KEY: " +getData().getWebtable() + "\t" +getData().getWebtable().getKeyIndex());
        getLogger().logData(String.format("\n key column: [%d] %s", getData().getWebtable().getKeyIndex(), getData().getWebtable().getKey().getHeader()));
        System.out.println(String.format("\n key column: [%d] %s", getData().getWebtable().getKeyIndex(), getData().getWebtable().getKey().getHeader()));

        

        //only to get candodate abstract results!
//        candidateAbstractSelection.run(config);
//            result.setWebtable(getData().getWebtable());
//            result.setMatchingData(getData());
//            getSimilarities().setInitialCandidateSimilarity(candidateAbstractSelection.getCandidateAbstractSimilarity());
//            classRefinement.mapInstances(getSimilarities().getInitialCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), result, "");
//            
//            result.setGoldStandard(getGoldStandard());
//                result.setSimilarities(getSimilarities());
//
//                result.getEvaluation().setNumCandidates(getSimilarities().getInitialCandidateSimilarity().getNumberOfNonZeroElements());
//
//                //analyseResult(result);
//                if (result.getEvaluation().getInstanceResult() == null) {
//                    //writeRunResult(config, result);
//                    //return result;
//                } else {
//                    System.out.println(webTableName);
////                if(result.getEvaluation().getCorrectKey()==1) {
////                    System.out.println("Correct key detected.");
////                }
////                else {
////                    System.out.println("Wrong key detected.");
////                }
//                    System.out.println(String.format("Total number of mappings: %d", result
//                            .getEvaluation().getInstanceResult().getInputSetSize()));
//                    
//                    EvaluationResult r = result.getEvaluation().getInstanceResult();
//                    System.out.println(String.format(
//                            "result:  \t     %.2f\t  %.2f\t  %.2f\t%d/%d/%d",
//                            r.getPrecision(), r.getRecall(), r.getF1Score(),
//                            r.getCorrect(), r.getInputSetSize(), r.getReferenceSetSize()));
//                    return result;
//                } 
//        }
//   
//        for(Table t : contextMatrix.getFirstDimension()) {
//            for(Table s : contextMatrix.getSecondDimension()) {
//                System.out.println("context: " + t + " vs. " +s);
//            }
//        }        
        // start actual matching process
        candidateSelection.run(config);

        if ((double) config.getValue(PAR_CONTEXT_WEIGHT) == 0.0) {
            getSimilarities().setClassSimilarity(candidateSelection.getClassSimilarity());
        } else {
            contextComponent.run(config);
            SimilarityMatrix<Table> contextMatrix = contextComponent.getClassContextMatrix();

            if (contextMatrix != null) {
                CombineNonOverlapping nonOverlapClass = new CombineNonOverlapping();
                nonOverlapClass.setAggregationType(CombinationType.WeightedSum);
                nonOverlapClass.setFirstWeight((double) config.getValue(PAR_CONTEXT_WEIGHT));
                nonOverlapClass.setSecondWeight(1.0 - (double) config.getValue(PAR_CONTEXT_WEIGHT));
                SimilarityMatrix allCandidateClasses = nonOverlapClass.match(contextMatrix, candidateSelection.getClassSimilarity());
                TopKCandidates topK = new TopKCandidates();
                getSimilarities().setClassSimilarity(topK.match(allCandidateClasses, 2));
            } else {
                getSimilarities().setClassSimilarity(candidateSelection.getClassSimilarity());
            }
        }

        try {
            BufferedWriter writeCandidate = new BufferedWriter(new FileWriter("candidates.csv", true));
            SimilarityMatrix<TableRow> simCand = candidateSelection.getInitialCandidateSimilarity();
            double average = 0.0;
            for (TableRow r1 : simCand.getFirstDimension()) {
                average += simCand.getMatches(r1).size();
            }
            writeCandidate.write(webtable + "\t" + average + "\n");
            writeCandidate.flush();
            writeCandidate.close();
        } catch (Exception e) {
        }

        candidateRefinement.setInitialCandidateSimilarity(candidateSelection.getInitialCandidateSimilarity());
        candidateRefinement.setClassSimilarity(getSimilarities().getClassSimilarity());
        candidateRefinement.run(config);
//            
        if (candidateRefinement.getCandidateSimilarity().getNumberOfNonZeroElements() == 0) {
            // stop here as we have no candidates
            getLogger().logData("No candidates, stopping!");
        } else {

            //getSimilarities().setClassSimilarity(candidateSelection.getClassSimilarity());
            getSimilarities().setInitialCandidateSimilarity(candidateRefinement.getCandidateSimilarity());


            try {
                BufferedWriter writeCandidate = new BufferedWriter(new FileWriter("candidatesRef.csv", true));
                SimilarityMatrix<TableRow> simCand = candidateSelection.getInitialCandidateSimilarity();
                double average = 0.0;
                for (TableRow r1 : simCand.getFirstDimension()) {
                    average += simCand.getMatches(r1).size();
                }
                writeCandidate.write(webtable + "\t" + average + "\n");
                writeCandidate.flush();
                writeCandidate.close();
            } catch (Exception e) {
            }

            if ((double) config.getValue(PAR_ABSTRACT_WEIGHT) > 0.0) {
                candidateAbstractSelection.run(config);
//
                CombineNonOverlapping nonOverlap = new CombineNonOverlapping();
                nonOverlap.setAggregationType(CombinationType.WeightedSum);
                nonOverlap.setFirstWeight((double) config.getValue(PAR_ABSTRACT_WEIGHT));
                nonOverlap.setSecondWeight(1.0 - (double) config.getValue(PAR_ABSTRACT_WEIGHT));
                SimilarityMatrix allCandidates = nonOverlap.match(candidateAbstractSelection.getCandidateAbstractSimilarity(), candidateRefinement.getCandidateSimilarity());
                getSimilarities().setInitialCandidateSimilarity(allCandidates);
            }
////            System.out.println("all candidates: ");
//
//            for (Object instance : allCandidates.getFirstDimension()) {
//                for (Object candidate : allCandidates.getMatches(instance)) {
//                    System.out.println("inst :" + instance + " cand: " + candidate + " sim: " + allCandidates.get(instance, candidate));
//                }
//            }

            //          getSimilarities().setInitialCandidateSimilarity(candidateRefinement.getCandidateSimilarity());

            if ((boolean) config.getValue(PAR_EVALUATE)) {
                addIntermediaResult("instances", "[01] baseline", candidateSelection.getInitialInstanceResult());
                addIntermediaResult("instances", "[02] pruned", candidateSelection.getPrunedInstanceResult());
                addIntermediaResult("instances", "[03] refined", candidateRefinement.getRefinedInstanceResult());
                addIntermediaResult("classes", "[01] baseline", candidateSelection.getInitialClassResult());
            }

            valueBased.run(config);
            getSimilarities().setValueSimilarity(valueBased.getValueSimilarity());
            getSimilarities().setLabelSimilarity(valueBased.getLabelSimilarity());

            classRefinement.run(config);
            getSimilarities().setClassSimilarity(classRefinement.getClassSimilarity());
            getSimilarities().setFinalClass(classRefinement.getFinalClass());
            getSimilarities().setCandidateSimilarity(classRefinement.getCandidateSimilarity());
            getSimilarities().setPropertySimilarity(classRefinement.getPropertySimilarity());
//  
            try {
                BufferedWriter writeProp = new BufferedWriter(new FileWriter("props.csv", true));
                double average1 = 0.0;
                for (TableColumn tc : classRefinement.getPropertySimilarity().getFirstDimension()) {
                    average1 += classRefinement.getPropertySimilarity().getMatches(tc).size();
                }
                writeProp.write(webtable + "\t" + average1 + "\n");
                writeProp.flush();
                writeProp.close();
            } catch (Exception e) {
            }


//            namedEntity.setPropertySimilarity(getSimilarities().getPropertySimilarity());
//            namedEntity.run(config);
//            SimilarityMatrix<TableColumn> weighted = namedEntity.getWeightedPropertySimilarity();
////            for(TableColumn c1 : weighted.getFirstDimension()) {
////                for(TableColumn c2 : weighted.getMatches(c1)) {
////                    System.out.println("class: " +classRefinement.getFinalClass() +"c1:  " + c1.getHeader() + " --- " + c2.getURI() + " ::: " + weighted.get(c1, c2));
////                }
////            }
//
//            //for now
//            getSimilarities().setPropertySimilarity(weighted);

            if ((boolean) config.getValue(PAR_EVALUATE)) {
                addIntermediaResult("properties", "[01]", classRefinement.getPropertyResult());
                addIntermediaResult("classes", "[02] refined", classRefinement.getClassResult());
                addIntermediaResult("instances", "[04] 2nd refinement", classRefinement.getInstanceResult());
            }
            if (classRefinement.getNumberOfPropMappings() != 0) {
                int iterationNum = (int) config.getValue(PAR_NUM_ITER);
                for (int i = 0; i < iterationNum; i++) {
                    iterative.run(config);
                    getSimilarities().setPropertySimilarity(iterative.getPropertySimilarity());
                    getSimilarities().setCandidateSimilarity(iterative.getInstanceSimilarity());
                    if ((boolean) config.getValue(PAR_EVALUATE)) {
                        addIntermediaResult("instances", String.format("[%02d]", iterationNum + 4), iterative.getInstanceResult());
                        addIntermediaResult("properties", String.format("[%02d]", iterationNum + 1), iterative.getPropertyResult());
                    }
                    //iterationNum++;
                }
            }

            SimilarityMatrix sim = getSimilarities().getCandidateSimilarity().copy();

            if (getMatchingParameters().getTableType() == tableType.lodtable) {
                //exploit sameAs links
                LinkBasedMatcher lm = matchers.getLinkBasedMatcher();
                getSimilarities().setCandidateSimilarity(lm.match(data));

                //if not values can be compared, use the column labels as indication
                SimilarityMatrix<TableColumn> combinedValues = new SparseSimilarityMatrixFactory().createSimilarityMatrix(getSimilarities().getLabelSimilarity().getFirstDimension().size(), getSimilarities().getLabelSimilarity().getSecondDimension().size());

                for (TableColumn c : getSimilarities().getLabelSimilarity().getFirstDimension()) {
                    for (TableColumn d : getSimilarities().getLabelSimilarity().getSecondDimension()) {
                        combinedValues.set(c, d, 0.0);
                    }
                }
                double wLbl = 0.3;

                for (TableColumn c : getSimilarities().getLabelSimilarity().getFirstDimension()) {
                    for (TableColumn d : getSimilarities().getLabelSimilarity().getSecondDimension()) {
                        if (c.getDataType() == d.getDataType() && getSimilarities().getPropertySimilarity().get(c, d) == null && getSimilarities().getLabelSimilarity().get(c, d) > 0.95) {
                            combinedValues.set(c, d, getSimilarities().getLabelSimilarity().get(c, d) * wLbl);
                        } else {
                            combinedValues.set(c, d, getSimilarities().getPropertySimilarity().get(c, d));
                        }
                    }
                }
                System.out.println("combined: " + combinedValues.getOutput());

                getSimilarities().setPropertySimilarity(combinedValues);
            }

            //TODO: do or don't???
            //getSimilarities().getPropertySimilarity().prune(0.01);
            //only continue if at least x% of all instances can be mapped to the final class
            double mappedInstances;
            if (iterative.getResult() != null) {
                mappedInstances = (double) iterative.getResult().getInstanceMappings().size() / (double) getData().getWebtableRowSet().size();
            } else {
                mappedInstances = (double) classRefinement.getNumberOfInstanceMappings() / (double) getData().getWebtableRowSet().size();
            }

            if (mappedInstances < (Double) config.getValue(PAR_MAPPED_RATIO_FILTER)) {
                getLogger().logData("Not enough consistent candidates, cancelling!");
                // not enough mappings with the selected class, we do not map this table at all
                getSimilarities().setClassSimilarity(new SparseSimilarityMatrix<Table>(0, 0));
                getSimilarities().setPropertySimilarity(new SparseSimilarityMatrix<TableColumn>(0, 0));
                getSimilarities().setCandidateSimilarity(new SparseSimilarityMatrix<TableRow>(0, 0));
            }

            System.out.println("*************************************************");
            System.out.println("Finished iterations");
            System.out.println("*************************************************");

            result.setWebtable(getData().getWebtable());
            result.setMatchingData(getData());

            classRefinement.mapInstances(sim, getMatchingParameters().isCollectMatchingInfo(), result, "");
            classRefinement.mapClasses(getSimilarities().getClassSimilarity(), result, getMatchingParameters().isCollectMatchingInfo());
            classRefinement.mapProperties(getSimilarities().getPropertySimilarity(), webTableName, getMatchingParameters().isCollectMatchingInfo(), result);

            if ((Boolean) config.getValue(WebtableToDBpediaMatchingProcess.PAR_EVALUATE)) {
                result.setGoldStandard(getGoldStandard());
                result.setSimilarities(getSimilarities());

                result.getEvaluation().setInstanceBaseLine(candidateSelection.getInitialInstanceResult());
                result.getEvaluation().setMaxCorrectCandidates(candidateSelection.getMaxCorrectCanddiates());
                result.getEvaluation().setNumCandidates(getSimilarities().getInitialCandidateSimilarity().getNumberOfNonZeroElements());

                analyseResult(result);
                if (result.getEvaluation().getInstanceResult() == null || result.getEvaluation().getInstanceMax() == null) {
                    //writeRunResult(config, result);
                    //return result;
                } else {
                    System.out.println(webTableName);
//                if(result.getEvaluation().getCorrectKey()==1) {
//                    System.out.println("Correct key detected.");
//                }
//                else {
//                    System.out.println("Wrong key detected.");
//                }
                    System.out.println(String.format("Total number of mappings: %d", result
                            .getEvaluation().getInstanceResult().getInputSetSize()));
                    System.out
                            .println("        \tPrecision\tRecall\tF1  \t#correct/#mappings/#reference\n");
                    EvaluationResult r = result.getEvaluation().getInstanceBaseLine();
                    System.out.println(String.format(
                            "baseline:\t     %.2f\t  %.2f\t  %.2f\t%d/%d/%d",
                            r.getPrecision(), r.getRecall(), r.getF1Score(),
                            r.getCorrect(), r.getInputSetSize(), r.getReferenceSetSize()));
                    r = result.getEvaluation().getInstanceResult();
                    System.out.println(String.format(
                            "result:  \t     %.2f\t  %.2f\t  %.2f\t%d/%d/%d",
                            r.getPrecision(), r.getRecall(), r.getF1Score(),
                            r.getCorrect(), r.getInputSetSize(), r.getReferenceSetSize()));
                    r = result.getEvaluation().getInstanceMax();
                    System.out.println(String.format(
                            "max:     \t     %.2f\t  %.2f\t  %.2f\t%d/%d/%d",
                            r.getPrecision(), r.getRecall(), r.getF1Score(),
                            r.getCorrect(), r.getInputSetSize(), r.getReferenceSetSize()));

                }
                //no time logging for now!
//                Timer tLog = Timer.getNamed("Write log", timer);
//                getLogger().writeLog(getData().getWebtable());
//                getLogger().logData(timer.toString());
//                tLog.stop();
            }
            if (result != null) {
                Timer tResult = Timer.getNamed("Write Mappings", timer);
                result.write((tableType) config.getValue(PAR_TABLE_TYPE));
                tResult.stop();
            }
        }
        tableRuntime = System.currentTimeMillis() - start;
//        writeRunResult(config, result);
        Timer tCleanup = Timer.getNamed("Clear cache", timer);

        TableCellCache.get()
                .removeTable(getData().getWebtable());
        TableRowCache.get()
                .removeTable(getData().getWebtable());
        TableColumnCache.get().removeTable(getData().getWebtable());
        getSimilarities()
                .clean();
        tCleanup.stop();

        timer.stop();
        return result;
    }

    protected void addIntermediaResult(String key, String iteration, EvaluationResult value) {
        if (getIntermediateResults() == null) {
            return;
        }
        HashMap<String, EvaluationResult> m = getIntermediateResults().get(key);

        if (m == null) {
            m = new HashMap<String, EvaluationResult>();
            getIntermediateResults().put(key, m);
        }

        m.put(iteration, value);
    }
    public static final int REASON_NOT_IN_DATA = 1;
    public static final int REASON_NOT_IN_CANDIDATES = 2;

    /**
     * determine the reasons for misclassifications
     *
     * @param result
     */
    protected void analyseResult(MatchingResult result) {

        Map<Correspondence<TableRow>, Integer> reasons = new HashMap<Correspondence<TableRow>, Integer>();
        Map<Integer, Integer> reasonsFN = new HashMap<>();

        int cntNotInData = 0;

        for (Correspondence<TableRow> cor : result.getInstanceMappings()) {

            if (!cor.isCorrect()) {

                Object correctKey = cor.getCorrectValue();

                if (correctKey != null) {
                    if (!isInData(correctKey)) {
                        reasons.put(cor, REASON_NOT_IN_DATA);
                        cntNotInData++;
                    } else if (!isInCandidateList(correctKey)) {
                        reasons.put(cor, REASON_NOT_IN_CANDIDATES);
                    }
                }

            }

        }
        for (Map.Entry<Object, Object> e : getGoldStandard().getInstanceGoldStandard().entrySet()) {
            SimilarityMatrix<TableRow> sim = similarities.getInitialCandidateSimilarity();
            for (TableRow tr : getData().getWebtableRowSet()) {
                boolean found = false;
                if (tr.getRowIndexInFile() == Integer.parseInt(e.getKey().toString())) {
                    for (TableRow candidates : sim.getMatches(tr)) {
                        if (e.getValue().equals(candidates.getURI())) {
                            found = true;
                        }
                    }
                }
                if (found) {
                    reasonsFN.put(tr.getRowIndexInFile(), REASON_NOT_IN_DATA);
                } else {
                    reasonsFN.put(tr.getRowIndexInFile(), REASON_NOT_IN_CANDIDATES);
                }
            }

        }

        getData().getWebtable().getKey();

        EvaluationResult inst = result.getEvaluation().getInstanceResult();
        //EvaluationResult max = new EvaluationResult(inst.getTruePositives()+cntNotInData, 0, inst.getFalseNegatives(), inst.getTotal());
        //EvaluationResult max = new EvaluationResult(inst.getCorrect()+cntNotInData, inst.getCorrect()+cntNotInData, inst.getReferenceSetSize());
        if (getData().getWebtable().getKey() == null || getData().getWebtable().getKey().getValues() == null || inst == null) {
            result.getEvaluation().setInstanceMax(null);
            result.setInstanceFNErrors(reasonsFN);
            result.setInstanceErrors(reasons);
            return;
        }
        int maxCountOfCorrect = Math.min(getData().getWebtable().getKey().getValues().size(), inst.getReferenceSetSize()) - cntNotInData;
        EvaluationResult max = new EvaluationResult(maxCountOfCorrect, maxCountOfCorrect, inst.getReferenceSetSize() - cntNotInData, getData().getWebtable().getKey().getValues().size() - cntNotInData);

        result.getEvaluation().setInstanceMax(max);
        result.setInstanceErrors(reasons);
        result.setInstanceFNErrors(reasonsFN);
    }

    protected boolean isInCandidateList(Object key) {

        for (TableRow r : getSimilarities().getInitialCandidateSimilarity().getSecondDimension()) {
            if (r.getURI().equals(key)) {
                return true;
            }
        }

        return false;
    }

    protected boolean isInData(Object key) {
        if (key != null
                && getKeyIndex().getLuceneBlocking().getCandidateMap().containsKey(key.toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public double evaluate(Configuration config) {

        Timer mainTimer = new Timer("Matching");
        String version = "";

        //super.evaluate(config);
        String path = config.getValue(PAR_WEBTABLE).toString();

        if (new File(path).isDirectory()) {
            HashSet<String> matchedTables = new HashSet<>();
            final Collection<MatchingResult> results = new LinkedList<MatchingResult>();

            Collection<File> files = Arrays.asList(new File(path).listFiles());
            files = Q.without(files, matchedTables, new Func<String, File>() {
                @Override
                public String invoke(File in) {
                    return in.getName();
                }
            });

            // running several matching processes in parallel can only work with individual instances, as intermediate results are stored as class member variables ...
            final Configuration configFinal = config;
            try {

                //new Parallel<File>().foreach(Arrays.asList(new File(web).listFiles()), new Consumer<File>() {
                new Parallel<File>().foreach(files, new Consumer<File>() {
                    @Override
                    public void execute(File parameter) {
                        try {

                            Configuration c = configFinal.clone();
                            c.getConfig().put(PAR_WEBTABLE, parameter.getAbsolutePath());
                            run(c);
                            MatchingResult r = getResult();
                            if (r != null && (Boolean) c.getValue(WebtableToDBpediaMatchingProcess.PAR_EVALUATE)) {
                                if (r.getEvaluation().getInstanceBaseLine() != null) {
                                    synchronized (results) {
                                        results.add(r);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println(String.format("Matching Table %s failed!", parameter.getName()));
                            e.printStackTrace();
                        }
                    }
                }, mainTimer, version);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            double res = 0.0;
            for (MatchingResult r : results) {
                res += (r.getEvaluation().getInstanceResult().getF1Score()
                        + r.getEvaluation().getPropertyResult().getF1Score()) / 2.0;
            }
            res = res / results.size();
            printEvaluation(config, res, "");
            return res;
        } else {
            //           Configuration c = config.clone();
//            c.getConfig().put(PAR_WEBTABLE, path);
//            webtable = path;
            //          initialiseComponents();
            run(config);
            MatchingResult r = getResult();
            double res = 0.0;
            if (r != null) {
                try {
                    if(r.getEvaluation().getClassResult() ==null && r.getEvaluation().getPropertyResult()==null) {
                        res = 0.0;
                    }
                    //only for the large GS!
                    else if(r.getEvaluation().getPropertyResult()==null) {
                        res = r.getEvaluation().getClassResult().getF1Score();
                    }
                    else {
                    res = (r.getEvaluation().getPropertyResult().getF1Score()
                            + r.getEvaluation().getClassResult().getF1Score()) / 2.0;
                    }
//                    res = (r.getEvaluation().getInstanceResult().getF1Score()
//                            + r.getEvaluation().getPropertyResult().getF1Score()
//                            + r.getEvaluation().getClassResult().getF1Score()) / 3.0;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("class result: " + r.getEvaluation().getClassResult() + " inst result: " + r.getEvaluation().getInstanceResult() + " prop: " + r.getEvaluation().getPropertyResult());
                    for (Parameter p : config.getConfig().keySet()) {
                        System.out.println(p.getName() + " DDD " + config.getConfig().get(p));
                    }
                }
                String fileName = new File(path).getName();
                printEvaluation(config, res, fileName);
            }
            return res;
        }
    }

    public MatchingResult getBestResult(Configuration c) {
        run(c);
        return getResult();
    }

    protected void printEvaluation(Configuration config, double result, String fileName) {
        System.out.println("_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_");
        System.out.println(String.format("Evaluation of current configuration: %.4f", result));
        System.out.println(config.print());
        System.out.println("_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_");

        try {
            File f = new File("opti/WebtableToDBpediaMatchingProcess" + fileName + ".csv");

            boolean writeHeaders = !f.exists();

            CSVWriter w = new CSVWriter(new BufferedWriter(new FileWriter(f, true)));

            if (writeHeaders) {
                String[] headers = new String[]{"run", "instance baseline prec", "instance baseline recall", "instance baseline f1", "max recall", "instance prec", "instance recall", "instance f1", "property prec", "property recall", "property f1", "class precision", "class recall", "class f1", "evaluation result", "runtime"};
                headers = (String[]) ArrayUtils.addAll(headers, config.getParameterNames());
                w.writeNext(headers);
            }

            String[] values;
            if(getResult().getEvaluation().getClassResult() == null) {
                values = new String[]{"NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA"};
            }
            else if(getResult().getEvaluation().getPropertyResult()==null) {
                values = new String[]{
                currentRun.toString(),
                    Double.toString(0.0),
                    Double.toString(0.0),
                    Double.toString(0.0),
                    Double.toString(0.0),
                    Double.toString(0.0),
                    Double.toString(0.0),
                    Double.toString(0.0),
                    Double.toString(0.0),
                    Double.toString(0.0),
                    Double.toString(0.0),
                    Double.toString(getResult().getEvaluation().getClassResult().getCorrect()),
                    Double.toString(getResult().getEvaluation().getClassResult().getInputSetSize()),
                    Double.toString(getResult().getEvaluation().getClassResult().getReferenceSetSize()),
                    Double.toString(result),
                    Long.toString(runtime),
            };
            } else {
                values = new String[]{
                currentRun.toString(),
                    Double.toString(getResult().getEvaluation().getInstanceBaseLine().getPrecision()),
                    Double.toString(getResult().getEvaluation().getInstanceBaseLine().getRecall()),
                    Double.toString(getResult().getEvaluation().getInstanceBaseLine().getF1Score()),
                    Double.toString(getResult().getEvaluation().getMaxRecall()),
                    Double.toString(getResult().getEvaluation().getInstanceResult().getCorrect()),
                    Double.toString(getResult().getEvaluation().getInstanceResult().getInputSetSize()),
                    Double.toString(getResult().getEvaluation().getInstanceResult().getReferenceSetSize()),
                    Double.toString(getResult().getEvaluation().getPropertyResult().getCorrect()),
                    Double.toString(getResult().getEvaluation().getPropertyResult().getInputSetSize()),
                    Double.toString(getResult().getEvaluation().getPropertyResult().getReferenceSetSize()),
                    Double.toString(getResult().getEvaluation().getClassResult().getCorrect()),
                    Double.toString(getResult().getEvaluation().getClassResult().getInputSetSize()),
                    Double.toString(getResult().getEvaluation().getClassResult().getReferenceSetSize()),
                    Double.toString(result),
                    Long.toString(runtime)
                };
            }
            
            values = (String[]) ArrayUtils.addAll(values, config.getValues());

            w.writeNext(values);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WebtableToDBpediaMatchingProcess clone() {
        WebtableToDBpediaMatchingProcess wp = new WebtableToDBpediaMatchingProcess();
        wp.setUriIndex(this.uriIndex);
        wp.setSimilarities(this.similarities);
        wp.setData(data);
        wp.setMatchers(matchers);
        wp.setGoldStandard(goldStandard);
        wp.setEvaluationParameters(evaluationParameters);
        wp.setLogger(logger);
        wp.setMatchingParameters(matchingParameters);
        wp.setParentTimer(parentTimer);
        wp.setRootTimer(rootTimer);
        wp.setKeyIndex(keyIndex);
        return wp;
    }

//    public Configuration optimizeComponents(Optimizer op, String webtable, ParameterRange range) {
//
//        Timer timer = Timer.getNamed("matchWebTable", getParentTimer());
//        setRootTimer(timer);
//
//        // initialise logger
//        setLogger(new MatchingLogger());
//        getLogger().prepareLog();
//
//        // reset candidate similarity in case this is not the first run for this
//        // instance
//        setSimilarities(new Similarities());
//
//        // load web table
//        String webtablePath = getWebtable();
//        getData().loadWebTable(webtablePath, getRootTimer(), getMatchingParameters());
//        String webTableName = new File(webtablePath).getName();
//        System.out.println(webTableName);
//        getLogger().logData(webTableName + "\n" + getData().getWebtable().printTable());
//        getLogger().logData("\n key column: " + getData().getWebtable().getKeyIndex());
//
//        // load gold standard for web table
//        initialiseEvaluation(webTableName);
//
//        // initialise partial matchers
//        setMatchers(new Matchers(getSimilarities(), getMatchingParameters(), getRootTimer(), getGoldStandard(), getLogger()));
//        getMatchers().getCandidateSelectionMatcher().setKeyIndex(getKeyIndex());
//        getMatchers().getCandidateRefinementMatcher().setKeyIndex(getKeyIndex());
//
//        // set initial parameters
//        getMatchingParameters().setFastJoinDelta(1); // = edit distance threshold
//        getMatchingParameters().setFastJoinTau(1); // = jaccard threshold
//
//        initialiseComponents();
//
//        // start actual matching process
//        Configuration bestCandidateSelection = op.optimize(candidateSelection, range);
//        candidateSelection.run(bestCandidateSelection);
//        getSimilarities().setInitialCandidateSimilarity(candidateSelection.getCandidateSimilarity());
//        getSimilarities().setClassSimilarity(candidateSelection.getClassSimilarity());
//        addIntermediaResult("instances", "[01] baseline", candidateSelection.getInitialInstanceResult());
//        addIntermediaResult("instances", "[02] pruned", candidateSelection.getPrunedInstanceResult());
//        addIntermediaResult("instances", "[03] refined", candidateSelection.getRefinedInstanceResult());
//        addIntermediaResult("classes", "[01] baseline", candidateSelection.getInitialClassResult());
//
//        Configuration bestValueBased = op.optimize(valueBased, range);
//        valueBased.run(bestValueBased);
//        getSimilarities().setValueSimilarity(valueBased.getValueSimilarity());
//        getSimilarities().setLabelSimilarity(valueBased.getLabelSimilarity());
//
//        Configuration bestClassRefinement = op.optimize(classRefinement, range);
//        classRefinement.run(bestClassRefinement);
//        getSimilarities().setClassSimilarity(classRefinement.getClassSimilarity());
//        getSimilarities().setFinalClass(classRefinement.getFinalClass());
//        getSimilarities().setCandidateSimilarity(classRefinement.getCandidateSimilarity());
//        addIntermediaResult("properties", "[01]", classRefinement.getPropertyResult());
//        addIntermediaResult("classes", "[02] refined", classRefinement.getClassResult());
//        addIntermediaResult("instances", "[04] 2nd refinement", classRefinement.getInstanceResult());
//
//        int iterationNum = 1;
//
//        Configuration bestIterative = op.optimize(iterative, range);
//        iterative.run(bestIterative);
//        getSimilarities().setPropertySimilarity(iterative.getPropertySimilarity());
//        getSimilarities().setCandidateSimilarity(iterative.getInstanceSimilarity());
//
//        addIntermediaResult("instances", String.format("[%02d]", iterationNum + 4), iterative.getInstanceResult());
//        addIntermediaResult("properties", String.format("[%02d]", iterationNum + 1), iterative.getPropertyResult());
//
//        System.out.println("*************************************************");
//        System.out.println("Finished iterations");
//        System.out.println("*************************************************");
//
//        result = new MatchingResult();
//        result.setGoldStandard(getGoldStandard());
//        result.setWebtable(getData().getWebtable());
//        result.setMatchingData(getData());
//
//        iterative.mapClasses(getSimilarities().getClassSimilarity(), result, true);
//        iterative.mapProperties(getSimilarities().getPropertySimilarity(), webTableName, true, result);
//        iterative.mapInstances(getSimilarities().getCandidateSimilarity(), true, result, "");
//
//        result.getEvaluation().setInstanceBaseLine(candidateSelection.getInitialInstanceResult());
//        result.getEvaluation().setNumCandidates(getSimilarities().getInitialCandidateSimilarity()
//                .getNumberOfNonZeroElements());
//
//        analyseResult(result);
//
//        System.out.println(webTableName);
//        System.out.println(String.format("Total number of mappings: %d", result
//                .getEvaluation().getInstanceResult().getInputSetSize()));
//        System.out
//                .println("        \tPrecision\tRecall\tF1  \t#correct/#mappings/#reference\n");
//        EvaluationResult r = result.getEvaluation().getInstanceBaseLine();
//        System.out.println(String.format(
//                "baseline:\t     %.2f\t  %.2f\t  %.2f\t%d/%d/%d",
//                r.getPrecision(), r.getRecall(), r.getF1Score(),
//                r.getCorrect(), r.getInputSetSize(), r.getReferenceSetSize()));
//        r = result.getEvaluation().getInstanceResult();
//        System.out.println(String.format(
//                "result:  \t     %.2f\t  %.2f\t  %.2f\t%d/%d/%d",
//                r.getPrecision(), r.getRecall(), r.getF1Score(),
//                r.getCorrect(), r.getInputSetSize(), r.getReferenceSetSize()));
//        r = result.getEvaluation().getInstanceMax();
//        System.out.println(String.format(
//                "max:     \t     %.2f\t  %.2f\t  %.2f\t%d/%d/%d",
//                r.getPrecision(), r.getRecall(), r.getF1Score(),
//                r.getCorrect(), r.getInputSetSize(), r.getReferenceSetSize()));
//
//        timer.stop();
//        getLogger().writeLog(getData().getWebtable());
//
//        return bestCandidateSelection.mergeWith(bestValueBased).mergeWith(bestClassRefinement).mergeWith(bestIterative);
//    }
    public WebtableToDBpediaMatchingProcess() {
        List<Parameter> params = CandidateSelectionComponent.getParams();
        params.addAll(ContextComponent.getParams());
        params.addAll(NEComponent.getParams());
        params.addAll(ValueBasedComponent.getParams());
        params.addAll(CandidateRefinementComponent.getParams());
        params.addAll(PropertyBasedClassRefinementComponent.getParams());
        params.addAll(IterativeComponent.getParams());
        params.add(PAR_WEBTABLE);
        params.add(PAR_MAPPED_RATIO_FILTER);
        params.add(PAR_EVALUATE);
        params.add(PAR_SPANNING_CELL_THRESHOLD);
        params.add(PAR_MAX_PARALLEL);
        params.add(PAR_TABLE_TYPE);
        params.add(PAR_LINK_BASED_INST_THRESHOLD);
        params.add(PAR_MAX_NUM_ROWS);
        params.add(PAR_MAX_NUM_COLS);
        params.add(PAR_NUM_ITER);
        params.add(PAR_ABSTRACT_WEIGHT);
        params.add(PAR_CONTEXT_WEIGHT);
        setParameters(params);
    }
}
