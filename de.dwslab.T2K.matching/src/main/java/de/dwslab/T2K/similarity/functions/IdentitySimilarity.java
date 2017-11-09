package de.dwslab.T2K.similarity.functions;

public class IdentitySimilarity<T> extends SimilarityFunction<T> {

    @Override
    public Double calculate(T first, T second) {
        if(first==null ^ second==null) {
            return 0.0;
        } else if(first==null && second == null) {
            return 1.0;
        } else {
            return first.equals(second) ? 1.0 : 0.0;
        }
    }

}
