package de.mannheim.uni.matching;

import de.dwslab.T2K.matching.MatchingAdapter;
import java.util.Collection;

public class TestInstanceMatchingAdapterWithList extends MatchingAdapter<TestInstance> {

    @Override
    public Object getLabel(TestInstance instance) {
        return instance.getLabel();
    }

    @Override
    public Object getType(TestInstance instance) {
        return instance.getType();
    }
    
    @Override
    public boolean isMultiValued(TestInstance instance) {
        return true;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Collection getLabels(TestInstance instance) {
        return instance.getList();
    }

    @Override
    public Object getTokens(TestInstance instance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
