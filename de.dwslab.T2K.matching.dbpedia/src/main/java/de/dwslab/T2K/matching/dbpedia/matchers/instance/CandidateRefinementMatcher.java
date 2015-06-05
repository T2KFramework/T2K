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

import java.util.Collection;
import java.util.LinkedList;

import de.dwslab.T2K.matching.dbpedia.blocking.LuceneRefinementBlocking;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowUriMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.settings.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.settings.KeyIndex;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.Similarities;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcherWithFiltering;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrixFactory;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.utils.timer.Timer;

public class CandidateRefinementMatcher extends PartialMatcher<TableRow> {

    private KeyIndex keyIndex;
    public KeyIndex getKeyIndex() {
        return keyIndex;
    }
    public void setKeyIndex(KeyIndex keyIndex) {
        this.keyIndex = keyIndex;
    }
    
    public CandidateRefinementMatcher(Similarities similarities,
            MatchingParameters matchingParameters, Timer rootTimer,
            GoldStandard goldStandard, MatchingLogger logger) {
        super(similarities, matchingParameters, rootTimer, goldStandard, logger);
    }

    private SimilarityMatrix<Table> classSimilarity;
    public SimilarityMatrix<Table> getClassSimilarity() {
        return classSimilarity;
    }
    public void setClassSimilarity(SimilarityMatrix<Table> classSimilarity) {
        this.classSimilarity = classSimilarity;
    }
    
    private SimilarityMatrix<TableRow> initialCandidateSimilarity;
    public SimilarityMatrix<TableRow> getInitialCandidateSimilarity() {
        return initialCandidateSimilarity;
    }
    public void setInitialCandidateSimilarity(
            SimilarityMatrix<TableRow> initialCandidateSimilarity) {
        this.initialCandidateSimilarity = initialCandidateSimilarity;
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
    
    public SimilarityMatrix<TableRow> match(MatchingData data) {
        Timer tim = Timer.getNamed("Candidate refinement", getRootTimer());
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Candidate refinement ... ");
        }

        // run candidate refinement
//        LabelBasedMatcher<TableRow> candidateRefinement = new LabelBasedMatcher<TableRow>();
        LabelBasedMatcherWithFiltering<TableRow> candidateRefinement = new LabelBasedMatcherWithFiltering<TableRow>();
        candidateRefinement.setSimilarityThreshold(getSimilarityThreshold());
            // calculate scores based on key/label string similarity
            StringSimilarityMeasure<TableRow> keyMeasure = new StringSimilarityMeasure<>();
            keyMeasure.setSimilarityFunction(getLabelSimilarityFunction());
            keyMeasure.setSetSimilarity(new MaxSimilarity<>());
        //keyMeasure.setSignatureFilter(new JaccardPrefixFiltering<TableRow>());
            keyMeasure.setSignatureFilter(getStringFilter());
            candidateRefinement.setSimilarityMeasure(keyMeasure);
        
        // determine the classes to use for refinement
        Collection<String> classes = new LinkedList<String>();
        if(getClassSimilarity().getFirstDimension().size()>0) {
            for(Table t : getClassSimilarity().getMatchesAboveThreshold(getClassSimilarity().getFirstDimension().iterator().next(), 0.0)) {
                String s = t.getHeader().replace(".csv", "").replace(".gz", "");
                classes.add(s);
                if(getMatchingParameters().isCollectMatchingInfo()) {
                    System.out.println("Class refinement: " + s + String.format("(%f)", getClassSimilarity().get(data.getWebtable(), t)));
                }
            }
        }
        
        // use lucene index lookup as blocking strategy
        LuceneRefinementBlocking<TableRow> refinementBlocking = new LuceneRefinementBlocking<TableRow>(getKeyIndex().getLuceneIndex(), new TableRowMatchingAdapter(), new TableRowUriMatchingAdapter(), getInitialCandidateSimilarity(), data.getCandidateMap(), classes);
        refinementBlocking.setNumDocuments(getSelectK());
        refinementBlocking.setMaxEditDistance(getMaxEditDist());
        candidateRefinement.setBlocking(refinementBlocking);
        // use a sparse similarity matrix
        candidateRefinement
                .setSimilarityMatrixFactory(new SparseSimilarityMatrixFactory());

        //candidateRefinement.setRunInParallel(false);
        
        // run matching
        Timer tm = Timer.getNamed("LabelBasedMatcher: candidate refinement", tim);
        SimilarityMatrix<TableRow> initialCandidateSimilarity = candidateRefinement.match(data.getWebtableRowSet(),
                data.getDbpediaRowSet(), new TableRowMatchingAdapter());
        tm.stop();
        
        initialCandidateSimilarity.prune(getSimilarityThreshold());
        
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("done.");
            initialCandidateSimilarity.printStatistics("Initial Candidate scores");
        }
        
        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println(initialCandidateSimilarity.getOutput(null, getGoldStandard().getInstanceGoldStandard().values(), new TableRowUriMatchingAdapter(), null));
            //System.out.println(initialCandidateSimilarity.listPairs());
        }
        tim.stop();
        
        return initialCandidateSimilarity;
    }
    
}
