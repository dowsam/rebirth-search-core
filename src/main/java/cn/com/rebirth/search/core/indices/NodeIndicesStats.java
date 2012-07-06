/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeIndicesStats.java 2012-7-6 14:29:09 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.index.cache.CacheStats;
import cn.com.rebirth.search.core.index.flush.FlushStats;
import cn.com.rebirth.search.core.index.get.GetStats;
import cn.com.rebirth.search.core.index.indexing.IndexingStats;
import cn.com.rebirth.search.core.index.merge.MergeStats;
import cn.com.rebirth.search.core.index.refresh.RefreshStats;
import cn.com.rebirth.search.core.index.search.stats.SearchStats;
import cn.com.rebirth.search.core.index.shard.DocsStats;
import cn.com.rebirth.search.core.index.store.StoreStats;

/**
 * The Class NodeIndicesStats.
 *
 * @author l.xue.nong
 */
public class NodeIndicesStats implements Streamable, Serializable, ToXContent {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7450965663704502853L;

	/** The store stats. */
	private StoreStats storeStats;

	/** The docs stats. */
	private DocsStats docsStats;

	/** The indexing stats. */
	private IndexingStats indexingStats;

	/** The get stats. */
	private GetStats getStats;

	/** The search stats. */
	private SearchStats searchStats;

	/** The cache stats. */
	private CacheStats cacheStats;

	/** The merge stats. */
	private MergeStats mergeStats;

	/** The refresh stats. */
	private RefreshStats refreshStats;

	/** The flush stats. */
	private FlushStats flushStats;

	/**
	 * Instantiates a new node indices stats.
	 */
	NodeIndicesStats() {
	}

	/**
	 * Instantiates a new node indices stats.
	 *
	 * @param storeStats the store stats
	 * @param docsStats the docs stats
	 * @param indexingStats the indexing stats
	 * @param getStats the get stats
	 * @param searchStats the search stats
	 * @param cacheStats the cache stats
	 * @param mergeStats the merge stats
	 * @param refreshStats the refresh stats
	 * @param flushStats the flush stats
	 */
	public NodeIndicesStats(StoreStats storeStats, DocsStats docsStats, IndexingStats indexingStats, GetStats getStats,
			SearchStats searchStats, CacheStats cacheStats, MergeStats mergeStats, RefreshStats refreshStats,
			FlushStats flushStats) {
		this.storeStats = storeStats;
		this.docsStats = docsStats;
		this.indexingStats = indexingStats;
		this.getStats = getStats;
		this.searchStats = searchStats;
		this.cacheStats = cacheStats;
		this.mergeStats = mergeStats;
		this.refreshStats = refreshStats;
		this.flushStats = flushStats;
	}

	/**
	 * Store.
	 *
	 * @return the store stats
	 */
	public StoreStats store() {
		return this.storeStats;
	}

	/**
	 * Gets the store.
	 *
	 * @return the store
	 */
	public StoreStats getStore() {
		return storeStats;
	}

	/**
	 * Docs.
	 *
	 * @return the docs stats
	 */
	public DocsStats docs() {
		return this.docsStats;
	}

	/**
	 * Gets the docs.
	 *
	 * @return the docs
	 */
	public DocsStats getDocs() {
		return this.docsStats;
	}

	/**
	 * Indexing.
	 *
	 * @return the indexing stats
	 */
	public IndexingStats indexing() {
		return indexingStats;
	}

	/**
	 * Gets the indexing.
	 *
	 * @return the indexing
	 */
	public IndexingStats getIndexing() {
		return indexing();
	}

	/**
	 * Gets the.
	 *
	 * @return the gets the stats
	 */
	public GetStats get() {
		return this.getStats;
	}

	/**
	 * Gets the gets the.
	 *
	 * @return the gets the
	 */
	public GetStats getGet() {
		return this.getStats;
	}

	/**
	 * Search.
	 *
	 * @return the search stats
	 */
	public SearchStats search() {
		return this.searchStats;
	}

	/**
	 * Gets the search.
	 *
	 * @return the search
	 */
	public SearchStats getSearch() {
		return this.searchStats;
	}

	/**
	 * Cache.
	 *
	 * @return the cache stats
	 */
	public CacheStats cache() {
		return this.cacheStats;
	}

	/**
	 * Gets the cache.
	 *
	 * @return the cache
	 */
	public CacheStats getCache() {
		return this.cache();
	}

	/**
	 * Merge.
	 *
	 * @return the merge stats
	 */
	public MergeStats merge() {
		return this.mergeStats;
	}

	/**
	 * Gets the merge.
	 *
	 * @return the merge
	 */
	public MergeStats getMerge() {
		return this.mergeStats;
	}

	/**
	 * Refresh.
	 *
	 * @return the refresh stats
	 */
	public RefreshStats refresh() {
		return this.refreshStats;
	}

	/**
	 * Gets the refresh.
	 *
	 * @return the refresh
	 */
	public RefreshStats getRefresh() {
		return this.refresh();
	}

	/**
	 * Flush.
	 *
	 * @return the flush stats
	 */
	public FlushStats flush() {
		return this.flushStats;
	}

	/**
	 * Gets the flush.
	 *
	 * @return the flush
	 */
	public FlushStats getFlush() {
		return this.flushStats;
	}

	/**
	 * Read indices stats.
	 *
	 * @param in the in
	 * @return the node indices stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static NodeIndicesStats readIndicesStats(StreamInput in) throws IOException {
		NodeIndicesStats stats = new NodeIndicesStats();
		stats.readFrom(in);
		return stats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		storeStats = StoreStats.readStoreStats(in);
		docsStats = DocsStats.readDocStats(in);
		indexingStats = IndexingStats.readIndexingStats(in);
		getStats = GetStats.readGetStats(in);
		searchStats = SearchStats.readSearchStats(in);
		cacheStats = CacheStats.readCacheStats(in);
		mergeStats = MergeStats.readMergeStats(in);
		refreshStats = RefreshStats.readRefreshStats(in);
		flushStats = FlushStats.readFlushStats(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		storeStats.writeTo(out);
		docsStats.writeTo(out);
		indexingStats.writeTo(out);
		getStats.writeTo(out);
		searchStats.writeTo(out);
		cacheStats.writeTo(out);
		mergeStats.writeTo(out);
		refreshStats.writeTo(out);
		flushStats.writeTo(out);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.INDICES);

		storeStats.toXContent(builder, params);
		docsStats.toXContent(builder, params);
		indexingStats.toXContent(builder, params);
		getStats.toXContent(builder, params);
		searchStats.toXContent(builder, params);
		cacheStats.toXContent(builder, params);
		mergeStats.toXContent(builder, params);
		refreshStats.toXContent(builder, params);
		flushStats.toXContent(builder, params);

		builder.endObject();
		return builder;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant INDICES. */
		static final XContentBuilderString INDICES = new XContentBuilderString("indices");
	}
}
