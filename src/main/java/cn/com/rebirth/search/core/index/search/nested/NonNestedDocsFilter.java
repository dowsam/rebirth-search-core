/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NonNestedDocsFilter.java 2012-3-29 15:02:25 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.search.nested;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.PrefixFilter;
import org.apache.lucene.util.FixedBitSet;

import cn.com.rebirth.search.core.index.mapper.internal.TypeFieldMapper;


/**
 * The Class NonNestedDocsFilter.
 *
 * @author l.xue.nong
 */
public class NonNestedDocsFilter extends Filter {

    
    /** The Constant INSTANCE. */
    public static final NonNestedDocsFilter INSTANCE = new NonNestedDocsFilter();

    
    /** The filter. */
    private final PrefixFilter filter = new PrefixFilter(new Term(TypeFieldMapper.NAME, "__"));

    
    /** The hash code. */
    private final int hashCode = filter.hashCode();

    
    /**
     * Instantiates a new non nested docs filter.
     */
    private NonNestedDocsFilter() {

    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
     */
    @Override
    public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
        DocIdSet docSet = filter.getDocIdSet(reader);
        if (docSet == null || docSet == DocIdSet.EMPTY_DOCIDSET) {
            
            
            docSet = new FixedBitSet(reader.maxDoc());
        }
        ((FixedBitSet) docSet).flip(0, reader.maxDoc());
        return docSet;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj == INSTANCE;
    }
}