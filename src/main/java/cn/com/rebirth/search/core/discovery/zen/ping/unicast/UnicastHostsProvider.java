/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core UnicastHostsProvider.java 2012-7-6 14:30:03 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery.zen.ping.unicast;

import java.util.List;

import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

/**
 * The Interface UnicastHostsProvider.
 *
 * @author l.xue.nong
 */
public interface UnicastHostsProvider {

	/**
	 * Builds the dynamic nodes.
	 *
	 * @return the list
	 */
	List<DiscoveryNode> buildDynamicNodes();
}
