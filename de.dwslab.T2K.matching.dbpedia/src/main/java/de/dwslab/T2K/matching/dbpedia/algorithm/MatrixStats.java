/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.dbpedia.algorithm;

import de.dwslab.T2K.matching.dbpedia.model.MatchingData;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.moment.Variance;

/**
 *
 * @author domi
 */
public class MatrixStats {

    private double mean;
    private double stad;
    private double vari;
    private double density;
    private double meanWdens;
    private double stadWdens;
    private double variWdens;
    private List possibleMatches;
    private Map<Object, List> possibleMatchesPerInstance;
    private MatchingData data;
    private double herfindahlIndex;
    private double normalizedHerinfahl;
    private double LC;
    private double lineBasedAverage;
    
    
    public MatrixStats(SimilarityMatrix m, MatchingData data) {
        this.data = data;
        compute(m);
    }

    public MatrixStats(SimilarityMatrix m, MatchingData data, List possibleMatchesByCol) {
        this.data = data;
        this.possibleMatches = possibleMatchesByCol;
        compute(m);
    }

    public MatrixStats(SimilarityMatrix m, MatchingData data, Map possibleMatchesByCol) {
        this.data = data;
        this.possibleMatchesPerInstance = possibleMatchesByCol;
        compute(m);
    }

    private void compute(SimilarityMatrix sm) {
        List<Double> values = new ArrayList<>();
        double herfindahlOverall = 0.0, averageOverall = 0.0, LC1 = 0.0;
        //used for classes where we only have a vector and not a whole matrix
        if (possibleMatches != null) {
            System.out.println("poss matches: " + possibleMatches.size());
            for (Object first : sm.getFirstDimension()) {
                for (Object second : possibleMatches) {
                    if (sm.get(first, second) == null) {
                        values.add(0.0);
                    } else {
                        values.add(sm.get(first, second));
                    }
                }

            }
            //used for instances and properties
        } else if (possibleMatchesPerInstance != null) {
            
            System.out.println("table: " + data.getWebtable());
            //System.out.println("number of keys: " + possibleMatchesPerInstance.size());
            for (Object first : possibleMatchesPerInstance.keySet()) {
                List<Double> valuesPerInstance = new ArrayList<>();
                //System.out.println("key: " + first + " cands: " + possibleMatchesPerInstance.get(first).size());
                for (Object second : possibleMatchesPerInstance.get(first)) {
                    if (sm.get(first, second) == null) {
                        valuesPerInstance.add(0.0);
                    } else {
                        valuesPerInstance.add(sm.get(first, second));
                    }
                }
                double sum = 0, quadraticSum = 0;
                for (double value : valuesPerInstance) {
                    sum += value;
                    quadraticSum += Math.pow(value, 2);
                }
                sum = Math.pow(sum, 2);
                //System.out.println("sum: " + sum + " quadratic: " + quadraticSum);
                if(sum > 0) {
                    herfindahlOverall += quadraticSum / sum;
                    averageOverall += sum/valuesPerInstance.size();
                    LC1 += Collections.max(valuesPerInstance)-(sum/valuesPerInstance.size());
                }
                values.addAll(valuesPerInstance);
            }
            //herfindahlOverall/possibleMatchesPerInstance.size() ???
            setHerfindahlIndex(herfindahlOverall);
            setNormalizedHerinfahl(getHerfindahlIndex()/(double)possibleMatchesPerInstance.keySet().size());            
            setLineBasedAverage(averageOverall/(double)sm.getFirstDimension().size());
            setLC(LC1/(double)sm.getFirstDimension().size());
        } else {
            for (Object first : sm.getFirstDimension()) {
                for (Object second : sm.getMatches(first)) {
                    values.add(sm.get(first, second));
                }
            }
        }
        double[] d = new double[values.size()];
        for (int x = 0; x <= values.size() - 1; x++) {
            d[x] = values.get(x);
        }
        System.out.println("value size: " + values.size());
        setDensity((double) values.size() / (double) (sm.getSecondDimension().size()));

        StandardDeviation sd = new StandardDeviation();
        setStad(sd.evaluate(d));

        Mean m = new Mean();
        setMean(m.evaluate(d));

        Variance v = new Variance();
        setVari(v.evaluate(d));

        setMeanWdens(getMean() / density);
        setStadWdens(getStad() / density);
        setVariWdens(getVari() / density);

        if (possibleMatches != null) {
            double sum = 0, quadraticSum = 0;
            for (double value : values) {
                sum += value;
                quadraticSum += Math.pow(value, 2);
            }
            sum = Math.pow(sum, 2);
            setHerfindahlIndex(quadraticSum / sum);
            setNormalizedHerinfahl(getHerfindahlIndex()/(double)sm.getFirstDimension().size());            
            setLineBasedAverage(averageOverall/(double)sm.getFirstDimension().size());
        }

        System.out.println("stats: " + data.getWebtable() + " name: " + sm.getName() + " avg: " + mean + "std: " + getStad() + " vari: " + " density: " + density
                + " meansDens " + meanWdens + " stdDens " + stadWdens + " variDens " + variWdens + " herfindah: " + getHerfindahlIndex() + " normalized herf:"+getNormalizedHerinfahl() );
        
        
    }

