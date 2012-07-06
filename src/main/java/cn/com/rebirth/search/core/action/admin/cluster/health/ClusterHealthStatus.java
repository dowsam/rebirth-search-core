/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterHealthStatus.java 2012-3-29 15:01:23 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.health;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;



/**
 * The Enum ClusterHealthStatus.
 *
 * @author l.xue.nong
 */
public enum ClusterHealthStatus {

	
	/** The GREEN. */
	GREEN((byte) 0),

	
	/** The YELLOW. */
	YELLOW((byte) 1),

	
	/** The RED. */
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
			throw new RestartIllegalArgumentException("No cluster health status for value [" + value + "]");
		}
	}
}
