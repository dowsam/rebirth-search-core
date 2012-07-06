/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MapperCompressionException.java 2012-7-6 14:29:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

/**
 * The Class MapperCompressionException.
 *
 * @author l.xue.nong
 */
public class MapperCompressionException extends MapperException {

	/**
	 * Instantiates a new mapper compression exception.
	 *
	 * @param message the message
	 */
	public MapperCompressionException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new mapper compression exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public MapperCompressionException(String message, Throwable cause) {
		super(message, cause);
	}
}