package de.dwslab.T2K.index;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

public interface IIndex {
	IndexSearcher getIndexSearcher();
	IndexWriter getIndexWriter();
	void closeIndexWriter();
	void closeIndexReader();
	int getNmDocs();
}
