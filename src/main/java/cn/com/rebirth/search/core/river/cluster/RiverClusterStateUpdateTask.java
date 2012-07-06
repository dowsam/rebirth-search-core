/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiverClusterStateUpdateTask.java 2012-7-6 14:30:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river.cluster;

/**
 * The Interface RiverClusterStateUpdateTask.
 *
 * @author l.xue.nong
 */
public interface RiverClusterStateUpdateTask {

	/**
	 * Execute.
	 *
	 * @param currentState the current state
	 * @return the river cluster state
	 */
	RiverClusterState execute(RiverClusterState currentState);
}
