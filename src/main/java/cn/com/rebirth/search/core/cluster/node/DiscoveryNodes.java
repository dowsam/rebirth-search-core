/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DiscoveryNodes.java 2012-7-6 14:29:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.node;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.search.commons.transport.TransportAddress;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;

/**
 * The Class DiscoveryNodes.
 *
 * @author l.xue.nong
 */
public class DiscoveryNodes implements Iterable<DiscoveryNode> {

	/** The Constant EMPTY_NODES. */
	public static final DiscoveryNodes EMPTY_NODES = newNodesBuilder().build();

	/** The nodes. */
	private final ImmutableMap<String, DiscoveryNode> nodes;

	/** The data nodes. */
	private final ImmutableMap<String, DiscoveryNode> dataNodes;

	/** The master nodes. */
	private final ImmutableMap<String, DiscoveryNode> masterNodes;

	/** The master node id. */
	private final String masterNodeId;

	/** The local node id. */
	private final String localNodeId;

	/**
	 * Instantiates a new discovery nodes.
	 *
	 * @param nodes the nodes
	 * @param dataNodes the data nodes
	 * @param masterNodes the master nodes
	 * @param masterNodeId the master node id
	 * @param localNodeId the local node id
	 */
	private DiscoveryNodes(ImmutableMap<String, DiscoveryNode> nodes, ImmutableMap<String, DiscoveryNode> dataNodes,
			ImmutableMap<String, DiscoveryNode> masterNodes, String masterNodeId, String localNodeId) {
		this.nodes = nodes;
		this.dataNodes = dataNodes;
		this.masterNodes = masterNodes;
		this.masterNodeId = masterNodeId;
		this.localNodeId = localNodeId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public UnmodifiableIterator<DiscoveryNode> iterator() {
		return nodes.values().iterator();
	}

	/**
	 * Valid.
	 *
	 * @return true, if successful
	 */
	public boolean valid() {
		return localNodeId != null;
	}

	/**
	 * Local node master.
	 *
	 * @return true, if successful
	 */
	public boolean localNodeMaster() {
		if (localNodeId == null) {

			return false;
		}
		return localNodeId.equals(masterNodeId);
	}

	/**
	 * Size.
	 *
	 * @return the int
	 */
	public int size() {
		return nodes.size();
	}

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public int getSize() {
		return size();
	}

	/**
	 * Nodes.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, DiscoveryNode> nodes() {
		return this.nodes;
	}

	/**
	 * Gets the nodes.
	 *
	 * @return the nodes
	 */
	public ImmutableMap<String, DiscoveryNode> getNodes() {
		return nodes();
	}

	/**
	 * Data nodes.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, DiscoveryNode> dataNodes() {
		return this.dataNodes;
	}

	/**
	 * Gets the data nodes.
	 *
	 * @return the data nodes
	 */
	public ImmutableMap<String, DiscoveryNode> getDataNodes() {
		return dataNodes();
	}

	/**
	 * Master nodes.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, DiscoveryNode> masterNodes() {
		return this.masterNodes;
	}

	/**
	 * Gets the master nodes.
	 *
	 * @return the master nodes
	 */
	public ImmutableMap<String, DiscoveryNode> getMasterNodes() {
		return masterNodes();
	}

	/**
	 * Master and data nodes.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, DiscoveryNode> masterAndDataNodes() {
		return MapBuilder.<String, DiscoveryNode> newMapBuilder().putAll(dataNodes).putAll(masterNodes).immutableMap();
	}

	/**
	 * Gets the.
	 *
	 * @param nodeId the node id
	 * @return the discovery node
	 */
	public DiscoveryNode get(String nodeId) {
		return nodes.get(nodeId);
	}

	/**
	 * Node exists.
	 *
	 * @param nodeId the node id
	 * @return true, if successful
	 */
	public boolean nodeExists(String nodeId) {
		return nodes.containsKey(nodeId);
	}

	/**
	 * Master node id.
	 *
	 * @return the string
	 */
	public String masterNodeId() {
		return this.masterNodeId;
	}

	/**
	 * Gets the master node id.
	 *
	 * @return the master node id
	 */
	public String getMasterNodeId() {
		return masterNodeId();
	}

	/**
	 * Local node id.
	 *
	 * @return the string
	 */
	public String localNodeId() {
		return this.localNodeId;
	}

	/**
	 * Gets the local node id.
	 *
	 * @return the local node id
	 */
	public String getLocalNodeId() {
		return localNodeId();
	}

	/**
	 * Local node.
	 *
	 * @return the discovery node
	 */
	public DiscoveryNode localNode() {
		return nodes.get(localNodeId);
	}

	/**
	 * Gets the local node.
	 *
	 * @return the local node
	 */
	public DiscoveryNode getLocalNode() {
		return localNode();
	}

	/**
	 * Master node.
	 *
	 * @return the discovery node
	 */
	public DiscoveryNode masterNode() {
		return nodes.get(masterNodeId);
	}

	/**
	 * Gets the master node.
	 *
	 * @return the master node
	 */
	public DiscoveryNode getMasterNode() {
		return masterNode();
	}

	/**
	 * Find by address.
	 *
	 * @param address the address
	 * @return the discovery node
	 */
	public DiscoveryNode findByAddress(TransportAddress address) {
		for (DiscoveryNode node : nodes.values()) {
			if (node.address().equals(address)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Checks if is all nodes.
	 *
	 * @param nodesIds the nodes ids
	 * @return true, if is all nodes
	 */
	public boolean isAllNodes(String... nodesIds) {
		return nodesIds == null || nodesIds.length == 0 || (nodesIds.length == 1 && nodesIds[0].equals("_all"));
	}

	/**
	 * Resolve nodes.
	 *
	 * @param nodesIds the nodes ids
	 * @return the string[]
	 */
	public String[] resolveNodes(String... nodesIds) {
		if (isAllNodes(nodesIds)) {
			int index = 0;
			nodesIds = new String[nodes.size()];
			for (DiscoveryNode node : this) {
				nodesIds[index++] = node.id();
			}
			return nodesIds;
		} else {
			Set<String> resolvedNodesIds = new HashSet<String>(nodesIds.length);
			for (String nodeId : nodesIds) {
				if (nodeId.equals("_local")) {
					String localNodeId = localNodeId();
					if (localNodeId != null) {
						resolvedNodesIds.add(localNodeId);
					}
				} else if (nodeId.equals("_master")) {
					String masterNodeId = masterNodeId();
					if (masterNodeId != null) {
						resolvedNodesIds.add(masterNodeId);
					}
				} else if (nodeExists(nodeId)) {
					resolvedNodesIds.add(nodeId);
				} else {

					for (DiscoveryNode node : this) {
						if (Regex.simpleMatch(nodeId, node.name())) {
							resolvedNodesIds.add(node.id());
						}
					}
					for (DiscoveryNode node : this) {
						if (node.address().match(nodeId)) {
							resolvedNodesIds.add(node.id());
						}
					}
					int index = nodeId.indexOf(':');
					if (index != -1) {
						String matchAttrName = nodeId.substring(0, index);
						String matchAttrValue = nodeId.substring(index + 1);
						for (DiscoveryNode node : this) {
							for (Map.Entry<String, String> entry : node.attributes().entrySet()) {
								String attrName = entry.getKey();
								String attrValue = entry.getValue();
								if (Regex.simpleMatch(matchAttrName, attrName)
										&& Regex.simpleMatch(matchAttrValue, attrValue)) {
									resolvedNodesIds.add(node.id());
								}
							}
						}
					}
				}
			}
			return resolvedNodesIds.toArray(new String[resolvedNodesIds.size()]);
		}
	}

	/**
	 * Removes the dead members.
	 *
	 * @param newNodes the new nodes
	 * @param masterNodeId the master node id
	 * @return the discovery nodes
	 */
	public DiscoveryNodes removeDeadMembers(Set<String> newNodes, String masterNodeId) {
		Builder builder = new Builder().masterNodeId(masterNodeId).localNodeId(localNodeId);
		for (DiscoveryNode node : this) {
			if (newNodes.contains(node.id())) {
				builder.put(node);
			}
		}
		return builder.build();
	}

	/**
	 * New node.
	 *
	 * @param node the node
	 * @return the discovery nodes
	 */
	public DiscoveryNodes newNode(DiscoveryNode node) {
		return new Builder().putAll(this).put(node).build();
	}

	/**
	 * Delta.
	 *
	 * @param other the other
	 * @return the delta
	 */
	public Delta delta(DiscoveryNodes other) {
		List<DiscoveryNode> removed = newArrayList();
		List<DiscoveryNode> added = newArrayList();
		for (DiscoveryNode node : other) {
			if (!this.nodeExists(node.id())) {
				removed.add(node);
			}
		}
		for (DiscoveryNode node : this) {
			if (!other.nodeExists(node.id())) {
				added.add(node);
			}
		}
		DiscoveryNode previousMasterNode = null;
		DiscoveryNode newMasterNode = null;
		if (masterNodeId != null) {
			if (other.masterNodeId == null || !other.masterNodeId.equals(masterNodeId)) {
				previousMasterNode = other.masterNode();
				newMasterNode = masterNode();
			}
		}
		return new Delta(previousMasterNode, newMasterNode, localNodeId, ImmutableList.copyOf(removed),
				ImmutableList.copyOf(added));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (DiscoveryNode node : this) {
			sb.append(node).append(',');
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Pretty print.
	 *
	 * @return the string
	 */
	public String prettyPrint() {
		StringBuilder sb = new StringBuilder();
		sb.append("nodes: \n");
		for (DiscoveryNode node : this) {
			sb.append("   ").append(node);
			if (node == localNode()) {
				sb.append(", local");
			}
			if (node == masterNode()) {
				sb.append(", master");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Empty delta.
	 *
	 * @return the delta
	 */
	public Delta emptyDelta() {
		return new Delta(null, null, localNodeId, DiscoveryNode.EMPTY_LIST, DiscoveryNode.EMPTY_LIST);
	}

	/**
	 * The Class Delta.
	 *
	 * @author l.xue.nong
	 */
	public static class Delta {

		/** The local node id. */
		private final String localNodeId;

		/** The previous master node. */
		private final DiscoveryNode previousMasterNode;

		/** The new master node. */
		private final DiscoveryNode newMasterNode;

		/** The removed. */
		private final ImmutableList<DiscoveryNode> removed;

		/** The added. */
		private final ImmutableList<DiscoveryNode> added;

		/**
		 * Instantiates a new delta.
		 *
		 * @param localNodeId the local node id
		 * @param removed the removed
		 * @param added the added
		 */
		public Delta(String localNodeId, ImmutableList<DiscoveryNode> removed, ImmutableList<DiscoveryNode> added) {
			this(null, null, localNodeId, removed, added);
		}

		/**
		 * Instantiates a new delta.
		 *
		 * @param previousMasterNode the previous master node
		 * @param newMasterNode the new master node
		 * @param localNodeId the local node id
		 * @param removed the removed
		 * @param added the added
		 */
		public Delta(@Nullable DiscoveryNode previousMasterNode, @Nullable DiscoveryNode newMasterNode,
				String localNodeId, ImmutableList<DiscoveryNode> removed, ImmutableList<DiscoveryNode> added) {
			this.previousMasterNode = previousMasterNode;
			this.newMasterNode = newMasterNode;
			this.localNodeId = localNodeId;
			this.removed = removed;
			this.added = added;
		}

		/**
		 * Checks for changes.
		 *
		 * @return true, if successful
		 */
		public boolean hasChanges() {
			return masterNodeChanged() || !removed.isEmpty() || !added.isEmpty();
		}

		/**
		 * Master node changed.
		 *
		 * @return true, if successful
		 */
		public boolean masterNodeChanged() {
			return newMasterNode != null;
		}

		/**
		 * Previous master node.
		 *
		 * @return the discovery node
		 */
		public DiscoveryNode previousMasterNode() {
			return previousMasterNode;
		}

		/**
		 * New master node.
		 *
		 * @return the discovery node
		 */
		public DiscoveryNode newMasterNode() {
			return newMasterNode;
		}

		/**
		 * Removed.
		 *
		 * @return true, if successful
		 */
		public boolean removed() {
			return !removed.isEmpty();
		}

		/**
		 * Removed nodes.
		 *
		 * @return the immutable list
		 */
		public ImmutableList<DiscoveryNode> removedNodes() {
			return removed;
		}

		/**
		 * Added.
		 *
		 * @return true, if successful
		 */
		public boolean added() {
			return !added.isEmpty();
		}

		/**
		 * Added nodes.
		 *
		 * @return the immutable list
		 */
		public ImmutableList<DiscoveryNode> addedNodes() {
			return added;
		}

		/**
		 * Short summary.
		 *
		 * @return the string
		 */
		public String shortSummary() {
			StringBuilder sb = new StringBuilder();
			if (!removed() && masterNodeChanged()) {
				if (newMasterNode.id().equals(localNodeId)) {

					sb.append("new_master ").append(newMasterNode());
				} else {

					sb.append("detected_master ").append(newMasterNode());
				}
			} else {
				if (masterNodeChanged()) {
					sb.append("master {new ").append(newMasterNode());
					if (previousMasterNode() != null) {
						sb.append(", previous ").append(previousMasterNode());
					}
					sb.append("}");
				}
				if (removed()) {
					if (masterNodeChanged()) {
						sb.append(", ");
					}
					sb.append("removed {");
					for (DiscoveryNode node : removedNodes()) {
						sb.append(node).append(',');
					}
					sb.append("}");
				}
			}
			if (added()) {

				if (!(addedNodes().size() == 1 && addedNodes().get(0).id().equals(localNodeId))) {
					if (removed() || masterNodeChanged()) {
						sb.append(", ");
					}
					sb.append("added {");
					for (DiscoveryNode node : addedNodes()) {
						if (!node.id().equals(localNodeId)) {

							sb.append(node).append(',');
						}
					}
					sb.append("}");
				}
			}
			return sb.toString();
		}
	}

	/**
	 * New nodes builder.
	 *
	 * @return the builder
	 */
	public static Builder newNodesBuilder() {
		return new Builder();
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder {

		/** The nodes. */
		private Map<String, DiscoveryNode> nodes = newHashMap();

		/** The master node id. */
		private String masterNodeId;

		/** The local node id. */
		private String localNodeId;

		/**
		 * Put all.
		 *
		 * @param nodes the nodes
		 * @return the builder
		 */
		public Builder putAll(DiscoveryNodes nodes) {
			this.masterNodeId = nodes.masterNodeId();
			this.localNodeId = nodes.localNodeId();
			for (DiscoveryNode node : nodes) {
				put(node);
			}
			return this;
		}

		/**
		 * Put.
		 *
		 * @param node the node
		 * @return the builder
		 */
		public Builder put(DiscoveryNode node) {
			nodes.put(node.id(), node);
			return this;
		}

		/**
		 * Put all.
		 *
		 * @param nodes the nodes
		 * @return the builder
		 */
		public Builder putAll(Iterable<DiscoveryNode> nodes) {
			for (DiscoveryNode node : nodes) {
				put(node);
			}
			return this;
		}

		/**
		 * Removes the.
		 *
		 * @param nodeId the node id
		 * @return the builder
		 */
		public Builder remove(String nodeId) {
			nodes.remove(nodeId);
			return this;
		}

		/**
		 * Master node id.
		 *
		 * @param masterNodeId the master node id
		 * @return the builder
		 */
		public Builder masterNodeId(String masterNodeId) {
			this.masterNodeId = masterNodeId;
			return this;
		}

		/**
		 * Local node id.
		 *
		 * @param localNodeId the local node id
		 * @return the builder
		 */
		public Builder localNodeId(String localNodeId) {
			this.localNodeId = localNodeId;
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the discovery nodes
		 */
		public DiscoveryNodes build() {
			ImmutableMap.Builder<String, DiscoveryNode> dataNodesBuilder = ImmutableMap.builder();
			ImmutableMap.Builder<String, DiscoveryNode> masterNodesBuilder = ImmutableMap.builder();
			for (Map.Entry<String, DiscoveryNode> nodeEntry : nodes.entrySet()) {
				if (nodeEntry.getValue().dataNode()) {
					dataNodesBuilder.put(nodeEntry.getKey(), nodeEntry.getValue());
				}
				if (nodeEntry.getValue().masterNode()) {
					masterNodesBuilder.put(nodeEntry.getKey(), nodeEntry.getValue());
				}
			}
			return new DiscoveryNodes(ImmutableMap.copyOf(nodes), dataNodesBuilder.build(), masterNodesBuilder.build(),
					masterNodeId, localNodeId);
		}

		/**
		 * Write to.
		 *
		 * @param nodes the nodes
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeTo(DiscoveryNodes nodes, StreamOutput out) throws IOException {
			if (nodes.masterNodeId() == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeUTF(nodes.masterNodeId);
			}
			out.writeVInt(nodes.size());
			for (DiscoveryNode node : nodes) {
				node.writeTo(out);
			}
		}

		/**
		 * Read from.
		 *
		 * @param in the in
		 * @param localNode the local node
		 * @return the discovery nodes
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static DiscoveryNodes readFrom(StreamInput in, @Nullable DiscoveryNode localNode) throws IOException {
			Builder builder = new Builder();
			if (in.readBoolean()) {
				builder.masterNodeId(in.readUTF());
			}
			if (localNode != null) {
				builder.localNodeId(localNode.id());
			}
			int size = in.readVInt();
			for (int i = 0; i < size; i++) {
				DiscoveryNode node = DiscoveryNode.readNode(in);
				if (localNode != null && node.id().equals(localNode.id())) {

					node = localNode;
				}
				builder.put(node);
			}
			return builder.build();
		}
	}
}
