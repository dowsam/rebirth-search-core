/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScrollQuerySearchResult.java 2012-7-6 14:29:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.query;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.search.SearchShardTarget;

/**
 * The Class ScrollQuerySearchResult.
 *
 * @author l.xue.nong
 */
public class ScrollQuerySearchResult implements Streamable {

	/** The query result. */
	private QuerySearchResult queryResult;

	/** The shard target. */
	private SearchShardTarget shardTarget;

	/**
	 * Instantiates a new scroll query search result.
	 */
	public ScrollQuerySearchResult() {
	}

	/**
	 * Instantiates a new scroll query search result.
	 *
	 * @param queryResult the query result
	 * @param shardTarget the shard target
	 */
	public ScrollQuerySearchResult(QuerySearchResult queryResult, SearchShardTarget shardTarget) {
		this.queryResult = queryResult;
		this.shardTarget = shardTarget;
	}

	/**
	 * Query result.
	 *
	 * @return the query search result
	 */
	public QuerySearchResult queryResult() {
		return queryResult;
	}

	/**
	 * Shard target.
	 *
	 * @return the search shard target
	 */
	public SearchShardTarget shardTarget() {
		return shardTarget;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		shardTarget = SearchShardTarget.readSearchShardTarget(in);
		queryResult = QuerySearchResult.readQuerySearchResult(in);
		queryResult.shardTarget(shardTarget);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		shardTarget.writeTo(out);
		queryResult.writeTo(out);
	}
}
