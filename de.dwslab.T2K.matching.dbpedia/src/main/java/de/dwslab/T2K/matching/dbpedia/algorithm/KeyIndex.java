package de.dwslab.T2K.matching.dbpedia.algorithm;

import de.dwslab.T2K.index.IIndex;
import de.dwslab.T2K.matching.dbpedia.LuceneBlocking;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;

public class KeyIndex {

    private IIndex luceneIndex;
    public IIndex getLuceneIndex() {
        return luceneIndex;
    }
    public void setLuceneIndex(IIndex luceneIndex) {
        this.luceneIndex = luceneIndex;
    }
    
    private LuceneBlocking<TableRow> luceneBlocking;
    public LuceneBlocking<TableRow> getLuceneBlocking() {
        return luceneBlocking;
    }
    public void setLuceneBlocking(LuceneBlocking<TableRow> luceneBlocking) {
        this.luceneBlocking = luceneBlocking;
    }
}
