/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RiverClusterStateUpdateTask.java 2012-3-29 15:02:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.river.cluster;


/**
 * The Interface RiverClusterStateUpdateTask.
 *
 * @author l.xue.nong
 */
public interface RiverClusterStateUpdateTask {

    /**
     * Execute.
     *
     * @param currentState the current state
     * @return the river cluster state
     */
    RiverClusterState execute(RiverClusterState currentState);
}
