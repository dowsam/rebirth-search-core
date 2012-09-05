/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core Discovery.java 2012-7-6 14:29:09 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.component.LifecycleComponent;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlock;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.node.service.NodeService;
import cn.com.rebirth.search.core.rest.RestStatus;

/**
 * The Interface Discovery.
 *
 * @author l.xue.nong
 */
public interface Discovery extends LifecycleComponent<Discovery> {

	/** The no master block. */
	final ClusterBlock NO_MASTER_BLOCK = new ClusterBlock(2, "no master", true, true, RestStatus.SERVICE_UNAVAILABLE,
			ClusterBlockLevel.ALL);

	/**
	 * Local node.
	 *
	 * @return the discovery node
	 */
	DiscoveryNode localNode();

	/**
	 * Adds the listener.
	 *
	 * @param listener the listener
	 */
	void addListener(InitialStateDiscoveryListener listener);

	/**
	 * Removes the listener.
	 *
	 * @param listener the listener
	 */
	void removeListener(InitialStateDiscoveryListener listener);

	/**
	 * Node description.
	 *
	 * @return the string
	 */
	String nodeDescription();

	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	void setNodeService(@Nullable NodeService nodeService);

	/**
	 * Publish.
	 *
	 * @param clusterState the cluster state
	 */
	void publish(ClusterState clusterState);
}
