/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.process;

import java.util.List;

/**
 *
 * @author domi
 */
public abstract class MatchingComponent {
    
    private List<Parameter> parameters;
    
    //parameters need to be determined before running
    
   public abstract void run(Configuration config);
    
    /**
     * Evaluate an aligment created by this component.
     * 
     * @return 
     */
    public double evaluate(Configuration config){
        run(config);
        return -1.0;
    }

    /**
     * @return the parameters
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
    
}
