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
package de.dwslab.T2K.index.dbpedia.abstractIndex;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class DBpediaAbstractIndexEntry {

    /**
     * @return the ABSTRACT_FIELD
     */
    public static String getABSTRACT_FIELD() {
        return ABSTRACT_FIELD;
    }

    /**
     * @param aABSTRACT_FIELD the ABSTRACT_FIELD to set
     */
    public static void setABSTRACT_FIELD(String aABSTRACT_FIELD) {
        ABSTRACT_FIELD = aABSTRACT_FIELD;
    }

    private String uri;
    private String abstractText;

    public static final String URI_FIELD = "uri";
    private static String ABSTRACT_FIELD = "abstract";

    public static DBpediaAbstractIndexEntry fromDocument(Document doc) {
        DBpediaAbstractIndexEntry e = new DBpediaAbstractIndexEntry();
        e.setUri(doc.getField(URI_FIELD).stringValue());
        e.setAbstractText(doc.getField(ABSTRACT_FIELD).stringValue());
        return e;
    }

    public Document createDocument() {
        Document doc = new Document();
        //doc.add(new StoredField(URI_FIELD, uri));
        //Field fld = new Field(ABSTRACT_FIELD, abstractText, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES);      
        //doc.add(fld);
        //doc.add(new TextField(ABSTRACT_FIELD, abstractText, Field.Store.YES));
         
        
        doc.add(new VecTextField(URI_FIELD, uri, Field.Store.YES));
        doc.add(new VecTextField(ABSTRACT_FIELD, abstractText, Field.Store.YES));
        return doc;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the abstractText
     */
    public String getAbstractText() {
        return abstractText;
    }

    /**
     * @param abstractText the abstractText to set
     */
    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

}
