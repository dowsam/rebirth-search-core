/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BloomCache.java 2012-7-6 14:30:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.bloom;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.component.CloseableComponent;
import cn.com.rebirth.search.commons.bloom.BloomFilter;
import cn.com.rebirth.search.core.index.IndexComponent;

/**
 * The Interface BloomCache.
 *
 * @author l.xue.nong
 */
public interface BloomCache extends IndexComponent, CloseableComponent {

	/**
	 * Filter.
	 *
	 * @param reader the reader
	 * @param fieldName the field name
	 * @param asyncLoad the async load
	 * @return the bloom filter
	 */
	BloomFilter filter(IndexReader reader, String fieldName, boolean asyncLoad);

	/**
	 * Clear.
	 */
	void clear();

	/**
	 * Clear.
	 *
	 * @param reader the reader
	 */
	void clear(IndexReader reader);

	/**
	 * Size in bytes.
	 *
	 * @return the long
	 */
	long sizeInBytes();

	/**
	 * Size in bytes.
	 *
	 * @param fieldName the field name
	 * @return the long
	 */
	long sizeInBytes(String fieldName);
}