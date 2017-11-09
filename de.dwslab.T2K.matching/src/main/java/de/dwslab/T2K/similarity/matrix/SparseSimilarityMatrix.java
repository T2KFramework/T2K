package de.dwslab.T2K.similarity.matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A similarity matrix that uses HashMaps as storage
 * @author Oliver
 *
 * @param <T>
 */
public class SparseSimilarityMatrix<T> extends SimilarityMatrix<T> {

    private Set<T> firstDim;
    private Set<T> secondDim;
    private Map<T, Map<T, Double>> sparseMartrix;

    protected int expectedFirstDimensionSize;
    protected int expectedSecondDimensionSize;
    
    public Map<T, Map<T, Double>> getSparseMartrix() {
        return sparseMartrix;
    }

    protected Map<T, Map<T, Double>> createOuterMap() {
        //return new HashMap<T, Map<T, Double>>(expectedFirstDimensionSize);
        return new HashMap<T, Map<T, Double>>();
    }

    protected Map<T, Double> createInnerMap() {
        //return new HashMap<T, Double>(expectedSecondDimensionSize);
        return new HashMap<T, Double>();
    }

    public SparseSimilarityMatrix(int firstDimension, int secondDimension) {
        expectedFirstDimensionSize = firstDimension;
        expectedSecondDimensionSize = secondDimension;
        sparseMartrix = createOuterMap();
    }

    @Override
    public Double get(T first, T second) {
        Map<T, Double> innerMap = getSparseMartrix().get(first);

        if (innerMap != null) {
            Double value = innerMap.get(second);

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    @Override
    public void set(T first, T second, Double similarity) {
        if (first == null || second == null) {
            throw new NullPointerException();
        }

        Map<T, Double> innerMap = getSparseMartrix().get(first);

        if (innerMap == null) {
            innerMap = createInnerMap();

            getSparseMartrix().put(first, innerMap);

            if (firstDim != null) {
                firstDim.add(first);
            }
        }

        if (secondDim != null && innerMap.get(second) == null) {
            secondDim.add(second);
        }

        if(similarity==null) {
            innerMap.remove(second);
        } else {
            innerMap.put(second, similarity);
        }
    }

    @Override
    public Collection<T> getMatches(T first) {
        return getMatchesAboveThreshold(first, Double.NEGATIVE_INFINITY);
    }

    @Override
    public Collection<T> getMatchesAboveThreshold(T first,
            double similarityThreshold) {
        Map<T, Double> innerMap = getSparseMartrix().get(first);

        if (innerMap != null) {
            Collection<T> result = new ArrayList<T>(innerMap.keySet().size());

            for (T second : innerMap.keySet()) {
                if (innerMap.get(second) != null
                        && innerMap.get(second) > similarityThreshold) {
                    result.add(second);
                }
            }

            return result;
        } else {
            return new ArrayList<T>();
        }
    }
    
    public T getBestPair(T first) {
        double max = -1.0;
        T best = null;
        Map<T, Double> innerMap = getSparseMartrix().get(first);
        if(innerMap == null) {
            return null;
        }
        for(T second : innerMap.keySet()) {
            if(innerMap.get(second)>max) {
                max = innerMap.get(second);
                best = second;
            }
        }
        return best;
    }
    
    public void setAllToNull(T first) {
        getSparseMartrix().put(first,null);
    }
    
    protected Set<T> createFirstDimensionCache() {
        return new HashSet<T>();
    }
    
    protected Set<T> createSecondDimensionCache() {
        return new HashSet<T>();
    }

    protected void cacheFirstDimension() {
        firstDim = createFirstDimensionCache();

        firstDim.addAll(getSparseMartrix().keySet());
    }

    protected void cacheSecondDimension() {
        secondDim = createSecondDimensionCache();

        for (Map<T, Double> map : getSparseMartrix().values()) {
            secondDim.addAll(map.keySet());
        }
    }

    @Override
    public Collection<T> getFirstDimension() {
        // // return a new arraylist to prevent an exception if during the
        // iteration some similarities are set to 0 (which will lead to a
        // modification of the keySet() collection)
        //
        // ArrayList<T> lst = new
        // ArrayList<T>(getSparseMartrix().keySet().size());
        //
        // for(T t : getSparseMartrix().keySet()) {
        // lst.add(t);
        // }
        //
        // //return getSparseMartrix().keySet();
        // return lst;

        if (firstDim == null) {
            synchronized (this) {
                cacheFirstDimension();
            }
        }
        return firstDim;
    }

    @Override
    public Collection<T> getSecondDimension() {

        // Set<T> result = new HashSet<T>();
        //
        // for(Map<T, Double> map : getSparseMartrix().values())
        // {
        // result.addAll(map.keySet());
        // }
        //
        // return result;

        if (secondDim == null) {
            synchronized (this) {
                cacheSecondDimension();
            }
        }
        
        return secondDim;
    }

    @Override
    protected SimilarityMatrix<T> createEmptyCopy() {
        return new SparseSimilarityMatrixFactory().createSimilarityMatrix(
                getFirstDimension().size(), getSecondDimension().size());
    }
}
