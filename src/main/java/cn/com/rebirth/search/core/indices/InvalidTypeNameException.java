/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InvalidTypeNameException.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.search.core.index.mapper.MapperException;

/**
 * The Class InvalidTypeNameException.
 *
 * @author l.xue.nong
 */
public class InvalidTypeNameException extends MapperException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -66216522957664522L;

	/**
	 * Instantiates a new invalid type name exception.
	 *
	 * @param message the message
	 */
	public InvalidTypeNameException(String message) {
		super(message);
	}

}
