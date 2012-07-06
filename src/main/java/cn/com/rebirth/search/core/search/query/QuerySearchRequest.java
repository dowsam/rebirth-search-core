/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QuerySearchRequest.java 2012-7-6 14:28:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.query;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.search.dfs.AggregatedDfs;

/**
 * The Class QuerySearchRequest.
 *
 * @author l.xue.nong
 */
public class QuerySearchRequest implements Streamable {

	/** The id. */
	private long id;

	/** The dfs. */
	private AggregatedDfs dfs;

	/**
	 * Instantiates a new query search request.
	 */
	public QuerySearchRequest() {
	}

	/**
	 * Instantiates a new query search request.
	 *
	 * @param id the id
	 * @param dfs the dfs
	 */
	public QuerySearchRequest(long id, AggregatedDfs dfs) {
		this.id = id;
		this.dfs = dfs;
	}

	/**
	 * Id.
	 *
	 * @return the long
	 */
	public long id() {
		return id;
	}

	/**
	 * Dfs.
	 *
	 * @return the aggregated dfs
	 */
	public AggregatedDfs dfs() {
		return dfs;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		id = in.readLong();
		dfs = AggregatedDfs.readAggregatedDfs(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeLong(id);
		dfs.writeTo(out);
	}
}
