/*
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
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
package de.dwslab.T2K.index.dbpedia;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class DBpediaIndexEntry {

	private String uri;
	private String label;
	private String class_label;
	
	public static final String URI_FIELD = "uri";
	public static final String LABEL_FIELD = "label";
	public static final String CLASS_LABEL_FIELD = "class_label";
	
	public static DBpediaIndexEntry fromDocument(Document doc)
	{
		DBpediaIndexEntry e = new DBpediaIndexEntry();
		e.setUri(doc.getField(URI_FIELD).stringValue());
		e.setLabel(doc.getField(LABEL_FIELD).stringValue());
		e.setClass_label(doc.getField(CLASS_LABEL_FIELD).stringValue());
		return e;
	}
	
	public Document createDocument()
	{
		Document doc = new Document();
		doc.add(new StoredField(URI_FIELD, uri));
		doc.add(new TextField(LABEL_FIELD, label, Field.Store.YES));
		doc.add(new StringField(CLASS_LABEL_FIELD, class_label, Field.Store.YES));
		
		return doc;
	}
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getClass_label() {
		return class_label;
	}
	
	public void setClass_label(String class_label) {
		this.class_label = class_label;
	}
	
}
