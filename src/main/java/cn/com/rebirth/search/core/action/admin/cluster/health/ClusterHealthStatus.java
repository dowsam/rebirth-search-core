/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterHealthStatus.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.health;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;

/**
 * The Enum ClusterHealthStatus.
 *
 * @author l.xue.nong
 */
public enum ClusterHealthStatus {

	/** The green. */
	GREEN((byte) 0),

	/** The yellow. */
	YELLOW((byte) 1),

	/** The red. */
	RED((byte) 2);

	/** The value. */
	private byte value;

	/**
	 * Instantiates a new cluster health status.
	 *
	 * @param value the value
	 */
	ClusterHealthStatus(byte value) {
		this.value = value;
	}

	/**
	 * Value.
	 *
	 * @return the byte
	 */
	public byte value() {
		return value;
	}

	/**
	 * From value.
	 *
	 * @param value the value
	 * @return the cluster health status
	 */
	public static ClusterHealthStatus fromValue(byte value) {
		switch (value) {
		case 0:
			return GREEN;
		case 1:
			return YELLOW;
		case 2:
			return RED;
		default:
			throw new RebirthIllegalArgumentException("No cluster health status for value [" + value + "]");
		}
	}
}
