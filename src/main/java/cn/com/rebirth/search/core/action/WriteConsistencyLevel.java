/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core WriteConsistencyLevel.java 2012-7-6 14:29:13 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;

/**
 * The Enum WriteConsistencyLevel.
 *
 * @author l.xue.nong
 */
public enum WriteConsistencyLevel {

	/** The default. */
	DEFAULT((byte) 0),

	/** The one. */
	ONE((byte) 1),

	/** The quorum. */
	QUORUM((byte) 2),

	/** The all. */
	ALL((byte) 3);

	/** The id. */
	private final byte id;

	/**
	 * Instantiates a new write consistency level.
	 *
	 * @param id the id
	 */
	WriteConsistencyLevel(byte id) {
		this.id = id;
	}

	/**
	 * Id.
	 *
	 * @return the byte
	 */
	public byte id() {
		return id;
	}

	/**
	 * From id.
	 *
	 * @param value the value
	 * @return the write consistency level
	 */
	public static WriteConsistencyLevel fromId(byte value) {
		if (value == 0) {
			return DEFAULT;
		} else if (value == 1) {
			return ONE;
		} else if (value == 2) {
			return QUORUM;
		} else if (value == 3) {
			return ALL;
		}
		throw new RebirthIllegalArgumentException("No write consistency match [" + value + "]");
	}

	/**
	 * From string.
	 *
	 * @param value the value
	 * @return the write consistency level
	 */
	public static WriteConsistencyLevel fromString(String value) {
		if (value.equals("default")) {
			return DEFAULT;
		} else if (value.equals("one")) {
			return ONE;
		} else if (value.equals("quorum")) {
			return QUORUM;
		} else if (value.equals("all")) {
			return ALL;
		}
		throw new RebirthIllegalArgumentException("No write consistency match [" + value + "]");
	}
}
