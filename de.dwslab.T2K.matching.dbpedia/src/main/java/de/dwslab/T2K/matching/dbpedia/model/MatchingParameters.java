package de.dwslab.T2K.matching.dbpedia.model;

import de.dwslab.T2K.matching.dbpedia.algorithm.WebtableToDBpediaMatchingProcess;
import de.dwslab.T2K.matching.firstline.FastJoinMatcher.FastJoinMeasure;

/**
 * contains all parameters of the matching algorithm
 * @author Oliver
 *
 */
public class MatchingParameters {

    private boolean useUnitDetection = true;

    public void setUseUnitDetection(boolean useUnitDetection) {
        this.useUnitDetection = useUnitDetection;
    }

    public boolean isUseUnitDetection() {
        return useUnitDetection;
    }

    private boolean collectMatchingInfo = false;

    public boolean isCollectMatchingInfo() {
        return collectMatchingInfo;
    }

    public void setCollectMatchingInfo(boolean collectMatchingInfo) {
        this.collectMatchingInfo = collectMatchingInfo;
    }

    private boolean forceInstanceOneToOneMapping = true;

    public boolean isForceInstanceOneToOneMapping() {
        return forceInstanceOneToOneMapping;
    }

    public void setForceInstanceOneToOneMapping(
            boolean forceInstanceOneToOneMapping) {
        this.forceInstanceOneToOneMapping = forceInstanceOneToOneMapping;
    }

    private boolean forcePropertyOneToOneMapping = true;

    public boolean isForcePropertyOneToOneMapping() {
        return forcePropertyOneToOneMapping;
    }

    public void setForcePropertyOneToOneMapping(
            boolean forcePropertyOneToOneMapping) {
        this.forcePropertyOneToOneMapping = forcePropertyOneToOneMapping;
    }

    private boolean forceClassOneToOneMapping = true;

    public boolean isForceClassOneToOneMapping() {
        return forceClassOneToOneMapping;
    }

    public void setForceClassOneToOneMapping(boolean forceClassOneToOneMapping) {
        this.forceClassOneToOneMapping = forceClassOneToOneMapping;
    }
    
    private int maxSpanningCells = 1;

    private double fastJoinDelta = 0.1;
    private double fastJoinTau = 0.1;
    private FastJoinMeasure fastJoinMeasure = FastJoinMeasure.FJACCARD;
    public double getFastJoinDelta() {
        return fastJoinDelta;
    }
    public void setFastJoinDelta(double fastJoinDelta) {
        this.fastJoinDelta = fastJoinDelta;
    }
    public double getFastJoinTau() {
        return fastJoinTau;
    }
    public void setFastJoinTau(double fastJoinTau) {
        this.fastJoinTau = fastJoinTau;
    }
    public FastJoinMeasure getFastJoinMeasure() {
        return fastJoinMeasure;
    }
    public void setFastJoinMeasure(FastJoinMeasure fastJoinMeasure) {
        this.fastJoinMeasure = fastJoinMeasure;
    }
    
    private String fastJoinPath;

    public String getFastJoinPath() {
        return fastJoinPath;
    }

    public void setFastJoinPath(String fastJoinPath) {
        this.fastJoinPath = fastJoinPath;
    }
    

    private boolean runParallel = true;

    public void setRunParallel(boolean runParallel) {
        this.runParallel = runParallel;
    }

    public boolean isRunParallel() {
        return runParallel;
    }
    
    
    private Double instanceScoreThreshold = 0.0;
    public Double getInstanceScoreThreshold() {
        return instanceScoreThreshold;
    }
    public void setInstanceScoreThreshold(Double instanceScoreThreshold) {
        this.instanceScoreThreshold = instanceScoreThreshold;
    }
    
    private WebtableToDBpediaMatchingProcess.tableType tableType;
    
    public WebtableToDBpediaMatchingProcess.tableType getTableType() {
        return this.tableType;
    }
    
    public void setTableType(WebtableToDBpediaMatchingProcess.tableType tableType) {
        this.tableType = tableType;
    } 
        
    // easier than writing getter/setter for every single attribute ...
    public final static int PROPERTY_TOP_K_CANDIDATES = 1;
    public final static int PROPERTY_FINAL_PRUNING = 2;
    public final static int PROPERTY_VALUE_PRUNING = 4;
    public final static int PROPERTY_TOP_K_VOTE = 8;
    public final static int PROPERTY_SELECT_TOP_K = 16;
    public final static int PROPERTY_CANDIDATE_PRUNING = 32;
    private int propertyMatchingFlags = PROPERTY_FINAL_PRUNING | PROPERTY_TOP_K_CANDIDATES | PROPERTY_SELECT_TOP_K;
    public int getPropertyMatchingFlags() {
        return propertyMatchingFlags;
    }
    public void setPropertyMatchingFlags(int propertyMatchingFlags) {
        this.propertyMatchingFlags = propertyMatchingFlags;
    }
    
    private double propertyValueThreshold = 0.0;
    public double getPropertyValueThreshold() {
        return propertyValueThreshold;
    }
    public void setPropertyValueThreshold(double propertyValueThreshold) {
        this.propertyValueThreshold = propertyValueThreshold;
    }
    private double propertyFinalThreshold = 0.05;
    public double getPropertyFinalThreshold() {
        return propertyFinalThreshold;
    }
    public void setPropertyFinalThreshold(double propertyFinalThreshold) {
        this.propertyFinalThreshold = propertyFinalThreshold;
    }
    private double propertyCandidateThreshold = 0.0;
    public double getPropertyCandidateThreshold() {
        return propertyCandidateThreshold;
    }
    public void setPropertyCandidateThreshold(double propertyCandidateThreshold) {
        this.propertyCandidateThreshold = propertyCandidateThreshold;
    }

    /**
     * @return the maxSpanningCells
     */
    public int getMaxSpanningCells() {
        return maxSpanningCells;
    }

    /**
     * @param maxSpanningCells the maxSpanningCells to set
     */
    public void setMaxSpanningCells(int maxSpanningCells) {
        this.maxSpanningCells = maxSpanningCells;
    }
    

}
