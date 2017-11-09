package de.dwslab.T2K.similarity.functions;

public class BiquadraticSimilarityDecorator<T> extends SimilarityFunctionDecorator<T> {

    public BiquadraticSimilarityDecorator(SimilarityFunction<T> innerFunction) {
        super(innerFunction);
    }
    
    @Override
    public Double calculate(T first, T second) {
        return Math.pow(getInnerFunction().calculate(first, second), 4);
    }
    
}
