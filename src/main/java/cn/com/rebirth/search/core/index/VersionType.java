/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core VersionType.java 2012-3-29 15:02:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;



/**
 * The Enum VersionType.
 *
 * @author l.xue.nong
 */
public enum VersionType {

	
	/** The INTERNAL. */
	INTERNAL((byte) 0),

	
	/** The EXTERNAL. */
	EXTERNAL((byte) 1);

	
	/** The value. */
	private final byte value;

	
	/**
	 * Instantiates a new version type.
	 *
	 * @param value the value
	 */
	VersionType(byte value) {
		this.value = value;
	}

	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public byte getValue() {
		return value;
	}

	
	/**
	 * From string.
	 *
	 * @param versionType the version type
	 * @return the version type
	 */
	public static VersionType fromString(String versionType) {
		if ("internal".equals(versionType)) {
			return INTERNAL;
		} else if ("external".equals(versionType)) {
			return EXTERNAL;
		}
		throw new RestartIllegalArgumentException("No version type match [" + versionType + "]");
	}

	
	/**
	 * From string.
	 *
	 * @param versionType the version type
	 * @param defaultVersionType the default version type
	 * @return the version type
	 */
	public static VersionType fromString(String versionType, VersionType defaultVersionType) {
		if (versionType == null) {
			return defaultVersionType;
		}
		if ("internal".equals(versionType)) {
			return INTERNAL;
		} else if ("external".equals(versionType)) {
			return EXTERNAL;
		}
		throw new RestartIllegalArgumentException("No version type match [" + versionType + "]");
	}

	
	/**
	 * From value.
	 *
	 * @param value the value
	 * @return the version type
	 */
	public static VersionType fromValue(byte value) {
		if (value == 0) {
			return INTERNAL;
		} else if (value == 1) {
			return EXTERNAL;
		}
		throw new RestartIllegalArgumentException("No version type match [" + value + "]");
	}
}