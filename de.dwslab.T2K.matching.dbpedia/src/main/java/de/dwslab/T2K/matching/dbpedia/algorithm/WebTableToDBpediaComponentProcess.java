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

import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.dbpedia.DBpediaIndexer;
import de.dwslab.T2K.index.io.InMemoryIndex;
import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.blocking.IdentityBlocking;
import de.dwslab.T2K.matching.blocking.TypeBasedBlocking;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.AlwaysMatchSimilarityFunction;
import de.dwslab.T2K.matching.dbpedia.WebJaccardSimilarity;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.DuplicateBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.LabelBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.SecondLineClassMatcher;
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
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnToCellHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowToCellHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.properties.Canoniser;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.MatchingProcess;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.secondline.Aggregate;
import de.dwslab.T2K.matching.secondline.AggregationType;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.CombineNonOverlapping;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.timer.Timer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.dictionary.Dictionary;

/**
 *
 * @author domi
 */
public class WebTableToDBpediaComponentProcess extends MatchingProcess {

    private String abstractCandidateResults = "";

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
    private LabelBasedSchemaMatcher labelMatcher;
    private DuplicateBasedSchemaMatcher dp;
    private NEComponent ne;

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

        labelMatcher = new LabelBasedSchemaMatcher(similarities, matchingParameters, rootTimer, goldStandard, logger);

        dp = new DuplicateBasedSchemaMatcher(similarities, matchingParameters, rootTimer, goldStandard, logger);

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
    public static final Parameter PAR_PROP_KEY_WEIGHT = new Parameter("Iterative.Property.Key_weight", 5);
    public static final Parameter PAR_LABEL_SIMILARITY = new Parameter("ValueBasedIntanceMatcher.LabelSimilarity", new AlwaysMatchSimilarityFunction());
    public static final Parameter PAR_PROP_NUM_CANDIDATES = new Parameter("Iterative.Property.Num_Cand", 2);
    public static final Parameter PAR_PROP_CANDIDATE_THRESHOLD = new Parameter("Iterative.Property.Cand_threshold", 0.1);
    public static final Parameter PAR_PROP_VALUE_THRESHOLD = new Parameter("Iterative.Property.Value_threshold", 0.3);
    public static final Parameter PAR_PROP_NUM_VOTES = new Parameter("Iterative.Property.Num_Votes", 2);
    public static final Parameter PAR_PROP_NUM_RESULTS = new Parameter("Iterative.Property.Num_Results", 2);
    public static final Parameter PAR_PROP_FINAL_THRESHOLD = new Parameter("Iterative.Property.Final_threshold", 0.0);
    public static final Parameter PAR_RUN_LABEL_MATCHING = new Parameter("ValueBasedInstanceMatcher.RunLabelMatching", true);
    public static final Parameter PAR_PROP_VALUE_WEIGHT = new Parameter("PropMatching.ValueWeight", 0.5);
    public static final Parameter PAR_PROP_LABEL_THRESHOLD = new Parameter("PropMatching.Label", 0.5);
    public static final Parameter PAR_NUM_ITER = new Parameter("Process.NumberOfIterations", 2);
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

