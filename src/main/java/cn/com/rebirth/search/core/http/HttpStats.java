/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HttpStats.java 2012-7-6 14:30:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.http;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.xcontent.ToXContent;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentBuilderString;

/**
 * The Class HttpStats.
 *
 * @author l.xue.nong
 */
public class HttpStats implements Streamable, ToXContent {

	/** The server open. */
	private long serverOpen;

	/** The total open. */
	private long totalOpen;

	/**
	 * Instantiates a new http stats.
	 */
	HttpStats() {

	}

	/**
	 * Instantiates a new http stats.
	 *
	 * @param serverOpen the server open
	 * @param totalOpen the total open
	 */
	public HttpStats(long serverOpen, long totalOpen) {
		this.serverOpen = serverOpen;
		this.totalOpen = totalOpen;
	}

	/**
	 * Server open.
	 *
	 * @return the long
	 */
	public long serverOpen() {
		return this.serverOpen;
	}

	/**
	 * Gets the server open.
	 *
	 * @return the server open
	 */
	public long getServerOpen() {
		return serverOpen();
	}

	/**
	 * Total open.
	 *
	 * @return the long
	 */
	public long totalOpen() {
		return this.totalOpen;
	}

	/**
	 * Gets the total open.
	 *
	 * @return the total open
	 */
	public long getTotalOpen() {
		return this.totalOpen;
	}

	/**
	 * Read http stats.
	 *
	 * @param in the in
	 * @return the http stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static HttpStats readHttpStats(StreamInput in) throws IOException {
		HttpStats stats = new HttpStats();
		stats.readFrom(in);
		return stats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		serverOpen = in.readVLong();
		totalOpen = in.readVLong();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVLong(serverOpen);
		out.writeVLong(totalOpen);
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant HTTP. */
		static final XContentBuilderString HTTP = new XContentBuilderString("http");

		/** The Constant CURRENT_OPEN. */
		static final XContentBuilderString CURRENT_OPEN = new XContentBuilderString("current_open");

		/** The Constant TOTAL_OPENED. */
		static final XContentBuilderString TOTAL_OPENED = new XContentBuilderString("total_opened");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.HTTP);
		builder.field(Fields.CURRENT_OPEN, serverOpen);
		builder.field(Fields.TOTAL_OPENED, totalOpen);
		builder.endObject();
		return builder;
	}
}