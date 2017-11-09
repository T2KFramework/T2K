package de.dwslab.T2K.matching.dbpedia.algorithm;

import au.com.bytecode.opencsv.CSVWriter;
import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.blocking.Blocking;
import de.dwslab.T2K.matching.blocking.IdentityBlocking;
import de.dwslab.T2K.matching.blocking.TypeBasedBlocking;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.AlwaysMatchSimilarityFunction;
import static de.dwslab.T2K.matching.dbpedia.algorithm.InstanceMatchingTask.PAR_ABSTRACT_WEIGHT;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.DuplicateBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.LabelBasedSchemaMatcher;
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
import de.dwslab.T2K.matching.dbpedia.properties.Canoniser;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcher;
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
import de.dwslab.T2K.tableprocessor.model.Statistic;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.tableprocessor.model.TableMapping;
import de.dwslab.T2K.tableprocessor.model.json.AnnotatedTable;
import de.dwslab.T2K.utils.concurrent.Consumer;
import de.dwslab.T2K.utils.concurrent.Parallel;
import de.dwslab.T2K.utils.data.Pair;
import de.dwslab.T2K.utils.io.CSVUtils;
import de.dwslab.T2K.utils.query.Func;
import de.dwslab.T2K.utils.query.Q;
import de.dwslab.T2K.utils.timer.Timer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

/**
 *
 * @author domi
 */
public class PropertyMatchingTask extends MatchingProcess {

    private long runtime;
    private long tableRuntime;
    private MatchingData data;
    private String abstractCandidateResults;

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

