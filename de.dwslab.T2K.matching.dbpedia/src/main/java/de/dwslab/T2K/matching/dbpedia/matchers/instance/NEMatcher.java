package de.dwslab.T2K.matching.dbpedia.matchers.instance;

import de.dwslab.T2K.matching.dbpedia.LuceneRefinementBlocking;
import de.dwslab.T2K.matching.dbpedia.algorithm.KeyIndex;
import de.dwslab.T2K.matching.dbpedia.algorithm.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.NEMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowUriMatchingAdapter;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcherWithFiltering;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrixFactory;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.utils.timer.Timer;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author domi
 */
public class NEMatcher extends PartialMatcher<TableRow> {

    public NEMatcher(Similarities similarities, MatchingParameters matchingParameters, Timer rootTimer, GoldStandard goldStandard, MatchingLogger logger) {
        super(similarities, matchingParameters, rootTimer, goldStandard, logger);
    }

      private KeyIndex keyIndex;
    public KeyIndex getKeyIndex() {
        return keyIndex;
    }
    public void setKeyIndex(KeyIndex keyIndex) {
        this.keyIndex = keyIndex;
    }
        
    private SimilarityMatrix<TableRow> initialCandidateSimilarity;
    public SimilarityMatrix<TableRow> getInitialCandidateSimilarity() {
        return initialCandidateSimilarity;
    }
    
    private SimilarityFunction<String> labelSimilarityFunction;
    public SimilarityFunction<String> getLabelSimilarityFunction() {
        return labelSimilarityFunction;
    }
    public void setLabelSimilarityFunction(
            SimilarityFunction<String> labelSimilarityFunction) {
        this.labelSimilarityFunction = labelSimilarityFunction;
    }
    
    private int selectK;
    public int getSelectK() {
        return selectK;
    }
    public void setSelectK(int selectK) {
        this.selectK = selectK;
    }
    
    private int maxEditDist;
    public int getMaxEditDist() {
        return maxEditDist;
    }
    public void setMaxEditDist(int maxEditDist) {
        this.maxEditDist = maxEditDist;
    }
    
    private double similarityThreshold;
    public double getSimilarityThreshold() {
        return similarityThreshold;
    }
    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
    
    private SignatureFilter<TableRow> stringFilter;
    public SignatureFilter<TableRow> getStringFilter() {
        return stringFilter;
    }
    public void setStringFilter(SignatureFilter<TableRow> stringFilter) {
        this.stringFilter = stringFilter;
    }
    
    private Collection<String> classes;
    
    
    @Override
    public SimilarityMatrix<TableRow> match(MatchingData data) {
        
        Timer tim = Timer.getNamed("NE Matching", getRootTimer());

        LabelBasedMatcherWithFiltering<TableRow> candidateRefinement = new LabelBasedMatcherWithFiltering<>();
        candidateRefinement.setSimilarityThreshold(getSimilarityThreshold());
        // calculate scores based on key/label string similarity
        StringSimilarityMeasure<TableRow> keyMeasure = new StringSimilarityMeasure<>();
        keyMeasure.setSimilarityFunction(getLabelSimilarityFunction());
        keyMeasure.setSetSimilarity(new MaxSimilarity<>());
        //keyMeasure.setSignatureFilter(new JaccardPrefixFiltering<TableRow>());
        keyMeasure.setSignatureFilter(getStringFilter());
        candidateRefinement.setSimilarityMeasure(keyMeasure);
        
//        System.out.println("classes: " +getClasses());
        initialCandidateSimilarity = new SparseSimilarityMatrix(data.getWebtableRowSet().size(), data.getDbpediaRowSet().size());
        LuceneRefinementBlocking<TableRow> refinementBlocking = new LuceneRefinementBlocking<>(getKeyIndex().getLuceneIndex(), adapter, new TableRowUriMatchingAdapter(), getInitialCandidateSimilarity(), data.getCandidateMap(),null);
        refinementBlocking.setNumDocuments(getSelectK());
        refinementBlocking.setMaxEditDistance(getMaxEditDist());        
        candidateRefinement.setBlocking(refinementBlocking);
        // use a sparse similarity matrix
        candidateRefinement.setSimilarityMatrixFactory(new SparseSimilarityMatrixFactory());

        // run matching
        Timer tm = Timer.getNamed("LabelBasedMatcher: candidate refinement", tim);
        initialCandidateSimilarity = candidateRefinement.match(data.getWebtableRowSet(),data.getDbpediaRowSet(), adapter);
        
        tm.stop();
        
        return initialCandidateSimilarity;

    }
    
    private NEMatchingAdapter adapter;
    
    /**
     * @param adapter the adapter to set
     */
    public void setAdapter(NEMatchingAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * @return the classes
     */
    public Collection<String> getClasses() {
        return classes;
    }

    /**
     * @param classes the classes to set
     */
    public void setClasses(Collection<String> classes) {
        this.classes = classes;
    }

}
