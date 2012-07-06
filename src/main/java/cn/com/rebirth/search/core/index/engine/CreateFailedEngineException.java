/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CreateFailedEngineException.java 2012-3-29 15:00:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class CreateFailedEngineException.
 *
 * @author l.xue.nong
 */
public class CreateFailedEngineException extends EngineException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2544428849521582153L;

	
	/** The type. */
	private final String type;

	
	/** The id. */
	private final String id;

	
	/**
	 * Instantiates a new creates the failed engine exception.
	 *
	 * @param shardId the shard id
	 * @param create the create
	 * @param cause the cause
	 */
	public CreateFailedEngineException(ShardId shardId, Engine.Create create, Throwable cause) {
		super(shardId, "Create failed for [" + create.type() + "#" + create.id() + "]", cause);
		this.type = create.type();
		this.id = create.id();
	}

	
	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return this.type;
	}

	
	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		return this.id;
	}
}
