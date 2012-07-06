/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexDeleteByQueryResponse.java 2012-3-29 15:01:13 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.deletebyquery;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionResponse;


/**
 * The Class IndexDeleteByQueryResponse.
 *
 * @author l.xue.nong
 */
public class IndexDeleteByQueryResponse implements ActionResponse, Streamable {

    
    /** The index. */
    private String index;

    
    /** The successful shards. */
    private int successfulShards;

    
    /** The failed shards. */
    private int failedShards;

    
    /**
     * Instantiates a new index delete by query response.
     *
     * @param index the index
     * @param successfulShards the successful shards
     * @param failedShards the failed shards
     */
    IndexDeleteByQueryResponse(String index, int successfulShards, int failedShards) {
        this.index = index;
        this.successfulShards = successfulShards;
        this.failedShards = failedShards;
    }

    
    /**
     * Instantiates a new index delete by query response.
     */
    IndexDeleteByQueryResponse() {

    }

    
    /**
     * Index.
     *
     * @return the string
     */
    public String index() {
        return this.index;
    }

    
    /**
     * Gets the index.
     *
     * @return the index
     */
    public String getIndex() {
        return index;
    }

    
    /**
     * Total shards.
     *
     * @return the int
     */
    public int totalShards() {
        return failedShards + successfulShards;
    }

    
    /**
     * Gets the total shards.
     *
     * @return the total shards
     */
    public int getTotalShards() {
        return totalShards();
    }

    
    /**
     * Successful shards.
     *
     * @return the int
     */
    public int successfulShards() {
        return successfulShards;
    }

    
    /**
     * Gets the successful shards.
     *
     * @return the successful shards
     */
    public int getSuccessfulShards() {
        return successfulShards;
    }

    
    /**
     * Failed shards.
     *
     * @return the int
     */
    public int failedShards() {
        return failedShards;
    }

    
    /**
     * Gets the failed shards.
     *
     * @return the failed shards
     */
    public int getFailedShards() {
        return failedShards;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        index = in.readUTF();
        successfulShards = in.readVInt();
        failedShards = in.readVInt();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeUTF(index);
        out.writeVInt(successfulShards);
        out.writeVInt(failedShards);
    }
}