/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeDisconnectedException.java 2012-7-6 14:29:42 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

/**
 * The Class NodeDisconnectedException.
 *
 * @author l.xue.nong
 */
public class NodeDisconnectedException extends ConnectTransportException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8204298392989604548L;

	/**
	 * Instantiates a new node disconnected exception.
	 *
	 * @param node the node
	 * @param action the action
	 */
	public NodeDisconnectedException(DiscoveryNode node, String action) {
		super(node, "disconnected", action, null);
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#fillInStackTrace()
	 */
	@Override
	public Throwable fillInStackTrace() {
		return null;
	}
}