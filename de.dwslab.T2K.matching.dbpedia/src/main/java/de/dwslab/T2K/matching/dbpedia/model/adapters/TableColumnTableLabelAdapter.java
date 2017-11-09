package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.tableprocessor.model.TableColumn;

public class TableColumnTableLabelAdapter extends MatchingAdapter<TableColumn> {

    @Override
    public Object getLabel(TableColumn instance) {
        String tbl = instance.getTable().getHeader();
        return tbl;
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
