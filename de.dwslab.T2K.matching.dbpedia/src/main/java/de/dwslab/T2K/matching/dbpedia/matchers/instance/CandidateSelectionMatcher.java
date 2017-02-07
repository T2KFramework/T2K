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
package de.dwslab.T2K.matching.dbpedia.matchers.instance;

import de.dwslab.T2K.matching.dbpedia.LuceneBlocking;
import de.dwslab.T2K.matching.dbpedia.algorithm.KeyIndex;
import de.dwslab.T2K.matching.dbpedia.algorithm.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowUriMatchingAdapter;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcherWithFiltering;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.functions.set.MaxSimilarity;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.utils.timer.Timer;

/**
 * Matcher that performs the candidate selection and assigns initial
 * similarities (label based) for the instances
 *
 * @author Oliver
 *
 */
public class CandidateSelectionMatcher extends PartialMatcher<TableRow> {

    private KeyIndex keyIndex;

    public KeyIndex getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(KeyIndex keyIndex) {
        this.keyIndex = keyIndex;
    }

    public CandidateSelectionMatcher(Similarities similarities,
            MatchingParameters matchingParameters, Timer rootTimer,
            GoldStandard goldStandard, MatchingLogger logger) {
        super(similarities, matchingParameters, rootTimer, goldStandard, logger);
    }
    private SimilarityMatrix<TableRow> candidateSimilarity;

    public SimilarityMatrix<TableRow> getCandidateSimilarity() {
        return candidateSimilarity;
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
    private boolean logCandidateSelection = false;

    public boolean isLogCandidateSelection() {
        return logCandidateSelection;
    }

    public void setLogCandidateSelection(boolean logCandidateSelection) {
        this.logCandidateSelection = logCandidateSelection;
    }

    public SimilarityMatrix<TableRow> match(MatchingData data) {
        Timer t = Timer.getNamed("Candidate Selection", getRootTimer());
        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("Candidate selection ... ");
        }

        Timer tm = Timer.getNamed("setup candidate selection", t);
        // run candidate selection

//        LabelBasedMatcher<TableRow> candidateSelection = new LabelBasedMatcher<TableRow>();
        LabelBasedMatcherWithFiltering<TableRow> candidateSelection = new LabelBasedMatcherWithFiltering<TableRow>();
        candidateSelection.setSimilarityThreshold(getSimilarityThreshold());

        // calculate scores based on key/label string similarity
        StringSimilarityMeasure<TableRow> keyMeasure = new StringSimilarityMeasure<>();
        keyMeasure.setSimilarityFunction(getLabelSimilarityFunction());
        keyMeasure.setSetSimilarity(new MaxSimilarity<>());

        //keyMeasure.setSignatureFilter(new JaccardPrefixFiltering<TableRow>());
        keyMeasure.setSignatureFilter(getStringFilter());

        candidateSelection.setSimilarityMeasure(keyMeasure);

        // use lucene index lookup as blocking strategy
        LuceneBlocking<TableRow> luceneBlocking = null;

        if (getKeyIndex().getLuceneBlocking() == null) {

            luceneBlocking = new LuceneBlocking<TableRow>(getKeyIndex().getLuceneIndex(),
                    new TableRowMatchingAdapter(), new TableRowUriMatchingAdapter());

            luceneBlocking.setNumDocuments(getSelectK());
            luceneBlocking.setMaxEditDistance(getMaxEditDist());

            getKeyIndex().setLuceneBlocking(luceneBlocking);

            luceneBlocking.setCandidateMap(data.getCandidateMap());

            if (isLogCandidateSelection()) {
                luceneBlocking.setVerbose(true);
            }
        } else {
            luceneBlocking = getKeyIndex().getLuceneBlocking();
        }

        candidateSelection.setBlocking(luceneBlocking);
        tm.stop();

        // run matching
        tm = Timer.getNamed("LabelBasedMatcher: candidate selection", t);

        candidateSelection.setParentTimer(tm);
        SimilarityMatrix<TableRow> initialCandidateSimilarity = candidateSelection.match(data.getWebtableRowSet(),
                data.getDbpediaRowSet(), new TableRowMatchingAdapter());

        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("before.");
            System.out.println(initialCandidateSimilarity.getOutput());
        }
        
//        SimilarityMatrix<TableRow> equivalentInstances = new SparseSimilarityMatrixFactory().createSimilarityMatrix(initialCandidateSimilarity.getFirstDimension().size(), initialCandidateSimilarity.getSecondDimension().size());
//
//        for (TableRow tr : initialCandidateSimilarity.getFirstDimension()) {
//            for (TableRow tr2 : initialCandidateSimilarity.getSecondDimension()) {
//                equivalentInstances.set(tr, tr2, 0.0);
//            }
//        }
//        
//        TableColumn sameAs = null;
//        for (TableColumn c : data.getWebtable().getColumns()) {
//            if (c.getHeader().contains("owl#sameAs")) {
//                sameAs = c;
//            }
//        }
//        if (sameAs != null) {
//            Map<Integer, Object> sameAsValues = sameAs.getValues();
//            for (TableRow tr : initialCandidateSimilarity.getFirstDimension()) {
//                Object link = sameAsValues.get(tr.getRowIndex());
//                if (link == null) {
//                    continue;
//                }    
//                for (TableRow tr2 : initialCandidateSimilarity.getSecondDimension()) {
//                    if (link instanceof List) {
//                        List l = (List) link;
//                        for (Object u : l) {
//                            if (u.toString().startsWith("http://dbpedia.org")) {
//                                if (u.toString().equals(tr2.getURI())) {
//                                    System.out.println(u);
//                                    equivalentInstances.set(tr, tr2, 3.0);
//                                } 
//                            }
//                        }
//                    } else {
//                        if (link.equals(tr2.getURI())) {
//                            System.out.println(link);
//                            equivalentInstances.set(tr, tr2, 3.0);
//                        } 
//                    }
//                }
//            }
//        }
//
//        
//        
//        Combine<TableRow> c = new Combine();
//        c.setAggregationType(CombinationType.Sum);
//        SimilarityMatrix<TableRow> weightedCandidates = c.match(initialCandidateSimilarity, equivalentInstances);
//        weightedCandidates.normalize();
//        
//        initialCandidateSimilarity = weightedCandidates;
        
