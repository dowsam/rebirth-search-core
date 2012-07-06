/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CountSearchResult.java 2012-7-6 14:28:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.count;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.search.SearchPhaseResult;
import cn.com.rebirth.search.core.search.SearchShardTarget;

/**
 * The Class CountSearchResult.
 *
 * @author l.xue.nong
 */
public class CountSearchResult implements Streamable, SearchPhaseResult {

	/** The id. */
	private long id;

	/** The total hits. */
	private long totalHits;

	/** The shard target. */
	private SearchShardTarget shardTarget;

	/**
	 * Instantiates a new count search result.
	 */
	public CountSearchResult() {
	}

	/**
	 * Instantiates a new count search result.
	 *
	 * @param id the id
	 * @param totalHits the total hits
	 */
	public CountSearchResult(long id, long totalHits) {
		this.id = id;
		this.totalHits = totalHits;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#id()
	 */
	public long id() {
		return this.id;
	}

	/**
	 * Total hits.
	 *
	 * @return the long
	 */
	public long totalHits() {
		return this.totalHits;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#shardTarget()
	 */
	@Override
	public SearchShardTarget shardTarget() {
		return shardTarget;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#shardTarget(cn.com.rebirth.search.core.search.SearchShardTarget)
	 */
	@Override
	public void shardTarget(SearchShardTarget shardTarget) {
		this.shardTarget = shardTarget;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		id = in.readLong();
		totalHits = in.readVLong();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeLong(id);
		out.writeVLong(totalHits);
	}
}