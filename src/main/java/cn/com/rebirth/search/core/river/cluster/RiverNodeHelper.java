/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RiverNodeHelper.java 2012-3-29 15:01:30 l.xue.nong$$
 */


package cn.com.rebirth.search.core.river.cluster;

import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.river.RiverName;


/**
 * The Class RiverNodeHelper.
 *
 * @author l.xue.nong
 */
public class RiverNodeHelper {

	
	/**
	 * Checks if is river node.
	 *
	 * @param node the node
	 * @return true, if is river node
	 */
	public static boolean isRiverNode(DiscoveryNode node) {
		
		if (node.clientNode()) {
			return false;
		}
		String river = node.attributes().get("river");
		
		if (river == null) {
			return true;
		}
		if ("_none_".equals(river)) {
			return false;
		}
		
		return true;
	}

	
	/**
	 * Checks if is river node.
	 *
	 * @param node the node
	 * @param riverName the river name
	 * @return true, if is river node
	 */
	public static boolean isRiverNode(DiscoveryNode node, RiverName riverName) {
		if (!isRiverNode(node)) {
			return false;
		}
		String river = node.attributes().get("river");
		
		return river == null || river.contains(riverName.type()) || river.contains(riverName.name());
	}
}
