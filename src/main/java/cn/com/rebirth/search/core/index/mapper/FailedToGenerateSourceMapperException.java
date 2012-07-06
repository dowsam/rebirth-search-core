/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FailedToGenerateSourceMapperException.java 2012-7-6 14:30:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

/**
 * The Class FailedToGenerateSourceMapperException.
 *
 * @author l.xue.nong
 */
public class FailedToGenerateSourceMapperException extends MapperException {

	/**
	 * Instantiates a new failed to generate source mapper exception.
	 *
	 * @param message the message
	 */
	public FailedToGenerateSourceMapperException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new failed to generate source mapper exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public FailedToGenerateSourceMapperException(String message, Throwable cause) {
		super(message, cause);
	}
}
