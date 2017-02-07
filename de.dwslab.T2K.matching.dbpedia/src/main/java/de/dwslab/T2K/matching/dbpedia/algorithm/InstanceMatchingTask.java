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
package de.dwslab.T2K.matching.dbpedia.algorithm;

import au.com.bytecode.opencsv.CSVWriter;
import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.dbpedia.DBpediaIndexer;
import de.dwslab.T2K.index.io.InMemoryIndex;
import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.WebJaccardSimilarity;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.PAR_NUM_ITER;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.model.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableCellCache;
import de.dwslab.T2K.matching.dbpedia.model.TableColumnCache;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.TableRowCache;
import de.dwslab.T2K.matching.dbpedia.model.adapters.CandidateAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.ColumnAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.DBpediaInstanceAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.DBpediaPropertyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnToCellHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowToCellHierarchyAdapter;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.MatchingProcess;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.secondline.Aggregate;
import de.dwslab.T2K.matching.secondline.AggregationType;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.CombineNonOverlapping;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.tableprocessor.model.TableMapping;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.query.Func;
import de.dwslab.T2K.utils.query.Q;
import de.dwslab.T2K.utils.timer.Timer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author domi
 */
public class InstanceMatchingTask extends MatchingProcess {

    private String abstractCandidateResults = "";

    /**
     * @return the namedEntity
     */
    public NEComponent getNEComponent() {
        return namedEntity;
    }

    /**
     * @return the abstractCandidateResults
     */
    public String getAbstractCandidateResults() {
        return abstractCandidateResults;
    }

    /**
     * @param abstractCandidateResults the abstractCandidateResults to set
     */
    public void setAbstractCandidateResults(String abstractCandidateResults) {
        this.abstractCandidateResults = abstractCandidateResults;
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

    }
    public static final Parameter PAR_WEBTABLE = new Parameter("Process.WebTable");
    public static final Parameter PAR_MAPPED_RATIO_FILTER = new Parameter("Process.MappedRatioFilter", 0.5);
    public static final Parameter PAR_EVALUATE = new Parameter("Process.Evaluate", true);
    public static final Parameter PAR_SPANNING_CELL_THRESHOLD = new Parameter("Process.SpanningCellThreshold", 1);
    public static final Parameter PAR_MAX_PARALLEL = new Parameter("Process.MaxParallel", 0);
    public static final Parameter PAR_TABLE_TYPE = new Parameter("Process.TableType", WebtableToDBpediaMatchingProcess.tableType.webtable);
    public static final Parameter PAR_MAX_NUM_ROWS = new Parameter("Process.MaxNumberOfRows", 0);
    public static final Parameter PAR_MAX_NUM_COLS = new Parameter("Process.MaxNumberOfCols", 0);
    public static final Parameter PAR_ABSTRACT_WEIGHT = new Parameter("Process.AbstractWeight", 0.0);
    public static final Parameter PAR_INST_FINAL_THRESHOLD = new Parameter("Iterative.Instance.Final_threshold", 0.8);
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
        getMatchingParameters().setTableType((de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.tableType) config.getValue(PAR_TABLE_TYPE));

        webtable = webtablePath;
        getData().loadWebTable(webtablePath, getRootTimer(), getMatchingParameters(), (de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.tableType) config.getValue(PAR_TABLE_TYPE));
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

        initialiseComponents();

        getLogger().logData(webTableName + "\n" + getData().getWebtable().printTable());
        getLogger().logData(String.format("\n key column: [%d] %s", getData().getWebtable().getKeyIndex(), getData().getWebtable().getKey().getHeader()));
        System.out.println(String.format("\n key column: [%d] %s", getData().getWebtable().getKeyIndex(), getData().getWebtable().getKey().getHeader()));

        // start actual matching process
        candidateSelection.run(config);
        getSimilarities().setClassSimilarity(candidateSelection.getClassSimilarity());


