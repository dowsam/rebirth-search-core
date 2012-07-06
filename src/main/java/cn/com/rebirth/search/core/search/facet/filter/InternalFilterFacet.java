/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalFilterFacet.java 2012-7-6 14:29:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.filter;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.InternalFacet;

/**
 * The Class InternalFilterFacet.
 *
 * @author l.xue.nong
 */
public class InternalFilterFacet implements FilterFacet, InternalFacet {

	/** The Constant STREAM_TYPE. */
	private static final String STREAM_TYPE = "filter";

	/**
	 * Register streams.
	 */
	public static void registerStreams() {
		Streams.registerStream(STREAM, STREAM_TYPE);
	}

	/** The stream. */
	static Stream STREAM = new Stream() {
		@Override
		public Facet readFacet(String type, StreamInput in) throws IOException {
			return readFilterFacet(in);
		}
	};

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.InternalFacet#streamType()
	 */
	@Override
	public String streamType() {
		return STREAM_TYPE;
	}

	/** The name. */
	private String name;

	/** The count. */
	private long count;

	/**
	 * Instantiates a new internal filter facet.
	 */
	private InternalFilterFacet() {

	}

	/**
	 * Instantiates a new internal filter facet.
	 *
	 * @param name the name
	 * @param count the count
	 */
	public InternalFilterFacet(String name, long count) {
		this.name = name;
		this.count = count;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#type()
	 */
	@Override
	public String type() {
		return TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#name()
	 */
	public String name() {
		return name;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#getName()
	 */
	@Override
	public String getName() {
		return name();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.filter.FilterFacet#count()
	 */
	public long count() {
		return count;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.filter.FilterFacet#getCount()
	 */
	public long getCount() {
		return count;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant _TYPE. */
		static final XContentBuilderString _TYPE = new XContentBuilderString("_type");

		/** The Constant COUNT. */
		static final XContentBuilderString COUNT = new XContentBuilderString("count");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.field(Fields._TYPE, FilterFacet.TYPE);
		builder.field(Fields.COUNT, count);
		builder.endObject();
		return builder;
	}

	/**
	 * Read filter facet.
	 *
	 * @param in the in
	 * @return the filter facet
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static FilterFacet readFilterFacet(StreamInput in) throws IOException {
		InternalFilterFacet result = new InternalFilterFacet();
		result.readFrom(in);
		return result;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		name = in.readUTF();
		count = in.readVLong();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(name);
		out.writeVLong(count);
	}
}