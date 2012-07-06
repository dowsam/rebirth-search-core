/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardMissingException.java 2012-3-29 15:02:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index;

import cn.com.rebirth.search.core.index.shard.IndexShardException;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class IndexShardMissingException.
 *
 * @author l.xue.nong
 */
public class IndexShardMissingException extends IndexShardException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4857905889685355366L;

	
	/**
	 * Instantiates a new index shard missing exception.
	 *
	 * @param shardId the shard id
	 */
	public IndexShardMissingException(ShardId shardId) {
		super(shardId, "missing");
	}
}