/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.index.pageResolving;

import de.dwslab.T2K.index.IIndex;
import de.dwslab.index.management.IndexManagerBase;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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