    @Override
    public void run(Configuration config) {

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
        result.setWebtable(getData().getWebtable());
        result.setMatchingData(getData());
        result.setGoldStandard(getGoldStandard());
        result.setSimilarities(getSimilarities());

        // initialise logger
        setLogger(new MatchingLogger());
        getLogger().prepareLog();

        // reset candidate similarity in case this is not the first run for this
        // instance
        setSimilarities(new Similarities());

        String webtablePath = (String) config.getValue(PAR_WEBTABLE);

        getMatchingParameters().setMaxSpanningCells((Integer) config.getValue(PAR_SPANNING_CELL_THRESHOLD));
        getMatchingParameters().setTableType((de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.tableType) config.getValue(PAR_TABLE_TYPE));

        webtable = webtablePath;
        getData().loadWebTable(webtablePath, getRootTimer(), getMatchingParameters(), (de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.tableType) config.getValue(PAR_TABLE_TYPE));
        String webTableName = new File(webtablePath).getName();

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
        if (!getData().getWebtable().isHasKey()) {
            getLogger().logData("Skipping table no key column");
            return result;
        }
        getLogger().logData(String.format("\n key column: [%d] %s", getData().getWebtable().getKeyIndex(), getData().getWebtable().getKey().getHeader()));
        System.out.println(String.format("\n key column: [%d] %s", getData().getWebtable().getKeyIndex(), getData().getWebtable().getKey().getHeader()));

        boolean useContext = true;
        boolean WN = false;
        boolean dict = false;
        boolean propLabel = true;
        boolean propValue = true;
        boolean instValue = true;

        // start actual matching process
        candidateSelection.run(config);
        getSimilarities().setInitialCandidateSimilarity(candidateSelection.getInitialCandidateSimilarity());



        if (!useContext) {
            SecondLineClassMatcher classMatcher = new SecondLineClassMatcher(similarities, matchingParameters, rootTimer, goldStandard, logger);
            classMatcher.setCandidateSimilarity(getSimilarities().getInitialCandidateSimilarity());
            SimilarityMatrix<Table> classSimilarity = classMatcher.match(getData());
            classSimilarity.pruneWithNullEqualOrBelow(0.0);
            getSimilarities().setClassSimilarity(classSimilarity);
        } else {
            SimilarityMatrix<Table> classes = classMatching(config, getSimilarities().getInitialCandidateSimilarity());
            classes.pruneWithNullEqualOrBelow(0.0);
            getSimilarities().setClassSimilarity(classes);
        }

        SimilarityMatrix<Table> one2oneClasses = candidateSelection.mapClasses(getSimilarities().getClassSimilarity().copy(), new MatchingResult(), false);


        List<Table> chosenClass = new ArrayList<>();
        for (Table a : one2oneClasses.getFirstDimension()) {
            for (Table b : one2oneClasses.getMatches(a)) {
                chosenClass.add(b);
            }
        }

        if (useContext) {
            SecondLineClassMatcher classMatcher2 = new SecondLineClassMatcher(similarities, matchingParameters, rootTimer, goldStandard, logger);
            classMatcher2.setCandidateSimilarity(getSimilarities().getInitialCandidateSimilarity());
            SimilarityMatrix<Table> classSimilarity2 = classMatcher2.match(getData());
            classSimilarity2.pruneWithNullEqualOrBelow(0.0);
            SimilarityMatrix<Table> onlyMajority = candidateSelection.mapClasses(classSimilarity2, new MatchingResult(), false);
            for (Table a : onlyMajority.getFirstDimension()) {
                for (Table b : onlyMajority.getMatches(a)) {
                    if (!chosenClass.contains(b)) {
                        for (TableColumn c : getData().getWebtable().getColumns()) {
                            System.out.println("head: " + c.getHeader().toString().toLowerCase());
                            System.out.println("chosen: " + chosenClass.get(0).getHeader().replace(".csv.gz", "").toLowerCase());
                            if (c.getHeader().toString().toLowerCase().equals(chosenClass.get(0).getHeader().replace(".csv.gz", "").toLowerCase()) && !c.isKey()) {
                                System.out.println("same same");
                                System.out.println("key " + getData().getWebtable().getKeyIndex());
                                System.out.println(getData().getWebtable().getColumns().indexOf(c));
                                getData().getWebtable().setKey(c, getData().getWebtable().getColumns().indexOf(c));
                                System.out.println("key now " + getData().getWebtable().getKeyIndex());
                                candidateSelection.run(config);
                                getSimilarities().setInitialCandidateSimilarity(candidateSelection.getInitialCandidateSimilarity());

                                candidateRefinement.setInitialCandidateSimilarity(candidateSelection.getInitialCandidateSimilarity());
                                candidateRefinement.setClassSimilarity(getSimilarities().getClassSimilarity());
                                candidateRefinement.run(config);
                                SimilarityMatrix<TableRow> refinement = candidateRefinement.getCandidateSimilarity();
                                getSimilarities().setCandidateSimilarity(refinement);

                            }
                        }
                    }
                }
            }
        }

        candidateRefinement.setInitialCandidateSimilarity(getSimilarities().getInitialCandidateSimilarity());
        candidateRefinement.setClassSimilarity(getSimilarities().getClassSimilarity());
        candidateRefinement.run(config);
        SimilarityMatrix<TableRow> refinement = candidateRefinement.getCandidateSimilarity();


        if (refinement.getNumberOfNonZeroElements() == 0) {
            // stop here as we have no candidates
            getLogger().logData("No candidates, stopping!");
            getSimilarities().setClassSimilarity(new SparseSimilarityMatrix<Table>(0, 0));
            getSimilarities().setPropertySimilarity(new SparseSimilarityMatrix<TableColumn>(0, 0));
            getSimilarities().setCandidateSimilarity(new SparseSimilarityMatrix<TableRow>(0, 0));

            candidateSelection.mapInstances(getSimilarities().getCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), result, "");
            candidateSelection.mapProperties(getSimilarities().getPropertySimilarity().copy(), webTableName, true, result);
            result.setSimilarities(similarities);

        } else {

            getSimilarities().setInitialCandidateSimilarity(refinement);


            SimilarityMatrix<TableRow> candiBasedOnAbstracts = null;
            if ((double) config.getValue(PAR_ABSTRACT_WEIGHT) > 0.0) {
                candidateAbstractSelection.run(config);
                SimilarityMatrix<TableRow> firstAbstracts = candidateAbstractSelection.getCandidateAbstractSimilarity();

                StringSimilarityMeasure<TableRow> keyMeasure = new StringSimilarityMeasure<>();
                keyMeasure.setSimilarityFunction(new WebJaccardSimilarity());
                keyMeasure.setSetSimilarity(new MaxSimilarity<>());

                candiBasedOnAbstracts = new SparseSimilarityMatrix(firstAbstracts.getFirstDimension().size(), firstAbstracts.getSecondDimension().size());
            }

            SimilarityMatrix<TableRow> allCandis = null;
            if (candiBasedOnAbstracts != null) {
                CombineNonOverlapping nonOverlap = new CombineNonOverlapping();
                nonOverlap.setAggregationType(CombinationType.Sum);
                allCandis = nonOverlap.match(candiBasedOnAbstracts, candidateRefinement.getCandidateSimilarity());
                allCandis.normalize();

            } else {
                allCandis = candidateRefinement.getCandidateSimilarity();
            }

            getSimilarities().setCandidateSimilarity(allCandis);

            if(instValue) {
            
            valueBased.run(config);

            getSimilarities().setValueSimilarity(valueBased.getValueSimilarity());
            }
            else {
                getSimilarities().setValueSimilarity(new SparseSimilarityMatrix<TableCell>(0,0));
            }

            //start property matching!
            SimilarityMatrix<TableColumn> propertySim = null;

            if (WN) {
                addWn();
            }

            SimilarityMatrix<TableColumn> labelSimilarity = new SparseSimilarityMatrix<>(0, 0);

            if (propLabel) {
                getMatchers().getLabelBasedSchemaMatcher().setLabelSimilarity((SimilarityFunction<String>) config.getValue(PAR_LABEL_SIMILARITY));
                getMatchers().getLabelBasedSchemaMatcher().setSetSimilarity(new MaxSimilarity<String>());
                getMatchers().getLabelBasedSchemaMatcher().setBlocking(new IdentityBlocking<>());
                getMatchers().getLabelBasedSchemaMatcher().setAllowedClasses(chosenClass);
                labelSimilarity = getMatchers().getLabelBasedSchemaMatcher().match(getData());
            }

            dp.setValueSimilarity(getSimilarities().getValueSimilarity());
            dp.setCandidateSimilarity(getSimilarities().getCandidateSimilarity());
            dp.setNumCandidatesPerInstance((int) config.getValue(PAR_PROP_NUM_CANDIDATES));
            dp.setCandidateThreshold((double) config.getValue(PAR_PROP_CANDIDATE_THRESHOLD));
            dp.setValueThreshold((double) config.getValue(PAR_PROP_VALUE_THRESHOLD));
            dp.setNumVotesPerInstance((int) config.getValue(PAR_PROP_NUM_VOTES));

            LabelBasedSchemaMatcher lbs = new LabelBasedSchemaMatcher(similarities, matchingParameters, timer, goldStandard, logger);
            lbs.setLabelSimilarity((SimilarityFunction<String>) new AlwaysMatchSimilarityFunction());
            lbs.setBlocking(new TypeBasedBlocking<>(new TableColumnMatchingAdapter()));
            SimilarityMatrix<TableColumn> allLabelSimilarity = lbs.match(getData());

            dp.setLabelSimilarity(allLabelSimilarity);
            dp.setNumResults((int) config.getValue(PAR_PROP_NUM_RESULTS));
            dp.setFinalThreshold(0.0);

            SimilarityMatrix<TableColumn> props = dp.match(data);


            Set<String> allowedCols = new HashSet<>();
            for (Table t : chosenClass) {
                System.out.println("chosen classes: " + t);
                for (TableColumn c : t.getColumns()) {
                    allowedCols.add(c.getURI());
                }
            }

            for (String c : allowedCols) {
                System.out.println("allowed: " + c);
            }

            Canoniser propertyCanoniser = evaluationParameters.getEquivPropertyCanoniser();


            for (TableColumn c1 : props.getFirstDimension()) {
                for (TableColumn d : props.getMatches(c1)) {
                    if (!allowedCols.contains(d.getURI())) {
                        props.set(c1, d, null);
                        continue;
                    }
                    if (c1.getDataType() == TableColumn.ColumnDataType.numeric && c1.getColumnStatistic().getKurtosis() < 2) {
                        props.set(c1, d, null);
                        continue;
                    }
                    double value = props.get(c1, d);
                    if (d.getURI().contains("http://www.w3.org") || d.getURI().contains("http://purl.org") || d.getURI().contains("wikiPage")) {
                        props.set(c1, d, null);
                        continue;
                    }

                    String newUri = propertyCanoniser.canoniseResource(d.getURI().toString());
                    if (!newUri.equals(d.getURI().toString())) {
                        props.set(c1, d, null);
                        for (TableColumn check : getData().getDbpediaColSet()) {
                            if (check.getURI().toString().equals(newUri)) {
                                props.set(c1, check, value);
                            }
                        }
                    }
                }
            }

            labelSimilarity.pruneWithNullEqualOrBelow(0.0);

            for (TableColumn c1 : labelSimilarity.getFirstDimension()) {
                for (TableColumn d : labelSimilarity.getMatches(c1)) {
                    if (!allowedCols.contains(d.getURI())) {
                        System.out.println("not an allowed col label: " + d + " class " + d.getTable());
                        labelSimilarity.set(c1, d, null);
                        continue;
                    }
                    if (c1.getDataType() == TableColumn.ColumnDataType.numeric && c1.getColumnStatistic().getKurtosis() < 0) {
                        labelSimilarity.set(c1, d, null);
                        continue;
                    }
                    if (c1.getDataType() == TableColumn.ColumnDataType.numeric && d.getDataType() != TableColumn.ColumnDataType.numeric
                            || c1.getDataType() == TableColumn.ColumnDataType.date && d.getDataType() != TableColumn.ColumnDataType.date) {
                        labelSimilarity.set(c1, d, null);
                        continue;
                    }

                    double value = labelSimilarity.get(c1, d);
                    if (d.getURI().contains("http://www.w3.org") || d.getURI().contains("http://purl.org") || d.getURI().contains("wikiPage")) {
                        labelSimilarity.set(c1, d, null);
                    }
                    String newUri = propertyCanoniser.canoniseResource(d.getURI().toString());
                    if (!newUri.equals(d.getURI().toString())) {
                        for (TableColumn check : getData().getDbpediaColSet()) {
                            if (check.getURI().toString().equals(newUri)) {
                                labelSimilarity.set(c1, d, null);
                                labelSimilarity.set(c1, check, value);
                            }
                        }
                    }
                }
            }
            int size =0;
            Map<TableColumn, List<TableColumn>> possibleMatchesByCol = new HashMap<>();
            for (TableColumn c : getData().getWebtable().getColumns()) {
                if (c.isKey()) {
                    continue;
                }

                for (TableColumn d : props.getMatches(c)) {
                    if (!allowedCols.contains(d.getURI())) {
                        continue;
                    }
                    if (possibleMatchesByCol.containsKey(c)) {
                        size++;
                        possibleMatchesByCol.get(c).add(d);
                    } else {
                        List<TableColumn> x = new ArrayList<>();
                        x.add(d);
                        size++;
                        possibleMatchesByCol.put(c, x);
                    }
                }
                for (TableColumn d : labelSimilarity.getMatches(c)) {
                    if (!allowedCols.contains(d.getURI())) {
                        continue;
                    }
                    if (possibleMatchesByCol.containsKey(c)) {
                        size++;
                        possibleMatchesByCol.get(c).add(d);
                    } else {
                        List<TableColumn> x = new ArrayList<>();
                        size++;
                        x.add(d);
                        possibleMatchesByCol.put(c, x);
                    }
                }
            }

            SimilarityMatrix majority = new SparseSimilarityMatrix(possibleMatchesByCol.size(), getData().getDbpediaColSet().size());

            for (TableColumn c : possibleMatchesByCol.keySet()) {

                for (TableColumn d : possibleMatchesByCol.get(c)) {
                    double counter = 0;
                    if (props.get(c, d) != null) {
                        counter++;
                    }
                    if (labelSimilarity.get(c, d) != null) {
                        counter++;
                    }
                    majority.set(c, d, counter);
                    System.out.println("no. matches " + c + " - " + d + " - " + counter);
                }
            }

            SimilarityMatrix columnWidth = new SparseSimilarityMatrix(0, 0);
                    //new SparseSimilarityMatrix(getData().getWebtable().getColumns().size(), size);

            for (TableColumn c : possibleMatchesByCol.keySet()) {
                for (TableColumn d : possibleMatchesByCol.get(c)) {
                    if (c.getDataType() != d.getDataType()) {
                        continue;
                    }
//                if (!allowedClasses.contains(d.getTable().getHeader().split("\\.")[0].toLowerCase())) {
//                    continue;
//                }

                    double first = c.getColumnStatistic().getAverageValueLength();
                    double second = d.getColumnStatistic().getAverageValueLength();

                    //             System.out.println("c: " + c.getHeader() + " d: " + d.getURI() + " class: " + d.getTable().getHeader() + " first: " + first + " second " + second);
                    if(first == 0.0 || second == 0.0) {
                        continue;
                    }
                    if (first >= second) {
                        if (first == 0.0) {
                            continue;
                        } else {
                            columnWidth.set(c, d, second / first);
                        }
                    } else {
                        if (second == 0.0) {
                            continue;
                        } else {
                            columnWidth.set(c, d, first / second);

                        }
                    }
                }
            }
            columnWidth.normalize();
            
            if(!propValue) {
                props = new SparseSimilarityMatrix<>(0,0);
            }
            
            Map<SimilarityMatrix, MatrixStats> stats = new HashMap<>();
            props.setName("props");
            labelSimilarity.setName("label");
            columnWidth.setName("width");
            stats.put(labelSimilarity, new MatrixStats(labelSimilarity, getData(), possibleMatchesByCol));
            stats.put(props, new MatrixStats(props, getData(), possibleMatchesByCol));
            stats.put(columnWidth, new MatrixStats(columnWidth, getData(), possibleMatchesByCol));

            //stats.put(classCocoSim, new MatrixStats(classCocoSim, getData(), possibleMatchesByCol));

            CombineNonOverlapping nonOverlap = new CombineNonOverlapping();
            nonOverlap.setAggregationType(CombinationType.Sum);

            SimilarityMatrix x = props.copy();
            if (stats.get(props).getMean() != Double.NaN) {
                x.multiplyScalar(stats.get(props).getMean());
            }
            
            SimilarityMatrix y = labelSimilarity.copy();
            y.multiplyScalar(stats.get(labelSimilarity).getMean());
            SimilarityMatrix<TableColumn> allCandidates = nonOverlap.match(x, y);
            allCandidates.pruneWithNullEqualOrBelow(0.0);
            propertySim = allCandidates;
            
            x = columnWidth.copy();
            if (stats.get(columnWidth).getMean() != Double.NaN) {
                x.multiplyScalar(stats.get(columnWidth).getMean());
                allCandidates = nonOverlap.match(propertySim,x);
                allCandidates.pruneWithNullEqualOrBelow(0.0);
            propertySim = allCandidates;
            }

            MatchingResult r1 = new MatchingResult();
            MatchingResult r2 = new MatchingResult();
            MatchingResult r5 = new MatchingResult();

            candidateSelection.mapProperties(props, webtable, false, r1);
            candidateSelection.mapProperties(labelSimilarity, webtable, false, r2);
            candidateSelection.mapProperties(columnWidth, webtable, false, r5);

            System.out.println("correl comp prop value" + webTableName + "\t" + stats.get(props).getNormalizedHerinfahl() + "\t" + stats.get(props).getHerfindahlIndex() + "\t" + stats.get(props).getMean() + "\t" + stats.get(props).getStad()
                    + "\t" + stats.get(props).getLineBasedAverage() + "\t" + stats.get(props).getLC() + "\t" + r1.getEvaluation().getPropertyResult().getPrecision() + "\t" + r1.getEvaluation().getPropertyResult().getRecall());
            System.out.println("correl comp prop label" + webTableName + "\t" + stats.get(labelSimilarity).getNormalizedHerinfahl() + "\t" + stats.get(labelSimilarity).getHerfindahlIndex() + "\t" + stats.get(labelSimilarity).getMean() + "\t" + stats.get(labelSimilarity).getStad()
                    + "\t" + stats.get(labelSimilarity).getLineBasedAverage() + "\t" + stats.get(labelSimilarity).getLC() + "\t" + r2.getEvaluation().getPropertyResult().getPrecision() + "\t" + r2.getEvaluation().getPropertyResult().getRecall());

            System.out.println("correl comp prop width" + webTableName + "\t" + stats.get(columnWidth).getNormalizedHerinfahl() + "\t" + stats.get(columnWidth).getHerfindahlIndex() + "\t" + stats.get(columnWidth).getMean() + "\t" + stats.get(columnWidth).getStad()
                    + "\t" + stats.get(columnWidth).getLineBasedAverage() + "\t" + stats.get(columnWidth).getLC() + "\t" + r5.getEvaluation().getPropertyResult().getPrecision() + "\t" + r5.getEvaluation().getPropertyResult().getRecall());

            
            getSimilarities().setPropertySimilarity(propertySim);
            

            SimilarityMatrix<TableCell> inst = Matcher.multiplyParentSimilarity(propertySim, getSimilarities().getValueSimilarity(), new TableColumnToCellHierarchyAdapter());

            Aggregate<TableRow, TableCell> a = new Aggregate<>();
            a.setAggregationType(AggregationType.Sum);
            SimilarityMatrix<TableRow> propWeightedInstanceCand = a.match(allCandis, inst, new TableRowToCellHierarchyAdapter());
            SimilarityMatrix<TableRow> values = propWeightedInstanceCand;
            values.normalize();

            if(!instValue) {
                values = new SparseSimilarityMatrix<>(0,0);
            }
            
            Map<TableRow, List<TableRow>> possibleMatchesByRow = new HashMap<>();
            Map<String, TableRow> rowToUri = new HashMap<>();


            for (TableRow c : getData().getWebtableRowSet()) {

                for (TableRow d : allCandis.getMatches(c)) {
                    if (possibleMatchesByRow.containsKey(c)) {
                        possibleMatchesByRow.get(c).add(d);
                    } else {
                        List<TableRow> tr = new ArrayList<>();
                        tr.add(d);
                        possibleMatchesByRow.put(c, tr);
                    }
                }
                for (TableRow d : values.getMatches(c)) {
                    if (possibleMatchesByRow.containsKey(c)) {
                        possibleMatchesByRow.get(c).add(d);
                    } else {
                        List<TableRow> tr = new ArrayList<>();
                        tr.add(d);
                        possibleMatchesByRow.put(c, tr);
                    }
                }
                if (candidateAbstractSelection.getCandidateAbstractSimilarity() != null) {

                    for (TableRow d : candidateAbstractSelection.getCandidateAbstractSimilarity().getMatches(c)) {
                        if (possibleMatchesByRow.containsKey(c)) {
                            possibleMatchesByRow.get(c).add(d);
                        } else {
                            List<TableRow> tr = new ArrayList<>();
                            tr.add(d);
                            possibleMatchesByRow.put(c, tr);
                        }
                    }
                }

                Map<String, Integer> duplicateDetection = new HashMap<>();



                if (!possibleMatchesByRow.isEmpty() && possibleMatchesByRow.containsKey(c)) {
                    for (TableRow d : possibleMatchesByRow.get(c)) {
                        //System.out.println("d: " + d);
                        if (!duplicateDetection.containsKey(d.getURI().toString())) {
                            duplicateDetection.put(d.getURI().toString(), 0);
                        }
                        int counter = 0;
                        double sum = 0.0;

                        if (allCandis.get(c, d) != null) {
                            counter++;
                            sum += allCandis.get(c, d);
                        }
                        if (values.get(c, d) != null) {
                            counter++;
                            sum += values.get(c, d);
                        }

                        if (candidateAbstractSelection.getCandidateAbstractSimilarity() != null) {
                            if (candidateAbstractSelection.getCandidateAbstractSimilarity().get(c, d) != null) {
                                counter++;
                                sum += candidateAbstractSelection.getCandidateAbstractSimilarity().get(c, d);
                            }
                        }
                        counter *= sum;

                        //System.out.println("counter " + counter);

                        if (counter >= duplicateDetection.get(d.getURI().toString())) {
                            duplicateDetection.put(d.getURI().toString(), counter);
                            rowToUri.put(d.getURI().toString(), d);
                        }
                    }
                }
            }

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

            //no value matching!
            //values = new SparseSimilarityMatrix<>(0, 0);


            allCandis.setName("inst label");
            values.setName("inst values");

            Map<SimilarityMatrix, MatrixStats> instanceStats = new HashMap<>();
            instanceStats.put(allCandis, new MatrixStats(allCandis, getData(), possibleMatchesByRow));
            instanceStats.put(values, new MatrixStats(values, getData(), possibleMatchesByRow));

            if (candidateAbstractSelection.getCandidateAbstractSimilarity() != null) {
                candidateAbstractSelection.getCandidateAbstractSimilarity().setName("inst abs");
                instanceStats.put(candidateAbstractSelection.getCandidateAbstractSimilarity(), new MatrixStats(candidateAbstractSelection.getCandidateAbstractSimilarity(), getData(), possibleMatchesByRow));
            }

            //currently no correlation analysis
            r1 = new MatchingResult();
            r2 = new MatchingResult();

            candidateSelection.mapInstances(allCandis, false, r1, webtable);
            candidateSelection.mapInstances(values, false, r2, webtable);

            System.out.println("correl comp inst label" + webTableName + "\t" + instanceStats.get(allCandis).getNormalizedHerinfahl() + "\t" + instanceStats.get(allCandis).getHerfindahlIndex() + "\t" + instanceStats.get(allCandis).getMean() + "\t" + instanceStats.get(allCandis).getStad()
                    + "\t" + r1.getEvaluation().getInstanceResult().getPrecision() + "\t" + r1.getEvaluation().getInstanceResult().getRecall());
            System.out.println("correl comp inst value" + webTableName + "\t" + instanceStats.get(values).getNormalizedHerinfahl() + "\t" + instanceStats.get(values).getHerfindahlIndex() + "\t" + instanceStats.get(values).getMean() + "\t" + instanceStats.get(values).getStad()
                    + "\t" + r2.getEvaluation().getInstanceResult().getPrecision() + "\t" + r2.getEvaluation().getInstanceResult().getRecall());


            SimilarityMatrix<TableRow> combinedWithWeight;

            nonOverlap = new CombineNonOverlapping();
            nonOverlap.setAggregationType(CombinationType.Sum);

            SimilarityMatrix x1 = allCandis.copy();
            x1.multiplyScalar(instanceStats.get(allCandis).getHerfindahlIndex());
            SimilarityMatrix y1 = values.copy();
            y1.multiplyScalar(instanceStats.get(values).getHerfindahlIndex());
            combinedWithWeight = nonOverlap.match(x1, y1);

            SimilarityMatrix<TableRow> abstractMatrix = null;
            if ((double) config.getValue(PAR_ABSTRACT_WEIGHT) > 0.0) {

                MatchingResult r3 = new MatchingResult();
                candidateSelection.mapInstances(candidateAbstractSelection.getCandidateAbstractSimilarity(), false, r3, webtable);
                System.out.println("correl comp inst abs" + webTableName + "\t" + instanceStats.get(candidateAbstractSelection.getCandidateAbstractSimilarity()).getNormalizedHerinfahl() + "\t" + instanceStats.get(candidateAbstractSelection.getCandidateAbstractSimilarity()).getHerfindahlIndex() + "\t" + instanceStats.get(candidateAbstractSelection.getCandidateAbstractSimilarity()).getMean() + "\t" + instanceStats.get(candidateAbstractSelection.getCandidateAbstractSimilarity()).getStad()
                        + "\t" + r3.getEvaluation().getInstanceResult().getPrecision() + "\t" + r3.getEvaluation().getInstanceResult().getRecall());

                
                abstractMatrix = candidateAbstractSelection.getCandidateAbstractSimilarity();
                x1 = abstractMatrix.copy();
                //x1.multiplyScalar(instanceStats.get(candidateAbstractSelection.getCandidateAbstractSimilarity()).getHerfindahlIndex());
                x1.multiplyScalar((double) config.getValue(PAR_ABSTRACT_WEIGHT));
                y1 = combinedWithWeight.copy();
                //usually not!
                y1.multiplyScalar(1.0- (double) config.getValue(PAR_ABSTRACT_WEIGHT));
                combinedWithWeight = nonOverlap.match(x1, y1);
            }

            for (TableRow c1 : combinedWithWeight.getFirstDimension()) {
                for (TableRow d : combinedWithWeight.getMatches(c1)) {
                    if (rowToUri.containsKey(d.getURI().toString()) && !rowToUri.get(d.getURI().toString()).equals(d)) {
                        combinedWithWeight.set(c1, d, null);
                    }
                }
            }
            getSimilarities().setCandidateSimilarity(combinedWithWeight);


            //see later!
//            TopKCandidates top = new TopKCandidates();
//            SimilarityMatrix<TableRow> topMatrix = top.match(getSimilarities().getCandidateSimilarity(), 3);
//            List<TableRow> dbInstToCheck = new ArrayList<>();
//            List<TableRow> wtRowsToCheck = new ArrayList<>();
//            for (TableRow r : topMatrix.getFirstDimension()) {
//                for (TableRow d : topMatrix.getMatches(r)) {
//                    dbInstToCheck.add(d);
//                }
//                if (topMatrix.getMatches(r).size() > 0) {
//                    wtRowsToCheck.add(r);
//                }
//            }
//
//            for (TableColumn d : getSimilarities().getPropertySimilarity().getSecondDimension()) {
//                double size = 0, notEmpty = 0;
//                for (TableRow r : dbInstToCheck) {
//                    if (d.getValues().get(r.getRowIndex()) instanceof String) {
//                        String s = (String) d.getValues().get(r.getRowIndex());
//                        if (!s.isEmpty()) {
//                            notEmpty++;
//                            size += s.length();
//                        }
//                    }
//                    if (d.getValues().get(r.getRowIndex()) instanceof Double) {
//                        String s = String.valueOf(d.getValues().get(r.getRowIndex()));
//                        if (!s.isEmpty()) {
//                            notEmpty++;
//                            size += s.length();
//                        }
//                    }
//                }
//                
//            }

//            double mappedInstances;
//            mappedInstances = (double) candidateRefinement.getNumberOfInstanceMappings() / (double) getData().getWebtableRowSet().size();
//
//            if (mappedInstances < (Double) config.getValue(PAR_MAPPED_RATIO_FILTER)) {
//                getLogger().logData("Not enough consistent candidates, cancelling!");
//                // not enough mappings with the selected class, we do not map this table at all
//                result.setWebtable(getData().getWebtable());
//                result.setMatchingData(getData());
//                result.setGoldStandard(getGoldStandard());
//                result.setSimilarities(getSimilarities());
//                getSimilarities().setClassSimilarity(new SparseSimilarityMatrix<Table>(0, 0));
//                getSimilarities().setPropertySimilarity(new SparseSimilarityMatrix<TableColumn>(0, 0));
//                getSimilarities().setCandidateSimilarity(new SparseSimilarityMatrix<TableRow>(0, 0));
//            }

            candidateRefinement.mapInstances(getSimilarities().getCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), result, "");
            candidateSelection.mapProperties(getSimilarities().getPropertySimilarity().copy(), webTableName, true, result);
            candidateSelection.mapClasses(getSimilarities().getClassSimilarity().copy(), result, false);

