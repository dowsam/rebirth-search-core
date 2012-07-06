/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalSearchHits.java 2012-7-6 14:30:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.internal;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.search.SearchHit;
import cn.com.rebirth.search.core.search.SearchHits;
import cn.com.rebirth.search.core.search.SearchShardTarget;
import cn.com.rebirth.search.core.search.internal.InternalSearchHits.StreamContext;

import com.google.common.collect.Iterators;

/**
 * The Class InternalSearchHits.
 *
 * @author l.xue.nong
 */
public class InternalSearchHits implements SearchHits {

	/**
	 * The Class StreamContext.
	 *
	 * @author l.xue.nong
	 */
	public static class StreamContext {

		/**
		 * The Enum ShardTargetType.
		 *
		 * @author l.xue.nong
		 */
		public static enum ShardTargetType {

			/** The stream. */
			STREAM,

			/** The lookup. */
			LOOKUP,

			/** The no stream. */
			NO_STREAM
		}

		/** The shard handle lookup. */
		private IdentityHashMap<SearchShardTarget, Integer> shardHandleLookup = new IdentityHashMap<SearchShardTarget, Integer>();

		/** The handle shard lookup. */
		private TIntObjectHashMap<SearchShardTarget> handleShardLookup = new TIntObjectHashMap<SearchShardTarget>();

		/** The stream shard target. */
		private ShardTargetType streamShardTarget = ShardTargetType.STREAM;

		/**
		 * Reset.
		 *
		 * @return the stream context
		 */
		public StreamContext reset() {
			shardHandleLookup.clear();
			handleShardLookup.clear();
			streamShardTarget = ShardTargetType.STREAM;
			return this;
		}

		/**
		 * Shard handle lookup.
		 *
		 * @return the identity hash map
		 */
		public IdentityHashMap<SearchShardTarget, Integer> shardHandleLookup() {
			return shardHandleLookup;
		}

		/**
		 * Handle shard lookup.
		 *
		 * @return the t int object hash map
		 */
		public TIntObjectHashMap<SearchShardTarget> handleShardLookup() {
			return handleShardLookup;
		}

		/**
		 * Stream shard target.
		 *
		 * @return the shard target type
		 */
		public ShardTargetType streamShardTarget() {
			return streamShardTarget;
		}

		/**
		 * Stream shard target.
		 *
		 * @param streamShardTarget the stream shard target
		 * @return the stream context
		 */
		public StreamContext streamShardTarget(ShardTargetType streamShardTarget) {
			this.streamShardTarget = streamShardTarget;
			return this;
		}
	}

	/** The Constant cache. */
	private static final ThreadLocal<ThreadLocals.CleanableValue<StreamContext>> cache = new ThreadLocal<ThreadLocals.CleanableValue<StreamContext>>() {
		@Override
		protected ThreadLocals.CleanableValue<StreamContext> initialValue() {
			return new ThreadLocals.CleanableValue<StreamContext>(new StreamContext());
		}
	};

	/**
	 * Stream context.
	 *
	 * @return the stream context
	 */
	public static StreamContext streamContext() {
		return cache.get().get().reset();
	}

	/** The Constant EMPTY. */
	public static final InternalSearchHit[] EMPTY = new InternalSearchHit[0];

	/** The hits. */
	private InternalSearchHit[] hits;

	/** The total hits. */
	public long totalHits;

	/** The max score. */
	private float maxScore;

	/**
	 * Instantiates a new internal search hits.
	 */
	InternalSearchHits() {

	}

	/**
	 * Instantiates a new internal search hits.
	 *
	 * @param hits the hits
	 * @param totalHits the total hits
	 * @param maxScore the max score
	 */
	public InternalSearchHits(InternalSearchHit[] hits, long totalHits, float maxScore) {
		this.hits = hits;
		this.totalHits = totalHits;
		this.maxScore = maxScore;
	}

