/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalCountDateHistogramFacet.java 2012-7-6 14:29:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.datehistogram;

import gnu.trove.iterator.TLongLongIterator;
import gnu.trove.map.hash.TLongLongHashMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.search.facet.Facet;

/**
 * The Class InternalCountDateHistogramFacet.
 *
 * @author l.xue.nong
 */
public class InternalCountDateHistogramFacet extends InternalDateHistogramFacet {

	/** The Constant STREAM_TYPE. */
	private static final String STREAM_TYPE = "cdHistogram";

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

		/** The time. */
		private final long time;

		/** The count. */
		private final long count;

		/**
		 * Instantiates a new count entry.
		 *
		 * @param time the time
		 * @param count the count
		 */
		public CountEntry(long time, long count) {
			this.time = time;
			this.count = count;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#time()
		 */
		@Override
		public long time() {
			return time;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getTime()
		 */
		@Override
		public long getTime() {
			return time();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#count()
		 */
		@Override
		public long count() {
			return count;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getCount()
		 */
		@Override
		public long getCount() {
			return count();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#totalCount()
		 */
		@Override
		public long totalCount() {
			return 0;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getTotalCount()
		 */
		@Override
		public long getTotalCount() {
			return 0;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#total()
		 */
		@Override
		public double total() {
			return Double.NaN;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getTotal()
		 */
		@Override
		public double getTotal() {
			return total();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#mean()
		 */
		@Override
		public double mean() {
			return Double.NaN;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getMean()
		 */
		@Override
		public double getMean() {
			return mean();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#min()
		 */
		@Override
		public double min() {
			return Double.NaN;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getMin()
		 */
		@Override
		public double getMin() {
			return Double.NaN;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#max()
		 */
		@Override
		public double max() {
			return Double.NaN;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getMax()
		 */
		@Override
		public double getMax() {
			return Double.NaN;
		}
	}

	/** The name. */
	private String name;

	/** The comparator type. */
	private ComparatorType comparatorType;

	/** The counts. */
	TLongLongHashMap counts;

	/** The cached counts. */
	boolean cachedCounts;

	/** The entries. */
	CountEntry[] entries = null;

	/**
	 * Instantiates a new internal count date histogram facet.
	 */
	private InternalCountDateHistogramFacet() {
	}

	/**
	 * Instantiates a new internal count date histogram facet.
	 *
	 * @param name the name
	 * @param comparatorType the comparator type
	 * @param counts the counts
	 * @param cachedCounts the cached counts
	 */
	public InternalCountDateHistogramFacet(String name, ComparatorType comparatorType, TLongLongHashMap counts,
			boolean cachedCounts) {
		this.name = name;
		this.comparatorType = comparatorType;
		this.counts = counts;
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
	 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet#entries()
	 */
	@Override
	public List<CountEntry> entries() {
		return Arrays.asList(computeEntries());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacet#getEntries()
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
	 * Release cache.
	 */
	void releaseCache() {
		if (cachedCounts) {
			CacheRecycler.pushLongLongMap(counts);
			cachedCounts = false;
			counts = null;
		}
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
		entries = new CountEntry[counts.size()];
		int i = 0;
		for (TLongLongIterator it = counts.iterator(); it.hasNext();) {
			it.advance();
			entries[i++] = new CountEntry(it.key(), it.value());
		}
		releaseCache();
		Arrays.sort(entries, comparatorType.comparator());
		return entries;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.datehistogram.InternalDateHistogramFacet#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		if (facets.size() == 1) {
			return facets.get(0);
		}
		TLongLongHashMap counts = CacheRecycler.popLongLongMap();

		for (Facet facet : facets) {
			InternalCountDateHistogramFacet histoFacet = (InternalCountDateHistogramFacet) facet;
			for (TLongLongIterator it = histoFacet.counts.iterator(); it.hasNext();) {
				it.advance();
				counts.adjustOrPutValue(it.key(), it.value(), it.value());
			}
			histoFacet.releaseCache();

		}

		return new InternalCountDateHistogramFacet(name, comparatorType, counts, true);
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

		/** The Constant TIME. */
		static final XContentBuilderString TIME = new XContentBuilderString("time");

		/** The Constant COUNT. */
		static final XContentBuilderString COUNT = new XContentBuilderString("count");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.field(Fields._TYPE, TYPE);
		builder.startArray(Fields.ENTRIES);
		for (Entry entry : computeEntries()) {
			builder.startObject();
			builder.field(Fields.TIME, entry.time());
			builder.field(Fields.COUNT, entry.count());
			builder.endObject();
		}
		builder.endArray();
		builder.endObject();
		return builder;
	}

	/**
	 * Read histogram facet.
	 *
	 * @param in the in
	 * @return the internal count date histogram facet
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InternalCountDateHistogramFacet readHistogramFacet(StreamInput in) throws IOException {
		InternalCountDateHistogramFacet facet = new InternalCountDateHistogramFacet();
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

		int size = in.readVInt();
		counts = CacheRecycler.popLongLongMap();
		cachedCounts = true;
		for (int i = 0; i < size; i++) {
			long key = in.readLong();
			counts.put(key, in.readVLong());
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(name);
		out.writeByte(comparatorType.id());
		out.writeVInt(counts.size());
		for (TLongLongIterator it = counts.iterator(); it.hasNext();) {
			it.advance();
			out.writeLong(it.key());
			out.writeVLong(it.value());
		}
		releaseCache();
	}
}