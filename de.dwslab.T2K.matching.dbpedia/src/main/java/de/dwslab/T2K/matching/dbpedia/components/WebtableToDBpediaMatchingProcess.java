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
package de.dwslab.T2K.matching.dbpedia.components;

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
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.Matchers;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.TableCellCache;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.TableRowCache;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.settings.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.settings.KeyIndex;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.Similarities;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.MatchingProcess;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.timer.Timer;

public class WebtableToDBpediaMatchingProcess extends MatchingProcess {
    
    public enum tableType{webtable,lodtable}

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

    protected void initialiseComponents() {
        candidateSelection = new CandidateSelectionComponent();
        candidateSelection.initialise(getMatchers(), getData(), getEvaluationParameters(), getGoldStandard(), getLogger(), getMatchingParameters(), getSimilarities(), getRootTimer());
        candidateSelection.setKeyIndex(getKeyIndex());
        candidateSelection.setWebTableName(getWebtableName());
        valueBased = new ValueBasedComponent();
        valueBased.initialise(getMatchers(), getData(), getEvaluationParameters(), getGoldStandard(), getLogger(), getMatchingParameters(), getSimilarities(), getRootTimer());
        valueBased.setWebTableName(getWebtableName());
        classRefinement = new PropertyBasedClassRefinementComponent();
        classRefinement.initialise(getMatchers(), getData(), getEvaluationParameters(), getGoldStandard(), getLogger(), getMatchingParameters(), getSimilarities(), getRootTimer());
        classRefinement.setWebTableName(getWebtableName());
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
    public static final Parameter PAR_TABLE_TYPE = new Parameter("Process.TableType",tableType.webtable);

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
            result = new MatchingResult();

            @SuppressWarnings("unchecked")
            List<String> tbls = (List<String>) config.getValue(PAR_WEBTABLE);

            MatchingResult r = null;

            for (String t : tbls) {
                config.getConfig().put(PAR_WEBTABLE, t);

                r = runSingle(config);

                result.getEvaluation().merge(r.getEvaluation());
            }

            // put the list back into the config, in case the optimisation algorithm needs to re-use it
            config.getConfig().put(PAR_WEBTABLE, tbls);
        } else {
            result = runSingle(config);
        }

