/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.blocking;

import de.dwslab.T2K.matching.MatchingAdapter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author dritze
 */
public class SimpleBlokcing<T> extends Blocking<T> {

    private MatchingAdapter adapter;

    public Collection<T> getCandidates(T instance,
            Collection<T> candidates) {
        Set<T> blockedCand = new HashSet<T>();
        String label = (String) adapter.getLabel(instance);
        if(label.length()<1){
            return blockedCand;
        }
        String firstChar = label.substring(0, 1);
        for (T t : candidates) {
            String labelCand = (String) adapter.getLabel(t);
            if(labelCand.length()<1){
               continue;
            }
            if (firstChar.equals(labelCand.substring(0, 1))) {
                blockedCand.add(t);
            }
        }
        return blockedCand;
    }

    /**
     * @return the adapter
     */
    public MatchingAdapter getAdapter() {
        return adapter;
    }

    /**
     * @param adapter the adapter to set
     */
    public void setAdapter(MatchingAdapter adapter) {
        this.adapter = adapter;
    }

}
