/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core Transport.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import java.io.IOException;

import cn.com.rebirth.commons.component.LifecycleComponent;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.transport.BoundTransportAddress;
import cn.com.rebirth.search.commons.transport.TransportAddress;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

/**
 * The Interface Transport.
 *
 * @author l.xue.nong
 */
public interface Transport extends LifecycleComponent<Transport> {

	/**
	 * Transport service adapter.
	 *
	 * @param service the service
	 */
	void transportServiceAdapter(TransportServiceAdapter service);

	/**
	 * Bound address.
	 *
	 * @return the bound transport address
	 */
	BoundTransportAddress boundAddress();

	/**
	 * Addresses from string.
	 *
	 * @param address the address
	 * @return the transport address[]
	 * @throws Exception the exception
	 */
	TransportAddress[] addressesFromString(String address) throws Exception;

	/**
	 * Address supported.
	 *
	 * @param address the address
	 * @return true, if successful
	 */
	boolean addressSupported(Class<? extends TransportAddress> address);

	/**
	 * Node connected.
	 *
	 * @param node the node
	 * @return true, if successful
	 */
	boolean nodeConnected(DiscoveryNode node);

	/**
	 * Connect to node.
	 *
	 * @param node the node
	 * @throws ConnectTransportException the connect transport exception
	 */
	void connectToNode(DiscoveryNode node) throws ConnectTransportException;

	/**
	 * Connect to node light.
	 *
	 * @param node the node
	 * @throws ConnectTransportException the connect transport exception
	 */
	void connectToNodeLight(DiscoveryNode node) throws ConnectTransportException;

	/**
	 * Disconnect from node.
	 *
	 * @param node the node
	 */
	void disconnectFromNode(DiscoveryNode node);

	/**
	 * Send request.
	 *
	 * @param <T> the generic type
	 * @param node the node
	 * @param requestId the request id
	 * @param action the action
	 * @param message the message
	 * @param options the options
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TransportException the transport exception
	 */
	<T extends Streamable> void sendRequest(DiscoveryNode node, long requestId, String action, Streamable message,
			TransportRequestOptions options) throws IOException, TransportException;

	/**
	 * Server open.
	 *
	 * @return the long
	 */
	long serverOpen();
}
