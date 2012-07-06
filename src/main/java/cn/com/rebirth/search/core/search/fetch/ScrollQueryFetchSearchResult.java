/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScrollQueryFetchSearchResult.java 2012-7-6 14:28:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.search.SearchShardTarget;

/**
 * The Class ScrollQueryFetchSearchResult.
 *
 * @author l.xue.nong
 */
public class ScrollQueryFetchSearchResult implements Streamable {

	/** The result. */
	private QueryFetchSearchResult result;

	/** The shard target. */
	private SearchShardTarget shardTarget;

	/**
	 * Instantiates a new scroll query fetch search result.
	 */
	public ScrollQueryFetchSearchResult() {
	}

	/**
	 * Instantiates a new scroll query fetch search result.
	 *
	 * @param result the result
	 * @param shardTarget the shard target
	 */
	public ScrollQueryFetchSearchResult(QueryFetchSearchResult result, SearchShardTarget shardTarget) {
		this.result = result;
		this.shardTarget = shardTarget;
	}

	/**
	 * Result.
	 *
	 * @return the query fetch search result
	 */
	public QueryFetchSearchResult result() {
		return result;
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
		result = QueryFetchSearchResult.readQueryFetchSearchResult(in);
		result.shardTarget(shardTarget);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		shardTarget.writeTo(out);
		result.writeTo(out);
	}
}
