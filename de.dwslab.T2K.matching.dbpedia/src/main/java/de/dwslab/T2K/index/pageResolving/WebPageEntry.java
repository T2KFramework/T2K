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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.index.pageResolving;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;

/**
 *
 * @author domi
 */
public class WebPageEntry {
    
    private String webPageURI;
    private String tableID;
    
    public static final String URI_FIELD = "uri";
    public static final String ID_FIELD = "id";

    public static WebPageEntry fromDocument(Document doc) {
        WebPageEntry e = new WebPageEntry();
        e.setUri(doc.getField(URI_FIELD).stringValue());
        e.setTableID(doc.getField(ID_FIELD).stringValue());
        return e;
    }

    public Document createDocument() {
        Document doc = new Document();
        doc.add(new StoredField(URI_FIELD, getUri()));
        doc.add(new TextField(ID_FIELD, getTableID(),Field.Store.YES));
        return doc;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return webPageURI;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.webPageURI = uri;
    }

    /**
     * @return the tableID
     */
    public String getTableID() {
        return tableID;
    }

    /**
     * @param tableID the tableID to set
     */
    public void setTableID(String tableID) {
        this.tableID = tableID;
    }

    
}
