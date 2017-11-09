package de.dwslab.T2K.matching.dbpedia.tableToTable;
/** 
 *
 * Copyright (C) 2015 Data and Web Science Group, University of Mannheim, Germany (code@dwslab.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;


/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class WebTableIndexEntry {

	private String entityLabels;
	private String table;
	private String dbpediaClass;
	
	public static final String ENTITY_LABELS_FIELD = "entity_labels";
	public static final String TABLE_FIELD = "table";
	public static final String DBPEDIA_CLASS_FIELD = "dbpedia_class";
	
	public static WebTableIndexEntry fromDocument(Document doc)
	{
		WebTableIndexEntry e = new WebTableIndexEntry();
		
                if(doc.getField(ENTITY_LABELS_FIELD) != null) {
                    e.setEntityLabels(doc.getField(ENTITY_LABELS_FIELD).stringValue());
                }
		e.setTable(doc.getField(TABLE_FIELD).stringValue());
		e.setDbpediaClass(doc.getField(DBPEDIA_CLASS_FIELD).stringValue());

		return e;
	}
	
	public Document createDocument()
	{
		Document doc = new Document();
		
		doc.add(new TextField(ENTITY_LABELS_FIELD, entityLabels, Field.Store.NO));
		doc.add(new StoredField(TABLE_FIELD, table));
		doc.add(new StringField(DBPEDIA_CLASS_FIELD, dbpediaClass, Field.Store.YES));
		
		return doc;
	}

	public String getEntityLabels() {
		return entityLabels;
	}

	public void setEntityLabels(String entityLabels) {
		this.entityLabels = entityLabels;
	}

	public String getDbpediaClass() {
		return dbpediaClass;
	}

	public void setDbpediaClass(String dbpediaClass) {
		this.dbpediaClass = dbpediaClass;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dbpediaClass == null) ? 0 : dbpediaClass.hashCode());
		result = prime * result + ((entityLabels == null) ? 0 : entityLabels.hashCode());
		result = prime * result + ((table == null) ? 0 : table.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WebTableIndexEntry other = (WebTableIndexEntry) obj;
		if (dbpediaClass == null) {
			if (other.dbpediaClass != null)
				return false;
		} else if (!dbpediaClass.equals(other.dbpediaClass))
			return false;
		if (entityLabels == null) {
			if (other.entityLabels != null)
				return false;
		} else if (!entityLabels.equals(other.entityLabels))
			return false;
		if (table == null) {
			if (other.table != null)
				return false;
		} else if (!table.equals(other.table))
			return false;
		return true;
	}


}
