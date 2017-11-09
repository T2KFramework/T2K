package de.dwslab.T2K.matching.dbpedia.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import au.com.bytecode.opencsv.CSVWriter;
import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.dbpedia.WebJaccardSimilarity;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateRefinementMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateSelectionMatcher;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableToRowHierarchyAdapter;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.CombineHierarchy;
import de.dwslab.T2K.matching.similarity.signatures.JaccardPrefixFiltering;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.string.JaccardSimilarity;
import de.dwslab.T2K.similarity.functions.string.LevenshteinSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.utils.timer.Timer;
import java.util.Arrays;
import java.util.HashSet;

public class CandidateSelectionComponent extends WebtableToDBpediaMatchingComponent {

    private KeyIndex keyIndex;

    public KeyIndex getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(KeyIndex keyIndex) {
        this.keyIndex = keyIndex;
    }

    private EvaluationResult initialInstanceResult;

    public EvaluationResult getInitialInstanceResult() {
        return initialInstanceResult;
    }

    private EvaluationResult prunedInstanceResult;

    public EvaluationResult getPrunedInstanceResult() {
        return prunedInstanceResult;
    }

    private EvaluationResult initialClassResult;

    public EvaluationResult getInitialClassResult() {
        return initialClassResult;
    }

    Collection<Correspondence<TableRow>> initialInstanceMappings;

    public int getMaxCorrectCanddiates() {
        int numCorrect = 0;
        for (Correspondence<TableRow> cor : initialInstanceMappings) {
            if (isInCandidateList(cor.getCorrectValue())) {
                numCorrect++;
            }
        }
        return numCorrect;
    }

