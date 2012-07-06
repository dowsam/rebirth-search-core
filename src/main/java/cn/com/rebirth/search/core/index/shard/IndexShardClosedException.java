/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardClosedException.java 2012-3-29 15:01:18 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.shard;


/**
 * The Class IndexShardClosedException.
 *
 * @author l.xue.nong
 */
public class IndexShardClosedException extends IllegalIndexShardStateException {
    
    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4331320626608080591L;

	
	/**
	 * Instantiates a new index shard closed exception.
	 *
	 * @param shardId the shard id
	 */
	public IndexShardClosedException(ShardId shardId) {
        super(shardId, IndexShardState.CLOSED, "Closed");
    }

    
    /**
     * Instantiates a new index shard closed exception.
     *
     * @param shardId the shard id
     * @param t the t
     */
    public IndexShardClosedException(ShardId shardId, Throwable t) {
        super(shardId, IndexShardState.CLOSED, "Closed", t);
    }
}
