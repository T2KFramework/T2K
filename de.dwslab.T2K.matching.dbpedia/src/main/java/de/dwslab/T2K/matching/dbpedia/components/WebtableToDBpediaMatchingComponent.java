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
package de.dwslab.T2K.matching.dbpedia.components;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.correspondences.CorrespondenceGenerator;
import de.dwslab.T2K.matching.dbpedia.logging.MatchingLogger;
import de.dwslab.T2K.matching.dbpedia.matchers.Matchers;
import de.dwslab.T2K.matching.dbpedia.model.MatchingResult;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.settings.EvaluationParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.GoldStandard;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingData;
import de.dwslab.T2K.matching.dbpedia.model.settings.MatchingParameters;
import de.dwslab.T2K.matching.dbpedia.model.settings.Similarities;
import de.dwslab.T2K.matching.process.MatchingComponent;
import de.dwslab.T2K.matching.secondline.BestChoiceMatching;
import de.dwslab.T2K.matching.secondline.ConflictResolution;
import de.dwslab.T2K.matching.secondline.OneToOneConstraint;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.utils.timer.Timer;

public abstract class WebtableToDBpediaMatchingComponent extends MatchingComponent {

    private MatchingParameters matchingParameters;
    public MatchingParameters getMatchingParameters() {
        return matchingParameters;
    }
    public void setMatchingParameters(MatchingParameters matchingParameters) {
        this.matchingParameters = matchingParameters;
    }
    
    private Similarities similarities;
    public Similarities getSimilarities() {
        return similarities;
    }
    protected void setSimilarities(Similarities similarities) {
        this.similarities = similarities;
    }
    
    private Matchers matchers;
    public Matchers getMatchers() {
        return matchers;
    }
    public void setMatchers(Matchers matchers) {
        this.matchers = matchers;
    }
    
    private EvaluationParameters evaluationParameters;
    public EvaluationParameters getEvaluationParameters() {
        return evaluationParameters;
    }
    public void setEvaluationParameters(
            EvaluationParameters evaluationParameters) {
        this.evaluationParameters = evaluationParameters;
    }
    
    private GoldStandard goldStandard;
    public GoldStandard getGoldStandard() {
        return goldStandard;
    }
    public void setGoldStandard(GoldStandard goldStandard) {
        this.goldStandard = goldStandard;
    }
    
    private MatchingData data;
    public MatchingData getData() {
        return data;
    }
    public void setData(MatchingData data) {
        this.data = data;
    }
    
    private Timer rootTimer = null;
    protected Timer getRootTimer() {
        return rootTimer;
    }
    protected void setRootTimer(Timer rootTimer) {
        this.rootTimer = rootTimer;
    }
    
    private MatchingLogger logger;
    public MatchingLogger getLogger() {
        return logger;
    }
    public void setLogger(MatchingLogger logger) {
        this.logger = logger;
    }
    
    private String webTableName;
    public String getWebTableName() {
        return webTableName;
    }
    public void setWebTableName(String webTableName) {
        this.webTableName = webTableName;
    }
    
    public void initialise(Matchers m, MatchingData data, EvaluationParameters eval, GoldStandard gold, MatchingLogger log, MatchingParameters param, Similarities sim, Timer rootTimer) {
        setMatchers(m);
        setData(data);
        setEvaluationParameters(eval);
        setGoldStandard(gold);
        setLogger(log);
        setMatchingParameters(param);
        setSimilarities(sim);
        setRootTimer(rootTimer);
    }
    
