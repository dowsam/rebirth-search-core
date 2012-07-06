/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core Discovery.java 2012-3-29 15:01:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.discovery;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.commons.component.LifecycleComponent;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlock;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.node.service.NodeService;
import cn.com.rebirth.search.core.rest.RestStatus;


/**
 * The Interface Discovery.
 *
 * @author l.xue.nong
 */
public interface Discovery extends LifecycleComponent<Discovery> {

    
    /** The N o_ maste r_ block. */
    final ClusterBlock NO_MASTER_BLOCK = new ClusterBlock(2, "no master", true, true, RestStatus.SERVICE_UNAVAILABLE, ClusterBlockLevel.ALL);

    
    /**
     * Local node.
     *
     * @return the discovery node
     */
    DiscoveryNode localNode();

    
    /**
     * Adds the listener.
     *
     * @param listener the listener
     */
    void addListener(InitialStateDiscoveryListener listener);

    
    /**
     * Removes the listener.
     *
     * @param listener the listener
     */
    void removeListener(InitialStateDiscoveryListener listener);

    
    /**
     * Node description.
     *
     * @return the string
     */
    String nodeDescription();

    
    /**
     * Sets the node service.
     *
     * @param nodeService the new node service
     */
    void setNodeService(@Nullable NodeService nodeService);

    
    /**
     * Publish.
     *
     * @param clusterState the cluster state
     */
    void publish(ClusterState clusterState);
}
