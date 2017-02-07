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
package de.dwslab.T2K.tableprocessor.model;

/**
 *
 * @author domi
 */
public class Statistic {
    
    private Comparable minimalValue;
    private Comparable maximalValue;
    private double standardDeviation;
    private double distinctValues;
    private double average;
    private double kurtosis;
    private double skewness;
    private double variance;
    private double averageValueLength;

    /**
     * @return the minimalValue
     */
    public Comparable getMinimalValue() {
        return minimalValue;
    }

    /**
     * @param minimalValue the minimalValue to set
     */
    public void setMinimalValue(Comparable minimalValue) {
        this.minimalValue = minimalValue;
    }

    /**
     * @return the maximalValue
     */
    public Comparable getMaximalValue() {
        return maximalValue;
    }

    /**
     * @param maximalValue the maximalValue to set
     */
    public void setMaximalValue(Comparable maximalValue) {
        this.maximalValue = maximalValue;
    }

    /**
     * @return the standardDeviation
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * @param standardDeviation the standardDeviation to set
     */
    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    /**
     * @return the distinctValues
     */
    public double getDistinctValues() {
        return distinctValues;
    }

    /**
     * @param distinctValues the distinctValues to set
     */
    public void setDistinctValues(double distinctValues) {
        this.distinctValues = distinctValues;
    }

    /**
     * @return the average
     */
    public double getAverage() {
        return average;
    }

    /**
     * @param average the average to set
     */
    public void setAverage(double average) {
        this.average = average;
    }

    /**
     * @return the kurtosis
     */
    public double getKurtosis() {
        return kurtosis;
    }

    /**
     * @param kurtosis the kurtosis to set
     */
    public void setKurtosis(double kurtosis) {
        this.kurtosis = kurtosis;
    }

    /**
     * @return the skewness
     */
    public double getSkewness() {
        return skewness;
    }

    /**
     * @param skewness the skewness to set
     */
    public void setSkewness(double skewness) {
        this.skewness = skewness;
    }

    /**
     * @return the variance
     */
    public double getVariance() {
        return variance;
    }

    /**
     * @param variance the variance to set
     */
    public void setVariance(double variance) {
        this.variance = variance;
    }

    /**
     * @return the averageValueLength
     */
    public double getAverageValueLength() {
        return averageValueLength;
    }

    /**
     * @param averageValueLength the averageValueLength to set
     */
    public void setAverageValueLength(double averageValueLength) {
        this.averageValueLength = averageValueLength;
    }
    
    
    
}
