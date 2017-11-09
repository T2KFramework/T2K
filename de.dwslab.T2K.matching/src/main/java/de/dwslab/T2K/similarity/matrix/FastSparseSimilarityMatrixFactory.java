package de.dwslab.T2K.similarity.matrix;

public class FastSparseSimilarityMatrixFactory extends SimilarityMatrixFactory {

    public <T> SimilarityMatrix<T> createSimilarityMatrix(int firstDimension, int secondDimension) {
        return new FastSparseSimilarityMatrix<T>(firstDimension, secondDimension);
    }
    
}
