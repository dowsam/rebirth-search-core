/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core EnableMergePolicy.java 2012-3-29 15:01:09 l.xue.nong$$
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