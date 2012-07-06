/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NoNodeAvailableException.java 2012-3-29 15:02:29 l.xue.nong$$
 */


package cn.com.rebirth.search.core.client.transport;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class NoNodeAvailableException.
 *
 * @author l.xue.nong
 */
public class NoNodeAvailableException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8511557280492157435L;

	
	/**
	 * Instantiates a new no node available exception.
	 */
	public NoNodeAvailableException() {
		super("No node available");
	}

}
