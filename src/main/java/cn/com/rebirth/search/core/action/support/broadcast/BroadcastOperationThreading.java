/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BroadcastOperationThreading.java 2012-3-29 15:00:47 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support.broadcast;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;



/**
 * The Enum BroadcastOperationThreading.
 *
 * @author l.xue.nong
 */
public enum BroadcastOperationThreading {

	
	/** The N o_ threads. */
	NO_THREADS((byte) 0),

	
	/** The SINGL e_ thread. */
	SINGLE_THREAD((byte) 1),

	
	/** The THREA d_ pe r_ shard. */
	THREAD_PER_SHARD((byte) 2);

	
	/** The id. */
	private final byte id;

	
	/**
	 * Instantiates a new broadcast operation threading.
	 *
	 * @param id the id
	 */
	BroadcastOperationThreading(byte id) {
		this.id = id;
	}

	
	/**
	 * Id.
	 *
	 * @return the byte
	 */
	public byte id() {
		return this.id;
	}

	
	/**
	 * From id.
	 *
	 * @param id the id
	 * @return the broadcast operation threading
	 */
	public static BroadcastOperationThreading fromId(byte id) {
		if (id == 0) {
			return NO_THREADS;
		}
		if (id == 1) {
			return SINGLE_THREAD;
		}
		if (id == 2) {
			return THREAD_PER_SHARD;
		}
		throw new RestartIllegalArgumentException("No type matching id [" + id + "]");
	}

	
	/**
	 * From string.
	 *
	 * @param value the value
	 * @param defaultValue the default value
	 * @return the broadcast operation threading
	 */
	public static BroadcastOperationThreading fromString(String value, BroadcastOperationThreading defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		return BroadcastOperationThreading.valueOf(value.toUpperCase());
	}
}
