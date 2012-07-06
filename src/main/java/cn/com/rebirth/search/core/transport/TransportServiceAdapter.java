/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportServiceAdapter.java 2012-3-29 15:02:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;


/**
 * The Interface TransportServiceAdapter.
 *
 * @author l.xue.nong
 */
public interface TransportServiceAdapter {

    
    /**
     * Received.
     *
     * @param size the size
     */
    void received(long size);

    
    /**
     * Sent.
     *
     * @param size the size
     */
    void sent(long size);

    
    /**
     * Handler.
     *
     * @param action the action
     * @return the transport request handler
     */
    TransportRequestHandler handler(String action);

    
    /**
     * Removes the.
     *
     * @param requestId the request id
     * @return the transport response handler
     */
    TransportResponseHandler remove(long requestId);

    
    /**
     * Raise node connected.
     *
     * @param node the node
     */
    void raiseNodeConnected(DiscoveryNode node);

    
    /**
     * Raise node disconnected.
     *
     * @param node the node
     */
    void raiseNodeDisconnected(DiscoveryNode node);
}
