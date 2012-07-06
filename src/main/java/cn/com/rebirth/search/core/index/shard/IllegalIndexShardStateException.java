/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IllegalIndexShardStateException.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard;

/**
 * The Class IllegalIndexShardStateException.
 *
 * @author l.xue.nong
 */
public class IllegalIndexShardStateException extends IndexShardException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5766390244739887309L;

	/** The current state. */
	private final IndexShardState currentState;

	/**
	 * Instantiates a new illegal index shard state exception.
	 *
	 * @param shardId the shard id
	 * @param currentState the current state
	 * @param msg the msg
	 */
	public IllegalIndexShardStateException(ShardId shardId, IndexShardState currentState, String msg) {
		super(shardId, "CurrentState[" + currentState + "] " + msg);
		this.currentState = currentState;
	}

	/**
	 * Instantiates a new illegal index shard state exception.
	 *
	 * @param shardId the shard id
	 * @param currentState the current state
	 * @param msg the msg
	 * @param ex the ex
	 */
	public IllegalIndexShardStateException(ShardId shardId, IndexShardState currentState, String msg, Throwable ex) {
		super(shardId, "CurrentState[" + currentState + "] ", ex);
		this.currentState = currentState;
	}

	/**
	 * Current state.
	 *
	 * @return the index shard state
	 */
	public IndexShardState currentState() {
		return currentState;
	}
}
