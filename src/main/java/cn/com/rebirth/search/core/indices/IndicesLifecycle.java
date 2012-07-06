/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesLifecycle.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;

/**
 * The Interface IndicesLifecycle.
 *
 * @author l.xue.nong
 */
public interface IndicesLifecycle {

	/**
	 * Adds the listener.
	 *
	 * @param listener the listener
	 */
	void addListener(Listener listener);

	/**
	 * Removes the listener.
	 *
	 * @param listener the listener
	 */
	void removeListener(Listener listener);

	/**
	 * The Class Listener.
	 *
	 * @author l.xue.nong
	 */
	public abstract static class Listener {

		/**
		 * Shard routing changed.
		 *
		 * @param indexShard the index shard
		 * @param oldRouting the old routing
		 * @param newRouting the new routing
		 */
		public void shardRoutingChanged(IndexShard indexShard, @Nullable ShardRouting oldRouting,
				ShardRouting newRouting) {

		}

		/**
		 * Before index created.
		 *
		 * @param index the index
		 */
		public void beforeIndexCreated(Index index) {

		}

		/**
		 * After index created.
		 *
		 * @param indexService the index service
		 */
		public void afterIndexCreated(IndexService indexService) {

		}

		/**
		 * Before index shard created.
		 *
		 * @param shardId the shard id
		 */
		public void beforeIndexShardCreated(ShardId shardId) {

		}

		/**
		 * After index shard created.
		 *
		 * @param indexShard the index shard
		 */
		public void afterIndexShardCreated(IndexShard indexShard) {

		}

		/**
		 * After index shard started.
		 *
		 * @param indexShard the index shard
		 */
		public void afterIndexShardStarted(IndexShard indexShard) {

		}

		/**
		 * Before index closed.
		 *
		 * @param indexService the index service
		 * @param delete the delete
		 */
		public void beforeIndexClosed(IndexService indexService, boolean delete) {

		}

		/**
		 * After index closed.
		 *
		 * @param index the index
		 * @param delete the delete
		 */
		public void afterIndexClosed(Index index, boolean delete) {

		}

		/**
		 * Before index shard closed.
		 *
		 * @param shardId the shard id
		 * @param indexShard the index shard
		 * @param delete the delete
		 */
		public void beforeIndexShardClosed(ShardId shardId, @Nullable IndexShard indexShard, boolean delete) {

		}

		/**
		 * After index shard closed.
		 *
		 * @param shardId the shard id
		 * @param delete the delete
		 */
		public void afterIndexShardClosed(ShardId shardId, boolean delete) {

		}
	}

}
