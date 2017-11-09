package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author domi
 */
public class NEMatchingAdapter extends MatchingAdapter<TableRow> {

    private int columnIndex;

    @Override
    public Object getLabel(TableRow instance) {
        if(instance.getURI() != null &&instance.getURI().toString().contains("dbpedia.org")) {
            return instance.getKey();
        }
        return instance.getTable().getColumns().get(columnIndex).getValues().get(instance.getRowIndex());
    }

    @Override
    public boolean isMultiValued(TableRow instance) {
        if(instance.getURI() != null && instance.getURI().toString().contains("dbpedia.org")) {
            return instance.getKey() instanceof List;
        }
        return instance.getTable().getColumns().get(columnIndex).getValues().get(instance.getRowIndex()) instanceof List;
    }

    @Override
    public Collection getLabels(TableRow instance) {
        if (isMultiValued(instance)) {
            if(instance.getURI() != null && instance.getURI().toString().contains("dbpedia.org")) {
                return (List)instance.getKey();
            }
            return (List) instance.getTable().getColumns().get(columnIndex).getValues().get(instance.getRowIndex());
        } else {
            return super.getLabels(instance);
        }
    }

    @Override
    public Object getType(TableRow instance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getTokens(TableRow instance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the columnIndex
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * @param columnIndex the columnIndex to set
     */
    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

}
