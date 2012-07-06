/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CachedDfSource.java 2012-3-29 15:01:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.dfs;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;


/**
 * The Class CachedDfSource.
 *
 * @author l.xue.nong
 */
public class CachedDfSource extends Searcher {

    /** The dfs. */
    private final AggregatedDfs dfs;

    /** The max doc. */
    private final int maxDoc;

    /**
     * Instantiates a new cached df source.
     *
     * @param dfs the dfs
     * @param similarity the similarity
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CachedDfSource(AggregatedDfs dfs, Similarity similarity) throws IOException {
        this.dfs = dfs;
        setSimilarity(similarity);
        if (dfs.maxDoc() > Integer.MAX_VALUE) {
            maxDoc = Integer.MAX_VALUE;
        } else {
            maxDoc = (int) dfs.maxDoc();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Searcher#docFreq(org.apache.lucene.index.Term)
     */
    public int docFreq(Term term) {
        int df = dfs.dfMap().get(term);
        if (df == -1) {
            return 1;

        }
        return df;
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Searcher#docFreqs(org.apache.lucene.index.Term[])
     */
    public int[] docFreqs(Term[] terms) {
        int[] result = new int[terms.length];
        for (int i = 0; i < terms.length; i++) {
            result[i] = docFreq(terms[i]);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Searcher#maxDoc()
     */
    public int maxDoc() {
        return this.maxDoc;
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Searcher#rewrite(org.apache.lucene.search.Query)
     */
    public Query rewrite(Query query) {
        
        
        
        
        return query;
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Searcher#close()
     */
    public void close() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Searcher#doc(int)
     */
    public Document doc(int i) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Searcher#doc(int, org.apache.lucene.document.FieldSelector)
     */
    public Document doc(int i, FieldSelector fieldSelector) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Searcher#explain(org.apache.lucene.search.Weight, int)
     */
    public Explanation explain(Weight weight, int doc) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Searcher#search(org.apache.lucene.search.Weight, org.apache.lucene.search.Filter, org.apache.lucene.search.Collector)
     */
    public void search(Weight weight, Filter filter, Collector results) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Searcher#search(org.apache.lucene.search.Weight, org.apache.lucene.search.Filter, int)
     */
    public TopDocs search(Weight weight, Filter filter, int n) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Searcher#search(org.apache.lucene.search.Weight, org.apache.lucene.search.Filter, int, org.apache.lucene.search.Sort)
     */
    public TopFieldDocs search(Weight weight, Filter filter, int n, Sort sort) {
        throw new UnsupportedOperationException();
    }

}
