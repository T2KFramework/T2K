package de.dwslab.T2K.matching.dbpedia.matchers.schema;

import java.util.Collection;

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.dbpedia.AlwaysMatchSimilarityFunction;
import de.dwslab.T2K.matching.dbpedia.algorithm.CandidateSelectionComponent;
import de.dwslab.T2K.matching.dbpedia.algorithm.MatrixStats;
import de.dwslab.T2K.matching.dbpedia.algorithm.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess;
import static de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess.PAR_TABLE_TYPE;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableClassMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableToRowHierarchyAdapter;
import de.dwslab.T2K.matching.secondline.Aggregate;
import de.dwslab.T2K.matching.secondline.AggregationType;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.Combine;
import de.dwslab.T2K.matching.secondline.CombineNonOverlapping;
import de.dwslab.T2K.matching.secondline.ConflictResolution;
import de.dwslab.T2K.matching.secondline.OneToOneConstraint;
import de.dwslab.T2K.matching.secondline.TopKCandidates;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrixFactory;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.timer.Timer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * Matcher for classes that uses existing similarity matrices
 *
 * @author Oliver
 *
 */
public class SecondLineClassMatcher extends PartialMatcher<Table> {

    public SecondLineClassMatcher(Similarities similarities,
            MatchingParameters matchingParameters, Timer rootTimer,
            GoldStandard goldStandard, MatchingLogger logger) {
        super(similarities, matchingParameters, rootTimer, goldStandard, logger);
    }
    private SimilarityMatrix<TableRow> candidateSimilarity;

    public SimilarityMatrix<TableRow> getCandidateSimilarity() {
        return candidateSimilarity;
    }

    public void setCandidateSimilarity(
            SimilarityMatrix<TableRow> candidateSimilarity) {
        this.candidateSimilarity = candidateSimilarity;
    }

    public SimilarityMatrix<Table> match(MatchingData data) {

        // store dimensions of the final similarity matrix as initial similarities
        SimilarityMatrix<Table> initial = Matcher.matchLabels(data.getWebTables(), data.getDbpediaTables(), new TableClassMatchingAdapter(), new StringSimilarityMeasure<Table>(new AlwaysMatchSimilarityFunction(), null));
        //getSimilarities().setInitialClassSimilarity(initial);

        if (getMatchingParameters().isCollectMatchingInfo()) {
            initial.printStatistics("Class Similarity Matrix");
        }

        Timer t = Timer.getNamed("Class matching", getRootTimer());

//        getSimilarities().setFinalClass(null);

        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("class matching 1/2 ... ");
        }


        int all = 0;
        int max = -1;
        for (Table tx : data.getDbpediaTables()) {
            if (tx.getKey() == null) {
                continue;
            }
            all += tx.getKey().getValues().size();
            if (tx.getKey().getValues().size() > max) {
                max = tx.getKey().getValues().size();
            }
        }
        boolean idf = false;
        if (getMatchingParameters().getTableType() == WebtableToDBpediaMatchingProcess.tableType.lodtable) {
            idf = true;
        }
        SimilarityMatrix<TableRow> classWeights = new SparseSimilarityMatrixFactory().createSimilarityMatrix(getCandidateSimilarity().getFirstDimension().size(), getCandidateSimilarity().getSecondDimension().size());

        for (Table dbp : data.getDbpediaTables()) {
            double value;
            if (dbp.getKey() == null) {
                value = 1;
            } else {
                value = (double) dbp.getKey().getValues().size() / (double) max;
            }
            value = 1 - value;
        }

        for (TableRow first : getCandidateSimilarity().getFirstDimension()) {
            //get second or matches?
            for (TableRow second : getCandidateSimilarity().getMatches(first)) {
                double value;
                if (idf) {
                    value = Math.log10((double) max / (double) second.getTable().getKey().getValues().size());
                } else {
                    //frequency-based with max. 
                    if (second.getTable().getKey() == null) {
                        value = 1;
                    }
                    value = (double) second.getTable().getKey().getValues().size() / (double) max;
                    value = 1 - value;
                }
                classWeights.set(first, second, value);
            }
        }
        if (idf) {
            classWeights.makeColumnStochastic();
        }
        //classWeights = new SparseSimilarityMatrix<>(0,0);
        Map<TableRow, List<TableRow>> possibleMatchesByCol = new HashMap<>();

