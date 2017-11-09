
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
public class TablePageTitleAdapter extends MatchingAdapter<Table> {

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
        List<String> pageTitle =new ArrayList<>();
        pageTitle.addAll(Arrays.asList(instance.getPageTitle().split("\\s")));
        List<String> tokens = StopWordRemover.removeStopWords(pageTitle);
        return tokens;
    }
    
}