        runtime = System.currentTimeMillis() - start;
        //return result;
    }

    protected void writeRunResult(Configuration config, MatchingResult result) {
        try {
            File f = new File("runs.csv");

            boolean writeHeaders = !f.exists();

            CSVWriter w = new CSVWriter(new BufferedWriter(new FileWriter(f, true)));

            if (writeHeaders) {
                String[] headers = new String[]{"run", "webtable", "runtime", "instance baseline prec", "instance baseline recall", "instance baseline f1", "max recall", "instance prec", "instance recall", "instance f1", "property prec", "property recall", "property f1", "class precision", "class recall", "class f1"};
                headers = (String[]) ArrayUtils.addAll(headers, config.getParameterNames());
                w.writeNext(headers);
            }

            String[] values = null;

            if (result.getEvaluation().getInstanceBaseLine() == null || result.getEvaluation().getInstanceResult() == null
                    || result.getEvaluation().getClassResult() == null || result.getEvaluation().getPropertyResult() == null) {
                values = new String[]{
                    currentRun.toString(),
                    getWebtableName(),
                    Long.toString(tableRuntime)
                };
            } else {
                values = new String[]{
                    currentRun.toString(),
                    getWebtableName(),
                    Long.toString(tableRuntime),
                    Double.toString(result.getEvaluation().getInstanceBaseLine().getPrecision()),
                    Double.toString(result.getEvaluation().getInstanceBaseLine().getRecall()),
                    Double.toString(result.getEvaluation().getInstanceBaseLine().getF1Score()),
                    Double.toString(result.getEvaluation().getMaxRecall()),
                    Double.toString(result.getEvaluation().getInstanceResult().getPrecision()),
                    Double.toString(result.getEvaluation().getInstanceResult().getRecall()),
                    Double.toString(result.getEvaluation().getInstanceResult().getF1Score()),
                    Double.toString(result.getEvaluation().getPropertyResult().getPrecision()),
                    Double.toString(result.getEvaluation().getPropertyResult().getRecall()),
                    Double.toString(result.getEvaluation().getPropertyResult().getF1Score()),
                    Double.toString(result.getEvaluation().getClassResult().getPrecision()),
                    Double.toString(result.getEvaluation().getClassResult().getRecall()),
                    Double.toString(result.getEvaluation().getClassResult().getF1Score())
                };
            }

            values = (String[]) ArrayUtils.addAll(values, config.getValues());

            w.writeNext(values);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

        webtable = webtablePath;
        getData().loadWebTable(webtablePath, getRootTimer(), getMatchingParameters(),(tableType)config.getValue(PAR_TABLE_TYPE));
        String webTableName = new File(webtablePath).getName();
        System.out.println(webTableName);
        System.out.println("type: " +config.getValue(PAR_TABLE_TYPE));
        
        if (getData().getWebtable() == null || getData().getWebtable().getKey() == null) {
            getLogger().logData("Cannot load table or no key detected, stopping!");
            // table cannot be loaded
            return result;
        }

        getLogger().logData(webTableName + "\n" + getData().getWebtable().printTable());
        getLogger().logData(String.format("\n key column: [%d] %s", getData().getWebtable().getKeyIndex(), getData().getWebtable().getKey().getHeader()));
        System.out.println(String.format("\n key column: [%d] %s", getData().getWebtable().getKeyIndex(), getData().getWebtable().getKey().getHeader()));

        // load gold standard for web table
        if ((boolean) config.getValue(PAR_EVALUATE)) {
            initialiseEvaluation(webTableName);
        }

        // initialise partial matchers
        setMatchers(new Matchers(getSimilarities(), getMatchingParameters(), getRootTimer(), getGoldStandard(), getLogger()));
        getMatchers().getCandidateSelectionMatcher().setKeyIndex(getKeyIndex());
        getMatchers().getCandidateRefinementMatcher().setKeyIndex(getKeyIndex());

        // set initial parameters
        getMatchingParameters().setFastJoinDelta(1); // = edit distance threshold
        getMatchingParameters().setFastJoinTau(1); // = jaccard threshold

        initialiseComponents();

        // start actual matching process
        candidateSelection.run(config);

        if (candidateSelection.getCandidateSimilarity().getNumberOfNonZeroElements() == 0) {
            // stop here as we have no candidates
            getLogger().logData("No candidates, stopping!");
        } else {
            getSimilarities().setInitialCandidateSimilarity(candidateSelection.getCandidateSimilarity());
            getSimilarities().setClassSimilarity(candidateSelection.getClassSimilarity());
            addIntermediaResult("instances", "[01] baseline", candidateSelection.getInitialInstanceResult());
            addIntermediaResult("instances", "[02] pruned", candidateSelection.getPrunedInstanceResult());
            addIntermediaResult("instances", "[03] refined", candidateSelection.getRefinedInstanceResult());
            addIntermediaResult("classes", "[01] baseline", candidateSelection.getInitialClassResult());

            valueBased.run(config);
            getSimilarities().setValueSimilarity(valueBased.getValueSimilarity());
            getSimilarities().setLabelSimilarity(valueBased.getLabelSimilarity());

            classRefinement.run(config);
            getSimilarities().setClassSimilarity(classRefinement.getClassSimilarity());
            getSimilarities().setFinalClass(classRefinement.getFinalClass());
            getSimilarities().setCandidateSimilarity(classRefinement.getCandidateSimilarity());
            getSimilarities().setPropertySimilarity(classRefinement.getPropertySimilarity());
            
            addIntermediaResult("properties", "[01]", classRefinement.getPropertyResult());
            addIntermediaResult("classes", "[02] refined", classRefinement.getClassResult());
            addIntermediaResult("instances", "[04] 2nd refinement", classRefinement.getInstanceResult());

            int iterationNum = 1;
            for (int i = 0; i < 3; i++) {
                iterative.run(config);
                getSimilarities().setPropertySimilarity(iterative.getPropertySimilarity());
                getSimilarities().setCandidateSimilarity(iterative.getInstanceSimilarity());

                addIntermediaResult("instances", String.format("[%02d]", iterationNum + 4), iterative.getInstanceResult());
                addIntermediaResult("properties", String.format("[%02d]", iterationNum + 1), iterative.getPropertyResult());

                iterationNum++;
            }

            //only continue if at least x% of all instances can be mapped to the final class
            double mappedInstances = (double) iterative.getResult().getInstanceMappings().size() / (double) getData().getWebtableRowSet().size();
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

            result.setGoldStandard(getGoldStandard());
            result.setWebtable(getData().getWebtable());
            result.setMatchingData(getData());

            iterative.mapClasses(getSimilarities().getClassSimilarity(), result, getMatchingParameters().isCollectMatchingInfo());
            iterative.mapProperties(getSimilarities().getPropertySimilarity(), webTableName, getMatchingParameters().isCollectMatchingInfo(), result);
            iterative.mapInstances(getSimilarities().getCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), result, "");

            result.getEvaluation().setInstanceBaseLine(candidateSelection.getInitialInstanceResult());
            result.getEvaluation().setMaxCorrectCandidates(candidateSelection.getMaxCorrectCanddiates());
            result.getEvaluation().setNumCandidates(getSimilarities().getInitialCandidateSimilarity()
                    .getNumberOfNonZeroElements());

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
                        r.getCorrect(), r.getInputSetSize(),r.getReferenceSetSize()));

            }            
            if(result!=null) {
                Timer tResult = Timer.getNamed("Write Mappings", timer);
                result.write();
                tResult.stop();
            }
        }

        Timer tLog = Timer.getNamed("Write log", timer);
        getLogger().writeLog(getData().getWebtable());
        getLogger().logData(timer.toString());
        tLog.stop();

        tableRuntime = System.currentTimeMillis() - start;