    @Override
    public double evaluate(Configuration config) {
        super.evaluate(config);
        // evaluation based on candidate list
        // good if all correct candidates are in the list (position doesn't matter)
        // good if the candidate lists are as small as possible

        // i.e. number of correct candidates / number of total candidates
        int numCorrect = 0;
        for (Correspondence<TableRow> cor : initialInstanceMappings) {
            if (isInCandidateList(cor.getCorrectValue())) {
                numCorrect++;
            }
        }

        //double result= (double)numCorrect / (double)refinedCandidateSimilarity.getSecondDimension().size();
        //double result= (double)numCorrect - 1.0/(double)refinedCandidateSimilarity.getSecondDimension().size();
        double result = (double) numCorrect / (double) initialInstanceResult.getInputSetSize(); // = max. recall
        //double prec = refinedInstanceResult.getPrecision();

        //double result = 2 * maxRecall * prec / (maxRecall + prec);
        //double result = numCorrect;
        System.out.println(String.format("%d / %d correct candidates = %.4f", numCorrect, initialCandidateSimilarity.getSecondDimension().size(), result));
        System.out.println(config.print());

        try {
            File f = new File("CandidateSelectionComponent.csv");

            boolean writeHeaders = !f.exists();

            CSVWriter w = new CSVWriter(new BufferedWriter(new FileWriter(f, true)));

            if (writeHeaders) {
                String[] headers = new String[]{"webtable", "baseline prec", "baseline recall", "baseline f1", "pruned prec", "pruned recall", "pruned f1", "refined prec", "refined recall", "refined f1", "max recall"};
                headers = (String[]) ArrayUtils.addAll(headers, config.getParameterNames());
                w.writeNext(headers);
            }

            String[] values = new String[]{
                getWebTableName(),
                Double.toString(initialInstanceResult.getPrecision()),
                Double.toString(initialInstanceResult.getRecall()),
                Double.toString(initialInstanceResult.getF1Score()),
                Double.toString(prunedInstanceResult.getPrecision()),
                Double.toString(prunedInstanceResult.getRecall()),
                Double.toString(prunedInstanceResult.getF1Score()),
                Double.toString(result)
            };

            values = (String[]) ArrayUtils.addAll(values, config.getValues());

            w.writeNext(values);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    protected boolean isInCandidateList(Object key) {

        for (TableRow r : initialCandidateSimilarity.getSecondDimension()) {
            if (key instanceof List) {
                for (Object s : (List) key) {
                    if (r.getURI().equals(s)) {
                        return true;
                    }
                }
            }
            if (r.getURI().equals(key)) {
                return true;
            }
        }

        return false;
    }

    private SimilarityMatrix<TableRow> initialCandidateSimilarity;
    private SimilarityMatrix<TableRow> prunedCandidateSimilarity;
    private SimilarityMatrix<Table> classSimilarity;

    public SimilarityMatrix<Table> getClassSimilarity() {
        return classSimilarity;
    }

    public static final Parameter PAR_INITIAL_SIMILARITY_FUNCTION = new Parameter("CandidateSelection.InitialSimilarityFunction", new WebJaccardSimilarity());
    public static final Parameter PAR_INITIAL_K = new Parameter("CandidateSelection.InitialK", 500);
    public static final Parameter PAR_INTIAL_EDIT_DIST = new Parameter("CandidateSelection.InitialEditDistance", 0);
    public static final Parameter PAR_INITIAL_THRESHOLD = new Parameter("CandidateSelection.InitialThreshold", 1.0);
    public static final Parameter PAR_INITIAL_STRING_FILTERING = new Parameter("CandidateSelection.InitialStringFiltering", new JaccardPrefixFiltering<>());

    protected static final List<Parameter> params;

    public static List<Parameter> getParams() {
        return params;
    }

    static {
        ArrayList<Parameter> l = new ArrayList<>();
        l.add(PAR_INITIAL_SIMILARITY_FUNCTION);
        l.add(PAR_INITIAL_K);
        l.add(PAR_INTIAL_EDIT_DIST);
        l.add(PAR_INITIAL_STRING_FILTERING);
        l.add(PAR_INITIAL_THRESHOLD);
        params = l;
    }

    public CandidateSelectionComponent() {
        setParameters(params);
    }

    @SuppressWarnings("unchecked")
    protected void initialiseParameters(Configuration config) {

        CandidateSelectionMatcher candsel = getMatchers().getCandidateSelectionMatcher();
        candsel.setLabelSimilarityFunction((SimilarityFunction<String>) config.getValue(PAR_INITIAL_SIMILARITY_FUNCTION));
        candsel.setSelectK((Integer) config.getValue(PAR_INITIAL_K));
        candsel.setMaxEditDist((Integer) config.getValue(PAR_INTIAL_EDIT_DIST));
        candsel.setSimilarityThreshold((Double) config.getValue(PAR_INITIAL_THRESHOLD));
        candsel.setStringFilter((SignatureFilter<TableRow>) config.getValue(PAR_INITIAL_STRING_FILTERING));

    }

    @Override
    public void run(Configuration config) {
        initialiseParameters(config);

        System.out.println("Candidate Selection Component");

        MatchingResult logResult = new MatchingResult();

        // Candidate Selection
        getMatchers().getCandidateSelectionMatcher().setLogCandidateSelection(true);
        setInitialCandidateSimilarity(getMatchers().getCandidateSelectionMatcher().match(getData()));

        if (getGoldStandard() != null) {
//            for(String s : getData().getCandidateMap().keySet()) {
//                System.out.println("map! " + s + "\t" +getData().getCandidateMap().get(s) + "\t" + getData().getCandidateMap().get(s).get(0).getTable().getHeader());
//            }
            // we re-use the set of candidates created during candidate selection, so we can't adjust the gold standard earlier
            getGoldStandard().adjustToSubset(getKeyIndex().getLuceneBlocking().getCandidateMap().keySet(), getData().getDbpediaPropUris());
        }

        boolean wiki = true;
        if (wiki) {
            for (TableRow r : getData().getWebtableRowSet()) {
                boolean hyphen = false; 
                List<String> key = new ArrayList<>();
                String keyAsString = "";                
                if (r.getKey() instanceof List) {
                    List l = (List) r.getKey();
                    keyAsString += l.get(0).toString();
                    if(l.get(0).toString().contains("-") && !l.get(0).toString().contains("\\s")) {
                        key.addAll(Arrays.asList(l.get(0).toString().split("-")));
                        hyphen = true;
                    }
                    else {
                        key.addAll(Arrays.asList(l.get(0).toString().split("\\s")));
                    }
                } else {
                    keyAsString += r.getKey().toString();
                    if(r.getKey().toString().contains("-") && !r.getKey().toString().contains("\\s")) {
                        key.addAll(Arrays.asList(r.getKey().toString().split("-")));
                        hyphen = true;
                    }
                    else {
                        key.addAll(Arrays.asList(r.getKey().toString().split("\\s")));
                    }
                }
//                key.add(r.getTable().getKey().getHeader().toString().toLowerCase());
//                keyAsString += " " + r.getTable().getKey().getHeader().toString().toLowerCase();
                keyAsString = keyAsString.trim();
                //try without header!
                //TODO: > 2 or > 1?
                if ((key.size() > 2 && keyAsString.matches(".*\\d.*") && !hyphen) || 
                (key.size() > 1 && keyAsString.matches(".*\\d.*") && hyphen)) {
                    for (String s : getData().getCandidateMap().keySet()) {
                        boolean containsAll = true;
                        for (String keyS : key) {
                            if (!s.toLowerCase().contains(keyS)) {
                                containsAll = false;
                                break;
                            }
                        }
                        if (containsAll) {
                            List<TableRow> rows = getData().getCandidateMap().get(s);                            
                            for (TableRow r2 : rows) {
                                JaccardSimilarity gsj = new JaccardSimilarity();
                                double js = gsj.calculate(keyAsString, s);
                                getInitialCandidateSimilarity().set(r, r2, 1.0+js);
                            }
                        }
                    }
                }
                //try with header
                //TODO: > 2 or > 1?
                key.add(r.getTable().getKey().getHeader().toString().toLowerCase());
                keyAsString += " " + r.getTable().getKey().getHeader().toString().toLowerCase();
                keyAsString = keyAsString.trim();
                if (key.size() > 2 && keyAsString.matches(".*\\d.*")) {
                    for (String s : getData().getCandidateMap().keySet()) {
                        boolean containsAll = true;
                        for (String keyS : key) {
                            if (!s.toLowerCase().contains(keyS)) {
                                containsAll = false;
                                break;
                            }
                        }
                        if (containsAll) {
                            List<TableRow> rows = getData().getCandidateMap().get(s);                            
                            for (TableRow r2 : rows) {
                                JaccardSimilarity gsj = new JaccardSimilarity();
                                double js = gsj.calculate(keyAsString, s);
                                getInitialCandidateSimilarity().set(r, r2, 1.0+js);
                            }
                        }
                    }
                }   
//                //try with title
//                key.add(r.getTable().getHeader().replaceAll("\\.html.*", "").toLowerCase());
//                key.remove(r.getTable().getKey().getHeader().toString().toLowerCase());
//                keyAsString += " " + r.getTable().getHeader().replaceAll("\\.html.*", "").toLowerCase();
//                keyAsString = keyAsString.trim();
//                if (key.size() > 2 && keyAsString.matches(".*\\d.*")) {
//                    for (String s : getData().getCandidateMap().keySet()) {
//                        boolean containsAll = true;
//                        for (String keyS : key) {
//                            if (!s.toLowerCase().contains(keyS)) {
//                                containsAll = false;
//                                break;
//                            }
//                        }
//                        if (containsAll) {
//                            List<TableRow> rows = getData().getCandidateMap().get(s);                            
//                            for (TableRow r2 : rows) {
//                                JaccardSimilarity gsj = new JaccardSimilarity();
//                                double js = gsj.calculate(keyAsString, s);
//                                getInitialCandidateSimilarity().set(r, r2, 1.0+js);
//                            }
//                        }
//                    }
//                }                
            }            
        }
        getInitialCandidateSimilarity().pruneWithNull((Double) config.getValue(PAR_INITIAL_THRESHOLD));
        
        for(TableRow r : getInitialCandidateSimilarity().getFirstDimension()) {
            for(TableRow s : getInitialCandidateSimilarity().getMatches(r)) {
               System.out.println("res: " + r + "\t" + s + "\t" + getInitialCandidateSimilarity().get(r, s) + "\t" + s.getURI() + "\t" + s.getTable().getHeader());
            }
        }

        mapInstances(getInitialCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), logResult, "baseline");

        initialInstanceResult = logResult.getEvaluation().getInstanceResult();
        initialInstanceMappings = logResult.getInstanceMappings();
        //addIntermediaResult("instances", "[01] baseline", instanceBaseLine);

        // Class Matching
        // current approach is to use class matching to reduce the number of wrong candidates, so we only consider the majority class
        getMatchers().getClassMatcher().setCandidateSimilarity(getInitialCandidateSimilarity());
        classSimilarity = getMatchers().getClassMatcher().match(getData());
        mapClasses(classSimilarity, logResult, false);
        initialClassResult = logResult.getEvaluation().getClassResult();

        //TEST!!!!
        mapInstances(getInitialCandidateSimilarity(), getMatchingParameters().isCollectMatchingInfo(), logResult, "not majority");
        //updateCandidatesBasedOnClasses();        
        //mapInstances(prunedCandidateSimilarity, getMatchingParameters().isCollectMatchingInfo(), logResult, "pruned");

        prunedInstanceResult = logResult.getEvaluation().getInstanceResult();
        //addIntermediaResult("instances", "[02] pruned", logResult.getEvaluation().getInstanceResult());

        // candidate refinement
        getMatchingParameters().setFastJoinDelta(0.5); // = edit distance threshold
        getMatchingParameters().setFastJoinTau(0.5); // = jaccard threshold
        //       getMatchers().getCandidateRefinementMatcher().setClassSimilarity(classSimilarity);

        //TEST!!!
        //getMatchers().getCandidateRefinementMatcher().setInitialCandidateSimilarity(prunedCandidateSimilarity);
        //     getMatchers().getCandidateRefinementMatcher().setInitialCandidateSimilarity(getInitialCandidateSimilarity());
    }

    /**
     * @return the initialCandidateSimilarity
     */
    public SimilarityMatrix<TableRow> getInitialCandidateSimilarity() {
        return initialCandidateSimilarity;
    }

    /**
     * @param initialCandidateSimilarity the initialCandidateSimilarity to set
     */
    public void setInitialCandidateSimilarity(SimilarityMatrix<TableRow> initialCandidateSimilarity) {
        this.initialCandidateSimilarity = initialCandidateSimilarity;
    }

    /**
     * removes candidates that do not belong to the majority class
     */
//    protected void updateCandidatesBasedOnClasses() {
//        Timer t = Timer.getNamed("Update Candidates based on Classes", getRootTimer());
//        int before = initialCandidateSimilarity.getNumberOfNonZeroElements();
//        
//        // multiply new class similarities with candidate similarities (sets all candidates with wrong class to 0)
//        CombineHierarchy<Table, TableRow> c = new CombineHierarchy<Table, TableRow>();
//        c.setAggregationType(CombinationType.Multiply);
//        Timer tm = Timer.getNamed("Multiply Class with Candidate similarities", t);
//        prunedCandidateSimilarity = c.match(classSimilarity, initialCandidateSimilarity, new TableToRowHierarchyAdapter());
//        tm.stop();
//        
//        prunedCandidateSimilarity.normalize();
//                
//        if(getMatchingParameters().isCollectMatchingInfo()) {
//            System.out.println("Majority Class decision:");
//            System.out.println(prunedCandidateSimilarity.getOutput());
//            System.out.println(String.format("Remove candiates with wrong class: removed %d/%d", before - prunedCandidateSimilarity.getNumberOfNonZeroElements(), before));
//        }
//        t.stop();
//    }
}
