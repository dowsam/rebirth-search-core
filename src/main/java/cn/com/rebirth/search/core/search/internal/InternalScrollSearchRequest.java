/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalScrollSearchRequest.java 2012-7-6 14:30:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.internal;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.search.Scroll;

/**
 * The Class InternalScrollSearchRequest.
 *
 * @author l.xue.nong
 */
public class InternalScrollSearchRequest implements Streamable {

	/** The id. */
	private long id;

	/** The scroll. */
	private Scroll scroll;

	/**
	 * Instantiates a new internal scroll search request.
	 */
	public InternalScrollSearchRequest() {
	}

	/**
	 * Instantiates a new internal scroll search request.
	 *
	 * @param id the id
	 */
	public InternalScrollSearchRequest(long id) {
		this.id = id;
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
	 * Scroll.
	 *
	 * @return the scroll
	 */
	public Scroll scroll() {
		return scroll;
	}

	/**
	 * Scroll.
	 *
	 * @param scroll the scroll
	 * @return the internal scroll search request
	 */
	public InternalScrollSearchRequest scroll(Scroll scroll) {
		this.scroll = scroll;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		id = in.readLong();
		if (in.readBoolean()) {
			scroll = Scroll.readScroll(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeLong(id);
		if (scroll == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			scroll.writeTo(out);
		}
	}
}
