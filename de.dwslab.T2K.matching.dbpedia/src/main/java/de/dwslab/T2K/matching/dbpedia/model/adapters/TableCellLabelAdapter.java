package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.MatchingAdapter;

public class TableCellLabelAdapter extends MatchingAdapter<TableCell> {

    @Override
    public Object getLabel(TableCell instance) {
        String col = instance.getTable().getColumns().get(instance.getColumnIndex()).getHeader().toString();
        String row = instance.getTable().getKey().getValues().get(instance.getRowIndex()).toString();
        return row + "/" + col;
    }

    @Override
    public Object getType(TableCell instance) {
        return null;
    }

    @Override
    public Object getTokens(TableCell instance) {
        return null;
    }

}
