/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StoreStats.java 2012-7-6 14:29:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.xcontent.ToXContent;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentBuilderString;

/**
 * The Class StoreStats.
 *
 * @author l.xue.nong
 */
public class StoreStats implements Streamable, ToXContent {

	/** The size in bytes. */
	private long sizeInBytes;

	/**
	 * Instantiates a new store stats.
	 */
	public StoreStats() {

	}

	/**
	 * Instantiates a new store stats.
	 *
	 * @param sizeInBytes the size in bytes
	 */
	public StoreStats(long sizeInBytes) {
		this.sizeInBytes = sizeInBytes;
	}

	/**
	 * Adds the.
	 *
	 * @param stats the stats
	 */
	public void add(StoreStats stats) {
		if (stats == null) {
			return;
		}
		sizeInBytes += stats.sizeInBytes;
	}

	/**
	 * Size in bytes.
	 *
	 * @return the long
	 */
	public long sizeInBytes() {
		return sizeInBytes;
	}

	/**
	 * Gets the size in bytes.
	 *
	 * @return the size in bytes
	 */
	public long getSizeInBytes() {
		return sizeInBytes;
	}

	/**
	 * Size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue size() {
		return new ByteSizeValue(sizeInBytes);
	}

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public ByteSizeValue getSize() {
		return size();
	}

	/**
	 * Read store stats.
	 *
	 * @param in the in
	 * @return the store stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static StoreStats readStoreStats(StreamInput in) throws IOException {
		StoreStats store = new StoreStats();
		store.readFrom(in);
		return store;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		sizeInBytes = in.readVLong();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVLong(sizeInBytes);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.STORE);
		builder.field(Fields.SIZE, size().toString());
		builder.field(Fields.SIZE_IN_BYTES, sizeInBytes);
		builder.endObject();
		return builder;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant STORE. */
		static final XContentBuilderString STORE = new XContentBuilderString("store");

		/** The Constant SIZE. */
		static final XContentBuilderString SIZE = new XContentBuilderString("size");

		/** The Constant SIZE_IN_BYTES. */
		static final XContentBuilderString SIZE_IN_BYTES = new XContentBuilderString("size_in_bytes");
	}
}
