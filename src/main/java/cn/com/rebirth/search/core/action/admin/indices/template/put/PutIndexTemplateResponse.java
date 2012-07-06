/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PutIndexTemplateResponse.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.template.put;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionResponse;


/**
 * The Class PutIndexTemplateResponse.
 *
 * @author l.xue.nong
 */
public class PutIndexTemplateResponse implements ActionResponse, Streamable {

	
	/** The acknowledged. */
	private boolean acknowledged;

	
	/**
	 * Instantiates a new put index template response.
	 */
	PutIndexTemplateResponse() {
	}

	
	/**
	 * Instantiates a new put index template response.
	 *
	 * @param acknowledged the acknowledged
	 */
	PutIndexTemplateResponse(boolean acknowledged) {
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
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		acknowledged = in.readBoolean();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeBoolean(acknowledged);
	}
}
