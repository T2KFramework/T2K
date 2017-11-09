package de.dwslab.T2K.matching.dbpedia.model.adapters;

import java.util.Collection;

import de.dwslab.T2K.matching.MatchingHierarchyAdapater;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;

public class TableToColumnMatchingAdapter extends MatchingHierarchyAdapater<Table, TableColumn> {

    @Override
    public Collection<TableColumn> getParts(Table instance) {
        return instance.getColumns();
    }

}
