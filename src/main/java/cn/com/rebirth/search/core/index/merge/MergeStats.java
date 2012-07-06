/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MergeStats.java 2012-7-6 14:30:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.merge;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;

/**
 * The Class MergeStats.
 *
 * @author l.xue.nong
 */
public class MergeStats implements Streamable, ToXContent {

	/** The total. */
	private long total;

	/** The total time in millis. */
	private long totalTimeInMillis;

	/** The total num docs. */
	private long totalNumDocs;

	/** The total size in bytes. */
	private long totalSizeInBytes;

	/** The current. */
	private long current;

	/** The current num docs. */
	private long currentNumDocs;

	/** The current size in bytes. */
	private long currentSizeInBytes;

	/**
	 * Instantiates a new merge stats.
	 */
	public MergeStats() {

	}

	/**
	 * Adds the.
	 *
	 * @param totalMerges the total merges
	 * @param totalMergeTime the total merge time
	 * @param totalNumDocs the total num docs
	 * @param totalSizeInBytes the total size in bytes
	 * @param currentMerges the current merges
	 * @param currentNumDocs the current num docs
	 * @param currentSizeInBytes the current size in bytes
	 */
	public void add(long totalMerges, long totalMergeTime, long totalNumDocs, long totalSizeInBytes,
			long currentMerges, long currentNumDocs, long currentSizeInBytes) {
		this.total += totalMerges;
		this.totalTimeInMillis += totalMergeTime;
		this.totalNumDocs += totalNumDocs;
		this.totalSizeInBytes += totalSizeInBytes;
		this.current += currentMerges;
		this.currentNumDocs += currentNumDocs;
		this.currentSizeInBytes += currentSizeInBytes;
	}

	/**
	 * Adds the.
	 *
	 * @param mergeStats the merge stats
	 */
	public void add(MergeStats mergeStats) {
		if (mergeStats == null) {
			return;
		}
		this.total += mergeStats.total;
		this.totalTimeInMillis += mergeStats.totalTimeInMillis;
		this.totalNumDocs += mergeStats.totalNumDocs;
		this.totalSizeInBytes += mergeStats.totalSizeInBytes;
		this.current += mergeStats.current;
		this.currentNumDocs += mergeStats.currentNumDocs;
		this.currentSizeInBytes += mergeStats.currentSizeInBytes;
	}

	/**
	 * Total.
	 *
	 * @return the long
	 */
	public long total() {
		return this.total;
	}

	/**
	 * Total time in millis.
	 *
	 * @return the long
	 */
	public long totalTimeInMillis() {
		return this.totalTimeInMillis;
	}

	/**
	 * Total time.
	 *
	 * @return the time value
	 */
	public TimeValue totalTime() {
		return new TimeValue(totalTimeInMillis);
	}

	/**
	 * Total num docs.
	 *
	 * @return the long
	 */
	public long totalNumDocs() {
		return this.totalNumDocs;
	}

	/**
	 * Total size in bytes.
	 *
	 * @return the long
	 */
	public long totalSizeInBytes() {
		return this.totalSizeInBytes;
	}

	/**
	 * Total size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue totalSize() {
		return new ByteSizeValue(totalSizeInBytes);
	}

	/**
	 * Current.
	 *
	 * @return the long
	 */
	public long current() {
		return this.current;
	}

	/**
	 * Current num docs.
	 *
	 * @return the long
	 */
	public long currentNumDocs() {
		return this.currentNumDocs;
	}

	/**
	 * Current size in bytes.
	 *
	 * @return the long
	 */
	public long currentSizeInBytes() {
		return this.currentSizeInBytes;
	}

	/**
	 * Current size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue currentSize() {
		return new ByteSizeValue(currentSizeInBytes);
	}

	/**
	 * Read merge stats.
	 *
	 * @param in the in
	 * @return the merge stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static MergeStats readMergeStats(StreamInput in) throws IOException {
		MergeStats stats = new MergeStats();
		stats.readFrom(in);
		return stats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.MERGES);
		builder.field(Fields.CURRENT, current);
		builder.field(Fields.CURRENT_DOCS, currentNumDocs);
		builder.field(Fields.CURRENT_SIZE, currentSize().toString());
		builder.field(Fields.CURRENT_SIZE_IN_BYTES, currentSizeInBytes);
		builder.field(Fields.TOTAL, total);
		builder.field(Fields.TOTAL_TIME, totalTime().toString());
		builder.field(Fields.TOTAL_TIME_IN_MILLIS, totalTimeInMillis);
		builder.field(Fields.TOTAL_DOCS, totalNumDocs);
		builder.field(Fields.TOTAL_SIZE, totalSize().toString());
		builder.field(Fields.TOTAL_SIZE_IN_BYTES, totalSizeInBytes);
		builder.endObject();
		return builder;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant MERGES. */
		static final XContentBuilderString MERGES = new XContentBuilderString("merges");

		/** The Constant CURRENT. */
		static final XContentBuilderString CURRENT = new XContentBuilderString("current");

		/** The Constant CURRENT_DOCS. */
		static final XContentBuilderString CURRENT_DOCS = new XContentBuilderString("current_docs");

		/** The Constant CURRENT_SIZE. */
		static final XContentBuilderString CURRENT_SIZE = new XContentBuilderString("current_size");

		/** The Constant CURRENT_SIZE_IN_BYTES. */
		static final XContentBuilderString CURRENT_SIZE_IN_BYTES = new XContentBuilderString("current_size_in_bytes");

		/** The Constant TOTAL. */
		static final XContentBuilderString TOTAL = new XContentBuilderString("total");

		/** The Constant TOTAL_TIME. */
		static final XContentBuilderString TOTAL_TIME = new XContentBuilderString("total_time");

		/** The Constant TOTAL_TIME_IN_MILLIS. */
		static final XContentBuilderString TOTAL_TIME_IN_MILLIS = new XContentBuilderString("total_time_in_millis");

		/** The Constant TOTAL_DOCS. */
		static final XContentBuilderString TOTAL_DOCS = new XContentBuilderString("total_docs");

		/** The Constant TOTAL_SIZE. */
		static final XContentBuilderString TOTAL_SIZE = new XContentBuilderString("total_size");

		/** The Constant TOTAL_SIZE_IN_BYTES. */
		static final XContentBuilderString TOTAL_SIZE_IN_BYTES = new XContentBuilderString("total_size_in_bytes");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		total = in.readVLong();
		totalTimeInMillis = in.readVLong();
		totalNumDocs = in.readVLong();
		totalSizeInBytes = in.readVLong();
		current = in.readVLong();
		currentNumDocs = in.readVLong();
		currentSizeInBytes = in.readVLong();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVLong(total);
		out.writeVLong(totalTimeInMillis);
		out.writeVLong(totalNumDocs);
		out.writeVLong(totalSizeInBytes);
		out.writeVLong(current);
		out.writeVLong(currentNumDocs);
		out.writeVLong(currentSizeInBytes);
	}
}