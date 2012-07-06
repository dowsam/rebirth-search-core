/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FailedToResolveConfigException.java 2012-7-6 14:30:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.env;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class FailedToResolveConfigException.
 *
 * @author l.xue.nong
 */
public class FailedToResolveConfigException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4981915417511956899L;

	/**
	 * Instantiates a new failed to resolve config exception.
	 *
	 * @param msg the msg
	 */
	public FailedToResolveConfigException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new failed to resolve config exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public FailedToResolveConfigException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
