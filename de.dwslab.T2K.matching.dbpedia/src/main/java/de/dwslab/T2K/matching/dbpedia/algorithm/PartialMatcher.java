package de.dwslab.T2K.matching.dbpedia.algorithm;

import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.model.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.Similarities;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.timer.Timer;
import java.util.Collection;

/**
 * super class for all partial matchers used by the algorithm
 * @author Oliver
 *
 */
public abstract class PartialMatcher<T> {

//    private Similarities similarities;
//    public Similarities getSimilarities() {
//        return similarities;
//    }
//    public void setSimilarities(Similarities similarities) {
//        this.similarities = similarities;
//    }
    
    private MatchingParameters matchingParameters;
    public MatchingParameters getMatchingParameters() {
        return matchingParameters;
    }
    public void setMatchingParameters(MatchingParameters matchingParameters) {
        this.matchingParameters = matchingParameters;
    }
    
    private GoldStandard goldStandard;
    public GoldStandard getGoldStandard() {
        return goldStandard;
    }
    public void setGoldStandard(GoldStandard goldStandard) {
        this.goldStandard = goldStandard;
    }
    
    private Timer rootTimer;
    public Timer getRootTimer() {
        return rootTimer;
    }
    public void setRootTimer(Timer rootTimer) {
        this.rootTimer = rootTimer;
    }
    
    private MatchingLogger logger;
    public MatchingLogger getLogger() {
        return logger;
    }
    public void setLogger(MatchingLogger logger) {
        this.logger = logger;
    }
    
    public PartialMatcher(Similarities similarities, MatchingParameters matchingParameters, Timer rootTimer, GoldStandard goldStandard, MatchingLogger logger) {
        //setSimilarities(similarities);
        setMatchingParameters(matchingParameters);
        setRootTimer(rootTimer);
        setGoldStandard(goldStandard);
        setLogger(logger);
    }
    
    public abstract SimilarityMatrix<T> match(MatchingData data);
}
