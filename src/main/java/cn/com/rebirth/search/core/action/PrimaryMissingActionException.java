/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PrimaryMissingActionException.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class PrimaryMissingActionException.
 *
 * @author l.xue.nong
 */
public class PrimaryMissingActionException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1969543396090693324L;

	/**
	 * Instantiates a new primary missing action exception.
	 *
	 * @param message the message
	 */
	public PrimaryMissingActionException(String message) {
		super(message);
	}
}
