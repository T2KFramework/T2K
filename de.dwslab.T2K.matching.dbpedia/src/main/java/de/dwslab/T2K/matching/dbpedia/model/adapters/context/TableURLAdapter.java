
package de.dwslab.T2K.matching.dbpedia.model.adapters.context;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.tableprocessor.model.Table;

/**
 *
 * @author domi
 */
public class TableURLAdapter extends MatchingAdapter<Table> {

    @Override
    public Object getLabel(Table instance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getType(Table instance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getTokens(Table instance) {
        return instance.getSource();
    }
    
}
