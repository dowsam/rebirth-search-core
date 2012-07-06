/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardStatus.java 2012-3-29 15:02:01 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.status;

import java.util.Iterator;

import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.core.index.flush.FlushStats;
import cn.com.rebirth.search.core.index.merge.MergeStats;
import cn.com.rebirth.search.core.index.refresh.RefreshStats;
import cn.com.rebirth.search.core.index.shard.ShardId;

import com.google.common.collect.Iterators;


/**
 * The Class IndexShardStatus.
 *
 * @author l.xue.nong
 */
public class IndexShardStatus implements Iterable<ShardStatus> {

	
	/** The shard id. */
	private final ShardId shardId;

	
	/** The shards. */
	private final ShardStatus[] shards;

	
	/**
	 * Instantiates a new index shard status.
	 *
	 * @param shardId the shard id
	 * @param shards the shards
	 */
	IndexShardStatus(ShardId shardId, ShardStatus[] shards) {
		this.shardId = shardId;
		this.shards = shards;
	}

	
	/**
	 * Shard id.
	 *
	 * @return the shard id
	 */
	public ShardId shardId() {
		return this.shardId;
	}

	
	/**
	 * Gets the shard id.
	 *
	 * @return the shard id
	 */
	public ShardId getShardId() {
		return shardId();
	}

	
	/**
	 * Shards.
	 *
	 * @return the shard status[]
	 */
	public ShardStatus[] shards() {
		return this.shards;
	}

	
	/**
	 * Gets the shards.
	 *
	 * @return the shards
	 */
	public ShardStatus[] getShards() {
		return shards();
	}

	
	/**
	 * Gets the at.
	 *
	 * @param position the position
	 * @return the at
	 */
	public ShardStatus getAt(int position) {
		return shards[position];
	}

	
	/**
	 * Primary store size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue primaryStoreSize() {
		long bytes = -1;
		for (ShardStatus shard : shards()) {
			if (!shard.shardRouting().primary()) {
				
				continue;
			}
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
		for (ShardStatus shard : shards()) {
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
		for (ShardStatus shard : shards()) {
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
		for (ShardStatus shard : shards()) {
			if (!shard.shardRouting().primary()) {
				
				continue;
			}
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
		return this.docs;
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
		for (ShardStatus shard : shards) {
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
		for (ShardStatus shard : shards) {
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
		for (ShardStatus shard : shards) {
			flushStats.add(shard.flushStats);
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
	public Iterator<ShardStatus> iterator() {
		return Iterators.forArray(shards);
	}

}