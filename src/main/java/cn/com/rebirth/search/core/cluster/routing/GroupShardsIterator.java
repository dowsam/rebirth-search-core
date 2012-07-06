/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GroupShardsIterator.java 2012-7-6 14:28:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

import java.util.Collection;
import java.util.Iterator;

/**
 * The Class GroupShardsIterator.
 *
 * @author l.xue.nong
 */
public class GroupShardsIterator implements Iterable<ShardIterator> {

	/** The iterators. */
	private final Collection<ShardIterator> iterators;

	/**
	 * Instantiates a new group shards iterator.
	 *
	 * @param iterators the iterators
	 */
	public GroupShardsIterator(Collection<ShardIterator> iterators) {
		this.iterators = iterators;
	}

	/**
	 * Total size.
	 *
	 * @return the int
	 */
	public int totalSize() {
		int size = 0;
		for (ShardIterator shard : iterators) {
			size += shard.size();
		}
		return size;
	}

	/**
	 * Total size with1 for empty.
	 *
	 * @return the int
	 */
	public int totalSizeWith1ForEmpty() {
		int size = 0;
		for (ShardIterator shard : iterators) {
			int sizeActive = shard.size();
			if (sizeActive == 0) {
				size += 1;
			} else {
				size += sizeActive;
			}
		}
		return size;
	}

	/**
	 * Size.
	 *
	 * @return the int
	 */
	public int size() {
		return iterators.size();
	}

	/**
	 * Iterators.
	 *
	 * @return the collection
	 */
	public Collection<ShardIterator> iterators() {
		return iterators;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ShardIterator> iterator() {
		return iterators.iterator();
	}
}
