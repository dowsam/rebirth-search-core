/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexShardAlreadyExistsException.java 2012-7-6 14:30:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class IndexShardAlreadyExistsException.
 *
 * @author l.xue.nong
 */
public class IndexShardAlreadyExistsException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4346588180725030794L;

	/**
	 * Instantiates a new index shard already exists exception.
	 *
	 * @param message the message
	 */
	public IndexShardAlreadyExistsException(String message) {
		super(message);
	}
}