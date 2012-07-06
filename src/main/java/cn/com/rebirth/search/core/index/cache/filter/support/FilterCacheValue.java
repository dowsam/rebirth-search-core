/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FilterCacheValue.java 2012-3-29 15:01:48 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.cache.filter.support;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;

import cn.com.rebirth.search.commons.lucene.docset.DocSet;
import cn.com.rebirth.search.commons.lucene.docset.DocSets;


/**
 * The Class FilterCacheValue.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public class FilterCacheValue<T> {

	
	/** The value. */
	private final T value;

	
	/**
	 * Instantiates a new filter cache value.
	 *
	 * @param value the value
	 */
	public FilterCacheValue(T value) {
		this.value = value;
	}

	
	/**
	 * Value.
	 *
	 * @return the t
	 */
	public T value() {
		return value;
	}

	
	/**
	 * Cacheable.
	 *
	 * @param reader the reader
	 * @param set the set
	 * @return the doc set
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static DocSet cacheable(IndexReader reader, DocIdSet set) throws IOException {
		if (set == null) {
			return DocSet.EMPTY_DOC_SET;
		}
		if (set == DocIdSet.EMPTY_DOCIDSET) {
			return DocSet.EMPTY_DOC_SET;
		}

		DocIdSetIterator it = set.iterator();
		if (it == null) {
			return DocSet.EMPTY_DOC_SET;
		}
		int doc = it.nextDoc();
		if (doc == DocIdSetIterator.NO_MORE_DOCS) {
			return DocSet.EMPTY_DOC_SET;
		}
		return DocSets.cacheable(reader, set);
	}
}