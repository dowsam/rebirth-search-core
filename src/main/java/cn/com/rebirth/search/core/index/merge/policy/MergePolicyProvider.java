/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MergePolicyProvider.java 2012-3-29 15:01:24 l.xue.nong$$
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
