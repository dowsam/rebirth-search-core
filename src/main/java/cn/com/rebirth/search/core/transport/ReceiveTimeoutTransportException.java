/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ReceiveTimeoutTransportException.java 2012-7-6 14:29:31 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

/**
 * The Class ReceiveTimeoutTransportException.
 *
 * @author l.xue.nong
 */
public class ReceiveTimeoutTransportException extends ActionTransportException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -907184560720859424L;

	/**
	 * Instantiates a new receive timeout transport exception.
	 *
	 * @param node the node
	 * @param action the action
	 * @param msg the msg
	 */
	public ReceiveTimeoutTransportException(DiscoveryNode node, String action, String msg) {
		super(node.name(), node.address(), action, msg, null);
	}
}
