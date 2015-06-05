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

import de.dwslab.T2K.matching.blocking.IdentityBlocking;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.PartialMatcher;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnUriMatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.settings.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.Similarities;
import de.dwslab.T2K.matching.dbpedia.similarity.AlwaysMatchSimilarityFunction;
import de.dwslab.T2K.matching.firstline.LabelBasedMatcher;
import de.dwslab.T2K.similarity.functions.SimilarityFunction;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.similarity.measures.StringSimilarityMeasure;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.timer.Timer;

/**
 * Label-based Schema Matcher for properties
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

    /**
     * @param labelSimilarity the labelSimilarity to set
     */
    public void setLabelSimilarity(SimilarityFunction<String> labelSimilarity) {
        this.labelSimilarity = labelSimilarity;
    }
    
    public SimilarityMatrix<TableColumn> match(MatchingData data) {
        
        Timer t = Timer.getNamed("Label matching", getRootTimer());
        
        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.print("label matching ... ");
        }
        
        LabelBasedMatcher<TableColumn> labelMatching = new LabelBasedMatcher<TableColumn>();
        
        labelMatching.setBlocking(new IdentityBlocking<TableColumn>());

        // calculate scores based on column/label string similarity
        StringSimilarityMeasure<TableColumn> colMeasure = new StringSimilarityMeasure<>();
        colMeasure.setSimilarityFunction(getLabelSimilarity());
        labelMatching.setSimilarityMeasure(colMeasure);


        labelMatching.setCollectMatchingInfo(getMatchingParameters().isCollectMatchingInfo());
        labelMatching.setRunInParallel(false);
        
        // run matching
        Timer tm = Timer.getNamed("LabelBasedMatcher: instance labels", t);
         SimilarityMatrix<TableColumn> labelSimilarity = labelMatching.match(data.getWebtable().getColumns(),
         data.getDbpediaColSet(), new TableColumnMatchingAdapter());
         tm.stop();
         
         // set the matching score of all key columns to 1
         makeKeysMatch(labelSimilarity, data);
         
         // set the similarity of all unmatched pairs to 0 
        for(TableColumn c : labelSimilarity.getFirstDimension()) {
            for(TableColumn c2 : labelSimilarity.getSecondDimension()) {
                if(labelSimilarity.get(c, c2)==null) {
                    labelSimilarity.set(c, c2, 0.0);
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
        for(Table matchingTable : getClassSimilarity().getSecondDimension()) {
            sim.set(wtKey, matchingTable.getKey(), 1.0);
        }
    }


}
