/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardGatewayException.java 2012-3-29 15:01:15 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.gateway;

import cn.com.rebirth.search.core.index.shard.IndexShardException;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class IndexShardGatewayException.
 *
 * @author l.xue.nong
 */
public class IndexShardGatewayException extends IndexShardException {

    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 138437180629211098L;

	
    /**
     * Instantiates a new index shard gateway exception.
     *
     * @param shardId the shard id
     * @param msg the msg
     */
    public IndexShardGatewayException(ShardId shardId, String msg) {
        super(shardId, msg);
    }

    
    /**
     * Instantiates a new index shard gateway exception.
     *
     * @param shardId the shard id
     * @param msg the msg
     * @param cause the cause
     */
    public IndexShardGatewayException(ShardId shardId, String msg, Throwable cause) {
        super(shardId, msg, cause);
    }
}
