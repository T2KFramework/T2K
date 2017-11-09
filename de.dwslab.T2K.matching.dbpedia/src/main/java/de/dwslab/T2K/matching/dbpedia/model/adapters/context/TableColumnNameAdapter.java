
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
public class TableColumnNameAdapter extends MatchingAdapter<Table> {

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
        List<String> columnNames = new ArrayList<>();
        for(TableColumn tc : instance.getColumns()) {
            if(tc.isKey()) {
                continue;
            }
            String[] header = tc.getHeader().toString().split("\\s");
            for(int i=0; i<header.length; i++) {
                header[i] = header[i].toLowerCase();
                if(header[i].equals("name") || header[i].equals("label")) {
                    continue;
                }
            }
            columnNames.addAll(Arrays.asList(header));
        }
        
        return StopWordRemover.removeStopWords(columnNames);
    }
    
}
