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

import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateAbstractMatcher;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableRowUriMatchingAdapter;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author domi
 */
public class CandidateAbstractSelectionComponent extends WebtableToDBpediaMatchingComponent {

    private SimilarityMatrix<TableRow> initialCandidateAbstracrSimilarity;
    
    public SimilarityMatrix<TableRow> getCandidateAbstractSimilarity() {
        return initialCandidateAbstracrSimilarity;
    }
    
    
    
    public static final Parameter PAR_ABSTRACT_THRESHOLD = new Parameter("CandidateAbstract.Threshold", 0.8);

    protected static final List<Parameter> params;
    public static List<Parameter> getParams() {
        return params;
    }
    
    static {
        ArrayList<Parameter> l = new ArrayList<>();
        l.add(PAR_ABSTRACT_THRESHOLD);
        params = l;
    }
        
    public CandidateAbstractSelectionComponent() {
        setParameters(params);
    }
        
    @SuppressWarnings("unchecked")
    protected void initialiseParameters(Configuration config) {
                
        CandidateAbstractMatcher candref = getMatchers().getCandidateAbstractMatcher();
        candref.setThreshold((double)config.getValue(PAR_ABSTRACT_THRESHOLD));
    }
    
    @Override
    public void run(Configuration config) {
        
        try {
            initialiseParameters(config);
            initialCandidateAbstracrSimilarity = getMatchers().getCandidateAbstractMatcher().match(getData());     
            //initialCandidateAbstracrSimilarity.makeColumnStochastic();
            if (getMatchingParameters().isCollectMatchingInfo()) {
                System.out.println(initialCandidateAbstracrSimilarity.getOutput(null, getGoldStandard().getInstanceGoldStandard().values(), new TableRowUriMatchingAdapter(), null));
            //System.out.println(initialCandidateSimilarity.listPairs());
            }            
            
            
        } catch (Exception ex) {
            Logger.getLogger(CandidateAbstractSelectionComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
