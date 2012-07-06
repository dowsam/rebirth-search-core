/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MasterNotDiscoveredException.java 2012-7-6 14:28:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class MasterNotDiscoveredException.
 *
 * @author l.xue.nong
 */
public class MasterNotDiscoveredException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7040710728643573676L;

	/**
	 * Instantiates a new master not discovered exception.
	 */
	public MasterNotDiscoveredException() {
		super("");
	}

	/**
	 * Instantiates a new master not discovered exception.
	 *
	 * @param message the message
	 */
	public MasterNotDiscoveredException(String message) {
		super(message);
	}

}
