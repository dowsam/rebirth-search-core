/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeOperationRequest.java 2012-7-6 14:29:10 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.nodes;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Class NodeOperationRequest.
 *
 * @author l.xue.nong
 */
public abstract class NodeOperationRequest implements Streamable {

	/** The node id. */
	private String nodeId;

	/**
	 * Instantiates a new node operation request.
	 */
	protected NodeOperationRequest() {

	}

	/**
	 * Instantiates a new node operation request.
	 *
	 * @param nodeId the node id
	 */
	protected NodeOperationRequest(String nodeId) {
		this.nodeId = nodeId;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		nodeId = in.readUTF();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(nodeId);
	}
}
