/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchStats.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.stats;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;

/**
 * The Class SearchStats.
 *
 * @author l.xue.nong
 */
public class SearchStats implements Streamable, ToXContent {

	/**
	 * The Class Stats.
	 *
	 * @author l.xue.nong
	 */
	public static class Stats implements Streamable, ToXContent {

		/** The query count. */
		private long queryCount;

		/** The query time in millis. */
		private long queryTimeInMillis;

		/** The query current. */
		private long queryCurrent;

		/** The fetch count. */
		private long fetchCount;

		/** The fetch time in millis. */
		private long fetchTimeInMillis;

		/** The fetch current. */
		private long fetchCurrent;

		/**
		 * Instantiates a new stats.
		 */
		Stats() {

		}

		/**
		 * Instantiates a new stats.
		 *
		 * @param queryCount the query count
		 * @param queryTimeInMillis the query time in millis
		 * @param queryCurrent the query current
		 * @param fetchCount the fetch count
		 * @param fetchTimeInMillis the fetch time in millis
		 * @param fetchCurrent the fetch current
		 */
		public Stats(long queryCount, long queryTimeInMillis, long queryCurrent, long fetchCount,
				long fetchTimeInMillis, long fetchCurrent) {
			this.queryCount = queryCount;
			this.queryTimeInMillis = queryTimeInMillis;
			this.queryCurrent = queryCurrent;
			this.fetchCount = fetchCount;
			this.fetchTimeInMillis = fetchTimeInMillis;
			this.fetchCurrent = fetchCurrent;
		}

		/**
		 * Adds the.
		 *
		 * @param stats the stats
		 */
		public void add(Stats stats) {
			queryCount += stats.queryCount;
			queryTimeInMillis += stats.queryTimeInMillis;
			queryCurrent += stats.queryCurrent;

			fetchCount += stats.fetchCount;
			fetchTimeInMillis += stats.fetchTimeInMillis;
			fetchCurrent += stats.fetchCurrent;
		}

		/**
		 * Query count.
		 *
		 * @return the long
		 */
		public long queryCount() {
			return queryCount;
		}

		/**
		 * Gets the query count.
		 *
		 * @return the query count
		 */
		public long getQueryCount() {
			return queryCount;
		}

		/**
		 * Query time.
		 *
		 * @return the time value
		 */
		public TimeValue queryTime() {
			return new TimeValue(queryTimeInMillis);
		}

		/**
		 * Query time in millis.
		 *
		 * @return the long
		 */
		public long queryTimeInMillis() {
			return queryTimeInMillis;
		}

		/**
		 * Gets the query time in millis.
		 *
		 * @return the query time in millis
		 */
		public long getQueryTimeInMillis() {
			return queryTimeInMillis;
		}

		/**
		 * Query current.
		 *
		 * @return the long
		 */
		public long queryCurrent() {
			return queryCurrent;
		}

		/**
		 * Gets the query current.
		 *
		 * @return the query current
		 */
		public long getQueryCurrent() {
			return queryCurrent;
		}

		/**
		 * Fetch count.
		 *
		 * @return the long
		 */
		public long fetchCount() {
			return fetchCount;
		}

		/**
		 * Gets the fetch count.
		 *
		 * @return the fetch count
		 */
		public long getFetchCount() {
			return fetchCount;
		}

		/**
		 * Fetch time.
		 *
		 * @return the time value
		 */
		public TimeValue fetchTime() {
			return new TimeValue(fetchTimeInMillis);
		}

		/**
		 * Fetch time in millis.
		 *
		 * @return the long
		 */
		public long fetchTimeInMillis() {
			return fetchTimeInMillis;
		}

		/**
		 * Gets the fetch time in millis.
		 *
		 * @return the fetch time in millis
		 */
		public long getFetchTimeInMillis() {
			return fetchTimeInMillis;
		}

		/**
		 * Fetch current.
		 *
		 * @return the long
		 */
		public long fetchCurrent() {
			return fetchCurrent;
		}

		/**
		 * Gets the fetch current.
		 *
		 * @return the fetch current
		 */
		public long getFetchCurrent() {
			return fetchCurrent;
		}

		/**
		 * Read stats.
		 *
		 * @param in the in
		 * @return the stats
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static Stats readStats(StreamInput in) throws IOException {
			Stats stats = new Stats();
			stats.readFrom(in);
			return stats;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			queryCount = in.readVLong();
			queryTimeInMillis = in.readVLong();
			queryCurrent = in.readVLong();

			fetchCount = in.readVLong();
			fetchTimeInMillis = in.readVLong();
			fetchCurrent = in.readVLong();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeVLong(queryCount);
			out.writeVLong(queryTimeInMillis);
			out.writeVLong(queryCurrent);

			out.writeVLong(fetchCount);
			out.writeVLong(fetchTimeInMillis);
			out.writeVLong(fetchCurrent);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
		 */
		@Override
		public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
			builder.field(Fields.QUERY_TOTAL, queryCount);
			builder.field(Fields.QUERY_TIME, queryTime().toString());
			builder.field(Fields.QUERY_TIME_IN_MILLIS, queryTimeInMillis);
			builder.field(Fields.QUERY_CURRENT, queryCurrent);

