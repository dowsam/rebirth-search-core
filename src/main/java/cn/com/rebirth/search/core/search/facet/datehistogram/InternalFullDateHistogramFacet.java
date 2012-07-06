/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalFullDateHistogramFacet.java 2012-3-29 15:02:17 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.datehistogram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.trove.ExtTLongObjectHashMap;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.search.facet.Facet;


/**
 * The Class InternalFullDateHistogramFacet.
 *
 * @author l.xue.nong
 */
public class InternalFullDateHistogramFacet extends InternalDateHistogramFacet {

	
	/** The Constant STREAM_TYPE. */
	private static final String STREAM_TYPE = "fdHistogram";

	
	/**
	 * Register streams.
	 */
	public static void registerStreams() {
		Streams.registerStream(STREAM, STREAM_TYPE);
	}

	
	/** The STREAM. */
	static Stream STREAM = new Stream() {
		@Override
		public Facet readFacet(String type, StreamInput in) throws IOException {
			return readHistogramFacet(in);
		}
	};

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.InternalFacet#streamType()
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

		
		/** The time. */
		private final long time;

		
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
		 * @param time the time
		 * @param count the count
		 * @param min the min
		 * @param max the max
		 * @param totalCount the total count
		 * @param total the total
		 */
		public FullEntry(long time, long count, double min, double max, long totalCount, double total) {
			this.time = time;
			this.count = count;
			this.min = min;
			this.max = max;
			this.totalCount = totalCount;
			this.total = total;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#time()
		 */
		@Override
		public long time() {
			return time;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getTime()
		 */
		@Override
		public long getTime() {
			return time();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#count()
		 */
		@Override
		public long count() {
			return count;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getCount()
		 */
		@Override
		public long getCount() {
			return count();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#total()
		 */
		@Override
		public double total() {
			return total;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getTotal()
		 */
		@Override
		public double getTotal() {
			return total();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#totalCount()
		 */
		@Override
		public long totalCount() {
			return totalCount;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getTotalCount()
		 */
		@Override
		public long getTotalCount() {
			return this.totalCount;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#mean()
		 */
		@Override
		public double mean() {
			if (totalCount == 0) {
				return totalCount;
			}
			return total / totalCount;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getMean()
		 */
		@Override
		public double getMean() {
			return total / totalCount;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#min()
		 */
		@Override
		public double min() {
			return this.min;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getMin()
		 */
		@Override
		public double getMin() {
			return this.min;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#max()
		 */
		@Override
		public double max() {
			return this.max;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet.Entry#getMax()
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

	
	/** The t entries. */
	ExtTLongObjectHashMap<FullEntry> tEntries;

	
	/** The cached entries. */
	boolean cachedEntries;

	
	/** The entries. */
	Collection<FullEntry> entries;

	
	/**
	 * Instantiates a new internal full date histogram facet.
	 */
	private InternalFullDateHistogramFacet() {
	}

	
	/**
	 * Instantiates a new internal full date histogram facet.
	 *
	 * @param name the name
	 * @param comparatorType the comparator type
	 * @param entries the entries
	 * @param cachedEntries the cached entries
	 */
	public InternalFullDateHistogramFacet(String name, ComparatorType comparatorType,
			ExtTLongObjectHashMap<InternalFullDateHistogramFacet.FullEntry> entries, boolean cachedEntries) {
		this.name = name;
		this.comparatorType = comparatorType;
		this.tEntries = entries;
		this.cachedEntries = cachedEntries;
		this.entries = entries.valueCollection();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.Facet#name()
	 */
	@Override
	public String name() {
		return this.name;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.Facet#getName()
	 */
	@Override
	public String getName() {
		return name();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.Facet#type()
	 */
	@Override
	public String type() {
		return TYPE;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.Facet#getType()
	 */
	@Override
	public String getType() {
		return type();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet#entries()
	 */
	@Override
	public List<FullEntry> entries() {
		if (!(entries instanceof List)) {
			entries = new ArrayList<FullEntry>(entries);
		}
		return (List<FullEntry>) entries;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.datehistogram.DateHistogramFacet#getEntries()
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
	void releaseCache() {
		if (cachedEntries) {
			CacheRecycler.pushLongObjectMap(tEntries);
			cachedEntries = false;
			tEntries = null;
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.datehistogram.InternalDateHistogramFacet#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		if (facets.size() == 1) {
			
			InternalFullDateHistogramFacet internalFacet = (InternalFullDateHistogramFacet) facets.get(0);
			List<FullEntry> entries = internalFacet.entries();
			Collections.sort(entries, comparatorType.comparator());
			internalFacet.releaseCache();
			return internalFacet;
		}

		ExtTLongObjectHashMap<FullEntry> map = CacheRecycler.popLongObjectMap();

		for (Facet facet : facets) {
			InternalFullDateHistogramFacet histoFacet = (InternalFullDateHistogramFacet) facet;
			for (FullEntry fullEntry : histoFacet.entries) {
				FullEntry current = map.get(fullEntry.time);
				if (current != null) {
					current.count += fullEntry.count;
					current.total += fullEntry.total;
					current.totalCount += fullEntry.totalCount;
					if (fullEntry.min < current.min) {
						current.min = fullEntry.min;
					}
					if (fullEntry.max > current.max) {
						current.max = fullEntry.max;
					}
				} else {
					map.put(fullEntry.time, fullEntry);
				}
			}
			histoFacet.releaseCache();
		}

		
		Object[] values = map.internalValues();
		Arrays.sort(values, (Comparator) comparatorType.comparator());
		List<FullEntry> ordered = new ArrayList<FullEntry>(map.size());
		for (int i = 0; i < map.size(); i++) {
			FullEntry value = (FullEntry) values[i];
			if (value == null) {
				break;
			}
			ordered.add(value);
		}

		CacheRecycler.pushLongObjectMap(map);

		
		InternalFullDateHistogramFacet ret = new InternalFullDateHistogramFacet();
		ret.name = name;
		ret.comparatorType = comparatorType;
		ret.entries = ordered;
		return ret;
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
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.field(Fields._TYPE, TYPE);
		builder.startArray(Fields.ENTRIES);
		for (Entry entry : entries()) {
			builder.startObject();
			builder.field(Fields.TIME, entry.time());
			builder.field(Fields.COUNT, entry.count());
			builder.field(Fields.MIN, entry.min());
			builder.field(Fields.MAX, entry.max());
			builder.field(Fields.TOTAL, entry.total());
			builder.field(Fields.TOTAL_COUNT, entry.totalCount());
			builder.field(Fields.MEAN, entry.mean());
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
	 * @return the internal full date histogram facet
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InternalFullDateHistogramFacet readHistogramFacet(StreamInput in) throws IOException {
		InternalFullDateHistogramFacet facet = new InternalFullDateHistogramFacet();
		facet.readFrom(in);
		return facet;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		name = in.readUTF();
		comparatorType = ComparatorType.fromId(in.readByte());

		cachedEntries = false;
		int size = in.readVInt();
		entries = new ArrayList<FullEntry>(size);
		for (int i = 0; i < size; i++) {
			entries.add(new FullEntry(in.readLong(), in.readVLong(), in.readDouble(), in.readDouble(), in.readVLong(),
					in.readDouble()));
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(name);
		out.writeByte(comparatorType.id());
		out.writeVInt(entries.size());
		for (FullEntry entry : entries) {
			out.writeLong(entry.time);
			out.writeVLong(entry.count);
			out.writeDouble(entry.min);
			out.writeDouble(entry.max);
			out.writeVLong(entry.totalCount);
			out.writeDouble(entry.total);
		}
		releaseCache();
	}
}