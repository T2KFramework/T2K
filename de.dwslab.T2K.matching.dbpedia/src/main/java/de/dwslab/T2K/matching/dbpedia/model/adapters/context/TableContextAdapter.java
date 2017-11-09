
package de.dwslab.T2K.matching.dbpedia.model.adapters.context;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.tableprocessor.model.Table;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author domi
 */
public class TableContextAdapter extends MatchingAdapter<Table>{

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
        List<String> context =new ArrayList<>();
        context.addAll(Arrays.asList(instance.getContextAfterTable().split("\\s")));
        context.addAll(Arrays.asList(instance.getContextBeforeTable().split("\\s")));
        return StopWordRemover.removeStopWords(context);
    }
    
}
