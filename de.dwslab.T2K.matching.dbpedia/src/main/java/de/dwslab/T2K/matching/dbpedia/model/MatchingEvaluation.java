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
package de.dwslab.T2K.matching.dbpedia.model;

import de.dwslab.T2K.matching.evaluation.EvaluationResult;


public class MatchingEvaluation {

    private EvaluationResult instanceBaseLine;
    private EvaluationResult instanceResult;
    private EvaluationResult instanceMax;
    private EvaluationResult propertyResult;
    private EvaluationResult classResult;
    private EvaluationResult propertyRangeResult;
    private int numCandidates;
    private int maxCorrectCandidates;
    private int correctKey;

    public int getMaxCorrectCandidates() {
        return maxCorrectCandidates;
    }

    public void setMaxCorrectCandidates(int maxCorrectCandidates) {
        this.maxCorrectCandidates = maxCorrectCandidates;
    }

    public double getMaxRecall() {
        return (double) maxCorrectCandidates / (double) instanceBaseLine.getReferenceSetSize();
    }

    public EvaluationResult getInstanceBaseLine() {
        return instanceBaseLine;
    }

    public void setInstanceBaseLine(EvaluationResult instanceBaseLine) {
        this.instanceBaseLine = instanceBaseLine;
    }

    public EvaluationResult getInstanceResult() {
        return instanceResult;
    }

    public void setInstanceResult(EvaluationResult instanceResult) {
        this.instanceResult = instanceResult;
    }

    public EvaluationResult getInstanceMax() {
        return instanceMax;
    }

    public void setInstanceMax(EvaluationResult instanceMax) {
        this.instanceMax = instanceMax;
    }

    public EvaluationResult getPropertyResult() {
        return propertyResult;
    }

    public void setPropertyResult(EvaluationResult propertyResult) {
        this.propertyResult = propertyResult;
    }

    public EvaluationResult getClassResult() {
        return classResult;
    }

    public void setClassResult(EvaluationResult classResult) {
        this.classResult = classResult;
    }

    public int getNumCandidates() {
        return numCandidates;
    }

    public void setNumCandidates(int numCandidates) {
        this.numCandidates = numCandidates;
    }

    public MatchingEvaluation(EvaluationResult instanceBaseline, int maxCorrect, EvaluationResult instance, EvaluationResult property, EvaluationResult propertyRange, EvaluationResult cls) {
        this.instanceBaseLine = instanceBaseline;
        instanceResult = instance;
        propertyResult = property;
        propertyRangeResult = propertyRange;
        classResult = cls;
        this.maxCorrectCandidates = maxCorrect;
    }

    public void merge(MatchingEvaluation e) {
        numCandidates += e.getNumCandidates();
        if (e.getInstanceBaseLine() != null) {
            if (instanceBaseLine == null || instanceBaseLine.isNoGoldStandard()) {
                instanceBaseLine = e.getInstanceBaseLine();
            } else {
                instanceBaseLine.merge(e.getInstanceBaseLine());
            }
        }
        if (e.getInstanceResult() != null) {
            if (instanceResult == null || instanceResult.isNoGoldStandard()) {
                instanceResult = e.getInstanceResult();
            } else {
                instanceResult.merge(e.getInstanceResult());
            }
        }
        if (e.getPropertyResult() != null) {
            if (propertyResult == null || propertyResult.isNoGoldStandard()) {
                propertyResult = e.getPropertyResult();
            } else {
                propertyResult.merge(e.getPropertyResult());
            }
        }
        if (e.getPropertyRangeResult() != null) {
            if (propertyRangeResult == null || propertyRangeResult.isNoGoldStandard()) {
                propertyRangeResult = e.getPropertyRangeResult();
            } else {
                propertyRangeResult.merge(e.getPropertyRangeResult());
            }
        }
        if (e.getClassResult() != null) {
            if (classResult == null || classResult.isNoGoldStandard()) {
                classResult = e.getClassResult();
            } else {
                classResult.merge(e.getClassResult());
            }
        }
        maxCorrectCandidates += e.getMaxCorrectCandidates();
    }

    /**
     * @return the propertyRangeResult
     */
    public EvaluationResult getPropertyRangeResult() {
        return propertyRangeResult;
    }

    /**
     * @param propertyRangeResult the propertyRangeResult to set
     */
    public void setPropertyRangeResult(EvaluationResult propertyRangeResult) {
        this.propertyRangeResult = propertyRangeResult;
    }

    /**
     * @return the correctKey
     */
    public int getCorrectKey() {
        return correctKey;
    }

    /**
     * @param correctKey the correctKey to set
     */
    public void setCorrectKey(int correctKey) {
        this.correctKey = correctKey;
    }
}
