package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;

public class TableRowUriMatchingAdapter extends MatchingAdapter<TableRow> {

    @Override
    public Object getLabel(TableRow instance) {
        return instance.getURI();
    }

    @Override
    public Object getType(TableRow instance) {
        return null;
    }

    @Override
    public Object getTokens(TableRow instance) {
        return null;
    }

}
