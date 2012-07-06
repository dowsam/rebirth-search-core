/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexFailedEngineException.java 2012-7-6 14:29:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class IndexFailedEngineException.
 *
 * @author l.xue.nong
 */
public class IndexFailedEngineException extends EngineException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4911711613579284036L;

	/** The type. */
	private final String type;

	/** The id. */
	private final String id;

	/**
	 * Instantiates a new index failed engine exception.
	 *
	 * @param shardId the shard id
	 * @param index the index
	 * @param cause the cause
	 */
	public IndexFailedEngineException(ShardId shardId, Engine.Index index, Throwable cause) {
		super(shardId, "Index failed for [" + index.type() + "#" + index.id() + "]", cause);
		this.type = index.type();
		this.id = index.id();
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