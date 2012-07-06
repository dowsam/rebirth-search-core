/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexStatus.java 2012-3-29 15:01:25 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.status;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.core.index.flush.FlushStats;
import cn.com.rebirth.search.core.index.merge.MergeStats;
import cn.com.rebirth.search.core.index.refresh.RefreshStats;

import com.google.common.collect.Maps;


/**
 * The Class IndexStatus.
 *
 * @author l.xue.nong
 */
public class IndexStatus implements Iterable<IndexShardStatus> {

	
	/** The index. */
	private final String index;

	
	/** The index shards. */
	private final Map<Integer, IndexShardStatus> indexShards;

	
	/**
	 * Instantiates a new index status.
	 *
	 * @param index the index
	 * @param shards the shards
	 */
	IndexStatus(String index, ShardStatus[] shards) {
		this.index = index;

		Map<Integer, List<ShardStatus>> tmpIndexShards = Maps.newHashMap();
		for (ShardStatus shard : shards) {
			List<ShardStatus> lst = tmpIndexShards.get(shard.shardRouting().id());
			if (lst == null) {
				lst = newArrayList();
				tmpIndexShards.put(shard.shardRouting().id(), lst);
			}
			lst.add(shard);
		}
		indexShards = Maps.newHashMap();
		for (Map.Entry<Integer, List<ShardStatus>> entry : tmpIndexShards.entrySet()) {
			indexShards.put(entry.getKey(), new IndexShardStatus(entry.getValue().get(0).shardRouting().shardId(),
					entry.getValue().toArray(new ShardStatus[entry.getValue().size()])));
		}
	}

	
	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return this.index;
	}

	
	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index();
	}

	
	/**
	 * Shards.
	 *
	 * @return the map
	 */
	public Map<Integer, IndexShardStatus> shards() {
		return this.indexShards;
	}

	
	/**
	 * Gets the shards.
	 *
	 * @return the shards
	 */
	public Map<Integer, IndexShardStatus> getShards() {
		return shards();
	}

	
	/**
	 * Primary store size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue primaryStoreSize() {
		long bytes = -1;
		for (IndexShardStatus shard : this) {
			if (shard.primaryStoreSize() != null) {
				if (bytes == -1) {
					bytes = 0;
				}
				bytes += shard.primaryStoreSize().bytes();
			}
		}
		if (bytes == -1) {
			return null;
		}
		return new ByteSizeValue(bytes);
	}

	
	/**
	 * Gets the primary store size.
	 *
	 * @return the primary store size
	 */
	public ByteSizeValue getPrimaryStoreSize() {
		return primaryStoreSize();
	}

	
	/**
	 * Store size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue storeSize() {
		long bytes = -1;
		for (IndexShardStatus shard : this) {
			if (shard.storeSize() != null) {
				if (bytes == -1) {
					bytes = 0;
				}
				bytes += shard.storeSize().bytes();
			}
		}
		if (bytes == -1) {
			return null;
		}
		return new ByteSizeValue(bytes);
	}

	
	/**
	 * Gets the store size.
	 *
	 * @return the store size
	 */
	public ByteSizeValue getStoreSize() {
		return storeSize();
	}

	
	/**
	 * Translog operations.
	 *
	 * @return the long
	 */
	public long translogOperations() {
		long translogOperations = -1;
		for (IndexShardStatus shard : this) {
			if (shard.translogOperations() != -1) {
				if (translogOperations == -1) {
					translogOperations = 0;
				}
				translogOperations += shard.translogOperations();
			}
		}
		return translogOperations;
	}

	
	/**
	 * Gets the translog operations.
	 *
	 * @return the translog operations
	 */
	public long getTranslogOperations() {
		return translogOperations();
	}

	
	/** The docs. */
	private transient DocsStatus docs;

	
	/**
	 * Docs.
	 *
	 * @return the docs status
	 */
	public DocsStatus docs() {
		if (docs != null) {
			return docs;
		}
		DocsStatus docs = null;
		for (IndexShardStatus shard : this) {
			if (shard.docs() == null) {
				continue;
			}
			if (docs == null) {
				docs = new DocsStatus();
			}
			docs.numDocs += shard.docs().numDocs();
			docs.maxDoc += shard.docs().maxDoc();
			docs.deletedDocs += shard.docs().deletedDocs();
		}
		this.docs = docs;
		return docs;
	}

	
	/**
	 * Gets the docs.
	 *
	 * @return the docs
	 */
	public DocsStatus getDocs() {
		return docs();
	}

	
	/**
	 * Merge stats.
	 *
	 * @return the merge stats
	 */
	public MergeStats mergeStats() {
		MergeStats mergeStats = new MergeStats();
		for (IndexShardStatus shard : this) {
			mergeStats.add(shard.mergeStats());
		}
		return mergeStats;
	}

	
	/**
	 * Gets the merge stats.
	 *
	 * @return the merge stats
	 */
	public MergeStats getMergeStats() {
		return this.mergeStats();
	}

	
	/**
	 * Refresh stats.
	 *
	 * @return the refresh stats
	 */
	public RefreshStats refreshStats() {
		RefreshStats refreshStats = new RefreshStats();
		for (IndexShardStatus shard : this) {
			refreshStats.add(shard.refreshStats());
		}
		return refreshStats;
	}

	
	/**
	 * Gets the refresh stats.
	 *
	 * @return the refresh stats
	 */
	public RefreshStats getRefreshStats() {
		return refreshStats();
	}

	
	/**
	 * Flush stats.
	 *
	 * @return the flush stats
	 */
	public FlushStats flushStats() {
		FlushStats flushStats = new FlushStats();
		for (IndexShardStatus shard : this) {
			flushStats.add(shard.flushStats());
		}
		return flushStats;
	}

	
	/**
	 * Gets the flush stats.
	 *
	 * @return the flush stats
	 */
	public FlushStats getFlushStats() {
		return flushStats();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<IndexShardStatus> iterator() {
		return indexShards.values().iterator();
	}

}
