package de.dwslab.T2K.matching.dbpedia.algorithm;

import com.google.common.primitives.Doubles;
import com.wcohen.ss.Jaccard;
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
import de.dwslab.T2K.matching.dbpedia.matchers.instance.IndirectInstanceMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.IndirectInstanceMatcherMapped;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.DuplicateBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.IndirectSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.IndirectSchemaMatcherMapped;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.LabelBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.SecondLineClassMatcher;
import de.dwslab.T2K.matching.dbpedia.model.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingPair;
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
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowToCellHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.context.StopWordRemover;
import de.dwslab.T2K.matching.dbpedia.properties.Canoniser;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
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
import de.dwslab.T2K.similarity.functions.string.GeneralisedStringJaccard;
import de.dwslab.T2K.similarity.functions.string.JaccardSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.timer.Timer;
import de.uni_mannheim.informatik.dws.t2k.normalisation.StringNormalizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.commons.math3.stat.inference.TTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

    private IIndex tableIndex;

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
    private InterTableComponent itc;

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

        itc = new InterTableComponent();
        itc.setTableIndex(tableIndex);

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

        //TODO: context currently not used
        boolean useContext = false;
        boolean WN = false;
        boolean dict = false;
        boolean propLabel = true;
        boolean propValue = true;
        boolean instValue = true;
        boolean freq = false;
        boolean itemset = false;
        boolean indirectRecall = false;
        boolean indirectPrecision = false;
        boolean instaceIndirect = false;
        Map<Table, SimilarityMatrix<TableRow>> mapped = null;

        // start actual matching process
        candidateSelection.run(config);

        getSimilarities().setInitialCandidateSimilarity(candidateSelection.getInitialCandidateSimilarity());
        System.out.println("cand sel: " + getData().getWebtable().getHeader() + " - " +candidateSelection.getInitialCandidateSimilarity().getNumberOfNonZeroElements() + " - " + getData().getWebtableRowSet().size());

        //try out if it makes sense to perform the T2T here -> could have a strong influence on the class decision etc.
//        candidateSelection.mapInstances(getSimilarities().getCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), result, "");
//        itc.setNumTabes(1000);
//        itc.setParams(matchingParameters);
//        itc.setParentTimer(rootTimer);
//        mapped = itc.computeCandidates(data.getWebtableRowSet(), result);
//        System.out.println("itc ready " + mapped.size());
//
//        IndirectInstanceMatcher iim = new IndirectInstanceMatcher();
//        SimilarityMatrix indirectMappings = iim.computeIndirectMappings(matchingParameters, rootTimer, data, config, result, goldStandard, matchers, logger, evaluationParameters, mapped, itc);
//
//        CombineNonOverlapping comb = new CombineNonOverlapping();
//        comb.setAggregationType(CombinationType.Sum);
//        SimilarityMatrix<TableRow> combinedBoth = comb.match(similarities.getInitialCandidateSimilarity(), indirectMappings);
//        getSimilarities().setInitialCandidateSimilarity(combinedBoth);
//        itc.setNumTabes(1000);
//        itc.setParams(matchingParameters);
//        itc.setParentTimer(rootTimer);
//        candidateSelection.mapInstances(getSimilarities().getInitialCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), result, "");
//        mapped = itc.computeCandidates(getData().getWebtableRowSet(), result);
//        Map<String, Integer> sortedClasses = sortByValue(itc.getClasses());
//        Map<String, Integer> sortedClassesWithOverlap = sortByValue(itc.getClasses());
//        for (String s : sortedClasses.keySet()) {
//            System.out.println("class indirect: " + s + " - " + sortedClasses.get(s));
//            System.out.println("class indirect overlap: " + s + " - " + sortedClassesWithOverlap.get(s));
//        }
//        String goldURI = "";
//        if (goldStandard.getClassGoldStandard().get(data.getWebtable().getHeader().replace(".json", "")) != null) {
//            goldURI = goldStandard.getClassGoldStandard().get(data.getWebtable().getHeader().replace(".json", "")).get(0).toString();
//        }
        List<SimilarityMatrix<Table>> allClassMatrices = null;
        if (!useContext) {
            SecondLineClassMatcher classMatcher = new SecondLineClassMatcher(similarities, matchingParameters, rootTimer, goldStandard, logger);
            classMatcher.setCandidateSimilarity(getSimilarities().getInitialCandidateSimilarity());
            SimilarityMatrix<Table> classSimilarity = classMatcher.match(getData());
            classSimilarity.pruneWithNullEqualOrBelow(0.0);
            getSimilarities().setClassSimilarity(classSimilarity);
        } else {
            allClassMatrices = classMatching(config, getSimilarities().getInitialCandidateSimilarity());
            SimilarityMatrix<Table> classes = allClassMatrices.get(0);
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
                            if (chosenClass.size() > 0) {
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
//            getSimilarities().setPropertySimilarity(new SparseSimilarityMatrix<TableColumn>(0, 0));
//            result.setWebtable(getData().getWebtable());
//            result.setMatchingData(getData());
//            result.setGoldStandard(getGoldStandard());
//            result.setSimilarities(getSimilarities());
//            candidateSelection.mapInstances(getSimilarities().getInitialCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), result, "");
//            candidateSelection.mapProperties(getSimilarities().getPropertySimilarity().copy(), webTableName, true, result);
//            candidateSelection.mapClasses(getSimilarities().getClassSimilarity().copy(), result, false);
//            result.getEvaluation().setInstanceBaseLine(candidateSelection.getInitialInstanceResult());
//            //  result.getEvaluation().setMaxCorrectCandidates(candidateSelection.getMaxCorrectCanddiates());
//            result.getEvaluation().setMaxCorrectCandidates(0);
//            result.getEvaluation().setNumCandidates(getSimilarities().getInitialCandidateSimilarity().getNumberOfNonZeroElements());
//
//            return result;

        }

        candidateRefinement.setInitialCandidateSimilarity(getSimilarities().getInitialCandidateSimilarity());
        candidateRefinement.setClassSimilarity(getSimilarities().getClassSimilarity());
        candidateRefinement.run(config);
        SimilarityMatrix<TableRow> refinement = candidateRefinement.getCandidateSimilarity();

        System.out.println("cand ref: " + getData().getWebtable().getHeader()+ " - " + candidateRefinement.getCandidateSimilarity().getNumberOfNonZeroElements()+ " - " + getData().getWebtableRowSet().size());

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

            if (instValue) {

                valueBased.run(config);

                getSimilarities().setValueSimilarity(valueBased.getValueSimilarity());
            } else {
                getSimilarities().setValueSimilarity(new SparseSimilarityMatrix<TableCell>(0, 0));
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

            Canoniser propertyCanoniser = evaluationParameters.getEquivPropertyCanoniser();

            Set<String> allowedCols = new HashSet<>();
            for (Table t : chosenClass) {
                System.out.println("chosen classes: " + t);
                for (TableColumn c : t.getColumns()) {
                    allowedCols.add(c.getURI());
                    //if label sim of equivalent properties should be counted
//                    for(String d : propertyCanoniser.backwardsCanoniseResource(c.getURI())) {
//                        allowedCols.add(d);
//                    }
                }
            }

//         try out to exclude all instances not belonging to the majority class!
//            best configuration for properties           
            for (TableRow r : allCandis.getFirstDimension()) {
                for (TableRow s : allCandis.getMatches(r)) {
                    for (Table t : chosenClass) {
                        String parent = getGoldStandard().getClassHierarchy().get(t.getHeader().replace(".csv.gz", "").replace(".csv", "").toLowerCase());
                        if (parent != null && !(parent.equals("work") || parent.equals("agent") || parent.equals("species") || parent.equals("place"))) {
                            if (!s.getTable().getHeader().equals(t.getHeader()) && !s.getTable().getHeader().replace(".csv.gz", "").replace(".csv", "").toLowerCase().equals(parent)) {
                                allCandis.set(r, s, null);
                            }
                        } else {
                            if (!s.getTable().getHeader().equals(t.getHeader())) {
                                allCandis.set(r, s, null);
                            }
                        }
                    }
                }
            }
            allCandis.pruneWithNullEqualOrBelow(0.0);
            for (String c : allowedCols) {
                System.out.println("allowed: " + c);
            }

            boolean kurti = false;

            for (TableColumn c1 : props.getFirstDimension()) {
                for (TableColumn d : props.getMatches(c1)) {
                    if (!allowedCols.contains(d.getURI())) {
                        props.set(c1, d, null);
                        continue;
                    }
//                    if (c1.getDataType() == TableColumn.ColumnDataType.numeric && c1.getColumnStatistic().getKurtosis() < 2) {
//                        kurti = true;
//                        props.set(c1, d, null);
//                        continue;
//                    }
                    Double value = props.get(c1, d);
                    if (value == null) {
                        continue;
                    }
                    if (d.getURI().contains("http://www.w3.org") || d.getURI().contains("http://purl.org") || d.getURI().contains("wikiPage")) {
                        props.set(c1, d, null);
                        continue;
                    }

                    String newUri = propertyCanoniser.canoniseResource(d.getURI());
                    if (!newUri.equals(d.getURI())) {
                        props.set(c1, d, null);
                        for (TableColumn check : chosenClass.get(0).getColumns()) {
                            if (check.getURI().equals(newUri)) {
                                if (props.get(c1, check) == null || value > props.get(c1, check)) {
                                    props.set(c1, check, value);
                                    check.setEquivBefore(d);
                                }
                            }
                        }
                    }
                }
            }

            labelSimilarity.pruneWithNullEqualOrBelow(0.0);

            for (TableColumn c1 : labelSimilarity.getFirstDimension()) {
                for (TableColumn d : labelSimilarity.getMatches(c1)) {
                    //equivalent properties, e.g. category are not included in the class and thus the similarity is not computed
                    //    System.out.println("label matches: " + c1.getHeader() + " - " + d.getURI() + " - " + labelSimilarity.get(c1, d));
                    if (!allowedCols.contains(d.getURI())) {
                        //        System.out.println("not an allowed col label: " + d + " class " + d.getTable());
                        labelSimilarity.set(c1, d, null);
                        continue;
                    }
                    //why was it 0 instead of 2?
//                    if (c1.getDataType() == TableColumn.ColumnDataType.numeric && c1.getColumnStatistic().getKurtosis() < 0) {
//                        kurti = true;
//                        labelSimilarity.set(c1, d, null);
//                        continue;
//                    }
                    if (c1.getDataType() == TableColumn.ColumnDataType.numeric && d.getDataType() != TableColumn.ColumnDataType.numeric
                            || c1.getDataType() == TableColumn.ColumnDataType.date && d.getDataType() != TableColumn.ColumnDataType.date) {
                        labelSimilarity.set(c1, d, null);
                        continue;
                    }

                    Double value = labelSimilarity.get(c1, d);
                    if (value == null) {
                        continue;
                    }
                    if (d.getURI().contains("http://www.w3.org") || d.getURI().contains("http://purl.org") || d.getURI().contains("wikiPage")) {
                        labelSimilarity.set(c1, d, null);
                    }
                    String newUri = propertyCanoniser.canoniseResource(d.getURI());
                    if (!newUri.equals(d.getURI())) {
                        //        System.out.println("equiv file used!" + d.getURI());
                        for (TableColumn check : chosenClass.get(0).getColumns()) {
                            if (check.getURI().equals(newUri)) {
                                //             System.out.println("found the according URI!" + d.getURI() + " - " + check.getURI());
                                labelSimilarity.set(c1, d, null);
                                if (labelSimilarity.get(c1, check) == null || value > labelSimilarity.get(c1, check)) {
                                    System.out.println("set!" + check.getURI() + " - " + value);
                                    labelSimilarity.set(c1, check, value);
                                    //check.setEquivBefore(d);
                                }
                            }
                        }
                    }
                }
            }

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
                        possibleMatchesByCol.get(c).add(d);
                    } else {
                        List<TableColumn> x = new ArrayList<>();
                        x.add(d);
                        possibleMatchesByCol.put(c, x);
                    }
                }
                for (TableColumn d : labelSimilarity.getMatches(c)) {
                    if (!allowedCols.contains(d.getURI())) {
                        continue;
                    }
                    if (possibleMatchesByCol.containsKey(c)) {
                        possibleMatchesByCol.get(c).add(d);
                    } else {
                        List<TableColumn> x = new ArrayList<>();
                        x.add(d);
                        possibleMatchesByCol.put(c, x);
                    }
                }
//                for (TableColumn d : kurt.getMatches(c)) {
//                    if (!allowedCols.contains(d.getURI())) {
//                        continue;
//                    }
//                    if (possibleMatchesByCol.containsKey(c)) {
//                        possibleMatchesByCol.get(c).add(d);
//                    } else {
//                        List<TableColumn> x = new ArrayList<>();
//                        x.add(d);
//                        possibleMatchesByCol.put(c, x);
//                    }
//                }
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
//                    if (kurt.get(c, d) != null) {
//                        counter++;
//                    }
                    majority.set(c, d, counter);
                    System.out.println("no. matches " + c + " - " + d + " - " + counter);
                }
            }

