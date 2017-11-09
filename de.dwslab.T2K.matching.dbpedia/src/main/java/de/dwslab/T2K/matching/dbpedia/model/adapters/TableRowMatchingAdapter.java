package de.dwslab.T2K.matching.dbpedia.model.adapters;

import java.util.Collection;
import java.util.List;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.context.StopWordRemover;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.lang.StringUtils;

public class TableRowMatchingAdapter extends MatchingAdapter<TableRow> {

	@Override
	public Object getLabel(TableRow instance) {
		String key = instance.getKey().toString();
                key = key.replace("&quot;", "");
                key = key.replace("&quot;", "");                
                return key;
	}

	@Override
	public boolean isMultiValued(TableRow instance) {
	    return instance.getKey() instanceof List;
	}
	
	@Override
	public Collection getLabels(TableRow instance) {
        if(isMultiValued(instance)) {
            return (List)instance.getKey();
        } else {
            return super.getLabels(instance);
        }
	}
	
	@Override
	public Object getType(TableRow instance) {
		return instance.getTable().getHeader();
	}

    @Override
    public Object getTokens(TableRow instance) {
        List<String> tokens = new ArrayList<>();
        for (TableCell tc : instance.getCells()) {
            String[] cellContent = tc.getValue().toString().split("\\s");
            tokens.addAll(Arrays.asList(cellContent));
        }
        return tokens;
    }

}
