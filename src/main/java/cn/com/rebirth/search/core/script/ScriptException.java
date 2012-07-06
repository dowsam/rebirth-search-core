/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScriptException.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.script;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class ScriptException.
 *
 * @author l.xue.nong
 */
public class ScriptException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6982865530748411149L;

	/**
	 * Instantiates a new script exception.
	 *
	 * @param msg the msg
	 */
	public ScriptException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new script exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public ScriptException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
