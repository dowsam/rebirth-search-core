/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalBoundedFullHistogramFacet.java 2012-7-6 14:28:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.histogram.bounded;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet;
import cn.com.rebirth.search.core.search.facet.histogram.InternalHistogramFacet;

/**
 * The Class InternalBoundedFullHistogramFacet.
 *
 * @author l.xue.nong
 */
public class InternalBoundedFullHistogramFacet extends InternalHistogramFacet {

	/** The Constant STREAM_TYPE. */
	private static final String STREAM_TYPE = "fBdHistogram";

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
			return readHistogramFacet(in);
		}
	};

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.InternalFacet#streamType()
	 */
	@Override
	public String streamType() {
		return STREAM_TYPE;
	}

	/**
	 * The Class FullEntry.
	 *
	 * @author l.xue.nong
	 */
	public static class FullEntry implements Entry {

		/** The key. */
		long key;

		/** The count. */
		long count;

		/** The total count. */
		long totalCount;

		/** The total. */
		double total;

		/** The min. */
		double min = Double.POSITIVE_INFINITY;

		/** The max. */
		double max = Double.NEGATIVE_INFINITY;

		/**
		 * Instantiates a new full entry.
		 *
		 * @param key the key
		 * @param count the count
		 * @param min the min
		 * @param max the max
		 * @param totalCount the total count
		 * @param total the total
		 */
		public FullEntry(long key, long count, double min, double max, long totalCount, double total) {
			this.key = key;
			this.count = count;
			this.min = min;
			this.max = max;
			this.totalCount = totalCount;
			this.total = total;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#key()
		 */
		@Override
		public long key() {
			return key;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#getKey()
		 */
		@Override
		public long getKey() {
			return key();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#count()
		 */
		@Override
		public long count() {
			return count;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#getCount()
		 */
		@Override
		public long getCount() {
			return count();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#total()
		 */
		@Override
		public double total() {
			return total;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#getTotal()
		 */
		@Override
		public double getTotal() {
			return total();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#totalCount()
		 */
		@Override
		public long totalCount() {
			return totalCount;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#getTotalCount()
		 */
		@Override
		public long getTotalCount() {
			return this.totalCount;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#mean()
		 */
		@Override
		public double mean() {
			if (totalCount == 0) {
				return 0;
			}
			return total / totalCount;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#getMean()
		 */
		@Override
		public double getMean() {
			return total / totalCount;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#min()
		 */
		@Override
		public double min() {
			return this.min;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#getMin()
		 */
		@Override
		public double getMin() {
			return this.min;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#max()
		 */
		@Override
		public double max() {
			return this.max;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#getMax()
		 */
		@Override
		public double getMax() {
			return this.max;
		}
	}

	/** The name. */
	private String name;

	/** The comparator type. */
	private ComparatorType comparatorType;

	/** The entries. */
	Object[] entries;

	/** The entries list. */
	List<Object> entriesList;

	/** The cached entries. */
	boolean cachedEntries;

	/** The size. */
	int size;

	/** The interval. */
	long interval;

	/** The offset. */
	long offset;

	/** The normalized. */
	boolean normalized;

	/**
	 * Instantiates a new internal bounded full histogram facet.
	 */
	private InternalBoundedFullHistogramFacet() {
	}

	/**
	 * Instantiates a new internal bounded full histogram facet.
	 *
	 * @param name the name
	 * @param comparatorType the comparator type
	 * @param interval the interval
	 * @param offset the offset
	 * @param size the size
	 * @param entries the entries
	 * @param cachedEntries the cached entries
	 */
	public InternalBoundedFullHistogramFacet(String name, ComparatorType comparatorType, long interval, long offset,
			int size, Object[] entries, boolean cachedEntries) {
		this.name = name;
		this.comparatorType = comparatorType;
		this.interval = interval;
		this.offset = offset;
		this.size = size;
		this.entries = entries;
		this.cachedEntries = cachedEntries;
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
		return TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#getType()
	 */
	@Override
	public String getType() {
		return type();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet#entries()
	 */
	@Override
	public List<FullEntry> entries() {
		normalize();
		if (entriesList == null) {
			Object[] newEntries = new Object[size];
			System.arraycopy(entries, 0, newEntries, 0, size);
			entriesList = Arrays.asList(newEntries);
		}
		releaseCache();
		return (List) entriesList;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet#getEntries()
	 */
	@Override
	public List<FullEntry> getEntries() {
		return entries();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry> iterator() {
		return (Iterator) entries().iterator();
	}

	/**
	 * Release cache.
	 */
	private void releaseCache() {
		if (cachedEntries) {
			cachedEntries = false;
			CacheRecycler.pushObjectArray(entries);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.histogram.InternalHistogramFacet#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		if (facets.size() == 1) {

			InternalBoundedFullHistogramFacet internalFacet = (InternalBoundedFullHistogramFacet) facets.get(0);
			if (comparatorType != ComparatorType.KEY) {
				Arrays.sort(internalFacet.entries, (Comparator) comparatorType.comparator());
			}
			return internalFacet;
		}

		InternalBoundedFullHistogramFacet first = (InternalBoundedFullHistogramFacet) facets.get(0);

		for (int f = 1; f < facets.size(); f++) {
			InternalBoundedFullHistogramFacet internalFacet = (InternalBoundedFullHistogramFacet) facets.get(f);
			for (int i = 0; i < size; i++) {
				FullEntry aggEntry = (FullEntry) first.entries[i];
				FullEntry entry = (FullEntry) internalFacet.entries[i];
				if (aggEntry == null) {
					first.entries[i] = entry;
				} else if (entry != null) {
					aggEntry.count += entry.count;
					aggEntry.totalCount += entry.totalCount;
					aggEntry.total += entry.total;
					if (entry.min < aggEntry.min) {
						aggEntry.min = entry.min;
					}
					if (entry.max > aggEntry.max) {
						aggEntry.max = entry.max;
					}
				}
			}
			internalFacet.releaseCache();
		}

		if (comparatorType != ComparatorType.KEY) {
			Arrays.sort(first.entries, (Comparator) comparatorType.comparator());
		}

		return first;
	}

	/**
	 * Normalize.
	 */
	private void normalize() {
		if (normalized) {
			return;
		}
		normalized = true;
		for (int i = 0; i < size; i++) {
			FullEntry entry = (FullEntry) entries[i];
			if (entry == null) {
				entries[i] = new FullEntry((i * interval) + offset, 0, Double.NaN, Double.NaN, 0, 0);
			} else {
				entry.key = (i * interval) + offset;
			}
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

		/** The Constant ENTRIES. */
		static final XContentBuilderString ENTRIES = new XContentBuilderString("entries");

		/** The Constant KEY. */
		static final XContentBuilderString KEY = new XContentBuilderString("key");

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
		builder.field(Fields._TYPE, HistogramFacet.TYPE);
		builder.startArray(Fields.ENTRIES);
		for (int i = 0; i < size; i++) {
			FullEntry entry = (FullEntry) entries[i];
			builder.startObject();
			if (normalized) {
				builder.field(Fields.KEY, entry.key());
			} else {
				builder.field(Fields.KEY, (i * interval) + offset);
			}
			if (entry == null) {
				builder.field(Fields.COUNT, 0);
				builder.field(Fields.TOTAL, 0);
				builder.field(Fields.TOTAL_COUNT, 0);
			} else {
				builder.field(Fields.COUNT, entry.count());
				builder.field(Fields.MIN, entry.min());
				builder.field(Fields.MAX, entry.max());
				builder.field(Fields.TOTAL, entry.total());
				builder.field(Fields.TOTAL_COUNT, entry.totalCount());
				builder.field(Fields.MEAN, entry.mean());
			}
			builder.endObject();
		}
		builder.endArray();
		builder.endObject();
		releaseCache();
		return builder;
	}

	/**
	 * Read histogram facet.
	 *
	 * @param in the in
	 * @return the internal bounded full histogram facet
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InternalBoundedFullHistogramFacet readHistogramFacet(StreamInput in) throws IOException {
		InternalBoundedFullHistogramFacet facet = new InternalBoundedFullHistogramFacet();
		facet.readFrom(in);
		return facet;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		name = in.readUTF();
		comparatorType = ComparatorType.fromId(in.readByte());

		offset = in.readLong();
		interval = in.readVLong();
		size = in.readVInt();
		entries = CacheRecycler.popObjectArray(size);
		cachedEntries = true;
		for (int i = 0; i < size; i++) {
			if (in.readBoolean()) {
				entries[i] = new FullEntry(i, in.readVLong(), in.readDouble(), in.readDouble(), in.readVLong(),
						in.readDouble());
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(name);
		out.writeByte(comparatorType.id());
		out.writeLong(offset);
		out.writeVLong(interval);
		out.writeVInt(size);
		for (int i = 0; i < size; i++) {
			FullEntry entry = (FullEntry) entries[i];
			if (entry == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);

				out.writeVLong(entry.count);
				out.writeDouble(entry.min);
				out.writeDouble(entry.max);
				out.writeVLong(entry.totalCount);
				out.writeDouble(entry.total);
			}
		}
		releaseCache();
	}
}