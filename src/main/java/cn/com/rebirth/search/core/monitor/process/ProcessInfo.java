/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ProcessInfo.java 2012-7-6 14:29:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.process;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;

/**
 * The Class ProcessInfo.
 *
 * @author l.xue.nong
 */
public class ProcessInfo implements Streamable, Serializable, ToXContent {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7804950551112597330L;

	/** The refresh interval. */
	long refreshInterval;

	/** The id. */
	private long id;

	/** The max file descriptors. */
	private long maxFileDescriptors = -1;

	/**
	 * Instantiates a new process info.
	 */
	ProcessInfo() {

	}

	/**
	 * Instantiates a new process info.
	 *
	 * @param id the id
	 * @param maxFileDescriptors the max file descriptors
	 */
	public ProcessInfo(long id, long maxFileDescriptors) {
		this.id = id;
		this.maxFileDescriptors = maxFileDescriptors;
	}

	/**
	 * Refresh interval.
	 *
	 * @return the long
	 */
	public long refreshInterval() {
		return this.refreshInterval;
	}

	/**
	 * Gets the refresh interval.
	 *
	 * @return the refresh interval
	 */
	public long getRefreshInterval() {
		return this.refreshInterval;
	}

	/**
	 * Id.
	 *
	 * @return the long
	 */
	public long id() {
		return this.id;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public long getId() {
		return id();
	}

	/**
	 * Max file descriptors.
	 *
	 * @return the long
	 */
	public long maxFileDescriptors() {
		return this.maxFileDescriptors;
	}

	/**
	 * Gets the max file descriptors.
	 *
	 * @return the max file descriptors
	 */
	public long getMaxFileDescriptors() {
		return maxFileDescriptors;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant PROCESS. */
		static final XContentBuilderString PROCESS = new XContentBuilderString("process");

		/** The Constant REFRESH_INTERVAL. */
		static final XContentBuilderString REFRESH_INTERVAL = new XContentBuilderString("refresh_interval");

		/** The Constant ID. */
		static final XContentBuilderString ID = new XContentBuilderString("id");

		/** The Constant MAX_FILE_DESCRIPTORS. */
		static final XContentBuilderString MAX_FILE_DESCRIPTORS = new XContentBuilderString("max_file_descriptors");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.PROCESS);
		builder.field(Fields.REFRESH_INTERVAL, refreshInterval);
		builder.field(Fields.ID, id);
		builder.field(Fields.MAX_FILE_DESCRIPTORS, maxFileDescriptors);
		builder.endObject();
		return builder;
	}

	/**
	 * Read process info.
	 *
	 * @param in the in
	 * @return the process info
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ProcessInfo readProcessInfo(StreamInput in) throws IOException {
		ProcessInfo info = new ProcessInfo();
		info.readFrom(in);
		return info;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		refreshInterval = in.readLong();
		id = in.readLong();
		maxFileDescriptors = in.readLong();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeLong(refreshInterval);
		out.writeLong(id);
		out.writeLong(maxFileDescriptors);
	}
}
