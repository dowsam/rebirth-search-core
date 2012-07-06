/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ThreadPoolRejectedException.java 2012-7-6 14:29:26 l.xue.nong$$
 */

package cn.com.rebirth.search.core.threadpool;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class ThreadPoolRejectedException.
 *
 * @author l.xue.nong
 */
public class ThreadPoolRejectedException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6181867762661421499L;

	/**
	 * Instantiates a new thread pool rejected exception.
	 */
	public ThreadPoolRejectedException() {
		super("rejected");
	}

}
