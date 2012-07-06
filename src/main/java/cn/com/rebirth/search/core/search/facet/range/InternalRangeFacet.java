/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalRangeFacet.java 2012-7-6 14:28:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.range;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.InternalFacet;

import com.google.common.collect.ImmutableList;

/**
 * The Class InternalRangeFacet.
 *
 * @author l.xue.nong
 */
public class InternalRangeFacet implements RangeFacet, InternalFacet {

	/** The Constant STREAM_TYPE. */
	private static final String STREAM_TYPE = "range";

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
			return readRangeFacet(in);
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

	/** The entries. */
	Entry[] entries;

	/**
	 * Instantiates a new internal range facet.
	 */
	InternalRangeFacet() {
	}

	/**
	 * Instantiates a new internal range facet.
	 *
	 * @param name the name
	 * @param entries the entries
	 */
	public InternalRangeFacet(String name, Entry[] entries) {
		this.name = name;
		this.entries = entries;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#name()
	 */
	@Override
	public String name() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#getName()
	 */
	@Override
	public String getName() {
		return name();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#type()
	 */
	@Override
	public String type() {
		return RangeFacet.TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#getType()
	 */
	@Override
	public String getType() {
		return RangeFacet.TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.range.RangeFacet#entries()
	 */
	@Override
	public List<Entry> entries() {
		return ImmutableList.copyOf(entries);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.range.RangeFacet#getEntries()
	 */
	@Override
	public List<Entry> getEntries() {
		return entries();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry> iterator() {
		return entries().iterator();
	}

	/**
	 * Read range facet.
	 *
	 * @param in the in
	 * @return the internal range facet
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InternalRangeFacet readRangeFacet(StreamInput in) throws IOException {
		InternalRangeFacet facet = new InternalRangeFacet();
		facet.readFrom(in);
		return facet;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		name = in.readUTF();
		entries = new Entry[in.readVInt()];
		for (int i = 0; i < entries.length; i++) {
			Entry entry = new Entry();
			entry.from = in.readDouble();
			entry.to = in.readDouble();
			if (in.readBoolean()) {
				entry.fromAsString = in.readUTF();
			}
			if (in.readBoolean()) {
				entry.toAsString = in.readUTF();
			}
			entry.count = in.readVLong();
			entry.totalCount = in.readVLong();
			entry.total = in.readDouble();
			entry.min = in.readDouble();
			entry.max = in.readDouble();
			entries[i] = entry;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(name);
		out.writeVInt(entries.length);
		for (Entry entry : entries) {
			out.writeDouble(entry.from);
			out.writeDouble(entry.to);
			if (entry.fromAsString == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeUTF(entry.fromAsString);
			}
			if (entry.toAsString == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeUTF(entry.toAsString);
			}
			out.writeVLong(entry.count);
			out.writeVLong(entry.totalCount);
			out.writeDouble(entry.total);
			out.writeDouble(entry.min);
			out.writeDouble(entry.max);
		}
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant _TYPE. */
		static final XContentBuilderString _TYPE = new XContentBuilderString("_type");

		/** The Constant RANGES. */
		static final XContentBuilderString RANGES = new XContentBuilderString("ranges");

		/** The Constant FROM. */
		static final XContentBuilderString FROM = new XContentBuilderString("from");

		/** The Constant FROM_STR. */
		static final XContentBuilderString FROM_STR = new XContentBuilderString("from_str");

		/** The Constant TO. */
		static final XContentBuilderString TO = new XContentBuilderString("to");

		/** The Constant TO_STR. */
		static final XContentBuilderString TO_STR = new XContentBuilderString("to_str");

		/** The Constant COUNT. */
		static final XContentBuilderString COUNT = new XContentBuilderString("count");

		/** The Constant TOTAL. */
		static final XContentBuilderString TOTAL = new XContentBuilderString("total");

		/** The Constant TOTAL_COUNT. */
		static final XContentBuilderString TOTAL_COUNT = new XContentBuilderString("total_count");

		/** The Constant MEAN. */
		static final XContentBuilderString MEAN = new XContentBuilderString("mean");

		/** The Constant MIN. */
		static final XContentBuilderString MIN = new XContentBuilderString("min");

		/** The Constant MAX. */
		static final XContentBuilderString MAX = new XContentBuilderString("max");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.field(Fields._TYPE, "range");
		builder.startArray(Fields.RANGES);
		for (Entry entry : entries) {
			builder.startObject();
			if (!Double.isInfinite(entry.from)) {
				builder.field(Fields.FROM, entry.from);
			}
			if (entry.fromAsString != null) {
				builder.field(Fields.FROM_STR, entry.fromAsString);
			}
			if (!Double.isInfinite(entry.to)) {
				builder.field(Fields.TO, entry.to);
			}
			if (entry.toAsString != null) {
				builder.field(Fields.TO_STR, entry.toAsString);
			}
			builder.field(Fields.COUNT, entry.count());

			if (entry.totalCount() > 0) {
				builder.field(Fields.MIN, entry.min());
				builder.field(Fields.MAX, entry.max());
			}
			builder.field(Fields.TOTAL_COUNT, entry.totalCount());
			builder.field(Fields.TOTAL, entry.total());
			builder.field(Fields.MEAN, entry.mean());
			builder.endObject();
		}
		builder.endArray();
		builder.endObject();
		return builder;
	}
}
