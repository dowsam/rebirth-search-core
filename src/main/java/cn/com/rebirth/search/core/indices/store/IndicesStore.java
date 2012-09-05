/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesStore.java 2012-7-6 14:30:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.store;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.io.FileSystemUtils;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;
import cn.com.rebirth.search.core.cluster.routing.IndexRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.IndexShardRoutingTable;
import cn.com.rebirth.search.core.cluster.routing.RoutingTable;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.ShardRoutingState;
import cn.com.rebirth.search.core.env.NodeEnvironment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.indices.IndicesService;

/**
 * The Class IndicesStore.
 *
 * @author l.xue.nong
 */
public class IndicesStore extends AbstractComponent implements ClusterStateListener {

	/** The node env. */
	private final NodeEnvironment nodeEnv;

	/** The indices service. */
	private final IndicesService indicesService;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The dangling timeout. */
	private final TimeValue danglingTimeout;

	/** The dangling indices. */
	private final Map<String, DanglingIndex> danglingIndices = ConcurrentCollections.newConcurrentMap();

	/** The dangling mutex. */
	private final Object danglingMutex = new Object();

	/**
	 * The Class DanglingIndex.
	 *
	 * @author l.xue.nong
	 */
	static class DanglingIndex {

		/** The index. */
		public final String index;

		/** The future. */
		public final ScheduledFuture future;

		/**
		 * Instantiates a new dangling index.
		 *
		 * @param index the index
		 * @param future the future
		 */
		DanglingIndex(String index, ScheduledFuture future) {
			this.index = index;
			this.future = future;
		}
	}

	/**
	 * Instantiates a new indices store.
	 *
	 * @param settings the settings
	 * @param nodeEnv the node env
	 * @param indicesService the indices service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 */
	@Inject
	public IndicesStore(Settings settings, NodeEnvironment nodeEnv, IndicesService indicesService,
			ClusterService clusterService, ThreadPool threadPool) {
		super(settings);
		this.nodeEnv = nodeEnv;
		this.indicesService = indicesService;
		this.clusterService = clusterService;
		this.threadPool = threadPool;

		this.danglingTimeout = componentSettings.getAsTime("dangling_timeout", TimeValue.timeValueHours(2));

		clusterService.addLast(this);
	}

	/**
	 * Close.
	 */
	public void close() {
		clusterService.remove(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.rebirth.search.core.cluster.ClusterChangedEvent)
	 */
	@Override
	public void clusterChanged(ClusterChangedEvent event) {
		if (!event.routingTableChanged()) {
			return;
		}

		if (event.state().blocks().disableStatePersistence()) {
			return;
		}

		RoutingTable routingTable = event.state().routingTable();
		for (IndexRoutingTable indexRoutingTable : routingTable) {
			IndexService indexService = indicesService.indexService(indexRoutingTable.index());
			if (indexService == null) {

				continue;
			}

			if (!indexService.store().persistent()) {
				continue;
			}
			for (IndexShardRoutingTable indexShardRoutingTable : indexRoutingTable) {

				if (indexService.hasShard(indexShardRoutingTable.shardId().id())) {
					continue;
				}
				if (!indexService.store().canDeleteUnallocated(indexShardRoutingTable.shardId())) {
					continue;
				}

				if (indexShardRoutingTable.countWithState(ShardRoutingState.STARTED) == indexShardRoutingTable.size()) {
					if (logger.isDebugEnabled()) {
						logger.debug("[{}][{}] deleting unallocated shard", indexShardRoutingTable.shardId().index()
								.name(), indexShardRoutingTable.shardId().id());
					}
					try {
						indexService.store().deleteUnallocated(indexShardRoutingTable.shardId());
					} catch (Exception e) {
						logger.debug("[" + indexShardRoutingTable.shardId().index().name() + "]["
								+ indexShardRoutingTable.shardId().id()
								+ "] failed to delete unallocated shard, ignoring", e);
					}
				}
			}
		}

		if (nodeEnv.hasNodeFile()) {

			for (IndexRoutingTable indexRoutingTable : routingTable) {
				IndexService indexService = indicesService.indexService(indexRoutingTable.index());
				if (indexService != null) {
					continue;
				}
				for (IndexShardRoutingTable indexShardRoutingTable : indexRoutingTable) {
					boolean shardCanBeDeleted = true;
					for (ShardRouting shardRouting : indexShardRoutingTable) {

						if (!shardRouting.active()) {
							shardCanBeDeleted = false;
							break;
						}
						String localNodeId = clusterService.localNode().id();

						if (localNodeId.equals(shardRouting.currentNodeId())
								|| localNodeId.equals(shardRouting.relocatingNodeId())) {

							shardCanBeDeleted = false;
							break;
						}
					}
					if (shardCanBeDeleted) {
						ShardId shardId = indexShardRoutingTable.shardId();
						for (File shardLocation : nodeEnv.shardLocations(shardId)) {
							if (shardLocation.exists()) {
								logger.debug("[{}][{}] deleting shard that is no longer used", shardId.index().name(),
										shardId.id());
								FileSystemUtils.deleteRecursively(shardLocation);
							}
						}
					}
				}
			}

			if (danglingTimeout.millis() >= 0) {
				synchronized (danglingMutex) {
					for (String danglingIndex : danglingIndices.keySet()) {
						if (event.state().metaData().hasIndex(danglingIndex)) {
							logger.debug("[{}] no longer dangling (created), removing", danglingIndex);
							DanglingIndex removed = danglingIndices.remove(danglingIndex);
							removed.future.cancel(false);
						}
					}

					try {
						for (String indexName : nodeEnv.findAllIndices()) {

							if (event.state().metaData().hasIndex(indexName)) {
								continue;
							}
							if (danglingIndices.containsKey(indexName)) {

								continue;
							}
							if (danglingTimeout.millis() == 0) {
								logger.info(
										"[{}] dangling index, exists on local file system, but not in cluster metadata, timeout set to 0, deleting now",
										indexName);
								FileSystemUtils.deleteRecursively(nodeEnv.indexLocations(new Index(indexName)));
							} else {
								logger.info(
										"[{}] dangling index, exists on local file system, but not in cluster metadata, scheduling to delete in [{}]",
										indexName, danglingTimeout);
								danglingIndices.put(
										indexName,
										new DanglingIndex(indexName, threadPool.schedule(danglingTimeout,
												ThreadPool.Names.SAME, new RemoveDanglingIndex(indexName))));
							}
						}
					} catch (Exception e) {
						logger.warn("failed to find dangling indices", e);
					}
				}
			}
		}
	}

	/**
	 * The Class RemoveDanglingIndex.
	 *
	 * @author l.xue.nong
	 */
	class RemoveDanglingIndex implements Runnable {

		/** The index. */
		private final String index;

		/**
		 * Instantiates a new removes the dangling index.
		 *
		 * @param index the index
		 */
		RemoveDanglingIndex(String index) {
			this.index = index;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			synchronized (danglingMutex) {
				DanglingIndex remove = danglingIndices.remove(index);

				if (remove == null) {
					return;
				}
				logger.info("[{}] deleting dangling index", index);
				FileSystemUtils.deleteRecursively(nodeEnv.indexLocations(new Index(index)));
			}
		}
	}
}
