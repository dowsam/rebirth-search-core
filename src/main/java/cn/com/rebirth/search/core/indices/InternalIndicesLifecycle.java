/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalIndicesLifecycle.java 2012-3-29 15:00:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices;

import java.util.concurrent.CopyOnWriteArrayList;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;


/**
 * The Class InternalIndicesLifecycle.
 *
 * @author l.xue.nong
 */
public class InternalIndicesLifecycle extends AbstractComponent implements IndicesLifecycle {

	
	/** The listeners. */
	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	
	/**
	 * Instantiates a new internal indices lifecycle.
	 *
	 * @param settings the settings
	 */
	@Inject
	public InternalIndicesLifecycle(Settings settings) {
		super(settings);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.indices.IndicesLifecycle#addListener(cn.com.summall.search.core.indices.IndicesLifecycle.Listener)
	 */
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.indices.IndicesLifecycle#removeListener(cn.com.summall.search.core.indices.IndicesLifecycle.Listener)
	 */
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	
	/**
	 * Shard routing changed.
	 *
	 * @param indexShard the index shard
	 * @param oldRouting the old routing
	 * @param newRouting the new routing
	 */
	public void shardRoutingChanged(IndexShard indexShard, @Nullable ShardRouting oldRouting, ShardRouting newRouting) {
		for (Listener listener : listeners) {
			listener.shardRoutingChanged(indexShard, oldRouting, newRouting);
		}
	}

	
	/**
	 * Before index created.
	 *
	 * @param index the index
	 */
	public void beforeIndexCreated(Index index) {
		for (Listener listener : listeners) {
			listener.beforeIndexCreated(index);
		}
	}

	
	/**
	 * After index created.
	 *
	 * @param indexService the index service
	 */
	public void afterIndexCreated(IndexService indexService) {
		for (Listener listener : listeners) {
			listener.afterIndexCreated(indexService);
		}
	}

	
	/**
	 * Before index shard created.
	 *
	 * @param shardId the shard id
	 */
	public void beforeIndexShardCreated(ShardId shardId) {
		for (Listener listener : listeners) {
			listener.beforeIndexShardCreated(shardId);
		}
	}

	
	/**
	 * After index shard created.
	 *
	 * @param indexShard the index shard
	 */
	public void afterIndexShardCreated(IndexShard indexShard) {
		for (Listener listener : listeners) {
			listener.afterIndexShardCreated(indexShard);
		}
	}

	
	/**
	 * After index shard started.
	 *
	 * @param indexShard the index shard
	 */
	public void afterIndexShardStarted(IndexShard indexShard) {
		for (Listener listener : listeners) {
			listener.afterIndexShardStarted(indexShard);
		}
	}

	
	/**
	 * Before index closed.
	 *
	 * @param indexService the index service
	 * @param delete the delete
	 */
	public void beforeIndexClosed(IndexService indexService, boolean delete) {
		for (Listener listener : listeners) {
			listener.beforeIndexClosed(indexService, delete);
		}
	}

	
	/**
	 * After index closed.
	 *
	 * @param index the index
	 * @param delete the delete
	 */
	public void afterIndexClosed(Index index, boolean delete) {
		for (Listener listener : listeners) {
			listener.afterIndexClosed(index, delete);
		}
	}

	
	/**
	 * Before index shard closed.
	 *
	 * @param shardId the shard id
	 * @param indexShard the index shard
	 * @param delete the delete
	 */
	public void beforeIndexShardClosed(ShardId shardId, @Nullable IndexShard indexShard, boolean delete) {
		for (Listener listener : listeners) {
			listener.beforeIndexShardClosed(shardId, indexShard, delete);
		}
	}

	
	/**
	 * After index shard closed.
	 *
	 * @param shardId the shard id
	 * @param delete the delete
	 */
	public void afterIndexShardClosed(ShardId shardId, boolean delete) {
		for (Listener listener : listeners) {
			listener.afterIndexShardClosed(shardId, delete);
		}
	}
}
