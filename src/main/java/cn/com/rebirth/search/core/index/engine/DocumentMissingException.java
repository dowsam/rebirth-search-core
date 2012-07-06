/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DocumentMissingException.java 2012-3-29 15:00:43 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class DocumentMissingException.
 *
 * @author l.xue.nong
 */
public class DocumentMissingException extends EngineException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3984938777972371960L;

	
	/**
	 * Instantiates a new document missing exception.
	 *
	 * @param shardId the shard id
	 * @param type the type
	 * @param id the id
	 */
	public DocumentMissingException(ShardId shardId, String type, String id) {
		super(shardId, "[" + type + "][" + id + "]: document missing");
	}

}
