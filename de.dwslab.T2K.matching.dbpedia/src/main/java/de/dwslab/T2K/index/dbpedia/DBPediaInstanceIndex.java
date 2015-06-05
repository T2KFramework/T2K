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
package de.dwslab.T2K.index.dbpedia;

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

import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.index.io.StringNormaliser;
import de.dwslab.T2K.index.management.IndexManagerBase;

public class DBPediaInstanceIndex extends IndexManagerBase {

    private boolean verbose = false;
    public boolean isVerbose() {
        return verbose;
    }
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
	public DBPediaInstanceIndex(IIndex index, String defaultField) {
		super(index, defaultField);
	}

	private boolean removeBrackets = false;
	public boolean isRemoveBrackets() {
        return removeBrackets;
    }
	public void setRemoveBrackets(boolean removeBrackets) {
        this.removeBrackets = removeBrackets;
    }
	
	   public List<DBpediaIndexEntry> searchMany(Collection<?> labels) {
	        long start, setup=0, search=0, result=0;
	        start = System.currentTimeMillis();
	        List<DBpediaIndexEntry> results = new LinkedList<DBpediaIndexEntry>();

	        IndexSearcher indexSearcher = getIndex().getIndexSearcher();

	        QueryParser queryParser = getQueryParserFromCache();
	        
	        try {           
	            Query q = null;
	            
	            q = new BooleanQuery();
	            
	            for(Object o : labels) {
	                Query q0 = null;
	                String value = QueryParserBase.escape((String)o);
    	            if(!isSearchExactMatches()) {
    	                
    	                //value = StringNormaliser.normalise(value, isRemoveBrackets());
    	                
    	                //String[] tokens = value.split("\\s+");
    	                
    	                List<String> tokens = StringNormaliser.tokenise(value, isRemoveBrackets());
    	                
    	                StringBuilder sb = new StringBuilder();
    	                
    	                for(String token : tokens) {
    	                    sb.append(token);
    	                    
    	                    if(getMaxEditDistance()>0) {
    	                        sb.append("~");
    	                        sb.append(getMaxEditDistance());
    	                    }
    	                    sb.append(" ");
    	                }
    	                
    	                value = sb.toString();
    	                
    	                if(value.trim().length()>0) {
    	                    q0 = queryParser.parse(value);
    	                }
    	            } else {
    	                if(value.trim().length()>0) {
    	                    q0 = new TermQuery(new Term(getDefaultField(), value));
    	                }
    	            }
    	            
    	            if(q0!=null) {
    	                ((BooleanQuery)q).add(q0, Occur.SHOULD);
    	            }
	            }
	            
	            if(q!=null) {
	                if(getFilterValues()!=null && getFilterValues().size()>0) {
	                    BooleanQuery filter = new BooleanQuery();
	                    
	                    for(String s : getFilterValues()) {
	                        filter.add(new TermQuery(new Term(getFilterField(), s)), Occur.SHOULD);
	                    }
	                    
	                    BooleanQuery all = new BooleanQuery();
	                    all.add(q, Occur.MUST);
	                    all.add(filter, Occur.MUST);
	                    
	                    q = all;
	                }
	                
	                if(isVerbose()) {
	                    System.out.println("Query: \n" + labels.toString() + "\n" + q.toString());
	                }
	                
	                setup = System.currentTimeMillis() - start;
	                start = System.currentTimeMillis();
	                
	                int numResults = getNumRetrievedDocsFromIndex();
	                ScoreDoc[] hits = indexSearcher.search(q, numResults).scoreDocs;
	                
	                search = System.currentTimeMillis() - start;
	                start = System.currentTimeMillis();
	                
	                if(hits != null)
	                {
	                    if(isVerbose()) {
	                        System.out.println(" found " + hits.length + " hits");
	                    }
	                    for (int i = 0; i < hits.length; i++) {
	                        
	                        Document doc = indexSearcher.doc(hits[i].doc);
	        
	                        DBpediaIndexEntry e = DBpediaIndexEntry.fromDocument(doc);

	                        if(isVerbose()) {
	                            System.out.println(e.getClass_label() + ": " + e.getLabel());
	                        }
	                        
	                        results.add(e);
	                    }
	                }
	            } else if(isVerbose()) {
                    System.out.println(String.format("Empty query for '%s'", labels));
                }
	            
	            result = System.currentTimeMillis() - start;

	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (ParseException e1) {
	            System.err.println(String.format("Parse exception for label '%s'", labels));
	            e1.printStackTrace();
	        }
	        if(isVerbose()) {
	            System.out.println(" returning " + results.size() + " documents");
	            System.out.println(String.format("setup: %d\tsearch: %d\tload: %d", setup, search, result));
	        }
	        return results;
	    }
	
	public List<DBpediaIndexEntry> search(String label) {
	    long start, setup=0, search=0, result=0;
	    start = System.currentTimeMillis();
		List<DBpediaIndexEntry> results = new LinkedList<DBpediaIndexEntry>();

		IndexSearcher indexSearcher = getIndex().getIndexSearcher();

		QueryParser queryParser = getQueryParserFromCache();
		
		try {			
			String value = QueryParserBase.escape(label); 
			Query q = null;
			
			if(!isSearchExactMatches()) {
    			
			    //value = StringNormaliser.normalise(value, isRemoveBrackets());
			    
			    //String[] tokens = value.split("\\s+");
			    
			    //List<String> tokens = StringNormaliser.tokenise(value, isRemoveBrackets());
			    List<String> tokens = StringNormaliser.tokenise(value, true);
			    
			    StringBuilder sb = new StringBuilder();
			    
			    for(String token : tokens) {
			        sb.append(token);
			        
			        if(getMaxEditDistance()>0) {
			            sb.append("~");
			            sb.append(getMaxEditDistance());
			        }
			        sb.append(" ");
			    }
			    
			    value = sb.toString();
    			
    			if(value.trim().length()>0) {
    			    q = queryParser.parse(value);
    			}
			} else {
			    if(value.trim().length()>0) {
			        q = new TermQuery(new Term(getDefaultField(), value));
			    }
			}
			
			if(q!=null) {
    			if(getFilterValues()!=null && getFilterValues().size()>0) {
    			    BooleanQuery filter = new BooleanQuery();
    			    
    			    for(String s : getFilterValues()) {
    			        filter.add(new TermQuery(new Term(getFilterField(), s)), Occur.SHOULD);
    			    }
    			    
    			    BooleanQuery all = new BooleanQuery();
    			    all.add(q, Occur.MUST);
    			    all.add(filter, Occur.MUST);
    			    
    			    q = all;
    			}
    			
    			if(isVerbose()) {
    			    System.out.println("Query: \n" + value + "\n" + q.toString());
    			}
    			
    			setup = System.currentTimeMillis() - start;
    			start = System.currentTimeMillis();
    			
    			int numResults = getNumRetrievedDocsFromIndex();
    			ScoreDoc[] hits = indexSearcher.search(q, numResults).scoreDocs;
    			
    			search = System.currentTimeMillis() - start;
    			start = System.currentTimeMillis();
    			
    			if(hits != null)
    			{
    			    if(isVerbose()) {
    			        System.out.println(" found " + hits.length + " hits");
    			    }
    				for (int i = 0; i < hits.length; i++) {
    					
    					Document doc = indexSearcher.doc(hits[i].doc);
    	
    					DBpediaIndexEntry e = DBpediaIndexEntry.fromDocument(doc);

    					if(isVerbose()) {
    					    System.out.println(e.getClass_label() + ": " + e.getLabel());
    					}
    					
    					results.add(e);
    				}
    			}
			} else {
			    if(isVerbose()) {
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
		if(isVerbose()) {
		    System.out.println(" returning " + results.size() + " documents");
		    System.out.println(String.format("setup: %d\tsearch: %d\tload: %d", setup, search, result));
		}
		return results;
	}

}
