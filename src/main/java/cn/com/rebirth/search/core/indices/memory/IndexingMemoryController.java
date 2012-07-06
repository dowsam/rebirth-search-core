/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexingMemoryController.java 2012-3-29 15:02:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.memory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeUnit;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.engine.EngineClosedException;
import cn.com.rebirth.search.core.index.engine.FlushNotAllowedEngineException;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.monitor.jvm.JvmInfo;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The Class IndexingMemoryController.
 *
 * @author l.xue.nong
 */
public class IndexingMemoryController extends AbstractLifecycleComponent<IndexingMemoryController> {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The indices service. */
	private final IndicesService indicesService;

	/** The indexing buffer. */
	private final ByteSizeValue indexingBuffer;

	/** The min shard index buffer size. */
	private final ByteSizeValue minShardIndexBufferSize;

	/** The max shard index buffer size. */
	private final ByteSizeValue maxShardIndexBufferSize;

	/** The inactive time. */
	private final TimeValue inactiveTime;

	/** The interval. */
	private final TimeValue interval;

	/** The listener. */
	private final Listener listener = new Listener();

	/** The shards indices status. */
	private final Map<ShardId, ShardIndexingStatus> shardsIndicesStatus = Maps.newHashMap();

	/** The scheduler. */
	private volatile ScheduledFuture scheduler;

	/** The mutex. */
	private final Object mutex = new Object();

