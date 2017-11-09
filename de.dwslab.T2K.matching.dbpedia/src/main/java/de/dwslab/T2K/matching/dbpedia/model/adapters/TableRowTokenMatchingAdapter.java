package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author domi
 */
public class TableRowTokenMatchingAdapter extends MatchingAdapter<TableRow> {

    @Override
    public Object getLabel(TableRow instance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getType(TableRow instance) {
        return instance.getTable().getHeader();
    }

    @Override
    public Object getTokens(TableRow instance) {
        List<String> tokens = new ArrayList<>();
        for (TableCell tc : instance.getCells()) {
            String[] tokenStrings = tc.getValue().toString().split("\\s");
            tokens.addAll(Arrays.asList(tokenStrings));
        }
        if (instance.getTable().getPageTitle() != null) {
            String[] tokenStrings = instance.getTable().getPageTitle().split("\\s");
            tokens.addAll(Arrays.asList(tokenStrings));
            tokenStrings = instance.getTable().getTableTitle().split("\\s");
            tokens.addAll(Arrays.asList(tokenStrings));
        }
        else {
            String tableFileName = instance.getTable().getHeader();
            String[] splitted = tableFileName.split("_");
            List<String> terms = new ArrayList<>();
            int count = 0;
            for(String s : splitted) {
                if(s.matches("[a-zA-Z]")) {
                    if(!s.equals("list") && !s.equals("of")) {
                        terms.add(s);
                    }
                    count++;
                }
            }
            double rel = (double)count/(splitted.length);
            if(rel>=0.5) {
                tokens.addAll(terms);                
            }
        }
//
//        tokenStrings = instance.getTable().getContextBeforeTable().split("\\s");
//        tokens.addAll(Arrays.asList(tokenStrings));
//
//        tokenStrings = instance.getTable().getContextAfterTable().split("\\s");
//        tokens.addAll(Arrays.asList(tokenStrings));
        return tokens;
    }

}
