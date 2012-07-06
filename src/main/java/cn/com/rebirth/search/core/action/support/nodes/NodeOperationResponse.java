/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodeOperationResponse.java 2012-3-29 15:01:59 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support.nodes;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;


/**
 * The Class NodeOperationResponse.
 *
 * @author l.xue.nong
 */
public abstract class NodeOperationResponse implements Streamable {

	
	/** The node. */
	private DiscoveryNode node;

	
	/**
	 * Instantiates a new node operation response.
	 */
	protected NodeOperationResponse() {
	}

	
	/**
	 * Instantiates a new node operation response.
	 *
	 * @param node the node
	 */
	protected NodeOperationResponse(DiscoveryNode node) {
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

	
	/**
	 * Gets the node.
	 *
	 * @return the node
	 */
	public DiscoveryNode getNode() {
		return node();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		node = DiscoveryNode.readNode(in);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		node.writeTo(out);
	}
}
