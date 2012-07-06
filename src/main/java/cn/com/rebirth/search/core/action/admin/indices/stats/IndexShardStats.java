/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardStats.java 2012-3-29 15:01:01 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.stats;

import java.util.Iterator;

import cn.com.rebirth.search.core.index.shard.ShardId;

import com.google.common.collect.Iterators;


/**
 * The Class IndexShardStats.
 *
 * @author l.xue.nong
 */
public class IndexShardStats implements Iterable<ShardStats> {

	
	/** The shard id. */
	private final ShardId shardId;

	
	/** The shards. */
	private final ShardStats[] shards;

	
	/**
	 * Instantiates a new index shard stats.
	 *
	 * @param shardId the shard id
	 * @param shards the shards
	 */
	IndexShardStats(ShardId shardId, ShardStats[] shards) {
		this.shardId = shardId;
		this.shards = shards;
	}

	
	/**
	 * Shard id.
	 *
	 * @return the shard id
	 */
	public ShardId shardId() {
		return this.shardId;
	}

	
	/**
	 * Gets the shard id.
	 *
	 * @return the shard id
	 */
	public ShardId getShardId() {
		return shardId();
	}

	
	/**
	 * Shards.
	 *
	 * @return the shard stats[]
	 */
	public ShardStats[] shards() {
		return shards;
	}

	
	/**
	 * Gets the shards.
	 *
	 * @return the shards
	 */
	public ShardStats[] getShards() {
		return shards;
	}

	
	/**
	 * Gets the at.
	 *
	 * @param position the position
	 * @return the at
	 */
	public ShardStats getAt(int position) {
		return shards[position];
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ShardStats> iterator() {
		return Iterators.forArray(shards);
	}

	
	/** The total. */
	private CommonStats total = null;

	
	/**
	 * Total.
	 *
	 * @return the common stats
	 */
	public CommonStats total() {
		if (total != null) {
			return total;
		}
		CommonStats stats = new CommonStats();
		for (ShardStats shard : shards) {
			stats.add(shard.stats());
		}
		total = stats;
		return stats;
	}

	
	/** The primary. */
	private CommonStats primary = null;

	
	/**
	 * Primary.
	 *
	 * @return the common stats
	 */
	public CommonStats primary() {
		if (primary != null) {
			return primary;
		}
		CommonStats stats = new CommonStats();
		for (ShardStats shard : shards) {
			if (shard.shardRouting().primary()) {
				stats.add(shard.stats());
			}
		}
		primary = stats;
		return stats;
	}
}
