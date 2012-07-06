/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core EnableMergePolicy.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.merge.policy;

/**
 * The Interface EnableMergePolicy.
 *
 * @author l.xue.nong
 */
public interface EnableMergePolicy {

	/**
	 * Checks if is merge enabled.
	 *
	 * @return true, if is merge enabled
	 */
	boolean isMergeEnabled();

	/**
	 * Enable merge.
	 */
	void enableMerge();

	/**
	 * Disable merge.
	 */
	void disableMerge();
}