	/**
	 * Shard target.
	 *
	 * @param shardTarget the shard target
	 */
	public void shardTarget(SearchShardTarget shardTarget) {
		for (InternalSearchHit hit : hits) {
			hit.shardTarget(shardTarget);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHits#totalHits()
	 */
	public long totalHits() {
		return totalHits;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHits#getTotalHits()
	 */
	@Override
	public long getTotalHits() {
		return totalHits();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHits#maxScore()
	 */
	@Override
	public float maxScore() {
		return this.maxScore;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHits#getMaxScore()
	 */
	@Override
	public float getMaxScore() {
		return maxScore();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHits#hits()
	 */
	public SearchHit[] hits() {
		return this.hits;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHits#getAt(int)
	 */
	@Override
	public SearchHit getAt(int position) {
		return hits[position];
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHits#getHits()
	 */
	@Override
	public SearchHit[] getHits() {
		return hits();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<SearchHit> iterator() {
		return Iterators.forArray(hits());
	}

	/**
	 * Internal hits.
	 *
	 * @return the internal search hit[]
	 */
	public InternalSearchHit[] internalHits() {
		return this.hits;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant HITS. */
		static final XContentBuilderString HITS = new XContentBuilderString("hits");

		/** The Constant TOTAL. */
		static final XContentBuilderString TOTAL = new XContentBuilderString("total");

		/** The Constant MAX_SCORE. */
		static final XContentBuilderString MAX_SCORE = new XContentBuilderString("max_score");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.HITS);
		builder.field(Fields.TOTAL, totalHits);
		if (Float.isNaN(maxScore)) {
			builder.nullField(Fields.MAX_SCORE);
		} else {
			builder.field(Fields.MAX_SCORE, maxScore);
		}
		builder.field(Fields.HITS);
		builder.startArray();
		for (SearchHit hit : hits) {
			hit.toXContent(builder, params);
		}
		builder.endArray();
		builder.endObject();
		return builder;
	}

	/**
	 * Read search hits.
	 *
	 * @param in the in
	 * @param context the context
	 * @return the internal search hits
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InternalSearchHits readSearchHits(StreamInput in, StreamContext context) throws IOException {
		InternalSearchHits hits = new InternalSearchHits();
		hits.readFrom(in, context);
		return hits;
	}

	/**
	 * Read search hits.
	 *
	 * @param in the in
	 * @return the internal search hits
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InternalSearchHits readSearchHits(StreamInput in) throws IOException {
		InternalSearchHits hits = new InternalSearchHits();
		hits.readFrom(in);
		return hits;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		readFrom(in, streamContext().streamShardTarget(StreamContext.ShardTargetType.LOOKUP));
	}

	/**
	 * Read from.
	 *
	 * @param in the in
	 * @param context the context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void readFrom(StreamInput in, StreamContext context) throws IOException {
		totalHits = in.readVLong();
		maxScore = in.readFloat();
		int size = in.readVInt();
		if (size == 0) {
			hits = EMPTY;
		} else {
			if (context.streamShardTarget() == StreamContext.ShardTargetType.LOOKUP) {

				int lookupSize = in.readVInt();
				for (int i = 0; i < lookupSize; i++) {
					context.handleShardLookup().put(in.readVInt(), SearchShardTarget.readSearchShardTarget(in));
				}
			}

			hits = new InternalSearchHit[size];
			for (int i = 0; i < hits.length; i++) {
				hits[i] = InternalSearchHit.readSearchHit(in, context);
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		writeTo(out, streamContext().streamShardTarget(StreamContext.ShardTargetType.LOOKUP));
	}

	/**
	 * Write to.
	 *
	 * @param out the out
	 * @param context the context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeTo(StreamOutput out, StreamContext context) throws IOException {
		out.writeVLong(totalHits);
		out.writeFloat(maxScore);
		out.writeVInt(hits.length);
		if (hits.length > 0) {
			if (context.streamShardTarget() == StreamContext.ShardTargetType.LOOKUP) {

				int counter = 1;
				for (InternalSearchHit hit : hits) {
					if (hit.shard() != null) {
						Integer handle = context.shardHandleLookup().get(hit.shard());
						if (handle == null) {
							context.shardHandleLookup().put(hit.shard(), counter++);
						}
					}
				}
				out.writeVInt(context.shardHandleLookup().size());
				if (!context.shardHandleLookup().isEmpty()) {
					for (Map.Entry<SearchShardTarget, Integer> entry : context.shardHandleLookup().entrySet()) {
						out.writeVInt(entry.getValue());
						entry.getKey().writeTo(out);
					}
				}
			}

			for (InternalSearchHit hit : hits) {
				hit.writeTo(out, context);
			}
		}
	}
}