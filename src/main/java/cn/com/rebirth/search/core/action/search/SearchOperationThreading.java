/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchOperationThreading.java 2012-7-6 14:29:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;

/**
 * The Enum SearchOperationThreading.
 *
 * @author l.xue.nong
 */
public enum SearchOperationThreading {

	/** The no threads. */
	NO_THREADS((byte) 0),

	/** The single thread. */
	SINGLE_THREAD((byte) 1),

	/** The thread per shard. */
	THREAD_PER_SHARD((byte) 2);

	/** The id. */
	private final byte id;

	/**
	 * Instantiates a new search operation threading.
	 *
	 * @param id the id
	 */
	SearchOperationThreading(byte id) {
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
	 * @return the search operation threading
	 */
	public static SearchOperationThreading fromId(byte id) {
		if (id == 0) {
			return NO_THREADS;
		}
		if (id == 1) {
			return SINGLE_THREAD;
		}
		if (id == 2) {
			return THREAD_PER_SHARD;
		}
		throw new RebirthIllegalArgumentException("No type matching id [" + id + "]");
	}

	/**
	 * From string.
	 *
	 * @param value the value
	 * @param defaultValue the default value
	 * @return the search operation threading
	 */
	public static SearchOperationThreading fromString(String value, @Nullable SearchOperationThreading defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if ("no_threads".equals(value) || "noThreads".equals(value)) {
			return NO_THREADS;
		} else if ("single_thread".equals(value) || "singleThread".equals(value)) {
			return SINGLE_THREAD;
		} else if ("thread_per_shard".equals(value) || "threadPerShard".equals(value)) {
			return THREAD_PER_SHARD;
		}
		throw new RebirthIllegalArgumentException("No value for search operation threading matching [" + value + "]");
	}
}