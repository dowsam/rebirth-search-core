/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PlainShardIterator.java 2012-7-6 14:29:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

import java.util.List;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class PlainShardIterator.
 *
 * @author l.xue.nong
 */
public class PlainShardIterator extends PlainShardsIterator implements ShardIterator {

	/** The shard id. */
	private final ShardId shardId;

	/**
	 * Instantiates a new plain shard iterator.
	 *
	 * @param shardId the shard id
	 * @param shards the shards
	 */
	public PlainShardIterator(ShardId shardId, List<ShardRouting> shards) {
		super(shards);
		this.shardId = shardId;
	}

	/**
	 * Instantiates a new plain shard iterator.
	 *
	 * @param shardId the shard id
	 * @param shards the shards
	 * @param index the index
	 */
	public PlainShardIterator(ShardId shardId, List<ShardRouting> shards, int index) {
		super(shards, index);
		this.shardId = shardId;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.PlainShardsIterator#reset()
	 */
	@Override
	public ShardIterator reset() {
		super.reset();
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.ShardIterator#shardId()
	 */
	@Override
	public ShardId shardId() {
		return this.shardId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		ShardIterator that = (ShardIterator) o;
		return shardId.equals(that.shardId());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return shardId.hashCode();
	}
}
