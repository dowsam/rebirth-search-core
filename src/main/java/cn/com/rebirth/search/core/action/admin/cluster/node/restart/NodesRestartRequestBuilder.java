/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodesRestartRequestBuilder.java 2012-3-29 15:00:54 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.node.restart;

import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.support.BaseClusterRequestBuilder;
import cn.com.rebirth.search.core.client.ClusterAdminClient;


/**
 * The Class NodesRestartRequestBuilder.
 *
 * @author l.xue.nong
 */
public class NodesRestartRequestBuilder extends BaseClusterRequestBuilder<NodesRestartRequest, NodesRestartResponse> {

    
    /**
     * Instantiates a new nodes restart request builder.
     *
     * @param clusterClient the cluster client
     */
    public NodesRestartRequestBuilder(ClusterAdminClient clusterClient) {
        super(clusterClient, new NodesRestartRequest());
    }

    
    /**
     * Sets the nodes ids.
     *
     * @param nodesIds the nodes ids
     * @return the nodes restart request builder
     */
    public NodesRestartRequestBuilder setNodesIds(String... nodesIds) {
        request.nodesIds(nodesIds);
        return this;
    }

    
    /**
     * Sets the delay.
     *
     * @param delay the delay
     * @return the nodes restart request builder
     */
    public NodesRestartRequestBuilder setDelay(TimeValue delay) {
        request.delay(delay);
        return this;
    }

    
    /**
     * Sets the delay.
     *
     * @param delay the delay
     * @return the nodes restart request builder
     */
    public NodesRestartRequestBuilder setDelay(String delay) {
        request.delay(delay);
        return this;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.admin.cluster.support.BaseClusterRequestBuilder#doExecute(cn.com.summall.search.core.action.ActionListener)
     */
    @Override
    protected void doExecute(ActionListener<NodesRestartResponse> listener) {
        client.nodesRestart(request, listener);
    }
}