/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MapperException.java 2012-3-29 15:02:40 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class MapperException.
 *
 * @author l.xue.nong
 */
public class MapperException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 195413174645935023L;

	
	/**
	 * Instantiates a new mapper exception.
	 *
	 * @param message the message
	 */
	public MapperException(String message) {
		super(message);
	}

	
	/**
	 * Instantiates a new mapper exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public MapperException(String message, Throwable cause) {
		super(message, cause);
	}
}