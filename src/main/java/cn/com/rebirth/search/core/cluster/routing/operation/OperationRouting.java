/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core OperationRouting.java 2012-3-29 15:00:59 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.operation;

import java.util.Map;
import java.util.Set;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.routing.GroupShardsIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.index.IndexShardMissingException;
import cn.com.rebirth.search.core.indices.IndexMissingException;


/**
 * The Interface OperationRouting.
 *
 * @author l.xue.nong
 */
public interface OperationRouting {

	
	/**
	 * Index shards.
	 *
	 * @param clusterState the cluster state
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @param routing the routing
	 * @return the shard iterator
	 * @throws IndexMissingException the index missing exception
	 * @throws IndexShardMissingException the index shard missing exception
	 */
	ShardIterator indexShards(ClusterState clusterState, String index, String type, String id, @Nullable String routing)
			throws IndexMissingException, IndexShardMissingException;

	
	/**
	 * Delete shards.
	 *
	 * @param clusterState the cluster state
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @param routing the routing
	 * @return the shard iterator
	 * @throws IndexMissingException the index missing exception
	 * @throws IndexShardMissingException the index shard missing exception
	 */
	ShardIterator deleteShards(ClusterState clusterState, String index, String type, String id, @Nullable String routing)
			throws IndexMissingException, IndexShardMissingException;

	
	/**
	 * Broadcast delete shards.
	 *
	 * @param clusterState the cluster state
	 * @param index the index
	 * @return the group shards iterator
	 * @throws IndexMissingException the index missing exception
	 * @throws IndexShardMissingException the index shard missing exception
	 */
	GroupShardsIterator broadcastDeleteShards(ClusterState clusterState, String index) throws IndexMissingException,
			IndexShardMissingException;

	
	/**
	 * Gets the shards.
	 *
	 * @param clusterState the cluster state
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @param routing the routing
	 * @param preference the preference
	 * @return the shards
	 * @throws IndexMissingException the index missing exception
	 * @throws IndexShardMissingException the index shard missing exception
	 */
	ShardIterator getShards(ClusterState clusterState, String index, String type, String id, @Nullable String routing,
			@Nullable String preference) throws IndexMissingException, IndexShardMissingException;

	
	/**
	 * Gets the shards.
	 *
	 * @param clusterState the cluster state
	 * @param index the index
	 * @param shardId the shard id
	 * @param preference the preference
	 * @return the shards
	 * @throws IndexMissingException the index missing exception
	 * @throws IndexShardMissingException the index shard missing exception
	 */
	ShardIterator getShards(ClusterState clusterState, String index, int shardId, @Nullable String preference)
			throws IndexMissingException, IndexShardMissingException;

	
	/**
	 * Delete by query shards.
	 *
	 * @param clusterState the cluster state
	 * @param index the index
	 * @param routing the routing
	 * @return the group shards iterator
	 * @throws IndexMissingException the index missing exception
	 */
	GroupShardsIterator deleteByQueryShards(ClusterState clusterState, String index, @Nullable Set<String> routing)
			throws IndexMissingException;

	
	/**
	 * Search shards count.
	 *
	 * @param clusterState the cluster state
	 * @param indices the indices
	 * @param concreteIndices the concrete indices
	 * @param queryHint the query hint
	 * @param routing the routing
	 * @param preference the preference
	 * @return the int
	 * @throws IndexMissingException the index missing exception
	 */
	int searchShardsCount(ClusterState clusterState, String[] indices, String[] concreteIndices,
			@Nullable String queryHint, @Nullable Map<String, Set<String>> routing, @Nullable String preference)
			throws IndexMissingException;

	
	/**
	 * Search shards.
	 *
	 * @param clusterState the cluster state
	 * @param indices the indices
	 * @param concreteIndices the concrete indices
	 * @param queryHint the query hint
	 * @param routing the routing
	 * @param preference the preference
	 * @return the group shards iterator
	 * @throws IndexMissingException the index missing exception
	 */
	GroupShardsIterator searchShards(ClusterState clusterState, String[] indices, String[] concreteIndices,
			@Nullable String queryHint, @Nullable Map<String, Set<String>> routing, @Nullable String preference)
			throws IndexMissingException;
}
