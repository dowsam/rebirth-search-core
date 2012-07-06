/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IdReaderTypeCache.java 2012-3-29 15:01:54 l.xue.nong$$
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
