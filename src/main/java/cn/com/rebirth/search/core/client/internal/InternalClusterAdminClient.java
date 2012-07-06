/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalClusterAdminClient.java 2012-3-29 15:01:30 l.xue.nong$$
 */


package cn.com.rebirth.search.core.client.internal;

import cn.com.rebirth.search.core.client.ClusterAdminClient;
import cn.com.rebirth.search.core.threadpool.ThreadPool;


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
