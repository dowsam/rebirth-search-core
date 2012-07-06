/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MasterNotDiscoveredException.java 2012-3-29 15:01:31 l.xue.nong$$
 */


package cn.com.rebirth.search.core.discovery;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class MasterNotDiscoveredException.
 *
 * @author l.xue.nong
 */
public class MasterNotDiscoveredException extends RestartException {

	
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
