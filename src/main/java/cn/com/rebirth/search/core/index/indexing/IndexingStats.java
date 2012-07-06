/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexingStats.java 2012-7-6 14:28:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.indexing;

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
 * The Class IndexingStats.
 *
 * @author l.xue.nong
 */
public class IndexingStats implements Streamable, ToXContent {

	/**
	 * The Class Stats.
	 *
	 * @author l.xue.nong
	 */
	public static class Stats implements Streamable, ToXContent {

		/** The index count. */
		private long indexCount;

		/** The index time in millis. */
		private long indexTimeInMillis;

		/** The index current. */
		private long indexCurrent;

		/** The delete count. */
		private long deleteCount;

		/** The delete time in millis. */
		private long deleteTimeInMillis;

		/** The delete current. */
		private long deleteCurrent;

		/**
		 * Instantiates a new stats.
		 */
		Stats() {

		}

		/**
		 * Instantiates a new stats.
		 *
		 * @param indexCount the index count
		 * @param indexTimeInMillis the index time in millis
		 * @param indexCurrent the index current
		 * @param deleteCount the delete count
		 * @param deleteTimeInMillis the delete time in millis
		 * @param deleteCurrent the delete current
		 */
		public Stats(long indexCount, long indexTimeInMillis, long indexCurrent, long deleteCount,
				long deleteTimeInMillis, long deleteCurrent) {
			this.indexCount = indexCount;
			this.indexTimeInMillis = indexTimeInMillis;
			this.indexCurrent = indexCurrent;
			this.deleteCount = deleteCount;
			this.deleteTimeInMillis = deleteTimeInMillis;
			this.deleteCurrent = deleteCurrent;
		}

		/**
		 * Adds the.
		 *
		 * @param stats the stats
		 */
		public void add(Stats stats) {
			indexCount += stats.indexCount;
			indexTimeInMillis += stats.indexTimeInMillis;
			indexCurrent += stats.indexCurrent;

			deleteCount += stats.deleteCount;
			deleteTimeInMillis += stats.deleteTimeInMillis;
			deleteCurrent += stats.deleteCurrent;
		}

		/**
		 * Index count.
		 *
		 * @return the long
		 */
		public long indexCount() {
			return indexCount;
		}

		/**
		 * Gets the index count.
		 *
		 * @return the index count
		 */
		public long getIndexCount() {
			return indexCount;
		}

		/**
		 * Index time.
		 *
		 * @return the time value
		 */
		public TimeValue indexTime() {
			return new TimeValue(indexTimeInMillis);
		}

		/**
		 * Index time in millis.
		 *
		 * @return the long
		 */
		public long indexTimeInMillis() {
			return indexTimeInMillis;
		}

		/**
		 * Gets the index time in millis.
		 *
		 * @return the index time in millis
		 */
		public long getIndexTimeInMillis() {
			return indexTimeInMillis;
		}

		/**
		 * Index current.
		 *
		 * @return the long
		 */
		public long indexCurrent() {
			return indexCurrent;
		}

		/**
		 * Gets the index current.
		 *
		 * @return the index current
		 */
		public long getIndexCurrent() {
			return indexCurrent;
		}

		/**
		 * Delete count.
		 *
		 * @return the long
		 */
		public long deleteCount() {
			return deleteCount;
		}

		/**
		 * Gets the delete count.
		 *
		 * @return the delete count
		 */
		public long getDeleteCount() {
			return deleteCount;
		}

		/**
		 * Delete time.
		 *
		 * @return the time value
		 */
		public TimeValue deleteTime() {
			return new TimeValue(deleteTimeInMillis);
		}

		/**
		 * Delete time in millis.
		 *
		 * @return the long
		 */
		public long deleteTimeInMillis() {
			return deleteTimeInMillis;
		}

		/**
		 * Gets the delete time in millis.
		 *
		 * @return the delete time in millis
		 */
		public long getDeleteTimeInMillis() {
			return deleteTimeInMillis;
		}

		/**
		 * Delete current.
		 *
		 * @return the long
		 */
		public long deleteCurrent() {
			return deleteCurrent;
		}

		/**
		 * Gets the delete current.
		 *
		 * @return the delete current
		 */
		public long getDeleteCurrent() {
			return deleteCurrent;
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
			indexCount = in.readVLong();
			indexTimeInMillis = in.readVLong();
			indexCurrent = in.readVLong();

			deleteCount = in.readVLong();
			deleteTimeInMillis = in.readVLong();
			deleteCurrent = in.readVLong();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeVLong(indexCount);
			out.writeVLong(indexTimeInMillis);
			out.writeVLong(indexCurrent);

			out.writeVLong(deleteCount);
			out.writeVLong(deleteTimeInMillis);
			out.writeVLong(deleteCurrent);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
		 */
		@Override
		public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
			builder.field(Fields.INDEX_TOTAL, indexCount);
			builder.field(Fields.INDEX_TIME, indexTime().toString());
			builder.field(Fields.INDEX_TIME_IN_MILLIS, indexTimeInMillis);
			builder.field(Fields.INDEX_CURRENT, indexCurrent);

			builder.field(Fields.DELETE_TOTAL, deleteCount);
			builder.field(Fields.DELETE_TIME, deleteTime().toString());
			builder.field(Fields.DELETE_TIME_IN_MILLIS, deleteTimeInMillis);
			builder.field(Fields.DELETE_CURRENT, deleteCurrent);

			return builder;
		}
	}