            if ((Boolean) config.getValue(de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.PAR_EVALUATE)) {
                result.setWebtable(getData().getWebtable());
                result.setMatchingData(getData());
                result.setGoldStandard(getGoldStandard());
                result.setSimilarities(getSimilarities());

                result.getEvaluation().setInstanceBaseLine(candidateSelection.getInitialInstanceResult());
                result.getEvaluation().setMaxCorrectCandidates(candidateSelection.getMaxCorrectCanddiates());
                result.getEvaluation().setNumCandidates(getSimilarities().getInitialCandidateSimilarity().getNumberOfNonZeroElements());

                analyseResult(result);

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

    public void addDict() {
        File f = new File("dict.csv");
        try {
            Map<String, List<String>> additionalNames = new HashMap<>();
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line = read.readLine();
            while (line != null) {
                String[] content = null;
                try {
                    content = line.split("\t");
                } catch (Exception e1) {
                    e1.printStackTrace();
                    System.out.println("no tab! " + line);
                }
                List<String> names = new ArrayList<>();
                for (int i = 1; i < content.length; i++) {
                    if (!content[i].isEmpty()) {
                        names.add(content[i]);
                    }
                }
                if (content[0].contains("dbpedia.org/ontology") && !content[0].contains("wikiPage")) {
                    additionalNames.put(content[0], names);
                }
                line = read.readLine();
            }
            System.out.println(additionalNames.size());

            for (TableColumn d : getData().getDbpediaColSet()) {
                if (additionalNames.containsKey(d.getURI())) {
                    additionalNames.get(d.getURI()).add(d.getHeader().toString());
                    d.setHeaderList(additionalNames.get(d.getURI()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addWn() {
        Dictionary dictionary = null;
        try {
            JWNL.initialize(new FileInputStream("properties.xml"));
            dictionary = Dictionary.getInstance();

        } catch (Exception e) {
            getLogger().logData("Could not initiate Wordnet.");
        }

        TableColumnMatchingAdapter adap = new TableColumnMatchingAdapter();

        for (TableColumn c : getData().getWebtable().getColumns()) {
            try {
                IndexWord indexWord = dictionary.getIndexWord(POS.NOUN, adap.getLabel(c).toString());
                PointerTargetTree hypernyms = PointerUtils.getInstance().getHypernymTree(indexWord.getSense(1));

                List<String> hypernymsToAdd = new ArrayList<>();
                int i = 0;
                for (Object node : hypernyms.toList()) {
                    if (node instanceof PointerTargetNodeList) {
                        PointerTargetNodeList list = (PointerTargetNodeList) node;
                        for (Object x : list) {
                            PointerTargetNode realNode = (PointerTargetNode) x;
                            for (Word w : realNode.getSynset().getWords()) {
                                if (i < 5) {
                                    System.out.println(" hypernym word: " + c.getHeader() + " added: " + w.getLemma());
                                    hypernymsToAdd.add(w.getLemma());
                                    i++;
                                }
                            }
                        }
                    }
                }
                if (!hypernymsToAdd.isEmpty()) {
                    hypernymsToAdd.add(adap.getLabel(c).toString());
                    c.setHeaderList(hypernymsToAdd);
                }


                PointerTargetTree hyponymes = PointerUtils.getInstance().getHyponymTree(indexWord.getSense(1));

                List<String> hyponymsToAdd = new ArrayList<>();
                i = 0;
                for (Object node : hyponymes.toList()) {
                    if (node instanceof PointerTargetNodeList) {
                        PointerTargetNodeList list = (PointerTargetNodeList) node;
                        for (Object x : list) {
                            PointerTargetNode realNode = (PointerTargetNode) x;
                            for (Word w : realNode.getSynset().getWords()) {
                                if (i < 5) {
                                    System.out.println(" hyponym word: " + c.getHeader() + " added: " + w.getLemma());
                                    hyponymsToAdd.add(w.getLemma());
                                    i++;
                                }
                            }
                        }
                    }
                }
                if (!hyponymsToAdd.isEmpty()) {
                    hyponymsToAdd.add(adap.getLabel(c).toString());
                    c.setHeaderList(hyponymsToAdd);
                }

            } catch (Exception e) {
                continue;
            }
        }
    }

    @Override
    public WebTableToDBpediaComponentProcess clone() {
        WebTableToDBpediaComponentProcess wp = new WebTableToDBpediaComponentProcess();
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

    public WebTableToDBpediaComponentProcess() {
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

    private SimilarityMatrix<Table> classMatching(Configuration config, SimilarityMatrix<TableRow> instSim) {
        ContextComponent context = new ContextComponent();
        context.initialise(matchers, data, evaluationParameters, goldStandard, logger, matchingParameters, similarities, rootTimer);
        context.run(config);
        
//         SimilarityMatrix<Table> pageTitle = new SparseSimilarityMatrix<>(0,0);
        
        SimilarityMatrix<Table> pageTitle = context.getPageTitleSimilarity();
        pageTitle.normalize();
         
//        SimilarityMatrix<Table> contextSim = new SparseSimilarityMatrix<>(0,0);
//        SimilarityMatrix<Table> content = new SparseSimilarityMatrix<>(0,0);
//        SimilarityMatrix<Table> colNames = new SparseSimilarityMatrix<>(0,0);
        SimilarityMatrix<Table> contextSim = context.getContexteSimilarity();        
        contextSim.normalize();
        SimilarityMatrix<Table> content = context.getContentSimilarity();
        content.normalize();
        SimilarityMatrix<Table> colNames = context.getColumnNamesSimilarity();
        colNames.normalize();
//        SimilarityMatrix<Table> url = new SparseSimilarityMatrix<>(0,0);
        SimilarityMatrix<Table> url = context.getUrlSimilarity();


        
        
        //getSimilarities().setClassSimilarity(context.getClassContextMatrix());

        System.out.println(data.getWebtable() + " url: " + data.getWebtable().getSource());

        SecondLineClassMatcher classMatcher = new SecondLineClassMatcher(similarities, matchingParameters, rootTimer, goldStandard, logger);
        classMatcher.setCandidateSimilarity(instSim);
        SimilarityMatrix<Table> classSimilarity = classMatcher.match(getData());
        classSimilarity.pruneWithNullEqualOrBelow(0.0);
        //classSimilarity = new SparseSimilarityMatrix<>(0,0);

        List<Table> possibleMatchesByCol = new ArrayList<>();

        for (Table d : pageTitle.getMatches(getData().getWebtable())) {
            possibleMatchesByCol.add(d);
        }
        for (Table d : contextSim.getMatches(getData().getWebtable())) {
            possibleMatchesByCol.add(d);
        }
        for (Table d : content.getMatches(getData().getWebtable())) {
            possibleMatchesByCol.add(d);
        }
        for (Table d : colNames.getMatches(getData().getWebtable())) {
            System.out.println("col matches " + d);
            possibleMatchesByCol.add(d);
        }
        for (Table d : url.getMatches(getData().getWebtable())) {
            possibleMatchesByCol.add(d);
        }
        for (Table d : classSimilarity.getMatches(getData().getWebtable())) {
            possibleMatchesByCol.add(d);
        }

        SimilarityMatrix majority = new SparseSimilarityMatrix(1, possibleMatchesByCol.size());
        for (Table d : possibleMatchesByCol) {
            double counter = 0;
            if (pageTitle.getMatches(getData().getWebtable()).contains(d)) {
                System.out.println("TITLE: " + d + pageTitle.get(getData().getWebtable(), d));
                counter++;
            }
            if (contextSim.getMatches(getData().getWebtable()).contains(d)) {
                System.out.println("CONTEXT: " + d + contextSim.get(getData().getWebtable(), d));
                counter++;
            }
            if (content.getMatches(getData().getWebtable()).contains(d)) {
                System.out.println("CONTENT: " + d + content.get(getData().getWebtable(), d));
                counter++;
            }
            if (colNames.getMatches(getData().getWebtable()).contains(d)) {
                System.out.println("COL: " + d + colNames.get(getData().getWebtable(), d));
                counter++;
            }
            if (classSimilarity.getMatches(getData().getWebtable()).contains(d)) {
                System.out.println("CLASS FOUND: " + d + classSimilarity.get(getData().getWebtable(), d));
                counter++;
            }
            if (url.getMatches(getData().getWebtable()).contains(d)) {
                System.out.println("URI: " + d + url.get(getData().getWebtable(), d));
                counter++;
            }
            System.out.println("COUNTER: " + d.getHeader() + " " + counter );
            majority.set(getData().getWebtable(), d, counter);
        }

        pageTitle.setName("page title");
        content.setName("content");
        contextSim.setName("context");
        colNames.setName("col names");
        classSimilarity.setName("class");
        url.setName("url");

        Map<SimilarityMatrix, MatrixStats> stats = new HashMap<>();
        stats.put(pageTitle, new MatrixStats(pageTitle, getData(), possibleMatchesByCol));
        stats.put(content, new MatrixStats(content, getData(), possibleMatchesByCol));
        stats.put(contextSim, new MatrixStats(contextSim, getData(), possibleMatchesByCol));
        stats.put(colNames, new MatrixStats(colNames, getData(), possibleMatchesByCol));
        stats.put(classSimilarity, new MatrixStats(classSimilarity, getData(), possibleMatchesByCol));
        stats.put(url, new MatrixStats(url, getData(), possibleMatchesByCol));

        CombineNonOverlapping nonOverlap = new CombineNonOverlapping();
        nonOverlap.setAggregationType(CombinationType.Sum);
        SimilarityMatrix x = pageTitle.copy();
        if (stats.get(pageTitle).getHerfindahlIndex() != Double.NaN) {
            x.multiplyScalar(stats.get(pageTitle).getHerfindahlIndex());
        }
        SimilarityMatrix y = content.copy();
        y.multiplyScalar(stats.get(content).getHerfindahlIndex());
        SimilarityMatrix<Table> first = nonOverlap.match(x, y);

        x = contextSim.copy();
        if (stats.get(contextSim).getHerfindahlIndex() != Double.NaN) {
            x.multiplyScalar(stats.get(contextSim).getHerfindahlIndex());
        }
        y = colNames.copy();
        y.multiplyScalar(stats.get(colNames).getHerfindahlIndex());
        SimilarityMatrix<TableColumn> second = nonOverlap.match(x, y);

        x = first.copy();
        y = second.copy();
        SimilarityMatrix<Table> third = nonOverlap.match(x, y);

        x = third.copy();
        y = url.copy();
        y.multiplyScalar(stats.get(url).getHerfindahlIndex());
        SimilarityMatrix<Table> forth = nonOverlap.match(x, y);

        x = forth.copy();
        y = classSimilarity.copy();
        y.multiplyScalar(stats.get(classSimilarity).getHerfindahlIndex());
        SimilarityMatrix<Table> combinedWithWeight = nonOverlap.match(x, y);

        MatchingResult r1 = new MatchingResult();
        MatchingResult r2 = new MatchingResult();
        MatchingResult r3 = new MatchingResult();
        MatchingResult r4 = new MatchingResult();
        MatchingResult r5 = new MatchingResult();
        MatchingResult r6 = new MatchingResult();

        candidateSelection.mapClasses(pageTitle, r1, false);
        candidateSelection.mapClasses(content, r2, false);
        candidateSelection.mapClasses(contextSim, r3, false);
        candidateSelection.mapClasses(colNames, r4, false);
        candidateSelection.mapClasses(classSimilarity, r5, false);
        candidateSelection.mapClasses(url, r6, false);


        System.out.println("correl comp class title" + getData().getWebtable() + "\t" + stats.get(pageTitle).getNormalizedHerinfahl() + "\t" + stats.get(pageTitle).getHerfindahlIndex() + "\t" + stats.get(pageTitle).getMean() + "\t" + stats.get(pageTitle).getStad()
                + "\t" + r1.getEvaluation().getClassResult().getPrecision() + "\t" + r1.getEvaluation().getClassResult().getRecall());
        System.out.println("correl comp class content" + getData().getWebtable() + "\t" + stats.get(content).getNormalizedHerinfahl() + "\t" + stats.get(content).getHerfindahlIndex() + "\t" + stats.get(content).getMean() + "\t" + stats.get(content).getStad()
                + "\t" + r2.getEvaluation().getClassResult().getPrecision() + "\t" + r2.getEvaluation().getClassResult().getRecall());
        System.out.println("correl comp class cotext" + getData().getWebtable() + "\t" + stats.get(contextSim).getNormalizedHerinfahl() + "\t" + stats.get(contextSim).getHerfindahlIndex() + "\t" + stats.get(contextSim).getMean() + "\t" + stats.get(contextSim).getStad()
                + "\t" + r3.getEvaluation().getClassResult().getPrecision() + "\t" + r3.getEvaluation().getClassResult().getRecall());
        System.out.println("correl comp class col names" + getData().getWebtable() + "\t" + stats.get(colNames).getNormalizedHerinfahl() + "\t" + stats.get(colNames).getHerfindahlIndex() + "\t" + stats.get(colNames).getMean() + "\t" + stats.get(colNames).getStad()
                + "\t" + r4.getEvaluation().getClassResult().getPrecision() + "\t" + r4.getEvaluation().getClassResult().getRecall());
        System.out.println("correl comp class majo" + getData().getWebtable() + "\t" + stats.get(classSimilarity).getNormalizedHerinfahl() + "\t" + stats.get(classSimilarity).getHerfindahlIndex() + "\t" + stats.get(classSimilarity).getMean() + "\t" + stats.get(classSimilarity).getStad()
                + "\t" + r5.getEvaluation().getClassResult().getPrecision() + "\t" + r5.getEvaluation().getClassResult().getRecall());

        System.out.println("correl comp class url" + getData().getWebtable() + "\t" + stats.get(url).getNormalizedHerinfahl() + "\t" + stats.get(url).getHerfindahlIndex() + "\t" + stats.get(url).getMean() + "\t" + stats.get(url).getStad()
                + "\t" + r6.getEvaluation().getClassResult().getPrecision() + "\t" + r6.getEvaluation().getClassResult().getRecall());

        nonOverlap.setAggregationType(CombinationType.Multiply);
        SimilarityMatrix<Table> combinedWithMajority = nonOverlap.match(majority, combinedWithWeight);

        for (Table t : combinedWithMajority.getFirstDimension()) {
            System.out.println("CLASS: " + t.getHeader() + " -- " + getGoldStandard().getClassGoldStandard().get(t.getHeader().replace(".json", "")));
            for (Table s : combinedWithMajority.getMatches(t)) {
                System.out.println("CLASS: " + s + " sim " + combinedWithMajority.get(t, s));
            }
        }
        //return combinedWithWeight;
        return combinedWithMajority;
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
}
