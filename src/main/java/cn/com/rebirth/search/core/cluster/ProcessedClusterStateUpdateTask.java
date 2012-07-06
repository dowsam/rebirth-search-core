/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ProcessedClusterStateUpdateTask.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster;

/**
 * The Interface ProcessedClusterStateUpdateTask.
 *
 * @author l.xue.nong
 */
public interface ProcessedClusterStateUpdateTask extends ClusterStateUpdateTask {

	/**
	 * Cluster state processed.
	 *
	 * @param clusterState the cluster state
	 */
	void clusterStateProcessed(ClusterState clusterState);
}
