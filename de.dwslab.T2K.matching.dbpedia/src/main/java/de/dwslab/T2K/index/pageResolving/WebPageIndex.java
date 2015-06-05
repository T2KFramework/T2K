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

import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.management.IndexManagerBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;

/**
 *
 * @author domi
 */
public class WebPageIndex extends IndexManagerBase {

    public WebPageIndex(IIndex index, String defaultField) {
        super(index, defaultField);
    }

    private boolean verbose = false;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public List<WebPageEntry> search(String id) {
        long start, setup = 0, search = 0, result = 0;
        start = System.currentTimeMillis();
        List<WebPageEntry> results = new LinkedList<>();

        IndexSearcher indexSearcher = getIndex().getIndexSearcher();
        
        try {
            String value = QueryParserBase.escape(id);
            Query q = null;
            if (value.trim().length() > 0) {
                q = new TermQuery(new Term(getDefaultField(), value));
            }

            if (q != null) {

//                if (isVerbose()) {
                System.out.println("Query: \n" + value + "\n" + q.toString());
//                }

                setup = System.currentTimeMillis() - start;
                start = System.currentTimeMillis();

                int numResults = getNumRetrievedDocsFromIndex();
                ScoreDoc[] hits = indexSearcher.search(q, numResults).scoreDocs;

                search = System.currentTimeMillis() - start;
                start = System.currentTimeMillis();

                if (hits != null) {
                    if (isVerbose()) {
                        System.out.println(" found " + hits.length + " hits");
                    }
                    for (int i = 0; i < hits.length; i++) {
                        Document doc = indexSearcher.doc(hits[i].doc);
                        WebPageEntry e = WebPageEntry.fromDocument(doc);
                        results.add(e);
                    }
                }
            } else {
                if (isVerbose()) {
                    System.out.println(String.format("Empty query for '%s'", id));
                }
            }

            result = System.currentTimeMillis() - start;

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isVerbose()) {
            System.out.println(" returning " + results.size() + " documents");
            System.out.println(String.format("setup: %d\tsearch: %d\tload: %d", setup, search, result));
        }
        return results;
    }

    public String tokenise(String s) {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
        List<String> result = new ArrayList<String>();

        try {
            // resolve non unicode chars
            s = StringEscapeUtils.unescapeJava(s);
                
            // tokenise
            TokenStream stream = analyzer.tokenStream(getDefaultField(), s);
            stream.reset();
                        
            // enumerate tokens
            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
            stream.close();
        } catch (IOException e) {
            // not thrown b/c we're using a string reader...
        }
        
        analyzer.close();
        
        return StringUtils.join(result, " ");
    }
    
}
