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
package de.dwslab.T2K.matching.dbpedia.matchers.schema;

import java.util.Collection;

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableClassMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableToRowHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.settings.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.Similarities;
import de.dwslab.T2K.matching.dbpedia.similarity.AlwaysMatchSimilarityFunction;
import de.dwslab.T2K.matching.secondline.Aggregate;
import de.dwslab.T2K.matching.secondline.AggregationType;
import de.dwslab.T2K.matching.secondline.ConflictResolution;
import de.dwslab.T2K.matching.secondline.OneToOneConstraint;
import de.dwslab.T2K.matching.secondline.TopKCandidates;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrixFactory;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.utils.timer.Timer;

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
        // for each instance of the webtable, we want to consider the class of
        // the best candidate
        // hence, we first create a 1:1 mapping
        OneToOneConstraint oneToOne = new OneToOneConstraint(
                ConflictResolution.Maximum);
        oneToOne.setSimilarityMatrixFactory(new SparseSimilarityMatrixFactory());
        Timer tm = Timer.getNamed("Find best candidate per instance", t);
        //SimilarityMatrix<TableRow> bestCandidates = oneToOne.match(getSimilarities().getCandidateSimilarity());
        SimilarityMatrix<TableRow> bestCandidates = oneToOne.match(getCandidateSimilarity());
        tm.stop();

        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("done.");
            bestCandidates.printStatistics("best candidate per instance");
        }

        // now aggregate the candidate scores to the dimensions of the initial
        // class similarity matrix
        Aggregate<Table, TableRow> classSum = new Aggregate<Table, TableRow>();
        if (!getMatchingParameters().isRunParallel()) {
            classSum.setRunInParallel(false);;
        }
        classSum.setAggregationType(AggregationType.Count);
        classSum.setSimilarityMatrixFactory(new SparseSimilarityMatrixFactory());
        tm = Timer.getNamed("Count classes", t);
//        SimilarityMatrix<Table> classInstSimilarity = classSum.match(getSimilarities().getClassSimilarity(),bestCandidates, new TableToRowHierarchyAdapter());
        SimilarityMatrix<Table> classInstSimilarity = classSum.match(initial, bestCandidates, new TableToRowHierarchyAdapter());
        tm.stop();
        if (getMatchingParameters().isCollectMatchingInfo()) {
            classInstSimilarity.printStatistics("Class counts");
            System.out.println(classInstSimilarity.getOutput());
        }
        getLogger().logData("Class counts\n" + classInstSimilarity.getOutput());
        classInstSimilarity.normalize(candidateSimilarity.getFirstDimension()
                .size());

        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("class matching 1/2 ... done");
            System.out.println("class matching 2/2 ... ");
            classInstSimilarity.printStatistics("unpruned Class similarities");
            System.out.println(classInstSimilarity.getOutput());
        }

        // make sure that the table is mapped to only one class (with max. score)
//        OneToOneConstraint one = new OneToOneConstraint(ConflictResolution.Maximum);
//        SimilarityMatrix<Table> sim = one.match(getClassSimilarity());
        SimilarityMatrix<Table> sim = classInstSimilarity;

        // we don't want to use all possible classes for the refinement step, so we take only the best ...
        SimilarityMatrix<Table> pruned = sim.copy();
        pruned.prune(0.1);

        if (pruned.getNumberOfNonZeroElements() == 0) {
            if (getMatchingParameters().isCollectMatchingInfo()) {
                System.out.println("Class similarities too low, using top 5 classes");
            }
            // none of the classes made it ... so we just take the top 5
            TopKCandidates top = new TopKCandidates();
            sim = top.match(sim, 5);
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
