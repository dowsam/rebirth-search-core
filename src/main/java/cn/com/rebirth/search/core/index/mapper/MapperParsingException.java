/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MapperParsingException.java 2012-7-6 14:29:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

/**
 * The Class MapperParsingException.
 *
 * @author l.xue.nong
 */
public class MapperParsingException extends MapperException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2756488992372208205L;

	/**
	 * Instantiates a new mapper parsing exception.
	 *
	 * @param message the message
	 */
	public MapperParsingException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new mapper parsing exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public MapperParsingException(String message, Throwable cause) {
		super(message, cause);
	}

}