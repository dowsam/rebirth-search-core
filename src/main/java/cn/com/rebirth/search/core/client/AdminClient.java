/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AdminClient.java 2012-7-6 14:29:54 l.xue.nong$$
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
