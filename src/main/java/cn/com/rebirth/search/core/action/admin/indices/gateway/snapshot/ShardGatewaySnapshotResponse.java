/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardGatewaySnapshotResponse.java 2012-3-29 15:01:30 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse;


/**
 * The Class ShardGatewaySnapshotResponse.
 *
 * @author l.xue.nong
 */
class ShardGatewaySnapshotResponse extends BroadcastShardOperationResponse {

    
    /**
     * Instantiates a new shard gateway snapshot response.
     */
    ShardGatewaySnapshotResponse() {
    }

    
    /**
     * Instantiates a new shard gateway snapshot response.
     *
     * @param index the index
     * @param shardId the shard id
     */
    public ShardGatewaySnapshotResponse(String index, int shardId) {
        super(index, shardId);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationResponse#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationResponse#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
    }
}