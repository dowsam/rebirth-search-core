/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterRerouteRequest.java 2012-3-29 15:01:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.reroute;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;


/**
 * The Class ClusterRerouteRequest.
 *
 * @author l.xue.nong
 */
public class ClusterRerouteRequest extends MasterNodeOperationRequest {

    
    /**
     * Instantiates a new cluster reroute request.
     */
    public ClusterRerouteRequest() {
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.ActionRequest#validate()
     */
    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
    }
}
