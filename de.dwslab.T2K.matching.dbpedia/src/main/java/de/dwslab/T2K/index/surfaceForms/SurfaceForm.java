/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.index.surfaceForms;

import java.util.List;

/**
 *
 * @author domi
 */
public class SurfaceForm implements Comparable<Object>{
    
    private String label;
    private double score;    
    
    public SurfaceForm(String label, double score) {
        this.label = label;
        this.score = score;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int compareTo(Object t) {
        SurfaceForm f = (SurfaceForm)t;
        if(f.getScore()>this.getScore()) {
            return 1;
        }
        if(f.getScore()==getScore()) {
            return 0;
        }
        return -1;
    }

    /**
     * @return the score
     */
    public double getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(double score) {
        this.score = score;
    }
    
}
