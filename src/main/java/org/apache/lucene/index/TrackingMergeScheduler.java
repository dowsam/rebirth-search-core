/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TrackingMergeScheduler.java 2012-7-6 14:29:23 l.xue.nong$$
 */

package org.apache.lucene.index;

import java.util.concurrent.ConcurrentMap;

import cn.com.rebirth.commons.concurrent.ConcurrentCollections;

/**
 * The Class TrackingMergeScheduler.
 *
 * @author l.xue.nong
 */
public class TrackingMergeScheduler {

	/** The Constant merges. */
	private static final ConcurrentMap<Thread, MergePolicy.OneMerge> merges = ConcurrentCollections.newConcurrentMap();

	/**
	 * Sets the current merge.
	 *
	 * @param merge the new current merge
	 */
	public static void setCurrentMerge(MergePolicy.OneMerge merge) {
		merges.put(Thread.currentThread(), merge);
	}

	/**
	 * Removes the current merge.
	 */
	public static void removeCurrentMerge() {
		merges.remove(Thread.currentThread());
	}

	/**
	 * Gets the current merge.
	 *
	 * @return the current merge
	 */
	public static MergePolicy.OneMerge getCurrentMerge() {
		return merges.get(Thread.currentThread());
	}
}
