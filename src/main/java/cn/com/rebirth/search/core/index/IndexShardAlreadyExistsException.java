/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardAlreadyExistsException.java 2012-3-29 15:02:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class IndexShardAlreadyExistsException.
 *
 * @author l.xue.nong
 */
public class IndexShardAlreadyExistsException extends RestartException {

	
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