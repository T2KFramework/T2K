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
package de.dwslab.T2K.matching.dbpedia.matchers.instance;

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnToCellHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowToCellHierarchyAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowUriMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.settings.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.Similarities;
import de.dwslab.T2K.matching.secondline.Aggregate;
import de.dwslab.T2K.matching.secondline.AggregationType;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.timer.Timer;

/**
 * Matcher for instances that uses existing similarity matrices and applies pruning in the end
 * @author Oliver
 *
 */
public class PruningInstanceMatcher extends PartialMatcher<TableRow> {

    public PruningInstanceMatcher(Similarities similarities,
            MatchingParameters matchingParameters, Timer rootTimer,
            GoldStandard goldStandard, MatchingLogger logger) {
        super(similarities, matchingParameters, rootTimer, goldStandard, logger);
    }
    
    private SimilarityMatrix<TableColumn> propertySimilarity;
    public SimilarityMatrix<TableColumn> getPropertySimilarity() {
        return propertySimilarity;
    }
    public void setPropertySimilarity(
            SimilarityMatrix<TableColumn> propertySimilarity) {
        this.propertySimilarity = propertySimilarity;
    }
    
    private SimilarityMatrix<TableCell> valueSimilarity;
    public SimilarityMatrix<TableCell> getValueSimilarity() {
        return valueSimilarity;
    }
    public void setValueSimilarity(SimilarityMatrix<TableCell> valueSimilarity) {
        this.valueSimilarity = valueSimilarity;
    }
    
    private SimilarityMatrix<TableRow> initialCandidateSimilarity;
    public SimilarityMatrix<TableRow> getInitialCandidateSimilarity() {
        return initialCandidateSimilarity;
    }
    public void setInitialCandidateSimilarity(
            SimilarityMatrix<TableRow> initialCandidateSimilarity) {
        this.initialCandidateSimilarity = initialCandidateSimilarity;
    }
    
    private double similarityThreshold;
    public double getSimilarityThreshold() {
        return similarityThreshold;
    }
    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
    
    public SimilarityMatrix<TableRow> match(MatchingData data) {
        Timer t = Timer.getNamed("Candidate matching", getRootTimer());
        
        /*
         * combine value scores with property scores so that only the scores of the currently matched properties remain
         * => set all scores of unmapped combinations to 0
         */
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.print("candidate matching 1/2 ... ");
        }
        Timer tm = Timer.getNamed("CombineHierarchy: weight value-based similarities with property similarities", t);
        SimilarityMatrix<TableCell> inst = Matcher.multiplyParentSimilarity(getPropertySimilarity(), getValueSimilarity(), new TableColumnToCellHierarchyAdapter());
        tm.stop();
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("done");
        }
        
        //TODO this pruning here seems to make the results quite good, but also cancels out the value scores in most cases (i.e. only key matching is left ...)
        //inst.prune(0.1);
        
        
        /*
         * aggregate the combined value scores to candidate scores (row based)
         */
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.print("candidate matching 2/2 ... ");
        }
        Aggregate<TableRow, TableCell> a = new Aggregate<TableRow, TableCell>();
        a.setAggregationType(AggregationType.Sum);
        a.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
        tm = Timer.getNamed("Aggregate to candidates", t);
        SimilarityMatrix<TableRow> propWeightedInstanceCand = a.match(getInitialCandidateSimilarity(), inst, new TableRowToCellHierarchyAdapter());
        tm.stop();
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("done");
            System.out.println("Aggregation (value->instance) steps:");
            System.out.println(a.getLog().toString());
        }

        SimilarityMatrix<TableRow> candSim = propWeightedInstanceCand;
        
//        candSim.prune(getMatchingParameters().getInstanceScoreThreshold());
        candSim.prune(getSimilarityThreshold());

        //TODO we could try a 1:1 mapping here ...

        //getSimilarities().setCandidateSimilarity(candSim);
        
        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println(candSim.getOutput(null, getGoldStandard().getInstanceGoldStandard().values(), new TableRowUriMatchingAdapter(), null));
        }
        t.stop();
        
        return candSim;
    }
}
