/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core VersionType.java 2012-7-6 14:29:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;

/**
 * The Enum VersionType.
 *
 * @author l.xue.nong
 */
public enum VersionType {

	/** The internal. */
	INTERNAL((byte) 0),

	/** The external. */
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
		throw new RebirthIllegalArgumentException("No version type match [" + versionType + "]");
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
		throw new RebirthIllegalArgumentException("No version type match [" + versionType + "]");
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
		throw new RebirthIllegalArgumentException("No version type match [" + value + "]");
	}
}