        for (TableRow c : data.getWebtableRowSet()) {
            for (TableRow d : getCandidateSimilarity().getMatches(c)) {
                if (possibleMatchesByCol.containsKey(c)) {
                    possibleMatchesByCol.get(c).add(d);
                } else {
                    List<TableRow> x = new ArrayList<>();
                    x.add(d);
                    possibleMatchesByCol.put(c, x);
                }
            }
            for (TableRow d : classWeights.getMatches(c)) {
                if (possibleMatchesByCol.containsKey(c)) {
                    possibleMatchesByCol.get(c).add(d);
                } else {
                    List<TableRow> x = new ArrayList<>();
                    x.add(d);
                    possibleMatchesByCol.put(c, x);
                }
            }
        }

//        classWeights = new SparseSimilarityMatrix<>(0, 0);
        Combine<TableRow> c = new Combine();
//        c.setAggregationType(CombinationType.Sum);
        c.setAggregationType(CombinationType.WeightedSum);

        getCandidateSimilarity().setName("class cand sim");
        classWeights.setName("class size sim");
        
        
        Map<SimilarityMatrix, MatrixStats> stats = new HashMap<>();
        stats.put(getCandidateSimilarity(), new MatrixStats(getCandidateSimilarity(), data, possibleMatchesByCol));
        stats.put(classWeights, new MatrixStats(classWeights, data, possibleMatchesByCol));
//
//        MatchingResult r1 = new MatchingResult();
//        MatchingResult r2 = new MatchingResult();
//            
//        CandidateSelectionComponent candidateSelection = new CandidateSelectionComponent();
//        
//        
//            candidateSelection.mapInstances(allCandis, false, r1, webtable);
//            candidateSelection.mapInstances(values, false, r2, webtable);
//            
//            System.out.println("correl comp inst label" + webTableName +"\t" + instanceStats.get(allCandis).getNormalizedHerinfahl() + "\t" +instanceStats.get(allCandis).getHerfindahlIndex() + "\t" + instanceStats.get(allCandis).getMean() + "\t" + instanceStats.get(allCandis).getStad()
//                    + "\t" +  r1.getEvaluation().getInstanceResult().getPrecision() + "\t" + r1.getEvaluation().getInstanceResult().getRecall());
//            System.out.println("correl comp inst value" + webTableName +"\t" + instanceStats.get(values).getNormalizedHerinfahl() + "\t" +instanceStats.get(values).getHerfindahlIndex() + "\t" + instanceStats.get(values).getMean() + "\t" + instanceStats.get(values).getStad()
//                    + "\t" +  r2.getEvaluation().getInstanceResult().getPrecision() + "\t" + r2.getEvaluation().getInstanceResult().getRecall());
//
//        

        SimilarityMatrix<TableRow> weightedCandidates;
        CombineNonOverlapping<TableRow> nonOverlap = new CombineNonOverlapping();
        nonOverlap.setAggregationType(CombinationType.Sum);

        SimilarityMatrix x1 = getCandidateSimilarity().copy();
        x1.multiplyScalar(stats.get(getCandidateSimilarity()).getHerfindahlIndex());
        SimilarityMatrix y1 = classWeights.copy();
        y1.multiplyScalar(stats.get(classWeights).getHerfindahlIndex());
        weightedCandidates = nonOverlap.match(x1, y1);

//was used before
//             SimilarityMatrix<TableRow> weightedCandidates = c.match(getCandidateSimilarity(), classWeights);

//        
//        // for each instance of the webtable, we want to consider the class of
//        // the best candidate
//        // hence, we first create a 1:1 mapping
        OneToOneConstraint oneToOne = new OneToOneConstraint(ConflictResolution.Maximum);
        oneToOne.setSimilarityMatrixFactory(new SparseSimilarityMatrixFactory());
        Timer tm = Timer.getNamed("Find best candidate per instance", t);
        SimilarityMatrix<TableRow> bestCandidates = oneToOne.match(weightedCandidates);
                
        //SimilarityMatrix<TableRow> bestCandidates = getCandidateSimilarity();


        tm.stop();
//        
//        if (getMatchingParameters().isCollectMatchingInfo()) {
//            System.out.println("done.");
//            bestCandidates.printStatistics("best candidate per instance");
//        }
//
        //TODO: tried this but it does not help for the class (musicalArtist instead of Person...) -> need a proper way to include the hierarchy!
        TopKCandidates top = new TopKCandidates();
        SimilarityMatrix<TableRow> simTop = top.match(weightedCandidates, 3);


        Aggregate<Table, TableRow> classSum = new Aggregate<Table, TableRow>();
        if (!getMatchingParameters().isRunParallel()) {
            classSum.setRunInParallel(false);
        }
        // now aggregate the candidate scores to the dimensions of the initial
        // class similarity matrix
        classSum.setAggregationType(AggregationType.Count);
        classSum.setSimilarityMatrixFactory(new SparseSimilarityMatrixFactory());
        tm = Timer.getNamed("Count classes", t);
        SimilarityMatrix<Table> classInstSimilarity = classSum.match(initial,simTop, new TableToRowHierarchyAdapter());
//        SimilarityMatrix<Table> classInstSimilarity = classSum.match(initial, bestCandidates, new TableToRowHierarchyAdapter());