//        writeRunResult(config, result);

        Timer tCleanup = Timer.getNamed("Clear cache", timer);
        TableCellCache.get().removeTable(getData().getWebtable());
        TableRowCache.get().removeTable(getData().getWebtable());
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

        getData().getWebtable().getKey();

        EvaluationResult inst = result.getEvaluation().getInstanceResult();
        //EvaluationResult max = new EvaluationResult(inst.getTruePositives()+cntNotInData, 0, inst.getFalseNegatives(), inst.getTotal());
        //EvaluationResult max = new EvaluationResult(inst.getCorrect()+cntNotInData, inst.getCorrect()+cntNotInData, inst.getReferenceSetSize());
        if (getData().getWebtable().getKey() == null || getData().getWebtable().getKey().getValues() == null || inst == null) {
            result.getEvaluation().setInstanceMax(null);
            result.setInstanceErrors(reasons);
            return;
        }
        int maxCountOfCorrect = Math.min(getData().getWebtable().getKey().getValues().size(), inst.getReferenceSetSize()) - cntNotInData;
        EvaluationResult max = new EvaluationResult(maxCountOfCorrect, maxCountOfCorrect, inst.getReferenceSetSize() - cntNotInData, getData().getWebtable().getKey().getValues().size() - cntNotInData);

        result.getEvaluation().setInstanceMax(max);
        result.setInstanceErrors(reasons);
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
        super.evaluate(config);

        double r = (result.getEvaluation().getInstanceResult().getF1Score()
                + result.getEvaluation().getPropertyResult().getF1Score()
                + result.getEvaluation().getClassResult().getF1Score()) / 3.0;

        printEvaluation(config, r);

        return r;
    }

    protected void printEvaluation(Configuration config, double result) {
        System.out.println("_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_");
        System.out.println(String.format("Evaluation of current configuration: %.4f", result));
        System.out.println(config.print());
        System.out.println("_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_");

        try {
            File f = new File("WebtableToDBpediaMatchingProcess.csv");

            boolean writeHeaders = !f.exists();

            CSVWriter w = new CSVWriter(new BufferedWriter(new FileWriter(f, true)));

            if (writeHeaders) {
                String[] headers = new String[]{"run", "instance baseline prec", "instance baseline recall", "instance baseline f1", "max recall", "instance prec", "instance recall", "instance f1", "property prec", "property recall", "property f1", "class precision", "class recall", "class f1", "evaluation result", "runtime"};
                headers = (String[]) ArrayUtils.addAll(headers, config.getParameterNames());
                w.writeNext(headers);
            }

            String[] values = new String[]{
                currentRun.toString(),
                Double.toString(getResult().getEvaluation().getInstanceBaseLine().getPrecision()),
                Double.toString(getResult().getEvaluation().getInstanceBaseLine().getRecall()),
                Double.toString(getResult().getEvaluation().getInstanceBaseLine().getF1Score()),
                Double.toString(getResult().getEvaluation().getMaxRecall()),
                Double.toString(getResult().getEvaluation().getInstanceResult().getPrecision()),
                Double.toString(getResult().getEvaluation().getInstanceResult().getRecall()),
                Double.toString(getResult().getEvaluation().getInstanceResult().getF1Score()),
                Double.toString(getResult().getEvaluation().getPropertyResult().getPrecision()),
                Double.toString(getResult().getEvaluation().getPropertyResult().getRecall()),
                Double.toString(getResult().getEvaluation().getPropertyResult().getF1Score()),
                Double.toString(getResult().getEvaluation().getClassResult().getPrecision()),
                Double.toString(getResult().getEvaluation().getClassResult().getRecall()),
                Double.toString(getResult().getEvaluation().getClassResult().getF1Score()),
                Double.toString(result),
                Long.toString(runtime)
            };

            values = (String[]) ArrayUtils.addAll(values, config.getValues());

            w.writeNext(values);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        params.addAll(ValueBasedComponent.getParams());
        params.addAll(PropertyBasedClassRefinementComponent.getParams());
        params.addAll(IterativeComponent.getParams());
        params.add(PAR_WEBTABLE);
        params.add(PAR_MAPPED_RATIO_FILTER);
        params.add(PAR_EVALUATE);
        params.add(PAR_SPANNING_CELL_THRESHOLD);
        params.add(PAR_MAX_PARALLEL);
        params.add(PAR_TABLE_TYPE);
        setParameters(params);
    }
}
