/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MergeSchedulerProvider.java 2012-7-6 14:30:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.merge.scheduler;

import org.apache.lucene.index.MergeScheduler;

import cn.com.rebirth.search.core.index.merge.MergeStats;
import cn.com.rebirth.search.core.index.shard.IndexShardComponent;

/**
 * The Interface MergeSchedulerProvider.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface MergeSchedulerProvider<T extends MergeScheduler> extends IndexShardComponent {

	/**
	 * New merge scheduler.
	 *
	 * @return the t
	 */
	T newMergeScheduler();

	/**
	 * Stats.
	 *
	 * @return the merge stats
	 */
	MergeStats stats();
}
