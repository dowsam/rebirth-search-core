/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalIpTermsFacet.java 2012-7-6 14:30:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.terms.ip;

import gnu.trove.iterator.TLongIntIterator;
import gnu.trove.map.hash.TLongIntHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.collect.BoundedTreeSet;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.index.mapper.ip.IpFieldMapper;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.terms.InternalTermsFacet;
import cn.com.rebirth.search.core.search.facet.terms.TermsFacet;

import com.google.common.collect.ImmutableList;

/**
 * The Class InternalIpTermsFacet.
 *
 * @author l.xue.nong
 */
public class InternalIpTermsFacet extends InternalTermsFacet {

	/** The Constant STREAM_TYPE. */
	private static final String STREAM_TYPE = "ipTerms";

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
			return readTermsFacet(in);
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
	 * The Class LongEntry.
	 *
	 * @author l.xue.nong
	 */
	public static class LongEntry implements Entry {

		/** The term. */
		long term;

		/** The count. */
		int count;

		/**
		 * Instantiates a new long entry.
		 *
		 * @param term the term
		 * @param count the count
		 */
		public LongEntry(long term, int count) {
			this.term = term;
			this.count = count;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet.Entry#term()
		 */
		public String term() {
			return IpFieldMapper.longToIp(term);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet.Entry#getTerm()
		 */
		public String getTerm() {
			return term();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet.Entry#termAsNumber()
		 */
		@Override
		public Number termAsNumber() {
			return term;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet.Entry#getTermAsNumber()
		 */
		@Override
		public Number getTermAsNumber() {
			return termAsNumber();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet.Entry#count()
		 */
		public int count() {
			return count;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet.Entry#getCount()
		 */
		public int getCount() {
			return count();
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Entry o) {
			long anotherVal = ((LongEntry) o).term;
			if (term < anotherVal) {
				return -1;
			}
			if (term == anotherVal) {
				int i = count - o.count();
				if (i == 0) {
					i = System.identityHashCode(this) - System.identityHashCode(o);
				}
				return i;
			}
			return 1;
		}
	}

	/** The name. */
	private String name;

	/** The required size. */
	int requiredSize;

	/** The missing. */
	long missing;

	/** The total. */
	long total;

	/** The entries. */
	Collection<LongEntry> entries = ImmutableList.of();

	/** The comparator type. */
	ComparatorType comparatorType;

	/**
	 * Instantiates a new internal ip terms facet.
	 */
	InternalIpTermsFacet() {
	}

	/**
	 * Instantiates a new internal ip terms facet.
	 *
	 * @param name the name
	 * @param comparatorType the comparator type
	 * @param requiredSize the required size
	 * @param entries the entries
	 * @param missing the missing
	 * @param total the total
	 */
	public InternalIpTermsFacet(String name, ComparatorType comparatorType, int requiredSize,
			Collection<LongEntry> entries, long missing, long total) {
		this.name = name;
		this.comparatorType = comparatorType;
		this.requiredSize = requiredSize;
		this.entries = entries;
		this.missing = missing;
		this.total = total;
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
	 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet#entries()
	 */
	@Override
	public List<LongEntry> entries() {
		if (!(entries instanceof List)) {
			entries = ImmutableList.copyOf(entries);
		}
		return (List<LongEntry>) entries;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet#getEntries()
	 */
	@Override
	public List<LongEntry> getEntries() {
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
	 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet#missingCount()
	 */
	@Override
	public long missingCount() {
		return this.missing;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet#getMissingCount()
	 */
	@Override
	public long getMissingCount() {
		return missingCount();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet#totalCount()
	 */
	@Override
	public long totalCount() {
		return this.total;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet#getTotalCount()
	 */
	@Override
	public long getTotalCount() {
		return totalCount();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet#otherCount()
	 */
	@Override
	public long otherCount() {
		long other = total;
		for (Entry entry : entries) {
			other -= entry.count();
		}
		return other;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.terms.TermsFacet#getOtherCount()
	 */
	@Override
	public long getOtherCount() {
		return otherCount();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.terms.InternalTermsFacet#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		if (facets.size() == 1) {
			return facets.get(0);
		}
		InternalIpTermsFacet first = (InternalIpTermsFacet) facets.get(0);
		TLongIntHashMap aggregated = CacheRecycler.popLongIntMap();
		long missing = 0;
		long total = 0;
		for (Facet facet : facets) {
			InternalIpTermsFacet mFacet = (InternalIpTermsFacet) facet;
			missing += mFacet.missingCount();
			total += mFacet.totalCount();
			for (LongEntry entry : mFacet.entries) {
				aggregated.adjustOrPutValue(entry.term, entry.count(), entry.count());
			}
		}

		BoundedTreeSet<LongEntry> ordered = new BoundedTreeSet<LongEntry>(first.comparatorType.comparator(),
				first.requiredSize);
		for (TLongIntIterator it = aggregated.iterator(); it.hasNext();) {
			it.advance();
			ordered.add(new LongEntry(it.key(), it.value()));
		}
		first.entries = ordered;
		first.missing = missing;
		first.total = total;

		CacheRecycler.pushLongIntMap(aggregated);

		return first;
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

		/** The Constant TOTAL. */
		static final XContentBuilderString TOTAL = new XContentBuilderString("total");

		/** The Constant OTHER. */
		static final XContentBuilderString OTHER = new XContentBuilderString("other");

		/** The Constant TERMS. */
		static final XContentBuilderString TERMS = new XContentBuilderString("terms");

		/** The Constant TERM. */
		static final XContentBuilderString TERM = new XContentBuilderString("term");

		/** The Constant COUNT. */
		static final XContentBuilderString COUNT = new XContentBuilderString("count");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.field(Fields._TYPE, TermsFacet.TYPE);
		builder.field(Fields.MISSING, missing);
		builder.field(Fields.TOTAL, total);
		builder.field(Fields.OTHER, otherCount());
		builder.startArray(Fields.TERMS);
		for (LongEntry entry : entries) {
			builder.startObject();
			builder.field(Fields.TERM, entry.term());
			builder.field(Fields.COUNT, entry.count());
			builder.endObject();
		}
		builder.endArray();
		builder.endObject();
		return builder;
	}

	/**
	 * Read terms facet.
	 *
	 * @param in the in
	 * @return the internal ip terms facet
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InternalIpTermsFacet readTermsFacet(StreamInput in) throws IOException {
		InternalIpTermsFacet facet = new InternalIpTermsFacet();
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
		total = in.readVLong();

		int size = in.readVInt();
		entries = new ArrayList<LongEntry>(size);
		for (int i = 0; i < size; i++) {
			entries.add(new LongEntry(in.readLong(), in.readVInt()));
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
		out.writeVLong(total);

		out.writeVInt(entries.size());
		for (LongEntry entry : entries) {
			out.writeLong(entry.term);
			out.writeVInt(entry.count());
		}
	}
}