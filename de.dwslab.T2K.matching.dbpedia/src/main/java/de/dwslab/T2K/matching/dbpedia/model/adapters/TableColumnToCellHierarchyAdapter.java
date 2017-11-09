package de.dwslab.T2K.matching.dbpedia.model.adapters;

import java.util.Collection;
import java.util.HashSet;

import de.dwslab.T2K.matching.MatchingHierarchyAdapater;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableCellCache;
import de.dwslab.T2K.matching.dbpedia.model.TableColumnCache;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;

public class TableColumnToCellHierarchyAdapter extends MatchingHierarchyAdapater<TableColumn, TableCell> {

	@Override
	public Collection<TableCell> getParts(TableColumn instance) {

	    TableColumnCache colCache = TableColumnCache.get();
	    
        Collection<TableCell> cells = colCache.get(instance);
        
	    if(cells==null) {
	    
	        synchronized (instance) {
                
	            if(cells==null) {
	                
	                HashSet<TableCell> cellsTmp = new HashSet<TableCell>(
	                        instance.getValues().size());

	                int colIndex = instance.getTable().getColumns().indexOf(instance);

	                Table t = instance.getTable();
	                TableCellCache cache = TableCellCache.get();

	                for (Integer key : instance.getValues().keySet()) {

	                    TableCell c = cache.getOrCreate(t, key, colIndex);
	                    cellsTmp.add(c);

	                }

	                colCache.set(instance, cellsTmp);
	                
	                
	                cells = cellsTmp;
	            }
	            
            }

	    }
		
		return cells;
	}

}
