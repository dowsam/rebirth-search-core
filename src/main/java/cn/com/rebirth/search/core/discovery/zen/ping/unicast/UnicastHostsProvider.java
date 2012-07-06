/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core UnicastHostsProvider.java 2012-3-29 15:02:34 l.xue.nong$$
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
