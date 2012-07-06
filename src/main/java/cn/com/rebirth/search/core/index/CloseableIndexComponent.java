/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CloseableIndexComponent.java 2012-7-6 14:29:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Interface CloseableIndexComponent.
 *
 * @author l.xue.nong
 */
public interface CloseableIndexComponent {

	/**
	 * Close.
	 *
	 * @param delete the delete
	 * @throws RebirthException the rebirth exception
	 */
	void close(boolean delete) throws RebirthException;
}