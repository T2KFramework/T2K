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
import de.dwslab.T2K.matching.dbpedia.model.settings.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.Similarities;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.utils.timer.Timer;

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
