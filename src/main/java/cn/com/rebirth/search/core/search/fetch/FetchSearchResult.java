/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FetchSearchResult.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.search.SearchShardTarget;
import cn.com.rebirth.search.core.search.internal.InternalSearchHits;
import cn.com.rebirth.search.core.search.internal.InternalSearchHits.StreamContext;

/**
 * The Class FetchSearchResult.
 *
 * @author l.xue.nong
 */
public class FetchSearchResult implements Streamable, FetchSearchResultProvider {

	/** The id. */
	private long id;

	/** The shard target. */
	private SearchShardTarget shardTarget;

	/** The hits. */
	private InternalSearchHits hits;

	/** The counter. */
	private transient int counter;

	/**
	 * Instantiates a new fetch search result.
	 */
	public FetchSearchResult() {

	}

	/**
	 * Instantiates a new fetch search result.
	 *
	 * @param id the id
	 * @param shardTarget the shard target
	 */
	public FetchSearchResult(long id, SearchShardTarget shardTarget) {
		this.id = id;
		this.shardTarget = shardTarget;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSearchResultProvider#fetchResult()
	 */
	@Override
	public FetchSearchResult fetchResult() {
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#id()
	 */
	public long id() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#shardTarget()
	 */
	public SearchShardTarget shardTarget() {
		return this.shardTarget;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#shardTarget(cn.com.rebirth.search.core.search.SearchShardTarget)
	 */
	@Override
	public void shardTarget(SearchShardTarget shardTarget) {
		this.shardTarget = shardTarget;
	}

	/**
	 * Hits.
	 *
	 * @param hits the hits
	 */
	public void hits(InternalSearchHits hits) {
		this.hits = hits;
	}

	/**
	 * Hits.
	 *
	 * @return the internal search hits
	 */
	public InternalSearchHits hits() {
		return hits;
	}

	/**
	 * Inits the counter.
	 *
	 * @return the fetch search result
	 */
	public FetchSearchResult initCounter() {
		counter = 0;
		return this;
	}

	/**
	 * Counter get and increment.
	 *
	 * @return the int
	 */
	public int counterGetAndIncrement() {
		return counter++;
	}

	/**
	 * Read fetch search result.
	 *
	 * @param in the in
	 * @return the fetch search result
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static FetchSearchResult readFetchSearchResult(StreamInput in) throws IOException {
		FetchSearchResult result = new FetchSearchResult();
		result.readFrom(in);
		return result;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		id = in.readLong();
		hits = InternalSearchHits.readSearchHits(in,
				InternalSearchHits.streamContext().streamShardTarget(StreamContext.ShardTargetType.NO_STREAM));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeLong(id);
		hits.writeTo(out, InternalSearchHits.streamContext().streamShardTarget(StreamContext.ShardTargetType.NO_STREAM));
	}
}
