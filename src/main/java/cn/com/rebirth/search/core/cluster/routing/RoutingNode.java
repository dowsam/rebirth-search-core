/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RoutingNode.java 2012-7-6 14:29:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

/**
 * The Class RoutingNode.
 *
 * @author l.xue.nong
 */
public class RoutingNode implements Iterable<MutableShardRouting> {

	/** The node id. */
	private final String nodeId;

	/** The node. */
	private final DiscoveryNode node;

	/** The shards. */
	private final List<MutableShardRouting> shards;

	/**
	 * Instantiates a new routing node.
	 *
	 * @param nodeId the node id
	 * @param node the node
	 */
	public RoutingNode(String nodeId, DiscoveryNode node) {
		this(nodeId, node, new ArrayList<MutableShardRouting>());
	}

	/**
	 * Instantiates a new routing node.
	 *
	 * @param nodeId the node id
	 * @param node the node
	 * @param shards the shards
	 */
	public RoutingNode(String nodeId, DiscoveryNode node, List<MutableShardRouting> shards) {
		this.nodeId = nodeId;
		this.node = node;
		this.shards = shards;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<MutableShardRouting> iterator() {
		return shards.iterator();
	}

	/**
	 * Node.
	 *
	 * @return the discovery node
	 */
	public DiscoveryNode node() {
		return this.node;
	}

	/**
	 * Node id.
	 *
	 * @return the string
	 */
	public String nodeId() {
		return this.nodeId;
	}

	/**
	 * Shards.
	 *
	 * @return the list
	 */
	public List<MutableShardRouting> shards() {
		return this.shards;
	}

	/**
	 * Adds the.
	 *
	 * @param shard the shard
	 */
	public void add(MutableShardRouting shard) {
		shards.add(shard);
		shard.assignToNode(node.id());
	}

	/**
	 * Removes the by shard id.
	 *
	 * @param shardId the shard id
	 */
	public void removeByShardId(int shardId) {
		for (Iterator<MutableShardRouting> it = shards.iterator(); it.hasNext();) {
			MutableShardRouting shard = it.next();
			if (shard.id() == shardId) {
				it.remove();
			}
		}
	}

	/**
	 * Number of shards with state.
	 *
	 * @param states the states
	 * @return the int
	 */
	public int numberOfShardsWithState(ShardRoutingState... states) {
		int count = 0;
		for (MutableShardRouting shardEntry : this) {
			for (ShardRoutingState state : states) {
				if (shardEntry.state() == state) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Shards with state.
	 *
	 * @param states the states
	 * @return the list
	 */
	public List<MutableShardRouting> shardsWithState(ShardRoutingState... states) {
		List<MutableShardRouting> shards = newArrayList();
		for (MutableShardRouting shardEntry : this) {
			for (ShardRoutingState state : states) {
				if (shardEntry.state() == state) {
					shards.add(shardEntry);
				}
			}
		}
		return shards;
	}

	/**
	 * Shards with state.
	 *
	 * @param index the index
	 * @param states the states
	 * @return the list
	 */
	public List<MutableShardRouting> shardsWithState(String index, ShardRoutingState... states) {
		List<MutableShardRouting> shards = newArrayList();
		for (MutableShardRouting shardEntry : this) {
			if (!shardEntry.index().equals(index)) {
				continue;
			}
			for (ShardRoutingState state : states) {
				if (shardEntry.state() == state) {
					shards.add(shardEntry);
				}
			}
		}
		return shards;
	}

	/**
	 * Number of shards not with state.
	 *
	 * @param state the state
	 * @return the int
	 */
	public int numberOfShardsNotWithState(ShardRoutingState state) {
		int count = 0;
		for (MutableShardRouting shardEntry : this) {
			if (shardEntry.state() != state) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Number of owning shards.
	 *
	 * @return the int
	 */
	public int numberOfOwningShards() {
		int count = 0;
		for (MutableShardRouting shardEntry : this) {
			if (shardEntry.state() != ShardRoutingState.RELOCATING) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Pretty print.
	 *
	 * @return the string
	 */
	public String prettyPrint() {
		StringBuilder sb = new StringBuilder();
		sb.append("-----node_id[").append(nodeId).append("][" + (node == null ? "X" : "V") + "]\n");
		for (MutableShardRouting entry : shards) {
			sb.append("--------").append(entry.shortSummary()).append('\n');
		}
		return sb.toString();
	}
}
