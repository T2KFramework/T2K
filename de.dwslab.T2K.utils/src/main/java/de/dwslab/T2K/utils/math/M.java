package de.dwslab.T2K.utils.math;

public class M {

    public static Comparable min(Comparable first, Comparable second) {
        if(first==null) {
            return second;
        }
        
        if(second==null) {
            return first;
        }
        
        if(first.compareTo(second)<0) {
            return first;
        } else {
            return second;
        }
    }
    
    public static Comparable max(Comparable first, Comparable second) {
        
        if(first==null) {
            return second;
        }
        
        if(second==null) {
            return first;
        }
        
        if(first.compareTo(second)>0) {
            return first;
        } else {
            return second;
        }
    }
    
}
