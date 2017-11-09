package de.dwslab.T2K.matching.dbpedia;

import java.util.Collection;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.blocking.TypeBasedBlocking;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;

public class LoggingTypeBasedBlocking extends TypeBasedBlocking<TableCell> {

    public LoggingTypeBasedBlocking(MatchingAdapter<TableCell> adapter) {
        super(adapter);
    }

    @Override
    public Collection<TableCell> getCandidates(TableCell instance,
            Collection<TableCell> candidates) {
        Collection<TableCell> col = super.getCandidates(instance, candidates);
        
        StringBuilder sb = new StringBuilder();
        
        String key = instance.getTable().getKey().getValues().get(instance.getRowIndex()).toString();
        String value = instance.getValue() + "";
        
        sb.append(key + ": " + value + " -> ");
        
        for(TableCell c : col) {
            String column = c.getTable().getColumns().get(c.getColumnIndex()).getHeader().toString();
            String v = c.getValue() + "";
            sb.append(column+"="+v+"; ");
        }
        
        System.out.println(sb.toString());
        
        return col;
    }
}
