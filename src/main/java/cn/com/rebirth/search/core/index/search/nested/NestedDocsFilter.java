/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NestedDocsFilter.java 2012-7-6 14:29:16 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.nested;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.PrefixFilter;

import cn.com.rebirth.search.core.index.mapper.internal.TypeFieldMapper;

/**
 * The Class NestedDocsFilter.
 *
 * @author l.xue.nong
 */
public class NestedDocsFilter extends Filter {

	/** The Constant INSTANCE. */
	public static final NestedDocsFilter INSTANCE = new NestedDocsFilter();

	/** The filter. */
	private final PrefixFilter filter = new PrefixFilter(new Term(TypeFieldMapper.NAME, "__"));

	/** The hash code. */
	private final int hashCode = filter.hashCode();

	/**
	 * Instantiates a new nested docs filter.
	 */
	private NestedDocsFilter() {

	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		return filter.getDocIdSet(reader);
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