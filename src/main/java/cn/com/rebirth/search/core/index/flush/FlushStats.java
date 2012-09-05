/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FlushStats.java 2012-7-6 14:30:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.flush;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.ToXContent;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentBuilderString;

/**
 * The Class FlushStats.
 *
 * @author l.xue.nong
 */
public class FlushStats implements Streamable, ToXContent {

	/** The total. */
	private long total;

	/** The total time in millis. */
	private long totalTimeInMillis;

	/**
	 * Instantiates a new flush stats.
	 */
	public FlushStats() {

	}

	/**
	 * Instantiates a new flush stats.
	 *
	 * @param total the total
	 * @param totalTimeInMillis the total time in millis
	 */
	public FlushStats(long total, long totalTimeInMillis) {
		this.total = total;
		this.totalTimeInMillis = totalTimeInMillis;
	}

	/**
	 * Adds the.
	 *
	 * @param total the total
	 * @param totalTimeInMillis the total time in millis
	 */
	public void add(long total, long totalTimeInMillis) {
		this.total += total;
		this.totalTimeInMillis += totalTimeInMillis;
	}

	/**
	 * Adds the.
	 *
	 * @param flushStats the flush stats
	 */
	public void add(FlushStats flushStats) {
		if (flushStats == null) {
			return;
		}
		this.total += flushStats.total;
		this.totalTimeInMillis += flushStats.totalTimeInMillis;
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
	 * Read flush stats.
	 *
	 * @param in the in
	 * @return the flush stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static FlushStats readFlushStats(StreamInput in) throws IOException {
		FlushStats flushStats = new FlushStats();
		flushStats.readFrom(in);
		return flushStats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.FLUSH);
		builder.field(Fields.TOTAL, total);
		builder.field(Fields.TOTAL_TIME, totalTime().toString());
		builder.field(Fields.TOTAL_TIME_IN_MILLIS, totalTimeInMillis);
		builder.endObject();
		return builder;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant FLUSH. */
		static final XContentBuilderString FLUSH = new XContentBuilderString("flush");

		/** The Constant TOTAL. */
		static final XContentBuilderString TOTAL = new XContentBuilderString("total");

		/** The Constant TOTAL_TIME. */
		static final XContentBuilderString TOTAL_TIME = new XContentBuilderString("total_time");

		/** The Constant TOTAL_TIME_IN_MILLIS. */
		static final XContentBuilderString TOTAL_TIME_IN_MILLIS = new XContentBuilderString("total_time_in_millis");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		total = in.readVLong();
		totalTimeInMillis = in.readVLong();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVLong(total);
		out.writeVLong(totalTimeInMillis);
	}
}