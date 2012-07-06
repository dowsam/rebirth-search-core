/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexRoutingTable.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

/**
 * The Class IndexRoutingTable.
 *
 * @author l.xue.nong
 */
public class IndexRoutingTable implements Iterable<IndexShardRoutingTable> {

	/** The index. */
	private final String index;

	/** The shards. */
	private final ImmutableMap<Integer, IndexShardRoutingTable> shards;

	/** The all shards. */
	private final ImmutableList<ShardRouting> allShards;

	/** The all active shards. */
	private final ImmutableList<ShardRouting> allActiveShards;

	/** The counter. */
	private final AtomicInteger counter = new AtomicInteger();

	/**
	 * Instantiates a new index routing table.
	 *
	 * @param index the index
	 * @param shards the shards
	 */
	IndexRoutingTable(String index, Map<Integer, IndexShardRoutingTable> shards) {
		this.index = index;
		this.shards = ImmutableMap.copyOf(shards);
		ImmutableList.Builder<ShardRouting> allShards = ImmutableList.builder();
		ImmutableList.Builder<ShardRouting> allActiveShards = ImmutableList.builder();
		for (IndexShardRoutingTable indexShardRoutingTable : shards.values()) {
			for (ShardRouting shardRouting : indexShardRoutingTable) {
				allShards.add(shardRouting);
				if (shardRouting.active()) {
					allActiveShards.add(shardRouting);
				}
			}
		}
		this.allShards = allShards.build();
		this.allActiveShards = allActiveShards.build();
	}

	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return this.index;
	}

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index();
	}

	/**
	 * Normalize versions.
	 *
	 * @return the index routing table
	 */
	public IndexRoutingTable normalizeVersions() {
		IndexRoutingTable.Builder builder = new Builder(this.index);
		for (IndexShardRoutingTable shardTable : shards.values()) {
			builder.addIndexShard(shardTable.normalizeVersions());
		}
		return builder.build();
	}

	/**
	 * Validate.
	 *
	 * @param validation the validation
	 * @param metaData the meta data
	 */
	public void validate(RoutingTableValidation validation, MetaData metaData) {
		if (!metaData.hasIndex(index())) {
			validation.addIndexFailure(index(), "Exists in routing does not exists in metadata");
			return;
		}
		IndexMetaData indexMetaData = metaData.index(index());

		if (indexMetaData.numberOfShards() != shards().size()) {
			Set<Integer> expected = Sets.newHashSet();
			for (int i = 0; i < indexMetaData.numberOfShards(); i++) {
				expected.add(i);
			}
			for (IndexShardRoutingTable indexShardRoutingTable : this) {
				expected.remove(indexShardRoutingTable.shardId().id());
			}
			validation.addIndexFailure(index(), "Wrong number of shards in routing table, missing: " + expected);
		}

		for (IndexShardRoutingTable indexShardRoutingTable : this) {
			int routingNumberOfReplicas = indexShardRoutingTable.size() - 1;
			if (routingNumberOfReplicas != indexMetaData.numberOfReplicas()) {
				validation.addIndexFailure(index(), "Shard [" + indexShardRoutingTable.shardId().id()
						+ "] routing table has wrong number of replicas, expected [" + indexMetaData.numberOfReplicas()
						+ "], got [" + routingNumberOfReplicas + "]");
			}
			for (ShardRouting shardRouting : indexShardRoutingTable) {
				if (!shardRouting.index().equals(index())) {
					validation.addIndexFailure(index(), "shard routing has an index [" + shardRouting.index()
							+ "] that is different than the routing table");
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public UnmodifiableIterator<IndexShardRoutingTable> iterator() {
		return shards.values().iterator();
	}

	/**
	 * Number of nodes shards are allocated on.
	 *
	 * @param excludedNodes the excluded nodes
	 * @return the int
	 */
	public int numberOfNodesShardsAreAllocatedOn(String... excludedNodes) {
		Set<String> nodes = Sets.newHashSet();
		for (IndexShardRoutingTable shardRoutingTable : this) {
			for (ShardRouting shardRouting : shardRoutingTable) {
				if (shardRouting.assignedToNode()) {
					String currentNodeId = shardRouting.currentNodeId();
					boolean excluded = false;
					if (excludedNodes != null) {
						for (String excludedNode : excludedNodes) {
							if (currentNodeId.equals(excludedNode)) {
								excluded = true;
								break;
							}
						}
					}
					if (!excluded) {
						nodes.add(currentNodeId);
					}
				}
			}
		}
		return nodes.size();
	}

	/**
	 * Shards.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<Integer, IndexShardRoutingTable> shards() {
		return shards;
	}

	/**
	 * Gets the shards.
	 *
	 * @return the shards
	 */
	public ImmutableMap<Integer, IndexShardRoutingTable> getShards() {
		return shards();
	}

	/**
	 * Shard.
	 *
	 * @param shardId the shard id
	 * @return the index shard routing table
	 */
	public IndexShardRoutingTable shard(int shardId) {
		return shards.get(shardId);
	}

	/**
	 * All primary shards active.
	 *
	 * @return true, if successful
	 */
	public boolean allPrimaryShardsActive() {
		return primaryShardsActive() == shards().size();
	}

	/**
	 * Primary shards active.
	 *
	 * @return the int
	 */
	public int primaryShardsActive() {
		int counter = 0;
		for (IndexShardRoutingTable shardRoutingTable : this) {
			if (shardRoutingTable.primaryShard().active()) {
				counter++;
			}
		}
		return counter;
	}

	/**
	 * All primary shards unassigned.
	 *
	 * @return true, if successful
	 */
	public boolean allPrimaryShardsUnassigned() {
		return primaryShardsUnassigned() == shards.size();
	}

	/**
	 * Primary shards unassigned.
	 *
	 * @return the int
	 */
	public int primaryShardsUnassigned() {
		int counter = 0;
		for (IndexShardRoutingTable shardRoutingTable : this) {
			if (shardRoutingTable.primaryShard().unassigned()) {
				counter++;
			}
		}
		return counter;
	}

	/**
	 * Shards with state.
	 *
	 * @param states the states
	 * @return the list
	 */
	public List<ShardRouting> shardsWithState(ShardRoutingState... states) {
		List<ShardRouting> shards = newArrayList();
		for (IndexShardRoutingTable shardRoutingTable : this) {
			shards.addAll(shardRoutingTable.shardsWithState(states));
		}
		return shards;
	}

	/**
	 * Random all shards it.
	 *
	 * @return the shards iterator
	 */
	public ShardsIterator randomAllShardsIt() {
		return new PlainShardsIterator(allShards, counter.incrementAndGet());
	}

	/**
	 * Random all active shards it.
	 *
	 * @return the shards iterator
	 */
	public ShardsIterator randomAllActiveShardsIt() {
		return new PlainShardsIterator(allActiveShards, counter.incrementAndGet());
	}

	/**
	 * Group by shards it.
	 *
	 * @return the group shards iterator
	 */
	public GroupShardsIterator groupByShardsIt() {

		ArrayList<ShardIterator> set = new ArrayList<ShardIterator>(shards.size());
		for (IndexShardRoutingTable indexShard : this) {
			set.add(indexShard.shardsIt());
		}
		return new GroupShardsIterator(set);
	}

	/**
	 * Group by all it.
	 *
	 * @return the group shards iterator
	 */
	public GroupShardsIterator groupByAllIt() {

		ArrayList<ShardIterator> set = new ArrayList<ShardIterator>();
		for (IndexShardRoutingTable indexShard : this) {
			for (ShardRouting shardRouting : indexShard) {
				set.add(shardRouting.shardsIt());
			}
		}
		return new GroupShardsIterator(set);
	}

	/**
	 * Validate.
	 *
	 * @throws RoutingValidationException the routing validation exception
	 */
	public void validate() throws RoutingValidationException {
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder {

		/** The index. */
		private final String index;

		/** The shards. */
		private final Map<Integer, IndexShardRoutingTable> shards = new HashMap<Integer, IndexShardRoutingTable>();

		/**
		 * Instantiates a new builder.
		 *
		 * @param index the index
		 */
		public Builder(String index) {
			this.index = index;
		}

		/**
		 * Read from.
		 *
		 * @param in the in
		 * @return the index routing table
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static IndexRoutingTable readFrom(StreamInput in) throws IOException {
			String index = in.readUTF();
			Builder builder = new Builder(index);

			int size = in.readVInt();
			for (int i = 0; i < size; i++) {
				builder.addIndexShard(IndexShardRoutingTable.Builder.readFromThin(in, index));
			}

			return builder.build();
		}

		/**
		 * Write to.
		 *
		 * @param index the index
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeTo(IndexRoutingTable index, StreamOutput out) throws IOException {
			out.writeUTF(index.index());
			out.writeVInt(index.shards.size());
			for (IndexShardRoutingTable indexShard : index) {
				IndexShardRoutingTable.Builder.writeToThin(indexShard, out);
			}
		}

		/**
		 * Initialize empty.
		 *
		 * @param indexMetaData the index meta data
		 * @return the builder
		 */
		public Builder initializeEmpty(IndexMetaData indexMetaData) {
			return initializeEmpty(indexMetaData, true);
		}

		/**
		 * Initialize empty.
		 *
		 * @param indexMetaData the index meta data
		 * @param fromApi the from api
		 * @return the builder
		 */
		public Builder initializeEmpty(IndexMetaData indexMetaData, boolean fromApi) {
			for (int shardId = 0; shardId < indexMetaData.numberOfShards(); shardId++) {
				for (int i = 0; i <= indexMetaData.numberOfReplicas(); i++) {
					addShard(shardId, null, i == 0, ShardRoutingState.UNASSIGNED, 0, fromApi);
				}
			}
			return this;
		}

		/**
		 * Adds the replica.
		 *
		 * @return the builder
		 */
		public Builder addReplica() {
			for (int shardId : shards.keySet()) {

				addShard(shardId, null, false, ShardRoutingState.UNASSIGNED, 0, false);
			}
			return this;
		}

		/**
		 * Removes the replica.
		 *
		 * @return the builder
		 */
		public Builder removeReplica() {
			for (int shardId : shards.keySet()) {
				IndexShardRoutingTable indexShard = shards.get(shardId);
				if (indexShard.replicaShards().isEmpty()) {

					return this;
				}

				IndexShardRoutingTable.Builder builder = new IndexShardRoutingTable.Builder(indexShard.shardId(),
						indexShard.allocatedPostApi());
				for (ShardRouting shardRouting : indexShard) {
					builder.addShard(new ImmutableShardRouting(shardRouting));
				}

				boolean removed = false;
				for (ShardRouting shardRouting : indexShard) {
					if (!shardRouting.primary() && !shardRouting.assignedToNode()) {
						builder.removeShard(shardRouting);
						removed = true;
						break;
					}
				}
				if (!removed) {
					for (ShardRouting shardRouting : indexShard) {
						if (!shardRouting.primary()) {
							builder.removeShard(shardRouting);
							removed = true;
							break;
						}
					}
				}
				shards.put(shardId, builder.build());
			}
			return this;
		}

		/**
		 * Adds the index shard.
		 *
		 * @param indexShard the index shard
		 * @return the builder
		 */
		public Builder addIndexShard(IndexShardRoutingTable indexShard) {
			shards.put(indexShard.shardId().id(), indexShard);
			return this;
		}

		/**
		 * Adds the shard.
		 *
		 * @param shard the shard
		 * @param fromApi the from api
		 * @return the builder
		 */
		public Builder addShard(ShardRouting shard, boolean fromApi) {
			return internalAddShard(new ImmutableShardRouting(shard), fromApi);
		}

		/**
		 * Adds the shard.
		 *
		 * @param shardId the shard id
		 * @param nodeId the node id
		 * @param primary the primary
		 * @param state the state
		 * @param version the version
		 * @param fromApi the from api
		 * @return the builder
		 */
		private Builder addShard(int shardId, String nodeId, boolean primary, ShardRoutingState state, long version,
				boolean fromApi) {
			ImmutableShardRouting shard = new ImmutableShardRouting(index, shardId, nodeId, primary, state, version);
			return internalAddShard(shard, fromApi);
		}

		/**
		 * Internal add shard.
		 *
		 * @param shard the shard
		 * @param fromApi the from api
		 * @return the builder
		 */
		private Builder internalAddShard(ImmutableShardRouting shard, boolean fromApi) {
			IndexShardRoutingTable indexShard = shards.get(shard.id());
			if (indexShard == null) {
				indexShard = new IndexShardRoutingTable.Builder(shard.shardId(), fromApi ? false : true)
						.addShard(shard).build();
			} else {
				indexShard = new IndexShardRoutingTable.Builder(indexShard).addShard(shard).build();
			}
			shards.put(indexShard.shardId().id(), indexShard);
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the index routing table
		 * @throws RoutingValidationException the routing validation exception
		 */
		public IndexRoutingTable build() throws RoutingValidationException {
			IndexRoutingTable indexRoutingTable = new IndexRoutingTable(index, ImmutableMap.copyOf(shards));
			indexRoutingTable.validate();
			return indexRoutingTable;
		}
	}

	/**
	 * Pretty print.
	 *
	 * @return the string
	 */
	public String prettyPrint() {
		StringBuilder sb = new StringBuilder("-- index [" + index + "]\n");
		for (IndexShardRoutingTable indexShard : this) {
			sb.append("----shard_id [").append(indexShard.shardId().index().name()).append("][")
					.append(indexShard.shardId().id()).append("]\n");
			for (ShardRouting shard : indexShard) {
				sb.append("--------").append(shard.shortSummary()).append("\n");
			}
		}
		return sb.toString();
	}

}
