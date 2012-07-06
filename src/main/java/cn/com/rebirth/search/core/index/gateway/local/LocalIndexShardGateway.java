/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LocalIndexShardGateway.java 2012-7-6 14:28:53 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.gateway.local;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.stream.InputStreamStreamInput;
import cn.com.rebirth.search.core.index.gateway.IndexShardGateway;
import cn.com.rebirth.search.core.index.gateway.IndexShardGatewayRecoveryException;
import cn.com.rebirth.search.core.index.gateway.RecoveryStatus;
import cn.com.rebirth.search.core.index.gateway.SnapshotStatus;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.IndexShardState;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.index.translog.TranslogStreams;
import cn.com.rebirth.search.core.index.translog.fs.FsTranslog;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

/**
 * The Class LocalIndexShardGateway.
 *
 * @author l.xue.nong
 */
public class LocalIndexShardGateway extends AbstractIndexShardComponent implements IndexShardGateway {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The index shard. */
	private final InternalIndexShard indexShard;

	/** The recovery status. */
	private final RecoveryStatus recoveryStatus = new RecoveryStatus();

	/** The flush scheduler. */
	private volatile ScheduledFuture flushScheduler;

	/** The sync interval. */
	private final TimeValue syncInterval;

	/**
	 * Instantiates a new local index shard gateway.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param threadPool the thread pool
	 * @param indexShard the index shard
	 */
	@Inject
	public LocalIndexShardGateway(ShardId shardId, @IndexSettings Settings indexSettings, ThreadPool threadPool,
			IndexShard indexShard) {
		super(shardId, indexSettings);
		this.threadPool = threadPool;
		this.indexShard = (InternalIndexShard) indexShard;

		syncInterval = componentSettings.getAsTime("sync", TimeValue.timeValueSeconds(5));
		if (syncInterval.millis() > 0) {
			this.indexShard.translog().syncOnEachOperation(false);
			flushScheduler = threadPool.schedule(syncInterval, ThreadPool.Names.SAME, new Sync());
		} else if (syncInterval.millis() == 0) {
			flushScheduler = null;
			this.indexShard.translog().syncOnEachOperation(true);
		} else {
			flushScheduler = null;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "local";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#recoveryStatus()
	 */
	@Override
	public RecoveryStatus recoveryStatus() {
		return recoveryStatus;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#recover(boolean, cn.com.rebirth.search.core.index.gateway.RecoveryStatus)
	 */
	@Override
	public void recover(boolean indexShouldExists, RecoveryStatus recoveryStatus)
			throws IndexShardGatewayRecoveryException {
		recoveryStatus.index().startTime(System.currentTimeMillis());
		recoveryStatus.updateStage(RecoveryStatus.Stage.INDEX);
		long version = -1;
		long translogId = -1;
		try {
			if (IndexReader.indexExists(indexShard.store().directory())) {
				version = IndexReader.getCurrentVersion(indexShard.store().directory());
				Map<String, String> commitUserData = IndexReader.getCommitUserData(indexShard.store().directory());
				if (commitUserData.containsKey(Translog.TRANSLOG_ID_KEY)) {
					translogId = Long.parseLong(commitUserData.get(Translog.TRANSLOG_ID_KEY));
				} else {
					translogId = version;
				}
			} else if (indexShouldExists) {
				throw new IndexShardGatewayRecoveryException(shardId(),
						"shard allocated for local recovery (post api), should exists, but doesn't");
			}
		} catch (IOException e) {
			throw new IndexShardGatewayRecoveryException(shardId(),
					"Failed to fetch index version after copying it over", e);
		}
		recoveryStatus.index().updateVersion(version);
		recoveryStatus.index().time(System.currentTimeMillis() - recoveryStatus.index().startTime());

		try {
			int numberOfFiles = 0;
			long totalSizeInBytes = 0;
			for (String name : indexShard.store().directory().listAll()) {
				numberOfFiles++;
				totalSizeInBytes += indexShard.store().directory().fileLength(name);
			}
			recoveryStatus.index().files(numberOfFiles, totalSizeInBytes, numberOfFiles, totalSizeInBytes);
		} catch (Exception e) {

		}

		recoveryStatus.start().startTime(System.currentTimeMillis());
		recoveryStatus.updateStage(RecoveryStatus.Stage.START);
		if (translogId == -1) {

			indexShard.start("post recovery from gateway, no translog");

			recoveryStatus.start().time(System.currentTimeMillis() - recoveryStatus.start().startTime());
			recoveryStatus.start().checkIndexTime(indexShard.checkIndexTook());
			return;
		}

		FsTranslog translog = (FsTranslog) indexShard.translog();
		String translogName = "translog-" + translogId;
		String recoverTranslogName = translogName + ".recovering";

		File recoveringTranslogFile = null;
		for (File translogLocation : translog.locations()) {
			File tmpRecoveringFile = new File(translogLocation, recoverTranslogName);
			if (!tmpRecoveringFile.exists()) {
				File tmpTranslogFile = new File(translogLocation, translogName);
				if (tmpTranslogFile.exists()) {
					for (int i = 0; i < 3; i++) {
						if (tmpTranslogFile.renameTo(tmpRecoveringFile)) {
							recoveringTranslogFile = tmpRecoveringFile;
							break;
						}
					}
				}
			} else {
				recoveringTranslogFile = tmpRecoveringFile;
				break;
			}
		}

		if (recoveringTranslogFile == null || !recoveringTranslogFile.exists()) {

			indexShard.start("post recovery from gateway, no translog");

			recoveryStatus.start().time(System.currentTimeMillis() - recoveryStatus.start().startTime());
			recoveryStatus.start().checkIndexTime(indexShard.checkIndexTook());
			return;
		}

		indexShard.performRecoveryPrepareForTranslog();
		recoveryStatus.start().time(System.currentTimeMillis() - recoveryStatus.start().startTime());
		recoveryStatus.start().checkIndexTime(indexShard.checkIndexTook());

		recoveryStatus.translog().startTime(System.currentTimeMillis());
		recoveryStatus.updateStage(RecoveryStatus.Stage.TRANSLOG);
		try {
			InputStreamStreamInput si = new InputStreamStreamInput(new FileInputStream(recoveringTranslogFile));
			while (true) {
				Translog.Operation operation;
				try {
					int opSize = si.readInt();
					operation = TranslogStreams.readTranslogOperation(si);
				} catch (EOFException e) {

					break;
				} catch (IOException e) {

					break;
				}
				recoveryStatus.translog().addTranslogOperations(1);
				indexShard.performRecoveryOperation(operation);
			}
		} catch (Throwable e) {

			indexShard.translog().close(true);
			throw new IndexShardGatewayRecoveryException(shardId, "failed to recover shard", e);
		}
		indexShard.performRecoveryFinalization(true);

		recoveringTranslogFile.delete();

		recoveryStatus.translog().time(System.currentTimeMillis() - recoveryStatus.translog().startTime());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#type()
	 */
	@Override
	public String type() {
		return "local";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#snapshot(cn.com.rebirth.search.core.index.gateway.IndexShardGateway.Snapshot)
	 */
	@Override
	public SnapshotStatus snapshot(Snapshot snapshot) {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#lastSnapshotStatus()
	 */
	@Override
	public SnapshotStatus lastSnapshotStatus() {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#currentSnapshotStatus()
	 */
	@Override
	public SnapshotStatus currentSnapshotStatus() {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#requiresSnapshot()
	 */
	@Override
	public boolean requiresSnapshot() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#requiresSnapshotScheduling()
	 */
	@Override
	public boolean requiresSnapshotScheduling() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.CloseableIndexComponent#close(boolean)
	 */
	@Override
	public void close(boolean delete) {
		if (flushScheduler != null) {
			flushScheduler.cancel(false);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexShardGateway#obtainSnapshotLock()
	 */
	@Override
	public SnapshotLock obtainSnapshotLock() throws Exception {
		return NO_SNAPSHOT_LOCK;
	}

	/**
	 * The Class Sync.
	 *
	 * @author l.xue.nong
	 */
	class Sync implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {

			if (indexShard.state() == IndexShardState.CLOSED) {
				return;
			}
			if (indexShard.state() == IndexShardState.STARTED && indexShard.translog().syncNeeded()) {
				threadPool.executor(ThreadPool.Names.SNAPSHOT).execute(new Runnable() {
					@Override
					public void run() {
						try {
							indexShard.translog().sync();
						} catch (Exception e) {
							if (indexShard.state() == IndexShardState.STARTED) {
								logger.warn("failed to sync translog", e);
							}
						}
						if (indexShard.state() != IndexShardState.CLOSED) {
							flushScheduler = threadPool.schedule(syncInterval, ThreadPool.Names.SAME, Sync.this);
						}
					}
				});
			} else {
				flushScheduler = threadPool.schedule(syncInterval, ThreadPool.Names.SAME, Sync.this);
			}
		}
	}
}
