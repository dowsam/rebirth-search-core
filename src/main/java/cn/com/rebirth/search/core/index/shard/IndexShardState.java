/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardState.java 2012-3-29 15:02:07 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.shard;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;



/**
 * The Enum IndexShardState.
 *
 * @author l.xue.nong
 */
public enum IndexShardState {

	
	/** The CREATED. */
	CREATED((byte) 0),

	
	/** The RECOVERING. */
	RECOVERING((byte) 1),

	
	/** The STARTED. */
	STARTED((byte) 2),

	
	/** The RELOCATED. */
	RELOCATED((byte) 3),

	
	/** The CLOSED. */
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
	 * @throws SumMallSearchIllegalArgumentException the sum mall search illegal argument exception
	 */
	public static IndexShardState fromId(byte id) throws RestartIllegalArgumentException {
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
		throw new RestartIllegalArgumentException("No mapping for id [" + id + "]");
	}
}
