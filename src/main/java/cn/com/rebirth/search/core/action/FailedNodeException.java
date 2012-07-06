/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FailedNodeException.java 2012-7-6 14:29:20 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class FailedNodeException.
 *
 * @author l.xue.nong
 */
public class FailedNodeException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1157952813948214142L;

	/** The node id. */
	private final String nodeId;

	/**
	 * Instantiates a new failed node exception.
	 *
	 * @param nodeId the node id
	 * @param msg the msg
	 * @param cause the cause
	 */
	public FailedNodeException(String nodeId, String msg, Throwable cause) {
		super(msg, cause);
		this.nodeId = nodeId;
	}

	/**
	 * Node id.
	 *
	 * @return the string
	 */
	public String nodeId() {
		return this.nodeId;
	}
}
