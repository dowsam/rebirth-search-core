/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalClient.java 2012-7-6 14:30:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client.internal;

import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.client.Client;

/**
 * The Interface InternalClient.
 *
 * @author l.xue.nong
 */
public interface InternalClient extends Client {

	/**
	 * Thread pool.
	 *
	 * @return the thread pool
	 */
	ThreadPool threadPool();
}
