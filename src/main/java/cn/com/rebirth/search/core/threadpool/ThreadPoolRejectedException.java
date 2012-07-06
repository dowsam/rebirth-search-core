/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ThreadPoolRejectedException.java 2012-3-29 15:02:02 l.xue.nong$$
 */


package cn.com.rebirth.search.core.threadpool;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class ThreadPoolRejectedException.
 *
 * @author l.xue.nong
 */
public class ThreadPoolRejectedException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6181867762661421499L;

	
	/**
	 * Instantiates a new thread pool rejected exception.
	 */
	public ThreadPoolRejectedException() {
		super("rejected");
	}

}
