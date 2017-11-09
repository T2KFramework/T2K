package de.dwslab.T2K.similarity.functions;

import de.dwslab.T2K.matching.ValueRange;

public class SimilarityFunctionDecorator<T> extends SimilarityFunction<T> {

    private SimilarityFunction<T> innerFunction;
    protected SimilarityFunction<T> getInnerFunction() {
        return innerFunction;
    }
    
    public SimilarityFunctionDecorator(SimilarityFunction<T> innerFunction) {
        this.innerFunction = innerFunction;
    }
    
    @Override
    public Double calculate(T first, T second) {
        return innerFunction.calculate(first, second);
    }
    
    @Override
    public void setValueRange(ValueRange range) {
        innerFunction.setValueRange(range);
    }
    
}
