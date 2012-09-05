/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IdCache.java 2012-7-6 14:29:26 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.id;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.component.CloseableComponent;
import cn.com.rebirth.search.core.index.IndexComponent;

/**
 * The Interface IdCache.
 *
 * @author l.xue.nong
 */
public interface IdCache extends IndexComponent, CloseableComponent, Iterable<IdReaderCache> {

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
	 * Refresh.
	 *
	 * @param readers the readers
	 * @throws Exception the exception
	 */
	void refresh(IndexReader[] readers) throws Exception;

	/**
	 * Reader.
	 *
	 * @param reader the reader
	 * @return the id reader cache
	 */
	IdReaderCache reader(IndexReader reader);
}
