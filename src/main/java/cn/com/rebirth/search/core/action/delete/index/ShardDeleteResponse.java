/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardDeleteResponse.java 2012-7-6 14:29:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.delete.index;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionResponse;

/**
 * The Class ShardDeleteResponse.
 *
 * @author l.xue.nong
 */
public class ShardDeleteResponse implements ActionResponse, Streamable {

	/** The version. */
	private long version;

	/** The not found. */
	private boolean notFound;

	/**
	 * Instantiates a new shard delete response.
	 */
	public ShardDeleteResponse() {
	}

	/**
	 * Instantiates a new shard delete response.
	 *
	 * @param version the version
	 * @param notFound the not found
	 */
	public ShardDeleteResponse(long version, boolean notFound) {
		this.version = version;
		this.notFound = notFound;
	}

	/**
	 * Version.
	 *
	 * @return the long
	 */
	public long version() {
		return version;
	}

	/**
	 * Not found.
	 *
	 * @return true, if successful
	 */
	public boolean notFound() {
		return notFound;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		version = in.readLong();
		notFound = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeLong(version);
		out.writeBoolean(notFound);
	}
}