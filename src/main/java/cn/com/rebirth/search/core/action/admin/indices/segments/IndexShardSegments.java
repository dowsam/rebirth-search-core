/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardSegments.java 2012-7-6 14:30:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.segments;

import java.util.Iterator;

import cn.com.rebirth.search.core.index.shard.ShardId;

import com.google.common.collect.Iterators;

/**
 * The Class IndexShardSegments.
 *
 * @author l.xue.nong
 */
public class IndexShardSegments implements Iterable<ShardSegments> {

	/** The shard id. */
	private final ShardId shardId;

	/** The shards. */
	private final ShardSegments[] shards;

	/**
	 * Instantiates a new index shard segments.
	 *
	 * @param shardId the shard id
	 * @param shards the shards
	 */
	IndexShardSegments(ShardId shardId, ShardSegments[] shards) {
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
		return this.shardId;
	}

	/**
	 * Gets the at.
	 *
	 * @param i the i
	 * @return the at
	 */
	public ShardSegments getAt(int i) {
		return shards[i];
	}

	/**
	 * Shards.
	 *
	 * @return the shard segments[]
	 */
	public ShardSegments[] shards() {
		return this.shards;
	}

	/**
	 * Gets the shards.
	 *
	 * @return the shards
	 */
	public ShardSegments[] getShards() {
		return this.shards;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ShardSegments> iterator() {
		return Iterators.forArray(shards);
	}
}