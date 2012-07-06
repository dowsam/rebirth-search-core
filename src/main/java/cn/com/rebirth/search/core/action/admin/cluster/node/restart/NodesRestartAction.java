/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodesRestartAction.java 2012-3-29 15:02:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.node.restart;

import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.client.ClusterAdminClient;


/**
 * The Class NodesRestartAction.
 *
 * @author l.xue.nong
 */
public class NodesRestartAction extends ClusterAction<NodesRestartRequest, NodesRestartResponse, NodesRestartRequestBuilder> {

    
    /** The Constant INSTANCE. */
    public static final NodesRestartAction INSTANCE = new NodesRestartAction();
    
    
    /** The Constant NAME. */
    public static final String NAME = "cluster/nodes/restart";

    
    /**
     * Instantiates a new nodes restart action.
     */
    private NodesRestartAction() {
        super(NAME);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.GenericAction#newResponse()
     */
    @Override
    public NodesRestartResponse newResponse() {
        return new NodesRestartResponse();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.admin.cluster.ClusterAction#newRequestBuilder(cn.com.summall.search.core.client.ClusterAdminClient)
     */
    @Override
    public NodesRestartRequestBuilder newRequestBuilder(ClusterAdminClient client) {
        return new NodesRestartRequestBuilder(client);
    }
}
