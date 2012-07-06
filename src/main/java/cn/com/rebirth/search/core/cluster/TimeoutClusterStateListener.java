/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TimeoutClusterStateListener.java 2012-3-29 15:02:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster;

import cn.com.rebirth.commons.unit.TimeValue;


/**
 * The listener interface for receiving timeoutClusterState events.
 * The class that is interested in processing a timeoutClusterState
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addTimeoutClusterStateListener<code> method. When
 * the timeoutClusterState event occurs, that object's appropriate
 * method is invoked.
 *
 * @see TimeoutClusterStateEvent
 */
public interface TimeoutClusterStateListener extends ClusterStateListener {

    
    /**
     * Post added.
     */
    void postAdded();

    
    /**
     * On close.
     */
    void onClose();

    
    /**
     * On timeout.
     *
     * @param timeout the timeout
     */
    void onTimeout(TimeValue timeout);
}
