/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterStateUpdateTask.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster;

/**
 * The Interface ClusterStateUpdateTask.
 *
 * @author l.xue.nong
 */
public interface ClusterStateUpdateTask {

	/**
	 * Execute.
	 *
	 * @param currentState the current state
	 * @return the cluster state
	 */
	ClusterState execute(ClusterState currentState);
}
