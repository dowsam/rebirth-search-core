/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardGatewaySnapshotNotAllowedException.java 2012-3-29 15:02:33 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.gateway;

import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class IndexShardGatewaySnapshotNotAllowedException.
 *
 * @author l.xue.nong
 */
public class IndexShardGatewaySnapshotNotAllowedException extends IndexShardGatewayException {

    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2092057498756703117L;

	
    /**
     * Instantiates a new index shard gateway snapshot not allowed exception.
     *
     * @param shardId the shard id
     * @param msg the msg
     */
    public IndexShardGatewaySnapshotNotAllowedException(ShardId shardId, String msg) {
        super(shardId, msg);
    }
}
