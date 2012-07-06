/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DocumentAlreadyExistsException.java 2012-3-29 15:02:19 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class DocumentAlreadyExistsException.
 *
 * @author l.xue.nong
 */
public class DocumentAlreadyExistsException extends EngineException {

    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4084650143886524731L;

	
    /**
     * Instantiates a new document already exists exception.
     *
     * @param shardId the shard id
     * @param type the type
     * @param id the id
     */
    public DocumentAlreadyExistsException(ShardId shardId, String type, String id) {
        super(shardId, "[" + type + "][" + id + "]: document already exists");
    }
}
