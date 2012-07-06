/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RiverRouting.java 2012-3-29 15:02:48 l.xue.nong$$
 */


package cn.com.rebirth.search.core.river.routing;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.river.RiverName;


/**
 * The Class RiverRouting.
 *
 * @author l.xue.nong
 */
public class RiverRouting implements Streamable {

	
	/** The river name. */
	private RiverName riverName;

	
	/** The node. */
	private DiscoveryNode node;

	
	/**
	 * Instantiates a new river routing.
	 */
	private RiverRouting() {
	}

	
	/**
	 * Instantiates a new river routing.
	 *
	 * @param riverName the river name
	 * @param node the node
	 */
	RiverRouting(RiverName riverName, DiscoveryNode node) {
		this.riverName = riverName;
		this.node = node;
	}

	
	/**
	 * River name.
	 *
	 * @return the river name
	 */
	public RiverName riverName() {
		return riverName;
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
	 * Node.
	 *
	 * @param node the node
	 */
	void node(DiscoveryNode node) {
		this.node = node;
	}

	
	/**
	 * Read river routing.
	 *
	 * @param in the in
	 * @return the river routing
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static RiverRouting readRiverRouting(StreamInput in) throws IOException {
		RiverRouting routing = new RiverRouting();
		routing.readFrom(in);
		return routing;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		riverName = new RiverName(in.readUTF(), in.readUTF());
		if (in.readBoolean()) {
			node = DiscoveryNode.readNode(in);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(riverName.type());
		out.writeUTF(riverName.name());
		if (node == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			node.writeTo(out);
		}
	}
}
