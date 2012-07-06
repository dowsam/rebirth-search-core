/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardManagement.java 2012-3-29 15:00:59 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.shard;

import java.io.IOException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.component.CloseableComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.IndexServiceManagement;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.store.Store;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.jmx.JmxService;
import cn.com.rebirth.search.core.jmx.MBean;
import cn.com.rebirth.search.core.jmx.ManagedAttribute;


/**
 * The Class IndexShardManagement.
 *
 * @author l.xue.nong
 */
@MBean(objectName = "", description = "")
public class IndexShardManagement extends AbstractIndexShardComponent implements CloseableComponent {

	
	/**
	 * Builds the shard group name.
	 *
	 * @param shardId the shard id
	 * @return the string
	 */
	public static String buildShardGroupName(ShardId shardId) {
		return IndexServiceManagement.buildIndexGroupName(shardId.index()) + ",subService=shards,shard=" + shardId.id();
	}

	
	/** The jmx service. */
	private final JmxService jmxService;

	
	/** The index shard. */
	private final IndexShard indexShard;

	
	/** The store. */
	private final Store store;

	
	/** The translog. */
	private final Translog translog;

	
	/**
	 * Instantiates a new index shard management.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param jmxService the jmx service
	 * @param indexShard the index shard
	 * @param store the store
	 * @param translog the translog
	 */
	@Inject
	public IndexShardManagement(ShardId shardId, @IndexSettings Settings indexSettings, JmxService jmxService,
			IndexShard indexShard, Store store, Translog translog) {
		super(shardId, indexSettings);
		this.jmxService = jmxService;
		this.indexShard = indexShard;
		this.store = store;
		this.translog = translog;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.CloseableComponent#close()
	 */
	public void close() {
		jmxService.unregisterGroup(buildShardGroupName(indexShard.shardId()));
	}

	
	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	@ManagedAttribute(description = "Index Name")
	public String getIndex() {
		return indexShard.shardId().index().name();
	}

	
	/**
	 * Gets the shard id.
	 *
	 * @return the shard id
	 */
	@ManagedAttribute(description = "Shard Id")
	public int getShardId() {
		return indexShard.shardId().id();
	}

	
	/**
	 * Gets the store size.
	 *
	 * @return the store size
	 */
	@ManagedAttribute(description = "Storage Size")
	public String getStoreSize() {
		try {
			return store.estimateSize().toString();
		} catch (IOException e) {
			return "NA";
		}
	}

	
	/**
	 * Gets the translog id.
	 *
	 * @return the translog id
	 */
	@ManagedAttribute(description = "The current transaction log id")
	public long getTranslogId() {
		return translog.currentId();
	}

	
	/**
	 * Gets the translog number of operations.
	 *
	 * @return the translog number of operations
	 */
	@ManagedAttribute(description = "Number of transaction log operations")
	public long getTranslogNumberOfOperations() {
		return translog.estimatedNumberOfOperations();
	}

	
	/**
	 * Gets the translog size.
	 *
	 * @return the translog size
	 */
	@ManagedAttribute(description = "Estimated size in memory the transaction log takes")
	public String getTranslogSize() {
		return new ByteSizeValue(translog.memorySizeInBytes()).toString();
	}

	
	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	@ManagedAttribute(description = "The state of the shard")
	public String getState() {
		return indexShard.state().toString();
	}

	
	/**
	 * Checks if is primary.
	 *
	 * @return true, if is primary
	 */
	@ManagedAttribute(description = "Primary")
	public boolean isPrimary() {
		return indexShard.routingEntry().primary();
	}

	
	/**
	 * Gets the routing state.
	 *
	 * @return the routing state
	 */
	@ManagedAttribute(description = "The state of the shard as perceived by the cluster")
	public String getRoutingState() {
		return indexShard.routingEntry().state().toString();
	}

	
	/**
	 * Gets the num docs.
	 *
	 * @return the num docs
	 */
	@ManagedAttribute(description = "The number of documents in the index")
	public int getNumDocs() {
		Engine.Searcher searcher = indexShard.searcher();
		try {
			return searcher.reader().numDocs();
		} finally {
			searcher.release();
		}
	}

	
	/**
	 * Gets the max doc.
	 *
	 * @return the max doc
	 */
	@ManagedAttribute(description = "The total number of documents in the index (including deleted ones)")
	public int getMaxDoc() {
		Engine.Searcher searcher = indexShard.searcher();
		try {
			return searcher.reader().maxDoc();
		} finally {
			searcher.release();
		}
	}
}
