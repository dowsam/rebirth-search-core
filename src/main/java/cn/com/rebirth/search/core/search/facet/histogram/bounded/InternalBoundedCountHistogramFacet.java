/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalBoundedCountHistogramFacet.java 2012-7-6 14:29:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.histogram.bounded;

import java.io.IOException;
import java.util.Arrays;
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
 * The Class InternalBoundedCountHistogramFacet.
 *
 * @author l.xue.nong
 */
public class InternalBoundedCountHistogramFacet extends InternalHistogramFacet {

	/** The Constant STREAM_TYPE. */
	private static final String STREAM_TYPE = "cBdHistogram";

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
	 * The Class CountEntry.
	 *
	 * @author l.xue.nong
	 */
	public static class CountEntry implements Entry {

		/** The key. */
		private final long key;

		/** The count. */
		private final long count;

		/**
		 * Instantiates a new count entry.
		 *
		 * @param key the key
		 * @param count the count
		 */
		public CountEntry(long key, long count) {
			this.key = key;
			this.count = count;
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
			return Double.NaN;
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
			return 0;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#getTotalCount()
		 */
		@Override
		public long getTotalCount() {
			return 0;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#mean()
		 */
		@Override
		public double mean() {
			return Double.NaN;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#getMean()
		 */
		@Override
		public double getMean() {
			return mean();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#min()
		 */
		@Override
		public double min() {
			return Double.NaN;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#getMin()
		 */
		@Override
		public double getMin() {
			return Double.NaN;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#max()
		 */
		@Override
		public double max() {
			return Double.NaN;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet.Entry#getMax()
		 */
		@Override
		public double getMax() {
			return Double.NaN;
		}
	}

	/** The name. */
	private String name;

	/** The comparator type. */
	ComparatorType comparatorType;

	/** The cached counts. */
	boolean cachedCounts;

	/** The counts. */
	int[] counts;

	/** The size. */
	int size;

	/** The interval. */
	long interval;

	/** The offset. */
	long offset;

	/** The entries. */
	CountEntry[] entries = null;

	/**
	 * Instantiates a new internal bounded count histogram facet.
	 */
	private InternalBoundedCountHistogramFacet() {
	}

	/**
	 * Instantiates a new internal bounded count histogram facet.
	 *
	 * @param name the name
	 * @param comparatorType the comparator type
	 * @param interval the interval
	 * @param offset the offset
	 * @param size the size
	 * @param counts the counts
	 * @param cachedCounts the cached counts
	 */
	public InternalBoundedCountHistogramFacet(String name, ComparatorType comparatorType, long interval, long offset,
			int size, int[] counts, boolean cachedCounts) {
		this.name = name;
		this.comparatorType = comparatorType;
		this.interval = interval;
		this.offset = offset;
		this.counts = counts;
		this.size = size;
		this.cachedCounts = cachedCounts;
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
	public List<CountEntry> entries() {
		return Arrays.asList(computeEntries());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet#getEntries()
	 */
	@Override
	public List<CountEntry> getEntries() {
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
	 * Compute entries.
	 *
	 * @return the count entry[]
	 */
	private CountEntry[] computeEntries() {
		if (entries != null) {
			return entries;
		}
		entries = new CountEntry[size];
		for (int i = 0; i < size; i++) {
			entries[i] = new CountEntry((i * interval) + offset, counts[i]);
		}
		releaseCache();
		return entries;
	}

	/**
	 * Release cache.
	 */
	void releaseCache() {
		if (cachedCounts) {
			cachedCounts = false;
			CacheRecycler.pushIntArray(counts);
			counts = null;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.histogram.InternalHistogramFacet#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		if (facets.size() == 1) {
			InternalBoundedCountHistogramFacet firstHistoFacet = (InternalBoundedCountHistogramFacet) facets.get(0);
			if (comparatorType != ComparatorType.KEY) {
				Arrays.sort(firstHistoFacet.entries, comparatorType.comparator());
			}
			return facets.get(0);
		}
		InternalBoundedCountHistogramFacet firstHistoFacet = (InternalBoundedCountHistogramFacet) facets.get(0);
		for (int i = 1; i < facets.size(); i++) {
			InternalBoundedCountHistogramFacet histoFacet = (InternalBoundedCountHistogramFacet) facets.get(i);
			for (int j = 0; j < firstHistoFacet.size; j++) {
				firstHistoFacet.counts[j] += histoFacet.counts[j];
			}
			histoFacet.releaseCache();
		}
		if (comparatorType != ComparatorType.KEY) {
			Arrays.sort(firstHistoFacet.entries, comparatorType.comparator());
		}

		return firstHistoFacet;
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
			builder.startObject();
			builder.field(Fields.KEY, (i * interval) + offset);
			builder.field(Fields.COUNT, counts[i]);
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
	 * @return the internal bounded count histogram facet
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InternalBoundedCountHistogramFacet readHistogramFacet(StreamInput in) throws IOException {
		InternalBoundedCountHistogramFacet facet = new InternalBoundedCountHistogramFacet();
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
		counts = CacheRecycler.popIntArray(size);
		cachedCounts = true;
		for (int i = 0; i < size; i++) {
			counts[i] = in.readVInt();
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
			out.writeVInt(counts[i]);
		}
		releaseCache();
	}
}