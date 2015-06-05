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
package de.dwslab.T2K.matching.dbpedia.matchers;

import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateRefinementMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateSelectionMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.PruningInstanceMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.ValueBasedInstanceMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.DuplicateBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.LabelBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.PropertyBasedClassMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.SecondLineClassMatcher;
import de.dwslab.T2K.matching.dbpedia.model.settings.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.Similarities;
import de.dwslab.T2K.utils.timer.Timer;

/**
 * Contains all partial matchers that are used by the algorithm
 * @author Oliver
 *
 */
public class Matchers {

    private ValueBasedInstanceMatcher valueBasedInstanceMatcher;
    public ValueBasedInstanceMatcher getValueBasedInstanceMatcher() {
        return valueBasedInstanceMatcher;
    }
    public void setValueBasedInstanceMatcher(
            ValueBasedInstanceMatcher valueBasedInstanceMatcher) {
        this.valueBasedInstanceMatcher = valueBasedInstanceMatcher;
    }
    
    private LabelBasedSchemaMatcher labelBasedSchemaMatcher;
    public LabelBasedSchemaMatcher getLabelBasedSchemaMatcher() {
        return labelBasedSchemaMatcher;
    }
    public void setLabelBasedSchemaMatcher(
            LabelBasedSchemaMatcher labelBasedSchemaMatcher) {
        this.labelBasedSchemaMatcher = labelBasedSchemaMatcher;
    }
    
    private CandidateSelectionMatcher candidateSelectionMatcher;
    public CandidateSelectionMatcher getCandidateSelectionMatcher() {
        return candidateSelectionMatcher;
    }
    public void setCandidateSelectionMatcher(
            CandidateSelectionMatcher candidateSelectionMatcher) {
        this.candidateSelectionMatcher = candidateSelectionMatcher;
    }
    
    private CandidateRefinementMatcher candidateRefinementMatcher;
    public CandidateRefinementMatcher getCandidateRefinementMatcher() {
        return candidateRefinementMatcher;
    }
    public void setCandidateRefinementMatcher(
            CandidateRefinementMatcher candidateRefinementMatcher) {
        this.candidateRefinementMatcher = candidateRefinementMatcher;
    }
    
    private DuplicateBasedSchemaMatcher propertyMatcher;
    public DuplicateBasedSchemaMatcher getPropertyMatcher() {
        return propertyMatcher;
    }
    public void setPropertyMatcher(DuplicateBasedSchemaMatcher propertyMatcher) {
        this.propertyMatcher = propertyMatcher;
    }
    
    private PruningInstanceMatcher instanceMatcher;
    public PruningInstanceMatcher getInstanceMatcher() {
        return instanceMatcher;
    }
    public void setInstanceMatcher(PruningInstanceMatcher instanceMatcher) {
        this.instanceMatcher = instanceMatcher;
    }
    
    private SecondLineClassMatcher classMatcher;
    public SecondLineClassMatcher getClassMatcher() {
        return classMatcher;
    }
    public void setClassMatcher(SecondLineClassMatcher classMatcher) {
        this.classMatcher = classMatcher;
    }
    
    private PropertyBasedClassMatcher propertyBasedClassMatcher;
    public PropertyBasedClassMatcher getPropertyBasedClassMatcher() {
        return propertyBasedClassMatcher;
    }
    public void setPropertyBasedClassMatcher(
            PropertyBasedClassMatcher propertyBasedClassMatcher) {
        this.propertyBasedClassMatcher = propertyBasedClassMatcher;
    }
    
    public Matchers(Similarities sim, MatchingParameters par, Timer t, GoldStandard g, MatchingLogger logger) {
        valueBasedInstanceMatcher = new ValueBasedInstanceMatcher(sim, par, t, g, logger);
        labelBasedSchemaMatcher = new LabelBasedSchemaMatcher(sim, par, t, g, logger);
        candidateSelectionMatcher = new CandidateSelectionMatcher(sim, par, t, g, logger);
        candidateRefinementMatcher = new CandidateRefinementMatcher(sim, par, t, g, logger);
        propertyMatcher = new DuplicateBasedSchemaMatcher(sim, par, t, g, logger);
        instanceMatcher = new PruningInstanceMatcher(sim, par, t, g, logger);
        classMatcher = new SecondLineClassMatcher(sim, par, t, g, logger);
        propertyBasedClassMatcher = new PropertyBasedClassMatcher(sim, par, t, g, logger);
    }
}
