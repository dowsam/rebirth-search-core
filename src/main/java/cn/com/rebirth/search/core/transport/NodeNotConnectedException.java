/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodeNotConnectedException.java 2012-3-29 15:02:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;


/**
 * The Class NodeNotConnectedException.
 *
 * @author l.xue.nong
 */
public class NodeNotConnectedException extends ConnectTransportException {

    
    /**
     * Instantiates a new node not connected exception.
     *
     * @param node the node
     * @param msg the msg
     */
    public NodeNotConnectedException(DiscoveryNode node, String msg) {
        super(node, msg);
    }

    
    /**
     * Instantiates a new node not connected exception.
     *
     * @param node the node
     * @param msg the msg
     * @param cause the cause
     */
    public NodeNotConnectedException(DiscoveryNode node, String msg, Throwable cause) {
        super(node, msg, cause);
    }
}
