/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IdReaderTypeCache.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.id;

import cn.com.rebirth.search.commons.BytesWrap;

/**
 * The Interface IdReaderTypeCache.
 *
 * @author l.xue.nong
 */
public interface IdReaderTypeCache {

	/**
	 * Parent id by doc.
	 *
	 * @param docId the doc id
	 * @return the bytes wrap
	 */
	BytesWrap parentIdByDoc(int docId);

	/**
	 * Doc by id.
	 *
	 * @param id the id
	 * @return the int
	 */
	int docById(BytesWrap id);
}
