/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TranslogService.java 2012-3-29 15:01:39 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.translog;

import java.util.concurrent.ScheduledFuture;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeUnit;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.engine.FlushNotAllowedEngineException;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.settings.IndexSettingsService;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.IllegalIndexShardStateException;
import cn.com.rebirth.search.core.index.shard.IndexShardState;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.threadpool.ThreadPool;


/**
 * The Class TranslogService.
 *
 * @author l.xue.nong
 */
public class TranslogService extends AbstractIndexShardComponent {

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The index settings service. */
	private final IndexSettingsService indexSettingsService;

	
	/** The index shard. */
	private final IndexShard indexShard;

	
	/** The translog. */
	private final Translog translog;

	
	/** The flush threshold operations. */
	private int flushThresholdOperations;

	
	/** The flush threshold size. */
	private ByteSizeValue flushThresholdSize;

	
	/** The flush threshold period. */
	private TimeValue flushThresholdPeriod;

	
	/** The disable flush. */
	private boolean disableFlush;

	
	/** The interval. */
	private final TimeValue interval;

	
	/** The future. */
	private ScheduledFuture future;

	
	/** The apply settings. */
	private final ApplySettings applySettings = new ApplySettings();

	
	/**
	 * Instantiates a new translog service.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param indexSettingsService the index settings service
	 * @param threadPool the thread pool
	 * @param indexShard the index shard
	 * @param translog the translog
	 */
	@Inject
	public TranslogService(ShardId shardId, @IndexSettings Settings indexSettings,
			IndexSettingsService indexSettingsService, ThreadPool threadPool, IndexShard indexShard, Translog translog) {
		super(shardId, indexSettings);
		this.threadPool = threadPool;
		this.indexSettingsService = indexSettingsService;
		this.indexShard = indexShard;
		this.translog = translog;

		this.flushThresholdOperations = componentSettings.getAsInt("flush_threshold_ops",
				componentSettings.getAsInt("flush_threshold", 5000));
		this.flushThresholdSize = componentSettings.getAsBytesSize("flush_threshold_size", new ByteSizeValue(200,
				ByteSizeUnit.MB));
		this.flushThresholdPeriod = componentSettings.getAsTime("flush_threshold_period",
				TimeValue.timeValueMinutes(30));
		this.interval = componentSettings.getAsTime("interval", TimeValue.timeValueMillis(5000));
		this.disableFlush = componentSettings.getAsBoolean("disable_flush", false);

		logger.debug("interval [" + interval + "], flush_threshold_ops [" + flushThresholdOperations
				+ "], flush_threshold_size [{}], flush_threshold_period [{}]", flushThresholdSize, flushThresholdPeriod);

		this.future = threadPool.schedule(interval, ThreadPool.Names.SAME, new TranslogBasedFlush());

		indexSettingsService.addListener(applySettings);
	}

	
	/**
	 * Close.
	 */
	public void close() {
		indexSettingsService.removeListener(applySettings);
		this.future.cancel(true);
	}

