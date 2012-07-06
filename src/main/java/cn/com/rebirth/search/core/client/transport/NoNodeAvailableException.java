/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NoNodeAvailableException.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client.transport;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class NoNodeAvailableException.
 *
 * @author l.xue.nong
 */
public class NoNodeAvailableException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8511557280492157435L;

	/**
	 * Instantiates a new no node available exception.
	 */
	public NoNodeAvailableException() {
		super("No node available");
	}

}
