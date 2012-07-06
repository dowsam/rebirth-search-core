/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PlainShardsIterator.java 2012-7-6 14:29:10 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

import java.util.List;

/**
 * The Class PlainShardsIterator.
 *
 * @author l.xue.nong
 */
public class PlainShardsIterator implements ShardsIterator {

	/** The shards. */
	private final List<ShardRouting> shards;

	/** The size. */
	private final int size;

	/** The index. */
	private final int index;

	/** The limit. */
	private final int limit;

	/** The counter. */
	private volatile int counter;

	/**
	 * Instantiates a new plain shards iterator.
	 *
	 * @param shards the shards
	 */
	public PlainShardsIterator(List<ShardRouting> shards) {
		this(shards, 0);
	}

	/**
	 * Instantiates a new plain shards iterator.
	 *
	 * @param shards the shards
	 * @param index the index
	 */
	public PlainShardsIterator(List<ShardRouting> shards, int index) {
		this.shards = shards;
		this.size = shards.size();
		if (size == 0) {
			this.index = 0;
		} else {
			this.index = Math.abs(index % size);
		}
		this.counter = this.index;
		this.limit = this.index + size;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.ShardsIterator#reset()
	 */
	@Override
	public ShardsIterator reset() {
		this.counter = this.index;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.ShardsIterator#remaining()
	 */
	@Override
	public int remaining() {
		return limit - counter;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.ShardsIterator#firstOrNull()
	 */
	@Override
	public ShardRouting firstOrNull() {
		if (size == 0) {
			return null;
		}
		return shards.get(index);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.ShardsIterator#nextOrNull()
	 */
	@Override
	public ShardRouting nextOrNull() {
		if (size == 0) {
			return null;
		}
		int counter = (this.counter);
		if (counter >= size) {
			if (counter >= limit) {
				return null;
			}
			this.counter = counter + 1;
			return shards.get(counter - size);
		} else {
			this.counter = counter + 1;
			return shards.get(counter);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.ShardsIterator#size()
	 */
	@Override
	public int size() {
		return size;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.ShardsIterator#sizeActive()
	 */
	@Override
	public int sizeActive() {
		int count = 0;
		for (int i = 0; i < size; i++) {
			if (shards.get(i).active()) {
				count++;
			}
		}
		return count;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.ShardsIterator#assignedReplicasIncludingRelocating()
	 */
	@Override
	public int assignedReplicasIncludingRelocating() {
		int count = 0;
		for (int i = 0; i < size; i++) {
			ShardRouting shard = shards.get(i);
			if (shard.unassigned()) {
				continue;
			}

			if (shard.primary()) {
				if (shard.relocating()) {
					count++;
				}
			} else {
				count++;

				if (shard.relocating()) {
					count++;
				}
			}
		}
		return count;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.routing.ShardsIterator#asUnordered()
	 */
	@Override
	public Iterable<ShardRouting> asUnordered() {
		return shards;
	}
}