        try {
            BufferedWriter writeCandidate = new BufferedWriter(new FileWriter("classes.csv", true));
            double average = 0.0;
            for (Table r1 : classInstSimilarity.getFirstDimension()) {
                average += classInstSimilarity.getMatches(r1).size();
            }
            writeCandidate.write(data.getWebtable().getHeader() + "\t" + average + "\n");
            writeCandidate.flush();
            writeCandidate.close();
        } catch (IOException ex) {
            Logger.getLogger(SecondLineClassMatcher.class.getName()).log(Level.SEVERE, null, ex);
        }

        tm.stop();
        if (getMatchingParameters().isCollectMatchingInfo()) {
            classInstSimilarity.printStatistics("Class counts");
            System.out.println(classInstSimilarity.getOutput());
        }
        getLogger().logData("Class counts\n" + classInstSimilarity.getOutput());
        classInstSimilarity.normalize(candidateSimilarity.getFirstDimension().size());

//        int max = -1;
//        int all=0;
//        for (Table tx : data.getDbpediaTables()) {
//            if (tx.getKey() == null) {
//                continue;
//            }
//            if (tx.getKey().getValues().size() > max) {
//                max = tx.getKey().getValues().size();
//                all +=all;
//            }
//        }
//        SimilarityMatrix<Table> classFrequency =  new SparseSimilarityMatrixFactory().createSimilarityMatrix(classInstSimilarity.getFirstDimension().size(),classInstSimilarity.getSecondDimension().size());
//        
//        double highestValue =-1.0;
//        for(Table first : classFrequency.getFirstDimension()) {
//            for(Table second : classFrequency.getSecondDimension()) {
//                double value = Math.log10((double)all/(double)second.getKey().getValues().size());          
//                if(value>highestValue) {
//                    highestValue = value;
//                }
//                classFrequency.set(first, second, value);
//            }
//        }
//        Combine<Table> c = new Combine();
//        c.setAggregationType(CombinationType.Multiply);
//        SimilarityMatrix<Table> weightedCandidates = c.match(classInstSimilarity, classFrequency);


        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("class matching 1/2 ... done");
            System.out.println("class matching 2/2 ... ");
            weightedCandidates.printStatistics("unpruned Class similarities");
            System.out.println(weightedCandidates.getOutput());
        }

        // make sure that the table is mapped to only one class (with max. score)
//        OneToOneConstraint one = new OneToOneConstraint(ConflictResolution.Maximum);
//        SimilarityMatrix<Table> sim = one.match(getClassSimilarity());
        SimilarityMatrix<Table> sim = classInstSimilarity;
        sim.normalize();

        // we don't want to use all possible classes for the refinement step, so we take only the best ...
        SimilarityMatrix<Table> pruned = sim.copy();
        //pruned.prune(0.1);
        //currently not, TEST!!!
        pruned.prune(0.1);

        if (pruned.getNumberOfNonZeroElements() == 0) {
            if (getMatchingParameters().isCollectMatchingInfo()) {
                System.out.println("Class similarities too low, using top 5 classes");
            }
            // none of the classes made it ... so we just take the top 5
            TopKCandidates topClass = new TopKCandidates();
            sim = topClass.match(sim, 5);
        } else {
            if (getMatchingParameters().isCollectMatchingInfo()) {
                System.out.println("Using all classes with similarity above 0.1");
            }
            sim = pruned;
        }

        sim.normalize();

        //getSimilarities().setClassSimilarity(sim);

        applyClassHierarchy(sim, data.getDbpediaTables());

        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("class matching 2/2 ... done");
            sim.printStatistics("Class similarities");
            System.out.println(sim.getOutput());
        }
        getLogger().logData("class matching\n" + sim.getOutput());
        t.stop();

        return sim;
    }

    protected void applyClassHierarchy(SimilarityMatrix<Table> sim, Collection<Table> dbpediaTables) {
        //SimilarityMatrix<Table> sim = getSimilarities().getClassSimilarity();

        if (sim.getFirstDimension().size() > 0) {
            // include class hierarchy
            Table table = sim.getFirstDimension().iterator().next();
            Table cls = sim.getSecondDimension().iterator().next();
            String clsName = cls.getHeader().replace(".csv", "");
            if (getGoldStandard() != null && getGoldStandard().getClassHierarchy() != null) {
                String parent = getGoldStandard().getClassHierarchy().get(clsName);

                while (parent != null) {

                    String newParent = null;

                    for (Table t : dbpediaTables) {
                        clsName = t.getHeader().replace(".csv", "");

                        if (clsName.equals(parent)) {
                            sim.set(table, t, 0.99); // don't set it to 1 so that the original class gets chosen as final class
                            newParent = getGoldStandard().getClassHierarchy().get(parent);
                            break;
                        }
                    }

                    parent = newParent;

                }
            }
        }
    }
}
