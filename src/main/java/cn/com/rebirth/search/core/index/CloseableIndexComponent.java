/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CloseableIndexComponent.java 2012-3-29 15:00:46 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index;

import cn.com.rebirth.commons.exception.RestartException;



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
	 * @throws SumMallSearchException the sum mall search exception
	 */
	void close(boolean delete) throws RestartException;
}