/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexShardGatewayService.java 2012-3-29 15:02:53 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.gateway;

import java.util.concurrent.ScheduledFuture;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.index.CloseableIndexComponent;
import cn.com.rebirth.search.core.index.deletionpolicy.SnapshotIndexCommit;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.engine.EngineException;
import cn.com.rebirth.search.core.index.engine.SnapshotFailedEngineException;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.settings.IndexSettingsService;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.IllegalIndexShardStateException;
import cn.com.rebirth.search.core.index.shard.IndexShardClosedException;
import cn.com.rebirth.search.core.index.shard.IndexShardNotStartedException;
import cn.com.rebirth.search.core.index.shard.IndexShardState;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.threadpool.ThreadPool;


/**
 * The Class IndexShardGatewayService.
 *
 * @author l.xue.nong
 */
public class IndexShardGatewayService extends AbstractIndexShardComponent implements CloseableIndexComponent {

	
	/** The snapshot on close. */
	private final boolean snapshotOnClose;

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The index settings service. */
	private final IndexSettingsService indexSettingsService;

	
	/** The index shard. */
	private final InternalIndexShard indexShard;

	
	/** The shard gateway. */
	private final IndexShardGateway shardGateway;

	
	/** The last index version. */
	private volatile long lastIndexVersion;

	
	/** The last translog id. */
	private volatile long lastTranslogId = -1;

	
	/** The last total translog operations. */
	private volatile int lastTotalTranslogOperations;

	
	/** The last translog length. */
	private volatile long lastTranslogLength;

	
	/** The snapshot interval. */
	private volatile TimeValue snapshotInterval;

	
	/** The snapshot schedule future. */
	private volatile ScheduledFuture snapshotScheduleFuture;

	
	/** The recovery status. */
	private RecoveryStatus recoveryStatus;

	
	/** The snapshot lock. */
	private IndexShardGateway.SnapshotLock snapshotLock;

	
	/** The snapshot runnable. */
	private final SnapshotRunnable snapshotRunnable = new SnapshotRunnable();

	
	/** The apply settings. */
	private final ApplySettings applySettings = new ApplySettings();

	
	/**
	 * Instantiates a new index shard gateway service.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param indexSettingsService the index settings service
	 * @param threadPool the thread pool
	 * @param indexShard the index shard
	 * @param shardGateway the shard gateway
	 */
	@Inject
	public IndexShardGatewayService(ShardId shardId, @IndexSettings Settings indexSettings,
			IndexSettingsService indexSettingsService, ThreadPool threadPool, IndexShard indexShard,
			IndexShardGateway shardGateway) {
		super(shardId, indexSettings);
		this.threadPool = threadPool;
		this.indexSettingsService = indexSettingsService;
		this.indexShard = (InternalIndexShard) indexShard;
		this.shardGateway = shardGateway;

		this.snapshotOnClose = componentSettings.getAsBoolean("snapshot_on_close", true);
		this.snapshotInterval = componentSettings.getAsTime("snapshot_interval", TimeValue.timeValueSeconds(10));

		indexSettingsService.addListener(applySettings);
	}

