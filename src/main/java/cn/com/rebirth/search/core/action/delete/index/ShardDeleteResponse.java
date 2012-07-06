/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardDeleteResponse.java 2012-3-29 15:02:37 l.xue.nong$$
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
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		version = in.readLong();
		notFound = in.readBoolean();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeLong(version);
		out.writeBoolean(notFound);
	}
}