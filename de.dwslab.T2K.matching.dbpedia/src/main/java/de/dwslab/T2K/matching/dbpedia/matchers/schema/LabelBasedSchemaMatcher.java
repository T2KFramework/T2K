package de.dwslab.T2K.matching.dbpedia.matchers.schema;

import de.dwslab.T2K.matching.blocking.Blocking;
import de.dwslab.T2K.matching.blocking.IdentityBlocking;
import de.dwslab.T2K.matching.blocking.TypeBasedBlocking;
import de.dwslab.T2K.matching.dbpedia.AlwaysMatchSimilarityFunction;
import de.dwslab.T2K.matching.dbpedia.algorithm.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnHeaderMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnUriMatchingAdapter;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcher;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.ComplexSetSimilarity;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.timer.Timer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Label-based Schema Matcher for properties
 *
 * @author Oliver
 *
 */
public class LabelBasedSchemaMatcher extends PartialMatcher<TableColumn> {

    public LabelBasedSchemaMatcher(Similarities sim, MatchingParameters par, Timer t, GoldStandard g, MatchingLogger logger) {
        super(sim, par, t, g, logger);
    }
    private SimilarityMatrix<Table> classSimilarity;

    public SimilarityMatrix<Table> getClassSimilarity() {
        return classSimilarity;
    }

    public void setClassSimilarity(SimilarityMatrix<Table> classSimilarity) {
        this.classSimilarity = classSimilarity;
    }
    private SimilarityFunction<String> labelSimilarity = null;

    /**
     * @return the labelSimilarity
     */
    public SimilarityFunction<String> getLabelSimilarity() {
        return labelSimilarity;
    }
    private Blocking blocking = null;

    /**
     * @return the labelSimilarity
     */
    public void setBlocking(Blocking blocking) {
        this.blocking = blocking;
    }

    /**
     * @param labelSimilarity the labelSimilarity to set
     */
    public void setLabelSimilarity(SimilarityFunction<String> labelSimilarity) {
        this.labelSimilarity = labelSimilarity;
    }

    private ComplexSetSimilarity<String> setSimilarity = null;

    private List<Table> allowedClasses;

    private Collection<TableColumn> dbpediaColSet;

    private Collection<TableColumn> wtColSet;

    private Collection<TableColumn> otherColSet;

    public SimilarityMatrix<TableColumn> match(MatchingData data) {

        Timer t = Timer.getNamed("Label matching", getRootTimer());

        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.print("label matching ... ");
        }

        LabelBasedMatcher<TableColumn> labelMatching = new LabelBasedMatcher<TableColumn>();

        labelMatching.setBlocking(blocking);

        // calculate scores based on column/label string similarity
        StringSimilarityMeasure<TableColumn> colMeasure = new StringSimilarityMeasure<>();
        colMeasure.setSimilarityFunction(getLabelSimilarity());
        colMeasure.setSetSimilarity(new MaxSimilarity());
        labelMatching.setSimilarityMeasure(colMeasure);

        labelMatching.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
        labelMatching.setRunInParallel(false);

        // run matching
        Timer tm = Timer.getNamed("LabelBasedMatcher: instance labels", t);
//        Collection<TableColumn> dedupProperties = new HashSet<>();
//        Map<String, TableColumn> uriMap = new HashMap<>();
//        for(TableColumn tc : data.getDbpediaColSet()) {
//            if(!uriMap.containsKey(tc.getURI())) {
//                uriMap.put(tc.getURI(), tc);
//            }
//        }
//        for(String uri : uriMap.keySet()) {
//            dedupProperties.add(uriMap.get(uri));
//        }
//        
//         SimilarityMatrix<TableColumn> labelSimilarity = labelMatching.match(data.getWebtable().getColumns(),
//         dedupProperties, new TableColumnMatchingAdapter());
        Collection<TableColumn> allowedCols = new HashSet<>();

