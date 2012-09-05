/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalClusterAdminClient.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client.internal;

import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.client.ClusterAdminClient;

/**
 * The Interface InternalClusterAdminClient.
 *
 * @author l.xue.nong
 */
public interface InternalClusterAdminClient extends ClusterAdminClient {

	/**
	 * Thread pool.
	 *
	 * @return the thread pool
	 */
	ThreadPool threadPool();
}
