package de.dwslab.T2K.matching.dbpedia.model.adapters;

import java.util.Collection;
import java.util.List;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.utils.query.Func;
import de.dwslab.T2K.utils.query.Q;

public class TableCellMatchingAdapter extends MatchingAdapter<TableCell> {

	@Override
	public Object getLabel(TableCell instance) {
		return instance.getValue();
	}

	@Override
	public Object getType(TableCell instance) {
		return instance.getType();
	}

	@Override
	public boolean isMultiValued(TableCell instance) {
	    return instance.getValue() instanceof List;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public Collection getLabels(TableCell instance) {
	    if(isMultiValued(instance)) {
//	        return (Collection)instance.getValue();
	        //TODO the list should not contain null values, but it does!
	        return Q.where((Collection)instance.getValue(), new Func<Boolean, Object>() {

                @Override
                public Boolean invoke(Object in) {
                    return in != null;
                }
	            
            });
	    } else {
	        return super.getLabels(instance);
	    }
	}

    @Override
    public Object getTokens(TableCell instance) {
        return null;
    }
}
