/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.process;

/**
 *
 * @author domi
 */
public class Parameter{
    
    private String name;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }  

    private Object defaultValue;
    public Object getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    @Override
    public boolean equals(Object o) {
        return hashCode()==o.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
    
    public Parameter(String name) {
        this.name = name;
    }
    
    public Parameter(String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }
}
