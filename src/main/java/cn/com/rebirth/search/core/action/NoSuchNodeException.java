/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NoSuchNodeException.java 2012-3-29 15:02:24 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

/**
 * The Class NoSuchNodeException.
 *
 * @author l.xue.nong
 */
public class NoSuchNodeException extends FailedNodeException {

	private static final long serialVersionUID = 3455389632183022241L;

	/**
	 * Instantiates a new no such node exception.
	 *
	 * @param nodeId the node id
	 */
	public NoSuchNodeException(String nodeId) {
		super(nodeId, "No such node [" + nodeId + "]", null);
	}
}
