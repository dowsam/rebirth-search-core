/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HttpInfo.java 2012-7-6 14:29:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.http;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.transport.BoundTransportAddress;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;

/**
 * The Class HttpInfo.
 *
 * @author l.xue.nong
 */
public class HttpInfo implements Streamable, Serializable, ToXContent {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7098235936576017447L;

	/** The address. */
	private BoundTransportAddress address;

	/**
	 * Instantiates a new http info.
	 */
	HttpInfo() {
	}

	/**
	 * Instantiates a new http info.
	 *
	 * @param address the address
	 */
	public HttpInfo(BoundTransportAddress address) {
		this.address = address;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant HTTP. */
		static final XContentBuilderString HTTP = new XContentBuilderString("http");

		/** The Constant BOUND_ADDRESS. */
		static final XContentBuilderString BOUND_ADDRESS = new XContentBuilderString("bound_address");

		/** The Constant PUBLISH_ADDRESS. */
		static final XContentBuilderString PUBLISH_ADDRESS = new XContentBuilderString("publish_address");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.HTTP);
		builder.field(Fields.BOUND_ADDRESS, address.boundAddress().toString());
		builder.field(Fields.PUBLISH_ADDRESS, address.publishAddress().toString());
		builder.endObject();
		return builder;
	}

	/**
	 * Read http info.
	 *
	 * @param in the in
	 * @return the http info
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static HttpInfo readHttpInfo(StreamInput in) throws IOException {
		HttpInfo info = new HttpInfo();
		info.readFrom(in);
		return info;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		address = BoundTransportAddress.readBoundTransportAddress(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		address.writeTo(out);
	}

	/**
	 * Address.
	 *
	 * @return the bound transport address
	 */
	public BoundTransportAddress address() {
		return address;
	}

	/**
	 * Gets the address.
	 *
	 * @return the address
	 */
	public BoundTransportAddress getAddress() {
		return address();
	}
}