	/**
	 * Instantiates a new indexing memory controller.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param indicesService the indices service
	 */
	@Inject
	public IndexingMemoryController(Settings settings, ThreadPool threadPool, IndicesService indicesService) {
		super(settings);
		this.threadPool = threadPool;
		this.indicesService = indicesService;

		ByteSizeValue indexingBuffer;
		String indexingBufferSetting = componentSettings.get("index_buffer_size", "10%");
		if (indexingBufferSetting.endsWith("%")) {
			double percent = Double.parseDouble(indexingBufferSetting.substring(0, indexingBufferSetting.length() - 1));
			indexingBuffer = new ByteSizeValue(
					(long) (((double) JvmInfo.jvmInfo().mem().heapMax().bytes()) * (percent / 100)));
			ByteSizeValue minIndexingBuffer = componentSettings.getAsBytesSize("min_index_buffer_size",
					new ByteSizeValue(48, ByteSizeUnit.MB));
			ByteSizeValue maxIndexingBuffer = componentSettings.getAsBytesSize("max_index_buffer_size", null);

			if (indexingBuffer.bytes() < minIndexingBuffer.bytes()) {
				indexingBuffer = minIndexingBuffer;
			}
			if (maxIndexingBuffer != null && indexingBuffer.bytes() > maxIndexingBuffer.bytes()) {
				indexingBuffer = maxIndexingBuffer;
			}
		} else {
			indexingBuffer = ByteSizeValue.parseBytesSizeValue(indexingBufferSetting, null);
		}

		this.indexingBuffer = indexingBuffer;
		this.minShardIndexBufferSize = componentSettings.getAsBytesSize("min_shard_index_buffer_size",
				new ByteSizeValue(4, ByteSizeUnit.MB));

		this.maxShardIndexBufferSize = componentSettings.getAsBytesSize("max_shard_index_buffer_size",
				new ByteSizeValue(512, ByteSizeUnit.MB));

		this.inactiveTime = componentSettings.getAsTime("shard_inactive_time", TimeValue.timeValueMinutes(30));

		this.interval = componentSettings.getAsTime("interval", TimeValue.timeValueSeconds(30));

		logger.debug("using index_buffer_size [" + this.indexingBuffer + "], with min_shard_index_buffer_size ["
				+ this.minShardIndexBufferSize + "], max_shard_index_buffer_size [" + this.maxShardIndexBufferSize
				+ "], shard_inactive_time [{}]", this.inactiveTime);

	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RestartException {
		indicesService.indicesLifecycle().addListener(listener);

		this.scheduler = threadPool.scheduleWithFixedDelay(new ShardsIndicesStatusChecker(), interval);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RestartException {
		indicesService.indicesLifecycle().removeListener(listener);
		if (scheduler != null) {
			scheduler.cancel(false);
			scheduler = null;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RestartException {
	}

	/**
	 * The Class ShardsIndicesStatusChecker.
	 *
	 * @author l.xue.nong
	 */
	class ShardsIndicesStatusChecker implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			synchronized (mutex) {
				boolean activeInactiveStatusChanges = false;
				List<IndexShard> activeToInactiveIndexingShards = Lists.newArrayList();
				List<IndexShard> inactiveToActiveIndexingShards = Lists.newArrayList();
				for (IndexService indexService : indicesService) {
					for (IndexShard indexShard : indexService) {
						long time = threadPool.estimatedTimeInMillis();
						Translog translog = ((InternalIndexShard) indexShard).translog();
						ShardIndexingStatus status = shardsIndicesStatus.get(indexShard.shardId());
						if (status == null) {
							continue;
						}

						if (status.translogId == translog.currentId() && translog.estimatedNumberOfOperations() == 0) {
							if (status.time == -1) {
								status.time = time;
							}

							if (!status.inactiveIndexing) {

								if ((time - status.time) > inactiveTime.millis()
										&& indexShard.mergeStats().current() == 0) {

									activeToInactiveIndexingShards.add(indexShard);
									status.inactiveIndexing = true;
									activeInactiveStatusChanges = true;
									logger.debug("marking shard [" + indexShard.shardId().index().name() + "]["
											+ indexShard.shardId().id()
											+ "] as inactive (inactive_time[{}]) indexing wise, setting size to [{}]",
											inactiveTime, Engine.INACTIVE_SHARD_INDEXING_BUFFER);
								}
							}
						} else {
							if (status.inactiveIndexing) {
								inactiveToActiveIndexingShards.add(indexShard);
								status.inactiveIndexing = false;
								activeInactiveStatusChanges = true;
								logger.debug("marking shard [{}][{}] as active indexing wise", indexShard.shardId()
										.index().name(), indexShard.shardId().id());
							}
							status.time = -1;
						}
						status.translogId = translog.currentId();
						status.translogNumberOfOperations = translog.estimatedNumberOfOperations();
					}
				}
				for (IndexShard indexShard : activeToInactiveIndexingShards) {

					try {
						((InternalIndexShard) indexShard).engine().updateIndexingBufferSize(
								Engine.INACTIVE_SHARD_INDEXING_BUFFER);
					} catch (EngineClosedException e) {

					} catch (FlushNotAllowedEngineException e) {

					}
				}
				if (activeInactiveStatusChanges) {
					calcAndSetShardIndexingBuffer("shards became active/inactive (indexing wise)");
				}
			}
		}
	}

	/**
	 * The Class Listener.
	 *
	 * @author l.xue.nong
	 */
	class Listener extends cn.com.rebirth.search.core.indices.IndicesLifecycle.Listener {

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.indices.IndicesLifecycle.Listener#afterIndexShardCreated(cn.com.summall.search.core.index.shard.service.IndexShard)
		 */
		@Override
		public void afterIndexShardCreated(IndexShard indexShard) {
			synchronized (mutex) {
				calcAndSetShardIndexingBuffer("created_shard[" + indexShard.shardId().index().name() + "]["
						+ indexShard.shardId().id() + "]");
				shardsIndicesStatus.put(indexShard.shardId(), new ShardIndexingStatus());
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.indices.IndicesLifecycle.Listener#afterIndexShardClosed(cn.com.summall.search.core.index.shard.ShardId, boolean)
		 */
		@Override
		public void afterIndexShardClosed(ShardId shardId, boolean delete) {
			synchronized (mutex) {
				calcAndSetShardIndexingBuffer("removed_shard[" + shardId.index().name() + "][" + shardId.id() + "]");
				shardsIndicesStatus.remove(shardId);
			}
		}
	}

	/**
	 * Calc and set shard indexing buffer.
	 *
	 * @param reason the reason
	 */
	private void calcAndSetShardIndexingBuffer(String reason) {
		int shardsCount = countShards();
		if (shardsCount == 0) {
			return;
		}
		ByteSizeValue shardIndexingBufferSize = calcShardIndexingBuffer(shardsCount);
		if (shardIndexingBufferSize == null) {
			return;
		}
		if (shardIndexingBufferSize.bytes() < minShardIndexBufferSize.bytes()) {
			shardIndexingBufferSize = minShardIndexBufferSize;
		}
		if (shardIndexingBufferSize.bytes() > maxShardIndexBufferSize.bytes()) {
			shardIndexingBufferSize = maxShardIndexBufferSize;
		}
		logger.debug("recalculating shard indexing buffer (reason=" + reason + "), total is [" + indexingBuffer
				+ "] with [{}] active shards, each shard set to [{}]", shardsCount, shardIndexingBufferSize);
		for (IndexService indexService : indicesService) {
			for (IndexShard indexShard : indexService) {
				ShardIndexingStatus status = shardsIndicesStatus.get(indexShard.shardId());
				if (status == null || !status.inactiveIndexing) {
					try {
						((InternalIndexShard) indexShard).engine().updateIndexingBufferSize(shardIndexingBufferSize);
					} catch (EngineClosedException e) {

						continue;
					} catch (FlushNotAllowedEngineException e) {

						continue;
					} catch (Exception e) {
						logger.warn("failed to set shard [" + indexShard.shardId().index().name()
								+ "][{}] index buffer to [{}]", indexShard.shardId().id(), shardIndexingBufferSize);
					}
				}
			}
		}
	}

	/**
	 * Calc shard indexing buffer.
	 *
	 * @param shardsCount the shards count
	 * @return the byte size value
	 */
	private ByteSizeValue calcShardIndexingBuffer(int shardsCount) {
		return new ByteSizeValue(indexingBuffer.bytes() / shardsCount);
	}

	/**
	 * Count shards.
	 *
	 * @return the int
	 */
	private int countShards() {
		int shardsCount = 0;
		for (IndexService indexService : indicesService) {
			for (IndexShard indexShard : indexService) {
				ShardIndexingStatus status = shardsIndicesStatus.get(indexShard.shardId());
				if (status == null || !status.inactiveIndexing) {
					shardsCount++;
				}
			}
		}
		return shardsCount;
	}

	/**
	 * The Class ShardIndexingStatus.
	 *
	 * @author l.xue.nong
	 */
	static class ShardIndexingStatus {

		/** The translog id. */
		long translogId = -1;

		/** The translog number of operations. */
		int translogNumberOfOperations = -1;

		/** The inactive indexing. */
		boolean inactiveIndexing = false;

		/** The time. */
		long time = -1;
	}
}
