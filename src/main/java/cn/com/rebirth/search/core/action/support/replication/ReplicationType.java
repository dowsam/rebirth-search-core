/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ReplicationType.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.replication;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;

/**
 * The Enum ReplicationType.
 *
 * @author l.xue.nong
 */
public enum ReplicationType {

	/** The sync. */
	SYNC((byte) 0),

	/** The async. */
	ASYNC((byte) 1),

	/** The default. */
	DEFAULT((byte) 2);

	/** The id. */
	private byte id;

	/**
	 * Instantiates a new replication type.
	 *
	 * @param id the id
	 */
	ReplicationType(byte id) {
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
	 * @param id the id
	 * @return the replication type
	 */
	public static ReplicationType fromId(byte id) {
		if (id == 0) {
			return ASYNC;
		} else if (id == 1) {
			return SYNC;
		} else if (id == 2) {
			return DEFAULT;
		} else {
			throw new RebirthIllegalArgumentException("No type match for [" + id + "]");
		}
	}

	/**
	 * From string.
	 *
	 * @param type the type
	 * @return the replication type
	 */
	public static ReplicationType fromString(String type) {
		if ("async".equals(type)) {
			return ASYNC;
		} else if ("sync".equals(type)) {
			return SYNC;
		} else if ("default".equals(type)) {
			return DEFAULT;
		}
		throw new RebirthIllegalArgumentException("No replication type match for [" + type
				+ "], should be either `async`, or `sync`");
	}
}
