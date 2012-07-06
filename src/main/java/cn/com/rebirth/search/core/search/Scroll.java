/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core Scroll.java 2012-7-6 14:30:15 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.TimeValue;

/**
 * The Class Scroll.
 *
 * @author l.xue.nong
 */
public class Scroll implements Streamable {

	/** The keep alive. */
	private TimeValue keepAlive;

	/**
	 * Instantiates a new scroll.
	 */
	private Scroll() {

	}

	/**
	 * Instantiates a new scroll.
	 *
	 * @param keepAlive the keep alive
	 */
	public Scroll(TimeValue keepAlive) {
		this.keepAlive = keepAlive;
	}

	/**
	 * Keep alive.
	 *
	 * @return the time value
	 */
	public TimeValue keepAlive() {
		return keepAlive;
	}

	/**
	 * Read scroll.
	 *
	 * @param in the in
	 * @return the scroll
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Scroll readScroll(StreamInput in) throws IOException {
		Scroll scroll = new Scroll();
		scroll.readFrom(in);
		return scroll;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		if (in.readBoolean()) {
			keepAlive = TimeValue.readTimeValue(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		if (keepAlive == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			keepAlive.writeTo(out);
		}
	}
}