        initialCandidateSimilarity.normalize();

        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("weighted.");
            System.out.println(initialCandidateSimilarity.getOutput());
        }
        
        
//        for(TableRow r : initialCandidateSimilarity.getFirstDimension()) {
//            System.out.println("match before! " + r);
//            for(TableRow re : initialCandidateSimilarity.getMatches(r)) {
//                System.out.println("value: " +re);
//            }
//        }

        tm.stop();

        tm = Timer.getNamed("candidate pruning", t);
        initialCandidateSimilarity.pruneWithNull(getSimilarityThreshold());
        tm.stop();
//        
//        for(TableRow r : initialCandidateSimilarity.getFirstDimension()) {
//            System.out.println("match! " + r);
//            for(TableRow re : initialCandidateSimilarity.getMatches(r)) {
//                System.out.println("value: " +re);
//            }
//        }

        if (getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println("done.");
            System.out.println(initialCandidateSimilarity.getOutput());
        }

        //getSimilarities().setInitialCandidateSimilarity(initialCandidateSimilarity);
        candidateSimilarity = initialCandidateSimilarity;

//        if (getMatchingParameters().isCollectMatchingInfo()) {
        //System.out.println(initialCandidateSimilarity.getOutput(null, instanceGoldStandard.values(), new TableRowUriMatchingAdapter(), null));
        //System.out.println(initialCandidateSimilarity.listPairs());
//        }
        t.stop();

        return candidateSimilarity;
    }
}
