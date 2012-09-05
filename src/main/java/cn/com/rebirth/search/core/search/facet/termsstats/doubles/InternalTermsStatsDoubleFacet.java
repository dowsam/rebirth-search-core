/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalTermsStatsDoubleFacet.java 2012-7-6 14:29:20 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.termsstats.doubles;

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
import cn.com.rebirth.commons.trove.ExtTDoubleObjectHashMap;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.termsstats.InternalTermsStatsFacet;

import com.google.common.collect.ImmutableList;

/**
 * The Class InternalTermsStatsDoubleFacet.
 *
 * @author l.xue.nong
 */
public class InternalTermsStatsDoubleFacet extends InternalTermsStatsFacet {

	/** The Constant STREAM_TYPE. */
	private static final String STREAM_TYPE = "dTS";

	/**
	 * Register stream.
	 */
	public static void registerStream() {
		Streams.registerStream(STREAM, STREAM_TYPE);
	}

	/** The stream. */
	static Stream STREAM = new Stream() {
		@Override
		public Facet readFacet(String type, StreamInput in) throws IOException {
			return readTermsStatsFacet(in);
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
	 * Instantiates a new internal terms stats double facet.
	 */
	public InternalTermsStatsDoubleFacet() {
	}

	/**
	 * The Class DoubleEntry.
	 *
	 * @author l.xue.nong
	 */
	public static class DoubleEntry implements Entry {

		/** The term. */
		double term;

		/** The count. */
		long count;

		/** The total count. */
		long totalCount;

		/** The total. */
		double total;

		/** The min. */
		double min;

		/** The max. */
		double max;

		/**
		 * Instantiates a new double entry.
		 *
		 * @param term the term
		 * @param count the count
		 * @param totalCount the total count
		 * @param total the total
		 * @param min the min
		 * @param max the max
		 */
		public DoubleEntry(double term, long count, long totalCount, double total, double min, double max) {
			this.term = term;
			this.count = count;
			this.total = total;
			this.totalCount = totalCount;
			this.min = min;
			this.max = max;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#term()
		 */
		@Override
		public String term() {
			return Double.toString(term);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#getTerm()
		 */
		@Override
		public String getTerm() {
			return term();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#termAsNumber()
		 */
		@Override
		public Number termAsNumber() {
			return term;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#getTermAsNumber()
		 */
		@Override
		public Number getTermAsNumber() {
			return termAsNumber();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#count()
		 */
		@Override
		public long count() {
			return count;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#getCount()
		 */
		@Override
		public long getCount() {
			return count();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#totalCount()
		 */
		@Override
		public long totalCount() {
			return this.totalCount;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#getTotalCount()
		 */
		@Override
		public long getTotalCount() {
			return this.totalCount;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#min()
		 */
		@Override
		public double min() {
			return this.min;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#getMin()
		 */
		@Override
		public double getMin() {
			return min();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#max()
		 */
		@Override
		public double max() {
			return max;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#getMax()
		 */
		@Override
		public double getMax() {
			return max();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#total()
		 */
		@Override
		public double total() {
			return total;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#getTotal()
		 */
		@Override
		public double getTotal() {
			return total();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#mean()
		 */
		@Override
		public double mean() {
			if (totalCount == 0) {
				return 0;
			}
			return total / totalCount;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet.Entry#getMean()
		 */
		@Override
		public double getMean() {
			return mean();
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Entry o) {
			DoubleEntry other = (DoubleEntry) o;
			return (term < other.term ? -1 : (term == other.term ? 0 : 1));
		}
	}

	/** The name. */
	private String name;

	/** The required size. */
	int requiredSize;

	/** The missing. */
	long missing;

	/** The entries. */
	Collection<DoubleEntry> entries = ImmutableList.of();

	/** The comparator type. */
	ComparatorType comparatorType;

	/**
	 * Instantiates a new internal terms stats double facet.
	 *
	 * @param name the name
	 * @param comparatorType the comparator type
	 * @param requiredSize the required size
	 * @param entries the entries
	 * @param missing the missing
	 */
	public InternalTermsStatsDoubleFacet(String name, ComparatorType comparatorType, int requiredSize,
			Collection<DoubleEntry> entries, long missing) {
		this.name = name;
		this.comparatorType = comparatorType;
		this.requiredSize = requiredSize;
		this.entries = entries;
		this.missing = missing;
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
		return this.name;
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
	 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet#entries()
	 */
	@Override
	public List<DoubleEntry> entries() {
		if (!(entries instanceof List)) {
			entries = ImmutableList.copyOf(entries);
		}
		return (List<DoubleEntry>) entries;
	}

	/**
	 * Mutable list.
	 *
	 * @return the list
	 */
	List<DoubleEntry> mutableList() {
		if (!(entries instanceof List)) {
			entries = new ArrayList<DoubleEntry>(entries);
		}
		return (List<DoubleEntry>) entries;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet#getEntries()
	 */
	@Override
	public List<DoubleEntry> getEntries() {
		return entries();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public Iterator<Entry> iterator() {
		return (Iterator) entries.iterator();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet#missingCount()
	 */
	@Override
	public long missingCount() {
		return this.missing;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet#getMissingCount()
	 */
	@Override
	public long getMissingCount() {
		return missingCount();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.termsstats.InternalTermsStatsFacet#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		if (facets.size() == 1) {
			if (requiredSize == 0) {

				InternalTermsStatsDoubleFacet tsFacet = (InternalTermsStatsDoubleFacet) facets.get(0);
				if (!tsFacet.entries.isEmpty()) {
					List<DoubleEntry> entries = tsFacet.mutableList();
					Collections.sort(entries, comparatorType.comparator());
				}
			}
			return facets.get(0);
		}
		int missing = 0;
		ExtTDoubleObjectHashMap<DoubleEntry> map = CacheRecycler.popDoubleObjectMap();
		for (Facet facet : facets) {
			InternalTermsStatsDoubleFacet tsFacet = (InternalTermsStatsDoubleFacet) facet;
			missing += tsFacet.missing;
			for (Entry entry : tsFacet) {
				DoubleEntry doubleEntry = (DoubleEntry) entry;
				DoubleEntry current = map.get(doubleEntry.term);
				if (current != null) {
					current.count += doubleEntry.count;
					current.totalCount += doubleEntry.totalCount;
					current.total += doubleEntry.total;
					if (doubleEntry.min < current.min) {
						current.min = doubleEntry.min;
					}
					if (doubleEntry.max > current.max) {
						current.max = doubleEntry.max;
					}
				} else {
					map.put(doubleEntry.term, doubleEntry);
				}
			}
		}

		if (requiredSize == 0) {
			DoubleEntry[] entries1 = map.values(new DoubleEntry[map.size()]);
			Arrays.sort(entries1, comparatorType.comparator());
			CacheRecycler.pushDoubleObjectMap(map);
			return new InternalTermsStatsDoubleFacet(name, comparatorType, requiredSize, Arrays.asList(entries1),
					missing);
		} else {
			Object[] values = map.internalValues();
			Arrays.sort(values, (Comparator) comparatorType.comparator());
			List<DoubleEntry> ordered = new ArrayList<DoubleEntry>(map.size());
			for (int i = 0; i < requiredSize; i++) {
				DoubleEntry value = (DoubleEntry) values[i];
				if (value == null) {
					break;
				}
				ordered.add(value);
			}
			CacheRecycler.pushDoubleObjectMap(map);
			return new InternalTermsStatsDoubleFacet(name, comparatorType, requiredSize, ordered, missing);
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

		/** The Constant MISSING. */
		static final XContentBuilderString MISSING = new XContentBuilderString("missing");

		/** The Constant TERMS. */
		static final XContentBuilderString TERMS = new XContentBuilderString("terms");

		/** The Constant TERM. */
		static final XContentBuilderString TERM = new XContentBuilderString("term");

		/** The Constant COUNT. */
		static final XContentBuilderString COUNT = new XContentBuilderString("count");

		/** The Constant TOTAL_COUNT. */
		static final XContentBuilderString TOTAL_COUNT = new XContentBuilderString("total_count");

		/** The Constant MIN. */
		static final XContentBuilderString MIN = new XContentBuilderString("min");

		/** The Constant MAX. */
		static final XContentBuilderString MAX = new XContentBuilderString("max");

		/** The Constant TOTAL. */
		static final XContentBuilderString TOTAL = new XContentBuilderString("total");

		/** The Constant MEAN. */
		static final XContentBuilderString MEAN = new XContentBuilderString("mean");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.field(Fields._TYPE, InternalTermsStatsFacet.TYPE);
		builder.field(Fields.MISSING, missing);
		builder.startArray(Fields.TERMS);
		for (Entry entry : entries) {
			builder.startObject();
			builder.field(Fields.TERM, ((DoubleEntry) entry).term);
			builder.field(Fields.COUNT, entry.count());
			builder.field(Fields.TOTAL_COUNT, entry.totalCount());
			builder.field(Fields.MIN, entry.min());
			builder.field(Fields.MAX, entry.max());
			builder.field(Fields.TOTAL, entry.total());
			builder.field(Fields.MEAN, entry.mean());
			builder.endObject();
		}
		builder.endArray();
		builder.endObject();
		return builder;
	}

	/**
	 * Read terms stats facet.
	 *
	 * @param in the in
	 * @return the internal terms stats double facet
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InternalTermsStatsDoubleFacet readTermsStatsFacet(StreamInput in) throws IOException {
		InternalTermsStatsDoubleFacet facet = new InternalTermsStatsDoubleFacet();
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
		requiredSize = in.readVInt();
		missing = in.readVLong();

		int size = in.readVInt();
		entries = new ArrayList<DoubleEntry>(size);
		for (int i = 0; i < size; i++) {
			entries.add(new DoubleEntry(in.readDouble(), in.readVLong(), in.readVLong(), in.readDouble(), in
					.readDouble(), in.readDouble()));
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(name);
		out.writeByte(comparatorType.id());
		out.writeVInt(requiredSize);
		out.writeVLong(missing);

		out.writeVInt(entries.size());
		for (Entry entry : entries) {
			out.writeDouble(((DoubleEntry) entry).term);
			out.writeVLong(entry.count());
			out.writeVLong(entry.totalCount());
			out.writeDouble(entry.total());
			out.writeDouble(entry.min());
			out.writeDouble(entry.max());
		}
	}
}