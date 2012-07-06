/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FailedToResolveConfigException.java 2012-3-29 15:02:35 l.xue.nong$$
 */


package cn.com.rebirth.search.core.env;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class FailedToResolveConfigException.
 *
 * @author l.xue.nong
 */
public class FailedToResolveConfigException extends RestartException {

	
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