	static {
		IndexMetaData.addDynamicSettings("index.translog.flush_threshold_ops", "index.translog.flush_threshold_size",
				"index.translog.flush_threshold_period", "index.translog.disable_flush");
	}

	
	/**
	 * The Class ApplySettings.
	 *
	 * @author l.xue.nong
	 */
	class ApplySettings implements IndexSettingsService.Listener {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.settings.IndexSettingsService.Listener#onRefreshSettings(cn.com.summall.search.commons.settings.Settings)
		 */
		@Override
		public void onRefreshSettings(Settings settings) {
			int flushThresholdOperations = settings.getAsInt("index.translog.flush_threshold_ops",
					TranslogService.this.flushThresholdOperations);
			if (flushThresholdOperations != TranslogService.this.flushThresholdOperations) {
				logger.info("updating flush_threshold_ops from [{}] to [{}]",
						TranslogService.this.flushThresholdOperations, flushThresholdOperations);
				TranslogService.this.flushThresholdOperations = flushThresholdOperations;
			}
			ByteSizeValue flushThresholdSize = settings.getAsBytesSize("index.translog.flush_threshold_size",
					TranslogService.this.flushThresholdSize);
			if (!flushThresholdSize.equals(TranslogService.this.flushThresholdSize)) {
				logger.info("updating flush_threshold_size from [{}] to [{}]", TranslogService.this.flushThresholdSize,
						flushThresholdSize);
				TranslogService.this.flushThresholdSize = flushThresholdSize;
			}
			TimeValue flushThresholdPeriod = settings.getAsTime("index.translog.flush_threshold_period",
					TranslogService.this.flushThresholdPeriod);
			if (!flushThresholdPeriod.equals(TranslogService.this.flushThresholdPeriod)) {
				logger.info("updating flush_threshold_period from [{}] to [{}]",
						TranslogService.this.flushThresholdPeriod, flushThresholdPeriod);
				TranslogService.this.flushThresholdPeriod = flushThresholdPeriod;
			}
			boolean disableFlush = settings.getAsBoolean("index.translog.disable_flush",
					TranslogService.this.disableFlush);
			if (disableFlush != TranslogService.this.disableFlush) {
				logger.info("updating disable_flush from [{}] to [{}]", TranslogService.this.disableFlush, disableFlush);
				TranslogService.this.disableFlush = disableFlush;
			}
		}
	}

	
	/**
	 * The Class TranslogBasedFlush.
	 *
	 * @author l.xue.nong
	 */
	private class TranslogBasedFlush implements Runnable {

		
		/** The last flush time. */
		private volatile long lastFlushTime = System.currentTimeMillis();

		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (indexShard.state() == IndexShardState.CLOSED) {
				return;
			}

			
			if (disableFlush) {
				reschedule();
				return;
			}

			if (indexShard.state() == IndexShardState.CREATED) {
				reschedule();
				return;
			}

			if (flushThresholdOperations > 0) {
				int currentNumberOfOperations = translog.estimatedNumberOfOperations();
				if (currentNumberOfOperations > flushThresholdOperations) {
					logger.trace("flushing translog, operations [{}], breached [{}]", currentNumberOfOperations,
							flushThresholdOperations);
					asyncFlushAndReschedule();
					return;
				}
			}

			if (flushThresholdSize.bytes() > 0) {
				long sizeInBytes = translog.translogSizeInBytes();
				if (sizeInBytes > flushThresholdSize.bytes()) {
					logger.trace("flushing translog, size [{}], breached [{}]", new ByteSizeValue(sizeInBytes),
							flushThresholdSize);
					asyncFlushAndReschedule();
					return;
				}
			}

			if (flushThresholdPeriod.millis() > 0) {
				if ((threadPool.estimatedTimeInMillis() - lastFlushTime) > flushThresholdPeriod.millis()) {
					logger.trace("flushing translog, last_flush_time [{}], breached [{}]", lastFlushTime,
							flushThresholdPeriod);
					asyncFlushAndReschedule();
					return;
				}
			}

			reschedule();
		}

		
		/**
		 * Reschedule.
		 */
		private void reschedule() {
			future = threadPool.schedule(interval, ThreadPool.Names.SAME, this);
		}

		
		/**
		 * Async flush and reschedule.
		 */
		private void asyncFlushAndReschedule() {
			threadPool.executor(ThreadPool.Names.FLUSH).execute(new Runnable() {
				@Override
				public void run() {
					try {
						indexShard.flush(new Engine.Flush());
					} catch (IllegalIndexShardStateException e) {
						
					} catch (FlushNotAllowedEngineException e) {
						
					} catch (Exception e) {
						logger.warn("failed to flush shard on translog threshold", e);
					}
					lastFlushTime = threadPool.estimatedTimeInMillis();

					if (indexShard.state() != IndexShardState.CLOSED) {
						future = threadPool.schedule(interval, ThreadPool.Names.SAME, TranslogBasedFlush.this);
					}
				}
			});
		}
	}
}
