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
package de.dwslab.T2K.matching.evaluation;

/**
 * Calculates performances measures such as Precision and Recall for a result.
 * @author Oliver
 *
 */
public class EvaluationResult {

    private int correct;
    private int inputSetSize;
    private int referenceSetSize;
    private int totalPopulationSize;
    private boolean noGoldStandard;
    
    public int getCorrect() {
        return correct;
    }
    protected void setCorrect(int correct) {
        this.correct = correct;
    }
    public int getInputSetSize() {
        return inputSetSize;
    }
    protected void setInputSetSize(int inputSetSize) {
        this.inputSetSize = inputSetSize;
    }
    public int getReferenceSetSize() {
        return referenceSetSize;
    }
    protected void setReferenceSetSize(int referenceSetSize) {
        this.referenceSetSize = referenceSetSize;
    }
    
    public double getPrecision() {
        if(getInputSetSize() == 0) {
            // handle special cases
            // 1) input set size == 0 (no mappings) --> divide by zero
            // 2) reference set size == 0 (no mappings in GS) --> input set should also be 0!
            
            if(getReferenceSetSize()==0) {
                return 1.0;
            } else {
                return 0;
            }
        } else {
            return (double)getCorrect() / (double)getInputSetSize();
        }
    }
    public double getRecall() {
        // handle special cases
        // 1) reference set size == 0 (no mappings in GS/no GS) --> divide by zero
        
        if(getReferenceSetSize() == 0) {
            return 1.0;
        } else {
            return (double)getCorrect() / (double)getReferenceSetSize();
        }
    }
    public double getF1Score() {
        return getFxScore(1);
    }
    public double getFxScore(double x)
    {
        if(getPrecision()==0 || getRecall()==0) {
            return 0;
        }
        else {
            return (1 + x*x) * getPrecision() * getRecall() / ( x*x*getPrecision() + getRecall() );
        }
    }
    
    public double getAccuracy() {
        if(totalPopulationSize==0) {
            return 0.0;
        }
        return (double)(correct+(totalPopulationSize-referenceSetSize))/(double)totalPopulationSize;
    }
    
    /**
     * Assuming the set of mappings A and the set of reference mappings B the parameters are defined as
     * @param correct |A intersect B|
     * @param inputSetSize |A|
     * @param referenceSetSize |B|
     */
    public EvaluationResult(int correct, int inputSetSize, int referenceSetSize, int totalPopulationSize)
    {
        setCorrect(correct);
        setInputSetSize(inputSetSize);
        setReferenceSetSize(referenceSetSize);
        setTotalPopulationSize(totalPopulationSize);
        if(referenceSetSize == 0) {
            noGoldStandard = true;
        }
    }
    
    public void merge(EvaluationResult r) {
        correct += r.getCorrect();
        inputSetSize += r.getInputSetSize();
        referenceSetSize += r.getReferenceSetSize();
        totalPopulationSize += r.getTotalPopulationSize();
    }

    /**
     * @return the totalPopulationSize
     */
    public int getTotalPopulationSize() {
        return totalPopulationSize;
    }

    /**
     * @param totalPopulationSize the totalPopulationSize to set
     */
    public void setTotalPopulationSize(int totalPopulationSize) {
        this.totalPopulationSize = totalPopulationSize;
    }

    /**
     * @return the noGoldStandard
     */
    public boolean isNoGoldStandard() {
        return noGoldStandard;
    }
    
}
