/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DeleteIndexTemplateResponse.java 2012-7-6 14:29:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.template.delete;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionResponse;

/**
 * The Class DeleteIndexTemplateResponse.
 *
 * @author l.xue.nong
 */
public class DeleteIndexTemplateResponse implements ActionResponse, Streamable {

	/** The acknowledged. */
	private boolean acknowledged;

	/**
	 * Instantiates a new delete index template response.
	 */
	DeleteIndexTemplateResponse() {
	}

	/**
	 * Instantiates a new delete index template response.
	 *
	 * @param acknowledged the acknowledged
	 */
	DeleteIndexTemplateResponse(boolean acknowledged) {
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
