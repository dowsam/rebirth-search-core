/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PlainOperationRouting.java 2012-7-6 14:30:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.operation.plain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.GroupShardsIterator;
import cn.com.rebirth.search.core.cluster.routing.IndexRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.IndexShardRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.cluster.routing.allocation.decider.AwarenessAllocationDecider;
import cn.com.rebirth.search.core.cluster.routing.operation.OperationRouting;
import cn.com.rebirth.search.core.cluster.routing.operation.hash.HashFunction;
import cn.com.rebirth.search.core.cluster.routing.operation.hash.djb.DjbHashFunction;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexShardMissingException;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.indices.IndexMissingException;

/**
 * The Class PlainOperationRouting.
 *
 * @author l.xue.nong
 */
public class PlainOperationRouting extends AbstractComponent implements OperationRouting {

	/** The hash function. */
	private final HashFunction hashFunction;

	/** The use type. */
	private final boolean useType;

	/** The awareness allocation decider. */
	private final AwarenessAllocationDecider awarenessAllocationDecider;

	/**
	 * Instantiates a new plain operation routing.
	 *
	 * @param indexSettings the index settings
	 * @param hashFunction the hash function
	 * @param awarenessAllocationDecider the awareness allocation decider
	 */
	@Inject
	public PlainOperationRouting(Settings indexSettings, HashFunction hashFunction,
			AwarenessAllocationDecider awarenessAllocationDecider) {
		super(indexSettings);
		this.hashFunction = hashFunction;
		this.useType = indexSettings.getAsBoolean("cluster.routing.operation.use_type", false);
		this.awarenessAllocationDecider = awarenessAllocationDecider;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.operation.OperationRouting#indexShards(cn.com.rebirth.search.core.cluster.ClusterState, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ShardIterator indexShards(ClusterState clusterState, String index, String type, String id,
			@Nullable String routing) throws IndexMissingException, IndexShardMissingException {
		return shards(clusterState, index, type, id, routing).shardsIt();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.operation.OperationRouting#deleteShards(cn.com.rebirth.search.core.cluster.ClusterState, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ShardIterator deleteShards(ClusterState clusterState, String index, String type, String id,
			@Nullable String routing) throws IndexMissingException, IndexShardMissingException {
		return shards(clusterState, index, type, id, routing).shardsIt();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.operation.OperationRouting#getShards(cn.com.rebirth.search.core.cluster.ClusterState, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ShardIterator getShards(ClusterState clusterState, String index, String type, String id,
			@Nullable String routing, @Nullable String preference) throws IndexMissingException,
			IndexShardMissingException {
		return preferenceActiveShardIterator(shards(clusterState, index, type, id, routing), clusterState.nodes()
				.localNodeId(), clusterState.nodes(), preference);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.operation.OperationRouting#getShards(cn.com.rebirth.search.core.cluster.ClusterState, java.lang.String, int, java.lang.String)
	 */
	@Override
	public ShardIterator getShards(ClusterState clusterState, String index, int shardId, @Nullable String preference)
			throws IndexMissingException, IndexShardMissingException {
		return preferenceActiveShardIterator(shards(clusterState, index, shardId), clusterState.nodes().localNodeId(),
				clusterState.nodes(), preference);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.operation.OperationRouting#broadcastDeleteShards(cn.com.rebirth.search.core.cluster.ClusterState, java.lang.String)
	 */
	@Override
	public GroupShardsIterator broadcastDeleteShards(ClusterState clusterState, String index)
			throws IndexMissingException {
		return indexRoutingTable(clusterState, index).groupByShardsIt();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.operation.OperationRouting#deleteByQueryShards(cn.com.rebirth.search.core.cluster.ClusterState, java.lang.String, java.util.Set)
	 */
	@Override
	public GroupShardsIterator deleteByQueryShards(ClusterState clusterState, String index,
			@Nullable Set<String> routing) throws IndexMissingException {
		if (routing == null || routing.isEmpty()) {
			return indexRoutingTable(clusterState, index).groupByShardsIt();
		}

		HashSet<ShardIterator> set = new HashSet<ShardIterator>();
		IndexRoutingTable indexRouting = indexRoutingTable(clusterState, index);
		for (String r : routing) {
			int shardId = shardId(clusterState, index, null, null, r);
			IndexShardRoutingTable indexShard = indexRouting.shard(shardId);
			if (indexShard == null) {
				throw new IndexShardMissingException(new ShardId(index, shardId));
			}
			set.add(indexShard.shardsRandomIt());
		}
		return new GroupShardsIterator(set);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.operation.OperationRouting#searchShardsCount(cn.com.rebirth.search.core.cluster.ClusterState, java.lang.String[], java.lang.String[], java.lang.String, java.util.Map, java.lang.String)
	 */
	@Override
	public int searchShardsCount(ClusterState clusterState, String[] indices, String[] concreteIndices,
			@Nullable String queryHint, @Nullable Map<String, Set<String>> routing, @Nullable String preference)
			throws IndexMissingException {
		if (concreteIndices == null || concreteIndices.length == 0) {
			concreteIndices = clusterState.metaData().concreteAllOpenIndices();
		}
		if (routing != null) {
			HashSet<ShardId> set = new HashSet<ShardId>();
			for (String index : concreteIndices) {
				IndexRoutingTable indexRouting = indexRoutingTable(clusterState, index);
				Set<String> effectiveRouting = routing.get(index);
				if (effectiveRouting != null) {
					for (String r : effectiveRouting) {
						int shardId = shardId(clusterState, index, null, null, r);
						IndexShardRoutingTable indexShard = indexRouting.shard(shardId);
						if (indexShard == null) {
							throw new IndexShardMissingException(new ShardId(index, shardId));
						}

						set.add(indexShard.shardId());
					}
				}
			}
			return set.size();
		} else {

			int count = 0;
			for (String index : concreteIndices) {
				IndexRoutingTable indexRouting = indexRoutingTable(clusterState, index);
				count += indexRouting.shards().size();
			}
			return count;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.operation.OperationRouting#searchShards(cn.com.rebirth.search.core.cluster.ClusterState, java.lang.String[], java.lang.String[], java.lang.String, java.util.Map, java.lang.String)
	 */
	@Override
	public GroupShardsIterator searchShards(ClusterState clusterState, String[] indices, String[] concreteIndices,
			@Nullable String queryHint, @Nullable Map<String, Set<String>> routing, @Nullable String preference)
			throws IndexMissingException {
		if (concreteIndices == null || concreteIndices.length == 0) {
			concreteIndices = clusterState.metaData().concreteAllOpenIndices();
		}

		if (routing != null) {

			HashSet<ShardIterator> set = new HashSet<ShardIterator>();
			for (String index : concreteIndices) {
				IndexRoutingTable indexRouting = indexRoutingTable(clusterState, index);
				Set<String> effectiveRouting = routing.get(index);
				if (effectiveRouting != null) {
					for (String r : effectiveRouting) {
						int shardId = shardId(clusterState, index, null, null, r);
						IndexShardRoutingTable indexShard = indexRouting.shard(shardId);
						if (indexShard == null) {
							throw new IndexShardMissingException(new ShardId(index, shardId));
						}

						set.add(preferenceActiveShardIterator(indexShard, clusterState.nodes().localNodeId(),
								clusterState.nodes(), preference));
					}
				}
			}
			return new GroupShardsIterator(set);
		} else {

			ArrayList<ShardIterator> set = new ArrayList<ShardIterator>();
			for (String index : concreteIndices) {
				IndexRoutingTable indexRouting = indexRoutingTable(clusterState, index);
				for (IndexShardRoutingTable indexShard : indexRouting) {
					set.add(preferenceActiveShardIterator(indexShard, clusterState.nodes().localNodeId(),
							clusterState.nodes(), preference));
				}
			}
			return new GroupShardsIterator(set);
		}
	}

	/**
	 * Preference active shard iterator.
	 *
	 * @param indexShard the index shard
	 * @param localNodeId the local node id
	 * @param nodes the nodes
	 * @param preference the preference
	 * @return the shard iterator
	 */
	private ShardIterator preferenceActiveShardIterator(IndexShardRoutingTable indexShard, String localNodeId,
			DiscoveryNodes nodes, @Nullable String preference) {
		if (preference == null) {
			String[] awarenessAttributes = awarenessAllocationDecider.awarenessAttributes();
			if (awarenessAttributes.length == 0) {
				return indexShard.activeShardsRandomIt();
			} else {
				return indexShard.preferAttributesActiveShardsIt(awarenessAttributes, nodes);
			}
		}
		if (preference.charAt(0) == '_') {
			if ("_local".equals(preference)) {
				return indexShard.preferNodeActiveShardsIt(localNodeId);
			}
			if ("_primary".equals(preference)) {
				return indexShard.primaryShardIt();
			}
			if ("_only_local".equals(preference) || "_onlyLocal".equals(preference)) {
				return indexShard.onlyNodeActiveShardsIt(localNodeId);
			}
			if (preference.startsWith("_only_node:")) {
				return indexShard.onlyNodeActiveShardsIt(preference.substring("_only_node:".length()));
			}
		}

		String[] awarenessAttributes = awarenessAllocationDecider.awarenessAttributes();
		if (awarenessAttributes.length == 0) {
			return indexShard.activeShardsIt(DjbHashFunction.DJB_HASH(preference));
		} else {
			return indexShard.preferAttributesActiveShardsIt(awarenessAttributes, nodes,
					DjbHashFunction.DJB_HASH(preference));
		}
	}

	/**
	 * Index meta data.
	 *
	 * @param clusterState the cluster state
	 * @param index the index
	 * @return the index meta data
	 */
	public IndexMetaData indexMetaData(ClusterState clusterState, String index) {
		IndexMetaData indexMetaData = clusterState.metaData().index(index);
		if (indexMetaData == null) {
			throw new IndexMissingException(new Index(index));
		}
		return indexMetaData;
	}

	/**
	 * Index routing table.
	 *
	 * @param clusterState the cluster state
	 * @param index the index
	 * @return the index routing table
	 */
	protected IndexRoutingTable indexRoutingTable(ClusterState clusterState, String index) {
		IndexRoutingTable indexRouting = clusterState.routingTable().index(index);
		if (indexRouting == null) {
			throw new IndexMissingException(new Index(index));
		}
		return indexRouting;
	}

	/**
	 * Shards.
	 *
	 * @param clusterState the cluster state
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @param routing the routing
	 * @return the index shard routing table
	 */
	protected IndexShardRoutingTable shards(ClusterState clusterState, String index, String type, String id,
			String routing) {
		int shardId = shardId(clusterState, index, type, id, routing);
		return shards(clusterState, index, shardId);
	}

	/**
	 * Shards.
	 *
	 * @param clusterState the cluster state
	 * @param index the index
	 * @param shardId the shard id
	 * @return the index shard routing table
	 */
	protected IndexShardRoutingTable shards(ClusterState clusterState, String index, int shardId) {
		IndexShardRoutingTable indexShard = indexRoutingTable(clusterState, index).shard(shardId);
		if (indexShard == null) {
			throw new IndexShardMissingException(new ShardId(index, shardId));
		}
		return indexShard;
	}

	/**
	 * Shard id.
	 *
	 * @param clusterState the cluster state
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @param routing the routing
	 * @return the int
	 */
	private int shardId(ClusterState clusterState, String index, String type, @Nullable String id,
			@Nullable String routing) {
		if (routing == null) {
			if (!useType) {
				return Math.abs(hash(id) % indexMetaData(clusterState, index).numberOfShards());
			} else {
				return Math.abs(hash(type, id) % indexMetaData(clusterState, index).numberOfShards());
			}
		}
		return Math.abs(hash(routing) % indexMetaData(clusterState, index).numberOfShards());
	}

	/**
	 * Hash.
	 *
	 * @param routing the routing
	 * @return the int
	 */
	protected int hash(String routing) {
		return hashFunction.hash(routing);
	}

	/**
	 * Hash.
	 *
	 * @param type the type
	 * @param id the id
	 * @return the int
	 */
	protected int hash(String type, String id) {
		if (type == null || "_all".equals(type)) {
			throw new RebirthIllegalArgumentException(
					"Can't route an operation with no type and having type part of the routing (for backward comp)");
		}
		return hashFunction.hash(type, id);
	}
}
