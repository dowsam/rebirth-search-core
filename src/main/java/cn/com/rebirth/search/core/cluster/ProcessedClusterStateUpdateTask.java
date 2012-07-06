/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ProcessedClusterStateUpdateTask.java 2012-3-29 15:00:48 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster;


/**
 * The Interface ProcessedClusterStateUpdateTask.
 *
 * @author l.xue.nong
 */
public interface ProcessedClusterStateUpdateTask extends ClusterStateUpdateTask {

    
    /**
     * Cluster state processed.
     *
     * @param clusterState the cluster state
     */
    void clusterStateProcessed(ClusterState clusterState);
}