	/** The total stats. */
	private Stats totalStats;

	/** The type stats. */
	@Nullable
	private Map<String, Stats> typeStats;

	/**
	 * Instantiates a new indexing stats.
	 */
	public IndexingStats() {
		totalStats = new Stats();
	}

	/**
	 * Instantiates a new indexing stats.
	 *
	 * @param totalStats the total stats
	 * @param typeStats the type stats
	 */
	public IndexingStats(Stats totalStats, @Nullable Map<String, Stats> typeStats) {
		this.totalStats = totalStats;
		this.typeStats = typeStats;
	}

	/**
	 * Adds the.
	 *
	 * @param indexingStats the indexing stats
	 */
	public void add(IndexingStats indexingStats) {
		add(indexingStats, true);
	}

	/**
	 * Adds the.
	 *
	 * @param indexingStats the indexing stats
	 * @param includeTypes the include types
	 */
	public void add(IndexingStats indexingStats, boolean includeTypes) {
		if (indexingStats == null) {
			return;
		}
		totalStats.add(indexingStats.totalStats);
		if (includeTypes && indexingStats.typeStats != null && !indexingStats.typeStats.isEmpty()) {
			if (typeStats == null) {
				typeStats = new HashMap<String, Stats>(indexingStats.typeStats.size());
			}
			for (Map.Entry<String, Stats> entry : indexingStats.typeStats.entrySet()) {
				Stats stats = typeStats.get(entry.getKey());
				if (stats == null) {
					typeStats.put(entry.getKey(), entry.getValue());
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
	 * Type stats.
	 *
	 * @return the map
	 */
	@Nullable
	public Map<String, Stats> typeStats() {
		return this.typeStats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
		builder.startObject(Fields.INDEXING);
		totalStats.toXContent(builder, params);
		if (typeStats != null && !typeStats.isEmpty()) {
			builder.startObject(Fields.TYPES);
			for (Map.Entry<String, Stats> entry : typeStats.entrySet()) {
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

		/** The Constant INDEXING. */
		static final XContentBuilderString INDEXING = new XContentBuilderString("indexing");

		/** The Constant TYPES. */
		static final XContentBuilderString TYPES = new XContentBuilderString("types");

		/** The Constant INDEX_TOTAL. */
		static final XContentBuilderString INDEX_TOTAL = new XContentBuilderString("index_total");

		/** The Constant INDEX_TIME. */
		static final XContentBuilderString INDEX_TIME = new XContentBuilderString("index_time");

		/** The Constant INDEX_TIME_IN_MILLIS. */
		static final XContentBuilderString INDEX_TIME_IN_MILLIS = new XContentBuilderString("index_time_in_millis");

		/** The Constant INDEX_CURRENT. */
		static final XContentBuilderString INDEX_CURRENT = new XContentBuilderString("index_current");

		/** The Constant DELETE_TOTAL. */
		static final XContentBuilderString DELETE_TOTAL = new XContentBuilderString("delete_total");

		/** The Constant DELETE_TIME. */
		static final XContentBuilderString DELETE_TIME = new XContentBuilderString("delete_time");

		/** The Constant DELETE_TIME_IN_MILLIS. */
		static final XContentBuilderString DELETE_TIME_IN_MILLIS = new XContentBuilderString("delete_time_in_millis");

		/** The Constant DELETE_CURRENT. */
		static final XContentBuilderString DELETE_CURRENT = new XContentBuilderString("delete_current");
	}

	/**
	 * Read indexing stats.
	 *
	 * @param in the in
	 * @return the indexing stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static IndexingStats readIndexingStats(StreamInput in) throws IOException {
		IndexingStats indexingStats = new IndexingStats();
		indexingStats.readFrom(in);
		return indexingStats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		totalStats = Stats.readStats(in);
		if (in.readBoolean()) {
			int size = in.readVInt();
			typeStats = new HashMap<String, Stats>(size);
			for (int i = 0; i < size; i++) {
				typeStats.put(in.readUTF(), Stats.readStats(in));
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		totalStats.writeTo(out);
		if (typeStats == null || typeStats.isEmpty()) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeVInt(typeStats.size());
			for (Map.Entry<String, Stats> entry : typeStats.entrySet()) {
				out.writeUTF(entry.getKey());
				entry.getValue().writeTo(out);
			}
		}
	}
}
