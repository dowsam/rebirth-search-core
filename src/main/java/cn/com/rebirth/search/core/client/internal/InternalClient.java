/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalClient.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.client.internal;

import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.threadpool.ThreadPool;


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
