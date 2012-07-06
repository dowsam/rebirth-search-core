/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardState.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;

/**
 * The Enum IndexShardState.
 *
 * @author l.xue.nong
 */
public enum IndexShardState {

	/** The created. */
	CREATED((byte) 0),

	/** The recovering. */
	RECOVERING((byte) 1),

	/** The started. */
	STARTED((byte) 2),

	/** The relocated. */
	RELOCATED((byte) 3),

	/** The closed. */
	CLOSED((byte) 4);

	/** The id. */
	private final byte id;

	/**
	 * Instantiates a new index shard state.
	 *
	 * @param id the id
	 */
	IndexShardState(byte id) {
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
	 * @return the index shard state
	 * @throws RebirthIllegalArgumentException the rebirth illegal argument exception
	 */
	public static IndexShardState fromId(byte id) throws RebirthIllegalArgumentException {
		if (id == 0) {
			return CREATED;
		} else if (id == 1) {
			return RECOVERING;
		} else if (id == 2) {
			return STARTED;
		} else if (id == 3) {
			return RELOCATED;
		} else if (id == 4) {
			return CLOSED;
		}
		throw new RebirthIllegalArgumentException("No mapping for id [" + id + "]");
	}
}
