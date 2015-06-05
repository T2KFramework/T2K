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

package de.mannheim.uni.index.io;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;

import de.dwslab.T2K.index.io.DefaultIndex;
import junit.framework.TestCase;

public class DefaultIndexTest extends TestCase {

    public void testIndexWriter() throws IOException {
        
        DefaultIndex idx = new DefaultIndex("out/test");
        
        IndexWriter w = idx.getIndexWriter();
        
        Document d = new Document();
        d.add(new StringField("field1", "value1", Store.YES));
        d.add(new StringField("field2", "value2", Store.YES));
        
        w.addDocument(d);
        
        idx.closeIndexWriter();
        
        w = idx.getIndexWriter();
        
        d = new Document();
        d.add(new StringField("field1", "value3", Store.YES));
        d.add(new StringField("field2", "value4", Store.YES));
        
        w.addDocument(d);
        
        idx.closeIndexWriter();
    }
    
}
