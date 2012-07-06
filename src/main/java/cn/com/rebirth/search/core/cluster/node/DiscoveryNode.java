/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DiscoveryNode.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.node;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import cn.com.rebirth.commons.Version;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.transport.TransportAddress;
import cn.com.rebirth.search.commons.transport.TransportAddressSerializers;
import cn.com.rebirth.search.core.RestartSearchCoreVersion;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * The Class DiscoveryNode.
 *
 * @author l.xue.nong
 */
public class DiscoveryNode implements Streamable, Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6396983001046601389L;

	/**
	 * Node requires local storage.
	 *
	 * @param settings the settings
	 * @return true, if successful
	 */
	public static boolean nodeRequiresLocalStorage(Settings settings) {
		return !(settings.getAsBoolean("node.client", false) || (!settings.getAsBoolean("node.data", true) && !settings
				.getAsBoolean("node.master", true)));
	}

	/**
	 * Client node.
	 *
	 * @param settings the settings
	 * @return true, if successful
	 */
	public static boolean clientNode(Settings settings) {
		String client = settings.get("node.client");
		return client != null && client.equals("true");
	}

	/**
	 * Master node.
	 *
	 * @param settings the settings
	 * @return true, if successful
	 */
	public static boolean masterNode(Settings settings) {
		String master = settings.get("node.master");
		if (master == null) {
			return !clientNode(settings);
		}
		return master.equals("true");
	}

	/**
	 * Data node.
	 *
	 * @param settings the settings
	 * @return true, if successful
	 */
	public static boolean dataNode(Settings settings) {
		String data = settings.get("node.data");
		if (data == null) {
			return !clientNode(settings);
		}
		return data.equals("true");
	}

	/** The Constant EMPTY_LIST. */
	public static final ImmutableList<DiscoveryNode> EMPTY_LIST = ImmutableList.of();

	/** The node name. */
	private String nodeName = "".intern();

	/** The node id. */
	private String nodeId;

	/** The address. */
	private TransportAddress address;

	/** The attributes. */
	private ImmutableMap<String, String> attributes;

	/** The version. */
	private Version version = new RestartSearchCoreVersion();

	/**
	 * Instantiates a new discovery node.
	 */
	private DiscoveryNode() {
	}

	/**
	 * Instantiates a new discovery node.
	 *
	 * @param nodeId the node id
	 * @param address the address
	 */
	public DiscoveryNode(String nodeId, TransportAddress address) {
		this("", nodeId, address, ImmutableMap.<String, String> of());
	}

	/**
	 * Instantiates a new discovery node.
	 *
	 * @param nodeName the node name
	 * @param nodeId the node id
	 * @param address the address
	 * @param attributes the attributes
	 */
	public DiscoveryNode(String nodeName, String nodeId, TransportAddress address, Map<String, String> attributes) {
		if (nodeName == null) {
			this.nodeName = "".intern();
		} else {
			this.nodeName = nodeName.intern();
		}
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			builder.put(entry.getKey().intern(), entry.getValue().intern());
		}
		this.attributes = builder.build();
		this.nodeId = nodeId.intern();
		this.address = address;
	}

	/**
	 * Should connect to.
	 *
	 * @param otherNode the other node
	 * @return true, if successful
	 */
	public boolean shouldConnectTo(DiscoveryNode otherNode) {
		if (clientNode() && otherNode.clientNode()) {
			return false;
		}
		return true;
	}

	/**
	 * Address.
	 *
	 * @return the transport address
	 */
	public TransportAddress address() {
		return address;
	}

	/**
	 * Gets the address.
	 *
	 * @return the address
	 */
	public TransportAddress getAddress() {
		return address();
	}

	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		return nodeId;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id();
	}

	/**
	 * Name.
	 *
	 * @return the string
	 */
	public String name() {
		return this.nodeName;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name();
	}

	/**
	 * Attributes.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, String> attributes() {
		return this.attributes;
	}

	/**
	 * Gets the attributes.
	 *
	 * @return the attributes
	 */
	public ImmutableMap<String, String> getAttributes() {
		return attributes();
	}

	/**
	 * Data node.
	 *
	 * @return true, if successful
	 */
	public boolean dataNode() {
		String data = attributes.get("data");
		if (data == null) {
			return !clientNode();
		}
		return data.equals("true");
	}

	/**
	 * Checks if is data node.
	 *
	 * @return true, if is data node
	 */
	public boolean isDataNode() {
		return dataNode();
	}

	/**
	 * Client node.
	 *
	 * @return true, if successful
	 */
	public boolean clientNode() {
		String client = attributes.get("client");
		return client != null && client.equals("true");
	}

	/**
	 * Checks if is client node.
	 *
	 * @return true, if is client node
	 */
	public boolean isClientNode() {
		return clientNode();
	}

	/**
	 * Master node.
	 *
	 * @return true, if successful
	 */
	public boolean masterNode() {
		String master = attributes.get("master");
		if (master == null) {
			return !clientNode();
		}
		return master.equals("true");
	}

	/**
	 * Checks if is master node.
	 *
	 * @return true, if is master node
	 */
	public boolean isMasterNode() {
		return masterNode();
	}

	/**
	 * Version.
	 *
	 * @return the version
	 */
	public Version version() {
		return this.version;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public Version getVersion() {
		return this.version;
	}

	/**
	 * Read node.
	 *
	 * @param in the in
	 * @return the discovery node
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static DiscoveryNode readNode(StreamInput in) throws IOException {
		DiscoveryNode node = new DiscoveryNode();
		node.readFrom(in);
		return node;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		nodeName = in.readUTF().intern();
		nodeId = in.readUTF().intern();
		address = TransportAddressSerializers.addressFromStream(in);
		int size = in.readVInt();
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		for (int i = 0; i < size; i++) {
			builder.put(in.readUTF().intern(), in.readUTF().intern());
		}
		attributes = builder.build();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(nodeName);
		out.writeUTF(nodeId);
		TransportAddressSerializers.addressToStream(out, address);
		out.writeVInt(attributes.size());
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeUTF(entry.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DiscoveryNode))
			return false;

		DiscoveryNode other = (DiscoveryNode) obj;
		return this.nodeId.equals(other.nodeId);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return nodeId.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (nodeName.length() > 0) {
			sb.append('[').append(nodeName).append(']');
		}
		if (nodeId != null) {
			sb.append('[').append(nodeId).append(']');
		}
		if (address != null) {
			sb.append('[').append(address).append(']');
		}
		if (!attributes.isEmpty()) {
			sb.append(attributes);
		}
		return sb.toString();
	}
}
