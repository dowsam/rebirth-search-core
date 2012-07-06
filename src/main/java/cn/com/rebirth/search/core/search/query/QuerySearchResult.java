/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QuerySearchResult.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.query;

import java.io.IOException;

import org.apache.lucene.search.TopDocs;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.core.search.SearchShardTarget;
import cn.com.rebirth.search.core.search.facet.Facets;
import cn.com.rebirth.search.core.search.facet.InternalFacets;

/**
 * The Class QuerySearchResult.
 *
 * @author l.xue.nong
 */
public class QuerySearchResult implements Streamable, QuerySearchResultProvider {

	/** The id. */
	private long id;

	/** The shard target. */
	private SearchShardTarget shardTarget;

	/** The from. */
	private int from;

	/** The size. */
	private int size;

	/** The top docs. */
	private TopDocs topDocs;

	/** The facets. */
	private InternalFacets facets;

	/** The search timed out. */
	private boolean searchTimedOut;

	/**
	 * Instantiates a new query search result.
	 */
	public QuerySearchResult() {

	}

	/**
	 * Instantiates a new query search result.
	 *
	 * @param id the id
	 * @param shardTarget the shard target
	 */
	public QuerySearchResult(long id, SearchShardTarget shardTarget) {
		this.id = id;
		this.shardTarget = shardTarget;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.query.QuerySearchResultProvider#includeFetch()
	 */
	@Override
	public boolean includeFetch() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.query.QuerySearchResultProvider#queryResult()
	 */
	@Override
	public QuerySearchResult queryResult() {
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
		return shardTarget;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#shardTarget(cn.com.rebirth.search.core.search.SearchShardTarget)
	 */
	@Override
	public void shardTarget(SearchShardTarget shardTarget) {
		this.shardTarget = shardTarget;
	}

	/**
	 * Search timed out.
	 *
	 * @param searchTimedOut the search timed out
	 */
	public void searchTimedOut(boolean searchTimedOut) {
		this.searchTimedOut = searchTimedOut;
	}

	/**
	 * Search timed out.
	 *
	 * @return true, if successful
	 */
	public boolean searchTimedOut() {
		return searchTimedOut;
	}

	/**
	 * Top docs.
	 *
	 * @return the top docs
	 */
	public TopDocs topDocs() {
		return topDocs;
	}

	/**
	 * Top docs.
	 *
	 * @param topDocs the top docs
	 */
	public void topDocs(TopDocs topDocs) {
		this.topDocs = topDocs;
	}

	/**
	 * Facets.
	 *
	 * @return the facets
	 */
	public Facets facets() {
		return facets;
	}

	/**
	 * Facets.
	 *
	 * @param facets the facets
	 */
	public void facets(InternalFacets facets) {
		this.facets = facets;
	}

	/**
	 * From.
	 *
	 * @return the int
	 */
	public int from() {
		return from;
	}

	/**
	 * From.
	 *
	 * @param from the from
	 * @return the query search result
	 */
	public QuerySearchResult from(int from) {
		this.from = from;
		return this;
	}

	/**
	 * Size.
	 *
	 * @return the int
	 */
	public int size() {
		return size;
	}

	/**
	 * Size.
	 *
	 * @param size the size
	 * @return the query search result
	 */
	public QuerySearchResult size(int size) {
		this.size = size;
		return this;
	}

	/**
	 * Read query search result.
	 *
	 * @param in the in
	 * @return the query search result
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static QuerySearchResult readQuerySearchResult(StreamInput in) throws IOException {
		QuerySearchResult result = new QuerySearchResult();
		result.readFrom(in);
		return result;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		id = in.readLong();

		from = in.readVInt();
		size = in.readVInt();
		topDocs = Lucene.readTopDocs(in);
		if (in.readBoolean()) {
			facets = InternalFacets.readFacets(in);
		}
		searchTimedOut = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeLong(id);

		out.writeVInt(from);
		out.writeVInt(size);
		Lucene.writeTopDocs(out, topDocs, 0);
		if (facets == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			facets.writeTo(out);
		}
		out.writeBoolean(searchTimedOut);
	}
}
