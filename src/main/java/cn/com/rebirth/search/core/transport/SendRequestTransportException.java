/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SendRequestTransportException.java 2012-7-6 14:29:38 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.exception.RebirthWrapperException;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

/**
 * The Class SendRequestTransportException.
 *
 * @author l.xue.nong
 */
public class SendRequestTransportException extends ActionTransportException implements RebirthWrapperException {

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
