package de.dwslab.T2K.similarity.matrix;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/***
 * A Similarity Matrix that uses fastutil's hashmaps as storage
 * @author Oliver
 *
 */
public class FastSparseSimilarityMatrix<T> extends SparseSimilarityMatrix<T> {

    protected Map<T, Map<T, Double>> createOuterMap() {
        //return new HashMap<T, Map<T, Double>>();
        //return new Object2ObjectArrayMap<T, Map<T, Double>>(expectedFirstDimensionSize);
        return new Object2ObjectArrayMap<T, Map<T, Double>>();
    }

    protected Map<T, Double> createInnerMap() {
        //return new HashMap<T, Double>();
        //return new Object2DoubleArrayMap<>(expectedSecondDimensionSize);
        return new Object2DoubleArrayMap<>();
    }
    
    @Override
    protected Set<T> createFirstDimensionCache() {
        return new ObjectOpenHashSet<>();
    }

    @Override
    protected Set<T> createSecondDimensionCache() {
        return new ObjectOpenHashSet<>();
    }
    
    public FastSparseSimilarityMatrix(int firstDimension, int secondDimension) {
        super(firstDimension, secondDimension);
    }

}