    protected void mapInstances(SimilarityMatrix<TableRow> sim, boolean verbose, MatchingResult result, String name) {
        SimilarityMatrix<TableRow> candidates = null;

        if (getMatchingParameters().isForceInstanceOneToOneMapping()) {
            OneToOneConstraint constraintMatcher = new OneToOneConstraint(
                    ConflictResolution.Maximum);
            //candidates = constraintMatcher.match(getSimilarities().getCandidateSimilarity());
            candidates = constraintMatcher.match(sim);
        } else {
            BestChoiceMatching constraintMatcher = new BestChoiceMatching();
            constraintMatcher
                    .setForceOneToOneMapping(getMatchingParameters().isForceInstanceOneToOneMapping());
            //candidates = constraintMatcher.match(getSimilarities().getCandidateSimilarity());
            candidates = constraintMatcher.match(sim);
        }

        Collection<Correspondence<TableRow>> allCandidates;

        CorrespondenceGenerator generator = new CorrespondenceGenerator();

        allCandidates = generator.generateCorrespondences(candidates, 0.0);
        
        if (result != null) {
            result.setInstanceMappings(allCandidates);
            if(getGoldStandard()!=null) {
                result.evaluateInstances(verbose, getGoldStandard(), getData().getWebtable(), name);
            }
        }

        if(getMatchingParameters().isCollectMatchingInfo()) {
            System.out.println(allCandidates.size() + " instance correspondences");
        }
    }

    protected void mapProperties(SimilarityMatrix<TableColumn> sim,
            String webtableName,
            boolean verbose, MatchingResult result) {
        SimilarityMatrix<TableColumn> properties = null;

        if (getMatchingParameters().isForcePropertyOneToOneMapping()) {
            OneToOneConstraint constraintMatcher = new OneToOneConstraint(
                    ConflictResolution.Maximum);
            //properties = constraintMatcher.match(getSimilarities().getPropertySimilarity());
            properties = constraintMatcher.match(sim);
        } else {
            BestChoiceMatching constraintMatcher = new BestChoiceMatching();
            constraintMatcher
                    .setForceOneToOneMapping(getMatchingParameters().isForcePropertyOneToOneMapping());
            //properties = constraintMatcher.match(getSimilarities().getPropertySimilarity());
            properties = constraintMatcher.match(sim);
        }

        if (getMatchingParameters().isCollectMatchingInfo()) {
            //System.out.println("Property mapping");
            //System.out.println(properties.getOutput());
        }

        Collection<Correspondence<TableColumn>> allProps;

        CorrespondenceGenerator generator = new CorrespondenceGenerator();

        allProps = generator.generateCorrespondences(properties, 0.0);
        
        // remove key mapping
        List<Correspondence<TableColumn>> rem = new LinkedList<Correspondence<TableColumn>>();
        for(Correspondence<TableColumn> c : allProps) {
            if(c.getFirst().isKey()) {
                rem.add(c);
            }
        }
        for(Correspondence<TableColumn> c : rem) {
            allProps.remove(c);
        }

        if (result != null) {
            result.setPropertyMappings(allProps);       
            if(getGoldStandard()!=null) {
                result.evaluateProperties(verbose, getGoldStandard(), getData().getWebtable());
                result.evaluatePropertyRanges(verbose, getGoldStandard(), getData().getWebtable());         
            }
        }

        System.out.println(allProps.size() + " property correspondences");
    }
    
    protected void mapClasses(SimilarityMatrix<Table> sim, MatchingResult result, boolean verbose) {
        SimilarityMatrix<Table> classes = null;

        if (getMatchingParameters().isForceClassOneToOneMapping()) {
            OneToOneConstraint constraintMatcher = new OneToOneConstraint(
                    ConflictResolution.Maximum);
            //classes = constraintMatcher.match(getSimilarities().getClassSimilarity());
            classes = constraintMatcher.match(sim);
        } else {
            BestChoiceMatching constraintMatcher = new BestChoiceMatching();
            constraintMatcher
                    .setForceOneToOneMapping(getMatchingParameters().isForceClassOneToOneMapping());

            //classes = constraintMatcher.match(getSimilarities().getClassSimilarity());
            classes = constraintMatcher.match(sim);
        }
        
        Collection<Correspondence<Table>> allClasses;

        CorrespondenceGenerator generator = new CorrespondenceGenerator();

        allClasses = generator.generateCorrespondences(classes, 0.0);

        if (result != null) {
            result.setClassMappings(allClasses);
            result.evaluateClass(getEvaluationParameters(), getData().getWebtable(), verbose);
        }
    }
}
