/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IgnoreRecoveryException.java 2012-7-6 14:29:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.recovery;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class IgnoreRecoveryException.
 *
 * @author l.xue.nong
 */
public class IgnoreRecoveryException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4266953613533217698L;

	/**
	 * Instantiates a new ignore recovery exception.
	 *
	 * @param msg the msg
	 */
	public IgnoreRecoveryException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new ignore recovery exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public IgnoreRecoveryException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
