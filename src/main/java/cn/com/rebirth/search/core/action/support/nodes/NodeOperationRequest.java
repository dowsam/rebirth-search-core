/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodeOperationRequest.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support.nodes;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;


/**
 * The Class NodeOperationRequest.
 *
 * @author l.xue.nong
 */
public abstract class NodeOperationRequest implements Streamable {

    
    /** The node id. */
    private String nodeId;

    
    /**
     * Instantiates a new node operation request.
     */
    protected NodeOperationRequest() {

    }

    
    /**
     * Instantiates a new node operation request.
     *
     * @param nodeId the node id
     */
    protected NodeOperationRequest(String nodeId) {
        this.nodeId = nodeId;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        nodeId = in.readUTF();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeUTF(nodeId);
    }
}
