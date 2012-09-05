/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FilterCache.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.filter;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.component.CloseableComponent;
import cn.com.rebirth.search.core.index.IndexComponent;

/**
 * The Interface FilterCache.
 *
 * @author l.xue.nong
 */
public interface FilterCache extends IndexComponent, CloseableComponent {

	/**
	 * The Class EntriesStats.
	 *
	 * @author l.xue.nong
	 */
	static class EntriesStats {

		/** The size in bytes. */
		public final long sizeInBytes;

		/** The count. */
		public final long count;

		/**
		 * Instantiates a new entries stats.
		 *
		 * @param sizeInBytes the size in bytes
		 * @param count the count
		 */
		public EntriesStats(long sizeInBytes, long count) {
			this.sizeInBytes = sizeInBytes;
			this.count = count;
		}
	}

	/**
	 * Type.
	 *
	 * @return the string
	 */
	String type();

	/**
	 * Cache.
	 *
	 * @param filterToCache the filter to cache
	 * @return the filter
	 */
	Filter cache(Filter filterToCache);

	/**
	 * Checks if is cached.
	 *
	 * @param filter the filter
	 * @return true, if is cached
	 */
	boolean isCached(Filter filter);

	/**
	 * Clear.
	 *
	 * @param reader the reader
	 */
	void clear(IndexReader reader);

	/**
	 * Clear.
	 */
	void clear();

	/**
	 * Entries stats.
	 *
	 * @return the entries stats
	 */
	EntriesStats entriesStats();

	/**
	 * Evictions.
	 *
	 * @return the long
	 */
	long evictions();
}
