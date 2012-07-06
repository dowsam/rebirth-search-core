/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DocumentSourceMissingException.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.search.core.index.shard.ShardId;

/**
 * The Class DocumentSourceMissingException.
 *
 * @author l.xue.nong
 */
public class DocumentSourceMissingException extends EngineException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6608485274763877424L;

	/**
	 * Instantiates a new document source missing exception.
	 *
	 * @param shardId the shard id
	 * @param type the type
	 * @param id the id
	 */
	public DocumentSourceMissingException(ShardId shardId, String type, String id) {
		super(shardId, "[" + type + "][" + id + "]: document source missing");
	}

}