    /**
     * @return the mean
     */
    public double getMean() {
        return mean;
    }

    /**
     * @param mean the mean to set
     */
    public void setMean(double mean) {
        this.mean = mean;
    }

    /**
     * @return the stad
     */
    public double getStad() {
        return stad;
    }

    /**
     * @param stad the stad to set
     */
    public void setStad(double stad) {
        this.stad = stad;
    }

    /**
     * @return the vari
     */
    public double getVari() {
        return vari;
    }

    /**
     * @param vari the vari to set
     */
    public void setVari(double vari) {
        this.vari = vari;
    }

    /**
     * @return the density
     */
    public double getDensity() {
        return density;
    }

    /**
     * @param density the density to set
     */
    public void setDensity(double density) {
        this.density = density;
    }

    /**
     * @return the meanWdens
     */
    public double getMeanWdens() {
        return meanWdens;
    }

    /**
     * @param meanWdens the meanWdens to set
     */
    public void setMeanWdens(double meanWdens) {
        this.meanWdens = meanWdens;
    }

    /**
     * @return the stadWdens
     */
    public double getStadWdens() {
        return stadWdens;
    }

    /**
     * @param stadWdens the stadWdens to set
     */
    public void setStadWdens(double stadWdens) {
        this.stadWdens = stadWdens;
    }

    /**
     * @return the variWdens
     */
    public double getVariWdens() {
        return variWdens;
    }

    /**
     * @param variWdens the variWdens to set
     */
    public void setVariWdens(double variWdens) {
        this.variWdens = variWdens;
    }

    /**
     * @return the herfindahlIndex
     */
    public double getHerfindahlIndex() {
        return herfindahlIndex;
    }

    /**
     * @param herfindahlIndex the herfindahlIndex to set
     */
    public void setHerfindahlIndex(double herfindahlIndex) {
        this.herfindahlIndex = herfindahlIndex;
    }

    /**
     * @return the normalizedHerinfahl
     */
    public double getNormalizedHerinfahl() {
        return normalizedHerinfahl;
    }

    /**
     * @param normalizedHerinfahl the normalizedHerinfahl to set
     */
    public void setNormalizedHerinfahl(double normalizedHerinfahl) {
        this.normalizedHerinfahl = normalizedHerinfahl;
    }

    /**
     * @return the LC
     */
    public double getLC() {
        return LC;
    }

    /**
     * @param LC the LC to set
     */
    public void setLC(double LC) {
        this.LC = LC;
    }

    /**
     * @return the lineBasedAverage
     */
    public double getLineBasedAverage() {
        return lineBasedAverage;
    }

    /**
     * @param lineBasedAverage the lineBasedAverage to set
     */
    public void setLineBasedAverage(double lineBasedAverage) {
        this.lineBasedAverage = lineBasedAverage;
    }
}
