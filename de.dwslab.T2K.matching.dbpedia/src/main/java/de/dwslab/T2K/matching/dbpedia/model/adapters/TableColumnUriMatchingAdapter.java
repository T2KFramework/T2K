package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.tableprocessor.model.TableColumn;

public class TableColumnUriMatchingAdapter extends MatchingAdapter<TableColumn> {

    @Override
    public Object getLabel(TableColumn instance) {
        if(instance==null) {
            return null;
        } else {
            return instance.getURI();
        }
    }

    @Override
    public Object getType(TableColumn instance) {
        return null;
    }

    @Override
    public Object getTokens(TableColumn instance) {
        return null;
    }

    
    
}
