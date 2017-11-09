package de.dwslab.T2K.similarity.functions;

public class QuadraticSimilarityDecorator<T> extends SimilarityFunctionDecorator<T> {

    public QuadraticSimilarityDecorator(SimilarityFunction<T> innerFunction) {
        super(innerFunction);
    }
    
    @Override
    public Double calculate(T first, T second) {
        return Math.pow(getInnerFunction().calculate(first, second), 2);
    }
}
