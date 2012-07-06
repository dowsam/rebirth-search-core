/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterBlockLevel.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.block;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;

/**
 * The Enum ClusterBlockLevel.
 *
 * @author l.xue.nong
 */
public enum ClusterBlockLevel {

	/** The read. */
	READ(0),

	/** The write. */
	WRITE(1),

	/** The metadata. */
	METADATA(2);

	/** The Constant ALL. */
	public static final ClusterBlockLevel[] ALL = new ClusterBlockLevel[] { READ, WRITE, METADATA };

	/** The Constant READ_WRITE. */
	public static final ClusterBlockLevel[] READ_WRITE = new ClusterBlockLevel[] { READ, WRITE };

	/** The id. */
	private final int id;

	/**
	 * Instantiates a new cluster block level.
	 *
	 * @param id the id
	 */
	ClusterBlockLevel(int id) {
		this.id = id;
	}

	/**
	 * Id.
	 *
	 * @return the int
	 */
	public int id() {
		return this.id;
	}

	/**
	 * From id.
	 *
	 * @param id the id
	 * @return the cluster block level
	 */
	public static ClusterBlockLevel fromId(int id) {
		if (id == 0) {
			return READ;
		} else if (id == 1) {
			return WRITE;
		} else if (id == 2) {
			return METADATA;
		}
		throw new RebirthIllegalArgumentException("No cluster block level matching [" + id + "]");
	}
}
