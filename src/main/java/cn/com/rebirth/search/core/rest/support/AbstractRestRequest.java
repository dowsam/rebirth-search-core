/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractRestRequest.java 2012-7-6 14:30:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.support;

import cn.com.rebirth.commons.Booleans;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.rest.RestRequest;

/**
 * The Class AbstractRestRequest.
 *
 * @author l.xue.nong
 */
public abstract class AbstractRestRequest implements RestRequest {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#path()
	 */
	@Override
	public final String path() {
		return RestUtils.decodeComponent(rawPath());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#paramAsFloat(java.lang.String, float)
	 */
	@Override
	public float paramAsFloat(String key, float defaultValue) {
		String sValue = param(key);
		if (sValue == null) {
			return defaultValue;
		}
		try {
			return Float.parseFloat(sValue);
		} catch (NumberFormatException e) {
			throw new RebirthIllegalArgumentException("Failed to parse float parameter [" + key + "] with value ["
					+ sValue + "]", e);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#paramAsInt(java.lang.String, int)
	 */
	@Override
	public int paramAsInt(String key, int defaultValue) {
		String sValue = param(key);
		if (sValue == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(sValue);
		} catch (NumberFormatException e) {
			throw new RebirthIllegalArgumentException("Failed to parse int parameter [" + key + "] with value ["
					+ sValue + "]", e);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#paramAsLong(java.lang.String, long)
	 */
	@Override
	public long paramAsLong(String key, long defaultValue) {
		String sValue = param(key);
		if (sValue == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(sValue);
		} catch (NumberFormatException e) {
			throw new RebirthIllegalArgumentException("Failed to parse int parameter [" + key + "] with value ["
					+ sValue + "]", e);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#paramAsBoolean(java.lang.String, boolean)
	 */
	@Override
	public boolean paramAsBoolean(String key, boolean defaultValue) {
		return Booleans.parseBoolean(param(key), defaultValue);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#paramAsBooleanOptional(java.lang.String, java.lang.Boolean)
	 */
	@Override
	public Boolean paramAsBooleanOptional(String key, Boolean defaultValue) {
		String sValue = param(key);
		if (sValue == null) {
			return defaultValue;
		}
		return !(sValue.equals("false") || sValue.equals("0") || sValue.equals("off"));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#paramAsTime(java.lang.String, cn.com.rebirth.commons.unit.TimeValue)
	 */
	@Override
	public TimeValue paramAsTime(String key, TimeValue defaultValue) {
		return TimeValue.parseTimeValue(param(key), defaultValue);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#paramAsSize(java.lang.String, cn.com.rebirth.commons.unit.ByteSizeValue)
	 */
	@Override
	public ByteSizeValue paramAsSize(String key, ByteSizeValue defaultValue) {
		return ByteSizeValue.parseBytesSizeValue(param(key), defaultValue);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#paramAsStringArray(java.lang.String, java.lang.String[])
	 */
	@Override
	public String[] paramAsStringArray(String key, String[] defaultValue) {
		String value = param(key);
		if (value == null) {
			return defaultValue;
		}
		return Strings.splitStringByCommaToArray(value);
	}
}