        candidateRefinement.setInitialCandidateSimilarity(candidateSelection.getInitialCandidateSimilarity());
        candidateRefinement.setClassSimilarity(getSimilarities().getClassSimilarity());
        candidateRefinement.run(config);
        SimilarityMatrix<TableRow> refinement = candidateRefinement.getCandidateSimilarity();
        //       refinement.pruneWithNull(0.7);
        //maybe?
        //refinement.makeRowStochastic();

//            
        if (refinement.getNumberOfNonZeroElements() == 0) {
            // stop here as we have no candidates
            getLogger().logData("No candidates, stopping!");
        } else {

            //getSimilarities().setClassSimilarity(candidateSelection.getClassSimilarity());
            getSimilarities().setInitialCandidateSimilarity(refinement);

            if ((boolean) config.getValue(PAR_EVALUATE)) {
                addIntermediaResult("instances", "[01] baseline", candidateSelection.getInitialInstanceResult());
                addIntermediaResult("instances", "[02] pruned", candidateSelection.getPrunedInstanceResult());
                addIntermediaResult("instances", "[03] refined", candidateRefinement.getRefinedInstanceResult());
                addIntermediaResult("classes", "[01] baseline", candidateSelection.getInitialClassResult());
                System.out.println("refinement!!! " + candidateRefinement.getRefinedInstanceResult().getPrecision() + " rec: " + candidateRefinement.getRefinedInstanceResult().getRecall()
                        + " f " + candidateRefinement.getRefinedInstanceResult().getF1Score() + " correct " + candidateRefinement.getRefinedInstanceResult().getCorrect() + " found " + candidateRefinement.getRefinedInstanceResult().getInputSetSize());

            }

            SimilarityMatrix<TableColumn> propertySim = computePropertyMappings(true);



//            MatchingResult beforeAbstract = new MatchingResult();
//            candidateSelection.mapInstances(candidateRefinement.getCandidateSimilarity().copy(), true, beforeAbstract, webtable);

            //don't do that now
//            if ((double) config.getValue(PAR_ABSTRACT_WEIGHT) > 0.0) {
//                candidateAbstractSelection.run(config);
//                SimilarityMatrix<TableRow> firstAbstracts = candidateAbstractSelection.getCandidateAbstractSimilarity();
//                firstAbstracts.pruneWithNull(0.7);
//
//                CombineNonOverlapping nonOverlap = new CombineNonOverlapping();
//                nonOverlap.setAggregationType(CombinationType.WeightedSum);
//                nonOverlap.setFirstWeight((double) config.getValue(PAR_ABSTRACT_WEIGHT));
//                nonOverlap.setSecondWeight(1.0 - (double) config.getValue(PAR_ABSTRACT_WEIGHT));
//                SimilarityMatrix allCandidates = nonOverlap.match(firstAbstracts, candidateRefinement.getCandidateSimilarity());
//                getSimilarities().setCandidateSimilarity(allCandidates);
//            }

            //MatchingResult abstractMatching = new MatchingResult();
            //candidateSelection.mapInstances(candidateAbstractSelection.getCandidateAbstractSimilarity().copy(), true, abstractMatching, webtable);

//            StringBuilder b = new StringBuilder();
//                            for (TableRow r : getData().getWebtableRowSet()) {
//                    b.append(getData().getWebtable().getHeader() + "\t");
//                    b.append(r + "\t");
//                    boolean correctFoundCand = false;
//                    for (Correspondence c1 : beforeAbstract.getInstanceMappings()) {                        
//                        if (c1.getFirst().equals(r)) {
//                            b.append(c1.isCorrect() + "\t");
//                            correctFoundCand = true;
//                        }
//                    }
//                    if(!correctFoundCand) {
//                        b.append("-1\t");
//                    }
//                    boolean correctFoundAbstract = false;
//                    for (Correspondence c2 : abstractMatching.getInstanceMappings()) {
//                        if (c2.getFirst().equals(r)) {
//                            b.append(c2.isCorrect() + "\t");
//                            correctFoundAbstract = true;
//                        }
//                    }
//                    if(!correctFoundAbstract) {
//                        b.append("-1\t");
//                    }
//                    b.append(candidateRefinement.getCandidateSimilarity().getMatchesAboveThreshold(r, 0.0).size()+ "\t");
//                    b.append(candidateRefinement.getCandidateSimilarity().getMatchesAboveThreshold(r, 0.5).size()+ "\t");
//                    b.append(candidateRefinement.getCandidateSimilarity().getMatchesAboveThreshold(r, 0.7).size()+ "\t");
//                    b.append(candidateRefinement.getCandidateSimilarity().getMatchesAboveThreshold(r, 0.95).size()+ "\t");
//                    double sum = 0.0;
//                    for (TableRow cands : candidateRefinement.getCandidateSimilarity().getMatchesAboveThreshold(r, 0.0)) {
//                        sum += candidateRefinement.getCandidateSimilarity().get(r, cands);
//                    }
//                    b.append(sum + "\t");
//                    b.append(sum/candidateRefinement.getCandidateSimilarity().getMatchesAboveThreshold(r, 0.0).size()+"\t");
////                }
//                    b.append(candidateAbstractSelection.getCandidateAbstractSimilarity().getMatchesAboveThreshold(r, 0.0).size()+ "\t");
//                    b.append(candidateAbstractSelection.getCandidateAbstractSimilarity().getMatchesAboveThreshold(r, 0.5).size()+ "\t");
//                    b.append(candidateAbstractSelection.getCandidateAbstractSimilarity().getMatchesAboveThreshold(r, 0.7).size()+ "\t");
//                    b.append(candidateAbstractSelection.getCandidateAbstractSimilarity().getMatchesAboveThreshold(r, 0.95).size()+ "\t");
//                    sum = 0.0;
//                    for (TableRow cands : candidateAbstractSelection.getCandidateAbstractSimilarity().getMatchesAboveThreshold(r, 0.0)) {
//                        sum += candidateAbstractSelection.getCandidateAbstractSimilarity().get(r, cands);
//                    }
//                    b.append(sum + "\t");
//                    b.append(sum/candidateAbstractSelection.getCandidateAbstractSimilarity().getMatchesAboveThreshold(r, 0.0).size()+"\n");
//                    
//                    setAbstractCandidateResults(b.toString());
//                    // candidateSelection.mapInstances(candidateAbstractSelection.getCandidateAbstractSimilarity(), getMatchingParameters().isCollectMatchingInfo(), abstractMatching, "");
//
//                }

            SimilarityMatrix<TableRow> candiBasedOnAbstracts = null;
            if ((double) config.getValue(PAR_ABSTRACT_WEIGHT) > 0.0) {
                candidateAbstractSelection.run(config);
                SimilarityMatrix<TableRow> firstAbstracts = candidateAbstractSelection.getCandidateAbstractSimilarity();

                StringSimilarityMeasure<TableRow> keyMeasure = new StringSimilarityMeasure<>();
                keyMeasure.setSimilarityFunction(new WebJaccardSimilarity());
                keyMeasure.setSetSimilarity(new MaxSimilarity<>());

                TableRowMatchingAdapter adapti = new TableRowMatchingAdapter();

                candiBasedOnAbstracts = new SparseSimilarityMatrix(firstAbstracts.getFirstDimension().size(), firstAbstracts.getSecondDimension().size());

                for (TableRow r : firstAbstracts.getFirstDimension()) {
                    for (TableRow s : firstAbstracts.getMatches(r)) {
                        Double similarity = keyMeasure.calculate(r, s, adapti, null);
                        if (similarity != null) {
                            candiBasedOnAbstracts.set(r, s, similarity);
                        }
                    }
                }
            }

            SimilarityMatrix<TableRow> allCandis = null;
            if (candiBasedOnAbstracts != null) {
                CombineNonOverlapping nonOverlap = new CombineNonOverlapping();
                nonOverlap.setAggregationType(CombinationType.WeightedSum);
                nonOverlap.setFirstWeight(0.5);
                nonOverlap.setSecondWeight(0.5);
                allCandis = nonOverlap.match(candiBasedOnAbstracts, candidateRefinement.getCandidateSimilarity());

            } else {
                allCandis = candidateRefinement.getCandidateSimilarity();
            }

            getSimilarities().setCandidateSimilarity(allCandis);

            valueBased.run(config);

            getSimilarities().setValueSimilarity(valueBased.getValueSimilarity());
            //getSimilarities().setLabelSimilarity(valueBased.getLabelSimilarity());


            SimilarityMatrix<TableCell> inst = Matcher.multiplyParentSimilarity(propertySim, getSimilarities().getValueSimilarity(), new TableColumnToCellHierarchyAdapter());

            Aggregate<TableRow, TableCell> a = new Aggregate<>();
            a.setAggregationType(AggregationType.Sum);
            SimilarityMatrix<TableRow> propWeightedInstanceCand = a.match(allCandis, inst, new TableRowToCellHierarchyAdapter());
            SimilarityMatrix<TableRow> values = propWeightedInstanceCand;
            values.normalize();
            getSimilarities().setCandidateSimilarity(values);

            Map<TableRow, Set<TableRow>> possibleMatchesByCol = new HashMap<>();
            double sizeOfPoss = 0, numberCand = 0, numberValue = 0, numberAbs = 0;
            Map<String, TableRow> rowToUri = new HashMap<>();


            for (TableRow c : getData().getWebtableRowSet()) {

                for (TableRow d : allCandis.getMatches(c)) {
                    sizeOfPoss++;
                    if (possibleMatchesByCol.containsKey(c)) {
                        possibleMatchesByCol.get(c).add(d);
                    } else {
                        Set<TableRow> x = new HashSet<>();
                        x.add(d);
                        possibleMatchesByCol.put(c, x);
                    }
                }
                for (TableRow d : values.getMatches(c)) {
                    sizeOfPoss++;
                    if (possibleMatchesByCol.containsKey(c)) {
                        possibleMatchesByCol.get(c).add(d);
                    } else {
                        Set<TableRow> x = new HashSet<>();
                        x.add(d);
                        possibleMatchesByCol.put(c, x);
                    }
                }
                if (candidateAbstractSelection.getCandidateAbstractSimilarity() != null) {

                    for (TableRow d : candidateAbstractSelection.getCandidateAbstractSimilarity().getMatches(c)) {
                        sizeOfPoss++;
                        if (possibleMatchesByCol.containsKey(c)) {
                            possibleMatchesByCol.get(c).add(d);
                        } else {
                            Set<TableRow> x = new HashSet<>();
                            x.add(d);
                            possibleMatchesByCol.put(c, x);
                        }
                    }
                }
                Map<String, Integer> duplicateDetection = new HashMap<>();



                if (!possibleMatchesByCol.isEmpty() && possibleMatchesByCol.containsKey(c)) {
                    for (TableRow d : possibleMatchesByCol.get(c)) {
                        System.out.println("d: " + d);
                        if (!duplicateDetection.containsKey(d.getURI().toString())) {
                            duplicateDetection.put(d.getURI().toString(), 0);
                        }
                        int counter = 0;
                        double sum = 0.0;

                        if (allCandis.get(c, d) != null) {
                            counter++;
                            sum += allCandis.get(c, d);
                            numberCand++;
                        }
                        if (values.get(c, d) != null) {
                            counter++;
                            sum += values.get(c, d);
                            numberValue++;
                        }

                        if (candidateAbstractSelection.getCandidateAbstractSimilarity() != null) {
                            if (candidateAbstractSelection.getCandidateAbstractSimilarity().get(c, d) != null) {
                                counter++;
                                sum += candidateAbstractSelection.getCandidateAbstractSimilarity().get(c, d);
                                numberAbs++;
                            }
                        }
                        counter *= sum;

                        //System.out.println("counter " + counter);

                        if (counter >= duplicateDetection.get(d.getURI().toString())) {
                            duplicateDetection.put(d.getURI().toString(), counter);
                            rowToUri.put(d.getURI().toString(), d);
                            //System.out.println("added: " + d.getURI().toString() + " val: " + rowToUri.get(d.getURI().toString()));
                        }
                    }
                }
            }

            numberCand = numberCand / sizeOfPoss;
            numberValue = numberValue / sizeOfPoss;
            numberAbs = numberAbs / sizeOfPoss;

            numberCand = 1 - numberCand;
            numberValue = 1 - numberValue;
            numberAbs = 1 - numberAbs;

            for (TableRow c1 : allCandis.getFirstDimension()) {
                for (TableRow d : allCandis.getMatches(c1)) {
                    if (rowToUri.containsKey(d.getURI().toString()) && !rowToUri.get(d.getURI().toString()).equals(d)) {
                        allCandis.set(c1, d, null);
                    }
                }
            }

            for (TableRow c1 : values.getFirstDimension()) {
                for (TableRow d : values.getMatches(c1)) {
                    if (rowToUri.containsKey(d.getURI().toString()) && !rowToUri.get(d.getURI().toString()).equals(d)) {
                        values.set(c1, d, null);
                    }
                }
            }

            if (candidateAbstractSelection.getCandidateAbstractSimilarity() != null) {
                for (TableRow c1 : candidateAbstractSelection.getCandidateAbstractSimilarity().getFirstDimension()) {
                    for (TableRow d : candidateAbstractSelection.getCandidateAbstractSimilarity().getMatches(c1)) {
                        if (rowToUri.containsKey(d.getURI().toString()) && !rowToUri.get(d.getURI().toString()).equals(d)) {
                            candidateAbstractSelection.getCandidateAbstractSimilarity().set(c1, d, null);
                        }
                    }
                }
            }

            Map<SimilarityMatrix, MatrixStats> stats = new HashMap<>();
            stats.put(allCandis, new MatrixStats(allCandis, getData(), possibleMatchesByCol));
            stats.put(values, new MatrixStats(values, getData(), possibleMatchesByCol));
            if (candidateAbstractSelection.getCandidateAbstractSimilarity() != null) {
                stats.put(candidateAbstractSelection.getCandidateAbstractSimilarity(), new MatrixStats(candidateAbstractSelection.getCandidateAbstractSimilarity(), getData(), possibleMatchesByCol));
            }


            SimilarityMatrix<TableRow> combined, combinedWithWeight;

            CombineNonOverlapping nonOverlap = new CombineNonOverlapping();
            nonOverlap.setAggregationType(CombinationType.Sum);
//            nonOverlap.setFirstWeight(2.0);
//            nonOverlap.setSecondWeight(3.0);
            combined = nonOverlap.match(allCandis, values);

            nonOverlap = new CombineNonOverlapping();
            nonOverlap.setAggregationType(CombinationType.Sum);

            SimilarityMatrix x1 = allCandis.copy();
            x1.multiplyScalar(stats.get(allCandis).getHerfindahlIndex());
            SimilarityMatrix y1 = values.copy();
            y1.multiplyScalar(stats.get(values).getHerfindahlIndex());
            combinedWithWeight = nonOverlap.match(x1, y1);

            SimilarityMatrix<TableRow> abstractMatrix = null;
            if ((double) config.getValue(PAR_ABSTRACT_WEIGHT) > 0.0) {
                SimilarityMatrix abstractFiltering = candidateAbstractSelection.getCandidateAbstractSimilarity().copy();

                for (TableRow r1 : candidateAbstractSelection.getCandidateAbstractSimilarity().getFirstDimension()) {
                    double sum = 0.0, count = 0.0;
                    for (TableRow r2 : candidateAbstractSelection.getCandidateAbstractSimilarity().getMatches(r1)) {
                        sum += candidateAbstractSelection.getCandidateAbstractSimilarity().get(r1, r2);
                        count += 1;
                    }
                    double quot = sum / count;
                    if (quot < 0.5) {
                        for (TableRow r2 : candidateAbstractSelection.getCandidateAbstractSimilarity().getMatches(r1)) {
                            abstractFiltering.set(r1, r2, null);
                        }
                    }
                }
                abstractMatrix = candidateAbstractSelection.getCandidateAbstractSimilarity();
                //               abstractMatrix.pruneWithNull(0.7);
                nonOverlap = new CombineNonOverlapping();
                nonOverlap.setAggregationType(CombinationType.Sum);
                x1 = abstractMatrix.copy();
                x1.multiplyScalar(stats.get(candidateAbstractSelection.getCandidateAbstractSimilarity()).getHerfindahlIndex());
                y1 = combinedWithWeight.copy();
                stats.put(combinedWithWeight, new MatrixStats(combinedWithWeight, data));
                combinedWithWeight = nonOverlap.match(x1, y1);
                combined = nonOverlap.match(abstractMatrix, combined);

            }
            //currently no pruning
            //           combinedWithWeight.pruneWithNull(2.0);

            for (TableRow c1 : combinedWithWeight.getFirstDimension()) {
                for (TableRow d : combinedWithWeight.getMatches(c1)) {
                    if (rowToUri.containsKey(d.getURI().toString()) && !rowToUri.get(d.getURI().toString()).equals(d)) {
                        combinedWithWeight.set(c1, d, null);
                    }
                }
            }

            stats.put(combinedWithWeight, new MatrixStats(combinedWithWeight, data));

            getSimilarities().setCandidateSimilarity(combinedWithWeight);



            //TODO check which matrix is used!
            SimilarityMatrix sim = getSimilarities().getCandidateSimilarity().copy();

            SimilarityMatrix one2one = classRefinement.mapInstances(combinedWithWeight.copy(), getMatchingParameters().isCollectMatchingInfo(), new MatchingResult(), "");

            stats.put(one2one, new MatrixStats(sim, data));
            one2one.pruneWithNull(stats.get(one2one).getStad());

            getSimilarities().setCandidateSimilarity(one2one);

            StringBuilder b = new StringBuilder();

            EvaluationAdapter<TableRow> evalRow = new CandidateAdapter();
            EvaluationAdapter<TableRow> evalInstance = new DBpediaInstanceAdapter();

            for (TableRow c : possibleMatchesByCol.keySet()) {

                Object o1 = evalRow.getUniqueIdentifier(c);

                for (TableRow d : possibleMatchesByCol.get(c)) {
                    try {
                        if (!rowToUri.get(d.getURI().toString()).equals(d)) {
                            continue;
                        }
                    } catch (Exception e) {
                        System.out.println("error! " + c + " -- " + d.getURI());
                    }
                    b.append(c.getTable().getFullPath() + "_" + c + "\t");
                    b.append(d.getURI() + "\t");
                    if (allCandis.get(c, d) != null) {
                        b.append(allCandis.get(c, d) + "\t");
                    } else {
                        b.append("\t");
                    }
                    if (values.get(c, d) != null) {
                        b.append(values.get(c, d) + "\t");
                    } else {
                        b.append("\t");
                    }
                    if (abstractMatrix != null) {

                        if (abstractMatrix.get(c, d) != null) {
                            b.append(abstractMatrix.get(c, d) + "\t");
                        } else {
                            b.append("\t");
                        }
                    }
                    if (combined.get(c, d) != null) {
                        b.append(combined.get(c, d) + "\t");
                    } else {
                        b.append("\t");
                    }
                    if (combinedWithWeight.get(c, d) != null) {
                        b.append(combinedWithWeight.get(c, d) + "\t");
                    } else {
                        b.append("\t");
                    }
                    if (one2one.get(c, d) != null) {
                        b.append("1\t");
                    } else {
                        b.append("0\t");
                    }
                    Object o2 = evalInstance.getUniqueIdentifier(d);
                    if (!getGoldStandard().getInstanceGoldStandard().containsKey(o1)) {
                        b.append("0\n");
                    } else if (getGoldStandard().getInstanceGoldStandard().get(o1).equals(o2)) {
                        b.append("1\n");
                    } else {
                        b.append("0\n");
                    }
                }
            }
            abstractCandidateResults = b.toString();

            b = new StringBuilder();


            for (TableRow c1 : combinedWithWeight.getFirstDimension()) {

                Object o1 = evalRow.getUniqueIdentifier(c1);


                for (TableRow d : combinedWithWeight.getMatches(c1)) {
                    try {
                        if (!rowToUri.get(d.getURI().toString()).equals(d)) {
                            combinedWithWeight.set(c1, d, null);
                            continue;
                        }
                    } catch (Exception e) {
                        System.out.println("error! " + c1 + " -- " + d.getURI());
                    }
                    b.append(c1.getTable().getFullPath() + "_" + c1 + "\t");
                    b.append(d.getURI() + "\t");
                    if (allCandis.get(c1, d) != null) {
                        b.append(allCandis.get(c1, d) + "\t");
                    } else {
                        b.append("\t");
                    }
                    if (values.get(c1, d) != null) {
                        b.append(values.get(c1, d) + "\t");
                    } else {
                        b.append("\t");
                    }
                    if (allCandis.get(c1, d) != null || values.get(c1, d) != null) {
                        double mean = 0.0, std = 0.0, vari = 0.0, dens = 0.0, meanDen = 0.0, stdDen = 0.0, variDen = 0.0;
                        if (allCandis.get(c1, d) == null) {
                            std = values.get(c1, d) * stats.get(values).getStad();
                            mean = values.get(c1, d) * stats.get(values).getMean();
                            vari = values.get(c1, d) * stats.get(values).getVari();
                            dens = values.get(c1, d) * stats.get(values).getDensity();
                            meanDen = values.get(c1, d) * stats.get(values).getMeanWdens();
                            stdDen = values.get(c1, d) * stats.get(values).getStadWdens();
                            variDen = values.get(c1, d) * stats.get(values).getVariWdens();
                        } else if (values.get(c1, d) == null) {
                            std = allCandis.get(c1, d) * stats.get(allCandis).getStad();
                            mean = allCandis.get(c1, d) * stats.get(allCandis).getMean();
                            vari = allCandis.get(c1, d) * stats.get(allCandis).getVari();
                            dens = allCandis.get(c1, d) * stats.get(allCandis).getDensity();
                            meanDen = allCandis.get(c1, d) * stats.get(allCandis).getMeanWdens();
                            stdDen = allCandis.get(c1, d) * stats.get(allCandis).getStadWdens();
                            variDen = allCandis.get(c1, d) * stats.get(allCandis).getVariWdens();
                        } else {
                            std = allCandis.get(c1, d) * stats.get(allCandis).getStad() + values.get(c1, d) * stats.get(values).getStad();
                            mean = allCandis.get(c1, d) * stats.get(allCandis).getMean() + values.get(c1, d) * stats.get(values).getMean();
                            vari = allCandis.get(c1, d) * stats.get(allCandis).getVari() + values.get(c1, d) * stats.get(values).getVari();
                            dens = allCandis.get(c1, d) * stats.get(allCandis).getDensity() + values.get(c1, d) * stats.get(values).getDensity();
                            meanDen = allCandis.get(c1, d) * stats.get(allCandis).getMeanWdens() + values.get(c1, d) * stats.get(values).getMeanWdens();
                            stdDen = allCandis.get(c1, d) * stats.get(allCandis).getStadWdens() + values.get(c1, d) * stats.get(values).getStadWdens();
                            variDen = allCandis.get(c1, d) * stats.get(allCandis).getVariWdens() + values.get(c1, d) * stats.get(values).getVariWdens();
                        }
                        b.append(std + "\t");
                        b.append(mean + "\t");
                        b.append(vari + "\t");
                        b.append(dens + "\t");
                        b.append(meanDen + "\t");
                        b.append(stdDen + "\t");
                        b.append(variDen + "\t");
                    } else {
                        b.append("\t");
                        b.append("\t");
                        b.append("\t");
                        b.append("\t");
                        b.append("\t");
                        b.append("\t");
                        b.append("\t");
                    }
                    if (abstractMatrix != null) {

                        if (abstractMatrix.get(c1, d) != null) {
                            b.append(abstractMatrix.get(c1, d) + "\t");
                        } else {
                            b.append("\t");
                        }
                    }
                    if (combined.get(c1, d) != null) {
                        b.append(combined.get(c1, d) + "\t");
                    } else {
                        b.append("\t");
                    }
                    if (combinedWithWeight.get(c1, d) != null) {
                        b.append(combinedWithWeight.get(c1, d) + "\t");
                    } else {
                        b.append("\t");
                    }
                    if (one2one.get(c1, d) != null) {
                        b.append("1\t");
                    } else {
                        b.append("0\t");
                    }
                    Object o2 = evalInstance.getUniqueIdentifier(d);
                    if (!getGoldStandard().getInstanceGoldStandard().containsKey(o1)) {
                        b.append("0\n");
                    } else if (getGoldStandard().getInstanceGoldStandard().get(o1).equals(o2)) {
                        b.append("1\n");
                    } else {
                        b.append("0\n");
                    }
                }
            }
            abstractCandidateResults = b.toString();

            //TODO: do or don't???
            //getSimilarities().getPropertySimilarity().prune(0.01);
            //only continue if at least x% of all instances can be mapped to the final class
            double mappedInstances;
//            if (iterative.getResult() != null) {
//                mappedInstances = (double) iterative.getResult().getInstanceMappings().size() / (double) getData().getWebtableRowSet().size();
//            } else {
            mappedInstances = (double) classRefinement.getNumberOfInstanceMappings() / (double) getData().getWebtableRowSet().size();
//            }

            if (mappedInstances < (Double) config.getValue(PAR_MAPPED_RATIO_FILTER)) {
                getLogger().logData("Not enough consistent candidates, cancelling!");
                // not enough mappings with the selected class, we do not map this table at all
                getSimilarities().setClassSimilarity(new SparseSimilarityMatrix<Table>(0, 0));
                getSimilarities().setPropertySimilarity(new SparseSimilarityMatrix<TableColumn>(0, 0));
                getSimilarities().setCandidateSimilarity(new SparseSimilarityMatrix<TableRow>(0, 0));
            }

            result.setWebtable(getData().getWebtable());
            result.setMatchingData(getData());

            classRefinement.mapInstances(sim, getMatchingParameters().isCollectMatchingInfo(), result, "");

            if ((Boolean) config.getValue(de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.PAR_EVALUATE)) {
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
                //not yet!
                //               result.write((WebtableToDBpediaMatchingProcess.tableType)config.getValue(PAR_TABLE_TYPE));
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
                            if (r != null && (Boolean) c.getValue(de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.PAR_EVALUATE)) {
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
                    if (r.getEvaluation().getClassResult() == null && r.getEvaluation().getPropertyResult() == null) {
                        res = 0.0;
                    } //only for the large GS!
                    else if (r.getEvaluation().getPropertyResult() == null) {
                        res = r.getEvaluation().getClassResult().getF1Score();
                    } else {
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
            if (getResult().getEvaluation().getClassResult() == null) {
                values = new String[]{"NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA"};
            } else if (getResult().getEvaluation().getPropertyResult() == null) {
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
                    Long.toString(runtime),};
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

    private SimilarityMatrix<TableColumn> computePropertyMappings(boolean gs) {
        SimilarityMatrix<TableColumn> propertySim = null;
        EvaluationAdapter<TableColumn> evalCol = new ColumnAdapter();
        EvaluationAdapter<TableColumn> evalProp = new DBpediaPropertyAdapter(getGoldStandard().getPropertyCanoniser());
        if (gs) {
            propertySim = new SparseSimilarityMatrix(getData().getWebtable().getColumns().size(), getGoldStandard().getPropertyGoldStandard().size());
            Map<Object, Object> propGS = getGoldStandard().getPropertyGoldStandard();


            for (TableColumn c : getData().getWebtable().getColumns()) {
                if (propGS.containsKey(evalCol.getUniqueIdentifier(c))) {
                    for (TableColumn d : getData().getDbpediaColSet()) {
                        if (propGS.get(evalCol.getUniqueIdentifier(c)).equals(evalProp.getUniqueIdentifier(d))) {
                            propertySim.set(c, d, 1.0);
                        }
                    }
                } else if (c.isKey()) {
                    for (Table matchingTable : getSimilarities().getClassSimilarity().getMatchesAboveThreshold(getData().getWebtable(), 0.0)) {
                        propertySim.set(c, matchingTable.getKey(), 1.0);
                    }
                }
            }
        } else {
            try {
                propertySim = new SparseSimilarityMatrix(getData().getWebtable().getColumns().size(), getGoldStandard().getPropertyGoldStandard().size());

                TableMapping tm = new TableMapping();
                tm.readMapping("/home/dritze/WTMatching/mappings/" + getData().getWebtable().getHeader());
                Map<Integer, Pair<String, Double>> readPropMappings = tm.getMappedProperties();
                for (TableColumn c : getData().getWebtable().getColumns()) {
                    if (c.isKey()) {
                        for (Table matchingTable : getSimilarities().getClassSimilarity().getMatchesAboveThreshold(getData().getWebtable(), 0.0)) {
                            propertySim.set(c, matchingTable.getKey(), 1.0);
                        }
                        continue;
                    }
                    if (readPropMappings.containsKey((Integer) evalCol.getUniqueIdentifier(c)))  {
                        Pair<String, Double> singleCorres = readPropMappings.get((Integer) evalCol.getUniqueIdentifier(c));
                        for (TableColumn d : getData().getDbpediaColSet()) {
                            if (evalProp.getUniqueIdentifier(d).equals(singleCorres.getFirst())) {
                                propertySim.set(c, d, singleCorres.getSecond());
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return propertySim;
    }

    public InstanceMatchingTask clone() {
        InstanceMatchingTask wp = new InstanceMatchingTask();
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

    public InstanceMatchingTask() {
        List<Parameter> params = CandidateSelectionComponent.getParams();
        params.addAll(ValueBasedComponent.getParams());
        params.addAll(CandidateRefinementComponent.getParams());
        params.add(PAR_WEBTABLE);
        params.add(PAR_MAPPED_RATIO_FILTER);
        params.add(PAR_EVALUATE);
        params.add(PAR_SPANNING_CELL_THRESHOLD);
        params.add(PAR_MAX_PARALLEL);
        params.add(PAR_TABLE_TYPE);
        params.add(PAR_MAX_NUM_ROWS);
        params.add(PAR_MAX_NUM_COLS);
        params.add(PAR_NUM_ITER);
        params.add(PAR_ABSTRACT_WEIGHT);
        params.add(PAR_INST_FINAL_THRESHOLD);
        setParameters(params);
    }
}
