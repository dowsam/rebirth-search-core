/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ConnectTransportException.java 2012-3-29 15:00:59 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;


/**
 * The Class ConnectTransportException.
 *
 * @author l.xue.nong
 */
public class ConnectTransportException extends ActionTransportException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1703335216831057661L;
	
	
	/** The node. */
	private final DiscoveryNode node;

	
	/**
	 * Instantiates a new connect transport exception.
	 *
	 * @param node the node
	 * @param msg the msg
	 */
	public ConnectTransportException(DiscoveryNode node, String msg) {
		this(node, msg, null, null);
	}

	
	/**
	 * Instantiates a new connect transport exception.
	 *
	 * @param node the node
	 * @param msg the msg
	 * @param action the action
	 */
	public ConnectTransportException(DiscoveryNode node, String msg, String action) {
		this(node, msg, action, null);
	}

	
	/**
	 * Instantiates a new connect transport exception.
	 *
	 * @param node the node
	 * @param msg the msg
	 * @param cause the cause
	 */
	public ConnectTransportException(DiscoveryNode node, String msg, Throwable cause) {
		this(node, msg, null, cause);
	}

	
	/**
	 * Instantiates a new connect transport exception.
	 *
	 * @param node the node
	 * @param msg the msg
	 * @param action the action
	 * @param cause the cause
	 */
	public ConnectTransportException(DiscoveryNode node, String msg, String action, Throwable cause) {
		super(node.name(), node.address(), action, msg, cause);
		this.node = node;
	}

	
	/**
	 * Node.
	 *
	 * @return the discovery node
	 */
	public DiscoveryNode node() {
		return node;
	}
}
