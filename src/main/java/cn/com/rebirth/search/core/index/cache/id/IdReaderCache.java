/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IdReaderCache.java 2012-3-29 15:01:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.id;

import cn.com.rebirth.search.commons.BytesWrap;

/**
 * The Interface IdReaderCache.
 *
 * @author l.xue.nong
 */
public interface IdReaderCache {

	/**
	 * Reader cache key.
	 *
	 * @return the object
	 */
	Object readerCacheKey();

	/**
	 * Type.
	 *
	 * @param type the type
	 * @return the id reader type cache
	 */
	IdReaderTypeCache type(String type);

	/**
	 * Parent id by doc.
	 *
	 * @param type the type
	 * @param docId the doc id
	 * @return the bytes wrap
	 */
	BytesWrap parentIdByDoc(String type, int docId);

	/**
	 * Doc by id.
	 *
	 * @param type the type
	 * @param id the id
	 * @return the int
	 */
	int docById(String type, BytesWrap id);
}
