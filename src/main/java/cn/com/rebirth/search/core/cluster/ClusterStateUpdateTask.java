/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterStateUpdateTask.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster;


/**
 * The Interface ClusterStateUpdateTask.
 *
 * @author l.xue.nong
 */
public interface ClusterStateUpdateTask {

    
    /**
     * Execute.
     *
     * @param currentState the current state
     * @return the cluster state
     */
    ClusterState execute(ClusterState currentState);
}
