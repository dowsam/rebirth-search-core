/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DiscoveryNodesProvider.java 2012-3-29 15:02:34 l.xue.nong$$
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
