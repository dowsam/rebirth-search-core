/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeClosedException.java 2012-7-6 14:30:13 l.xue.nong$$
 */

package cn.com.rebirth.search.core.node;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

/**
 * The Class NodeClosedException.
 *
 * @author l.xue.nong
 */
public class NodeClosedException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3611865037540089354L;

	/**
	 * Instantiates a new node closed exception.
	 *
	 * @param node the node
	 */
	public NodeClosedException(DiscoveryNode node) {
		super("node closed " + node);
	}
}
