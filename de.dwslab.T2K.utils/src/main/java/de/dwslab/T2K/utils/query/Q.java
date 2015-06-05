/**
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
package de.dwslab.T2K.utils.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class Q {

    public static <T> T firstOrDefault(Collection<T> data) {
        Iterator<T> it = data.iterator();
        
        if(!it.hasNext()) {
            return null;
        } else {
            return it.next();
        }
    }
    
    public static <T> Collection<T> where(Collection<T> data, Func<Boolean, T> predicate) {
        Collection<T> result = new LinkedList<>();
        
        for(T instance : data) {
            if(predicate.invoke(instance)) {
                result.add(instance);
            }
        }
        
        return result;
    }
    
    public static <T, U> Map<T, Collection<U>> group(Collection<U> data, Func<T, U> keySelector) {
        Map<T, Collection<U>> result = new HashMap<T, Collection<U>>();
        
        for(U instance : data) {
            T key = keySelector.invoke(instance);
            
            Collection<U> collection = result.get(key);
            
            if(collection==null) {
                collection = new LinkedList<U>();
                result.put(key, collection);
            }
            
            collection.add(instance);
        }
        
        return result;
    }
    
    public static <T, U extends Comparable<U>> T max(Collection<T> data, Func<U, T> valueSelector) {
        T maxObj = null;
        U maxValue = null;
        
        for(T item : data) {
            U value = valueSelector.invoke(item);
            if(maxValue==null || value.compareTo(maxValue)>0) {
                maxValue = value;
                maxObj = item;
            }
        }
        
        return maxObj;
    }
    
    /**
     * returns all elements (de-duplicated) that appear in first or second
     * @param first
     * @param second
     * @return
     */
    public static <T> Collection<T> union(Collection<T> first, Collection<T> second) {
        Collection<T> result = new HashSet<T>(first.size()+second.size());
        
        result.addAll(first);
        result.addAll(second);
        
        return result;
    }
    
    /**
     * returns all elements that appear both in first and second
     * @param first
     * @param second
     * @return
     */
    public static <T> Collection<T> intersection(Collection<T> first, Collection<T> second) {
        Collection<T> result = new HashSet<>();
        
        for(T t : first) {
            if(second.contains(t)) {
                result.add(t);
            }
        }

        return result;
    }
    
    /**
     * returns all elements of first without the elements of second
     * @param first
     * @param second
     * @return
     */
    public static <T> Collection<T> without(Collection<T> first, Collection<T> second) {
        Collection<T> result = new LinkedList<T>(first);
        
        Iterator<T> it = result.iterator();
        
        while(it.hasNext()) {
            if(second.contains(it.next())) {
                it.remove();
            }
        }
        
        return result;
    }

    /**
     * returns all elements of first without the elements of second
     * @param first
     * @param second
     * @return
     */
    public static <T, U> Collection<T> without(Collection<T> first, Collection<U> second, Func<U, T> firstToSecond) {
        Collection<T> result = new LinkedList<T>(first);
        
        Iterator<T> it = result.iterator();
        
        while(it.hasNext()) {
            if(second.contains(firstToSecond.invoke(it.next()))) {
                it.remove();
            }
        }
        
        return result;
    }
    
    public static <T,U> Collection<U> typeAs(Collection<T> first, Func<U, T> transformation) {
        Collection<U> result = new ArrayList<U>(first.size());
        
        for(T t : first) {
            result.add(transformation.invoke(t));
        }
        
        return result;
    }
    
}
