/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MergePolicyProvider.java 2012-7-6 14:29:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.merge.policy;

import org.apache.lucene.index.MergePolicy;

import cn.com.rebirth.search.core.index.CloseableIndexComponent;
import cn.com.rebirth.search.core.index.shard.IndexShardComponent;

/**
 * The Interface MergePolicyProvider.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface MergePolicyProvider<T extends MergePolicy> extends IndexShardComponent, CloseableIndexComponent {

	/**
	 * New merge policy.
	 *
	 * @return the t
	 */
	T newMergePolicy();
}
