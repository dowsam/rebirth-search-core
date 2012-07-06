/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MasterNodeOperationRequest.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support.master;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequest;


/**
 * The Class MasterNodeOperationRequest.
 *
 * @author l.xue.nong
 */
public abstract class MasterNodeOperationRequest implements ActionRequest {

    
    /** The DEFAUL t_ maste r_ nod e_ timeout. */
    public static TimeValue DEFAULT_MASTER_NODE_TIMEOUT = TimeValue.timeValueSeconds(30);

    
    /** The master node timeout. */
    protected TimeValue masterNodeTimeout = DEFAULT_MASTER_NODE_TIMEOUT;

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded()
     */
    @Override
    public boolean listenerThreaded() {
        
        return true;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded(boolean)
     */
    @Override
    public MasterNodeOperationRequest listenerThreaded(boolean listenerThreaded) {
        
        return this;
    }

    
    /**
     * Master node timeout.
     *
     * @param timeout the timeout
     * @return the master node operation request
     */
    public MasterNodeOperationRequest masterNodeTimeout(TimeValue timeout) {
        this.masterNodeTimeout = timeout;
        return this;
    }

    
    /**
     * Master node timeout.
     *
     * @param timeout the timeout
     * @return the master node operation request
     */
    public MasterNodeOperationRequest masterNodeTimeout(String timeout) {
        return masterNodeTimeout(TimeValue.parseTimeValue(timeout, null));
    }

    
    /**
     * Master node timeout.
     *
     * @return the time value
     */
    public TimeValue masterNodeTimeout() {
        return this.masterNodeTimeout;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        masterNodeTimeout = TimeValue.readTimeValue(in);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        masterNodeTimeout.writeTo(out);
    }
}
