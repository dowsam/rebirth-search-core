/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardRoutingState.java 2012-7-6 14:30:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

import cn.com.rebirth.commons.exception.RebirthIllegalStateException;

/**
 * The Enum ShardRoutingState.
 *
 * @author l.xue.nong
 */
public enum ShardRoutingState {

	/** The unassigned. */
	UNASSIGNED((byte) 1),

	/** The initializing. */
	INITIALIZING((byte) 2),

	/** The started. */
	STARTED((byte) 3),

	/** The relocating. */
	RELOCATING((byte) 4);

	/** The value. */
	private byte value;

	/**
	 * Instantiates a new shard routing state.
	 *
	 * @param value the value
	 */
	ShardRoutingState(byte value) {
		this.value = value;
	}

	/**
	 * Value.
	 *
	 * @return the byte
	 */
	public byte value() {
		return this.value;
	}

	/**
	 * From value.
	 *
	 * @param value the value
	 * @return the shard routing state
	 */
	public static ShardRoutingState fromValue(byte value) {
		switch (value) {
		case 1:
			return UNASSIGNED;
		case 2:
			return INITIALIZING;
		case 3:
			return STARTED;
		case 4:
			return RELOCATING;
		default:
			throw new RebirthIllegalStateException("No should routing state mapped for [" + value + "]");
		}
	}
}
