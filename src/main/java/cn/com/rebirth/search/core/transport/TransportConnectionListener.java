/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportConnectionListener.java 2012-7-6 14:30:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

/**
 * The listener interface for receiving transportConnection events.
 * The class that is interested in processing a transportConnection
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addTransportConnectionListener<code> method. When
 * the transportConnection event occurs, that object's appropriate
 * method is invoked.
 *
 * @see TransportConnectionEvent
 */
public interface TransportConnectionListener {

	/**
	 * On node connected.
	 *
	 * @param node the node
	 */
	void onNodeConnected(DiscoveryNode node);

	/**
	 * On node disconnected.
	 *
	 * @param node the node
	 */
	void onNodeDisconnected(DiscoveryNode node);
}
