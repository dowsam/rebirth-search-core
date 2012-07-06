/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CreateIndexResponse.java 2012-3-29 15:02:31 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.create;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionResponse;


/**
 * The Class CreateIndexResponse.
 *
 * @author l.xue.nong
 */
public class CreateIndexResponse implements ActionResponse, Streamable {

    
    /** The acknowledged. */
    private boolean acknowledged;

    
    /**
     * Instantiates a new creates the index response.
     */
    CreateIndexResponse() {
    }

    
    /**
     * Instantiates a new creates the index response.
     *
     * @param acknowledged the acknowledged
     */
    CreateIndexResponse(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    
    /**
     * Acknowledged.
     *
     * @return true, if successful
     */
    public boolean acknowledged() {
        return acknowledged;
    }

    
    /**
     * Gets the acknowledged.
     *
     * @return the acknowledged
     */
    public boolean getAcknowledged() {
        return acknowledged();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        acknowledged = in.readBoolean();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeBoolean(acknowledged);
    }
}
