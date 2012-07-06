/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardOperationFailedException.java 2012-3-29 15:01:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;

import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.Streamable;


/**
 * The Interface ShardOperationFailedException.
 *
 * @author l.xue.nong
 */
public interface ShardOperationFailedException extends Streamable, Serializable {

	/**
	 * Index.
	 *
	 * @return the string
	 */
	String index();

	
	/**
	 * Shard id.
	 *
	 * @return the int
	 */
	int shardId();

	
	/**
	 * Reason.
	 *
	 * @return the string
	 */
	String reason();
}
