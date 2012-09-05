/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterService.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster;

import cn.com.rebirth.commons.component.LifecycleComponent;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.cluster.block.ClusterBlock;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.routing.operation.OperationRouting;

/**
 * The Interface ClusterService.
 *
 * @author l.xue.nong
 */
public interface ClusterService extends LifecycleComponent<ClusterService> {

	/**
	 * Local node.
	 *
	 * @return the discovery node
	 */
	DiscoveryNode localNode();

	/**
	 * State.
	 *
	 * @return the cluster state
	 */
	ClusterState state();

	/**
	 * Adds the initial state block.
	 *
	 * @param block the block
	 * @throws RebirthIllegalStateException the rebirth illegal state exception
	 */
	void addInitialStateBlock(ClusterBlock block) throws RebirthIllegalStateException;

	/**
	 * Operation routing.
	 *
	 * @return the operation routing
	 */
	OperationRouting operationRouting();

	/**
	 * Adds the first.
	 *
	 * @param listener the listener
	 */
	void addFirst(ClusterStateListener listener);

	/**
	 * Adds the last.
	 *
	 * @param listener the listener
	 */
	void addLast(ClusterStateListener listener);

	/**
	 * Adds the.
	 *
	 * @param listener the listener
	 */
	void add(ClusterStateListener listener);

	/**
	 * Removes the.
	 *
	 * @param listener the listener
	 */
	void remove(ClusterStateListener listener);

	/**
	 * Adds the.
	 *
	 * @param timeout the timeout
	 * @param listener the listener
	 */
	void add(TimeValue timeout, TimeoutClusterStateListener listener);

	/**
	 * Submit state update task.
	 *
	 * @param source the source
	 * @param updateTask the update task
	 */
	void submitStateUpdateTask(final String source, final ClusterStateUpdateTask updateTask);
}