        goldStandard = new GoldStandard();
        goldStandard.initialise(webtableName, getData().getWebtable(), getEvaluationParameters());
    }

    public void loadDBpedia(String dbpediaDirectory) {
        setData(new MatchingData());
        getData().loadDBpedia(dbpediaDirectory, getMatchingParameters());
    }
    private ValueBasedComponent valueBased;

    public ValueBasedComponent getValueBasedComponent() {
        return valueBased;
    }
    private CandidateSelectionComponent candidateSelection;
    private LabelBasedSchemaMatcher labelMatcher;
    private DuplicateBasedSchemaMatcher dp;
    private NEComponent ne;

    protected void initialiseComponents() {

        candidateSelection = new CandidateSelectionComponent();
        candidateSelection.initialise(getMatchers(), getData(), getEvaluationParameters(), getGoldStandard(), getLogger(), getMatchingParameters(), getSimilarities(), getRootTimer());
        candidateSelection.setKeyIndex(new KeyIndex());
        candidateSelection.setWebTableName(getWebtableName());

        valueBased = new ValueBasedComponent();
        valueBased.initialise(getMatchers(), getData(), getEvaluationParameters(), getGoldStandard(), getLogger(), getMatchingParameters(), getSimilarities(), getRootTimer());
        valueBased.setWebTableName(getWebtableName());

        labelMatcher = new LabelBasedSchemaMatcher(similarities, matchingParameters, rootTimer, goldStandard, logger);

        dp = new DuplicateBasedSchemaMatcher(similarities, matchingParameters, rootTimer, goldStandard, logger);

        ne = new NEComponent();
        ne.initialise(matchers, data, evaluationParameters, goldStandard, logger, matchingParameters, similarities, rootTimer);
        ne.setWebTableName(webtable);

    }
    public static final Parameter PAR_WEBTABLE = new Parameter("Process.WebTable");
    public static final Parameter PAR_MAPPED_RATIO_FILTER = new Parameter("Process.MappedRatioFilter", 0.5);
    public static final Parameter PAR_EVALUATE = new Parameter("Process.Evaluate", true);
    public static final Parameter PAR_SPANNING_CELL_THRESHOLD = new Parameter("Process.SpanningCellThreshold", 1);
    public static final Parameter PAR_MAX_PARALLEL = new Parameter("Process.MaxParallel", 0);
    public static final Parameter PAR_TABLE_TYPE = new Parameter("Process.TableType", WebtableToDBpediaMatchingProcess.tableType.webtable);
    public static final Parameter PAR_MAX_NUM_ROWS = new Parameter("Process.MaxNumberOfRows", 0);
    public static final Parameter PAR_MAX_NUM_COLS = new Parameter("Process.MaxNumberOfCols", 0);
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

        initialiseComponents();

        getLogger().logData(webTableName + "\n" + getData().getWebtable().printTable());
        getLogger().logData(String.format("\n key column: [%d] %s", getData().getWebtable().getKeyIndex(), getData().getWebtable().getKey().getHeader()));
        System.out.println(String.format("\n key column: [%d] %s", getData().getWebtable().getKeyIndex(), getData().getWebtable().getKey().getHeader()));

        boolean wordnet = false;
        if (wordnet) {

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
                    System.out.println("search for: " + adap.getLabel(c).toString());
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
                                        System.out.println(" word: " + c.getHeader() + " added: " + w.getLemma());
                                        hypernymsToAdd.add(w.getLemma());
                                        i++;
                                    }
                                }
                            }
                        }
                    }
                    if (!hypernymsToAdd.isEmpty()) {
                        hypernymsToAdd.add(c.getHeader().toString());
                        c.setHeaderList(hypernymsToAdd);
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                    continue;
                }
            }
        }


        getMatchers().getLabelBasedSchemaMatcher().setLabelSimilarity((SimilarityFunction<String>) config.getValue(PAR_LABEL_SIMILARITY));
        getMatchers().getLabelBasedSchemaMatcher().setSetSimilarity(new MaxSimilarity<String>());
        //test with no blocking!
        getMatchers().getLabelBasedSchemaMatcher().setBlocking(new TypeBasedBlocking<TableColumn>(new TableColumnMatchingAdapter()));
        // getMatchers().getLabelBasedSchemaMatcher().setBlocking(new IdentityBlocking());
        //       getMatchers().getLabelBasedSchemaMatcher().setAllowedClasses(allowedClasses);
        SimilarityMatrix<TableColumn> labelSimilarity = getMatchers().getLabelBasedSchemaMatcher().match(getData());



        //TEST: NO PRUNING FOR THE LEARNING
        //     labelSimilarity.pruneWithNull((double) config.getValue(PAR_PROP_LABEL_THRESHOLD));
        //labelSimilarity.normalize();

        getSimilarities().setPropertySimilarity(labelSimilarity);


        SimilarityMatrix<TableRow> instSim = computePropertyMappings(true);

        getMatchers().getClassMatcher().setCandidateSimilarity(instSim);
        SimilarityMatrix<Table> classSimilarity = getMatchers().getClassMatcher().match(getData());
        SimilarityMatrix<Table> classMatrix = candidateSelection.mapClasses(classSimilarity, new MatchingResult(), false);
        List<Table> chosenClass = new ArrayList<>();
        for (Table a : classSimilarity.getFirstDimension()) {
            for (Table b : classSimilarity.getMatches(a)) {
                System.out.println("class: " + b);
                chosenClass.add(b);
            }
        }

        getSimilarities().setCandidateSimilarity(instSim);
        valueBased.run(config);
        getSimilarities().setValueSimilarity(valueBased.getValueSimilarity());


        dp.setValueSimilarity(getSimilarities().getValueSimilarity());
        dp.setCandidateSimilarity(instSim);
        dp.setNumCandidatesPerInstance(1);
        dp.setCandidateThreshold((double) config.getValue(PAR_PROP_CANDIDATE_THRESHOLD));
        dp.setValueThreshold((double) config.getValue(PAR_PROP_VALUE_THRESHOLD));
        dp.setNumVotesPerInstance((int) config.getValue(PAR_PROP_NUM_VOTES));

        LabelBasedSchemaMatcher lbs = new LabelBasedSchemaMatcher(similarities, matchingParameters, timer, goldStandard, logger);
        lbs.setLabelSimilarity((SimilarityFunction<String>) new AlwaysMatchSimilarityFunction());
        lbs.setBlocking(new TypeBasedBlocking<TableColumn>(new TableColumnMatchingAdapter()));
        SimilarityMatrix<TableColumn> allLabelSimilarity = lbs.match(getData());

        dp.setLabelSimilarity(allLabelSimilarity);
        dp.setNumResults((int) config.getValue(PAR_PROP_NUM_RESULTS));
        dp.setFinalThreshold(0.0);


        SimilarityMatrix<TableColumn> props = dp.match(data);
