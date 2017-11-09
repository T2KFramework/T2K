package de.dwslab.T2K.similarity.matrix;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/***
 * A Similarity Matrix that uses a ByteBuffer as Storage
 * @author Oliver
 *
 *inspired by http://java-performance.info/use-case-optimizing-memory-footprint-of-read-only-csv-file-trove-unsafe-bytebuffer-data-compression/
 *
 * @param <T>
 */
public class ByteBufferedSimilarityMatrix<T> extends SimilarityMatrix<T> {

    private HashSet<T> firstDim;
    private HashSet<T> secondDim;
    private Map<T, Map<T, Double>> sparseMartrix;
    
    private static final int BUFFER_SIZE_STEP = 1024 * 1024;
    private Map<T, Integer> indexFirstDim;
    private Map<T, Integer> indexSecondDim;
    private ByteBuffer data = ByteBuffer.allocate(BUFFER_SIZE_STEP);
    
    
    
    @Override
    public Double get(T first, T second) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void set(T first, T second, Double similarity) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<T> getFirstDimension() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<T> getSecondDimension() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<T> getMatches(T first) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<T> getMatchesAboveThreshold(T first,
            double similarityThreshold) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected SimilarityMatrix<T> createEmptyCopy() {
        // TODO Auto-generated method stub
        return null;
    }

}
