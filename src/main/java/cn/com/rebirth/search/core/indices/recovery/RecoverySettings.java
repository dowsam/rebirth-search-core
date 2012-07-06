/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RecoverySettings.java 2012-7-6 14:29:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.recovery;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.concurrent.EsExecutors;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeUnit;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.RateLimiter;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;

import com.google.common.base.Objects;

/**
 * The Class RecoverySettings.
 *
 * @author l.xue.nong
 */
public class RecoverySettings extends AbstractComponent {

	static {
		MetaData.addDynamicSettings("indices.recovery.file_chunk_size");
		MetaData.addDynamicSettings("indices.recovery.translog_ops");
		MetaData.addDynamicSettings("indices.recovery.translog_size");
		MetaData.addDynamicSettings("indices.recovery.compress");
		MetaData.addDynamicSettings("indices.recovery.concurrent_streams");
		MetaData.addDynamicSettings("indices.recovery.max_size_per_sec");
	}

	/** The file chunk size. */
	private volatile ByteSizeValue fileChunkSize;

	/** The compress. */
	private volatile boolean compress;

	/** The translog ops. */
	private volatile int translogOps;

	/** The translog size. */
	private volatile ByteSizeValue translogSize;

	/** The concurrent streams. */
	private volatile int concurrentStreams;

	/** The concurrent stream pool. */
	private final ThreadPoolExecutor concurrentStreamPool;

	/** The max size per sec. */
	private volatile ByteSizeValue maxSizePerSec;

	/** The rate limiter. */
	private volatile RateLimiter rateLimiter;

	/**
	 * Instantiates a new recovery settings.
	 *
	 * @param settings the settings
	 * @param nodeSettingsService the node settings service
	 */
	@Inject
	public RecoverySettings(Settings settings, NodeSettingsService nodeSettingsService) {
		super(settings);

		this.fileChunkSize = componentSettings.getAsBytesSize("file_chunk_size", settings.getAsBytesSize(
				"index.shard.recovery.file_chunk_size", new ByteSizeValue(100, ByteSizeUnit.KB)));
		this.translogOps = componentSettings.getAsInt("translog_ops",
				settings.getAsInt("index.shard.recovery.translog_ops", 1000));
		this.translogSize = componentSettings.getAsBytesSize("translog_size",
				settings.getAsBytesSize("index.shard.recovery.translog_size", new ByteSizeValue(100, ByteSizeUnit.KB)));
		this.compress = componentSettings.getAsBoolean("compress", true);

		this.concurrentStreams = componentSettings.getAsInt("concurrent_streams",
				settings.getAsInt("index.shard.recovery.concurrent_streams", 5));
		this.concurrentStreamPool = EsExecutors.newScalingExecutorService(0, concurrentStreams, 60, TimeUnit.SECONDS,
				EsExecutors.daemonThreadFactory(settings, "[recovery_stream]"));

		this.maxSizePerSec = componentSettings.getAsBytesSize("max_size_per_sec", new ByteSizeValue(0));
		if (maxSizePerSec.bytes() <= 0) {
			rateLimiter = null;
		} else {
			rateLimiter = new RateLimiter(maxSizePerSec.mbFrac());
		}

		logger.debug("using max_size_per_sec[" + maxSizePerSec + "], concurrent_streams [" + concurrentStreams
				+ "], file_chunk_size [" + fileChunkSize + "], translog_size [" + translogSize
				+ "], translog_ops [{}], and compress [{}]", translogOps, compress);

		nodeSettingsService.addListener(new ApplySettings());
	}

	/**
	 * Close.
	 */
	public void close() {
		concurrentStreamPool.shutdown();
	}

	/**
	 * File chunk size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue fileChunkSize() {
		return fileChunkSize;
	}

	/**
	 * Compress.
	 *
	 * @return true, if successful
	 */
	public boolean compress() {
		return compress;
	}

	/**
	 * Translog ops.
	 *
	 * @return the int
	 */
	public int translogOps() {
		return translogOps;
	}

