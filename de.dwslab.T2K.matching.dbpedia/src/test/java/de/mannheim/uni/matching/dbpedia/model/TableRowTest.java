/**
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of
							Mannheim (t2k@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mannheim.uni.matching.dbpedia.model;

import java.util.Collection;

import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableToRowHierarchyAdapter;
import de.dwslab.T2K.tableprocessor.IO.TableReader;
import de.dwslab.T2K.tableprocessor.model.Table;
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
