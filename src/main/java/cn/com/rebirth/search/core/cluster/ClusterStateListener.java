/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterStateListener.java 2012-3-29 15:02:05 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster;


/**
 * The listener interface for receiving clusterState events.
 * The class that is interested in processing a clusterState
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addClusterStateListener<code> method. When
 * the clusterState event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ClusterStateEvent
 */
public interface ClusterStateListener {

    
    /**
     * Cluster changed.
     *
     * @param event the event
     */
    void clusterChanged(ClusterChangedEvent event);
}