	static {
		IndexMetaData.addDynamicSettings("index.gateway.snapshot_interval");
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
			TimeValue snapshotInterval = settings.getAsTime("index.gateway.snapshot_interval",
					IndexShardGatewayService.this.snapshotInterval);
			if (!snapshotInterval.equals(IndexShardGatewayService.this.snapshotInterval)) {
				logger.info("updating snapshot_interval from [{}] to [{}]",
						IndexShardGatewayService.this.snapshotInterval, snapshotInterval);
				IndexShardGatewayService.this.snapshotInterval = snapshotInterval;
				if (snapshotScheduleFuture != null) {
					snapshotScheduleFuture.cancel(false);
					snapshotScheduleFuture = null;
				}
				scheduleSnapshotIfNeeded();
			}
		}
	}

	
	/**
	 * Routing state changed.
	 */
	public void routingStateChanged() {
		scheduleSnapshotIfNeeded();
	}

	
	/**
	 * The listener interface for receiving recovery events.
	 * The class that is interested in processing a recovery
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addRecoveryListener<code> method. When
	 * the recovery event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see RecoveryEvent
	 */
	public static interface RecoveryListener {

		
		/**
		 * On recovery done.
		 */
		void onRecoveryDone();

		
		/**
		 * On ignore recovery.
		 *
		 * @param reason the reason
		 */
		void onIgnoreRecovery(String reason);

		
		/**
		 * On recovery failed.
		 *
		 * @param e the e
		 */
		void onRecoveryFailed(IndexShardGatewayRecoveryException e);
	}

	
	/**
	 * Recovery status.
	 *
	 * @return the recovery status
	 */
	public RecoveryStatus recoveryStatus() {
		if (recoveryStatus == null) {
			return recoveryStatus;
		}
		if (recoveryStatus.startTime() > 0 && recoveryStatus.stage() != RecoveryStatus.Stage.DONE) {
			recoveryStatus.time(System.currentTimeMillis() - recoveryStatus.startTime());
		}
		return recoveryStatus;
	}

	
	/**
	 * Snapshot status.
	 *
	 * @return the snapshot status
	 */
	public SnapshotStatus snapshotStatus() {
		SnapshotStatus snapshotStatus = shardGateway.currentSnapshotStatus();
		if (snapshotStatus != null) {
			return snapshotStatus;
		}
		return shardGateway.lastSnapshotStatus();
	}

	
	/**
	 * Recover.
	 *
	 * @param indexShouldExists the index should exists
	 * @param listener the listener
	 * @throws IndexShardGatewayRecoveryException the index shard gateway recovery exception
	 * @throws IgnoreGatewayRecoveryException the ignore gateway recovery exception
	 */
	public void recover(final boolean indexShouldExists, final RecoveryListener listener)
			throws IndexShardGatewayRecoveryException, IgnoreGatewayRecoveryException {
		if (indexShard.state() == IndexShardState.CLOSED) {
			
			listener.onIgnoreRecovery("shard closed");
			return;
		}
		if (!indexShard.routingEntry().primary()) {
			listener.onRecoveryFailed(new IndexShardGatewayRecoveryException(shardId,
					"Trying to recover when the shard is in backup state", null));
			return;
		}
		try {
			indexShard.recovering("from gateway");
		} catch (IllegalIndexShardStateException e) {
			
			listener.onIgnoreRecovery("already in recovering process, " + e.getMessage());
			return;
		}

		threadPool.generic().execute(new Runnable() {
			@Override
			public void run() {
				recoveryStatus = new RecoveryStatus();
				recoveryStatus.updateStage(RecoveryStatus.Stage.INIT);

				try {
					logger.debug("starting recovery from {} ...", shardGateway);
					shardGateway.recover(indexShouldExists, recoveryStatus);

					lastIndexVersion = recoveryStatus.index().version();
					lastTranslogId = -1;
					lastTranslogLength = 0;
					lastTotalTranslogOperations = recoveryStatus.translog().currentTranslogOperations();

					
					if (indexShard.state() != IndexShardState.STARTED) {
						indexShard.start("post recovery from gateway");
					}
					
					indexShard.refresh(new Engine.Refresh(false));

					recoveryStatus.time(System.currentTimeMillis() - recoveryStatus.startTime());
					recoveryStatus.updateStage(RecoveryStatus.Stage.DONE);

					if (logger.isDebugEnabled()) {
						StringBuilder sb = new StringBuilder();
						sb.append("recovery completed from ").append(shardGateway).append(", took [")
								.append(TimeValue.timeValueMillis(recoveryStatus.time())).append("]\n");
						sb.append("    index    : files           [").append(recoveryStatus.index().numberOfFiles())
								.append("] with total_size [")
								.append(new ByteSizeValue(recoveryStatus.index().totalSize())).append("], took[")
								.append(TimeValue.timeValueMillis(recoveryStatus.index().time())).append("]\n");
						sb.append("             : recovered_files [")
								.append(recoveryStatus.index().numberOfRecoveredFiles()).append("] with total_size [")
								.append(new ByteSizeValue(recoveryStatus.index().recoveredTotalSize())).append("]\n");
						sb.append("             : reusing_files   [")
								.append(recoveryStatus.index().numberOfReusedFiles()).append("] with total_size [")
								.append(new ByteSizeValue(recoveryStatus.index().reusedTotalSize())).append("]\n");
						sb.append("    start    : took [")
								.append(TimeValue.timeValueMillis(recoveryStatus.start().time()))
								.append("], check_index [")
								.append(TimeValue.timeValueMillis(recoveryStatus.start().checkIndexTime()))
								.append("]\n");
						sb.append("    translog : number_of_operations [")
								.append(recoveryStatus.translog().currentTranslogOperations()).append("], took [")
								.append(TimeValue.timeValueMillis(recoveryStatus.translog().time())).append("]");
						logger.debug(sb.toString());
					}
					listener.onRecoveryDone();
					scheduleSnapshotIfNeeded();
				} catch (IndexShardGatewayRecoveryException e) {
					if (indexShard.state() == IndexShardState.CLOSED) {
						
						listener.onIgnoreRecovery("shard closed");
						return;
					}
					if ((e.getCause() instanceof IndexShardClosedException)
							|| (e.getCause() instanceof IndexShardNotStartedException)) {
						
						listener.onIgnoreRecovery("shard closed");
						return;
					}
					listener.onRecoveryFailed(e);
				} catch (IndexShardClosedException e) {
					listener.onIgnoreRecovery("shard closed");
				} catch (IndexShardNotStartedException e) {
					listener.onIgnoreRecovery("shard closed");
				} catch (Exception e) {
					if (indexShard.state() == IndexShardState.CLOSED) {
						
						listener.onIgnoreRecovery("shard closed");
						return;
					}
					listener.onRecoveryFailed(new IndexShardGatewayRecoveryException(shardId, "failed recovery", e));
				}
			}
		});
	}

	
	/**
	 * Snapshot.
	 *
	 * @param reason the reason
	 * @throws IndexShardGatewaySnapshotFailedException the index shard gateway snapshot failed exception
	 */
	public synchronized void snapshot(final String reason) throws IndexShardGatewaySnapshotFailedException {
		if (!indexShard.routingEntry().primary()) {
			return;
			
		}
		if (indexShard.routingEntry().relocating()) {
			return;
		}
		if (indexShard.state() == IndexShardState.CREATED) {
			
			return;
		}
		if (indexShard.state() == IndexShardState.RECOVERING) {
			
			return;
		}

		if (snapshotLock == null) {
			try {
				snapshotLock = shardGateway.obtainSnapshotLock();
			} catch (Exception e) {
				logger.warn("failed to obtain snapshot lock, ignoring snapshot", e);
				return;
			}
		}

		try {
			SnapshotStatus snapshotStatus = indexShard.snapshot(new Engine.SnapshotHandler<SnapshotStatus>() {
				@Override
				public SnapshotStatus snapshot(SnapshotIndexCommit snapshotIndexCommit,
						Translog.Snapshot translogSnapshot) throws EngineException {
					if (lastIndexVersion != snapshotIndexCommit.getVersion()
							|| lastTranslogId != translogSnapshot.translogId()
							|| lastTranslogLength < translogSnapshot.length()) {

						logger.debug("snapshot ({}) to {} ...", reason, shardGateway);
						SnapshotStatus snapshotStatus = shardGateway.snapshot(new IndexShardGateway.Snapshot(
								snapshotIndexCommit, translogSnapshot, lastIndexVersion, lastTranslogId,
								lastTranslogLength, lastTotalTranslogOperations));

						lastIndexVersion = snapshotIndexCommit.getVersion();
						lastTranslogId = translogSnapshot.translogId();
						lastTranslogLength = translogSnapshot.length();
						lastTotalTranslogOperations = translogSnapshot.estimatedTotalOperations();
						return snapshotStatus;
					}
					return null;
				}
			});
			if (snapshotStatus != null) {
				if (logger.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder();
					sb.append("snapshot (").append(reason).append(") completed to ").append(shardGateway)
							.append(", took [").append(TimeValue.timeValueMillis(snapshotStatus.time())).append("]\n");
					sb.append("    index    : version [").append(lastIndexVersion).append("], number_of_files [")
							.append(snapshotStatus.index().numberOfFiles()).append("] with total_size [")
							.append(new ByteSizeValue(snapshotStatus.index().totalSize())).append("], took [")
							.append(TimeValue.timeValueMillis(snapshotStatus.index().time())).append("]\n");
					sb.append("    translog : id      [").append(lastTranslogId).append("], number_of_operations [")
							.append(snapshotStatus.translog().expectedNumberOfOperations()).append("], took [")
							.append(TimeValue.timeValueMillis(snapshotStatus.translog().time())).append("]");
					logger.debug(sb.toString());
				}
			}
		} catch (SnapshotFailedEngineException e) {
			if (e.getCause() instanceof IllegalStateException) {
				
			} else {
				throw new IndexShardGatewaySnapshotFailedException(shardId, "Failed to snapshot", e);
			}
		} catch (IllegalIndexShardStateException e) {
			
		} catch (IndexShardGatewaySnapshotFailedException e) {
			throw e;
		} catch (Exception e) {
			throw new IndexShardGatewaySnapshotFailedException(shardId, "Failed to snapshot", e);
		}
	}

	
	/**
	 * Snapshot on close.
	 */
	public void snapshotOnClose() {
		if (shardGateway.requiresSnapshot() && snapshotOnClose) {
			try {
				snapshot("shutdown");
			} catch (Exception e) {
				logger.warn("failed to snapshot on close", e);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.CloseableIndexComponent#close(boolean)
	 */
	public synchronized void close(boolean delete) {
		indexSettingsService.removeListener(applySettings);
		if (snapshotScheduleFuture != null) {
			snapshotScheduleFuture.cancel(true);
			snapshotScheduleFuture = null;
		}
		
		
		if (!indexShard.routingEntry().primary()) {
			delete = false;
		}
		shardGateway.close(delete);
		if (snapshotLock != null) {
			snapshotLock.release();
		}
	}

	
	/**
	 * Schedule snapshot if needed.
	 */
	private synchronized void scheduleSnapshotIfNeeded() {
		if (!shardGateway.requiresSnapshot()) {
			return;
		}
		if (!shardGateway.requiresSnapshotScheduling()) {
			return;
		}
		if (!indexShard.routingEntry().primary()) {
			
			return;
		}
		if (!indexShard.routingEntry().started()) {
			
			return;
		}
		if (snapshotScheduleFuture != null) {
			
			return;
		}
		if (snapshotInterval.millis() != -1) {
			
			if (logger.isDebugEnabled()) {
				logger.debug("scheduling snapshot every [{}]", snapshotInterval);
			}
			snapshotScheduleFuture = threadPool.schedule(snapshotInterval, ThreadPool.Names.SNAPSHOT, snapshotRunnable);
		}
	}

	
	/**
	 * The Class SnapshotRunnable.
	 *
	 * @author l.xue.nong
	 */
	private class SnapshotRunnable implements Runnable {

		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public synchronized void run() {
			try {
				snapshot("scheduled");
			} catch (Throwable e) {
				if (indexShard.state() == IndexShardState.CLOSED) {
					return;
				}
				logger.warn("failed to snapshot (scheduled)", e);
			}
			
			if (indexShard.state() != IndexShardState.CLOSED) {
				snapshotScheduleFuture = threadPool.schedule(snapshotInterval, ThreadPool.Names.SNAPSHOT, this);
			}
		}
	}
}
