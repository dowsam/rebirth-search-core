/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DocsStats.java 2012-7-6 14:29:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.xcontent.ToXContent;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentBuilderString;

/**
 * The Class DocsStats.
 *
 * @author l.xue.nong
 */
public class DocsStats implements Streamable, ToXContent {

	/** The count. */
	long count = 0;

	/** The deleted. */
	long deleted = 0;

	/**
	 * Instantiates a new docs stats.
	 */
	public DocsStats() {

	}

	/**
	 * Instantiates a new docs stats.
	 *
	 * @param count the count
	 * @param deleted the deleted
	 */
	public DocsStats(long count, long deleted) {
		this.count = count;
		this.deleted = deleted;
	}

	/**
	 * Adds the.
	 *
	 * @param docsStats the docs stats
	 */
	public void add(DocsStats docsStats) {
		if (docsStats == null) {
			return;
		}
		count += docsStats.count;
		deleted += docsStats.deleted;
	}

	/**
	 * Count.
	 *
	 * @return the long
	 */
	public long count() {
		return this.count;
	}

	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public long getCount() {
		return this.count;
	}

	/**
	 * Deleted.
	 *
	 * @return the long
	 */
	public long deleted() {
		return this.deleted;
	}

	/**
	 * Gets the deleted.
	 *
	 * @return the deleted
	 */
	public long getDeleted() {
		return this.deleted;
	}

	/**
	 * Read doc stats.
	 *
	 * @param in the in
	 * @return the docs stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static DocsStats readDocStats(StreamInput in) throws IOException {
		DocsStats docsStats = new DocsStats();
		docsStats.readFrom(in);
		return docsStats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		count = in.readVLong();
		deleted = in.readVLong();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVLong(count);
		out.writeVLong(deleted);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.DOCS);
		builder.field(Fields.COUNT, count);
		builder.field(Fields.DELETED, deleted);
		builder.endObject();
		return builder;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant DOCS. */
		static final XContentBuilderString DOCS = new XContentBuilderString("docs");

		/** The Constant COUNT. */
		static final XContentBuilderString COUNT = new XContentBuilderString("count");

		/** The Constant DELETED. */
		static final XContentBuilderString DELETED = new XContentBuilderString("deleted");
	}
}
