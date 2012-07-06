/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchPhaseExecutionException.java 2012-7-6 14:29:16 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class SearchPhaseExecutionException.
 *
 * @author l.xue.nong
 */
public class SearchPhaseExecutionException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5076395213258478118L;

	/** The phase name. */
	private final String phaseName;

	/** The shard failures. */
	private ShardSearchFailure[] shardFailures;

	/**
	 * Instantiates a new search phase execution exception.
	 *
	 * @param phaseName the phase name
	 * @param msg the msg
	 * @param shardFailures the shard failures
	 */
	public SearchPhaseExecutionException(String phaseName, String msg, ShardSearchFailure[] shardFailures) {
		super(buildMessage(phaseName, msg, shardFailures));
		this.phaseName = phaseName;
		this.shardFailures = shardFailures;
	}

	/**
	 * Instantiates a new search phase execution exception.
	 *
	 * @param phaseName the phase name
	 * @param msg the msg
	 * @param cause the cause
	 * @param shardFailures the shard failures
	 */
	public SearchPhaseExecutionException(String phaseName, String msg, Throwable cause,
			ShardSearchFailure[] shardFailures) {
		super(buildMessage(phaseName, msg, shardFailures), cause);
		this.phaseName = phaseName;
		this.shardFailures = shardFailures;
	}

	/**
	 * Phase name.
	 *
	 * @return the string
	 */
	public String phaseName() {
		return phaseName;
	}

	/**
	 * Shard failures.
	 *
	 * @return the shard search failure[]
	 */
	public ShardSearchFailure[] shardFailures() {
		return shardFailures;
	}

	/**
	 * Builds the message.
	 *
	 * @param phaseName the phase name
	 * @param msg the msg
	 * @param shardFailures the shard failures
	 * @return the string
	 */
	private static String buildMessage(String phaseName, String msg, ShardSearchFailure[] shardFailures) {
		StringBuilder sb = new StringBuilder();
		sb.append("Failed to execute phase [").append(phaseName).append("], ").append(msg);
		if (shardFailures != null && shardFailures.length > 0) {
			sb.append("; shardFailures ");
			for (ShardSearchFailure shardFailure : shardFailures) {
				if (shardFailure.shard() != null) {
					sb.append("{").append(shardFailure.shard()).append(": ").append(shardFailure.reason()).append("}");
				} else {
					sb.append("{").append(shardFailure.reason()).append("}");
				}
			}
		}
		return sb.toString();
	}
}
