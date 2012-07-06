/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SendRequestTransportException.java 2012-3-29 15:01:03 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.exception.RestartWrapperException;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;


/**
 * The Class SendRequestTransportException.
 *
 * @author l.xue.nong
 */
public class SendRequestTransportException extends ActionTransportException implements RestartWrapperException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3084423827819420536L;

	
	/**
	 * Instantiates a new send request transport exception.
	 *
	 * @param node the node
	 * @param action the action
	 * @param cause the cause
	 */
	public SendRequestTransportException(DiscoveryNode node, String action, Throwable cause) {
		super(node == null ? null : node.name(), node == null ? null : node.address(), action, cause);
	}
}
