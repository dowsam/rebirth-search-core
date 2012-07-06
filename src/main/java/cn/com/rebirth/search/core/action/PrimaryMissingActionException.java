/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PrimaryMissingActionException.java 2012-3-29 15:02:26 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class PrimaryMissingActionException.
 *
 * @author l.xue.nong
 */
public class PrimaryMissingActionException extends RestartException {

	
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