	/**
	 * Translog size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue translogSize() {
		return translogSize;
	}

	/**
	 * Concurrent streams.
	 *
	 * @return the int
	 */
	public int concurrentStreams() {
		return concurrentStreams;
	}

	/**
	 * Concurrent stream pool.
	 *
	 * @return the thread pool executor
	 */
	public ThreadPoolExecutor concurrentStreamPool() {
		return concurrentStreamPool;
	}

	/**
	 * Rate limiter.
	 *
	 * @return the rate limiter
	 */
	public RateLimiter rateLimiter() {
		return rateLimiter;
	}

	/**
	 * The Class ApplySettings.
	 *
	 * @author l.xue.nong
	 */
	class ApplySettings implements NodeSettingsService.Listener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.node.settings.NodeSettingsService.Listener#onRefreshSettings(cn.com.rebirth.commons.settings.Settings)
		 */
		@Override
		public void onRefreshSettings(Settings settings) {
			ByteSizeValue maxSizePerSec = settings.getAsBytesSize("indices.recovery.max_size_per_sec",
					RecoverySettings.this.maxSizePerSec);
			if (!Objects.equal(maxSizePerSec, RecoverySettings.this.maxSizePerSec)) {
				logger.info("updating [indices.recovery.max_size_per_sec] from [{}] to [{}]",
						RecoverySettings.this.maxSizePerSec, maxSizePerSec);
				RecoverySettings.this.maxSizePerSec = maxSizePerSec;
				if (maxSizePerSec.bytes() <= 0) {
					rateLimiter = null;
				} else if (rateLimiter != null) {
					rateLimiter.setMaxRate(maxSizePerSec.mbFrac());
				} else {
					rateLimiter = new RateLimiter(maxSizePerSec.mbFrac());
				}
			}

			ByteSizeValue fileChunkSize = settings.getAsBytesSize("indices.recovery.file_chunk_size",
					RecoverySettings.this.fileChunkSize);
			if (!fileChunkSize.equals(RecoverySettings.this.fileChunkSize)) {
				logger.info("updating [indices.recovery.file_chunk_size] from [{}] to [{}]",
						RecoverySettings.this.fileChunkSize, fileChunkSize);
				RecoverySettings.this.fileChunkSize = fileChunkSize;
			}

			int translogOps = settings.getAsInt("indices.recovery.translog_ops", RecoverySettings.this.translogOps);
			if (translogOps != RecoverySettings.this.translogOps) {
				logger.info("updating [indices.recovery.translog_ops] from [{}] to [{}]",
						RecoverySettings.this.translogOps, translogOps);
				RecoverySettings.this.translogOps = translogOps;
			}

			ByteSizeValue translogSize = settings.getAsBytesSize("indices.recovery.translog_size",
					RecoverySettings.this.translogSize);
			if (!translogSize.equals(RecoverySettings.this.translogSize)) {
				logger.info("updating [indices.recovery.translog_size] from [{}] to [{}]",
						RecoverySettings.this.translogSize, translogSize);
				RecoverySettings.this.translogSize = translogSize;
			}

			boolean compress = settings.getAsBoolean("indices.recovery.compress", RecoverySettings.this.compress);
			if (compress != RecoverySettings.this.compress) {
				logger.info("updating [indices.recovery.compress] from [{}] to [{}]", RecoverySettings.this.compress,
						compress);
				RecoverySettings.this.compress = compress;
			}

			int concurrentStreams = settings.getAsInt("indices.recovery.concurrent_streams",
					RecoverySettings.this.concurrentStreams);
			if (concurrentStreams != RecoverySettings.this.concurrentStreams) {
				logger.info("updating [indices.recovery.concurrent_streams] from [{}] to [{}]",
						RecoverySettings.this.concurrentStreams, concurrentStreams);
				RecoverySettings.this.concurrentStreams = concurrentStreams;
				RecoverySettings.this.concurrentStreamPool.setMaximumPoolSize(concurrentStreams);
			}
		}
	}
}
