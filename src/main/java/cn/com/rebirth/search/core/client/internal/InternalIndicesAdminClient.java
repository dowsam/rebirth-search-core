/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalIndicesAdminClient.java 2012-3-29 15:02:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.client.internal;

import cn.com.rebirth.search.core.client.IndicesAdminClient;
import cn.com.rebirth.search.core.threadpool.ThreadPool;


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
