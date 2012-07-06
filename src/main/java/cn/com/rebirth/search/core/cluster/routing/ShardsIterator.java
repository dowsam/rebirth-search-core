/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardsIterator.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

/**
 * The Interface ShardsIterator.
 *
 * @author l.xue.nong
 */
public interface ShardsIterator {

	/**
	 * Reset.
	 *
	 * @return the shards iterator
	 */
	ShardsIterator reset();

	/**
	 * Size.
	 *
	 * @return the int
	 */
	int size();

	/**
	 * Size active.
	 *
	 * @return the int
	 */
	int sizeActive();

	/**
	 * Assigned replicas including relocating.
	 *
	 * @return the int
	 */
	int assignedReplicasIncludingRelocating();

	/**
	 * Next or null.
	 *
	 * @return the shard routing
	 */
	ShardRouting nextOrNull();

	/**
	 * First or null.
	 *
	 * @return the shard routing
	 */
	ShardRouting firstOrNull();

	/**
	 * Remaining.
	 *
	 * @return the int
	 */
	int remaining();

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	int hashCode();

	/**
	 * Equals.
	 *
	 * @param other the other
	 * @return true, if successful
	 */
	boolean equals(Object other);

	/**
	 * As unordered.
	 *
	 * @return the iterable
	 */
	Iterable<ShardRouting> asUnordered();
}
