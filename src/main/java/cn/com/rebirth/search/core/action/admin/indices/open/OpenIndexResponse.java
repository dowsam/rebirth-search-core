/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core OpenIndexResponse.java 2012-7-6 14:29:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.open;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionResponse;

/**
 * The Class OpenIndexResponse.
 *
 * @author l.xue.nong
 */
public class OpenIndexResponse implements ActionResponse, Streamable {

	/** The acknowledged. */
	private boolean acknowledged;

	/**
	 * Instantiates a new open index response.
	 */
	OpenIndexResponse() {
	}

	/**
	 * Instantiates a new open index response.
	 *
	 * @param acknowledged the acknowledged
	 */
	OpenIndexResponse(boolean acknowledged) {
		this.acknowledged = acknowledged;
	}

	/**
	 * Acknowledged.
	 *
	 * @return true, if successful
	 */
	public boolean acknowledged() {
		return acknowledged;
	}

	/**
	 * Gets the acknowledged.
	 *
	 * @return the acknowledged
	 */
	public boolean getAcknowledged() {
		return acknowledged();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		acknowledged = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeBoolean(acknowledged);
	}
}
