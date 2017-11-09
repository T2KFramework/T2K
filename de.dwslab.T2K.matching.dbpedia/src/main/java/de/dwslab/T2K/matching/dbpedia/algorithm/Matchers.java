package de.dwslab.T2K.matching.dbpedia.algorithm;

import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateAbstractMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateRefinementMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.CandidateSelectionMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.LinkBasedMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.NEMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.PruningInstanceMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.instance.ValueBasedInstanceMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.ContextBasedClassMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.DuplicateBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.LabelBasedSchemaMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.PropertyBasedClassMatcher;
import de.dwslab.T2K.matching.dbpedia.matchers.schema.SecondLineClassMatcher;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
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
    
    private CandidateAbstractMatcher candidateAbstractSelectionMatcher;
    public CandidateAbstractMatcher getCandidateAbstractMatcher() {
        return candidateAbstractSelectionMatcher;
    }
    public void setCandidateAbstractMatcher(
            CandidateAbstractMatcher candidateAbstractMatcher) {
        this.candidateAbstractSelectionMatcher = candidateAbstractMatcher;
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
    
    private LinkBasedMatcher linkBasedMatcher;
    
    private ContextBasedClassMatcher contextMatcher;
    
    private NEMatcher NEMatcher;
    
    
    public Matchers(Similarities sim, MatchingParameters par, Timer t, GoldStandard g, MatchingLogger logger) {
        valueBasedInstanceMatcher = new ValueBasedInstanceMatcher(sim, par, t, g, logger);
        labelBasedSchemaMatcher = new LabelBasedSchemaMatcher(sim, par, t, g, logger);
        candidateSelectionMatcher = new CandidateSelectionMatcher(sim, par, t, g, logger);
        candidateAbstractSelectionMatcher = new CandidateAbstractMatcher(sim, par, t, g, logger);
        candidateRefinementMatcher = new CandidateRefinementMatcher(sim, par, t, g, logger);
        propertyMatcher = new DuplicateBasedSchemaMatcher(sim, par, t, g, logger);
        instanceMatcher = new PruningInstanceMatcher(sim, par, t, g, logger);
        classMatcher = new SecondLineClassMatcher(sim, par, t, g, logger);
        propertyBasedClassMatcher = new PropertyBasedClassMatcher(sim, par, t, g, logger);
        linkBasedMatcher = new LinkBasedMatcher(sim, par, t, g, logger);
        contextMatcher = new ContextBasedClassMatcher(sim, par, t, g, logger);
        NEMatcher = new NEMatcher(sim, par, t, g, logger);
    }

    /**
     * @return the linkBasedMatcher
     */
    public LinkBasedMatcher getLinkBasedMatcher() {
        return linkBasedMatcher;
    }

    /**
     * @param linkBasedMatcher the linkBasedMatcher to set
     */
    public void setLinkBasedMatcher(LinkBasedMatcher linkBasedMatcher) {
        this.linkBasedMatcher = linkBasedMatcher;
    }

    /**
     * @return the contextMatcher
     */
    public ContextBasedClassMatcher getContextMatcher() {
        return contextMatcher;
    }

    /**
     * @param contextMatcher the contextMatcher to set
     */
    public void setContextMatcher(ContextBasedClassMatcher contextMatcher) {
        this.contextMatcher = contextMatcher;
    }

    /**
     * @return the NEMatcher
     */
    public NEMatcher getNEMatcher() {
        return NEMatcher;
    }

    /**
     * @param NEMatcher the NEMatcher to set
     */
    public void setNEMatcher(NEMatcher NEMatcher) {
        this.NEMatcher = NEMatcher;
    }
}
