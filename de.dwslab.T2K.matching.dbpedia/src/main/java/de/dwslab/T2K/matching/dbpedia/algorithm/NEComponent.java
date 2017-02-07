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
package de.dwslab.T2K.matching.dbpedia.algorithm;

import de.dwslab.T2K.matching.dbpedia.WebJaccardSimilarity;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.NEMatcher;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.NEMatchingAdapter;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.secondline.CombinationType;
import de.dwslab.T2K.matching.secondline.CombineNonOverlapping;
import de.dwslab.T2K.matching.similarity.signatures.JaccardPrefixFiltering;
import de.dwslab.T2K.matching.similarity.signatures.SignatureFilter;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.matrix.SparseSimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author domi
 */
public class NEComponent extends WebtableToDBpediaMatchingComponent {

    private KeyIndex keyIndex;

    public KeyIndex getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(KeyIndex keyIndex) {
        this.keyIndex = keyIndex;
    }
    
    SimilarityMatrix<TableColumn> propertySimilarity;

    public void setPropertySimilarity(SimilarityMatrix<TableColumn> propertySimilarity) {
        this.propertySimilarity = propertySimilarity;
    }
    
    private SimilarityMatrix<TableColumn> weightedPropertySimilarity;

    public static final Parameter PAR_REFINEMENT_SIMILARITY_FUNCTION = new Parameter("CandidateSelection.RefinementSimilarityFunction", new WebJaccardSimilarity());
    public static final Parameter PAR_REFINEMENT_K = new Parameter("CandidateSelection.RefinementK", 20);
    public static final Parameter PAR_REFINEMENT_EDIT_DIST = new Parameter("CandidateSelection.RefinementEditDistance", 2);
    public static final Parameter PAR_REFINEMENT_THRESHOLD = new Parameter("CandidateSelection.RefinementThreshold", 0.2);
    public static final Parameter PAR_REFINEMENT_STRING_FILTERING = new Parameter("CandidateSelection.RefinementStringFiltering", new JaccardPrefixFiltering<>());
    public static final Parameter PAR_NE_WEIGHT = new Parameter("NEComponent.NEWeight", 0.2);

    public static final Parameter PAR_LOG_CANDIDATE_SELECTION = new Parameter("CandidateSelection.VerboseLogging", false);

    protected static final List<Parameter> params;

    public static List<Parameter> getParams() {
        return params;
    }

    static {
        ArrayList<Parameter> l = new ArrayList<>();
        l.add(PAR_REFINEMENT_SIMILARITY_FUNCTION);
        l.add(PAR_REFINEMENT_K);
        l.add(PAR_REFINEMENT_EDIT_DIST);
        l.add(PAR_REFINEMENT_THRESHOLD);
        l.add(PAR_REFINEMENT_STRING_FILTERING);
        l.add(PAR_LOG_CANDIDATE_SELECTION);
        l.add(PAR_NE_WEIGHT);
        params = l;
    }

    @SuppressWarnings("unchecked")
    protected void initialiseParameters(Configuration config) {

        NEMatcher candref = getMatchers().getNEMatcher();
        candref.setLabelSimilarityFunction((SimilarityFunction<String>) config.getValue(PAR_REFINEMENT_SIMILARITY_FUNCTION));
        candref.setSelectK((Integer) config.getValue(PAR_REFINEMENT_K));
        candref.setMaxEditDist((Integer) config.getValue(PAR_REFINEMENT_EDIT_DIST));
        candref.setSimilarityThreshold((Double) config.getValue(PAR_REFINEMENT_THRESHOLD));
        candref.setStringFilter((SignatureFilter<TableRow>) config.getValue(PAR_REFINEMENT_STRING_FILTERING));
    }
    
    private List<SimilarityMatrix> allMatrices = new ArrayList<>();

    @Override
    public void run(Configuration config) {        
        
        initialiseParameters(config);
        Map<String,List<String>> ranges = getData().getNEPropertyRanges();
        SimilarityMatrix countProps = new SparseSimilarityMatrix(propertySimilarity.getFirstDimension().size(), propertySimilarity.getSecondDimension().size());
        boolean foundSomething = false;        
        
        for(TableColumn c : propertySimilarity.getFirstDimension()) {
            if(c.isKey()) {
                continue;
            }
            for(TableColumn db : propertySimilarity.getMatches(c)) {
                if(ranges.containsKey(db.getURI())) {
                    foundSomething = true;
//                    System.out.println("property: " +db.getURI());
                    NEMatchingAdapter adapter = new NEMatchingAdapter();
                    adapter.setColumnIndex(c.getTable().getColumns().indexOf(c));
//                    System.out.println("index of c: " + adapter.getColumnIndex());
                    getMatchers().getNEMatcher().setAdapter(adapter);
//                    System.out.println("NE: ");
//                    System.out.println(getMatchers().getNEMatcher());
//                    System.out.println(ranges.size());
//                    System.out.println(ranges.get(db.getURI()));
                    getMatchers().getNEMatcher().setClasses(ranges.get(db.getURI()));
                    SimilarityMatrix<TableRow> refinedCandidateSimilarity = getMatchers().getNEMatcher().match(getData());                    
                    allMatrices.add(refinedCandidateSimilarity);
                    //System.out.println("refines size: " + refinedCandidateSimilarity.getFirstDimension() + " X " + refinedCandidateSimilarity.getSecondDimension());
                    
                    int count =0;
                    for(TableRow r1 : refinedCandidateSimilarity.getFirstDimension()) {                        
                        if(refinedCandidateSimilarity.getMatches(r1).size()>0) {
                            for(TableRow r2 : refinedCandidateSimilarity.getMatches(r1)) {
                                if(ranges.get(db.getURI()).contains(r2.getTable().getHeader().replace(".csv","").replace(".gz", ""))) {
                                    count++;
                                    break;
                                }
                            }                            
                        }
                    }
                    double score = (double)count/(double)refinedCandidateSimilarity.getFirstDimension().size();
                    if(score > 0) {
                        countProps.set(c, db, score);
                    }
                }
            }
        }
        if(foundSomething) {
            CombineNonOverlapping c = new CombineNonOverlapping();
            c.setAggregationType(CombinationType.WeightedSum);
            double firstWeight = (double)config.getValue(PAR_NE_WEIGHT);
            c.setFirstWeight(1.0-firstWeight);
            c.setSecondWeight(firstWeight);
            weightedPropertySimilarity = c.match(propertySimilarity, countProps);        
        }
        else {
            weightedPropertySimilarity = propertySimilarity;
        }
    }

    /**
     * @return the weightedPropertySimilarity
     */
    public SimilarityMatrix<TableColumn> getWeightedPropertySimilarity() {
        return weightedPropertySimilarity;
    }

    /**
     * @param weightedPropertySimilarity the weightedPropertySimilarity to set
     */
    public void setWeightedPropertySimilarity(SimilarityMatrix<TableColumn> weightedPropertySimilarity) {
        this.weightedPropertySimilarity = weightedPropertySimilarity;
    }

    /**
     * @return the allMatrices
     */
    public List<SimilarityMatrix> getAllMatrices() {
        return allMatrices;
    }

    /**
     * @param allMatrices the allMatrices to set
     */
    public void setAllMatrices(List<SimilarityMatrix> allMatrices) {
        this.allMatrices = allMatrices;
    }

}
