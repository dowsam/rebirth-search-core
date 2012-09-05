/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalIndicesAdminClient.java 2012-7-6 14:30:29 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client.internal;

import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Interface InternalIndicesAdminClient.
 *
 * @author l.xue.nong
 */
public interface InternalIndicesAdminClient extends IndicesAdminClient {

	/**
	 * Thread pool.
	 *
	 * @return the thread pool
	 */
	ThreadPool threadPool();
}