        if (otherColSet != null) {
            setColSet(otherColSet);
        } else {
            if (getAllowedClasses() != null) {
                for (TableColumn c : data.getDbpediaColSet()) {
                    //if(getAllowedClasses().contains(c.getTable().getHeader().split("\\.")[0].toLowerCase())) {
                    if (getAllowedClasses().contains(c.getTable())) {
                        allowedCols.add(c);
                    }
                }
            } else {
                allowedCols = data.getDbpediaColSet();
            }
            if (getColSet() == null) {
                setColSet(allowedCols);
            } else {
                allowedCols = new HashSet<>();
                for (TableColumn c : getColSet()) {
                    //if(getAllowedClasses().contains(c.getTable().getHeader().split("\\.")[0].toLowerCase())) {
                    if (getAllowedClasses().contains(c.getTable())) {
                        allowedCols.add(c);
                    }
                }
                setColSet(allowedCols);
            }

        }
        SimilarityMatrix<TableColumn> labelSimilarity;

        if (wtColSet == null) {
            labelSimilarity = labelMatching.match(data.getWebtable().getColumns(),
                    getColSet(), new TableColumnMatchingAdapter());
        } else {
            labelSimilarity = labelMatching.match(wtColSet,
                    getColSet(), new TableColumnMatchingAdapter());
        }
        tm.stop();

        // set the matching score of all key columns to 1
        //CURRENTLY!
//         makeKeysMatch(labelSimilarity, data);
        // set the similarity of all unmatched pairs to 0 
        for (TableColumn c : labelSimilarity.getFirstDimension()) {
            for (TableColumn c2 : labelSimilarity.getSecondDimension()) {

                if (c.isKey()) {
                    labelSimilarity.set(c, c2, null);
                }

            }
        }

        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("done.");
            labelSimilarity.printStatistics("Label similarities");
            System.out.println(labelSimilarity.getOutput(null, getGoldStandard().getPropertyGoldStandard().values(), new TableColumnUriMatchingAdapter(), null));

            System.out.println(labelMatching.getMatchingLog().toString());
        }

        t.stop();

        return labelSimilarity;
    }

    protected void makeKeysMatch(SimilarityMatrix<TableColumn> sim, MatchingData data) {
        // set the matching score of all key columns to 1
        TableColumn wtKey = data.getWebtable().getKey();
        for (Table matchingTable : getClassSimilarity().getSecondDimension()) {
            sim.set(wtKey, matchingTable.getKey(), 1.0);
        }
    }

    /**
     * @return the allowedClasses
     */
    public List<Table> getAllowedClasses() {
        return allowedClasses;
    }

    /**
     * @param allowedClasses the allowedClasses to set
     */
    public void setAllowedClasses(List<Table> allowedClasses) {
        this.allowedClasses = allowedClasses;
    }

    /**
     * @return the setSimilarity
     */
    public ComplexSetSimilarity<String> getSetSimilarity() {
        return setSimilarity;
    }

    /**
     * @param setSimilarity the setSimilarity to set
     */
    public void setSetSimilarity(ComplexSetSimilarity<String> setSimilarity) {
        this.setSimilarity = setSimilarity;
    }

    /**
     * @return the colSet
     */
    public Collection<TableColumn> getColSet() {
        return dbpediaColSet;
    }

    /**
     * @param colSet the colSet to set
     */
    public void setColSet(Collection<TableColumn> colSet) {
        this.dbpediaColSet = colSet;
    }

    /**
     * @return the wtColSet
     */
    public Collection<TableColumn> getWtColSet() {
        return wtColSet;
    }

    /**
     * @param wtColSet the wtColSet to set
     */
    public void setWtColSet(Collection<TableColumn> wtColSet) {
        this.wtColSet = wtColSet;
    }

    /**
     * @return the otherColSet
     */
    public Collection<TableColumn> getOtherColSet() {
        return otherColSet;
    }

    /**
     * @param otherColSet the otherColSet to set
     */
    public void setOtherColSet(Collection<TableColumn> otherColSet) {
        this.otherColSet = otherColSet;
    }
}
