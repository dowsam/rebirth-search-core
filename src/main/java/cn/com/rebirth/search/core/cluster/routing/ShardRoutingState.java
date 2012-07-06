/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardRoutingState.java 2012-3-29 15:00:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing;

import cn.com.rebirth.commons.exception.RestartIllegalStateException;



/**
 * The Enum ShardRoutingState.
 *
 * @author l.xue.nong
 */
public enum ShardRoutingState {

	
	/** The UNASSIGNED. */
	UNASSIGNED((byte) 1),

	
	/** The INITIALIZING. */
	INITIALIZING((byte) 2),

	
	/** The STARTED. */
	STARTED((byte) 3),

	
	/** The RELOCATING. */
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
			throw new RestartIllegalStateException("No should routing state mapped for [" + value + "]");
		}
	}
}
