/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterState.java 2012-3-29 15:02:53 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster;

import java.io.IOException;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.io.stream.BytesStreamInput;
import cn.com.rebirth.search.commons.io.stream.BytesStreamOutput;
import cn.com.rebirth.search.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.search.core.cluster.block.ClusterBlocks;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.RoutingNodes;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.cluster.routing.allocation.AllocationExplanation;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;


/**
 * The Class ClusterState.
 *
 * @author l.xue.nong
 */
public class ClusterState {

	
	/** The version. */
	private final long version;

	
	/** The routing table. */
	private final RoutingTable routingTable;

	
	/** The nodes. */
	private final DiscoveryNodes nodes;

	
	/** The meta data. */
	private final MetaData metaData;

	
	/** The blocks. */
	private final ClusterBlocks blocks;

	
	/** The allocation explanation. */
	private final AllocationExplanation allocationExplanation;

	
	
	/** The routing nodes. */
	private volatile RoutingNodes routingNodes;

	
	/**
	 * Instantiates a new cluster state.
	 *
	 * @param version the version
	 * @param state the state
	 */
	public ClusterState(long version, ClusterState state) {
		this(version, state.metaData(), state.routingTable(), state.nodes(), state.blocks(), state
				.allocationExplanation());
	}

	
	/**
	 * Instantiates a new cluster state.
	 *
	 * @param version the version
	 * @param metaData the meta data
	 * @param routingTable the routing table
	 * @param nodes the nodes
	 * @param blocks the blocks
	 * @param allocationExplanation the allocation explanation
	 */
	public ClusterState(long version, MetaData metaData, RoutingTable routingTable, DiscoveryNodes nodes,
			ClusterBlocks blocks, AllocationExplanation allocationExplanation) {
		this.version = version;
		this.metaData = metaData;
		this.routingTable = routingTable;
		this.nodes = nodes;
		this.blocks = blocks;
		this.allocationExplanation = allocationExplanation;
	}

	
	/**
	 * Version.
	 *
	 * @return the long
	 */
	public long version() {
		return this.version;
	}

	
	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public long getVersion() {
		return version();
	}

	
	/**
	 * Nodes.
	 *
	 * @return the discovery nodes
	 */
	public DiscoveryNodes nodes() {
		return this.nodes;
	}

	
	/**
	 * Gets the nodes.
	 *
	 * @return the nodes
	 */
	public DiscoveryNodes getNodes() {
		return nodes();
	}

	
	/**
	 * Meta data.
	 *
	 * @return the meta data
	 */
	public MetaData metaData() {
		return this.metaData;
	}

	
	/**
	 * Gets the meta data.
	 *
	 * @return the meta data
	 */
	public MetaData getMetaData() {
		return metaData();
	}

	
	/**
	 * Routing table.
	 *
	 * @return the routing table
	 */
	public RoutingTable routingTable() {
		return routingTable;
	}

	
	/**
	 * Gets the routing table.
	 *
	 * @return the routing table
	 */
	public RoutingTable getRoutingTable() {
		return routingTable();
	}

	
	/**
	 * Routing nodes.
	 *
	 * @return the routing nodes
	 */
	public RoutingNodes routingNodes() {
		return routingTable.routingNodes(this);
	}

	
	/**
	 * Gets the routing nodes.
	 *
	 * @return the routing nodes
	 */
	public RoutingNodes getRoutingNodes() {
		return readOnlyRoutingNodes();
	}

	
	/**
	 * Blocks.
	 *
	 * @return the cluster blocks
	 */
	public ClusterBlocks blocks() {
		return this.blocks;
	}

	
	/**
	 * Gets the blocks.
	 *
	 * @return the blocks
	 */
	public ClusterBlocks getBlocks() {
		return blocks;
	}

	
	/**
	 * Allocation explanation.
	 *
	 * @return the allocation explanation
	 */
	public AllocationExplanation allocationExplanation() {
		return this.allocationExplanation;
	}

	
	/**
	 * Gets the allocation explanation.
	 *
	 * @return the allocation explanation
	 */
	public AllocationExplanation getAllocationExplanation() {
		return allocationExplanation();
	}

	
	/**
	 * Read only routing nodes.
	 *
	 * @return the routing nodes
	 */
	public RoutingNodes readOnlyRoutingNodes() {
		if (routingNodes != null) {
			return routingNodes;
		}
		routingNodes = routingTable.routingNodes(this);
		return routingNodes;
	}

	
	/**
	 * Builder.
	 *
	 * @return the builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	
	/**
	 * New cluster state builder.
	 *
	 * @return the builder
	 */
	public static Builder newClusterStateBuilder() {
		return new Builder();
	}

	
	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder {

		
		/** The version. */
		private long version = 0;

		
		/** The meta data. */
		private MetaData metaData = MetaData.EMPTY_META_DATA;

		
		/** The routing table. */
		private RoutingTable routingTable = RoutingTable.EMPTY_ROUTING_TABLE;

		
		/** The nodes. */
		private DiscoveryNodes nodes = DiscoveryNodes.EMPTY_NODES;

		
		/** The blocks. */
		private ClusterBlocks blocks = ClusterBlocks.EMPTY_CLUSTER_BLOCK;

		
		/** The allocation explanation. */
		private AllocationExplanation allocationExplanation = AllocationExplanation.EMPTY;

		
		/**
		 * Nodes.
		 *
		 * @param nodesBuilder the nodes builder
		 * @return the builder
		 */
		public Builder nodes(DiscoveryNodes.Builder nodesBuilder) {
			return nodes(nodesBuilder.build());
		}

		
		/**
		 * Nodes.
		 *
		 * @param nodes the nodes
		 * @return the builder
		 */
		public Builder nodes(DiscoveryNodes nodes) {
			this.nodes = nodes;
			return this;
		}

		
		/**
		 * Routing table.
		 *
		 * @param routingTable the routing table
		 * @return the builder
		 */
		public Builder routingTable(RoutingTable.Builder routingTable) {
			return routingTable(routingTable.build());
		}

		
		/**
		 * Routing result.
		 *
		 * @param routingResult the routing result
		 * @return the builder
		 */
		public Builder routingResult(RoutingAllocation.Result routingResult) {
			this.routingTable = routingResult.routingTable();
			this.allocationExplanation = routingResult.explanation();
			return this;
		}

		
		/**
		 * Routing table.
		 *
		 * @param routingTable the routing table
		 * @return the builder
		 */
		public Builder routingTable(RoutingTable routingTable) {
			this.routingTable = routingTable;
			return this;
		}

		
		/**
		 * Meta data.
		 *
		 * @param metaDataBuilder the meta data builder
		 * @return the builder
		 */
		public Builder metaData(MetaData.Builder metaDataBuilder) {
			return metaData(metaDataBuilder.build());
		}

		
		/**
		 * Meta data.
		 *
		 * @param metaData the meta data
		 * @return the builder
		 */
		public Builder metaData(MetaData metaData) {
			this.metaData = metaData;
			return this;
		}

		
		/**
		 * Blocks.
		 *
		 * @param blocksBuilder the blocks builder
		 * @return the builder
		 */
		public Builder blocks(ClusterBlocks.Builder blocksBuilder) {
			return blocks(blocksBuilder.build());
		}

		
		/**
		 * Blocks.
		 *
		 * @param block the block
		 * @return the builder
		 */
		public Builder blocks(ClusterBlocks block) {
			this.blocks = block;
			return this;
		}

		
		/**
		 * Allocation explanation.
		 *
		 * @param allocationExplanation the allocation explanation
		 * @return the builder
		 */
		public Builder allocationExplanation(AllocationExplanation allocationExplanation) {
			this.allocationExplanation = allocationExplanation;
			return this;
		}

		
		/**
		 * Version.
		 *
		 * @param version the version
		 * @return the builder
		 */
		public Builder version(long version) {
			this.version = version;
			return this;
		}

		
		/**
		 * State.
		 *
		 * @param state the state
		 * @return the builder
		 */
		public Builder state(ClusterState state) {
			this.version = state.version();
			this.nodes = state.nodes();
			this.routingTable = state.routingTable();
			this.metaData = state.metaData();
			this.blocks = state.blocks();
			this.allocationExplanation = state.allocationExplanation();
			return this;
		}

		
		/**
		 * Builds the.
		 *
		 * @return the cluster state
		 */
		public ClusterState build() {
			return new ClusterState(version, metaData, routingTable, nodes, blocks, allocationExplanation);
		}

		
		/**
		 * To bytes.
		 *
		 * @param state the state
		 * @return the byte[]
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static byte[] toBytes(ClusterState state) throws IOException {
			CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
			try {
				BytesStreamOutput os = cachedEntry.cachedBytes();
				writeTo(state, os);
				return os.copiedByteArray();
			} finally {
				CachedStreamOutput.pushEntry(cachedEntry);
			}
		}

		
		/**
		 * From bytes.
		 *
		 * @param data the data
		 * @param localNode the local node
		 * @return the cluster state
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static ClusterState fromBytes(byte[] data, DiscoveryNode localNode) throws IOException {
			return readFrom(new BytesStreamInput(data, false), localNode);
		}

		
		/**
		 * Write to.
		 *
		 * @param state the state
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeTo(ClusterState state, StreamOutput out) throws IOException {
			out.writeLong(state.version());
			MetaData.Builder.writeTo(state.metaData(), out);
			RoutingTable.Builder.writeTo(state.routingTable(), out);
			DiscoveryNodes.Builder.writeTo(state.nodes(), out);
			ClusterBlocks.Builder.writeClusterBlocks(state.blocks(), out);
			state.allocationExplanation().writeTo(out);
		}

		
		/**
		 * Read from.
		 *
		 * @param in the in
		 * @param localNode the local node
		 * @return the cluster state
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static ClusterState readFrom(StreamInput in, @Nullable DiscoveryNode localNode) throws IOException {
			Builder builder = new Builder();
			builder.version = in.readLong();
			builder.metaData = MetaData.Builder.readFrom(in);
			builder.routingTable = RoutingTable.Builder.readFrom(in);
			builder.nodes = DiscoveryNodes.Builder.readFrom(in, localNode);
			builder.blocks = ClusterBlocks.Builder.readClusterBlocks(in);
			builder.allocationExplanation = AllocationExplanation.readAllocationExplanation(in);
			return builder.build();
		}
	}
}
