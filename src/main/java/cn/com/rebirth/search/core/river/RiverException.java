/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiverException.java 2012-7-6 14:29:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class RiverException.
 *
 * @author l.xue.nong
 */
public class RiverException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6020210198377018052L;

	/** The river. */
	private final RiverName river;

	/**
	 * Instantiates a new river exception.
	 *
	 * @param river the river
	 * @param msg the msg
	 */
	public RiverException(RiverName river, String msg) {
		this(river, msg, null);
	}

	/**
	 * Instantiates a new river exception.
	 *
	 * @param river the river
	 * @param msg the msg
	 * @param cause the cause
	 */
	public RiverException(RiverName river, String msg, Throwable cause) {
		this(river, true, msg, cause);
	}

	/**
	 * Instantiates a new river exception.
	 *
	 * @param river the river
	 * @param withSpace the with space
	 * @param msg the msg
	 * @param cause the cause
	 */
	protected RiverException(RiverName river, boolean withSpace, String msg, Throwable cause) {
		super("[" + river.type() + "][" + river.name() + "]" + (withSpace ? " " : "") + msg, cause);
		this.river = river;
	}

	/**
	 * River name.
	 *
	 * @return the river name
	 */
	public RiverName riverName() {
		return river;
	}
}
