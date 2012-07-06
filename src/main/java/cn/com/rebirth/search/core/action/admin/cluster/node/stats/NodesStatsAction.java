/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodesStatsAction.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.node.stats;

import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.client.ClusterAdminClient;


/**
 * The Class NodesStatsAction.
 *
 * @author l.xue.nong
 */
public class NodesStatsAction extends ClusterAction<NodesStatsRequest, NodesStatsResponse, NodesStatsRequestBuilder> {

    
    /** The Constant INSTANCE. */
    public static final NodesStatsAction INSTANCE = new NodesStatsAction();
    
    
    /** The Constant NAME. */
    public static final String NAME = "cluster/nodes/stats";

    
    /**
     * Instantiates a new nodes stats action.
     */
    private NodesStatsAction() {
        super(NAME);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.GenericAction#newResponse()
     */
    @Override
    public NodesStatsResponse newResponse() {
        return new NodesStatsResponse();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.admin.cluster.ClusterAction#newRequestBuilder(cn.com.summall.search.core.client.ClusterAdminClient)
     */
    @Override
    public NodesStatsRequestBuilder newRequestBuilder(ClusterAdminClient client) {
        return new NodesStatsRequestBuilder(client);
    }
}
