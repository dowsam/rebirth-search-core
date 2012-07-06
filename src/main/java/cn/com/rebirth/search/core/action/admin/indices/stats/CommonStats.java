/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CommonStats.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.stats;

import java.io.IOException;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.index.flush.FlushStats;
import cn.com.rebirth.search.core.index.get.GetStats;
import cn.com.rebirth.search.core.index.indexing.IndexingStats;
import cn.com.rebirth.search.core.index.merge.MergeStats;
import cn.com.rebirth.search.core.index.refresh.RefreshStats;
import cn.com.rebirth.search.core.index.search.stats.SearchStats;
import cn.com.rebirth.search.core.index.shard.DocsStats;
import cn.com.rebirth.search.core.index.store.StoreStats;

/**
 * The Class CommonStats.
 *
 * @author l.xue.nong
 */
public class CommonStats implements Streamable, ToXContent {

	/** The docs. */
	@Nullable
	DocsStats docs;

	/** The store. */
	@Nullable
	StoreStats store;

	/** The indexing. */
	@Nullable
	IndexingStats indexing;

	/** The get. */
	@Nullable
	GetStats get;

	/** The search. */
	@Nullable
	SearchStats search;

	/** The merge. */
	@Nullable
	MergeStats merge;

	/** The refresh. */
	@Nullable
	RefreshStats refresh;

	/** The flush. */
	@Nullable
	FlushStats flush;

	/**
	 * Adds the.
	 *
	 * @param stats the stats
	 */
	public void add(CommonStats stats) {
		if (docs == null) {
			if (stats.docs() != null) {
				docs = new DocsStats();
				docs.add(stats.docs());
			}
		} else {
			docs.add(stats.docs());
		}
		if (store == null) {
			if (stats.store() != null) {
				store = new StoreStats();
				store.add(stats.store());
			}
		} else {
			store.add(stats.store());
		}
		if (indexing == null) {
			if (stats.indexing() != null) {
				indexing = new IndexingStats();
				indexing.add(stats.indexing());
			}
		} else {
			indexing.add(stats.indexing());
		}
		if (get == null) {
			if (stats.get() != null) {
				get = new GetStats();
				get.add(stats.get());
			}
		} else {
			get.add(stats.get());
		}
		if (search == null) {
			if (stats.search() != null) {
				search = new SearchStats();
				search.add(stats.search());
			}
		} else {
			search.add(stats.search());
		}
		if (merge == null) {
			if (stats.merge() != null) {
				merge = new MergeStats();
				merge.add(stats.merge());
			}
		} else {
			merge.add(stats.merge());
		}
		if (refresh == null) {
			if (stats.refresh() != null) {
				refresh = new RefreshStats();
				refresh.add(stats.refresh());
			}
		} else {
			refresh.add(stats.refresh());
		}
		if (flush == null) {
			if (stats.flush() != null) {
				flush = new FlushStats();
				flush.add(stats.flush());
			}
		} else {
			flush.add(stats.flush());
		}
	}

	/**
	 * Docs.
	 *
	 * @return the docs stats
	 */
	@Nullable
	public DocsStats docs() {
		return this.docs;
	}

	/**
	 * Gets the docs.
	 *
	 * @return the docs
	 */
	@Nullable
	public DocsStats getDocs() {
		return this.docs;
	}

	/**
	 * Store.
	 *
	 * @return the store stats
	 */
	@Nullable
	public StoreStats store() {
		return store;
	}

	/**
	 * Gets the store.
	 *
	 * @return the store
	 */
	@Nullable
	public StoreStats getStore() {
		return store;
	}

	/**
	 * Indexing.
	 *
	 * @return the indexing stats
	 */
	@Nullable
	public IndexingStats indexing() {
		return indexing;
	}

	/**
	 * Gets the indexing.
	 *
	 * @return the indexing
	 */
	@Nullable
	public IndexingStats getIndexing() {
		return indexing;
	}

	/**
	 * Gets the.
	 *
	 * @return the gets the stats
	 */
	@Nullable
	public GetStats get() {
		return get;
	}

	/**
	 * Gets the gets the.
	 *
	 * @return the gets the
	 */
	@Nullable
	public GetStats getGet() {
		return get;
	}

	/**
	 * Search.
	 *
	 * @return the search stats
	 */
	@Nullable
	public SearchStats search() {
		return search;
	}

	/**
	 * Gets the search.
	 *
	 * @return the search
	 */
	@Nullable
	public SearchStats getSearch() {
		return search;
	}

	/**
	 * Merge.
	 *
	 * @return the merge stats
	 */
	@Nullable
	public MergeStats merge() {
		return merge;
	}

	/**
	 * Gets the merge.
	 *
	 * @return the merge
	 */
	@Nullable
	public MergeStats getMerge() {
		return merge;
	}

	/**
	 * Refresh.
	 *
	 * @return the refresh stats
	 */
	@Nullable
	public RefreshStats refresh() {
		return refresh;
	}

	/**
	 * Gets the refresh.
	 *
	 * @return the refresh
	 */
	@Nullable
	public RefreshStats getRefresh() {
		return refresh;
	}

	/**
	 * Flush.
	 *
	 * @return the flush stats
	 */
	@Nullable
	public FlushStats flush() {
		return flush;
	}

	/**
	 * Gets the flush.
	 *
	 * @return the flush
	 */
	@Nullable
	public FlushStats getFlush() {
		return flush;
	}

	/**
	 * Read common stats.
	 *
	 * @param in the in
	 * @return the common stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static CommonStats readCommonStats(StreamInput in) throws IOException {
		CommonStats stats = new CommonStats();
		stats.readFrom(in);
		return stats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		if (in.readBoolean()) {
			docs = DocsStats.readDocStats(in);
		}
		if (in.readBoolean()) {
			store = StoreStats.readStoreStats(in);
		}
		if (in.readBoolean()) {
			indexing = IndexingStats.readIndexingStats(in);
		}
		if (in.readBoolean()) {
			get = GetStats.readGetStats(in);
		}
		if (in.readBoolean()) {
			search = SearchStats.readSearchStats(in);
		}
		if (in.readBoolean()) {
			merge = MergeStats.readMergeStats(in);
		}
		if (in.readBoolean()) {
			refresh = RefreshStats.readRefreshStats(in);
		}
		if (in.readBoolean()) {
			flush = FlushStats.readFlushStats(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		if (docs == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			docs.writeTo(out);
		}
		if (store == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			store.writeTo(out);
		}
		if (indexing == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			indexing.writeTo(out);
		}
		if (get == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			get.writeTo(out);
		}
		if (search == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			search.writeTo(out);
		}
		if (merge == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			merge.writeTo(out);
		}
		if (refresh == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			refresh.writeTo(out);
		}
		if (flush == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			flush.writeTo(out);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (docs != null) {
			docs.toXContent(builder, params);
		}
		if (store != null) {
			store.toXContent(builder, params);
		}
		if (indexing != null) {
			indexing.toXContent(builder, params);
		}
		if (get != null) {
			get.toXContent(builder, params);
		}
		if (search != null) {
			search.toXContent(builder, params);
		}
		if (merge != null) {
			merge.toXContent(builder, params);
		}
		if (refresh != null) {
			refresh.toXContent(builder, params);
		}
		if (flush != null) {
			flush.toXContent(builder, params);
		}
		return builder;
	}
}
