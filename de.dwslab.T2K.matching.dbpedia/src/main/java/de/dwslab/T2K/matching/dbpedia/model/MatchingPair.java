/** Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package de.dwslab.T2K.matching.dbpedia.model;

import de.dwslab.T2K.utils.data.Pair;
import java.util.Comparator;

/**
 *
 * @author dritze
 */
public class MatchingPair<T> implements Comparator {

    private Pair<T, T> matchingPair;
    private double finalScore = 0;
    private double valueScore = 0;
    private double labelScore = 0;
    private double overallNumbTables = 0;
    private double otherPairsLabel = 0;
    private double otherPairsValue = 0;
    private double occurenceFirstLabel = 0;
    private double occurenceFirstValue = 0;
    private double occurencePairLabel = 0;
    private double occurencePairValue = 0;

    /**
     * @return the matchingPair
     */
    public Pair<T, T> getMatchingPair() {
        return matchingPair;
    }

    /**
     * @param matchingPair the matchingPair to set
     */
    public void setMatchingPair(Pair<T, T> matchingPair) {
        this.matchingPair = matchingPair;
    }

    /**
     * @return the finalScore
     */
    public double getFinalScore() {
        return finalScore;
    }

    /**
     * @param finalScore the finalScore to set
     */
    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    /**
     * @return the valueScore
     */
    public double getValueScore() {
        return valueScore;
    }

    /**
     * @param valueScore the valueScore to set
     */
    public void setValueScore(double valueScore) {
        this.valueScore = valueScore;
    }

    /**
     * @return the labelScore
     */
    public double getLabelScore() {
        return labelScore;
    }

    /**
     * @param labelScore the labelScore to set
     */
    public void setLabelScore(double labelScore) {
        this.labelScore = labelScore;
    }

    /**
     * @return the overallNumbTables
     */
    public double getOverallNumbTables() {
        return overallNumbTables;
    }

    /**
     * @param overallNumbTables the overallNumbTables to set
     */
    public void setOverallNumbTables(double overallNumbTables) {
        this.overallNumbTables = overallNumbTables;
    }

    /**
     * @return the otherPairs
     */
    public double getOtherPairs() {
        return getOtherPairsLabel();
    }

    /**
     * @param otherPairs the otherPairs to set
     */
    public void setOtherPairs(double otherPairs) {
        this.setOtherPairsLabel(otherPairs);
    }

    /**
     * @return the occurenceFirst
     */
    public double getOccurenceFirst() {
        return getOccurenceFirstLabel();
    }

    /**
     * @param occurenceFirst the occurenceFirst to set
     */
    public void setOccurenceFirst(double occurenceFirst) {
        this.setOccurenceFirstLabel(occurenceFirst);
    }

    /**
     * @return the occurencePair
     */
    public double getOccurencePair() {
        return getOccurencePairLabel();
    }

    /**
     * @param occurencePair the occurencePair to set
     */
    public void setOccurencePair(double occurencePair) {
        this.setOccurencePairLabel(occurencePair);
    }

    /**
     * @return the otherPairsLabel
     */
    public double getOtherPairsLabel() {
        return otherPairsLabel;
    }

    /**
     * @param otherPairsLabel the otherPairsLabel to set
     */
    public void setOtherPairsLabel(double otherPairsLabel) {
        this.otherPairsLabel = otherPairsLabel;
    }

    /**
     * @return the otherPairsValue
     */
    public double getOtherPairsValue() {
        return otherPairsValue;
    }

    /**
     * @param otherPairsValue the otherPairsValue to set
     */
    public void setOtherPairsValue(double otherPairsValue) {
        this.otherPairsValue = otherPairsValue;
    }

    /**
     * @return the occurenceFirstLabel
     */
    public double getOccurenceFirstLabel() {
        return occurenceFirstLabel;
    }

    /**
     * @param occurenceFirstLabel the occurenceFirstLabel to set
     */
    public void setOccurenceFirstLabel(double occurenceFirstLabel) {
        this.occurenceFirstLabel = occurenceFirstLabel;
    }

    /**
     * @return the occurenceFirstValue
     */
    public double getOccurenceFirstValue() {
        return occurenceFirstValue;
    }

    /**
     * @param occurenceFirstValue the occurenceFirstValue to set
     */
    public void setOccurenceFirstValue(double occurenceFirstValue) {
        this.occurenceFirstValue = occurenceFirstValue;
    }

    /**
     * @return the occurencePairLabel
     */
    public double getOccurencePairLabel() {
        return occurencePairLabel;
    }

    /**
     * @param occurencePairLabel the occurencePairLabel to set
     */
    public void setOccurencePairLabel(double occurencePairLabel) {
        this.occurencePairLabel = occurencePairLabel;
    }

    /**
     * @return the occurencePairValue
     */
    public double getOccurencePairValue() {
        return occurencePairValue;
    }

    /**
     * @param occurencePairValue the occurencePairValue to set
     */
    public void setOccurencePairValue(double occurencePairValue) {
        this.occurencePairValue = occurencePairValue;
    }

    @Override
    public int compare(Object o1, Object o2) {
        MatchingPair mp1 = (MatchingPair) o1;
        MatchingPair mp2 = (MatchingPair) o2;
        if (mp1.getFinalScore() > mp2.getFinalScore()) {
            return 1;
        }
        if (mp1.getFinalScore() < mp2.getFinalScore()) {
            return -1;
        }
        return 0;
    }
}
