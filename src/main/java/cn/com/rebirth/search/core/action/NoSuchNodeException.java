/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NoSuchNodeException.java 2012-7-6 14:28:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

/**
 * The Class NoSuchNodeException.
 *
 * @author l.xue.nong
 */
public class NoSuchNodeException extends FailedNodeException {

	/** The Constant serialVersionUID. */
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