//            SimilarityMatrix<TableColumn> kurt1 = new SparseSimilarityMatrix(0, 0);
//
//            if (itemset) {
//                Map<String, Map<String, List<Double>>> itemsets = loadFrequentItemsset();
//
//                Map<String, Double> itemSetForClass = itemsets.get(chosenClass.get(0).getHeader().split("\\.")[0].toLowerCase());
//
//                for (TableColumn c : getData().getWebtable().getColumns()) {
//
//                    //only add the fi if nothing else fits
//                    if (possibleMatchesByCol.get(c) != null && !possibleMatchesByCol.get(c).isEmpty()) {
//                        continue;
//                    }
//                    int counter =0;
////                    boolean date = false;
////                    for(Object o : c.getValues().values()) {
////                        if(o.toString().contains("jan") || o.toString().contains("feb") || o.toString().contains("mar")
////                                || o.toString().contains("apr") || o.toString().contains("may") || o.toString().contains("june")
////                                || o.toString().contains("june") || o.toString().contains("july")
////                                || o.toString().contains("aug") || o.toString().contains("sep") || o.toString().contains("oct")
////                                || o.toString().contains("nov") || o.toString().contains("dec")) {
////                            counter++;
////                        }
////                    }
////                    if((double)counter/(double)c.getValues().values().size()>0.75) {
////                        System.out.println();
////                        date = true;
////                    }
//
//                    for (TableColumn d : getData().getDbpediaColSet()) {
//                        if (c.getDataType() != d.getDataType()) {
//                            continue;
////                            if (d.getDataType() == TableColumn.ColumnDataType.date && date) {
////                            }
////                            else {
////                                continue;
////                            }
//                        }
//                        if (!allowedCols.contains(d.getURI())) {
//                            continue;
//                        }
//                        if (itemSetForClass != null && !itemSetForClass.isEmpty()) {
//                            if (itemSetForClass.containsKey(d.getURI())) {
//                                kurt1.set(c, d, itemSetForClass.get(d.getURI()));
//                            }
//                        }
//
//                    }
//                }
//                //kurt.normalize();
//            }
            SimilarityMatrix<TableColumn> kurt = new SparseSimilarityMatrix(0, 0);

            SimilarityMatrix<TableColumn> sumFreqs = new SparseSimilarityMatrix(0, 0);
            SimilarityMatrix<TableColumn> countDT = new SparseSimilarityMatrix(0, 0);
            SimilarityMatrix<TableColumn> freqDT = new SparseSimilarityMatrix(0, 0);

            SimilarityMatrix<TableColumn> classStatsMatrix = new SparseSimilarityMatrix(0, 0);
            SimilarityMatrix<TableColumn> propStatsMatrix = new SparseSimilarityMatrix(0, 0);

            if (itemset) {
                Map<String, Map<String, Double>> itemsets = loadFrequentItemsset();
                Map<String, Double> classStats = loadFrequentItemssetClassStats();
                Map<String, Double> propStats = loadFrequentItemssetPropStats();

                Map<String, Double> itemSetForClass = itemsets.get(chosenClass.get(0).getHeader().split("\\.")[0].toLowerCase());
                System.out.println("class chosen: " + chosenClass.get(0).getHeader().split("\\.")[0].toLowerCase());
                System.out.println("itemset: " + itemsets.keySet());
                double numeric = 0.0, string = 0.0, date = 0.0;
                double numericCount = 0.0, stringCount = 0.0, dateCount = 0.0;

                if (itemSetForClass != null && !itemSetForClass.isEmpty()) {

                    for (TableColumn d : chosenClass.get(0).getColumns()) {
                        for (String s : itemSetForClass.keySet()) {
                            if (d.getURI().equals(s)) {
                                double freqPerType = itemSetForClass.get(d.getURI());
                                if (d.getDataType() == TableColumn.ColumnDataType.numeric) {
                                    numericCount++;
                                    numeric += freqPerType;
                                }
                                if (d.getDataType() == TableColumn.ColumnDataType.date) {
                                    dateCount++;
                                    date += freqPerType;
                                }
                                if (d.getDataType() == TableColumn.ColumnDataType.string) {
                                    stringCount++;
                                    string += freqPerType;
                                }
                            }
                        }
                    }

                    for (TableColumn c : possibleMatchesByCol.keySet()) {
                        for (TableColumn d : possibleMatchesByCol.get(c)) {
                            if (c.getDataType() != d.getDataType()) {
                                continue;
                            }
                            if (!allowedCols.contains(d.getURI())) {
                                continue;
                            }
                            if (itemSetForClass != null && !itemSetForClass.isEmpty()) {
                                if (itemSetForClass.containsKey(d.getURI())) {
                                    kurt.set(c, d, itemSetForClass.get(d.getURI()));
                                    // sumFreqs.set(c, d, itemSetForClass.get(d.getURI()));
                                    propStatsMatrix.set(c, d, propStats.get(d.getURI()));
                                    double allFreq = classStats.get(chosenClass.get(0).getHeader().split("\\.")[0].toLowerCase());
                                    classStatsMatrix.set(c, d, itemSetForClass.get(d.getURI()) / allFreq);

                                    if (d.getDataType() == TableColumn.ColumnDataType.numeric) {
                                        countDT.set(c, d, numericCount);
                                        freqDT.set(c, d, numeric);
                                    }
                                    if (d.getDataType() == TableColumn.ColumnDataType.date) {
                                        countDT.set(c, d, dateCount);
                                        freqDT.set(c, d, date);
                                    }
                                    if (d.getDataType() == TableColumn.ColumnDataType.string) {
                                        countDT.set(c, d, stringCount);
                                        freqDT.set(c, d, string);
                                    }
                                }
                            }

                        }
                    }
                    //kurt.normalize();
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
                    if (first == 0.0 || second == 0.0) {
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

            if (!propValue) {
                props = new SparseSimilarityMatrix<>(0, 0);
            }

            Map<SimilarityMatrix, MatrixStats> stats = new HashMap<>();
            props.setName("props");
            labelSimilarity.setName("label");
            columnWidth.setName("width");
            kurt.setName("kurt");
            stats.put(labelSimilarity, new MatrixStats(labelSimilarity, getData(), possibleMatchesByCol));
            stats.put(props, new MatrixStats(props, getData(), possibleMatchesByCol));
            stats.put(columnWidth, new MatrixStats(columnWidth, getData(), possibleMatchesByCol));
            stats.put(kurt, new MatrixStats(kurt, getData(), possibleMatchesByCol));

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
                allCandidates = nonOverlap.match(propertySim, x);
                allCandidates.pruneWithNullEqualOrBelow(0.0);
                propertySim = allCandidates;
            }
//            x = kurt.copy();
//            if (stats.get(kurt).getMean() != Double.NaN) {
//                x.multiplyScalar(stats.get(kurt).getMean());
//                allCandidates = nonOverlap.match(propertySim, x);
//                allCandidates.pruneWithNullEqualOrBelow(0.0);
//                propertySim = allCandidates;
//            }
//            allCandidates = nonOverlap.match(propertySim, kurt);
//            allCandidates.pruneWithNullEqualOrBelow(0.0);
//            propertySim = allCandidates;
            SparseSimilarityMatrix<TableColumn> beforePrunungProps = (SparseSimilarityMatrix< TableColumn>) propertySim.copy();
            propertySim.pruneWithNullEqualOrBelow(0.0);

            MatchingResult r1 = new MatchingResult();
            MatchingResult r2 = new MatchingResult();
            MatchingResult r5 = new MatchingResult();

            candidateSelection.mapProperties(props, webtable, false, r1);
            candidateSelection.mapProperties(labelSimilarity, webtable, false, r2);
            candidateSelection.mapProperties(kurt, webtable, false, r5);

            System.out.println("correl comp prop value" + webTableName + "\t" + stats.get(props).getNormalizedHerinfahl() + "\t" + stats.get(props).getHerfindahlIndex() + "\t" + stats.get(props).getMean() + "\t" + stats.get(props).getStad()
                    + "\t" + stats.get(props).getLineBasedAverage() + "\t" + stats.get(props).getLC() + "\t" + r1.getEvaluation().getPropertyResult().getPrecision() + "\t" + r1.getEvaluation().getPropertyResult().getRecall());
            System.out.println("correl comp prop label" + webTableName + "\t" + stats.get(labelSimilarity).getNormalizedHerinfahl() + "\t" + stats.get(labelSimilarity).getHerfindahlIndex() + "\t" + stats.get(labelSimilarity).getMean() + "\t" + stats.get(labelSimilarity).getStad()
                    + "\t" + stats.get(labelSimilarity).getLineBasedAverage() + "\t" + stats.get(labelSimilarity).getLC() + "\t" + r2.getEvaluation().getPropertyResult().getPrecision() + "\t" + r2.getEvaluation().getPropertyResult().getRecall());

            System.out.println("correl comp prop width" + webTableName + "\t" + stats.get(columnWidth).getNormalizedHerinfahl() + "\t" + stats.get(columnWidth).getHerfindahlIndex() + "\t" + stats.get(columnWidth).getMean() + "\t" + stats.get(columnWidth).getStad()
                    + "\t" + stats.get(columnWidth).getLineBasedAverage() + "\t" + stats.get(columnWidth).getLC() + "\t" + r5.getEvaluation().getPropertyResult().getPrecision() + "\t" + r5.getEvaluation().getPropertyResult().getRecall());

            System.out.println("correl comp prop kurt" + webTableName + "\t" + stats.get(kurt).getNormalizedHerinfahl() + "\t" + stats.get(kurt).getHerfindahlIndex() + "\t" + stats.get(kurt).getMean() + "\t" + stats.get(kurt).getStad()
                    + "\t" + stats.get(kurt).getLineBasedAverage() + "\t" + stats.get(kurt).getLC() + "\t" + r5.getEvaluation().getPropertyResult().getPrecision() + "\t" + r5.getEvaluation().getPropertyResult().getRecall());

            getSimilarities().setPropertySimilarity(propertySim);

            SimilarityMatrix<TableCell> inst = Matcher.multiplyParentSimilarity(propertySim, getSimilarities().getValueSimilarity(), new TableColumnToCellHierarchyAdapter());

            Aggregate<TableRow, TableCell> a = new Aggregate<>();
            a.setAggregationType(AggregationType.Sum);
            SimilarityMatrix<TableRow> propWeightedInstanceCand = a.match(allCandis, inst, new TableRowToCellHierarchyAdapter());
            SimilarityMatrix<TableRow> values = propWeightedInstanceCand;
            values.normalize();

            if (!instValue) {
                values = new SparseSimilarityMatrix<>(0, 0);
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
                y1.multiplyScalar(1.0 - (double) config.getValue(PAR_ABSTRACT_WEIGHT));
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

            if (freq) {
                SimilarityMatrix<TableRow> frequencyBased = new SparseSimilarityMatrix<>(combinedWithWeight.getFirstDimension().size(), combinedWithWeight.getSecondDimension().size());
                Map<String, Integer> freqs = getData().getFrequencies();

                System.out.println("freqs size: " + freqs.size());

                for (TableRow r : combinedWithWeight.getFirstDimension()) {
                    for (TableRow s : combinedWithWeight.getMatches(r)) {
                        System.out.println("should match freq: " + s.getURI().toString());
                        if (freqs.containsKey(s.getURI().toString())) {
                            double count = (double) freqs.get(s.getURI().toString()) / 177373.0;
                            System.out.println("freq set: " + r + "\t" + s + "\t" + count);
                            frequencyBased.set(r, s, count);
                        }
                    }
                }

                frequencyBased.setName("freq");

                instanceStats.put(frequencyBased, new MatrixStats(frequencyBased, getData(), possibleMatchesByRow));
                x1 = frequencyBased.copy();
                x1.multiplyScalar(instanceStats.get(frequencyBased).getHerfindahlIndex());
                y1 = combinedWithWeight.copy();
                combinedWithWeight = nonOverlap.match(x1, y1);

                MatchingResult r3 = new MatchingResult();
                candidateSelection.mapInstances(frequencyBased, false, r3, webtable);
                System.out.println("correl comp freq" + webTableName + "\t" + instanceStats.get(frequencyBased).getNormalizedHerinfahl() + "\t" + instanceStats.get(frequencyBased).getHerfindahlIndex() + "\t" + instanceStats.get(frequencyBased).getMean() + "\t" + instanceStats.get(frequencyBased).getStad()
                        + "\t" + r3.getEvaluation().getInstanceResult().getPrecision() + "\t" + r3.getEvaluation().getInstanceResult().getRecall());

                for (TableRow c1 : combinedWithWeight.getFirstDimension()) {
                    for (TableRow d : combinedWithWeight.getMatches(c1)) {
                        if (rowToUri.containsKey(d.getURI().toString()) && !rowToUri.get(d.getURI().toString()).equals(d)) {
                            combinedWithWeight.set(c1, d, null);
                        }
                    }
                }
            }

            SimilarityMatrix<TableRow> beforePrunung = combinedWithWeight.copy();
            System.out.println("ins thres! " + config.getValue(PAR_INST_FINAL_THRESHOLD));
            combinedWithWeight.pruneWithNull((Double) config.getValue(PAR_INST_FINAL_THRESHOLD));
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
            double mappedInstances = 0.0;
            double mappedProperties = 0.0;
            double propertyScoreSum = 0.0;
            double sameSame = 0.0;

            SimilarityMatrix<TableRow> instanceCorres = candidateSelection.mapInstances(getSimilarities().getCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), new MatchingResult(), "");
            SimilarityMatrix<TableColumn> propertyCorres = candidateSelection.mapProperties(getSimilarities().getPropertySimilarity().copy(), webTableName, true, new MatchingResult());
            instanceCorres.normalize();
            propertyCorres.normalize();

            for (TableColumn r : propertyCorres.getFirstDimension()) {
                for (TableColumn s : propertyCorres.getMatches(r)) {
                    propertyScoreSum += propertyCorres.get(r, s);
                }
                mappedProperties++;
            }

            Map<String, Integer> sameInstances = new HashMap();

            double instanceScoreSum = 0.0;
//            Map<String, Integer> countsPerClass = new HashMap();
//            for (TableRow r : instanceCorres.getFirstDimension()) {
//                for (TableRow s : instanceCorres.getMatches(r)) {
//                    instanceScoreSum += instanceCorres.get(r, s);
//                    if (countsPerClass.containsKey(s.getTable().getHeader())) {
//                        int countSingleClass = countsPerClass.get(s.getTable().getHeader());
//                        countSingleClass++;
//                        countsPerClass.put(s.getTable().getHeader(), countSingleClass);
//                    } else {
//                        countsPerClass.put(s.getTable().getHeader(), 1);
//                    }
//                    if (sameInstances.containsKey(s.getURI().toString())) {
//                        int countSingleClass = sameInstances.get(s.getURI().toString());
//                        countSingleClass++;
//                        sameInstances.put(s.getURI().toString(), countSingleClass);
//                    } else {
//                        sameInstances.put(s.getURI().toString(), 1);
//                    }
//                }
//            }
            Map<String, Integer> countsPerClass = new HashMap();
            instanceScoreSum = 0;
            int countInstance = 0;
            //int maxComingFromOne = 0;

            for (TableRow r : instanceCorres.getFirstDimension()) {
                for (TableRow s : instanceCorres.getMatches(r)) {
                    countInstance++;
//                    List<TableRow> allURIs = data.getUriMap().get(s.getURI().toString());
//                    if(allURIs.size()>maxComingFromOne) {
//                        maxComingFromOne = allURIs.size();
//                    }
                    instanceScoreSum += instanceCorres.get(r, s);
                    //for (TableRow otherRowWithURI : allURIs) {
                    if (countsPerClass.containsKey(s.getTable().getHeader())) {
                        int countSingleClass = countsPerClass.get(s.getTable().getHeader());
                        countSingleClass++;
                        countsPerClass.put(s.getTable().getHeader(), countSingleClass);
                    } else {
                        countsPerClass.put(s.getTable().getHeader(), 1);
                    }
                    //}
                    if (sameInstances.containsKey(s.getURI().toString())) {
                        int countSingleClass = sameInstances.get(s.getURI().toString());
                        countSingleClass++;
                        sameInstances.put(s.getURI().toString(), countSingleClass);
                    } else {
                        sameInstances.put(s.getURI().toString(), 1);
                    }
                }
            }

            int maxInstance = -1;
            for (String s : sameInstances.keySet()) {
                if (sameInstances.get(s) > maxInstance) {
                    maxInstance = sameInstances.get(s);
                }
            }

            String best = null;
            int max = -1;
            int allMapped = 0;
            for (String s : countsPerClass.keySet()) {
                if (countsPerClass.get(s) > max) {
                    best = s;
                    max = countsPerClass.get(s);
                }
                allMapped += countsPerClass.get(s);
            }
            //if (allMapped > 0) {
            if (countInstance > 0) {
                //mappedInstances = (double) max / allMapped;
                mappedInstances = (double) max / countInstance;
            }
//
            List<String> corres = new ArrayList();
            for (TableRow r : instanceCorres.getFirstDimension()) {
                for (TableRow s : instanceCorres.getMatches(r)) {
                    corres.add(s.getURI().toString());
                }
            }

            boolean filtered = false;
            getSimilarities().setBeforeFiltering(getSimilarities().getCandidateSimilarity());

            double classesPerInstances = 0.0;

            if (corres.size() > 0) {
                System.out.println("corres size: " + corres.size());
                System.out.println("counts per class: " + countsPerClass.size());
                //System.out.println("max from one: " +maxComingFromOne);
                classesPerInstances = (double) countsPerClass.size() / (double) corres.size();
                classesPerInstances = 1 - classesPerInstances;
            }

            if (classesPerInstances < 0.3) {

                filtered = true;
                try {
                    if (filtered && !getData().getWebtable().getHeader().contains("ip-")) {
                        System.out.println("WRONG FILTER classPerInst\t" + getData().getWebtable().getHeader() + "\t" + instanceScoreSum / corres.size() + "\t" + mappedInstances + "\t" + classesPerInstances + "\t" + allMapped + "\t" + getData().getWebtableRowSet().size() + "\t" + getData().getWebtable().getKey().getValues() + "\t" + corres + "\t" + chosenClass.get(0).getKey().getValues().size());
                    }
                } catch (Exception e) {

                }

                result.setWebtable(getData().getWebtable());
                result.setMatchingData(getData());
                result.setGoldStandard(getGoldStandard());
                result.setSimilarities(getSimilarities());
                getSimilarities().setClassSimilarity(new SparseSimilarityMatrix<Table>(0, 0));
                getSimilarities().setPropertySimilarity(new SparseSimilarityMatrix<TableColumn>(0, 0));
                getSimilarities().setCandidateSimilarity(new SparseSimilarityMatrix<TableRow>(0, 0));

            }

////
            double numberOfMapped = (double) countInstance / (double) getData().getWebtable().getTotalNumOfRows();
            if (numberOfMapped < 0.15) {

                filtered = true;
                try {
                    if (filtered && !getData().getWebtable().getHeader().contains("ip-")) {
                        System.out.println("WRONG FILTER numb mapped\t" + getData().getWebtable().getHeader() + "\t" + instanceScoreSum / corres.size() + "\t" + mappedInstances + "\t" + classesPerInstances + "\t" + allMapped + "\t" + getData().getWebtableRowSet().size() + "\t" + getData().getWebtable().getKey().getValues() + "\t" + corres + "\t" + chosenClass.get(0).getKey().getValues().size());
                    }
                } catch (Exception e) {

                }

                result.setWebtable(getData().getWebtable());
                result.setMatchingData(getData());
                result.setGoldStandard(getGoldStandard());
                result.setSimilarities(getSimilarities());
                getSimilarities().setClassSimilarity(new SparseSimilarityMatrix<Table>(0, 0));
                getSimilarities().setPropertySimilarity(new SparseSimilarityMatrix<TableColumn>(0, 0));
                getSimilarities().setCandidateSimilarity(new SparseSimilarityMatrix<TableRow>(0, 0));
            }

            if (allMapped < 3) {

                filtered = true;
                try {
                    if (filtered && !getData().getWebtable().getHeader().contains("ip-")) {
                        System.out.println("WRONG FILTER #mapped\t" + getData().getWebtable().getHeader() + "\t" + instanceScoreSum / corres.size() + "\t" + mappedInstances + "\t" + classesPerInstances + "\t" + allMapped + "\t" + getData().getWebtableRowSet().size() + "\t" + getData().getWebtable().getKey().getValues() + "\t" + corres + "\t" + chosenClass.get(0).getKey().getValues().size());
                    }
                } catch (Exception e) {

                }

                result.setWebtable(getData().getWebtable());
                result.setMatchingData(getData());
                result.setGoldStandard(getGoldStandard());
                result.setSimilarities(getSimilarities());
                getSimilarities().setClassSimilarity(new SparseSimilarityMatrix<Table>(0, 0));
                getSimilarities().setPropertySimilarity(new SparseSimilarityMatrix<TableColumn>(0, 0));
                getSimilarities().setCandidateSimilarity(new SparseSimilarityMatrix<TableRow>(0, 0));
            }
//
            if (mappedInstances < (Double) config.getValue(PAR_MAPPED_RATIO_FILTER)) {
                getLogger().logData("Not enough consistent candidates, cancelling!");

                filtered = true;
                if (filtered && !getData().getWebtable().getHeader().contains("ip-") && chosenClass.size() > 0) {
                    System.out.println("WRONG FILTER ratio\t" + getData().getWebtable().getHeader() + "\t" + instanceScoreSum / corres.size() + "\t" + mappedInstances + "\t" + classesPerInstances + "\t" + allMapped + "\t" + getData().getWebtableRowSet().size() + "\t" + getData().getWebtable().getKey().getValues() + "\t" + corres + "\t" + chosenClass.get(0).getKey().getValues().size());
                }

                // not enough mappings with the selected class, we do not map this table at all
                result.setWebtable(getData().getWebtable());
                result.setMatchingData(getData());
                result.setGoldStandard(getGoldStandard());
                result.setSimilarities(getSimilarities());
                getSimilarities().setClassSimilarity(new SparseSimilarityMatrix<Table>(0, 0));
                getSimilarities().setPropertySimilarity(new SparseSimilarityMatrix<TableColumn>(0, 0));
                getSimilarities().setCandidateSimilarity(new SparseSimilarityMatrix<TableRow>(0, 0));
            }
            double sameInstanceMappedRatio = maxInstance / (double) allMapped;
            if (sameInstanceMappedRatio > 0.5) {
                getLogger().logData("Mapped to the same instance");

                filtered = true;
                if (filtered && !getData().getWebtable().getHeader().contains("ip-") && chosenClass.size() > 0) {
                    System.out.println("WRONG FILTER same same\t" + getData().getWebtable().getHeader() + "\t" + instanceScoreSum / corres.size() + "\t" + mappedInstances + "\t" + classesPerInstances + "\t" + allMapped + "\t" + getData().getWebtableRowSet().size() + "\t" + getData().getWebtable().getKey().getValues() + "\t" + corres + "\t" + chosenClass.get(0).getKey().getValues().size());
                }

                // not enough mappings with the selected class, we do not map this table at all
                result.setWebtable(getData().getWebtable());
                result.setMatchingData(getData());
                result.setGoldStandard(getGoldStandard());
                result.setSimilarities(getSimilarities());
                getSimilarities().setClassSimilarity(new SparseSimilarityMatrix<Table>(0, 0));
                getSimilarities().setPropertySimilarity(new SparseSimilarityMatrix<TableColumn>(0, 0));
                getSimilarities().setCandidateSimilarity(new SparseSimilarityMatrix<TableRow>(0, 0));
            }

            try {
                if (!filtered && getData().getWebtable().getHeader().contains("ip-") && chosenClass.size() > 0) {
                    //              System.out.println("NO FILTER\t" + getData().getWebtable().getHeader() + "\t" + mappedInstances + "\t" + classesPerInstances / corres.size() + "\t" + allMapped + "\t" + getData().getWebtableRowSet().size() + "\t" + getData().getWebtable().getKey().getValues() + "\t" + corres + "\t" + chosenClass.get(0).getKey().getValues().size());
                }
            } catch (Exception e) {

            }
            //TODO!!! JUST FOR TESTING CLASSES!
            filtered = false;

            System.out.println("GS before MAP " + goldStandard.getInstanceGoldStandard().size());

            candidateSelection.mapClasses(getSimilarities().getClassSimilarity().copy(), result, false);
            candidateSelection.mapInstances(getSimilarities().getCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), result, "");
            candidateSelection.mapProperties(getSimilarities().getPropertySimilarity().copy(), webTableName, true, result);
            //candidateSelection.mapPropertiesAll(getSimilarities().getPropertySimilarity().copy(), webTableName, true, result);

            Map<TableRow, List<MatchingPair>> matchedRows = new HashMap<>();
            IndirectInstanceMatcherMapped idcm = null;
            SparseSimilarityMatrix<TableRow> combinedBoth2 = null;
            SimilarityMatrix<Table> originalClassSim = getSimilarities().getClassSimilarity();
            SparseSimilarityMatrix<Table> indirectClassMatrix = null;
            //try!
            if (!filtered) {
                SparseSimilarityMatrix<TableRow> matchedRowsIndirect2 = (SparseSimilarityMatrix<TableRow>) candidateSelection.mapInstances(getSimilarities().getCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), result, "");

                Map<Table, Integer> possibleClasses2 = new HashMap<>();
                for (TableRow tr : matchedRowsIndirect2.getFirstDimension()) {
                    for (TableRow tx : matchedRowsIndirect2.getMatches(tr)) {
                        for (TableRow allRows : data.getUriMap().get(tx.getURI().toString())) {
                            if (possibleClasses2.containsKey(allRows.getTable())) {
                                Integer current = possibleClasses2.get(allRows.getTable());
                                current++;
                                possibleClasses2.put(allRows.getTable(), current);
                            } else {
                                possibleClasses2.put(allRows.getTable(), 1);
                            }
                        }

                    }
                }
                Map<Table, Integer> sortedClasses2 = sortByValue(possibleClasses2);
                System.out.println("possible classes before" + getData().getWebtable() + "\t" + sortedClasses2 + "\t"
                        + goldStandard.getClassGoldStandard().get(getData().getWebtable().getHeader().replace(".json", "")));

                r1 = new MatchingResult();
                EvaluationAdapter<TableRow> evalRow = new CandidateAdapter();
                EvaluationAdapter<TableRow> evalInstance = new DBpediaInstanceAdapter(goldStandard.getInstanceCanoniser());

                if (mapped == null && (indirectPrecision || indirectRecall)) {
                    itc.setNumTabes(1000);
                    itc.setParams(matchingParameters);
                    itc.setParentTimer(rootTimer);
                    itc.setMinimalOverlap(0.0);
                    itc.setGs(goldStandard);
                    itc.setMatchingDataOri(data);
                    mapped = itc.computeCandidates(data.getWebtableRowSet(), result);
                    System.out.println("itc ready " + mapped.size());
                }
                if (indirectPrecision || indirectRecall) {
                    double sumClasses = 0.0;
                    Map<Table, Integer> countClasses = new HashMap<>();
                    for (Table t : mapped.keySet()) {
                        for (Pair p : t.getMapping().getMappedInstances().values()) {
                            List<TableRow> r = data.getUriMap().get(p.getFirst().toString());
                            for (TableRow s : r) {
                                sumClasses++;
                                if (countClasses.containsKey(s.getTable())) {
                                    int current = countClasses.get(s.getTable());
                                    current++;
                                    countClasses.put(s.getTable(), current);
                                } else {
                                    countClasses.put(s.getTable(), 1);
                                }
                            }
                        }
                    }
                    Map<Table, Integer> sortedClassesIndirect = sortByValue(countClasses);
                    System.out.println("possible classes indirect" + getData().getWebtable() + "\t" + sortedClassesIndirect + "\t"
                            + goldStandard.getClassGoldStandard().get(getData().getWebtable().getHeader().replace(".json", "")));
                    indirectClassMatrix = new SparseSimilarityMatrix<>(1, sortedClassesIndirect.size());
                    for (Table t : sortedClassesIndirect.keySet()) {
                        indirectClassMatrix.set(data.getWebtable(), t, (double) sortedClassesIndirect.get(t) / sumClasses);
                    }
                    //always contains 1 as highest value!
                    //indirectClassMatrix.normalize();
                    classStatsMatrix.normalize();

                    Map<Table, List<Table>> possibleMatchesByTable = new HashMap<>();

                    for (Table d : originalClassSim.getMatches(data.getWebtable())) {
                        if (possibleMatchesByTable.containsKey(data.getWebtable())) {
                            possibleMatchesByTable.get(data.getWebtable()).add(d);
                        } else {
                            List<Table> tr = new ArrayList<>();
                            tr.add(d);
                            possibleMatchesByTable.put(data.getWebtable(), tr);
                        }
                    }
                    for (Table d : indirectClassMatrix.getMatches(data.getWebtable())) {
                        if (possibleMatchesByTable.containsKey(data.getWebtable())) {
                            possibleMatchesByTable.get(data.getWebtable()).add(d);
                        } else {
                            List<Table> tr = new ArrayList<>();
                            tr.add(d);
                            possibleMatchesByTable.put(data.getWebtable(), tr);
                        }
                    }

                    Map<SimilarityMatrix, MatrixStats> classStats = new HashMap<>();
                    classStats.put(indirectClassMatrix, new MatrixStats(indirectClassMatrix, getData(), possibleMatchesByTable));
                    classStats.put(originalClassSim, new MatrixStats(originalClassSim, getData(), possibleMatchesByTable));

                    SimilarityMatrix x2 = indirectClassMatrix.copy();
                    x2.multiplyScalar(classStats.get(indirectClassMatrix).getHerfindahlIndex());
                    SimilarityMatrix y2 = originalClassSim.copy();
                    y2.multiplyScalar(classStats.get(originalClassSim).getHerfindahlIndex());
                    SimilarityMatrix<Table> classesCombined = nonOverlap.match(x2, y2);

                    boolean differentResult = false;
                    String chosenTable = "";
                    MatchingResult res = new MatchingResult();
                    candidateSelection.mapClasses(classesCombined, res, false);
                    for (Correspondence<Table> cor : res.getClassMappings()) {
                        System.out.println("combined: " + cor.getFirst().getHeader() + "\t" + cor.getSecond().getHeader() + "\t" + cor.isCorrect());
                        chosenTable = cor.getSecond().getHeader();
                    }
                    for (Correspondence<Table> cor : result.getClassMappings()) {
                        System.out.println("not combined: " + cor.getFirst().getHeader() + "\t" + cor.getSecond().getHeader() + "\t" + cor.isCorrect());
                        if (!chosenTable.equals(cor.getSecond().getHeader())) {
                            differentResult = true;
                        }
                    }

                    SimilarityMatrix<Table> allClasses = new SparseSimilarityMatrix<>(1, classesCombined.getSecondDimension().size());
                    double avgOri = originalClassSim.getSum() / originalClassSim.getSecondDimension().size();

                    for (Table t : classesCombined.getMatches(data.getWebtable())) {
                        double sum = 0.0, count = 0.0;
                        if (originalClassSim.get(data.getWebtable(), t) != null) {
                            sum += originalClassSim.get(data.getWebtable(), t);
                            count++;
                        }
                        if (indirectClassMatrix.get(data.getWebtable(), t) != null) {
                            sum += indirectClassMatrix.get(data.getWebtable(), t);
                            count++;
                        }
                        if (allClassMatrices != null) {
                            if (allClassMatrices.get(1).get(data.getWebtable(), t) != null) {
                                sum += allClassMatrices.get(1).get(data.getWebtable(), t);
                                count++;
                            }
                            if (allClassMatrices.get(2).get(data.getWebtable(), t) != null) {
                                sum += allClassMatrices.get(2).get(data.getWebtable(), t);
                                count++;
                            }
                            if (allClassMatrices.get(3).get(data.getWebtable(), t) != null) {
                                sum += allClassMatrices.get(3).get(data.getWebtable(), t);
                                count++;
                            }
                            if (allClassMatrices.get(4).get(data.getWebtable(), t) != null) {
                                sum += allClassMatrices.get(4).get(data.getWebtable(), t);
                                count++;
                            }
                            if (allClassMatrices.get(5).get(data.getWebtable(), t) != null) {
                                sum += allClassMatrices.get(5).get(data.getWebtable(), t);
                                count++;
                            }
                            if (allClassMatrices.get(6).get(data.getWebtable(), t) != null) {
                                sum += allClassMatrices.get(6).get(data.getWebtable(), t);
                                count++;
                            }
                            //classesCombined.set(getData().getWebtable(), t, classesCombined.get(data.getWebtable(), t)*count);
                            allClasses.set(getData().getWebtable(), t, sum);
                        }

                        if (sortedClassesIndirect == null) {
                            //if(differentResult) {
                            if (allClassMatrices != null) {
                                double diffIndOri = originalClassSim.getMaxValue() - indirectClassMatrix.getMaxValue();
                                System.out.println("classes combined: " + getData().getWebtable() + "\t" + t + "\t" + classesCombined.get(data.getWebtable(), t) + "\t"
                                        + originalClassSim.get(data.getWebtable(), t) + "\t" + indirectClassMatrix.get(data.getWebtable(), t) + "\t"
                                        + allClassMatrices.get(1).get(data.getWebtable(), t) + "\t" + allClassMatrices.get(2).get(data.getWebtable(), t)
                                        + "\t" + allClassMatrices.get(3).get(data.getWebtable(), t) + "\t" + allClassMatrices.get(4).get(data.getWebtable(), t)
                                        + "\t" + allClassMatrices.get(5).get(data.getWebtable(), t) + "\t" + allClassMatrices.get(6).get(data.getWebtable(), t) + "\t" + sum + "\t" + count
                                        + "\t" + goldStandard.getClassGoldStandard().get(getData().getWebtable().getHeader().replace(".json", "")) + "\t" + differentResult + "\t"
                                        + "0\t" + sumClasses + "\t" + mapped.size() + "\t" + getData().getWebtableRowSet().size() + "\t" + originalClassSim.getMaxValue() + "\t"
                                        + avgOri + "\t" + diffIndOri);
                            } else {
                                System.out.println("classes combined: " + getData().getWebtable() + "\t" + t + "\t" + classesCombined.get(data.getWebtable(), t) + "\t"
                                        + originalClassSim.get(data.getWebtable(), t) + "\t" + indirectClassMatrix.get(data.getWebtable(), t) + "\t"
                                        + allClassMatrices.get(1).get(data.getWebtable(), t) + "\t" + allClassMatrices.get(2).get(data.getWebtable(), t)
                                        + "\t" + allClassMatrices.get(3).get(data.getWebtable(), t) + "\t" + allClassMatrices.get(4).get(data.getWebtable(), t)
                                        + "\t" + allClassMatrices.get(5).get(data.getWebtable(), t) + "\t" + allClassMatrices.get(6).get(data.getWebtable(), t) + "\t" + sum + "\t" + count
                                        + "\t" + goldStandard.getClassGoldStandard().get(getData().getWebtable().getHeader().replace(".json", "")) + "\t" + differentResult + "\t"
                                        + sortedClassesIndirect.get(t) + "\t" + sumClasses + "\t" + mapped.size() + "\t" + getData().getWebtableRowSet().size() + "\t" + originalClassSim.getMaxValue() + "\t"
                                        + avgOri + "\t0");
                            }
                        }
                    }

                    // }
                    for (Table t : allClasses.getMatches(getData().getWebtable())) {
                        if (indirectClassMatrix.get(data.getWebtable(), t) == null && indirectClassMatrix.getMatches(data.getWebtable()) != null
                                && !indirectClassMatrix.getMatches(data.getWebtable()).isEmpty()) {
                            allClasses.set(data.getWebtable(), t, null);
                        }
                    }

                    candidateSelection.mapClasses(classesCombined, result, false);
                }

                //chekc if this is a good condition!
//                if (originalClassSim.getMaxValue() - avgOri < 0.6) {
//                    classesCombined.set(data.getWebtable(),indirectClassMatrix.getBestPair(data.getWebtable()),5.0);
//                }
                SimilarityMatrix<TableRow> combinedBoth = null;
                if (indirectRecall && instaceIndirect) {

                    //try to avoid computing complex similarity measure
                    for (TableRow xy : data.getWebtableRowSet()) {
                        if (xy.getKey() instanceof List) {
                            List<String> l = (List) xy.getKey();
                            if (l.size() == 1) {
                                xy.setKey(l.get(0));
                            }
                        }
                    }

                    IndirectInstanceMatcher iim = new IndirectInstanceMatcher();

//                    
//                    InterTableComponentForInst itcfi = new InterTableComponentForInst();
//                    itcfi.setGs(goldStandard);
//                    itcfi.setTableIndex(tableIndex);
//                    itcfi.setNumTabes(1000);
//                    itcfi.setParams(matchingParameters);
//                    itcfi.setParentTimer(rootTimer);
//                    itcfi.setMinimalOverlap(0.0);
//                    mapped = itcfi.computeCandidates(data.getWebtableRowSet(), result);
                    SimilarityMatrix<TableRow> indirectMappings = iim.computeIndirectMappings(matchingParameters, rootTimer, data, config, result, goldStandard, matchers, logger, evaluationParameters, itc.getMatricesAll(), itc);
                    SimilarityMatrix<TableRow> overallLabel = iim.getOverallLabel();
                    SimilarityMatrix<TableRow> overallValue = iim.getOverallValue();

                    Map<SimilarityMatrix, MatrixStats> statsIndirect = new HashMap<>();

                    Map<TableRow, List<TableRow>> possibleMatchesByRow2 = new HashMap<>();
                    for (TableRow r : overallLabel.getFirstDimension()) {
                        List<TableRow> l = new ArrayList<>();
                        l.addAll(overallLabel.getMatches(r));
                        possibleMatchesByRow2.put(r, l);
                    }
                    for (TableRow r : overallValue.getFirstDimension()) {
                        List<TableRow> l = new ArrayList<>();
                        l.addAll(overallValue.getMatches(r));
                        possibleMatchesByRow2.put(r, l);
                    }

                    statsIndirect.put(overallValue, new MatrixStats(overallValue, data, possibleMatchesByRow2));
                    statsIndirect.put(overallLabel, new MatrixStats(overallLabel, data, possibleMatchesByRow2));

                    CombineNonOverlapping comb = new CombineNonOverlapping();
                    comb.setAggregationType(CombinationType.Sum);
                    combinedBoth = comb.match(similarities.getCandidateSimilarity(), indirectMappings);

                    double averageInstScore = 0.0;
                    for (Correspondence<TableRow> ind : result.getInstanceMappings()) {
                        averageInstScore += ind.getSimilarity();
                    }
                    averageInstScore = averageInstScore / similarities.getCandidateSimilarity().getFirstDimension().size();

                    int countAll = 0;
                    for (String s : itc.getLabelsCount().keySet()) {
                        System.out.println("count label " + s + "\t" + itc.getLabelsCount().get(s));
                        countAll += itc.getLabelsCount().get(s);
                    }
                    System.out.println("count label all " + countAll);

                    //only output
                    int numberMissing = 0;
                    for (Object o : goldStandard.getInstanceGoldStandard().keySet()) {
                        Collection<Correspondence<TableRow>> corres2 = result.getInstanceMappings();
                        boolean detected = false;
                        for (Correspondence<TableRow> c : corres2) {
                            if (o.toString().equals(String.valueOf(c.getFirst().getRowIndex()))) {
                                detected = true;
                                break;
                            }
                        }
                        if (!detected) {
//                            for (TableRow s : data.getWebtableRowSet()) {
//                                if (o.toString().equals(String.valueOf(s.getRowIndex()))) {
//                                    List<String> labelNames = itc.getLabelsPerURI().get(goldStandard.getInstanceGoldStandard().get(o).toString());
//                                    System.out.println("for missing row " + data.getWebtable().getHeader() + "\t" + s + "\t" + labelNames + "\t"
//                                            + goldStandard.getInstanceGoldStandard().get(o).toString() + "\t"
//                                            + itc.getLabelsURIWithTableName().get(goldStandard.getInstanceGoldStandard().get(o).toString()));
//                                    for (TableRow r : indirectMappings.getMatches(s)) {
//                                        if (goldStandard.getInstanceGoldStandard() != null) {
//                                            List<TableRow> rows = data.getUriMap().get(goldStandard.getInstanceGoldStandard().get(o).toString());
//                                            boolean correctExists = false;
//                                            for (TableRow tr : rows) {
//                                                System.out.println("row missing: " + s + " possible: " + r.getURI() + " score: " + indirectMappings.get(s, r) + "\t"
//                                                        + overallLabel.get(s, r) + "\t" + overallValue.get(s, r) + "\t" + indirectMappings.get(s, tr) + "\t"
//                                                        + overallLabel.get(s, tr) + "\t" + overallValue.get(s, tr));
//                                            }
//                                        }
//                                    }
//                                }
//                            }

                            numberMissing++;
                        }
                    }
                    System.out.println("possbile recall: " + itc.getFoundInCorpus().size() + "\t" + numberMissing);

                    candidateSelection.mapInstances(combinedBoth.copy(), getMatchingParameters().isCollectMatchingInfo(), result, "");

                    for (Correspondence<TableRow> ind : result.getInstanceMappings()) {
                        if (result.getClassMappings().iterator().hasNext()) {
                            System.out.println("stats unmaped: " + ind.getFirst().getTable().getHeader() + "\t" + ind.getFirst() + "\t" + ind.getSecond() + "\t"
                                    + result.getClassMappings().iterator().next().getSecond().getHeader() + "\t" + ind.getSecond().getTable().getHeader() + "\t" + mapped.size() + "\t"
                                    + similarities.getCandidateSimilarity().getFirstDimension().size() + "\t" + ind.getFirst().getTable().getKey().getNumRows() + "\t" + numberMissing + "\t"
                                    + result.getPropertyMappings().size() + "\t" + averageInstScore + "\t" + ind.isCorrect() + "\t" + indirectMappings.get(ind.getFirst(), ind.getSecond())
                                    + "\t" + similarities.getCandidateSimilarity().get(ind.getFirst(), ind.getSecond()) + "\t" + overallLabel.get(ind.getFirst(), ind.getSecond())
                                    + "\t" + overallValue.get(ind.getFirst(), ind.getSecond()) + "\t" + result.getClassMappings().iterator().next().getSecond().getKey().getValues().size()
                                    + "\t" + itc.getFoundInCorpus().size() + "\t" + statsIndirect.get(overallValue).getNormalizedHerinfahl() + "\t"
                                    + statsIndirect.get(overallLabel).getNormalizedHerinfahl() + "\t" + iim.getOverlappingProps().size());
                        }
                        //ind.getSecond().getTable().getKey().getColumnStatistic().getDistinctValues()
                    }

                } else {
                    combinedBoth = similarities.getCandidateSimilarity();
                }

                if (indirectPrecision && instaceIndirect) {
                    List<TableRow> mappedNotIndirect = new ArrayList<>();
                    for (TableRow tr : getSimilarities().getCandidateSimilarity().getFirstDimension()) {
                        if (!getSimilarities().getCandidateSimilarity().getMatches(tr).isEmpty()) {
                            mappedNotIndirect.add(tr);
                        }
                    }
                    idcm = new IndirectInstanceMatcherMapped();
                    matchedRows = idcm.computeIndirectMappings(matchingParameters, rootTimer, data, config, result, goldStandard, matchers, logger, evaluationParameters, mapped, itc, mappedNotIndirect);

                    //not sure if it is the best way to just normalize the matrix...
                    //is one score higher than the other?
                    combinedBoth.normalize();

                    System.out.println("size indirect: " + idcm.getOverall1().getFirstDimension());

                    for (TableRow row1 : idcm.getOverall1().getFirstDimension()) {
                        System.out.println(goldStandard.getInstanceGoldStandard().get(String.valueOf(row1.getRowIndex())));
                        if (goldStandard.getInstanceGoldStandard().get(String.valueOf(row1.getRowIndex())) != null) {
                            String uri = goldStandard.getInstanceGoldStandard().get(String.valueOf(row1.getRowIndex())).toString();
                            System.out.println("gs for " + row1 + "\t" + uri);
                            for (TableRow row2 : idcm.getOverall1().getMatches(row1)) {
                                if (row2.getURI().equals(uri)) {
                                    System.out.println("from indirect: " + row1.getTable() + "\t" + row1 + "\t" + row2 + "\t" + idcm.getOverall1().get(row1, row2) + "\t1\t" + idcm.getOverall1().getBestPair(row1));
                                } else {
                                    System.out.println("from indirect: " + row1.getTable() + "\t" + row1 + "\t" + row2 + "\t" + idcm.getOverall1().get(row1, row2) + "\t0\t" + idcm.getOverall1().getBestPair(row1));
                                }
                            }
                            SparseSimilarityMatrix<TableRow> ssm = (SparseSimilarityMatrix) similarities.getCandidateSimilarity().copy();
                            ssm.normalize();
                            for (TableRow row2 : ssm.getMatches(row1)) {
                                if (row2.getURI().equals(uri)) {
                                    System.out.println("from indirect direct: " + row1.getTable() + "\t" + row1 + "\t" + row2 + "\t" + ssm.get(row1, row2) + "\t1\t" + ssm.getBestPair(row1));
                                } else {
                                    System.out.println("from indirect direct: " + row1.getTable() + "\t" + row1 + "\t" + row2 + "\t" + ssm.get(row1, row2) + "\t0\t" + ssm.getBestPair(row1));
                                }
                            }
                        }
                    }

                    CombineNonOverlapping comb2 = new CombineNonOverlapping();
                    comb2.setAggregationType(CombinationType.Sum);
                    combinedBoth2 = (SparseSimilarityMatrix<TableRow>) comb2.match(combinedBoth, idcm.getOverall1());
                } else {
                    combinedBoth2 = (SparseSimilarityMatrix<TableRow>) combinedBoth;
                }

                candidateSelection.mapInstances(combinedBoth2.copy(), getMatchingParameters().isCollectMatchingInfo(), result, "");

//                System.out.println("class mapping: " + result.getClassMappings().iterator().next().getSecond());
//                List<String> allowedClasses = new ArrayList<>();
//                allowedClasses.add(result.getClassMappings().iterator().next().getSecond().getHeader());
//                Collection<String[]> lines = CSVUtils.readCSV(evaluationParameters.getClassHierarchyLocation(), "\t");
//                String classChosen = result.getClassMappings().iterator().next().getSecond().getHeader().replace(".csv.gz", "");
//                classChosen = "http://dbpedia.org/ontology/" + classChosen;
//                for (String[] line : lines) {
//                    if (line[0].equals(classChosen)) {
//                        allowedClasses.add(line[1].replace("http://dbpedia.org/ontology/", "") + ".csv.gz");
//                    }
//                }
//
//                for (TableRow tr : combinedBoth2.getFirstDimension()) {
//                    TableRow bestCurrent = combinedBoth2.getBestPair(tr);
//                    TableRow bestRow = null;
//                    double score = -1.0;
//                    for (TableRow tr2 : combinedBoth2.getMatches(tr)) {
//                        //  System.out.println("row and class: " + tr2 + " - " + tr2.getTable());
//                        if (allowedClasses.contains(tr2.getTable().getHeader())) {
//                            if (combinedBoth2.get(tr, tr2) > score) {
//                                score = combinedBoth2.get(tr, tr2);
//                                bestRow = tr2;
//                            }
//                        }
//                        //  System.out.println("combined both2 matrix: " + tr + " - " + tr2 + " - " + combinedBoth2.get(tr, tr2));
//                    }
//                    if (bestRow != null && score == combinedBoth2.get(tr, bestCurrent)) {
//                        //   System.out.println("best row found: " + tr + " - " + bestRow);
//                        for (TableRow tr2 : combinedBoth2.getMatches(tr)) {
//                            if (!tr2.equals(bestRow)) {
//                                combinedBoth2.set(tr, tr2, null);
//                            }
//                        }
//                    }
//                }
//
//                SparseSimilarityMatrix<TableRow> matchedRowsIndirect = (SparseSimilarityMatrix<TableRow>) candidateSelection.mapInstances(combinedBoth2.copy(), getMatchingParameters().isCollectMatchingInfo(), result, "");
//
//                Map<Table, Integer> possibleClasses = new HashMap<>();
//                for (TableRow tr : matchedRowsIndirect.getFirstDimension()) {
//                    for (TableRow tx : matchedRowsIndirect.getMatches(tr)) {
//                        for (TableRow allRows : data.getUriMap().get(tx.getURI().toString())) {
//                            if (possibleClasses.containsKey(allRows.getTable())) {
//                                Integer current = possibleClasses.get(allRows.getTable());
//                                current++;
//                                possibleClasses.put(allRows.getTable(), current);
//                            } else {
//                                possibleClasses.put(allRows.getTable(), 1);
//                            }
//                        }
//
//                    }
//                }
//                Map<Table, Integer> sortedClasses = sortByValue(possibleClasses);
//                System.out.println("possible classes " + getData().getWebtable() + "\t" + sortedClasses + "\t"
//                        + goldStandard.getClassGoldStandard().get(getData().getWebtable().getHeader().replace(".json", "")));
//
//                countsPerClass = new HashMap();
//                instanceScoreSum = 0;
//                countInstance = 0;
//
//                for (TableRow r : matchedRowsIndirect.getFirstDimension()) {
//                    for (TableRow s : matchedRowsIndirect.getMatches(r)) {
//                        countInstance++;
//                        List<TableRow> allURIs = data.getUriMap().get(s.getURI().toString());
//                        instanceScoreSum += matchedRowsIndirect.get(r, s);
//                        for (TableRow otherRowWithURI : allURIs) {
//                            if (countsPerClass.containsKey(otherRowWithURI.getTable().getHeader())) {
//                                int countSingleClass = countsPerClass.get(otherRowWithURI.getTable().getHeader());
//                                countSingleClass++;
//                                countsPerClass.put(otherRowWithURI.getTable().getHeader(), countSingleClass);
//                            } else {
//                                countsPerClass.put(otherRowWithURI.getTable().getHeader(), 1);
//                            }
//                        }
//                        if (sameInstances.containsKey(s.getURI().toString())) {
//                            int countSingleClass = sameInstances.get(s.getURI().toString());
//                            countSingleClass++;
//                            sameInstances.put(s.getURI().toString(), countSingleClass);
//                        } else {
//                            sameInstances.put(s.getURI().toString(), 1);
//                        }
//                    }
//                }
//
//                allMapped = 0;
//                max = -1;
//                for (String s : countsPerClass.keySet()) {
//                    if (countsPerClass.get(s) > max) {
//                        best = s;
//                        max = countsPerClass.get(s);
//                    }
//                    allMapped += countsPerClass.get(s);
//                }
//                if (countInstance > 0) {
//                    mappedInstances = (double) max / countInstance;
//                }
//
//                for (String s : sameInstances.keySet()) {
//                    if (sameInstances.get(s) > maxInstance) {
//                        maxInstance = sameInstances.get(s);
//                    }
//                }
//
//                classesPerInstances = 0.0;
//                if (corres.size() > 0) {
//                    classesPerInstances = (double) countsPerClass.size() / (double) corres.size();
//                    classesPerInstances = 1 - classesPerInstances;
//                }
//                numberOfMapped = (double) countInstance / (double) getData().getWebtable().getTotalNumOfRows();
//                sameInstanceMappedRatio = maxInstance / (double) allMapped;
//                System.out.println("after class filtering: " + classesPerInstances + " - " + numberOfMapped + " - " + countInstance + " - " + sameInstanceMappedRatio + " - " + mappedInstances);
//                for (Correspondence<TableRow> check : result.getInstanceMappings()) {
//                    if (!check.isCorrect()) {
//                        if (check.getCorrectValue() != null) {
//                            String correct = check.getCorrectValue().toString();
//                            System.out.println("correct value for " + check.getFirst() + " - " + correct);
//                            for (TableRow t : combinedBoth2.getFirstDimension()) {
//                                for (TableRow t2 : combinedBoth2.getMatches(t)) {
//                                    //System.out.println("possible: " + t2.getURI());
//                                    if (correct.contains(t2.getURI().toString())) {
//                                        System.out.println("correct exists: " + t + " - " + t2.getURI() + " - " + combinedBoth2.get(t, t2) + " vs."
//                                                + check.getSecond().getURI() + " - " + combinedBoth2.get(t, check.getSecond()));
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                for (TableRow tr : matchedRows.keySet()) {
//                    boolean inGS = false;
//                    List<MatchingPair> mpList = matchedRows.get(tr);
//                    if (mpList.size() > 0) {
//                        Collections.sort(mpList, new ComparatorScore());
//                        for (Object o : getGoldStandard().getInstanceGoldStandard().keySet()) {
//                            Integer i = Integer.parseInt(o.toString());
//                            if (i == tr.getRowIndex()) {
//                                System.out.println("best found: " + tr + " - " + mpList.get(0).getMatchingPair().getSecond() + "- correct");
//                                inGS = true;
//                            }
//                        }
//                        if (!inGS) {
//                            System.out.println("best found: " + tr + " - " + mpList.get(0).getMatchingPair().getSecond() + "- incorrect");
//                        }
//                    }
//                    for (Correspondence<TableRow> corr : result.getInstanceMappings()) {
//                        if (corr.getFirst().equals(tr)) {
//                            System.out.println("before: " + tr + " - " + corr.getSecond() + " - " + corr.isCorrect());
//                        }
//                    }
//                }
                if (indirectPrecision || indirectRecall) {
                    //not only the final score, also value, label, #tables etc. could be useful
                    for (Correspondence<TableRow> corr : result.getInstanceMappings()) {
                        boolean indirectMatch = false;
                        if (matchedRows.get(corr.getFirst()) != null && matchedRows.get(corr.getFirst()).size() > 0) {
                            System.out.println("domi not null " + corr.getFirst() + " - " + matchedRows.get(corr.getFirst()));
                            for (MatchingPair<TableRow> mp : matchedRows.get(corr.getFirst())) {
                                System.out.println("mapped: " + corr.getFirst() + " to " + mp.getMatchingPair().getSecond() + " URI "
                                        + mp.getMatchingPair().getSecond().getURI() + " GS " + corr.getCorrectValue());
                                if (mp.getMatchingPair().getSecond().getURI().toString().equals(corr.getSecond().getURI().toString())) {

                                    System.out.println("score for mapping inst corr: " + getData().getWebtable() + "\t" + corr.getFirst() + "\t"
                                            + corr.getSecond().getURI() + "\t" + combinedWithWeight.get(corr.getFirst(), corr.getSecond()) + "\t"
                                            + mp.getLabelScore() + "\t" + mp.getValueScore() + "\t" + mp.getFinalScore() + "\t"
                                            + mp.getOccurenceFirstLabel() + "\t" + mp.getOccurenceFirstValue() + "\t"
                                            + mp.getOccurencePairLabel() + "\t" + mp.getOccurencePairValue() + "\t"
                                            + mp.getOtherPairsLabel() + "\t" + mp.getOtherPairsValue() + "\t" + mp.getOverallNumbTables() + "\t"
                                            + allCandis.get(corr.getFirst(), corr.getSecond()) + "\t" + values.get(corr.getFirst(), corr.getSecond()) + "\t" + combinedWithWeight.get(corr.getFirst(), corr.getSecond())
                                            + "\t" + corr.isCorrect());
                                    indirectMatch = true;
                                }
                            }
                        }
                        if (!indirectMatch) {
                            System.out.println("score for mapping inst: " + getData().getWebtable() + "\t" + corr.getFirst() + "\t"
                                    + corr.getSecond().getURI() + "\t" + combinedWithWeight.get(corr.getFirst(), corr.getSecond()) + "\t"
                                    + "NaN" + "\t" + mapped.size()
                                    + allCandis.get(corr.getFirst(), corr.getSecond()) + "\t" + values.get(corr.getFirst(), corr.getSecond()) + "\t" + combinedWithWeight.get(corr.getFirst(), corr.getSecond())
                                    + "\t" + corr.isCorrect());
                        }
                    }
                    System.out.println("indirect instance mapped combined: " + getData().getWebtable().getHeader() + "\t");
                }
                SimilarityMatrix<TableRow> matched = candidateSelection.mapInstances(beforePrunung, false, r1, webtable);

                for (TableRow r : matched.getFirstDimension()) {
                    Object o1 = evalRow.getUniqueIdentifier(r);
                    for (TableRow s : matched.getMatches(r)) {
                        Object o2 = evalInstance.getUniqueIdentifier(s);
                        if (!getGoldStandard().getInstanceGoldStandard().containsKey(o1)) {
                            System.out.println("find inst thres\t" + matched.get(r, s) + "\t" + "0");
                        } else if (getGoldStandard().getInstanceGoldStandard().get(o1).equals(o2)) {
                            System.out.println("find inst thres\t" + matched.get(r, s) + "\t" + "1");
                        } else {
                            System.out.println("find inst thres\t" + matched.get(r, s) + "\t" + "0");
                        }
                    }
                }
                SimilarityMatrix<TableColumn> matchedProp = candidateSelection.mapProperties(beforePrunungProps, webtable, false, r1);
                EvaluationAdapter<TableColumn> evalCol = new ColumnAdapter();
                EvaluationAdapter<TableColumn> evalPro = new DBpediaPropertyAdapter(
                        goldStandard.getPropertyCanoniser());

                for (TableColumn r : matchedProp.getFirstDimension()) {
                    Object o1 = evalCol.getUniqueIdentifier(r);
                    for (TableColumn s : matchedProp.getMatches(r)) {
                        Object o2 = evalPro.getUniqueIdentifier(s);
                        if (!getGoldStandard().getPropertyGoldStandard().containsKey(o1)) {
                            System.out.println("find prop thres\t" + matchedProp.get(r, s) + "\t" + "0");
                        } else if (getGoldStandard().getPropertyGoldStandard().get(o1).equals(o2)) {
                            System.out.println("find prop thres\t" + matchedProp.get(r, s) + "\t" + "1");
                        } else {
                            System.out.println("find prop thres\t" + matchedProp.get(r, s) + "\t" + "0");
                        }
                    }
                }

            }

            if ((Boolean) config.getValue(de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.PAR_EVALUATE)) {
                result.setWebtable(getData().getWebtable());
                result.setMatchingData(getData());
                result.setGoldStandard(getGoldStandard());
                result.setSimilarities(getSimilarities());

                result.getEvaluation().setInstanceBaseLine(candidateSelection.getInitialInstanceResult());
                //  result.getEvaluation().setMaxCorrectCandidates(candidateSelection.getMaxCorrectCanddiates());
                result.getEvaluation().setMaxCorrectCandidates(0);
                result.getEvaluation().setNumCandidates(getSimilarities().getInitialCandidateSimilarity().getNumberOfNonZeroElements());

                analyseResult(result);

                EvaluationAdapter<TableRow> evalInstance2 = new DBpediaInstanceAdapter(goldStandard.getInstanceCanoniser());

                for (Correspondence<TableRow> cor : result.getInstanceMappings()) {
                    System.out.println(cor.getFirst() + " - " + cor.getSecond() + " - " + cor.getSimilarity());

                    if (!cor.isCorrect()) {
                        String checkIncorrect = "";
                        checkIncorrect += cor.getFirst().getTable().getHeader() + "\t";
                        checkIncorrect += cor.getFirst().getKey() + "\t";
                        checkIncorrect += cor.getSecond().getKey() + "\t";
                        checkIncorrect += values.get(cor.getFirst(), cor.getSecond()) + "\t";
                        checkIncorrect += instanceStats.get(values).getHerfindahlIndex() + "\t";
                        checkIncorrect += allCandis.get(cor.getFirst(), cor.getSecond()) + "\t";
                        checkIncorrect += instanceStats.get(allCandis).getHerfindahlIndex() + "\t";
                        checkIncorrect += combinedWithWeight.get(cor.getFirst(), cor.getSecond()) + "\t";
                        if (idcm != null && idcm.getOverall1() != null) {
                            checkIncorrect += idcm.getOverall1().get(cor.getFirst(), cor.getSecond()) + "\t";
                        }
                        if (combinedBoth2 != null) {
                            checkIncorrect += combinedBoth2.get(cor.getFirst(), cor.getSecond()) + "\t";
                        }
                        if (candidateAbstractSelection.getCandidateAbstractSimilarity() != null && abstractMatrix != null) {
                            checkIncorrect += abstractMatrix.get(cor.getFirst(), cor.getSecond()) + "\t";
                        }
                        checkIncorrect += "0\t";

                        Object correctKey = cor.getCorrectValue();
                        checkIncorrect += correctKey + "\t";
                        checkIncorrect += evalInstance2.getUniqueIdentifier(cor.getSecond()) + "\t";
                        if (result.getClassMappings().iterator().hasNext()) {
                            checkIncorrect += result.getClassMappings().iterator().next().getSecond().getHeader() + "\t";
                        }
//                        if (correctKey != null) {
//                            checkIncorrect += correctKey + "\t";
//                            checkIncorrect += "correctExists\t";
//                            if (!isInData(correctKey)) {
//                                checkIncorrect += "notInLucene\t";
//                            } else if (!isInCandidateList(correctKey)) {
//                                checkIncorrect += "notInCandidate\t";
//                            }
//                        } else {
//                            checkIncorrect += "correctNotExists\t";
//                        }
                        if (result.getClassMappings().iterator().hasNext()) {
                            if (result.getClassMappings().iterator().next().isCorrect()) {
                                checkIncorrect += "1\t";
                            } else {
                                checkIncorrect += "0\t";
                            }
                        }

                        if (result.getClassMappings().iterator().hasNext()) {
                            if (result.getClassMappings().iterator().next().getSecond().equals(cor.getSecond().getTable())) {
                                checkIncorrect += "1\t";
                            } else {
                                checkIncorrect += "0\t";
                            }
                        }

                        boolean indirectMatch = false;
                        if (matchedRows != null && matchedRows.get(cor.getFirst()) != null) {
                            for (MatchingPair<TableRow> mp : matchedRows.get(cor.getFirst())) {
                                if (mp.getMatchingPair().getSecond().getURI().equals(cor.getSecond().getURI())) {

                                    checkIncorrect
                                            += +mp.getLabelScore() + "\t" + mp.getValueScore() + "\t" + mp.getFinalScore() + "\t"
                                            + mp.getOccurenceFirstLabel() + "\t" + mp.getOccurenceFirstValue() + "\t"
                                            + mp.getOccurencePairLabel() + "\t" + mp.getOccurencePairValue() + "\t"
                                            + mp.getOtherPairsLabel() + "\t" + mp.getOtherPairsValue() + "\t" + mp.getOverallNumbTables();
                                    indirectMatch = true;
                                }
                            }
                        }
                        if (!indirectMatch) {
                            checkIncorrect
                                    += 0 + "\t" + 0 + "\t" + 0 + "\t"
                                    + 0 + "\t" + 0 + "\t"
                                    + 0 + "\t" + 0 + "\t"
                                    + 0 + "\t" + 0 + "\t" + 0;//mapped.size();
                        }
                        System.out.println("check instance wrong: " + checkIncorrect);
                    } else {
                        String checkIncorrect = "";
                        checkIncorrect += cor.getFirst().getTable().getHeader() + "\t";
                        checkIncorrect += cor.getFirst().getKey() + "\t";
                        checkIncorrect += cor.getSecond().getKey() + "\t";
                        checkIncorrect += values.get(cor.getFirst(), cor.getSecond()) + "\t";
                        checkIncorrect += instanceStats.get(values).getHerfindahlIndex() + "\t";
                        checkIncorrect += allCandis.get(cor.getFirst(), cor.getSecond()) + "\t";
                        checkIncorrect += instanceStats.get(allCandis).getHerfindahlIndex() + "\t";
                        checkIncorrect += combinedWithWeight.get(cor.getFirst(), cor.getSecond()) + "\t";
                        if (idcm != null && idcm.getOverall1() != null) {
                            checkIncorrect += idcm.getOverall1().get(cor.getFirst(), cor.getSecond()) + "\t";
                        }
                        if (combinedBoth2 != null) {
                            checkIncorrect += combinedBoth2.get(cor.getFirst(), cor.getSecond()) + "\t";
                        }
                        if (candidateAbstractSelection.getCandidateAbstractSimilarity() != null && abstractMatrix != null) {
                            checkIncorrect += abstractMatrix.get(cor.getFirst(), cor.getSecond()) + "\t";
                        }
                        checkIncorrect += "1\t";

                        Object correctKey = cor.getCorrectValue();
                        checkIncorrect += correctKey + "\t";
                        checkIncorrect += evalInstance2.getUniqueIdentifier(cor.getSecond()) + "\t";
                        if (result.getClassMappings().iterator().hasNext()) {
                            checkIncorrect += result.getClassMappings().iterator().next().getSecond().getHeader() + "\t";
                        }
//                        if (correctKey != null) {
//                            checkIncorrect += correctKey + "\t";
//                            checkIncorrect += "correctExists\t";
//                            if (!isInData(correctKey)) {
//                                checkIncorrect += "notInLucene\t";
//                            } else if (!isInCandidateList(correctKey)) {
//                                checkIncorrect += "notInCandidate\t";
//                            }
//                        } else {
//                            checkIncorrect += "correctNotExists\t";
//                        }
                        if (result.getClassMappings().iterator().hasNext()) {
                            if (result.getClassMappings().iterator().next().isCorrect()) {
                                checkIncorrect += "1\t";
                            } else {
                                checkIncorrect += "0\t";
                            }
                        }
                        if (result.getClassMappings().iterator().hasNext()) {
                            if (result.getClassMappings().iterator().next().getSecond().equals(cor.getSecond().getTable())) {
                                checkIncorrect += "1\t";
                            } else {
                                checkIncorrect += "0\t";
                            }
                        }

                        boolean indirectMatch = false;
                        if (matchedRows != null && matchedRows.get(cor.getFirst()) != null) {
                            for (MatchingPair<TableRow> mp : matchedRows.get(cor.getFirst())) {
                                if (mp.getMatchingPair().getSecond().getURI().equals(cor.getSecond().getURI())) {

                                    checkIncorrect
                                            += +mp.getLabelScore() + "\t" + mp.getValueScore() + "\t" + mp.getFinalScore() + "\t"
                                            + mp.getOccurenceFirstLabel() + "\t" + mp.getOccurenceFirstValue() + "\t"
                                            + mp.getOccurencePairLabel() + "\t" + mp.getOccurencePairValue() + "\t"
                                            + mp.getOtherPairsLabel() + "\t" + mp.getOtherPairsValue() + "\t" + mp.getOverallNumbTables();
                                    indirectMatch = true;
                                }
                            }
                        }
                        if (!indirectMatch) {
                            checkIncorrect
                                    += 0 + "\t" + 0 + "\t" + 0 + "\t"
                                    + 0 + "\t" + 0 + "\t"
                                    + 0 + "\t" + 0 + "\t"
                                    + 0 + "\t" + 0 + "\t" + 0; //mapped.size();
                        }
                        System.out.println("check instance correct: " + checkIncorrect);
                    }

                }

                for (Map.Entry<Object, Object> e : getGoldStandard().getInstanceGoldStandard().entrySet()) {
                    String checkIncorrect = "";
                    SimilarityMatrix<TableRow> sim = similarities.getInitialCandidateSimilarity();

                    for (TableRow tr : getData().getWebtableRowSet()) {
                        if (!tr.getTable().getHeader().equals(getData().getWebtable().getHeader())) {
                            continue;
                        }
                        boolean foundInCandidates = false, detected = false;
                        if (tr.getRowIndexInFile() == Integer.parseInt(e.getKey().toString())) {
                            checkIncorrect += tr.getTable().getHeader() + "\t";
                            checkIncorrect += tr.getKey() + "\t";
                            checkIncorrect += tr.getCells() + "\t";
                            for (Correspondence<Table> corTable : result.getClassMappings()) {
                                checkIncorrect += corTable.getSecond() + "---" + corTable.isCorrect() + "\t";
                            }
                            for (Correspondence<TableColumn> corProp : result.getPropertyMappings()) {
                                checkIncorrect += corProp.getFirst() + "---" + corProp.getSecond() + "---" + corProp.isCorrect() + "\t";
                            }
                            checkIncorrect += "a" + e.getValue() + "\t";
                            for (TableRow candidates : sim.getMatches(tr)) {
                                if (e.getValue().equals(evalInstance2.getUniqueIdentifier(candidates))) {
                                    checkIncorrect += "b" + combinedWithWeight.get(tr, candidates) + "\t";
                                    checkIncorrect += "f" + beforePrunung.get(tr, candidates) + "\t";
                                    foundInCandidates = true;
                                }
                            }

                            for (Correspondence<TableRow> cor : result.getInstanceMappings()) {
                                if (cor.getFirst().getRowIndexInFile() == tr.getRowIndexInFile() && evalInstance2.getUniqueIdentifier(cor.getSecond()).equals(e.getValue())) {
                                    detected = true;
                                } else {
                                    if (cor.getFirst().getRowIndexInFile() == tr.getRowIndexInFile()
                                            && !evalInstance2.getUniqueIdentifier(cor.getSecond()).equals(e.getValue())) {
                                        checkIncorrect += "c" + cor.getSecond().getURI() + "\t";
                                        checkIncorrect += "d" + cor.getSimilarity() + "\t";
                                    }
                                }
                            }

                            if (foundInCandidates && !detected) {
                                checkIncorrect += "wrongChoice\t";
                            } else if (!detected && !foundInCandidates) {
                                checkIncorrect += "noCandidate\t";
                            }
                            if (!detected) {
                                System.out.println("check instance FN: " + checkIncorrect);
                            }
                        }
                    }
                }

                EvaluationAdapter<TableColumn> evalRow = new ColumnAdapter();
                EvaluationAdapter<TableColumn> evalInstance = new DBpediaPropertyAdapter(getGoldStandard().getPropertyCanoniser());

                boolean noMappings = false;
                List<TableColumn> unmappedCols = new ArrayList<>();
                if (result.getClassMappings().size() > 0) {
                    for (TableColumn tc : getData().getWebtable().getColumns()) {
                        if (tc.isKey()) {
                            continue;
                        }
                        boolean inCorres = false;
                        for (Correspondence<TableColumn> corresProp : result.getPropertyMappings()) {
                            if (corresProp.getFirst().equals(tc)) {
                                inCorres = true;
                            }
                        }
                        if (!inCorres) {
                            noMappings = true;
                            unmappedCols.add(tc);
                            System.out.println("unmatched col: " + tc.getTable().getHeader() + " - " + tc.getHeader() + " class: " + result.getClassMappings().iterator().next().getSecond().getHeader());
                        }
                    }
                }

                Map<TableColumn, List<MatchingPair>> matchedCols = null;
                SparseSimilarityMatrix<TableColumn> combinedBothProp = null;

                for (Object o : goldStandard.getInstanceGoldStandard().values()) {

                    if (result.getClassMappings().iterator().hasNext()) {
                        Table mappedTab = result.getClassMappings().iterator().next().getSecond();
                        if (mappedTab != null) {
                            System.out.println("all instances: " + o + "\t" + data.getUriMap().get(o.toString()) + "\t" + mappedTab.getHeader());
                        } else {
                            System.out.println("all instances: " + o + "\t" + data.getUriMap().get(o.toString()) + "\tno");
                        }
                    }
                }

                if (!filtered) {
                    if (indirectPrecision || indirectRecall) {
                        IndirectSchemaMatcher ism = new IndirectSchemaMatcher();
                        SimilarityMatrix<TableColumn> indirectMappingsProp = ism.computeIndirectMappings(matchingParameters, rootTimer, data, config, result, goldStandard, matchers, logger, evaluationParameters, unmappedCols, mapped);

                        CombineNonOverlapping combProp = new CombineNonOverlapping();
                        combProp.setAggregationType(CombinationType.Sum);
                        combinedBothProp = (SparseSimilarityMatrix<TableColumn>) combProp.match(propertySim, indirectMappingsProp);

                        candidateSelection.mapProperties(combinedBothProp.copy(), webTableName, true, result);

                        for (TableColumn tc : combinedBothProp.getFirstDimension()) {
                            TableColumn tx = combinedBothProp.getBestPair(tc);
                            for (Correspondence c : result.getPropertyMappings()) {
                                if (c.getFirst().equals(tc) && c.getSecond().equals(tx)) {
                                    if (c.isCorrect()) {
                                        System.out.println("stats unmapped props " + tc + "\t" + tx + "\t" + propertySim.get(tc, tx) + "\t" + indirectMappingsProp.get(tc, tx) + "\t" + tc.getDataType() + ism.getOverallLabel() + "\t" + ism.getOverallValue() + "\t1");
                                    } else {
                                        System.out.println("stats unmapped props " + tc + "\t" + tx + "\t" + propertySim.get(tc, tx) + "\t" + indirectMappingsProp.get(tc, tx) + "\t" + tc.getDataType() + ism.getOverallLabel() + "\t" + ism.getOverallValue() + "\t0");
                                    }
                                }
                            }
                        }
                    }

//                    SparseSimilarityMatrix<TableColumn> propertySimSparse = (SparseSimilarityMatrix<TableColumn>) propertySim;
//                    for (TableColumn tc : combinedBothProp.getFirstDimension()) {
//                        for (TableColumn tx : combinedBothProp.getMatches(tc)) {
//                            int type = 0;
//                            if (!indirectWithSameProperties.getFirstDimension().contains(tc)
//                                    || indirectWithSameProperties.get(tc, combinedBothProp.getBestPair(tc)) == null) {
//                                type = 3;
//                            } else {
//                                if (!propertySimSparse.getFirstDimension().contains(tc)
//                                        || propertySimSparse.get(tc, combinedBothProp.getBestPair(tc)) == null) 
//                                    type = 4;
//                                } else {
//                                    if (evalInstance.getUniqueIdentifier(combinedBothProp.getBestPair(tc)).equals(
//                                            evalInstance.getUniqueIdentifier(propertySimSparse.getBestPair(tc)))
//                                            && !evalInstance.getUniqueIdentifier(combinedBothProp.getBestPair(tc)).equals(
//                                                    evalInstance.getUniqueIdentifier(indirectWithSameProperties.getBestPair(tc)))) {
//                                        type = 1;
//                                    } else {
//                                        if (evalInstance.getUniqueIdentifier(combinedBothProp.getBestPair(tc)).equals(
//                                                evalInstance.getUniqueIdentifier(indirectWithSameProperties.getBestPair(tc)))
//                                                && !evalInstance.getUniqueIdentifier(combinedBothProp.getBestPair(tc)).equals(
//                                                        evalInstance.getUniqueIdentifier(propertySimSparse.getBestPair(tc)))) {
//                                            type = 2;
//                                        }
//                                    }
//                                }
//                            }
//
//                            boolean correct = false;
//                            if (goldStandard.getPropertyGoldStandard().get(data.getWebtable().getColumns().indexOf(tc)) != null
//                                    && goldStandard.getPropertyGoldStandard().get(data.getWebtable().getColumns().indexOf(tc)).equals(evalInstance.getUniqueIdentifier(tx))) {
//                                correct = true;
//                            }
//
//                            System.out.println("combined prop overlap after: " + data.getWebtable().getHeader() + "\t" + tc + "\t" + tx + "\t" + combinedBothProp.get(tc, tx)
//                                    + "\t" + propertySim.get(tc, tx) + "\t" + indirectMappingsProp.get(tc, tx) + "\t" + type + "\t" + correct);
//                        }
//                    }
                    //currently not!
                    if (indirectPrecision) {
                        IndirectSchemaMatcherMapped idsm = new IndirectSchemaMatcherMapped();
                        matchedCols = idsm.computeIndirectMappings(matchingParameters, rootTimer, data, config, result, goldStandard, matchers, logger, evaluationParameters, unmappedCols, mapped);
                        System.out.println("domi matched cols size: " + matchedCols.size());

                        //not only the final score, also value, label, #tables etc. could be useful
                        for (Correspondence<TableColumn> corr : result.getPropertyMappings()) {
                            boolean indirectMatch = false;
                            if (matchedCols.get(corr.getFirst()) != null) {
                                System.out.println("domi not null " + corr.getFirst() + " - " + matchedCols.get(corr.getFirst()));
                                for (MatchingPair<TableColumn> mp : matchedCols.get(corr.getFirst())) {
                                    if (evalInstance.getUniqueIdentifier(mp.getMatchingPair().getSecond()).equals(evalInstance.getUniqueIdentifier(corr.getSecond()))) {

                                        System.out.println("score for mapping props: " + getData().getWebtable() + "\t" + corr.getFirst() + "\t"
                                                + evalInstance.getUniqueIdentifier(corr.getSecond()) + "\t" + propertySim.get(corr.getFirst(), corr.getSecond()) + "\t"
                                                + mp.getLabelScore() + "\t" + mp.getValueScore() + "\t" + mp.getFinalScore() + "\t"
                                                + mp.getOccurenceFirstLabel() + "\t" + mp.getOccurenceFirstValue() + "\t"
                                                + mp.getOccurencePairLabel() + "\t" + mp.getOccurencePairValue() + "\t"
                                                + mp.getOtherPairsLabel() + "\t" + mp.getOtherPairsValue() + "\t" + mp.getOverallNumbTables() + "\t"
                                                + propertySim.get(corr.getFirst(), corr.getSecond()) + "\t" + props.get(corr.getFirst(), corr.getSecond()) + "\t" + labelSimilarity.get(corr.getFirst(), corr.getSecond())
                                                + "\t" + corr.isCorrect());
                                        indirectMatch = true;
                                    }
                                }
                            }
                            if (!indirectMatch) {
                                System.out.println("score for mapping props: " + getData().getWebtable() + "\t" + corr.getFirst() + "\t"
                                        + evalInstance.getUniqueIdentifier(corr.getSecond()) + "\t" + propertySim.get(corr.getFirst(), corr.getSecond()) + "\t"
                                        + "NaN" + "\t" + mapped.size()
                                        + +propertySim.get(corr.getFirst(), corr.getSecond()) + "\t" + props.get(corr.getFirst(), corr.getSecond()) + "\t" + labelSimilarity.get(corr.getFirst(), corr.getSecond())
                                        + "\t" + corr.isCorrect());
                            }
                        }
                    }
                }

                for (Correspondence<TableColumn> cor : result.getPropertyMappings()) {

                    if (!cor.isCorrect()) {
                        String checkIncorrect = "";
                        checkIncorrect += cor.getFirst().getTable().getHeader() + "\t";
                        checkIncorrect += "0\t";
                        checkIncorrect += cor.getFirst().getDataType() + "\t";
                        checkIncorrect += cor.getFirst().getHeader().toString() + "\t";
                        checkIncorrect += cor.getFirst().getValues().get(cor.getFirst().getTable().getColumns().indexOf(cor.getFirst())) + "\t";
                        checkIncorrect += evalInstance.getUniqueIdentifier(cor.getSecond()) + "\t";
                        checkIncorrect += cor.getSecond().getValues().get(cor.getSecond().getTable().getColumns().indexOf(cor.getSecond())) + "\t";

                        Double propValueToWrite = null;
                        if (props.get(cor.getFirst(), cor.getSecond()) == null) {
                            for (TableColumn t : props.getMatches(cor.getFirst())) {
                                if (t.getURI().equals(cor.getSecond().getURI())) {
                                    propValueToWrite = props.get(cor.getFirst(), t);
                                }
                            }
                        } else {
                            propValueToWrite = props.get(cor.getFirst(), cor.getSecond());
                        }
                        checkIncorrect += propValueToWrite + "\t";
                        checkIncorrect += stats.get(props).getMean() + "\t";

                        Double labelValueToWrite = null;
                        if (labelSimilarity.get(cor.getFirst(), cor.getSecond()) == null) {
                            for (TableColumn t : labelSimilarity.getMatches(cor.getFirst())) {
                                if (t.getURI().equals(cor.getSecond().getURI())) {
                                    labelValueToWrite = labelSimilarity.get(cor.getFirst(), t);
                                }
                            }
                        } else {
                            labelValueToWrite = labelSimilarity.get(cor.getFirst(), cor.getSecond());
                        }
                        checkIncorrect += labelValueToWrite + "\t";

                        checkIncorrect += stats.get(labelSimilarity).getMean() + "\t";
                        checkIncorrect += kurt.get(cor.getFirst(), cor.getSecond()) + "\t";
                        checkIncorrect += stats.get(kurt).getMean() + "\t";
                        checkIncorrect += freqDT.get(cor.getFirst(), cor.getSecond()) + "\t";
                        checkIncorrect += classStatsMatrix.get(cor.getFirst(), cor.getSecond()) + "\t";
                        checkIncorrect += propStatsMatrix.get(cor.getFirst(), cor.getSecond()) + "\t";
                        checkIncorrect += countDT.get(cor.getFirst(), cor.getSecond()) + "\t";
                        checkIncorrect += propertySim.get(cor.getFirst(), cor.getSecond()) + "\t";
                        checkIncorrect += cor.getFirst().getColumnStatistic().getKurtosis() + "\t";

//                        checkIncorrect += cor.getFirst().getColumnStatistic().getMinimalValue() + "\t";
//                        checkIncorrect += cor.getFirst().getColumnStatistic().getMaximalValue() + "\t";
//                        checkIncorrect += cor.getFirst().getColumnStatistic().getAverage() + "\t";
//                        checkIncorrect += cor.getFirst().getColumnStatistic().getStandardDeviation() + "\t";
//                        checkIncorrect += cor.getFirst().getColumnStatistic().getVariance() + "\t";
                        if (cor.getSecond().getEquivBefore() != null) {
//                            System.out.println("equi used: " + cor.getSecond().getEquivBefore().getURI() + " class: " + cor.getSecond().getTable() + " table ID " + cor.getFirst().getTable().getHeader());
//                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getMinimalValue() + "\t";
//                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getMaximalValue() + "\t";
//                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getAverage() + "\t";
//                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getStandardDeviation() + "\t";
//                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getVariance() + "\t";
//                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getLowerPercentile() + "\t";
//                            checkIncorrect += cor.getSecond().getEquivBefore().getColumnStatistic().getUpperPercentile() + "\t";
                            if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
                                if ((double) cor.getSecond().getEquivBefore().getColumnStatistic().getMinimalValue() <= cor.getFirst().getColumnStatistic().getAverage()
                                        && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getEquivBefore().getColumnStatistic().getMaximalValue()) {
                                    checkIncorrect += "1\t";
                                } else {
                                    checkIncorrect += "-1.0\t";
                                }
                                if ((double) cor.getSecond().getEquivBefore().getColumnStatistic().getLowerPercentile() <= cor.getFirst().getColumnStatistic().getAverage()
                                        && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getEquivBefore().getColumnStatistic().getUpperPercentile()) {
                                    checkIncorrect += "1\t";
                                } else {
                                    checkIncorrect += "-1.0\t";
                                }
                            } else {
                                checkIncorrect += "0\t";
                                checkIncorrect += "0\t";
                            }
                        } else {
//                            Correspondence<Table> decidedClass = result.getClassMappings().iterator().next();
//                            TableColumn propertyUsed = decidedClass.getSecond().getColumn(cor.getSecond().getHeader().toString() + " table ID " + cor.getFirst().getTable().getHeader());
//                            System.out.println("no equi used: " + cor.getSecond().getURI() + " class: " + cor.getSecond().getTable());
////                            checkIncorrect += cor.getSecond().getColumnStatistic().getMinimalValue() + "\t";
////                            checkIncorrect += cor.getSecond().getColumnStatistic().getMaximalValue() + "\t";
////                            checkIncorrect += cor.getSecond().getColumnStatistic().getAverage() + "\t";
////                            checkIncorrect += cor.getSecond().getColumnStatistic().getStandardDeviation() + "\t";
////                            checkIncorrect += cor.getSecond().getColumnStatistic().getVariance() + "\t";
////                            checkIncorrect += cor.getSecond().getColumnStatistic().getLowerPercentile() + "\t";
////                            checkIncorrect += cor.getSecond().getColumnStatistic().getUpperPercentile() + "\t";
                            if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
                                if ((double) cor.getSecond().getColumnStatistic().getMinimalValue() <= cor.getFirst().getColumnStatistic().getAverage()
                                        && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getColumnStatistic().getMaximalValue()) {
                                    checkIncorrect += "1\t";
                                } else {
                                    checkIncorrect += "-1.0\t";
                                }
                                if ((double) cor.getSecond().getColumnStatistic().getLowerPercentile() <= cor.getFirst().getColumnStatistic().getAverage()
                                        && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getColumnStatistic().getUpperPercentile()) {
                                    checkIncorrect += "1\t";
                                } else {
                                    checkIncorrect += "-1.0\t";
                                }
                            } else {
                                checkIncorrect += "0\t";
                                checkIncorrect += "0\t";
                            }
                        }
                        if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
                            TTest ttest = new TTest();
                            List<Double> tableValues = new ArrayList<>();
                            for (Object o : cor.getFirst().getValues().values()) {
                                if (o instanceof List) {
                                    List<Double> list = (List<Double>) o;
                                    tableValues.addAll(list);
                                } else {
                                    Double value = (double) o;
                                    tableValues.add(value);
                                }
                            }
                            double[] valuesTable = Doubles.toArray(tableValues);

                            List<Double> propValues = new ArrayList<>();
                            for (Object o : cor.getSecond().getValues().values()) {
                                if (o instanceof List) {
                                    List<Double> list = (List<Double>) o;
                                    propValues.addAll(list);
                                } else {
                                    Double value = (double) o;
                                    propValues.add(value);
                                }
                            }
                            double[] valuesProp = Doubles.toArray(propValues);
                            try {
                                checkIncorrect += ttest.t(valuesTable, valuesProp) + "\t";
                            } catch (Exception e) {
                                checkIncorrect += "-5\t";
                            }
                            double countIntTable = 0;
                            double countIntDBpedia = 0;
                            for (Object o : cor.getSecond().getValues().values()) {
                                try {
                                    if (!(o instanceof List) && (o instanceof Double)) {
                                        if ((double) o % 1 == 0) {
                                            countIntDBpedia++;
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("error 1" + e);
                                }
                            }
                            for (Object o : cor.getFirst().getValues().values()) {
                                try {
                                    if (!(o instanceof List) && (o instanceof Double)) {
                                        if ((double) o % 1 == 0) {
                                            countIntTable++;
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("error 2" + e);
                                }
                            }
                            countIntDBpedia = countIntDBpedia / (double) cor.getSecond().getValues().size();
                            countIntTable = countIntTable / (double) cor.getFirst().getValues().size();
//                            if(countIntDBpedia > 0.9 && countIntTable < 0.9) {
//                                checkIncorrect += "0\t";
//                            }
//                            else {
//                                checkIncorrect += "1\t";
//                            }
                            double diff = countIntDBpedia - countIntTable;
                            checkIncorrect += diff + "\t";
                            System.out.println("counts: " + countIntDBpedia + "  vs. " + countIntTable + " sizes " + cor.getSecond().getValues().size() + " vs. " + cor.getFirst().getValues().size());
                        } else {
                            checkIncorrect += "-5\t";
//                            checkIncorrect += "-8\t";
                            checkIncorrect += "0\t";
                        }

                        double distValueDBpedia = (double) cor.getSecond().getColumnStatistic().getDistinctValues() / (double) cor.getSecond().getValues().size();
                        double distValueDBpediaList = (double) cor.getSecond().getColumnStatistic().getDistinctValuesWithoutLists() / (double) cor.getSecond().getValues().size();
                        double distValueWT = (double) cor.getFirst().getColumnStatistic().getDistinctValuesWithoutLists() / (double) cor.getFirst().getValues().size();
                        double diff1, diff2;
                        diff1 = distValueDBpedia - distValueWT;
                        diff2 = distValueDBpediaList - distValueWT;
                        checkIncorrect += diff1 + "\t";
                        checkIncorrect += diff2 + "\t";

                        if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.string) {

                            Collection<Object> values1 = cor.getSecond().getValues().values();
                            Collection<Object> allValues = new HashSet<>();

                            if (cor.getSecond().getEquivBefore() != null) {
                                //System.out.println("equiv props: " + cor.getSecond().getURI() + " -- " + cor.getSecond().getEquivBefore().getURI());
                                Collection<Object> values2 = cor.getSecond().getEquivBefore().getValues().values();
                                allValues.addAll(values2);
                            } else {
                                allValues.addAll(values1);
                            }

                            Map<String, Integer> countsValuesDBpedia = new TreeMap<>();
                            for (Object o : allValues) {
                                if (o instanceof List) {
                                    List<String> list = (List<String>) o;
                                    try {
                                        for (String value : list) {
                                            if (value.contains(" ") || value.contains("_")) {
                                                String[] splittedValues;
                                                if (value.contains(" ")) {
                                                    splittedValues = value.split("\\s");
                                                } else {
                                                    splittedValues = value.split("_");
                                                }
                                                for (String s : splittedValues) {
                                                    s = s.replace("(", "");
                                                    if (!StopWordRemover.isStopWord(s)) {
                                                        if (countsValuesDBpedia.containsKey(s)) {
                                                            Integer current = countsValuesDBpedia.get(s);
                                                            current++;
                                                            countsValuesDBpedia.put(s, current);
                                                        } else {
                                                            countsValuesDBpedia.put(s, 1);
                                                        }
                                                    }
                                                }
                                            } else {
                                                value = StringNormalizer.normaliseValue(value, true);
                                                if (countsValuesDBpedia.containsKey(value)) {
                                                    Integer current = countsValuesDBpedia.get(value);
                                                    current++;
                                                    countsValuesDBpedia.put(value, current);
                                                } else {
                                                    countsValuesDBpedia.put(value, 1);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                    }
                                } else if (o instanceof String) {
                                    String value = (String) o;
                                    value = StringNormalizer.normaliseValue(value, true);
                                    if (value.contains(" ") || value.contains("_")) {
                                        String[] splittedValues;
                                        if (value.contains(" ")) {
                                            splittedValues = value.split("\\s");
                                        } else {
                                            splittedValues = value.split("_");
                                        }
                                        for (String s : splittedValues) {
                                            if (!StopWordRemover.isStopWord(s)) {
                                                if (countsValuesDBpedia.containsKey(s)) {
                                                    Integer current = countsValuesDBpedia.get(s);
                                                    current++;
                                                    countsValuesDBpedia.put(s, current);
                                                } else {
                                                    countsValuesDBpedia.put(s, 1);
                                                }
                                            }
                                        }
                                    } else {
                                        if (countsValuesDBpedia.containsKey(value)) {
                                            Integer current = countsValuesDBpedia.get(value);
                                            current++;
                                            countsValuesDBpedia.put(value, current);
                                        } else {
                                            countsValuesDBpedia.put(value, 1);
                                        }
                                    }
                                }
                            }
                            //System.out.println("count values: " + countsValues);

                            double sum = 0.0;

                            Map<String, Integer> countsValuesWT = new TreeMap<>();

                            //System.out.println("values table: " + cor.getFirst().getValues().values());
                            for (Object o : cor.getFirst().getValues().values()) {
                                //System.out.println(o);
                                if (o instanceof List) {
                                    List<String> list = (List<String>) o;
                                    for (String s : list) {
                                        if (s.contains(",")) {
                                            s = s.split(",")[0];
                                            s = s.trim();
                                        }
                                        if (s.contains("/")) {
                                            s = s.split("/")[0];
                                            s = s.trim();
                                        }
                                        if (s.contains(" ")) {
                                            String[] splittedValues = s.split("\\s");
                                            for (String t : splittedValues) {
                                                if (!StopWordRemover.isStopWord(t)) {
                                                    if (countsValuesDBpedia.containsKey(t)) {
                                                        //sum += countsValuesDBpedia.get(t);
                                                        if (countsValuesWT.containsKey(t)) {
                                                            Integer current = countsValuesWT.get(t);
                                                            current++;
                                                            countsValuesWT.put(t, current);
                                                        } else {
                                                            countsValuesWT.put(t, 1);
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            if (countsValuesDBpedia.containsKey(s)) {
                                                //sum += countsValuesDBpedia.get(s);
                                                if (countsValuesWT.containsKey(s)) {
                                                    Integer current = countsValuesWT.get(s);
                                                    current++;
                                                    countsValuesWT.put(s, current);
                                                } else {
                                                    countsValuesWT.put(s, 1);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    String s = (String) o;
                                    if (s.contains(",")) {
                                        s = s.split(",")[0];
                                        s = s.trim();
                                    }
                                    if (s.contains("/")) {
                                        s = s.split("/")[0];
                                        s = s.trim();
                                    }
                                    s = s.trim();
                                    if (s.contains(" ")) {
                                        String[] splittedValues = s.split("\\s");
                                        for (String t : splittedValues) {
                                            if (!StopWordRemover.isStopWord(t)) {
                                                if (countsValuesDBpedia.containsKey(t)) {
                                                    //sum += countsValuesDBpedia.get(t);
                                                    if (countsValuesWT.containsKey(t)) {
                                                        Integer current = countsValuesWT.get(t);
                                                        current++;
                                                        countsValuesWT.put(t, current);
                                                    } else {
                                                        countsValuesWT.put(t, 1);
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        if (countsValuesDBpedia.containsKey(s)) {
                                            //sum += countsValuesDBpedia.get(s);
                                            if (countsValuesWT.containsKey(s)) {
                                                Integer current = countsValuesWT.get(s);
                                                current++;
                                                countsValuesWT.put(s, current);
                                            } else {
                                                countsValuesWT.put(s, 1);
                                            }
                                        }
                                    }
                                }
                            }

                            //System.out.println("calculate cosine: " + cor.getSecond().getURI() + " of class " + cor.getSecond().getTable().getHeader() + " with " + cor.getFirst().getHeader());
                            checkIncorrect += calculateCosineSimilarity(countsValuesDBpedia, countsValuesWT) + "\t";

//                            sum = 0.0;
//
//                            try {
//                                //SoftballLeague_currentSeason    {2015 Men's Softball World Championship=1, 2013 NCAA Division III Softball Championship=1, 2013 NCAA Division II Softball Championship=1}
//                                BufferedReader read = new BufferedReader(new FileReader(new File("/backup-local-home/dritze/newGS/recognizer/DBpedia/dbpediaStatsString.csv")));
//                                String wholeLine = read.readLine();
//                                String className = "";
//                                for (Correspondence<Table> corTable : result.getClassMappings()) {
//                                    className = corTable.getSecond().getHeader();
//                                    className = className.replace(".tar.gz", "");
//                                    className = className.replace(".csv", "");
//                                    className = className.replace(".gz", "");
//                                }
//                                Set<String> urisToCheck = new HashSet<>();
//                                Canoniser c = new Canoniser();
//                                Collection<List<String>> equiProps = c.loadEquivalentResourcesExternal(getEvaluationParameters().getEquivalentPropertiesLocation());
//                                for (List l : equiProps) {
//                                    if (l.contains(cor.getSecond().getURI())) {
//                                        //System.out.println("uris to check! " + l);
//                                        urisToCheck.addAll(l);
//                                    }
//                                }
//
//                                //System.out.println("class prop name second: " + className + "_" + cor.getSecond().getURI().replace("http://dbpedia.org/ontology/", ""));
//                                //System.out.println("class prop name equi: " + className + "_" + evalInstance.getUniqueIdentifier(cor.getSecond()).toString().replace("http://dbpedia.org/ontology/", ""));
//                                Set<String> lineWithProp = new HashSet<>();
//                                while (wholeLine != null) {
//                                    if (!urisToCheck.isEmpty()) {
//                                        //System.out.println("not empty");
//                                        for (String s : urisToCheck) {
//                                            System.out.println("check " + s);
//                                            if (wholeLine.startsWith(className + "_" + s.replace("http://dbpedia.org/ontology/", "") + "\t")
//                                                    || wholeLine.startsWith(className + "_" + s.replace("http://dbpedia.org/ontology/", "") + "_label\t")) {
//                                                lineWithProp.add(wholeLine);
//                                                //    System.out.println("found");
//                                            }
//                                        }
//                                    } else {
//                                        if (wholeLine.startsWith(className + "_" + cor.getSecond().getURI().replace("http://dbpedia.org/ontology/", "") + "\t")
//                                                || wholeLine.startsWith(className + "_" + cor.getSecond().getURI().replace("http://dbpedia.org/ontology/", "") + "_label\t")) {
//                                            lineWithProp.add(wholeLine);
//                                            // System.out.println("found single: " + cor.getSecond().getURI());
//                                            break;
//                                        }
//                                        //continue;
//                                    }
//
////                                    if (wholeLine.startsWith(className + "_" + evalInstance.getUniqueIdentifier(cor.getSecond()).toString().replace("http://dbpedia.org/ontology/", ""))) {
////                                        secondLineWithProp = wholeLine;
////                                        //break;
////                                    }
//                                    wholeLine = read.readLine();
//                                }
//                                //System.out.println("line with prop " +lineWithProp);
//                                if (!lineWithProp.isEmpty()) {
//                                    for (String check : lineWithProp) {
//                                        String[] countArray = new String[0];
//                                        String[] countArray2 = new String[0];
//                                        String[] line = check.split("\t");
//                                        countArray = line[1].split(",");
//                                        Map<String, Integer> counts = new HashMap<>();
//                                        for (String s : countArray) {
//                                            s = s.replace("}", "");
//                                            s = s.replace("{", "");
//                                            String[] entry = s.split("=");
//                                            counts.put(StringNormalizer.normaliseValue(entry[0], true), Integer.parseInt(entry[1]));
//                                            //System.out.println("entry: " + StringNormalizer.normaliseValue(entry[0], true));
//                                        }
//                                        for (String s : countArray2) {
//                                            s = s.replace("}", "");
//                                            s = s.replace("{", "");
//                                            String[] entry = s.split("=");
//                                            counts.put(StringNormalizer.normaliseValue(entry[0], true), Integer.parseInt(entry[1]));
//                                            //System.out.println(StringNormalizer.normaliseValue(entry[0],true));
//                                        }
//                                        for (Object o : cor.getFirst().getValues().values()) {
//                                            //System.out.println(o);
//                                            if (o instanceof List) {
//                                                List<String> list = (List<String>) o;
//                                                for (String s : list) {
//                                                    if (counts.containsKey(s)) {
//                                                        sum += counts.get(s);
//                                                    }
//                                                    break;
//                                                }
//                                            } else {
//                                                String s = (String) o;
//                                                if (s.contains(",")) {
//                                                    s = s.split(",")[0];
//                                                    s = s.trim();
//                                                }
//                                                if (s.contains("/")) {
//                                                    s = s.split("/")[0];
//                                                    s = s.trim();
//                                                }
//                                                if (counts.containsKey(s)) {
//                                                    sum += counts.get(s);
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            checkIncorrect += sum + "\t";
                        } else {
//                            checkIncorrect += -1.0 + "\t";
//                            checkIncorrect += -1.0 + "\t";
                            checkIncorrect += "0\t";
                        }

                        //checkIncorrect += cor.getFirst().getValues().size() + "\t";
                        if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.date) {
                            TTest ttest = new TTest();
                            List<Date> tableValues = new ArrayList<>();
                            for (Object o : cor.getFirst().getValues().values()) {
                                if (o instanceof List) {
                                    List<Date> list = (List<Date>) o;
                                    tableValues.addAll(list);
                                } else {
                                    Date value = (Date) o;
                                    tableValues.add(value);
                                }
                            }
                            List<Date> dbpediaValues = new ArrayList<>();
                            for (Object o : cor.getSecond().getValues().values()) {
                                if (o instanceof List) {
                                    List<Date> list = (List<Date>) o;
                                    dbpediaValues.addAll(list);
                                } else {
                                    Date value = (Date) o;
                                    dbpediaValues.add(value);
                                }
                            }
                            Collections.sort(tableValues);
                            Date median;
                            int middle = tableValues.size() / 2;
                            median = tableValues.get(middle);

                            Collections.sort(dbpediaValues);
                            Date minDate = dbpediaValues.get(0);
                            Date maxDate = dbpediaValues.get(dbpediaValues.size() - 1);

                            if (minDate.before(median) && median.before(maxDate)) {
                                checkIncorrect += "1.0\t";
                            } else {
                                checkIncorrect += "0.0\t";
                            }

                        } else {
                            checkIncorrect += "-5\t";
                        }

                        boolean indirectMatch = false;
                        if (matchedCols != null && matchedCols.get(cor.getFirst()) != null) {
                            System.out.println("domi not null " + cor.getFirst() + " - " + matchedCols.get(cor.getFirst()));
                            for (MatchingPair<TableColumn> mp : matchedCols.get(cor.getFirst())) {
                                if (evalInstance.getUniqueIdentifier(mp.getMatchingPair().getSecond()).equals(evalInstance.getUniqueIdentifier(cor.getSecond()))) {

                                    checkIncorrect
                                            += +mp.getLabelScore() + "\t" + mp.getValueScore() + "\t" + mp.getFinalScore() + "\t"
                                            + mp.getOccurenceFirstLabel() + "\t" + mp.getOccurenceFirstValue() + "\t"
                                            + mp.getOccurencePairLabel() + "\t" + mp.getOccurencePairValue() + "\t"
                                            + mp.getOtherPairsLabel() + "\t" + mp.getOtherPairsValue() + "\t" + mp.getOverallNumbTables();
                                    indirectMatch = true;
                                }
                            }
                        }
                        if (!indirectMatch) {
                            checkIncorrect
                                    += +0 + "\t" + 0 + "\t" + 0 + "\t"
                                    + 0 + "\t" + 0 + "\t"
                                    + 0 + "\t" + 0 + "\t"
                                    + 0 + "\t" + 0 + "\t";//+ mapped.size();
                        }

                        if (combinedBothProp != null && combinedBothProp.get(cor.getFirst(), cor.getSecond()) != null) {
                            checkIncorrect += combinedBothProp.get(cor.getFirst(), cor.getSecond()) + "\t";
                            if (propertySim.get(cor.getFirst(), cor.getSecond()) == null) {
                                checkIncorrect += "1\t";
                            }
                        } else {
                            checkIncorrect += "0\t";
                            checkIncorrect += "0\t";
                        }

                        Object correctKey = cor.getCorrectValue() + "\t";
                        if (correctKey != null) {
                            checkIncorrect += correctKey + "\t";
                            checkIncorrect += "correctExists\t";
                        } else {
                            checkIncorrect += "correctNotExists\t";
                        }

                        List<TableColumn> correctCols = new ArrayList<>();
                        for (TableColumn cols : getData().getDbpediaColSet()) {
                            if (cols.getURI().equals(correctKey)) {
                                correctCols.add(cols);
                            }
                        }

//                        for(TableColumn col : correctCols) {
//                            if(freqAll.getMatches(cor.getFirst()).contains(col)) {
//                                checkIncorrect += "in freq all: " +freqAll.get(cor.getFirst(),col)+ "\t";
//                            }
//                        }
                        for (Correspondence<Table> corTable : result.getClassMappings()) {
                            //checkIncorrect += corTable.getSecond() + "---" + corTable.isCorrect() + "\t";
                            checkIncorrect += corTable.isCorrect() + "\t";
                        }
                        int countInst = 0;
                        for (Correspondence<TableRow> corProp : result.getInstanceMappings()) {
                            //checkIncorrect += corProp.getFirst() + "---" + corProp.getSecond() + "---" + corProp.isCorrect() + "\t";
                            countInst++;
                        }
                        checkIncorrect += (double) countInst / (double) result.getInstanceMappings().size();

                        System.out.println("propProp check property: " + checkIncorrect);
                    } else {
                        String correct = "";
                        correct += cor.getFirst().getTable().getHeader() + "\t";
                        correct += "1\t";
                        correct += cor.getFirst().getDataType() + "\t";
                        correct += cor.getFirst().getHeader().toString() + "\t";
                        correct += cor.getFirst().getValues().get(cor.getFirst().getTable().getColumns().indexOf(cor.getFirst())) + "\t";
                        correct += evalInstance.getUniqueIdentifier(cor.getSecond()) + "\t";
                        correct += cor.getSecond().getValues().get(cor.getSecond().getTable().getColumns().indexOf(cor.getSecond())) + "\t";

                        Double propValueToWrite = null;
                        if (props.get(cor.getFirst(), cor.getSecond()) == null) {
                            for (TableColumn t : props.getMatches(cor.getFirst())) {
                                if (t.getURI().equals(cor.getSecond().getURI())) {
                                    propValueToWrite = props.get(cor.getFirst(), t);
                                }
                            }
                        } else {
                            propValueToWrite = props.get(cor.getFirst(), cor.getSecond());
                        }
                        correct += propValueToWrite + "\t";
                        correct += stats.get(props).getMean() + "\t";

                        Double labelValueToWrite = null;
                        if (labelSimilarity.get(cor.getFirst(), cor.getSecond()) == null) {
                            for (TableColumn t : labelSimilarity.getMatches(cor.getFirst())) {
                                if (t.getURI().equals(cor.getSecond().getURI())) {
                                    labelValueToWrite = labelSimilarity.get(cor.getFirst(), t);
                                }
                            }
                        } else {
                            labelValueToWrite = labelSimilarity.get(cor.getFirst(), cor.getSecond());
                        }
                        correct += labelValueToWrite + "\t";

                        // System.out.println("label score: " + cor.getFirst() + " . " + cor.getSecond().getURI() + " of class " + cor.getSecond().getTable().getHeader());
                        for (TableColumn o : labelSimilarity.getFirstDimension()) {
                            for (TableColumn t : labelSimilarity.getMatches(o)) {
                                //System.out.println("score in label: " + o.getHeader() + " with " + t.getURI() + " from " + t.getTable().getHeader() + " score: " + props.get(o, t));
                            }
                        }

                        correct += stats.get(labelSimilarity).getMean() + "\t";
                        correct += kurt.get(cor.getFirst(), cor.getSecond()) + "\t";
                        correct += stats.get(kurt).getMean() + "\t";
                        correct += freqDT.get(cor.getFirst(), cor.getSecond()) + "\t";
                        correct += classStatsMatrix.get(cor.getFirst(), cor.getSecond()) + "\t";
                        correct += propStatsMatrix.get(cor.getFirst(), cor.getSecond()) + "\t";
                        correct += countDT.get(cor.getFirst(), cor.getSecond()) + "\t";
                        correct += propertySim.get(cor.getFirst(), cor.getSecond()) + "\t";
                        correct += cor.getFirst().getColumnStatistic().getKurtosis() + "\t";

//                        correct += cor.getFirst().getColumnStatistic().getMinimalValue() + "\t";
//                        correct += cor.getFirst().getColumnStatistic().getMaximalValue() + "\t";
//                        correct += cor.getFirst().getColumnStatistic().getAverage() + "\t";
//                        correct += cor.getFirst().getColumnStatistic().getStandardDeviation() + "\t";
//                        correct += cor.getFirst().getColumnStatistic().getVariance() + "\t";
//
                        if (cor.getSecond().getEquivBefore() != null) {
//                            System.out.println("equi used: " + cor.getSecond().getEquivBefore().getURI() + " class: " + cor.getSecond().getTable() + " table ID " + cor.getFirst().getTable().getHeader());
//                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getMinimalValue() + "\t";
//                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getMaximalValue() + "\t";
//                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getAverage() + "\t";
//                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getStandardDeviation() + "\t";
//                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getVariance() + "\t";
//                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getLowerPercentile() + "\t";
//                            correct += cor.getSecond().getEquivBefore().getColumnStatistic().getUpperPercentile() + "\t";
                            if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
                                if ((double) cor.getSecond().getEquivBefore().getColumnStatistic().getMinimalValue() <= cor.getFirst().getColumnStatistic().getAverage()
                                        && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getEquivBefore().getColumnStatistic().getMaximalValue()) {
                                    correct += "1\t";
                                } else {
                                    correct += "-1.0\t";
                                }
                                if ((double) cor.getSecond().getEquivBefore().getColumnStatistic().getLowerPercentile() <= cor.getFirst().getColumnStatistic().getAverage()
                                        && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getEquivBefore().getColumnStatistic().getUpperPercentile()) {
                                    correct += "1\t";
                                } else {
                                    correct += "-1.0\t";
                                }
                            } else {
                                correct += "0\t";
                                correct += "0\t";
                            }
                        } else {
//                            System.out.println("equi not used: " + cor.getSecond().getURI() + " class: " + cor.getSecond().getTable() + " table ID " + cor.getFirst().getTable().getHeader());
//                            Correspondence<Table> decidedClass = result.getClassMappings().iterator().next();
//                            TableColumn propertyUsed = decidedClass.getSecond().getColumn(cor.getSecond().getHeader().toString());
//                            correct += cor.getSecond().getColumnStatistic().getMinimalValue() + "\t";
//                            correct += cor.getSecond().getColumnStatistic().getMaximalValue() + "\t";
//                            correct += cor.getSecond().getColumnStatistic().getAverage() + "\t";
//                            correct += cor.getSecond().getColumnStatistic().getStandardDeviation() + "\t";
//                            correct += cor.getSecond().getColumnStatistic().getVariance() + "\t";
//                            correct += cor.getSecond().getColumnStatistic().getLowerPercentile() + "\t";
//                            correct += cor.getSecond().getColumnStatistic().getUpperPercentile() + "\t";
                            if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
                                if ((double) cor.getSecond().getColumnStatistic().getMinimalValue() <= cor.getFirst().getColumnStatistic().getAverage()
                                        && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getColumnStatistic().getMaximalValue()) {
                                    correct += "1\t";
                                } else {
                                    correct += "-1.0\t";
                                }
                                if ((double) cor.getSecond().getColumnStatistic().getLowerPercentile() <= cor.getFirst().getColumnStatistic().getAverage()
                                        && cor.getFirst().getColumnStatistic().getAverage() <= (double) cor.getSecond().getColumnStatistic().getUpperPercentile()) {
                                    correct += "1\t";
                                } else {
                                    correct += "-1.0\t";
                                }
                            } else {
                                correct += "0\t";
                                correct += "0\t";
                            }
                        }

                        if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.numeric) {
                            TTest ttest = new TTest();
                            List<Double> tableValues = new ArrayList<>();
                            for (Object o : cor.getFirst().getValues().values()) {
                                if (o instanceof List) {
                                    List<Double> list = (List<Double>) o;
                                    tableValues.addAll(list);
                                } else {
                                    Double value = (double) o;
                                    tableValues.add(value);
                                }
                            }
                            double[] valuesTable = Doubles.toArray(tableValues);

                            List<Double> propValues = new ArrayList<>();
                            for (Object o : cor.getSecond().getValues().values()) {
                                if (o instanceof List) {
                                    List<Double> list = (List<Double>) o;
                                    propValues.addAll(list);
                                } else {
                                    Double value = (double) o;
                                    propValues.add(value);
                                }
                            }
                            double[] valuesProp = Doubles.toArray(propValues);
                            try {
                                correct += ttest.t(valuesTable, valuesProp) + "\t";
                            } catch (Exception e) {
                                correct += "-5\t";
                            }
                            double countIntTable = 0;
                            double countIntDBpedia = 0;
                            for (Object o : cor.getSecond().getValues().values()) {
                                try {
                                    if (!(o instanceof List) && (o instanceof Double)) {
                                        if ((double) o % 1 == 0) {
                                            countIntDBpedia++;
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                            for (Object o : cor.getFirst().getValues().values()) {
                                try {
                                    if (!(o instanceof List) && (o instanceof Double)) {
                                        if ((double) o % 1 == 0) {
                                            countIntTable++;
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                            countIntDBpedia = countIntDBpedia / (double) cor.getSecond().getValues().size();
                            countIntTable = countIntTable / (double) cor.getFirst().getValues().size();
                            double diff = countIntDBpedia - countIntTable;
                            correct += diff + "\t";
//                            if(countIntDBpedia > 0.9 && countIntTable < 0.9) {
//                                correct += "0\t";
//                            }
//                            else {
//                                correct += "1\t";
//                            }
                        } else {
                            correct += "-5\t";
//                            correct += "-8\t";
                            correct += "0\t";
                        }

                        double distValueDBpedia = (double) cor.getSecond().getColumnStatistic().getDistinctValues() / (double) cor.getSecond().getValues().size();
                        double distValueDBpediaList = (double) cor.getSecond().getColumnStatistic().getDistinctValuesWithoutLists() / (double) cor.getSecond().getValues().size();
                        //test!
                        distValueDBpediaList = 0;
                        double distValueWT = (double) cor.getFirst().getColumnStatistic().getDistinctValues() / (double) cor.getFirst().getValues().size();
                        double diff1, diff2;
                        diff1 = distValueDBpedia - distValueWT;
                        diff2 = distValueDBpediaList - distValueWT;

                        correct += diff1 + "\t";
                        correct += diff2 + "\t";

                        if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.string) {
                            Collection<Object> values1 = cor.getSecond().getValues().values();
                            Collection<Object> allValues = new HashSet<>();

                            if (cor.getSecond().getEquivBefore() != null) {
                                //System.out.println("equiv props: " + cor.getSecond().getURI() + " -- " + cor.getSecond().getEquivBefore().getURI());
                                Collection<Object> values2 = cor.getSecond().getEquivBefore().getValues().values();
                                allValues.addAll(values2);
                            } else {
                                allValues.addAll(values1);
                            }

                            Map<String, Integer> countsValuesDBpedia = new TreeMap<>();
                            for (Object o : allValues) {
                                if (o instanceof List) {
                                    List<String> list = (List<String>) o;
                                    try {
                                        for (String value : list) {
                                            if (value.contains(" ") || value.contains("_")) {
                                                String[] splittedValues;
                                                if (value.contains(" ")) {
                                                    splittedValues = value.split("\\s");
                                                } else {
                                                    splittedValues = value.split("_");
                                                }
                                                for (String s : splittedValues) {
                                                    s = StringNormalizer.normaliseValue(s, true);
                                                    s = s.replace("(", "");
                                                    if (!StopWordRemover.isStopWord(s)) {
                                                        if (countsValuesDBpedia.containsKey(s)) {
                                                            Integer current = countsValuesDBpedia.get(s);
                                                            current++;
                                                            countsValuesDBpedia.put(s, current);
                                                        } else {
                                                            countsValuesDBpedia.put(s, 1);
                                                        }
                                                    }
                                                }
                                            } else {
                                                value = StringNormalizer.normaliseValue(value, true);
                                                if (countsValuesDBpedia.containsKey(value)) {
                                                    Integer current = countsValuesDBpedia.get(value);
                                                    current++;
                                                    countsValuesDBpedia.put(value, current);
                                                } else {
                                                    countsValuesDBpedia.put(value, 1);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                    }
                                } else if (o instanceof String) {
                                    String value = (String) o;
                                    value = StringNormalizer.normaliseValue(value, true);
                                    if (value.contains(" ") || value.contains("_")) {
                                        String[] splittedValues;
                                        if (value.contains(" ")) {
                                            splittedValues = value.split("\\s");
                                        } else {
                                            splittedValues = value.split("_");
                                        }
                                        for (String s : splittedValues) {
                                            s = s.replace("(", "");
                                            if (!StopWordRemover.isStopWord(s)) {
                                                if (countsValuesDBpedia.containsKey(s)) {
                                                    Integer current = countsValuesDBpedia.get(s);
                                                    current++;
                                                    countsValuesDBpedia.put(s, current);
                                                } else {
                                                    countsValuesDBpedia.put(s, 1);
                                                }
                                            }
                                        }
                                    } else {
                                        if (countsValuesDBpedia.containsKey(value)) {
                                            Integer current = countsValuesDBpedia.get(value);
                                            current++;
                                            countsValuesDBpedia.put(value, current);
                                        } else {
                                            countsValuesDBpedia.put(value, 1);
                                        }
                                    }
                                }
                            }
                            //System.out.println("count values: " + countsValues);

                            double sum = 0.0;

                            Map<String, Integer> countsValuesWT = new TreeMap<>();

                            //System.out.println("values table: " + cor.getFirst().getValues().values());
                            for (Object o : cor.getFirst().getValues().values()) {
                                //System.out.println(o);
                                if (o instanceof List) {
                                    List<String> list = (List<String>) o;
                                    for (String s : list) {
                                        if (s.contains(",")) {
                                            s = s.split(",")[0];
                                            s = s.trim();
                                        }
                                        if (s.contains("/")) {
                                            s = s.split("/")[0];
                                            s = s.trim();
                                        }
                                        if (s.contains("_")) {
                                            s = s.split("_")[0];
                                            s = s.trim();
                                        }
                                        s = s.replace("(", "");
                                        if (s.contains(" ")) {
                                            String[] splittedValues = s.split("\\s");
                                            for (String t : splittedValues) {
                                                if (!StopWordRemover.isStopWord(t)) {
                                                    if (countsValuesDBpedia.containsKey(t)) {
                                                        //sum += countsValuesDBpedia.get(t);
                                                        if (countsValuesWT.containsKey(t)) {
                                                            Integer current = countsValuesWT.get(t);
                                                            current++;
                                                            countsValuesWT.put(t, current);
                                                        } else {
                                                            countsValuesWT.put(t, 1);
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            if (countsValuesDBpedia.containsKey(s)) {
                                                //sum += countsValuesDBpedia.get(s);
                                                if (countsValuesWT.containsKey(s)) {
                                                    Integer current = countsValuesWT.get(s);
                                                    current++;
                                                    countsValuesWT.put(s, current);
                                                } else {
                                                    countsValuesWT.put(s, 1);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    String s = (String) o;
                                    if (s.contains(",")) {
                                        s = s.split(",")[0];
                                        s = s.trim();
                                    }
                                    if (s.contains("/")) {
                                        s = s.split("/")[0];
                                        s = s.trim();
                                    }
                                    s = s.trim();
                                    if (s.contains(" ")) {
                                        String[] splittedValues = s.split("\\s");
                                        for (String t : splittedValues) {
                                            if (!StopWordRemover.isStopWord(t)) {
                                                if (countsValuesDBpedia.containsKey(t)) {
                                                    //sum += countsValuesDBpedia.get(t);
                                                    if (countsValuesWT.containsKey(t)) {
                                                        Integer current = countsValuesWT.get(t);
                                                        current++;
                                                        countsValuesWT.put(t, current);
                                                    } else {
                                                        countsValuesWT.put(t, 1);
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        if (countsValuesDBpedia.containsKey(s)) {
                                            //sum += countsValuesDBpedia.get(s);
                                            if (countsValuesWT.containsKey(s)) {
                                                Integer current = countsValuesWT.get(s);
                                                current++;
                                                countsValuesWT.put(s, current);
                                            } else {
                                                countsValuesWT.put(s, 1);
                                            }
                                        }
                                    }
                                }
                            }

                            //System.out.println("calculate cosine: " + cor.getSecond().getURI() + " of class " + cor.getSecond().getTable().getHeader() + " with " + cor.getFirst().getHeader());
                            correct += calculateCosineSimilarity(countsValuesDBpedia, countsValuesWT) + "\t";
//
//                            sum = 0.0;
//                            try {
//                                //SoftballLeague_currentSeason    {2015 Men's Softball World Championship=1, 2013 NCAA Division III Softball Championship=1, 2013 NCAA Division II Softball Championship=1}
//                                BufferedReader read = new BufferedReader(new FileReader(new File("/backup-local-home/dritze/newGS/recognizer/DBpedia/dbpediaStatsString.csv")));
//                                String wholeLine = read.readLine();
//                                String className = "";
//                                for (Correspondence<Table> corTable : result.getClassMappings()) {
//                                    className = corTable.getSecond().getHeader();
//                                    className = className.replace(".tar.gz", "");
//                                    className = className.replace(".csv", "");
//                                    className = className.replace(".gz", "");
//                                }
//                                Set<String> urisToCheck = new HashSet<>();
//                                Canoniser c = new Canoniser();
//                                Collection<List<String>> equiProps = c.loadEquivalentResourcesExternal(getEvaluationParameters().getEquivalentPropertiesLocation());
//                                for (List l : equiProps) {
//                                    if (l.contains(cor.getSecond().getURI())) {
//                                        //System.out.println("uris to check! " + l);
//                                        urisToCheck.addAll(l);
//                                    }
//                                }
//
//                                //System.out.println("class prop name second: " + className + "_" + cor.getSecond().getURI().replace("http://dbpedia.org/ontology/", ""));
//                                //System.out.println("class prop name equi: " + className + "_" + evalInstance.getUniqueIdentifier(cor.getSecond()).toString().replace("http://dbpedia.org/ontology/", ""));
//                                Set<String> lineWithProp = new HashSet<>();
//                                while (wholeLine != null) {
//                                    if (!urisToCheck.isEmpty()) {
//                                        //System.out.println("not empty");
//                                        for (String s : urisToCheck) {
//                                            System.out.println("check " + s);
//                                            if (wholeLine.startsWith(className + "_" + s.replace("http://dbpedia.org/ontology/", "") + "\t")
//                                                    || wholeLine.startsWith(className + "_" + s.replace("http://dbpedia.org/ontology/", "") + "_label\t")) {
//                                                lineWithProp.add(wholeLine);
//                                                //    System.out.println("found");
//                                            }
//                                        }
//                                    } else {
//                                        if (wholeLine.startsWith(className + "_" + cor.getSecond().getURI().replace("http://dbpedia.org/ontology/", "") + "\t")
//                                                || wholeLine.startsWith(className + "_" + cor.getSecond().getURI().replace("http://dbpedia.org/ontology/", "") + "_label\t")) {
//                                            lineWithProp.add(wholeLine);
//                                            // System.out.println("found single: " + cor.getSecond().getURI());
//                                            break;
//                                        }
//                                        //continue;
//                                    }
//
////                                    if (wholeLine.startsWith(className + "_" + evalInstance.getUniqueIdentifier(cor.getSecond()).toString().replace("http://dbpedia.org/ontology/", ""))) {
////                                        secondLineWithProp = wholeLine;
////                                        //break;
////                                    }
//                                    wholeLine = read.readLine();
//                                }
//                                //System.out.println("line with prop " +lineWithProp);
//                                if (!lineWithProp.isEmpty()) {
//                                    for (String check : lineWithProp) {
//                                        String[] countArray = new String[0];
//                                        String[] countArray2 = new String[0];
//                                        String[] line = check.split("\t");
//                                        countArray = line[1].split(",");
//                                        Map<String, Integer> counts = new HashMap<>();
//                                        for (String s : countArray) {
//                                            s = s.replace("}", "");
//                                            s = s.replace("{", "");
//                                            String[] entry = s.split("=");
//                                            counts.put(StringNormalizer.normaliseValue(entry[0], true), Integer.parseInt(entry[1]));
//                                            //System.out.println("entry: " + StringNormalizer.normaliseValue(entry[0], true));
//                                        }
//                                        for (String s : countArray2) {
//                                            s = s.replace("}", "");
//                                            s = s.replace("{", "");
//                                            String[] entry = s.split("=");
//                                            counts.put(StringNormalizer.normaliseValue(entry[0], true), Integer.parseInt(entry[1]));
//                                            //System.out.println(StringNormalizer.normaliseValue(entry[0],true));
//                                        }
//                                        for (Object o : cor.getFirst().getValues().values()) {
//                                            //System.out.println(o);
//                                            if (o instanceof List) {
//                                                List<String> list = (List<String>) o;
//                                                for (String s : list) {
//                                                    if (counts.containsKey(s)) {
//                                                        sum += counts.get(s);
//                                                    }
//                                                    break;
//                                                }
//                                            } else {
//                                                String s = (String) o;
//                                                if (s.contains(",")) {
//                                                    s = s.split(",")[0];
//                                                    s = s.trim();
//                                                }
//                                                if (s.contains("/")) {
//                                                    s = s.split("/")[0];
//                                                    s = s.trim();
//                                                }
//                                                if (counts.containsKey(s)) {
//                                                    sum += counts.get(s);
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            correct += sum + "\t";
                        } else {
//                            correct += -1.0 + "\t";
//                            correct += -1.0 + "\t";
                            correct += "0\t";
                        }
//                        correct += cor.getFirst().getValues().size() + "\t";

                        if (cor.getFirst().getDataType() == TableColumn.ColumnDataType.date) {
                            TTest ttest = new TTest();
                            List<Date> tableValues = new ArrayList<>();
                            for (Object o : cor.getFirst().getValues().values()) {
                                if (o instanceof List) {
                                    List<Date> list = (List<Date>) o;
                                    tableValues.addAll(list);
                                } else {
                                    Date value = (Date) o;
                                    tableValues.add(value);
                                }
                            }
                            List<Date> dbpediaValues = new ArrayList<>();
                            for (Object o : cor.getSecond().getValues().values()) {
                                if (o instanceof List) {
                                    List<Date> list = (List<Date>) o;
                                    dbpediaValues.addAll(list);
                                } else {
                                    Date value = (Date) o;
                                    dbpediaValues.add(value);
                                }
                            }
                            Collections.sort(tableValues);
                            Date median;
                            int middle = tableValues.size() / 2;
                            median = tableValues.get(middle);

                            Collections.sort(dbpediaValues);
                            Date minDate = dbpediaValues.get(0);
                            Date maxDate = dbpediaValues.get(dbpediaValues.size() - 1);

                            if (minDate.before(median) && median.before(maxDate)) {
                                correct += "1.0\t";
                            } else {
                                correct += "0.0\t";
                            }

                        } else {
                            correct += "-5\t";
                        }

                        boolean indirectMatch = false;
                        if (matchedCols != null && matchedCols.get(cor.getFirst()) != null) {
                            System.out.println("domi not null " + cor.getFirst() + " - " + matchedCols.get(cor.getFirst()));
                            for (MatchingPair<TableColumn> mp : matchedCols.get(cor.getFirst())) {
                                if (evalInstance.getUniqueIdentifier(mp.getMatchingPair().getSecond()).equals(evalInstance.getUniqueIdentifier(cor.getSecond()))) {

                                    correct
                                            += +mp.getLabelScore() + "\t" + mp.getValueScore() + "\t" + mp.getFinalScore() + "\t"
                                            + mp.getOccurenceFirstLabel() + "\t" + mp.getOccurenceFirstValue() + "\t"
                                            + mp.getOccurencePairLabel() + "\t" + mp.getOccurencePairValue() + "\t"
                                            + mp.getOtherPairsLabel() + "\t" + mp.getOtherPairsValue() + "\t" + mp.getOverallNumbTables();
                                    indirectMatch = true;
                                }
                            }
                        }
                        if (!indirectMatch) {
                            correct
                                    += +0 + "\t" + 0 + "\t" + 0 + "\t"
                                    + 0 + "\t" + 0 + "\t"
                                    + 0 + "\t" + 0 + "\t"
                                    + 0 + "\t" + 0 + "\t";//+ mapped.size();
                        }

                        if (combinedBothProp != null && combinedBothProp.get(cor.getFirst(), cor.getSecond()) != null) {
                            correct += combinedBothProp.get(cor.getFirst(), cor.getSecond()) + "\t";
                            if (propertySim.get(cor.getFirst(), cor.getSecond()) == null) {
                                correct += "1\t";
                            }
                        } else {
                            correct += "0\t";
                            correct += "0\t";
                        }

                        Object correctKey = cor.getCorrectValue() + "\t";
                        correct += correctKey;

                        System.out.println("propProp correct property " + correct);
                    }

                }

                Map<String, String> datatypes = new HashMap<>();
                for (Map.Entry<Object, Object> e : getGoldStandard().getPropertyGoldStandard().entrySet()) {
//                    if (getData().getWebtable().getKeyIndex() == (int) e.getKey()) {
//                        continue;
//                    }
                    for (TableColumn tc : data.getDbpediaColSet()) {
                        if (e.getValue().toString().equals(tc.getURI())) {
                            if (!datatypes.containsKey(tc.getURI() + "-" + e.getKey() + "-" + data.getWebtable().getHeader())) {
                                datatypes.put(tc.getURI() + "-" + e.getKey() + "-" + data.getWebtable().getHeader(), tc.getDataType().toString());
                            }
                            break;
                        }
                    }

                    for (String datatype : datatypes.keySet()) {
                        System.out.println("datatype all: " + datatype + "\t" + datatypes.get(datatype));
                    }

                    String checkIncorrect = "";
                    SimilarityMatrix<TableRow> sim = similarities.getInitialCandidateSimilarity();
                    for (TableColumn tr : getData().getWebtable().getColumns()) {
                        if (!tr.getTable().getHeader().equals(getData().getWebtable().getHeader())) {
                            continue;
                        }
                        boolean foundInValueCandidates = false, detected = false, foundFreq = false, allowed = false;
                        if (tr.getTable().getColumns().indexOf(tr) == Integer.parseInt(e.getKey().toString())) {
                            checkIncorrect += tr.getTable().getHeader() + "\t";
                            checkIncorrect += tr.getHeader() + "\t";
                            checkIncorrect += tr.getValues() + "\t";
                            for (Correspondence<Table> corTable : result.getClassMappings()) {
                                //checkIncorrect += corTable.getSecond() + "---" + corTable.isCorrect() + "\t";
                                checkIncorrect += corTable.isCorrect() + "\t";
                                checkIncorrect += corTable.getSecond().getHeader() + "\t";
                            }
                            if (result.getClassMappings().isEmpty()) {
                                checkIncorrect += "tableFiltered\t";
                                checkIncorrect += "tableFiltered\t";
                            }
//                            int countInst = 0;
//                            for (Correspondence<TableRow> corInst : result.getInstanceMappings()) {
//                                //checkIncorrect += corProp.getFirst() + "---" + corProp.getSecond() + "---" + corProp.isCorrect() + "\t";
//                                countInst++;
//                            }
                            if (!result.getPropertyMappings().isEmpty()) {
                                for (Correspondence<TableColumn> tc : result.getPropertyMappings()) {
                                    if (tc.getSecond().getURI().equals(e.getValue().toString())) {
                                        continue;
                                    }
                                    checkIncorrect += tc.getSecond().getURI() + "---";
                                }
                                checkIncorrect += "\t";
                            } else {
                                checkIncorrect += "noOther\t";
                            }
                            checkIncorrect += (double) result.getInstanceMappings().size() / (double) getData().getWebtable().getKey().getNumRows() + "\t";
                            checkIncorrect += e.getValue() + "\t";
                            if (allowedCols.contains(e.getValue().toString())) {
                                checkIncorrect += "allowed\t";
                            } else {
                                checkIncorrect += "notAllowed\t";
                            }
                            for (TableColumn valueCandidates : propertySim.getMatches(tr)) {
                                if (e.getValue().equals(evalInstance.getUniqueIdentifier(valueCandidates))) {
                                    checkIncorrect += propertySim.get(tr, valueCandidates) + "\t";
                                    foundInValueCandidates = true;
                                }
                            }
                            for (TableColumn valueCandidates : labelSimilarity.getMatches(tr)) {
                                if (e.getValue().equals(evalInstance.getUniqueIdentifier(valueCandidates))) {
                                    checkIncorrect += labelSimilarity.get(tr, valueCandidates) + "\t";
                                    foundInValueCandidates = true;
                                }
                            }
//                            for (TableColumn valueCandidates : freqAll.getMatches(tr)) {
//                                if (e.getValue().equals(evalInstance.getUniqueIdentifier(valueCandidates))) {
//                                    checkIncorrect += freqAll.get(tr, valueCandidates) + "\t";
//                                    foundFreq = true;
//                                }
//                            }

                            for (Correspondence<TableColumn> cor : result.getPropertyMappings()) {
                                if (cor.getFirst().equals(tr) && evalInstance.getUniqueIdentifier(cor.getSecond()).equals(e.getValue())) {
                                    detected = true;
                                } else {
                                    if (cor.getFirst().equals(tr) && !evalInstance.getUniqueIdentifier(cor.getSecond()).equals(e.getValue())) {
                                        checkIncorrect += cor.getSecond().getURI() + "\t";
                                        checkIncorrect += cor.getSimilarity() + "\t";
                                    }
                                }
                            }

                            if (foundInValueCandidates && !detected) {
                                checkIncorrect += "wrongChoice\t";
                            } else if (!detected && !foundInValueCandidates && !foundFreq) {
                                checkIncorrect += "noCandidateAtAll\t";
                            } else if (!detected && !foundInValueCandidates && foundFreq) {
                                checkIncorrect += "onlyFreq\t";
                            }
                            checkIncorrect += kurti + "\t";
                            if (!detected) {
                                System.out.println("propProp property FN: " + checkIncorrect);
                            }
                        }
                    }
                }

                //TODO: try our later!
                if (filtered) {
//                    MatchingResult mr = new MatchingResult();
//                    SimilarityMatrix<TableRow> matched = candidateSelection.mapInstances(getSimilarities().getBeforeFiltering(), false, mr, webtable);

                    //itc.setNumTabes(1000);
                    //itc.computeCandidates(getData().getWebtableRowSet(), result);
                    //sortedClasses = sortByValue(itc.getClasses());
//                    String goldURI = "";
//                    if (goldStandard.getClassGoldStandard().get(data.getWebtable().getHeader().replace(".json", "")) != null) {
//                        goldURI = goldStandard.getClassGoldStandard().get(data.getWebtable().getHeader().replace(".json", "")).get(0).toString();
//                    }
//                    System.out.println("filtered tabs classes: " + getData().getWebtable() + " - " + " - " + goldURI + " - " + sortedClasses);
//
//                    List<String> allowedClasses = new ArrayList<>();
//                    if (!chosenClass.isEmpty()) {
//                        allowedClasses.add("http://dbpedia.org/ontology/" + chosenClass.get(0).getHeader().replace(".csv.gz", "").toLowerCase());
//                        itc.setDbpediaClass(allowedClasses);
//                        itc.computeCandidates(getData().getWebtableRowSet(), config, result);
//                        Map<Table, SimilarityMatrix<TableRow>> mapped = itc.computeCandidates(getData().getWebtableRowSet(), config, mr);
//                        System.out.println("filtered tabs #mapped: " + getData().getWebtable().getHeader() + " - " + mapped.size() + allowedClasses.get(0));
//                    }
                }

                for (Correspondence<Table> cor : result.getClassMappings()) {

                    double countInst = 0.0;
                    for (Correspondence<TableRow> row1 : result.getInstanceMappings()) {
                        for (TableRow rowClasses : data.getUriMap().get(row1.getSecond().getURI().toString())) {
                            if (rowClasses.getTable().getHeader().equals(cor.getSecond().getHeader())) {
                                countInst++;
                                break;
                            }
                        }
                    }

                    if (!cor.isCorrect()) {
                        String checkIncorrect = "";
                        checkIncorrect += cor.getFirst().getHeader() + "\t";
                        checkIncorrect += cor.getFirst().getKey() + "\t";
                        checkIncorrect += cor.getSecond().getHeader() + "\t";
                        checkIncorrect += cor.getSecond().getKey() + "\t";
                        checkIncorrect += cor.getSimilarity() + "\t";
                        checkIncorrect += originalClassSim.get(cor.getFirst(), cor.getSecond()) + "\t";
                        if (indirectClassMatrix != null) {
                            checkIncorrect += indirectClassMatrix.get(cor.getFirst(), cor.getSecond()) + "\t";
                        } else {
                            checkIncorrect += "-1.0\t";
                        }
                        checkIncorrect += countInst / (double) result.getInstanceMappings().size() + "\t";
                        checkIncorrect += (double) result.getInstanceMappings().size() + "\t";
                        checkIncorrect += (double) cor.getFirst().getKey().getValues().size() + "\t";
                        checkIncorrect += "0\t";

                        checkIncorrect += classesPerInstances + "\t";
                        checkIncorrect += numberOfMapped + "\t";
                        checkIncorrect += allMapped + "\t";
                        checkIncorrect += mappedInstances + "\t";
                        checkIncorrect += sameInstanceMappedRatio + "\t";
                        checkIncorrect += getSimilarities().getClassSimilarity().get(cor.getFirst(), cor.getSecond()) + "\t";
//
//                        Object correctKey = cor.getCorrectValue() + "\t";
//                        if (correctKey != null) {
//                            checkIncorrect += correctKey + "\t";
//                            checkIncorrect += "correctExists\t";
//                        } else {
//                            checkIncorrect += "correctNotExists\t";
//                        }

                        checkIncorrect += cor.getCorrectValue();

                        System.out.println("check class: " + checkIncorrect);
                    } else {
                        String checkIncorrect = "";
                        checkIncorrect += cor.getFirst().getHeader() + "\t";
                        checkIncorrect += cor.getFirst().getKey() + "\t";
                        checkIncorrect += cor.getSecond().getHeader() + "\t";
                        checkIncorrect += cor.getSecond().getKey() + "\t";
                        checkIncorrect += cor.getSimilarity() + "\t";
                        checkIncorrect += originalClassSim.get(cor.getFirst(), cor.getSecond()) + "\t";
                        if (indirectClassMatrix != null) {
                            checkIncorrect += indirectClassMatrix.get(cor.getFirst(), cor.getSecond()) + "\t";
                        } else {
                            checkIncorrect += "-1.0\t";
                        }
                        checkIncorrect += countInst / result.getInstanceMappings().size() + "\t";
                        checkIncorrect += (double) result.getInstanceMappings().size() + "\t";
                        checkIncorrect += (double) cor.getFirst().getKey().getValues().size() + "\t";
                        checkIncorrect += "1\t";

                        checkIncorrect += classesPerInstances + "\t";
                        checkIncorrect += numberOfMapped + "\t";
                        checkIncorrect += allMapped + "\t";
                        checkIncorrect += mappedInstances + "\t";
                        checkIncorrect += sameInstanceMappedRatio + "\t";
                        checkIncorrect += getSimilarities().getClassSimilarity().get(cor.getFirst(), cor.getSecond()) + "\t";

                        checkIncorrect += cor.getCorrectValue();

                        System.out.println("check class correct: " + checkIncorrect);
                    }

                }

                for (Map.Entry<Object, List<Object>> e : getGoldStandard().getClassGoldStandard().entrySet()) {
                    if (!e.getKey().equals(getData().getWebtable().getHeader().split("\\.")[0])) {
                        continue;
                    }

                    String checkIncorrect = "";
                    checkIncorrect += getData().getWebtable().getHeader() + "\t";
                    checkIncorrect += getData().getWebtable().getKey() + "\t";
                    checkIncorrect += e.getValue().get(0) + "\t";
                    //                checkIncorrect += classesPerInstances + "\t";
                    //                checkIncorrect += numberOfMapped + "\t";
                    checkIncorrect += allMapped + "\t";
                    checkIncorrect += mappedInstances + "\t";
                    //                checkIncorrect += sameInstanceMappedRatio + "\t";

                    boolean detected = false;
                    //TODO: check all classes in the GS not only the first
                    for (Correspondence<Table> cor : result.getClassMappings()) {
//                        if (cor.getFirst().getHeader().split("\\.")[0].equals(getData().getWebtable().getHeader().split("\\.")[0]) && 
//                                e.getValue().contains(cor.getSecond().getHeader().split("\\.")[0].toLowerCase())) {
                        if (cor.isCorrect()) {
                            checkIncorrect += "1\t";
                        } else {
                            if (!cor.getSecond().getHeader().split("\\.")[0].toLowerCase().equals(e.getValue().get(0))) {
                                checkIncorrect += "0\t";
                                checkIncorrect += cor.getSecond().getHeader().split("\\.")[0].toLowerCase() + "\t";
                                checkIncorrect += cor.getSimilarity() + "\t";
                            }
                        }
                    }
                    if (!detected) {
                        System.out.println("check class FN: " + checkIncorrect);
                    }

                }

            }
        }

        setTableRuntime(System.currentTimeMillis() - start);
//        writeRunResult(config, result);
        Timer tCleanup = Timer.getNamed("Clear cache", timer);

        TableCellCache.get().removeTable(getData().getWebtable());
        TableRowCache.get().removeTable(getData().getWebtable());
        TableColumnCache.get().removeTable(getData().getWebtable());
        getSimilarities().clean();
        tCleanup.stop();

        timer.stop();
        return result;
    }

    public Map<String, Integer> loadFrequencies() {
        File f = new File("freqs.csv");
        Map<String, Integer> freqs = new HashMap<>();
        try {
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line = read.readLine();
            while (line != null) {
                String[] content = line.split("\t");
                freqs.put(content[0], Integer.parseInt(content[1]));
                line = read.readLine();
            }

        } catch (Exception e) {
            System.out.println("error frequs");
        }
        return freqs;
    }

    public Map<String, Double> loadFrequentItemssetClassStats() {
        File dir = new File("FrequentItemsets");
        Map<String, Double> itemsets = new HashMap<>();

        try {
            for (File f : dir.listFiles()) {
                Double sum = 0.0;
                System.out.println(f.getName());
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(f);

                NodeList nameList = doc.getElementsByTagName("numberOfTransactions");

                for (int temp = 0; temp < nameList.getLength(); temp++) {
                    Element e1Element = (Element) nameList.item(temp);
                    itemsets.put(f.getName().split("\\.")[0].toLowerCase(), (double) Double.parseDouble(e1Element.getTextContent()));
                }
            }
        } catch (Exception e) {
            System.out.println("error itemsets");
        }

        return itemsets;
    }

    public Map<String, Double> loadFrequentItemssetPropStats() {
        File dir = new File("FrequentItemsets");
        Map<String, Double> itemsets = new HashMap<>();

        try {
            for (File f : dir.listFiles()) {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(f);

                NodeList nameList = doc.getElementsByTagName("com.rapidminer.operator.learner.associations.FrequentItemSet");

                for (int temp = 0; temp < nameList.getLength(); temp++) {
                    Element e1Element = (Element) nameList.item(temp);

                    Integer freq = Integer.parseInt(e1Element.getElementsByTagName("frequency").item(0).getTextContent());

                    if (itemsets.containsKey(e1Element.getElementsByTagName("name").item(0).getTextContent())) {
                        double sum = itemsets.get(e1Element.getElementsByTagName("name").item(0).getTextContent());
                        sum++;
                        itemsets.put(e1Element.getElementsByTagName("name").item(0).getTextContent(), sum);
                    } else {
                        itemsets.put(e1Element.getElementsByTagName("name").item(0).getTextContent(), 1.0);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("error itemsets");
        }

        return itemsets;
    }

    public Map<String, Map<String, Double>> loadFrequentItemsset() {
        File dir = new File("FrequentItemsets");

        Map<String, Map<String, Double>> itemsets = new HashMap<>();

        try {
            for (File f : dir.listFiles()) {
                Map<String, Double> m = new HashMap();
                Double sum = 0.0;
                System.out.println(f.getName());
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(f);

                NodeList nameList = doc.getElementsByTagName("com.rapidminer.operator.learner.associations.FrequentItemSet");

                for (int temp = 0; temp < nameList.getLength(); temp++) {
                    Element e1Element = (Element) nameList.item(temp);

                    System.out.println(e1Element
                            .getElementsByTagName("name")
                            .item(0)
                            .getTextContent());

                    System.out.println(e1Element
                            .getElementsByTagName("frequency")
                            .item(0)
                            .getTextContent());
                    Integer freq = Integer.parseInt(e1Element.getElementsByTagName("frequency").item(0).getTextContent());
                    sum += freq;
                    m.put(e1Element.getElementsByTagName("name").item(0).getTextContent(), (double) freq);
                }
//                for (String val : m.keySet()) {
//                    m.get(val).add(sum);
////                    m.put(val, m.get(val) / sum);
//                }
                itemsets.put(f.getName().split("\\.")[0].toLowerCase(), m);
            }
        } catch (Exception e) {
            System.out.println("error itemsets");
        }

        return itemsets;
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

    private List<SimilarityMatrix<Table>> classMatching(Configuration config, SimilarityMatrix<TableRow> instSim) {
        ContextComponent context = new ContextComponent();
        context.initialise(matchers, data, evaluationParameters, goldStandard, logger, matchingParameters, similarities, rootTimer);
        context.run(config);

//         SimilarityMatrix<Table> pageTitle = new SparseSimilarityMatrix<>(0,0);
        SimilarityMatrix<Table> pageTitle = context.getPageTitleSimilarity();
        pageTitle.normalize();

//        SimilarityMatrix<Table> contextSim = new SparseSimilarityMatrix<>(0, 0);
//        SimilarityMatrix<Table> content = new SparseSimilarityMatrix<>(0, 0);
//        SimilarityMatrix<Table> colNames = new SparseSimilarityMatrix<>(0, 0);
        SimilarityMatrix<Table> contextSim = context.getContexteSimilarity();
        contextSim.normalize();
        SimilarityMatrix<Table> content = context.getContentSimilarity();
        content.normalize();
        SimilarityMatrix<Table> colNames = context.getColumnNamesSimilarity();
//        colNames.normalize();
//        SimilarityMatrix<Table> url = new SparseSimilarityMatrix<>(0,0);
        SimilarityMatrix<Table> url = context.getUrlSimilarity();

        //getSimilarities().setClassSimilarity(context.getClassContextMatrix());
        System.out.println(data.getWebtable() + " url: " + data.getWebtable().getSource());

        SecondLineClassMatcher classMatcher = new SecondLineClassMatcher(similarities, matchingParameters, rootTimer, goldStandard, logger);
        classMatcher.setCandidateSimilarity(instSim);
        SimilarityMatrix<Table> classSimilarity = classMatcher.match(getData());
        classSimilarity.pruneWithNullEqualOrBelow(0.0);
//        classSimilarity = new SparseSimilarityMatrix<>(0, 0);

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
            System.out.println("COUNTER: " + d.getHeader() + " " + counter);
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
        List l = new ArrayList<>();
        l.add(combinedWithMajority);
        l.add(pageTitle);
        l.add(contextSim);
        l.add(content);
        l.add(colNames);
        l.add(url);
        l.add(classSimilarity);
        //return combinedWithWeight;
        return l;
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

    public static double calculateCosineSimilarity(Map<String, Integer> vecA, Map<String, Integer> vecB) {
        //vecA needs to be DBpedia!
        for (String s : vecA.keySet()) {
            if (!vecB.containsKey(s)) {
                vecB.put(s, 0);
            }
        }
        // System.out.println(vecA + " vs. " + vecB);

        double dotProduct = dotProduct(vecA, vecB);
        double magnitudeOfA = magnitude(vecA);
        double magnitudeOfB = magnitude(vecB);

        return dotProduct / (magnitudeOfA * magnitudeOfB);
    }

    private static double dotProduct(Map<String, Integer> vecA, Map<String, Integer> vecB) {
        // I'm not validating inputs here for simplicity.
        double dotProduct = 0;
        for (String s : vecA.keySet()) {
            dotProduct += (vecA.get(s) * vecB.get(s));
        }

        return dotProduct;
    }

    // Magnitude of the vector is the square root of the dot product of the vector with itself.
    private static double magnitude(Map<String, Integer> vector) {
        return Math.sqrt(dotProduct(vector, vector));
    }

    /**
     * @return the tableRuntime
     */
    public long getTableRuntime() {
        return tableRuntime;
    }

    /**
     * @param tableRuntime the tableRuntime to set
     */
    public void setTableRuntime(long tableRuntime) {
        this.tableRuntime = tableRuntime;
    }

    /**
     * @return the tableIndex
     */
    public IIndex getTableIndex() {
        return tableIndex;
    }

    /**
     * @param tableIndex the tableIndex to set
     */
    public void setTableIndex(IIndex tableIndex) {
        this.tableIndex = tableIndex;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;

    }

    class ComparatorScore implements Comparator<MatchingPair> {

        @Override
        public int compare(MatchingPair obj1, MatchingPair obj2) {
            if (obj1.getFinalScore() > obj2.getFinalScore()) {
                return 1;
            }
            if (obj1.getFinalScore() < obj2.getFinalScore()) {
                return -1;
            }
            return 0;
        }

    }

}
