/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RoutingTable.java 2012-7-6 14:29:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.indices.IndexMissingException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

/**
 * The Class RoutingTable.
 *
 * @author l.xue.nong
 */
public class RoutingTable implements Iterable<IndexRoutingTable> {

	/** The Constant EMPTY_ROUTING_TABLE. */
	public static final RoutingTable EMPTY_ROUTING_TABLE = builder().build();

	/** The version. */
	private final long version;

	/** The indices routing. */
	private final ImmutableMap<String, IndexRoutingTable> indicesRouting;

	/**
	 * Instantiates a new routing table.
	 *
	 * @param version the version
	 * @param indicesRouting the indices routing
	 */
	RoutingTable(long version, Map<String, IndexRoutingTable> indicesRouting) {
		this.version = version;
		this.indicesRouting = ImmutableMap.copyOf(indicesRouting);
	}

	/**
	 * Version.
	 *
	 * @return the long
	 */
	public long version() {
		return this.version;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public UnmodifiableIterator<IndexRoutingTable> iterator() {
		return indicesRouting.values().iterator();
	}

	/**
	 * Checks for index.
	 *
	 * @param index the index
	 * @return true, if successful
	 */
	public boolean hasIndex(String index) {
		return indicesRouting.containsKey(index);
	}

	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the index routing table
	 */
	public IndexRoutingTable index(String index) {
		return indicesRouting.get(index);
	}

	/**
	 * Indices routing.
	 *
	 * @return the map
	 */
	public Map<String, IndexRoutingTable> indicesRouting() {
		return indicesRouting;
	}

	/**
	 * Gets the indices routing.
	 *
	 * @return the indices routing
	 */
	public Map<String, IndexRoutingTable> getIndicesRouting() {
		return indicesRouting();
	}

	/**
	 * Routing nodes.
	 *
	 * @param state the state
	 * @return the routing nodes
	 */
	public RoutingNodes routingNodes(ClusterState state) {
		return new RoutingNodes(state);
	}

	/**
	 * Validate raise exception.
	 *
	 * @param metaData the meta data
	 * @return the routing table
	 * @throws RoutingValidationException the routing validation exception
	 */
	public RoutingTable validateRaiseException(MetaData metaData) throws RoutingValidationException {
		RoutingTableValidation validation = validate(metaData);
		if (!validation.valid()) {
			throw new RoutingValidationException(validation);
		}
		return this;
	}

	/**
	 * Validate.
	 *
	 * @param metaData the meta data
	 * @return the routing table validation
	 */
	public RoutingTableValidation validate(MetaData metaData) {
		RoutingTableValidation validation = new RoutingTableValidation();
		for (IndexRoutingTable indexRoutingTable : this) {
			indexRoutingTable.validate(validation, metaData);
		}
		return validation;
	}

	/**
	 * Shards with state.
	 *
	 * @param states the states
	 * @return the list
	 */
	public List<ShardRouting> shardsWithState(ShardRoutingState... states) {
		List<ShardRouting> shards = newArrayList();
		for (IndexRoutingTable indexRoutingTable : this) {
			shards.addAll(indexRoutingTable.shardsWithState(states));
		}
		return shards;
	}

	/**
	 * All shards.
	 *
	 * @param indices the indices
	 * @return the list
	 * @throws IndexMissingException the index missing exception
	 */
	public List<ShardRouting> allShards(String... indices) throws IndexMissingException {
		List<ShardRouting> shards = Lists.newArrayList();
		if (indices == null || indices.length == 0) {
			indices = indicesRouting.keySet().toArray(new String[indicesRouting.keySet().size()]);
		}
		for (String index : indices) {
			IndexRoutingTable indexRoutingTable = index(index);
			if (indexRoutingTable == null) {
				throw new IndexMissingException(new Index(index));
			}
			for (IndexShardRoutingTable indexShardRoutingTable : indexRoutingTable) {
				for (ShardRouting shardRouting : indexShardRoutingTable) {
					shards.add(shardRouting);
				}
			}
		}
		return shards;
	}

	/**
	 * All shards grouped.
	 *
	 * @param indices the indices
	 * @return the group shards iterator
	 * @throws IndexMissingException the index missing exception
	 */
	public GroupShardsIterator allShardsGrouped(String... indices) throws IndexMissingException {

		ArrayList<ShardIterator> set = new ArrayList<ShardIterator>();
		if (indices == null || indices.length == 0) {
			indices = indicesRouting.keySet().toArray(new String[indicesRouting.keySet().size()]);
		}
		for (String index : indices) {
			IndexRoutingTable indexRoutingTable = index(index);
			if (indexRoutingTable == null) {
				continue;

			}
			for (IndexShardRoutingTable indexShardRoutingTable : indexRoutingTable) {
				for (ShardRouting shardRouting : indexShardRoutingTable) {
					set.add(shardRouting.shardsIt());
				}
			}
		}
		return new GroupShardsIterator(set);
	}

	/**
	 * All active shards grouped.
	 *
	 * @param indices the indices
	 * @param includeEmpty the include empty
	 * @return the group shards iterator
	 * @throws IndexMissingException the index missing exception
	 */
	public GroupShardsIterator allActiveShardsGrouped(String[] indices, boolean includeEmpty)
			throws IndexMissingException {

		ArrayList<ShardIterator> set = new ArrayList<ShardIterator>();
		if (indices == null || indices.length == 0) {
			indices = indicesRouting.keySet().toArray(new String[indicesRouting.keySet().size()]);
		}
		for (String index : indices) {
			IndexRoutingTable indexRoutingTable = index(index);
			if (indexRoutingTable == null) {
				continue;

			}
			for (IndexShardRoutingTable indexShardRoutingTable : indexRoutingTable) {
				for (ShardRouting shardRouting : indexShardRoutingTable) {
					if (shardRouting.active()) {
						set.add(shardRouting.shardsIt());
					} else if (includeEmpty) {
						set.add(new PlainShardIterator(shardRouting.shardId(), ImmutableList.<ShardRouting> of()));
					}
				}
			}
		}
		return new GroupShardsIterator(set);
	}

	/**
	 * All assigned shards grouped.
	 *
	 * @param indices the indices
	 * @param includeEmpty the include empty
	 * @return the group shards iterator
	 * @throws IndexMissingException the index missing exception
	 */
	public GroupShardsIterator allAssignedShardsGrouped(String[] indices, boolean includeEmpty)
			throws IndexMissingException {

		ArrayList<ShardIterator> set = new ArrayList<ShardIterator>();
		if (indices == null || indices.length == 0) {
			indices = indicesRouting.keySet().toArray(new String[indicesRouting.keySet().size()]);
		}
		for (String index : indices) {
			IndexRoutingTable indexRoutingTable = index(index);
			if (indexRoutingTable == null) {
				continue;

			}
			for (IndexShardRoutingTable indexShardRoutingTable : indexRoutingTable) {
				for (ShardRouting shardRouting : indexShardRoutingTable) {
					if (shardRouting.assignedToNode()) {
						set.add(shardRouting.shardsIt());
					} else if (includeEmpty) {
						set.add(new PlainShardIterator(shardRouting.shardId(), ImmutableList.<ShardRouting> of()));
					}
				}
			}
		}
		return new GroupShardsIterator(set);
	}

	/**
	 * Active primary shards grouped.
	 *
	 * @param indices the indices
	 * @param includeEmpty the include empty
	 * @return the group shards iterator
	 * @throws IndexMissingException the index missing exception
	 */
	public GroupShardsIterator activePrimaryShardsGrouped(String[] indices, boolean includeEmpty)
			throws IndexMissingException {

		ArrayList<ShardIterator> set = new ArrayList<ShardIterator>();
		if (indices == null || indices.length == 0) {
			indices = indicesRouting.keySet().toArray(new String[indicesRouting.keySet().size()]);
		}
		for (String index : indices) {
			IndexRoutingTable indexRoutingTable = index(index);
			if (indexRoutingTable == null) {
				throw new IndexMissingException(new Index(index));
			}
			for (IndexShardRoutingTable indexShardRoutingTable : indexRoutingTable) {
				ShardRouting primary = indexShardRoutingTable.primaryShard();
				if (primary.active()) {
					set.add(primary.shardsIt());
				} else if (includeEmpty) {
					set.add(new PlainShardIterator(primary.shardId(), ImmutableList.<ShardRouting> of()));
				}
			}
		}
		return new GroupShardsIterator(set);
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
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder {

		/** The version. */
		private long version;

		/** The indices routing. */
		private final Map<String, IndexRoutingTable> indicesRouting = newHashMap();

		/**
		 * Routing table.
		 *
		 * @param routingTable the routing table
		 * @return the builder
		 */
		public Builder routingTable(RoutingTable routingTable) {
			version = routingTable.version;
			for (IndexRoutingTable indexRoutingTable : routingTable) {
				indicesRouting.put(indexRoutingTable.index(), indexRoutingTable);
			}
			return this;
		}

		/**
		 * Update nodes.
		 *
		 * @param routingNodes the routing nodes
		 * @return the builder
		 */
		public Builder updateNodes(RoutingNodes routingNodes) {

			this.version = routingNodes.routingTable().version();

			Map<String, IndexRoutingTable.Builder> indexRoutingTableBuilders = newHashMap();
			for (RoutingNode routingNode : routingNodes) {
				for (MutableShardRouting shardRoutingEntry : routingNode) {

					if (shardRoutingEntry.state() == ShardRoutingState.INITIALIZING
							&& shardRoutingEntry.relocatingNodeId() != null)
						continue;

					String index = shardRoutingEntry.index();
					IndexRoutingTable.Builder indexBuilder = indexRoutingTableBuilders.get(index);
					if (indexBuilder == null) {
						indexBuilder = new IndexRoutingTable.Builder(index);
						indexRoutingTableBuilders.put(index, indexBuilder);
					}

					boolean allocatedPostApi = routingNodes.routingTable().index(shardRoutingEntry.index())
							.shard(shardRoutingEntry.id()).allocatedPostApi();
					indexBuilder.addShard(new ImmutableShardRouting(shardRoutingEntry), !allocatedPostApi);
				}
			}
			for (MutableShardRouting shardRoutingEntry : Iterables.concat(routingNodes.unassigned(),
					routingNodes.ignoredUnassigned())) {
				String index = shardRoutingEntry.index();
				IndexRoutingTable.Builder indexBuilder = indexRoutingTableBuilders.get(index);
				if (indexBuilder == null) {
					indexBuilder = new IndexRoutingTable.Builder(index);
					indexRoutingTableBuilders.put(index, indexBuilder);
				}
				boolean allocatedPostApi = routingNodes.routingTable().index(shardRoutingEntry.index())
						.shard(shardRoutingEntry.id()).allocatedPostApi();
				indexBuilder.addShard(new ImmutableShardRouting(shardRoutingEntry), !allocatedPostApi);
			}
			for (IndexRoutingTable.Builder indexBuilder : indexRoutingTableBuilders.values()) {
				add(indexBuilder);
			}
			return this;
		}

		/**
		 * Update number of replicas.
		 *
		 * @param numberOfReplicas the number of replicas
		 * @param indices the indices
		 * @return the builder
		 * @throws IndexMissingException the index missing exception
		 */
		public Builder updateNumberOfReplicas(int numberOfReplicas, String... indices) throws IndexMissingException {
			if (indices == null || indices.length == 0) {
				indices = indicesRouting.keySet().toArray(new String[indicesRouting.keySet().size()]);
			}
			for (String index : indices) {
				IndexRoutingTable indexRoutingTable = indicesRouting.get(index);
				if (indexRoutingTable == null) {
					throw new IndexMissingException(new Index(index));
				}
				int currentNumberOfReplicas = indexRoutingTable.shards().get(0).size() - 1;
				IndexRoutingTable.Builder builder = new IndexRoutingTable.Builder(index);

				for (IndexShardRoutingTable indexShardRoutingTable : indexRoutingTable) {
					builder.addIndexShard(indexShardRoutingTable);
				}
				if (currentNumberOfReplicas < numberOfReplicas) {

					for (int i = 0; i < (numberOfReplicas - currentNumberOfReplicas); i++) {
						builder.addReplica();
					}
				} else if (currentNumberOfReplicas > numberOfReplicas) {
					int delta = currentNumberOfReplicas - numberOfReplicas;
					if (delta <= 0) {

					} else {
						for (int i = 0; i < delta; i++) {
							builder.removeReplica();
						}
					}
				}
				indicesRouting.put(index, builder.build());
			}
			return this;
		}

		/**
		 * Adds the.
		 *
		 * @param indexRoutingTable the index routing table
		 * @return the builder
		 */
		public Builder add(IndexRoutingTable indexRoutingTable) {
			indexRoutingTable.validate();
			indicesRouting.put(indexRoutingTable.index(), indexRoutingTable);
			return this;
		}

		/**
		 * Adds the.
		 *
		 * @param indexRoutingTableBuilder the index routing table builder
		 * @return the builder
		 */
		public Builder add(IndexRoutingTable.Builder indexRoutingTableBuilder) {
			add(indexRoutingTableBuilder.build());
			return this;
		}

		/**
		 * Removes the.
		 *
		 * @param index the index
		 * @return the builder
		 */
		public Builder remove(String index) {
			indicesRouting.remove(index);
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
		 * Builds the.
		 *
		 * @return the routing table
		 */
		public RoutingTable build() {

			for (IndexRoutingTable indexRoutingTable : indicesRouting.values()) {
				indicesRouting.put(indexRoutingTable.index(), indexRoutingTable.normalizeVersions());
			}
			return new RoutingTable(version, indicesRouting);
		}

		/**
		 * Read from.
		 *
		 * @param in the in
		 * @return the routing table
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static RoutingTable readFrom(StreamInput in) throws IOException {
			Builder builder = new Builder();
			builder.version = in.readLong();
			int size = in.readVInt();
			for (int i = 0; i < size; i++) {
				IndexRoutingTable index = IndexRoutingTable.Builder.readFrom(in);
				builder.add(index);
			}

			return builder.build();
		}

		/**
		 * Write to.
		 *
		 * @param table the table
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeTo(RoutingTable table, StreamOutput out) throws IOException {
			out.writeLong(table.version);
			out.writeVInt(table.indicesRouting.size());
			for (IndexRoutingTable index : table.indicesRouting.values()) {
				IndexRoutingTable.Builder.writeTo(index, out);
			}
		}
	}

	/**
	 * Pretty print.
	 *
	 * @return the string
	 */
	public String prettyPrint() {
		StringBuilder sb = new StringBuilder("routing_table:\n");
		for (Map.Entry<String, IndexRoutingTable> entry : indicesRouting.entrySet()) {
			sb.append(entry.getValue().prettyPrint()).append('\n');
		}
		return sb.toString();
	}

}
