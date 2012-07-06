/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core VersionConflictEngineException.java 2012-7-6 14:28:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class VersionConflictEngineException.
 *
 * @author l.xue.nong
 */
public class VersionConflictEngineException extends EngineException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1464147746877679169L;

	/** The current. */
	private final long current;

	/** The provided. */
	private final long provided;

	/**
	 * Instantiates a new version conflict engine exception.
	 *
	 * @param shardId the shard id
	 * @param type the type
	 * @param id the id
	 * @param current the current
	 * @param provided the provided
	 */
	public VersionConflictEngineException(ShardId shardId, String type, String id, long current, long provided) {
		super(shardId, "[" + type + "][" + id + "]: version conflict, current [" + current + "], provided [" + provided
				+ "]");
		this.current = current;
		this.provided = provided;
	}

	/**
	 * Gets the current version.
	 *
	 * @return the current version
	 */
	public long getCurrentVersion() {
		return this.current;
	}

	/**
	 * Gets the provided version.
	 *
	 * @return the provided version
	 */
	public long getProvidedVersion() {
		return this.provided;
	}
}
