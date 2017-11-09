package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.tableprocessor.model.Table;

public class TableClassMatchingAdapter extends MatchingAdapter<Table> {

	@Override
	public Object getLabel(Table instance) {
		return instance.getHeader();
	}

	@Override
	public Object getType(Table instance) {
		// intentionally returns null as tables represent types and have no types by themselves
		return null;
	}

    @Override
    public Object getTokens(Table instance) {
        return null;
    }

}
