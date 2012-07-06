/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DumpException.java 2012-3-29 15:02:28 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.dump;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class DumpException.
 *
 * @author l.xue.nong
 */
public class DumpException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8702115987348577959L;

	
	/**
	 * Instantiates a new dump exception.
	 *
	 * @param msg the msg
	 */
	public DumpException(String msg) {
		super(msg);
	}

	
	/**
	 * Instantiates a new dump exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public DumpException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
