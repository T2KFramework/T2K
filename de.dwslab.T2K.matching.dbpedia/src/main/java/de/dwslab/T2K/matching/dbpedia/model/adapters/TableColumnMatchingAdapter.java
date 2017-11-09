package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.MatchingAdapter;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class TableColumnMatchingAdapter extends MatchingAdapter<TableColumn> {

    @Override
    public Object getLabel(TableColumn instance) {
        String[] tokens = StringUtils.splitByCharacterTypeCamelCase(instance.getHeader().toString().replaceAll("\\(.*\\)", "").replace("_label", "").replace(".", ""));
        String header = "";
        for (String s : tokens) {
            s = s.trim();
            header = header + s.toLowerCase() + " ";
        }
        header = header.trim();
        return header;
    }

    @Override
    public Collection getLabels(TableColumn instance) {
        List<String> allTokens = new ArrayList<>();
        if(instance.getHeaderList() == null || instance.getHeaderList().isEmpty()) {
            allTokens.add(getLabel(instance).toString());
            return allTokens;
        }
        for (Object s : instance.getHeaderList()) {
            String[] tokens = StringUtils.splitByCharacterTypeCamelCase(s.toString().replaceAll("\\(.*\\)", "").replace("_label", "").replace(".", ""));
            String header = "";
            for (String t : tokens) {
                t = t.trim();
                header = header + t.toLowerCase() + " ";
            }
            header = header.trim();
            allTokens.add(header);
        }
        return allTokens;
    }

    @Override
    public Object getType(TableColumn instance) {
        return instance.getDataType();
    }

    @Override
    public Object getTokens(TableColumn instance) {
        return null;
    }

    @Override
    public boolean isMultiValued(TableColumn instance) {
        if(instance.getHeaderList() == null || instance.getHeaderList().isEmpty()) {
            return false;
        }
        return true;
    }
}
