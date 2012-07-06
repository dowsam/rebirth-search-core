/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NettyTransportManagement.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport.netty;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.jmx.MBean;
import cn.com.rebirth.search.core.jmx.ManagedAttribute;
import cn.com.rebirth.search.core.transport.Transport;

/**
 * The Class NettyTransportManagement.
 *
 * @author l.xue.nong
 */
@MBean(objectName = "service=transport,transportType=netty", description = "Netty Transport")
public class NettyTransportManagement {

	/** The transport. */
	private final NettyTransport transport;

	/**
	 * Instantiates a new netty transport management.
	 *
	 * @param transport the transport
	 */
	@Inject
	public NettyTransportManagement(Transport transport) {
		this.transport = (NettyTransport) transport;
	}

	/**
	 * Gets the number of outbound connections.
	 *
	 * @return the number of outbound connections
	 */
	@ManagedAttribute(description = "Number of connections this node has to other nodes")
	public long getNumberOfOutboundConnections() {
		return transport.connectedNodes.size();
	}

	/**
	 * Gets the worker count.
	 *
	 * @return the worker count
	 */
	@ManagedAttribute(description = "Number if IO worker threads")
	public int getWorkerCount() {
		return transport.workerCount;
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	@ManagedAttribute(description = "Port(s) netty was configured to bind on")
	public String getPort() {
		return transport.port;
	}

	/**
	 * Gets the bind host.
	 *
	 * @return the bind host
	 */
	@ManagedAttribute(description = "Host to bind to")
	public String getBindHost() {
		return transport.bindHost;
	}

	/**
	 * Gets the publish host.
	 *
	 * @return the publish host
	 */
	@ManagedAttribute(description = "Host to publish")
	public String getPublishHost() {
		return transport.publishHost;
	}

	/**
	 * Gets the connect timeout.
	 *
	 * @return the connect timeout
	 */
	@ManagedAttribute(description = "Connect timeout")
	public String getConnectTimeout() {
		return transport.connectTimeout.toString();
	}

	/**
	 * Gets the tcp no delay.
	 *
	 * @return the tcp no delay
	 */
	@ManagedAttribute(description = "TcpNoDelay")
	public Boolean getTcpNoDelay() {
		return transport.tcpNoDelay;
	}

	/**
	 * Gets the tcp keep alive.
	 *
	 * @return the tcp keep alive
	 */
	@ManagedAttribute(description = "TcpKeepAlive")
	public Boolean getTcpKeepAlive() {
		return transport.tcpKeepAlive;
	}

	/**
	 * Gets the reuse address.
	 *
	 * @return the reuse address
	 */
	@ManagedAttribute(description = "ReuseAddress")
	public Boolean getReuseAddress() {
		return transport.reuseAddress;
	}

	/**
	 * Gets the tcp send buffer size.
	 *
	 * @return the tcp send buffer size
	 */
	@ManagedAttribute(description = "TcpSendBufferSize")
	public String getTcpSendBufferSize() {
		if (transport.tcpSendBufferSize == null) {
			return null;
		}
		return transport.tcpSendBufferSize.toString();
	}

	/**
	 * Gets the tcp receive buffer size.
	 *
	 * @return the tcp receive buffer size
	 */
	@ManagedAttribute(description = "TcpReceiveBufferSize")
	public String getTcpReceiveBufferSize() {
		if (transport.tcpReceiveBufferSize == null) {
			return null;
		}
		return transport.tcpReceiveBufferSize.toString();
	}
}
