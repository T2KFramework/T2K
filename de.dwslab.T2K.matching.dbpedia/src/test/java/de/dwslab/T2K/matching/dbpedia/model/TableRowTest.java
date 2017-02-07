package de.dwslab.T2K.matching.dbpedia.model;

import de.dwslab.T2K.matching.dbpedia.model.adapters.TableToRowHierarchyAdapter;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
import java.util.Collection;
import junit.framework.TestCase;

public class TableRowTest extends TestCase {

	public void testGetKey() {

	}

	public void testGetCells() {
		TableReader r = new TableReader();
		Table webtable = null;
		try {
			webtable = r.readWebTable("in/web/11688006_0_8123036130090004213.csv");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TableToRowHierarchyAdapter rowAdapter = new TableToRowHierarchyAdapter();
		Collection<TableRow> rows = rowAdapter.getParts(webtable);
		
		for(TableRow row : rows) {
			
			// check whether all cells exist
			for(TableCell c : row.getCells()) {
				
				Object tableValue = webtable.getColumns().get(c.getColumnIndex()).getValues().get(c.getRowIndex());
				Object tableType = webtable.getColumns().get(c.getColumnIndex()).getDataType();
				
				Object type = c.getType();
				Object value = c.getValue();
				assertEquals(tableValue, value);
				assertEquals(tableType, type);
			}
			
			// check whether cells for all values were created
			for(int i = 0; i < webtable.getColumns().size(); i++) {
				
				Object tableValue = webtable.getColumns().get(i).getValues().get(row.getRowIndex());
				Object tableType = webtable.getColumns().get(i).getDataType();
				
				TableCell c = null;
				
				for(TableCell cell : row.getCells()) {
					if(cell.getColumnIndex()==i)
					{
						c = cell;
						break;
					}
				}
				
				Object type = c.getType();
				Object value = c.getValue();
				assertEquals(tableValue, value);
				assertEquals(tableType, type);
			}
		}
	}

	public void testGetURI() {
		//fail("Not yet implemented");
	}

}
