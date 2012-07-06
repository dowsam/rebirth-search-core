/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesExistsResponse.java 2012-7-6 14:30:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.exists;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionResponse;

/**
 * The Class IndicesExistsResponse.
 *
 * @author l.xue.nong
 */
public class IndicesExistsResponse implements ActionResponse, Streamable {

	/** The exists. */
	private boolean exists;

	/**
	 * Instantiates a new indices exists response.
	 */
	IndicesExistsResponse() {
	}

	/**
	 * Instantiates a new indices exists response.
	 *
	 * @param exists the exists
	 */
	public IndicesExistsResponse(boolean exists) {
		this.exists = exists;
	}

	/**
	 * Exists.
	 *
	 * @return true, if successful
	 */
	public boolean exists() {
		return this.exists;
	}

	/**
	 * Checks if is exists.
	 *
	 * @return true, if is exists
	 */
	public boolean isExists() {
		return exists();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		exists = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeBoolean(exists);
	}
}