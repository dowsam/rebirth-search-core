/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RecoveryEngineException.java 2012-7-6 14:30:38 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class RecoveryEngineException.
 *
 * @author l.xue.nong
 */
public class RecoveryEngineException extends EngineException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7835806718432256224L;

	/** The phase. */
	private final int phase;

	/**
	 * Instantiates a new recovery engine exception.
	 *
	 * @param shardId the shard id
	 * @param phase the phase
	 * @param msg the msg
	 * @param cause the cause
	 */
	public RecoveryEngineException(ShardId shardId, int phase, String msg, Throwable cause) {
		super(shardId, "Phase[" + phase + "] " + msg, cause);
		this.phase = phase;
	}

	/**
	 * Phase.
	 *
	 * @return the int
	 */
	public int phase() {
		return phase;
	}
}