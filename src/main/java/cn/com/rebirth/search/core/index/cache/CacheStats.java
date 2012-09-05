/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CacheStats.java 2012-7-6 14:29:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.xcontent.ToXContent;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentBuilderString;

/**
 * The Class CacheStats.
 *
 * @author l.xue.nong
 */
public class CacheStats implements Streamable, ToXContent {

	/** The field evictions. */
	long fieldEvictions;

	/** The filter evictions. */
	long filterEvictions;

	/** The filter count. */
	long filterCount;

	/** The field size. */
	long fieldSize = 0;

	/** The filter size. */
	long filterSize = 0;

	/** The bloom size. */
	long bloomSize = 0;

	/**
	 * Instantiates a new cache stats.
	 */
	public CacheStats() {
	}

	/**
	 * Instantiates a new cache stats.
	 *
	 * @param fieldEvictions the field evictions
	 * @param filterEvictions the filter evictions
	 * @param fieldSize the field size
	 * @param filterSize the filter size
	 * @param filterCount the filter count
	 * @param bloomSize the bloom size
	 */
	public CacheStats(long fieldEvictions, long filterEvictions, long fieldSize, long filterSize, long filterCount,
			long bloomSize) {
		this.fieldEvictions = fieldEvictions;
		this.filterEvictions = filterEvictions;
		this.fieldSize = fieldSize;
		this.filterSize = filterSize;
		this.filterCount = filterCount;
		this.bloomSize = bloomSize;
	}

	/**
	 * Adds the.
	 *
	 * @param stats the stats
	 */
	public void add(CacheStats stats) {
		this.fieldEvictions += stats.fieldEvictions;
		this.filterEvictions += stats.filterEvictions;
		this.fieldSize += stats.fieldSize;
		this.filterSize += stats.filterSize;
		this.filterCount += stats.filterCount;
		this.bloomSize += stats.bloomSize;
	}

	/**
	 * Field evictions.
	 *
	 * @return the long
	 */
	public long fieldEvictions() {
		return this.fieldEvictions;
	}

	/**
	 * Gets the field evictions.
	 *
	 * @return the field evictions
	 */
	public long getFieldEvictions() {
		return this.fieldEvictions();
	}

	/**
	 * Filter evictions.
	 *
	 * @return the long
	 */
	public long filterEvictions() {
		return this.filterEvictions;
	}

	/**
	 * Gets the filter evictions.
	 *
	 * @return the filter evictions
	 */
	public long getFilterEvictions() {
		return this.filterEvictions;
	}

	/**
	 * Filter mem evictions.
	 *
	 * @return the long
	 */
	public long filterMemEvictions() {
		return this.filterEvictions;
	}

	/**
	 * Gets the filter mem evictions.
	 *
	 * @return the filter mem evictions
	 */
	public long getFilterMemEvictions() {
		return this.filterEvictions;
	}

	/**
	 * Filter count.
	 *
	 * @return the long
	 */
	public long filterCount() {
		return this.filterCount;
	}

	/**
	 * Gets the filter count.
	 *
	 * @return the filter count
	 */
	public long getFilterCount() {
		return filterCount;
	}

	/**
	 * Field size in bytes.
	 *
	 * @return the long
	 */
	public long fieldSizeInBytes() {
		return this.fieldSize;
	}

	/**
	 * Gets the field size in bytes.
	 *
	 * @return the field size in bytes
	 */
	public long getFieldSizeInBytes() {
		return fieldSizeInBytes();
	}

	/**
	 * Field size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue fieldSize() {
		return new ByteSizeValue(fieldSize);
	}

	/**
	 * Gets the field size.
	 *
	 * @return the field size
	 */
	public ByteSizeValue getFieldSize() {
		return this.fieldSize();
	}

	/**
	 * Filter size in bytes.
	 *
	 * @return the long
	 */
	public long filterSizeInBytes() {
		return this.filterSize;
	}

	/**
	 * Gets the filter size in bytes.
	 *
	 * @return the filter size in bytes
	 */
	public long getFilterSizeInBytes() {
		return this.filterSizeInBytes();
	}

	/**
	 * Filter size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue filterSize() {
		return new ByteSizeValue(filterSize);
	}

	/**
	 * Gets the filter size.
	 *
	 * @return the filter size
	 */
	public ByteSizeValue getFilterSize() {
		return filterSize();
	}

	/**
	 * Bloom size in bytes.
	 *
	 * @return the long
	 */
	public long bloomSizeInBytes() {
		return this.bloomSize;
	}

	/**
	 * Gets the bloom size in bytes.
	 *
	 * @return the bloom size in bytes
	 */
	public long getBloomSizeInBytes() {
		return this.bloomSize;
	}

	/**
	 * Bloom size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue bloomSize() {
		return new ByteSizeValue(bloomSize);
	}

	/**
	 * Gets the bloom size.
	 *
	 * @return the bloom size
	 */
	public ByteSizeValue getBloomSize() {
		return bloomSize();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.CACHE);
		builder.field(Fields.FIELD_EVICTIONS, fieldEvictions);
		builder.field(Fields.FIELD_SIZE, fieldSize().toString());
		builder.field(Fields.FIELD_SIZE_IN_BYTES, fieldSize);
		builder.field(Fields.FILTER_COUNT, filterCount);
		builder.field(Fields.FILTER_EVICTIONS, filterEvictions);
		builder.field(Fields.FILTER_SIZE, filterSize().toString());
		builder.field(Fields.FILTER_SIZE_IN_BYTES, filterSize);
		builder.endObject();
		return builder;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant CACHE. */
		static final XContentBuilderString CACHE = new XContentBuilderString("cache");

		/** The Constant FIELD_SIZE. */
		static final XContentBuilderString FIELD_SIZE = new XContentBuilderString("field_size");

		/** The Constant FIELD_SIZE_IN_BYTES. */
		static final XContentBuilderString FIELD_SIZE_IN_BYTES = new XContentBuilderString("field_size_in_bytes");

		/** The Constant FIELD_EVICTIONS. */
		static final XContentBuilderString FIELD_EVICTIONS = new XContentBuilderString("field_evictions");

		/** The Constant FILTER_EVICTIONS. */
		static final XContentBuilderString FILTER_EVICTIONS = new XContentBuilderString("filter_evictions");

		/** The Constant FILTER_COUNT. */
		static final XContentBuilderString FILTER_COUNT = new XContentBuilderString("filter_count");

		/** The Constant FILTER_SIZE. */
		static final XContentBuilderString FILTER_SIZE = new XContentBuilderString("filter_size");

		/** The Constant FILTER_SIZE_IN_BYTES. */
		static final XContentBuilderString FILTER_SIZE_IN_BYTES = new XContentBuilderString("filter_size_in_bytes");
	}

	/**
	 * Read cache stats.
	 *
	 * @param in the in
	 * @return the cache stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static CacheStats readCacheStats(StreamInput in) throws IOException {
		CacheStats stats = new CacheStats();
		stats.readFrom(in);
		return stats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		fieldEvictions = in.readVLong();
		filterEvictions = in.readVLong();
		fieldSize = in.readVLong();
		filterSize = in.readVLong();
		filterCount = in.readVLong();
		bloomSize = in.readVLong();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVLong(fieldEvictions);
		out.writeVLong(filterEvictions);
		out.writeVLong(fieldSize);
		out.writeVLong(filterSize);
		out.writeVLong(filterCount);
		out.writeVLong(bloomSize);
	}
}