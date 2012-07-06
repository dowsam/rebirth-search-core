/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportServiceAdapter.java 2012-7-6 14:29:20 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

/**
 * The Interface TransportServiceAdapter.
 *
 * @author l.xue.nong
 */
public interface TransportServiceAdapter {

	/**
	 * Received.
	 *
	 * @param size the size
	 */
	void received(long size);

	/**
	 * Sent.
	 *
	 * @param size the size
	 */
	void sent(long size);

	/**
	 * Handler.
	 *
	 * @param action the action
	 * @return the transport request handler
	 */
	TransportRequestHandler handler(String action);

	/**
	 * Removes the.
	 *
	 * @param requestId the request id
	 * @return the transport response handler
	 */
	TransportResponseHandler remove(long requestId);

	/**
	 * Raise node connected.
	 *
	 * @param node the node
	 */
	void raiseNodeConnected(DiscoveryNode node);

	/**
	 * Raise node disconnected.
	 *
	 * @param node the node
	 */
	void raiseNodeDisconnected(DiscoveryNode node);
}
