package de.dwslab.T2K.matching.dbpedia.tableToTable;

/**
 *
 * Copyright (C) 2015 Data and Web Science Group, University of Mannheim,
 * Germany (code@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.io.StringNormaliser;
import de.dwslab.index.management.IndexManagerBase;
import de.uni_mannheim.informatik.dws.t2k.normalisation.StringNormalizer;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class WebTableValueIndex extends IndexManagerBase {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private boolean verbose = false;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public WebTableValueIndex(IIndex index, String defaultField) {
        super(index, defaultField);
    }

    private boolean removeBrackets = false;

    public boolean isRemoveBrackets() {
        return removeBrackets;
    }

    public void setRemoveBrackets(boolean removeBrackets) {
        this.removeBrackets = removeBrackets;
    }

    public List<WebTableIndexEntry> searchMany(String labels) {
        long start, setup = 0, search = 0, result = 0;
        start = System.currentTimeMillis();
        List<WebTableIndexEntry> results = new LinkedList<WebTableIndexEntry>();

        IndexSearcher indexSearcher = getIndex().getIndexSearcher();

        QueryParser queryParser = getQueryParserFromCache();

        try {
            Query q = null;

            q = new BooleanQuery();

            //        for(String lbl : labels) {
            Query q0 = null;
            String value = QueryParserBase.escape(labels);
            if (!isSearchExactMatches()) {
                List<String> tokens = StringNormaliser.tokenise(value, isRemoveBrackets());

                StringBuilder sb = new StringBuilder();

                for (String token : tokens) {
                    sb.append(token);

                    if (getMaxEditDistance() > 0) {
                        sb.append("~");
                        sb.append(getMaxEditDistance());
                    }
                    sb.append(" ");
                }

                value = sb.toString();

                if (value.trim().length() > 0) {
                    q0 = queryParser.parse(value);
                }
            } else {
                if (value.trim().length() > 0) {
                    q0 = new TermQuery(new Term(getDefaultField(), value));
                }
            }

            if (q0 != null) {
                ((BooleanQuery) q).add(q0, Occur.SHOULD);
            }
            //        }

            if (q != null) {
                if (getFilterValues() != null && getFilterValues().size() > 0) {
                    BooleanQuery filter = new BooleanQuery();

                    for (String s : getFilterValues()) {
                        filter.add(new TermQuery(new Term(getFilterField(), s)), Occur.SHOULD);
                    }

                    BooleanQuery all = new BooleanQuery();
                    all.add(q, Occur.MUST);
                    all.add(filter, Occur.MUST);

                    q = all;
                }

                if (isVerbose()) {
                    System.out.println("Query: \n" + labels + "\n" + q.toString());
                }

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

                        WebTableIndexEntry e = WebTableIndexEntry.fromDocument(doc);

                        if (isVerbose()) {
                            System.out.println(e.getTable() + ": " + e.getDbpediaClass() + ": " + e.getEntityLabels());
                        }

                        results.add(e);
                    }
                }
            }

            result = System.currentTimeMillis() - start;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e1) {
            System.err.println(String.format("Parse exception for label '%s'", labels));
            e1.printStackTrace();
        }
        if (isVerbose()) {
            System.out.println(" returning " + results.size() + " documents");
            System.out.println(String.format("setup: %d\tsearch: %d\tload: %d", setup, search, result));
        }
        return results;
    }

    public List<WebTableIndexEntry> search(String label) {
        long start, setup = 0, search = 0, result = 0;
        start = System.currentTimeMillis();
        List<WebTableIndexEntry> results = new LinkedList<WebTableIndexEntry>();
        BooleanQuery.setMaxClauseCount(10000);
        IndexSearcher indexSearcher = getIndex().getIndexSearcher();

        QueryParser queryParser = getNewQueryParser();
        try {
            String value = QueryParserBase.escape(label);
            Query q = null;

            if (!isSearchExactMatches()) {

                value = StringNormalizer.normaliseValue(value, true);

                //String[] tokens = value.split("\\s+");
                //List<String> tokens = StringNormaliser.tokenise(value, isRemoveBrackets());
                //List<String> tokens = StringNormaliser.tokenise(value, true);
                List<String> tokens = StringNormalizer.tokenise(value, true);
                
                StringBuilder sb = new StringBuilder();

                for (String token : tokens) {
                    if(token.length()<3) {
                        continue;
                    }
                    sb.append(token);

                    if (getMaxEditDistance() > 0) {
                        sb.append("~");
                        sb.append(getMaxEditDistance());
                    }
                    sb.append(" ");
                }

                value = sb.toString();

                if (value.trim().length() > 0) {
                    q = queryParser.parse(value);
                }
            } else {
                if (value.trim().length() > 0) {
                    q = new TermQuery(new Term(getDefaultField(), value));
                }
            }

            if (q != null) {
                if (getFilterValues() != null && getFilterValues().size() > 0) {
                    BooleanQuery filter = new BooleanQuery();

                    for (String s : getFilterValues()) {
                        filter.add(new TermQuery(new Term(getFilterField(), s)), Occur.SHOULD);
                    }

                    BooleanQuery all = new BooleanQuery();                    
                    all.add(q, Occur.MUST);
                    all.add(filter, Occur.MUST);

                    q = all;

                }

                if (isVerbose()) {
                    System.out.println("Query: \n" + value + "\n" + q.toString());
                }

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

                        WebTableIndexEntry e = WebTableIndexEntry.fromDocument(doc);

                        if (isVerbose()) {
                            System.out.println(e.getTable() + ": " + e.getDbpediaClass() + ": " + e.getEntityLabels());
                        }

                        results.add(e);
                    }
                }
            } else {
                if (isVerbose()) {
                    System.out.println(String.format("Empty query for '%s'", label));
                }
            }

            result = System.currentTimeMillis() - start;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e1) {
            System.err.println(String.format("Parse exception for label '%s'", label));
            e1.printStackTrace();
        }
        if (isVerbose()) {
            System.out.println(" returning " + results.size() + " documents");
            System.out.println(String.format("setup: %d\tsearch: %d\tload: %d", setup, search, result));
        }
        return results;
    }

}