			builder.field(Fields.FETCH_TOTAL, fetchCount);
			builder.field(Fields.FETCH_TIME, fetchTime().toString());
			builder.field(Fields.FETCH_TIME_IN_MILLIS, fetchTimeInMillis);
			builder.field(Fields.FETCH_CURRENT, fetchCurrent);

			return builder;
		}
	}

	/** The total stats. */
	private Stats totalStats;

	/** The group stats. */
	@Nullable
	Map<String, Stats> groupStats;

	/**
	 * Instantiates a new search stats.
	 */
	public SearchStats() {
		totalStats = new Stats();
	}

	/**
	 * Instantiates a new search stats.
	 *
	 * @param totalStats the total stats
	 * @param groupStats the group stats
	 */
	public SearchStats(Stats totalStats, @Nullable Map<String, Stats> groupStats) {
		this.totalStats = totalStats;
		this.groupStats = groupStats;
	}

	/**
	 * Adds the.
	 *
	 * @param searchStats the search stats
	 */
	public void add(SearchStats searchStats) {
		add(searchStats, true);
	}

	/**
	 * Adds the.
	 *
	 * @param searchStats the search stats
	 * @param includeTypes the include types
	 */
	public void add(SearchStats searchStats, boolean includeTypes) {
		if (searchStats == null) {
			return;
		}
		totalStats.add(searchStats.totalStats);
		if (includeTypes && searchStats.groupStats != null && !searchStats.groupStats.isEmpty()) {
			if (groupStats == null) {
				groupStats = new HashMap<String, Stats>(searchStats.groupStats.size());
			}
			for (Map.Entry<String, Stats> entry : searchStats.groupStats.entrySet()) {
				Stats stats = groupStats.get(entry.getKey());
				if (stats == null) {
					groupStats.put(entry.getKey(), entry.getValue());
				} else {
					stats.add(entry.getValue());
				}
			}
		}
	}

	/**
	 * Total.
	 *
	 * @return the stats
	 */
	public Stats total() {
		return this.totalStats;
	}

	/**
	 * Group stats.
	 *
	 * @return the map
	 */
	@Nullable
	public Map<String, Stats> groupStats() {
		return this.groupStats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
		builder.startObject(Fields.SEARCH);
		totalStats.toXContent(builder, params);
		if (groupStats != null && !groupStats.isEmpty()) {
			builder.startObject(Fields.GROUPS);
			for (Map.Entry<String, Stats> entry : groupStats.entrySet()) {
				builder.startObject(entry.getKey(), XContentBuilder.FieldCaseConversion.NONE);
				entry.getValue().toXContent(builder, params);
				builder.endObject();
			}
			builder.endObject();
		}
		builder.endObject();
		return builder;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant SEARCH. */
		static final XContentBuilderString SEARCH = new XContentBuilderString("search");

		/** The Constant GROUPS. */
		static final XContentBuilderString GROUPS = new XContentBuilderString("groups");

		/** The Constant QUERY_TOTAL. */
		static final XContentBuilderString QUERY_TOTAL = new XContentBuilderString("query_total");

		/** The Constant QUERY_TIME. */
		static final XContentBuilderString QUERY_TIME = new XContentBuilderString("query_time");

		/** The Constant QUERY_TIME_IN_MILLIS. */
		static final XContentBuilderString QUERY_TIME_IN_MILLIS = new XContentBuilderString("query_time_in_millis");

		/** The Constant QUERY_CURRENT. */
		static final XContentBuilderString QUERY_CURRENT = new XContentBuilderString("query_current");

		/** The Constant FETCH_TOTAL. */
		static final XContentBuilderString FETCH_TOTAL = new XContentBuilderString("fetch_total");

		/** The Constant FETCH_TIME. */
		static final XContentBuilderString FETCH_TIME = new XContentBuilderString("fetch_time");

		/** The Constant FETCH_TIME_IN_MILLIS. */
		static final XContentBuilderString FETCH_TIME_IN_MILLIS = new XContentBuilderString("fetch_time_in_millis");

		/** The Constant FETCH_CURRENT. */
		static final XContentBuilderString FETCH_CURRENT = new XContentBuilderString("fetch_current");
	}

	/**
	 * Read search stats.
	 *
	 * @param in the in
	 * @return the search stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static SearchStats readSearchStats(StreamInput in) throws IOException {
		SearchStats searchStats = new SearchStats();
		searchStats.readFrom(in);
		return searchStats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		totalStats = Stats.readStats(in);
		if (in.readBoolean()) {
			int size = in.readVInt();
			groupStats = new HashMap<String, Stats>(size);
			for (int i = 0; i < size; i++) {
				groupStats.put(in.readUTF(), Stats.readStats(in));
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		totalStats.writeTo(out);
		if (groupStats == null || groupStats.isEmpty()) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeVInt(groupStats.size());
			for (Map.Entry<String, Stats> entry : groupStats.entrySet()) {
				out.writeUTF(entry.getKey());
				entry.getValue().writeTo(out);
			}
		}
	}
}
