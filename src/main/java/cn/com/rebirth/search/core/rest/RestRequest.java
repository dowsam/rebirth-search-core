/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestRequest.java 2012-7-6 14:29:05 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest;

import java.util.Map;

import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.ToXContent.Params;

/**
 * The Interface RestRequest.
 *
 * @author l.xue.nong
 */
public interface RestRequest extends Params {

	/**
	 * The Enum Method.
	 *
	 * @author l.xue.nong
	 */
	enum Method {

		/** The get. */
		GET,

		/** The post. */
		POST,

		/** The put. */
		PUT,

		/** The delete. */
		DELETE,

		/** The options. */
		OPTIONS,

		/** The head. */
		HEAD
	}

	/**
	 * Method.
	 *
	 * @return the method
	 */
	Method method();

	/**
	 * Uri.
	 *
	 * @return the string
	 */
	String uri();

	/**
	 * Raw path.
	 *
	 * @return the string
	 */
	String rawPath();

	/**
	 * Path.
	 *
	 * @return the string
	 */
	String path();

	/**
	 * Checks for content.
	 *
	 * @return true, if successful
	 */
	boolean hasContent();

	/**
	 * Content unsafe.
	 *
	 * @return true, if successful
	 */
	boolean contentUnsafe();

	/**
	 * Content byte array.
	 *
	 * @return the byte[]
	 */
	byte[] contentByteArray();

	/**
	 * Content byte array offset.
	 *
	 * @return the int
	 */
	int contentByteArrayOffset();

	/**
	 * Content length.
	 *
	 * @return the int
	 */
	int contentLength();

	/**
	 * Content as string.
	 *
	 * @return the string
	 */
	String contentAsString();

	/**
	 * Header.
	 *
	 * @param name the name
	 * @return the string
	 */
	String header(String name);

	/**
	 * Checks for param.
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	boolean hasParam(String key);

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent.Params#param(java.lang.String)
	 */
	String param(String key);

	/**
	 * Param as string array.
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the string[]
	 */
	String[] paramAsStringArray(String key, String[] defaultValue);

	/**
	 * Param as float.
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the float
	 */
	float paramAsFloat(String key, float defaultValue);

	/**
	 * Param as int.
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the int
	 */
	int paramAsInt(String key, int defaultValue);

	/**
	 * Param as long.
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the long
	 */
	long paramAsLong(String key, long defaultValue);

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent.Params#paramAsBoolean(java.lang.String, boolean)
	 */
	boolean paramAsBoolean(String key, boolean defaultValue);

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent.Params#paramAsBooleanOptional(java.lang.String, java.lang.Boolean)
	 */
	Boolean paramAsBooleanOptional(String key, Boolean defaultValue);

	/**
	 * Param as time.
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the time value
	 */
	TimeValue paramAsTime(String key, TimeValue defaultValue);

	/**
	 * Param as size.
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the byte size value
	 */
	ByteSizeValue paramAsSize(String key, ByteSizeValue defaultValue);

	/**
	 * Params.
	 *
	 * @return the map
	 */
	Map<String, String> params();
}
