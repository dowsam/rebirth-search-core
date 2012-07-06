/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardRoutingTable.java 2012-3-29 15:01:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jsr166y.ThreadLocalRandom;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.index.shard.ShardId;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;


/**
 * The Class IndexShardRoutingTable.
 *
 * @author l.xue.nong
 */
public class IndexShardRoutingTable implements Iterable<ShardRouting> {

	
	/** The shard id. */
	final ShardId shardId;

	
	/** The primary. */
	final ShardRouting primary;

	
	/** The primary as list. */
	final ImmutableList<ShardRouting> primaryAsList;

	
	/** The replicas. */
	final ImmutableList<ShardRouting> replicas;

	
	/** The shards. */
	final ImmutableList<ShardRouting> shards;

	
	/** The active shards. */
	final ImmutableList<ShardRouting> activeShards;

	
	/** The assigned shards. */
	final ImmutableList<ShardRouting> assignedShards;

	
	/** The counter. */
	final AtomicInteger counter;

	
	/** The allocated post api. */
	final boolean allocatedPostApi;

	
	/**
	 * Instantiates a new index shard routing table.
	 *
	 * @param shardId the shard id
	 * @param shards the shards
	 * @param allocatedPostApi the allocated post api
	 */
	IndexShardRoutingTable(ShardId shardId, ImmutableList<ShardRouting> shards, boolean allocatedPostApi) {
		this.shardId = shardId;
		this.shards = shards;
		this.allocatedPostApi = allocatedPostApi;
		this.counter = new AtomicInteger(ThreadLocalRandom.current().nextInt(shards.size()));

		ShardRouting primary = null;
		List<ShardRouting> replicas = new ArrayList<ShardRouting>();
		List<ShardRouting> activeShards = new ArrayList<ShardRouting>();
		List<ShardRouting> assignedShards = new ArrayList<ShardRouting>();

		for (ShardRouting shard : shards) {
			if (shard.primary()) {
				primary = shard;
			} else {
				replicas.add(shard);
			}
			if (shard.active()) {
				activeShards.add(shard);
			}
			if (shard.assignedToNode()) {
				assignedShards.add(shard);
			}
		}

		this.primary = primary;
		if (primary != null) {
			this.primaryAsList = ImmutableList.of(primary);
		} else {
			this.primaryAsList = ImmutableList.of();
		}
		this.replicas = ImmutableList.copyOf(replicas);
		this.activeShards = ImmutableList.copyOf(activeShards);
		this.assignedShards = ImmutableList.copyOf(assignedShards);
	}

	
	/**
	 * Normalize versions.
	 *
	 * @return the index shard routing table
	 */
	public IndexShardRoutingTable normalizeVersions() {
		if (shards.isEmpty()) {
			return this;
		}
		if (shards.size() == 1) {
			return this;
		}
		long highestVersion = shards.get(0).version();
		boolean requiresNormalization = false;
		for (int i = 1; i < shards.size(); i++) {
			if (shards.get(i).version() != highestVersion) {
				requiresNormalization = true;
			}
			if (shards.get(i).version() > highestVersion) {
				highestVersion = shards.get(i).version();
			}
		}
		if (!requiresNormalization) {
			return this;
		}
		List<ShardRouting> shardRoutings = new ArrayList<ShardRouting>(shards.size());
		for (int i = 0; i < shards.size(); i++) {
			if (shards.get(i).version() == highestVersion) {
				shardRoutings.add(shards.get(i));
			} else {
				shardRoutings.add(new ImmutableShardRouting(shards.get(i), highestVersion));
			}
		}
		return new IndexShardRoutingTable(shardId, ImmutableList.copyOf(shardRoutings), allocatedPostApi);
	}

	
	/**
	 * Allocated post api.
	 *
	 * @return true, if successful
	 */
	public boolean allocatedPostApi() {
		return allocatedPostApi;
	}

	
	/**
	 * Shard id.
	 *
	 * @return the shard id
	 */
	public ShardId shardId() {
		return shardId;
	}

	
	/**
	 * Gets the shard id.
	 *
	 * @return the shard id
	 */
	public ShardId getShardId() {
		return shardId();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public UnmodifiableIterator<ShardRouting> iterator() {
		return shards.iterator();
	}

	
	/**
	 * Size.
	 *
	 * @return the int
	 */
	public int size() {
		return shards.size();
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
	 * Shards.
	 *
	 * @return the immutable list
	 */
	public ImmutableList<ShardRouting> shards() {
		return this.shards;
	}

	
	/**
	 * Gets the shards.
	 *
	 * @return the shards
	 */
	public ImmutableList<ShardRouting> getShards() {
		return shards();
	}

	
	/**
	 * Active shards.
	 *
	 * @return the immutable list
	 */
	public ImmutableList<ShardRouting> activeShards() {
		return this.activeShards;
	}

	
	/**
	 * Gets the active shards.
	 *
	 * @return the active shards
	 */
	public ImmutableList<ShardRouting> getActiveShards() {
		return activeShards();
	}

	
	/**
	 * Assigned shards.
	 *
	 * @return the immutable list
	 */
	public ImmutableList<ShardRouting> assignedShards() {
		return this.assignedShards;
	}

	
	/**
	 * Gets the assigned shards.
	 *
	 * @return the assigned shards
	 */
	public ImmutableList<ShardRouting> getAssignedShards() {
		return this.assignedShards;
	}

	
	/**
	 * Count with state.
	 *
	 * @param state the state
	 * @return the int
	 */
	public int countWithState(ShardRoutingState state) {
		int count = 0;
		for (ShardRouting shard : this) {
			if (state == shard.state()) {
				count++;
			}
		}
		return count;
	}

	
	/**
	 * Shards random it.
	 *
	 * @return the shard iterator
	 */
	public ShardIterator shardsRandomIt() {
		return new PlainShardIterator(shardId, shards, counter.getAndIncrement());
	}

	
	/**
	 * Shards it.
	 *
	 * @return the shard iterator
	 */
	public ShardIterator shardsIt() {
		return new PlainShardIterator(shardId, shards);
	}

	
	/**
	 * Shards it.
	 *
	 * @param index the index
	 * @return the shard iterator
	 */
	public ShardIterator shardsIt(int index) {
		return new PlainShardIterator(shardId, shards, index);
	}

	
	/**
	 * Active shards random it.
	 *
	 * @return the shard iterator
	 */
	public ShardIterator activeShardsRandomIt() {
		return new PlainShardIterator(shardId, activeShards, counter.getAndIncrement());
	}

	
	/**
	 * Active shards it.
	 *
	 * @return the shard iterator
	 */
	public ShardIterator activeShardsIt() {
		return new PlainShardIterator(shardId, activeShards);
	}

	
	/**
	 * Active shards it.
	 *
	 * @param index the index
	 * @return the shard iterator
	 */
	public ShardIterator activeShardsIt(int index) {
		return new PlainShardIterator(shardId, activeShards, index);
	}

	
	/**
	 * Assigned shards random it.
	 *
	 * @return the shard iterator
	 */
	public ShardIterator assignedShardsRandomIt() {
		return new PlainShardIterator(shardId, assignedShards, counter.getAndIncrement());
	}

	
	/**
	 * Assigned shards it.
	 *
	 * @return the shard iterator
	 */
	public ShardIterator assignedShardsIt() {
		return new PlainShardIterator(shardId, assignedShards);
	}

	
	/**
	 * Assigned shards it.
	 *
	 * @param index the index
	 * @return the shard iterator
	 */
	public ShardIterator assignedShardsIt(int index) {
		return new PlainShardIterator(shardId, assignedShards, index);
	}

	
	/**
	 * Primary shard it.
	 *
	 * @return the shard iterator
	 */
	public ShardIterator primaryShardIt() {
		return new PlainShardIterator(shardId, primaryAsList);
	}

	
	/**
	 * Prefer node shards it.
	 *
	 * @param nodeId the node id
	 * @return the shard iterator
	 */
	public ShardIterator preferNodeShardsIt(String nodeId) {
		return preferNodeShardsIt(nodeId, shards);
	}

	
	/**
	 * Only node active shards it.
	 *
	 * @param nodeId the node id
	 * @return the shard iterator
	 */
	public ShardIterator onlyNodeActiveShardsIt(String nodeId) {
		ArrayList<ShardRouting> ordered = new ArrayList<ShardRouting>(shards.size());
		
		for (int i = 0; i < shards.size(); i++) {
			ShardRouting shardRouting = shards.get(i);
			if (nodeId.equals(shardRouting.currentNodeId())) {
				ordered.add(shardRouting);
			}
		}
		return new PlainShardIterator(shardId, ordered);
	}

	
	/**
	 * Prefer node active shards it.
	 *
	 * @param nodeId the node id
	 * @return the shard iterator
	 */
	public ShardIterator preferNodeActiveShardsIt(String nodeId) {
		return preferNodeShardsIt(nodeId, activeShards);
	}

	
	/**
	 * Prefer node assigned shards it.
	 *
	 * @param nodeId the node id
	 * @return the shard iterator
	 */
	public ShardIterator preferNodeAssignedShardsIt(String nodeId) {
		return preferNodeShardsIt(nodeId, assignedShards);
	}

	
	/**
	 * Prefer node shards it.
	 *
	 * @param nodeId the node id
	 * @param shards the shards
	 * @return the shard iterator
	 */
	private ShardIterator preferNodeShardsIt(String nodeId, ImmutableList<ShardRouting> shards) {
		ArrayList<ShardRouting> ordered = new ArrayList<ShardRouting>(shards.size());
		
		int index = Math.abs(counter.getAndIncrement());
		for (int i = 0; i < shards.size(); i++) {
			int loc = (index + i) % shards.size();
			ShardRouting shardRouting = shards.get(loc);
			ordered.add(shardRouting);
			if (nodeId.equals(shardRouting.currentNodeId())) {
				
				ordered.set(i, ordered.get(0));
				ordered.set(0, shardRouting);
			}
		}
		return new PlainShardIterator(shardId, ordered);
	}

	
	/**
	 * The Class AttributesKey.
	 *
	 * @author l.xue.nong
	 */
	static class AttributesKey {

		
		/** The attributes. */
		final String[] attributes;

		
		/**
		 * Instantiates a new attributes key.
		 *
		 * @param attributes the attributes
		 */
		AttributesKey(String[] attributes) {
			this.attributes = attributes;
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Arrays.hashCode(attributes);
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return Arrays.equals(attributes, ((AttributesKey) obj).attributes);
		}
	}

	
	/**
	 * The Class AttributesRoutings.
	 *
	 * @author l.xue.nong
	 */
	static class AttributesRoutings {

		
		/** The with same attribute. */
		public final ImmutableList<ShardRouting> withSameAttribute;

		
		/** The without same attribute. */
		public final ImmutableList<ShardRouting> withoutSameAttribute;

		
		/** The total size. */
		public final int totalSize;

		
		/**
		 * Instantiates a new attributes routings.
		 *
		 * @param withSameAttribute the with same attribute
		 * @param withoutSameAttribute the without same attribute
		 */
		AttributesRoutings(ImmutableList<ShardRouting> withSameAttribute,
				ImmutableList<ShardRouting> withoutSameAttribute) {
			this.withSameAttribute = withSameAttribute;
			this.withoutSameAttribute = withoutSameAttribute;
			this.totalSize = withoutSameAttribute.size() + withSameAttribute.size();
		}
	}

	
	/** The active shards by attributes. */
	private volatile Map<AttributesKey, AttributesRoutings> activeShardsByAttributes = ImmutableMap.of();

	
	/** The shards by attribute mutex. */
	private final Object shardsByAttributeMutex = new Object();

	
	/**
	 * Prefer attributes active shards it.
	 *
	 * @param attributes the attributes
	 * @param nodes the nodes
	 * @return the shard iterator
	 */
	public ShardIterator preferAttributesActiveShardsIt(String[] attributes, DiscoveryNodes nodes) {
		return preferAttributesActiveShardsIt(attributes, nodes, counter.incrementAndGet());
	}

	
	/**
	 * Prefer attributes active shards it.
	 *
	 * @param attributes the attributes
	 * @param nodes the nodes
	 * @param index the index
	 * @return the shard iterator
	 */
	public ShardIterator preferAttributesActiveShardsIt(String[] attributes, DiscoveryNodes nodes, int index) {
		AttributesKey key = new AttributesKey(attributes);
		AttributesRoutings shardRoutings = activeShardsByAttributes.get(key);
		if (shardRoutings == null) {
			synchronized (shardsByAttributeMutex) {
				ArrayList<ShardRouting> from = new ArrayList<ShardRouting>(activeShards);
				ArrayList<ShardRouting> to = new ArrayList<ShardRouting>();
				for (String attribute : attributes) {
					String localAttributeValue = nodes.localNode().attributes().get(attribute);
					if (localAttributeValue == null) {
						continue;
					}
					for (Iterator<ShardRouting> iterator = from.iterator(); iterator.hasNext();) {
						ShardRouting fromShard = iterator.next();
						if (localAttributeValue
								.equals(nodes.get(fromShard.currentNodeId()).attributes().get(attribute))) {
							iterator.remove();
							to.add(fromShard);
						}
					}
				}

				shardRoutings = new AttributesRoutings(ImmutableList.copyOf(to), ImmutableList.copyOf(from));
				activeShardsByAttributes = MapBuilder.newMapBuilder(activeShardsByAttributes).put(key, shardRoutings)
						.immutableMap();
			}
		}
		
		
		ArrayList<ShardRouting> ordered = new ArrayList<ShardRouting>(shardRoutings.totalSize);
		index = Math.abs(index);
		for (int i = 0; i < shardRoutings.withSameAttribute.size(); i++) {
			int loc = (index + i) % shardRoutings.withSameAttribute.size();
			ShardRouting shardRouting = shardRoutings.withSameAttribute.get(loc);
			ordered.add(shardRouting);
		}
		for (int i = 0; i < shardRoutings.withoutSameAttribute.size(); i++) {
			int loc = (index + i) % shardRoutings.withoutSameAttribute.size();
			ShardRouting shardRouting = shardRoutings.withoutSameAttribute.get(loc);
			ordered.add(shardRouting);
		}

		return new PlainShardIterator(shardId, ordered);
	}

	
	/**
	 * Primary shard.
	 *
	 * @return the shard routing
	 */
	public ShardRouting primaryShard() {
		return primary;
	}

	
	/**
	 * Replica shards.
	 *
	 * @return the list
	 */
	public List<ShardRouting> replicaShards() {
		return this.replicas;
	}

	
	/**
	 * Shards with state.
	 *
	 * @param states the states
	 * @return the list
	 */
	public List<ShardRouting> shardsWithState(ShardRoutingState... states) {
		List<ShardRouting> shards = newArrayList();
		for (ShardRouting shardEntry : this) {
			for (ShardRoutingState state : states) {
				if (shardEntry.state() == state) {
					shards.add(shardEntry);
				}
			}
		}
		return shards;
	}

	
	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder {

		
		/** The shard id. */
		private ShardId shardId;

		
		/** The shards. */
		private final List<ShardRouting> shards;

		
		/** The allocated post api. */
		private boolean allocatedPostApi;

		
		/**
		 * Instantiates a new builder.
		 *
		 * @param indexShard the index shard
		 */
		public Builder(IndexShardRoutingTable indexShard) {
			this.shardId = indexShard.shardId;
			this.shards = newArrayList(indexShard.shards);
			this.allocatedPostApi = indexShard.allocatedPostApi();
		}

		
		/**
		 * Instantiates a new builder.
		 *
		 * @param shardId the shard id
		 * @param allocatedPostApi the allocated post api
		 */
		public Builder(ShardId shardId, boolean allocatedPostApi) {
			this.shardId = shardId;
			this.shards = newArrayList();
			this.allocatedPostApi = allocatedPostApi;
		}

		
		/**
		 * Adds the shard.
		 *
		 * @param shardEntry the shard entry
		 * @return the builder
		 */
		public Builder addShard(ImmutableShardRouting shardEntry) {
			for (ShardRouting shard : shards) {
				
				
				if (shard.assignedToNode() && shardEntry.assignedToNode()
						&& shard.currentNodeId().equals(shardEntry.currentNodeId())) {
					return this;
				}
			}
			shards.add(shardEntry);
			return this;
		}

		
		/**
		 * Removes the shard.
		 *
		 * @param shardEntry the shard entry
		 * @return the builder
		 */
		public Builder removeShard(ShardRouting shardEntry) {
			shards.remove(shardEntry);
			return this;
		}

		
		/**
		 * Builds the.
		 *
		 * @return the index shard routing table
		 */
		public IndexShardRoutingTable build() {
			
			if (!allocatedPostApi) {
				for (ShardRouting shardRouting : shards) {
					if (shardRouting.primary() && shardRouting.active()) {
						allocatedPostApi = true;
					}
				}
			}
			return new IndexShardRoutingTable(shardId, ImmutableList.copyOf(shards), allocatedPostApi);
		}

		
		/**
		 * Read from.
		 *
		 * @param in the in
		 * @return the index shard routing table
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static IndexShardRoutingTable readFrom(StreamInput in) throws IOException {
			String index = in.readUTF();
			return readFromThin(in, index);
		}

		
		/**
		 * Read from thin.
		 *
		 * @param in the in
		 * @param index the index
		 * @return the index shard routing table
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static IndexShardRoutingTable readFromThin(StreamInput in, String index) throws IOException {
			int iShardId = in.readVInt();
			boolean allocatedPostApi = in.readBoolean();
			Builder builder = new Builder(new ShardId(index, iShardId), allocatedPostApi);

			int size = in.readVInt();
			for (int i = 0; i < size; i++) {
				ImmutableShardRouting shard = ImmutableShardRouting.readShardRoutingEntry(in, index, iShardId);
				builder.addShard(shard);
			}

			return builder.build();
		}

		
		/**
		 * Write to.
		 *
		 * @param indexShard the index shard
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeTo(IndexShardRoutingTable indexShard, StreamOutput out) throws IOException {
			out.writeUTF(indexShard.shardId().index().name());
			writeToThin(indexShard, out);
		}

		
		/**
		 * Write to thin.
		 *
		 * @param indexShard the index shard
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeToThin(IndexShardRoutingTable indexShard, StreamOutput out) throws IOException {
			out.writeVInt(indexShard.shardId.id());
			out.writeBoolean(indexShard.allocatedPostApi());

			out.writeVInt(indexShard.shards.size());
			for (ShardRouting entry : indexShard) {
				entry.writeToThin(out);
			}
		}

	}
}
