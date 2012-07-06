/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FailedNodeException.java 2012-3-29 15:02:40 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class FailedNodeException.
 *
 * @author l.xue.nong
 */
public class FailedNodeException extends RestartException {

	
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
