
package de.dwslab.T2K.matching.dbpedia.model.adapters.context;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author domi
 */
public class TableContentAdapter extends MatchingAdapter<Table>{    

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
        List<String> content = new ArrayList<>();
        for(TableColumn tc : instance.getColumns()) {
            for(Integer i : tc.getValues().keySet()) {                
                content.addAll(Arrays.asList(tc.getValues().get(i).toString().split("\\s")));            
            }
        }
        return StopWordRemover.removeStopWords(content);
    }
    
}
