/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodesShutdownAction.java 2012-3-29 15:01:09 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.node.shutdown;

import cn.com.rebirth.search.core.action.admin.cluster.ClusterAction;
import cn.com.rebirth.search.core.client.ClusterAdminClient;


/**
 * The Class NodesShutdownAction.
 *
 * @author l.xue.nong
 */
public class NodesShutdownAction extends ClusterAction<NodesShutdownRequest, NodesShutdownResponse, NodesShutdownRequestBuilder> {

    
    /** The Constant INSTANCE. */
    public static final NodesShutdownAction INSTANCE = new NodesShutdownAction();
    
    
    /** The Constant NAME. */
    public static final String NAME = "cluster/nodes/shutdown";

    
    /**
     * Instantiates a new nodes shutdown action.
     */
    private NodesShutdownAction() {
        super(NAME);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.GenericAction#newResponse()
     */
    @Override
    public NodesShutdownResponse newResponse() {
        return new NodesShutdownResponse();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.admin.cluster.ClusterAction#newRequestBuilder(cn.com.summall.search.core.client.ClusterAdminClient)
     */
    @Override
    public NodesShutdownRequestBuilder newRequestBuilder(ClusterAdminClient client) {
        return new NodesShutdownRequestBuilder(client);
    }
}
