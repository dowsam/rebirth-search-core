/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AdminClient.java 2012-3-29 15:01:54 l.xue.nong$$
 */


package cn.com.rebirth.search.core.client;


/**
 * The Interface AdminClient.
 *
 * @author l.xue.nong
 */
public interface AdminClient {

	
	/**
	 * Cluster.
	 *
	 * @return the cluster admin client
	 */
	ClusterAdminClient cluster();

	
	/**
	 * Indices.
	 *
	 * @return the indices admin client
	 */
	IndicesAdminClient indices();
}
