/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterBlockException.java 2012-7-6 14:29:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.block;

import cn.com.rebirth.commons.exception.RebirthException;

import com.google.common.collect.ImmutableSet;

/**
 * The Class ClusterBlockException.
 *
 * @author l.xue.nong
 */
public class ClusterBlockException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8835825495004751219L;

	/** The blocks. */
	private final ImmutableSet<ClusterBlock> blocks;

	/**
	 * Instantiates a new cluster block exception.
	 *
	 * @param blocks the blocks
	 */
	public ClusterBlockException(ImmutableSet<ClusterBlock> blocks) {
		super(buildMessage(blocks));
		this.blocks = blocks;
	}

	/**
	 * Retryable.
	 *
	 * @return true, if successful
	 */
	public boolean retryable() {
		for (ClusterBlock block : blocks) {
			if (!block.retryable()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Blocks.
	 *
	 * @return the immutable set
	 */
	public ImmutableSet<ClusterBlock> blocks() {
		return blocks;
	}

	/**
	 * Builds the message.
	 *
	 * @param blocks the blocks
	 * @return the string
	 */
	private static String buildMessage(ImmutableSet<ClusterBlock> blocks) {
		StringBuilder sb = new StringBuilder("blocked by: ");
		for (ClusterBlock block : blocks) {
			sb.append("[").append(block.status()).append("/").append(block.id()).append("/")
					.append(block.description()).append("];");
		}
		return sb.toString();
	}

}
