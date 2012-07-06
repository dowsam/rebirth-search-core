/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DiscoveryNodesProvider.java 2012-7-6 14:29:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery.zen;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.node.service.NodeService;

/**
 * The Interface DiscoveryNodesProvider.
 *
 * @author l.xue.nong
 */
public interface DiscoveryNodesProvider {

	/**
	 * Nodes.
	 *
	 * @return the discovery nodes
	 */
	DiscoveryNodes nodes();

	/**
	 * Node service.
	 *
	 * @return the node service
	 */
	@Nullable
	NodeService nodeService();
}