//        props.normalize();
//        getSimilarities().setPropertySimilarity(props);

        Canoniser propertyCanoniser = evaluationParameters.getEquivPropertyCanoniser();



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

        for (TableColumn c1 : props.getFirstDimension()) {
            for (TableColumn d : props.getMatches(c1)) {
                if (!allowedCols.contains(d.getURI())) {
                    //            if (!allowedCols.contains(d.getURI()) || !chosenClass.contains(d.getTable())) {
                    System.out.println("not an allowed col props: " + d + " class " + d.getTable());
                    props.set(c1, d, null);
                    continue;
                }
                double value = props.get(c1, d);
//                    if (rowToUri.containsKey(d.getURI().toString()) && !rowToUri.get(d.getURI().toString()).equals(d)) {
//                        //props.set(c1, d, null);
//                    }
                if (d.getURI().contains("http://www.w3.org") || d.getURI().contains("http://purl.org")) {
                    props.set(c1, d, null);
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
        //before: 0.6! BUT NOW WE HAVE ANOTHER MEASURE!!
        labelSimilarity.pruneWithNullEqualOrBelow(0.0);


        for (TableColumn c1 : labelSimilarity.getFirstDimension()) {
            for (TableColumn d : labelSimilarity.getMatches(c1)) {

                if (!allowedCols.contains(d.getURI())) {
                    //              if (!allowedCols.contains(d.getURI()) || !chosenClass.contains(d.getTable()) ) {
                    System.out.println("not an allowed col label: " + d + " class " + d.getTable());
                    labelSimilarity.set(c1, d, null);
                    continue;
                }

                double value = labelSimilarity.get(c1, d);

                //              System.out.println("label c1: " + c1 + " d " + d + " value " + value);
//                if (rowToUri.containsKey(d.getURI().toString()) && !rowToUri.get(d.getURI().toString()).equals(d)) {
//                    labelSimilarity.set(c1, d, null);
////                }
                if (d.getURI().contains("http://www.w3.org") || d.getURI().contains("http://purl.org")) {
                    labelSimilarity.set(c1, d, null);
                }
                String newUri = propertyCanoniser.canoniseResource(d.getURI().toString());
                //               System.out.println("newUri: " + newUri + " d URI " + d.getURI() + " equals? " +newUri.equals(d.getURI().toString()));
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

        Map<TableColumn, List<TableColumn>> possibleMatchesByCol = new HashMap<>();
        Map<String, TableColumn> rowToUri = new HashMap<>();
        int sizeOfPoss = 0;
        for (TableColumn c : getData().getWebtable().getColumns()) {
            if (c.isKey()) {
                continue;
            }

            for (TableColumn d : props.getMatches(c)) {
                if (!allowedCols.contains(d.getURI())) {
                    continue;
                }
                sizeOfPoss++;
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
                sizeOfPoss++;
                if (possibleMatchesByCol.containsKey(c)) {
                    possibleMatchesByCol.get(c).add(d);
                } else {
                    List<TableColumn> x = new ArrayList<>();
                    x.add(d);
                    possibleMatchesByCol.put(c, x);
                }
            }


        }

        SimilarityMatrix<TableColumn> domain = new SparseSimilarityMatrix<>(data.getWebtable().getColumns().size(), sizeOfPoss);

//        Map<Table, Double> domainCounter = new HashMap<>();
//        for (TableRow x : instSim.getFirstDimension()) {
//            for (TableRow y : instSim.getMatches(x)) {
//                if (domainCounter.containsKey(y.getTable())) {
//                    domainCounter.put(y.getTable(), domainCounter.get(y.getTable()) + 1);
//                } else {
//                    domainCounter.put(y.getTable(), 1.0);
//                }
//            }
//        }
//
//        for (TableColumn c : possibleMatchesByCol.keySet()) {
//            for (TableColumn d : possibleMatchesByCol.get(c)) {
//                System.out.println("c: " + c + " -- " + d + " --- " + domainCounter.get(d.getTable()));
//                domain.set(c, d, domainCounter.get(d.getTable()));
//            }
//        }

        domain.normalize();

        SimilarityMatrix<TableColumn> kurt = new SparseSimilarityMatrix(getData().getWebtable().getColumns().size(), sizeOfPoss);

        for (TableColumn c : props.getFirstDimension()) {
            for (TableColumn d : props.getMatchesAboveThreshold(c, 0.001)) {
                if (d.getDataType() == TableColumn.ColumnDataType.numeric && d.getColumnStatistic().getKurtosis() == 0.0) {
                    computeKurt(d);
                }
                if (c.getDataType() == d.getDataType() && c.getDataType() == TableColumn.ColumnDataType.numeric) {
                    double value;
                    System.out.println("d kurt: " + d.getColumnStatistic().getKurtosis() + " c kurt: " + c.getColumnStatistic().getKurtosis());
                    if (d.getColumnStatistic().getKurtosis() > c.getColumnStatistic().getKurtosis()) {
                        value = c.getColumnStatistic().getKurtosis() / d.getColumnStatistic().getKurtosis();
                    } else {
                        value = d.getColumnStatistic().getKurtosis() / c.getColumnStatistic().getKurtosis();
                    }
                    System.out.println("set kurt: " + c + " -- " + d + " -- " + value);
                    if (value > 0) {
                        kurt.set(c, d, value);
                    } else {
                        kurt.set(c, d, 0.0);
                    }
                }
            }
        }
        kurt.normalize();

        SimilarityMatrix columnWidth = new SparseSimilarityMatrix(getData().getWebtable().getColumns().size(), sizeOfPoss);

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

        Map<SimilarityMatrix, MatrixStats> stats = new HashMap<>();
        props.setName("props");
        labelSimilarity.setName("label");
        stats.put(labelSimilarity, new MatrixStats(labelSimilarity, getData(), possibleMatchesByCol));
        stats.put(props, new MatrixStats(props, getData(), possibleMatchesByCol));

        CombineNonOverlapping nonOverlap = new CombineNonOverlapping();
        nonOverlap.setAggregationType(CombinationType.Sum);

        SimilarityMatrix x = props.copy();
        if (stats.get(props).getHerfindahlIndex() != Double.NaN) {
            x.multiplyScalar(stats.get(props).getHerfindahlIndex());
        }

        SimilarityMatrix y = labelSimilarity.copy();
        y.multiplyScalar(stats.get(labelSimilarity).getHerfindahlIndex());
        SimilarityMatrix<TableColumn> allCandidates = nonOverlap.match(x, y);

//test again! wrong multiply!!!
//        stats.put(kurt, new MatrixStats(kurt, getData(), possibleMatchesByCol));
//        x = kurt.copy();
//        x.setName("kurt");
//        if (stats.get(kurt).getHerfindahlIndex() != Double.NaN) {
//            x.multiplyScalar(stats.get(kurt).getHerfindahlIndex());
//            allCandidates = nonOverlap.match(allCandidates, x);
//        }

//        domain.setName("domain");
//        stats.put(domain, new MatrixStats(domain, getData(), possibleMatchesByCol));
//        x = domain.copy();
//        if (stats.get(domain).getHerfindahlIndex() != Double.NaN) {
//            x.multiplyScalar(stats.get(domain).getHerfindahlIndex());
//            allCandidates = nonOverlap.match(allCandidates, x);
//        }

        //       allCandidates = labelSimilarity;



        //no pruning currently!
        allCandidates.pruneWithNullEqualOrBelow(0.0);



//        for (TableColumn c : allCandidates.getFirstDimension()) {
//            for (TableColumn d : allCandidates.getMatches(c)) {
//                if (rowToUri.containsKey(d.getURI().toString()) && !rowToUri.get(d.getURI().toString()).equals(d)) {
//                    allCandidates.set(c, d, null);
//                }
//            }
//        }

        for (TableColumn c : allCandidates.getFirstDimension()) {
            Map<String, Double> duplicateDetection = new HashMap<>();
            for (TableColumn d : allCandidates.getMatches(c)) {
//                    System.out.println("d: " + d);
                if (!duplicateDetection.containsKey(d.getURI().toString())) {
                    duplicateDetection.put(d.getURI().toString(), 0.0);
                }
                double sum = allCandidates.get(c, d);
                //System.out.println("counter " + counter);

                if (sum >= duplicateDetection.get(d.getURI().toString())) {
                    duplicateDetection.put(d.getURI().toString(), sum);
                    rowToUri.put(d.getURI().toString(), d);
                    System.out.println("added: " + d.getURI().toString() + " val: " + rowToUri.get(d.getURI().toString()));
                }
            }
        }

        for (TableColumn c1 : allCandidates.getFirstDimension()) {
            for (TableColumn d : allCandidates.getMatches(c1)) {
                if (rowToUri.containsKey(d.getURI().toString()) && !rowToUri.get(d.getURI().toString()).equals(d)) {
                    allCandidates.set(c1, d, null);
                }
            }
        }


        getSimilarities().setPropertySimilarity(allCandidates);
//        getSimilarities().setPropertySimilarity(props);

//        currently no pruning!
//               allCandidates.pruneWithNull((double) config.getValue(PAR_PROP_FINAL_THRESHOLD));


        //later!
//        ne.setPropertySimilarity(allCandidates);
//        ne.run(config);
//        getSimilarities().setPropertySimilarity(ne.getWeightedPropertySimilarity());

        SimilarityMatrix<TableColumn> one2one = candidateSelection.mapProperties(getSimilarities().getPropertySimilarity().copy(), webTableName, true, result);
        SimilarityMatrix<TableColumn> one2oneNE = candidateSelection.mapProperties(getSimilarities().getPropertySimilarity().copy(), webTableName, true, result);

        StringBuilder b = new StringBuilder();

        EvaluationAdapter<TableColumn> evalRow = new ColumnAdapter();
        EvaluationAdapter<TableColumn> evalInstance = new DBpediaPropertyAdapter(
                getGoldStandard().getPropertyCanoniser());

        for (TableColumn c : allCandidates.getFirstDimension()) {
            if (c.isKey()) {
                continue;
            }
            Object o1 = evalRow.getUniqueIdentifier(c);

            for (TableColumn d : allCandidates.getMatches(c)) {
                if (d.getURI().contains("http://www.w3.org") || d.getURI().contains("http://purl.org")) {
                    continue;
                }
                b.append(c.getTable().getFullPath() + "_" + c.getHeader() + "\t");
                b.append(d.getURI() + "\t");
                if (props.get(c, d) != null) {
                    b.append(props.get(c, d) + "\t");
                } else {
                    b.append("\t");
                }
                if (labelSimilarity.get(c, d) != null) {
                    b.append(labelSimilarity.get(c, d) + "\t");
                } else {
                    b.append("\t");
                }
                if (kurt.get(c, d) != null) {
                    b.append(kurt.get(c, d) + "\t");
                } else {
                    b.append("\t");
                }
//                if (columnWidth.get(c, d) != null) {
//                    System.out.println("append " + " c: " + c.getHeader() + " d: " + d.getURI() + " class: " + d.getTable().getHeader() + " score: " + columnWidth.get(c, d));
//                    b.append(columnWidth.get(c, d) + "\t");
//                } else {
//                    b.append("\t");
//                }                
                if (allCandidates.get(c, d) != null) {
                    b.append(allCandidates.get(c, d) + "\t");
                } else {
                    b.append("\t");
                }
                if (one2one.get(c, d) != null) {
                    b.append("1\t");
                } else {
                    b.append("0\t");
                }
//                if (one2oneNE.get(c, d) != null) {
//                    b.append("1\t");
//                } else {
//                    b.append("0\t");
//                }
                Object o2 = evalInstance.getUniqueIdentifier(d);
                if (!getGoldStandard().getPropertyGoldStandard().containsKey(o1)) {
                    b.append("0\n");
                } else if (getGoldStandard().getPropertyGoldStandard().get(o1).equals(o2)) {
                    b.append("1\n");
                } else {
                    b.append("0\n");
                }
            }
        }
        abstractCandidateResults = b.toString();


        candidateSelection.mapProperties(getSimilarities().getPropertySimilarity(), webTableName, true, result);

        result.setWebtable(getData().getWebtable());
        result.setMatchingData(getData());

        if ((Boolean) config.getValue(PAR_EVALUATE)) {
            result.setGoldStandard(getGoldStandard());
            result.setSimilarities(getSimilarities());

            //          analyseResult(result);
            if (result.getEvaluation().getPropertyResult() == null) {
                //writeRunResult(config, result);
                //return result;
            } else {
                System.out.println(webTableName);
//               

                System.out.println("\tPrecision\tRecall\tF1  \t#correct/#mappings/#reference\n");
                EvaluationResult r = result.getEvaluation().getPropertyResult();
                System.out.println(String.format(
                        "result:\t     %.2f\t  %.2f\t  %.2f\t%d/%d/%d",
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
//                    if (!isInData(correctKey)) {
//                        reasons.put(cor, REASON_NOT_IN_DATA);
//                        cntNotInData++;
//                    } else if (!isInCandidateList(correctKey)) {
//                        reasons.put(cor, REASON_NOT_IN_CANDIDATES);
//                    }
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

    private void computeKurt(TableColumn c) {
        SynchronizedDescriptiveStatistics statistics = new SynchronizedDescriptiveStatistics();
        //no limit on the values stored in the dataset?! -1?
        statistics.setWindowSize(-1);
        for (Object o : c.getValues().values()) {
            if (o instanceof List) {
                for (Object innerObject : (List) o) {
                    statistics.addValue((double) innerObject);
                }
            } else {
                statistics.addValue((double) o);
            }
        }
        Statistic s = new Statistic();
        c.setColumnStatistic(s);
        s.setKurtosis(statistics.getKurtosis());
    }

    private SimilarityMatrix<TableRow> computePropertyMappings(boolean gs) {
        SimilarityMatrix<TableRow> instSim = null;
        EvaluationAdapter<TableRow> evalRow = new CandidateAdapter();
        EvaluationAdapter<TableRow> evalInstance = new DBpediaInstanceAdapter(goldStandard.getInstanceCanoniser());
        if (gs) {
            instSim = new SparseSimilarityMatrix(getData().getWebtable().getTotalNumOfRows(), getGoldStandard().getInstanceGoldStandard().size());
            Map<Object, Object> instGS = getGoldStandard().getInstanceGoldStandard();

            for (TableRow r : getData().getWebtableRowSet()) {

                if (instGS.containsKey(evalRow.getUniqueIdentifier(r))) {
                    try {
                        for (TableRow d : getData().getUriMap().get(instGS.get(evalRow.getUniqueIdentifier(r)).toString())) {
                            instSim.set(r, d, 1.0);
                        }
                    } catch (Exception e) {
                        System.out.println(evalRow.getUniqueIdentifier(r));
                    }
                }
            }

        } else {
            try {
                instSim = new SparseSimilarityMatrix(getData().getWebtable().getTotalNumOfRows(), getGoldStandard().getInstanceGoldStandard().size());

                TableMapping tm = new TableMapping();
                tm.readMapping("/home/dritze/WTMatching/mappings/" + getData().getWebtable().getHeader());
                Map<Integer, Pair<String, Double>> readInstMappings = tm.getMappedInstances();
                for (TableRow r : getData().getWebtableRowSet()) {

                    if (readInstMappings.containsKey(Integer.parseInt(evalRow.getUniqueIdentifier(r).toString()))) {
                        Pair<String, Double> singleCorres = readInstMappings.get(Integer.parseInt(evalRow.getUniqueIdentifier(r).toString()));
                        for (TableRow d : getData().getDbpediaRowSet()) {
                            if (evalInstance.getUniqueIdentifier(d).equals(singleCorres.getFirst())) {
                                instSim.set(r, d, singleCorres.getSecond());
                            }
                        }
                    }
                }

            } catch (Exception e) {
            }
        }
        return instSim;
    }

    public PropertyMatchingTask clone() {
        PropertyMatchingTask wp = new PropertyMatchingTask();
        wp.setSimilarities(this.similarities);
        wp.setData(data);
        wp.setMatchers(matchers);
        wp.setGoldStandard(goldStandard);
        wp.setEvaluationParameters(evaluationParameters);
        wp.setLogger(logger);
        wp.setMatchingParameters(matchingParameters);
        wp.setParentTimer(parentTimer);
        wp.setRootTimer(rootTimer);
        return wp;
    }

    public PropertyMatchingTask() {
        List<Parameter> params = CandidateSelectionComponent.getParams();
        params.add(PAR_WEBTABLE);
        params.add(PAR_MAPPED_RATIO_FILTER);
        params.add(PAR_EVALUATE);
        params.add(PAR_SPANNING_CELL_THRESHOLD);
        params.add(PAR_MAX_PARALLEL);
        params.add(PAR_TABLE_TYPE);
        params.add(PAR_MAX_NUM_ROWS);
        params.add(PAR_MAX_NUM_COLS);
        params.add(PAR_PROP_KEY_WEIGHT);
        params.add(PAR_LABEL_SIMILARITY);

        params.add(PAR_RUN_LABEL_MATCHING);

        params.add(PAR_PROP_NUM_CANDIDATES);
        params.add(PAR_PROP_CANDIDATE_THRESHOLD);
        params.add(PAR_PROP_VALUE_THRESHOLD);
        params.add(PAR_PROP_NUM_VOTES);
        params.add(PAR_PROP_NUM_RESULTS);
        params.add(PAR_PROP_FINAL_THRESHOLD);
        params.add(PAR_PROP_VALUE_WEIGHT);
        params.add(PAR_PROP_LABEL_THRESHOLD);
        setParameters(params);
    }
}
