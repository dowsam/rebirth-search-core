/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ReceiveTimeoutTransportException.java 2012-3-29 15:01:13 l.xue.nong$$
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
