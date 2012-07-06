/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeleteFailedEngineException.java 2012-3-29 15:02:35 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class DeleteFailedEngineException.
 *
 * @author l.xue.nong
 */
public class DeleteFailedEngineException extends EngineException {

    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4062079572835884599L;

	
    /**
     * Instantiates a new delete failed engine exception.
     *
     * @param shardId the shard id
     * @param delete the delete
     * @param cause the cause
     */
    public DeleteFailedEngineException(ShardId shardId, Engine.Delete delete, Throwable cause) {
        super(shardId, "Delete failed for [" + delete.uid().text() + "]", cause);
    }
}