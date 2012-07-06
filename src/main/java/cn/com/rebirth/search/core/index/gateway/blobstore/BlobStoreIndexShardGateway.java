/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BlobStoreIndexShardGateway.java 2012-3-29 15:02:26 l.xue.nong$$
 */
package cn.com.rebirth.search.core.index.gateway.blobstore;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.blobstore.BlobContainer;
import cn.com.rebirth.search.commons.blobstore.BlobMetaData;
import cn.com.rebirth.search.commons.blobstore.BlobPath;
import cn.com.rebirth.search.commons.blobstore.BlobStore;
import cn.com.rebirth.search.commons.blobstore.ImmutableBlobContainer;
import cn.com.rebirth.search.commons.io.FastByteArrayInputStream;
import cn.com.rebirth.search.commons.io.FastByteArrayOutputStream;
import cn.com.rebirth.search.commons.io.stream.BytesStreamInput;
import cn.com.rebirth.search.commons.lucene.store.InputStreamIndexInput;
import cn.com.rebirth.search.commons.lucene.store.ThreadSafeInputStreamIndexInput;
import cn.com.rebirth.search.core.index.deletionpolicy.SnapshotIndexCommit;
import cn.com.rebirth.search.core.index.gateway.CommitPoint;
import cn.com.rebirth.search.core.index.gateway.CommitPoints;
import cn.com.rebirth.search.core.index.gateway.IndexGateway;
import cn.com.rebirth.search.core.index.gateway.IndexShardGateway;
import cn.com.rebirth.search.core.index.gateway.IndexShardGatewayRecoveryException;
import cn.com.rebirth.search.core.index.gateway.IndexShardGatewaySnapshotFailedException;
import cn.com.rebirth.search.core.index.gateway.RecoveryStatus;
import cn.com.rebirth.search.core.index.gateway.SnapshotStatus;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;
import cn.com.rebirth.search.core.index.store.Store;
import cn.com.rebirth.search.core.index.store.StoreFileMetaData;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.index.translog.TranslogStreams;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * The Class BlobStoreIndexShardGateway.
 *
 * @author l.xue.nong
 */
public abstract class BlobStoreIndexShardGateway extends AbstractIndexShardComponent implements IndexShardGateway {

	/** The thread pool. */
	protected final ThreadPool threadPool;

	/** The index shard. */
	protected final InternalIndexShard indexShard;

	/** The store. */
	protected final Store store;

	/** The chunk size. */
	protected final ByteSizeValue chunkSize;

	/** The blob store. */
	protected final BlobStore blobStore;

	/** The shard path. */
	protected final BlobPath shardPath;

	/** The blob container. */
	protected final ImmutableBlobContainer blobContainer;

	/** The recovery status. */
	private volatile RecoveryStatus recoveryStatus;

	/** The last snapshot status. */
	private volatile SnapshotStatus lastSnapshotStatus;

	/** The current snapshot status. */
	private volatile SnapshotStatus currentSnapshotStatus;

	/**
	 * Instantiates a new blob store index shard gateway.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param threadPool the thread pool
	 * @param indexGateway the index gateway
	 * @param indexShard the index shard
	 * @param store the store
	 */
	protected BlobStoreIndexShardGateway(ShardId shardId, @IndexSettings Settings indexSettings, ThreadPool threadPool,
			IndexGateway indexGateway, IndexShard indexShard, Store store) {
		super(shardId, indexSettings);

		this.threadPool = threadPool;
		this.indexShard = (InternalIndexShard) indexShard;
		this.store = store;

		BlobStoreIndexGateway blobStoreIndexGateway = (BlobStoreIndexGateway) indexGateway;

		this.chunkSize = blobStoreIndexGateway.chunkSize();
		this.blobStore = blobStoreIndexGateway.blobStore();
		this.shardPath = blobStoreIndexGateway.shardPath(shardId.id());

		this.blobContainer = blobStore.immutableBlobContainer(shardPath);

		this.recoveryStatus = new RecoveryStatus();
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.gateway.IndexShardGateway#recoveryStatus()
	 */
	@Override
	public RecoveryStatus recoveryStatus() {
		return this.recoveryStatus;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return type() + "://" + blobStore + "/" + shardPath;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.gateway.IndexShardGateway#requiresSnapshot()
	 */
	@Override
	public boolean requiresSnapshot() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.gateway.IndexShardGateway#requiresSnapshotScheduling()
	 */
	@Override
	public boolean requiresSnapshotScheduling() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.gateway.IndexShardGateway#obtainSnapshotLock()
	 */
	@Override
	public SnapshotLock obtainSnapshotLock() throws Exception {
		return NO_SNAPSHOT_LOCK;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.CloseableIndexComponent#close(boolean)
	 */
	@Override
	public void close(boolean delete) throws RestartException {
		if (delete) {
			blobStore.delete(shardPath);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.gateway.IndexShardGateway#lastSnapshotStatus()
	 */
	@Override
	public SnapshotStatus lastSnapshotStatus() {
		return this.lastSnapshotStatus;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.gateway.IndexShardGateway#currentSnapshotStatus()
	 */
	@Override
	public SnapshotStatus currentSnapshotStatus() {
		SnapshotStatus snapshotStatus = this.currentSnapshotStatus;
		if (snapshotStatus == null) {
			return snapshotStatus;
		}
		if (snapshotStatus.stage() != SnapshotStatus.Stage.DONE
				|| snapshotStatus.stage() != SnapshotStatus.Stage.FAILURE) {
			snapshotStatus.time(System.currentTimeMillis() - snapshotStatus.startTime());
		}
		return snapshotStatus;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.gateway.IndexShardGateway#snapshot(cn.com.summall.search.core.index.gateway.IndexShardGateway.Snapshot)
	 */
	@Override
	public SnapshotStatus snapshot(final Snapshot snapshot) throws IndexShardGatewaySnapshotFailedException {
		currentSnapshotStatus = new SnapshotStatus();
		currentSnapshotStatus.startTime(System.currentTimeMillis());

		try {
			doSnapshot(snapshot);
			currentSnapshotStatus.time(System.currentTimeMillis() - currentSnapshotStatus.startTime());
			currentSnapshotStatus.updateStage(SnapshotStatus.Stage.DONE);
		} catch (Exception e) {
			currentSnapshotStatus.time(System.currentTimeMillis() - currentSnapshotStatus.startTime());
			currentSnapshotStatus.updateStage(SnapshotStatus.Stage.FAILURE);
			currentSnapshotStatus.failed(e);
			if (e instanceof IndexShardGatewaySnapshotFailedException) {
				throw (IndexShardGatewaySnapshotFailedException) e;
			} else {
				throw new IndexShardGatewaySnapshotFailedException(shardId, e.getMessage(), e);
			}
		} finally {
			this.lastSnapshotStatus = currentSnapshotStatus;
			this.currentSnapshotStatus = null;
		}
		return this.lastSnapshotStatus;
	}

	/**
	 * Do snapshot.
	 *
	 * @param snapshot the snapshot
	 * @throws IndexShardGatewaySnapshotFailedException the index shard gateway snapshot failed exception
	 */
	private void doSnapshot(final Snapshot snapshot) throws IndexShardGatewaySnapshotFailedException {
		ImmutableMap<String, BlobMetaData> blobs;
		try {
			blobs = blobContainer.listBlobs();
		} catch (IOException e) {
			throw new IndexShardGatewaySnapshotFailedException(shardId, "failed to list blobs", e);
		}

		long generation = findLatestFileNameGeneration(blobs);
		CommitPoints commitPoints = buildCommitPoints(blobs);

		currentSnapshotStatus.index().startTime(System.currentTimeMillis());
		currentSnapshotStatus.updateStage(SnapshotStatus.Stage.INDEX);

		final SnapshotIndexCommit snapshotIndexCommit = snapshot.indexCommit();
		final Translog.Snapshot translogSnapshot = snapshot.translogSnapshot();

		final CountDownLatch indexLatch = new CountDownLatch(snapshotIndexCommit.getFiles().length);
		final CopyOnWriteArrayList<Throwable> failures = new CopyOnWriteArrayList<Throwable>();
		final List<CommitPoint.FileInfo> indexCommitPointFiles = Lists.newArrayList();

		int indexNumberOfFiles = 0;
		long indexTotalFilesSize = 0;
		for (final String fileName : snapshotIndexCommit.getFiles()) {
			StoreFileMetaData md;
			try {
				md = store.metaData(fileName);
			} catch (IOException e) {
				throw new IndexShardGatewaySnapshotFailedException(shardId, "Failed to get store file metadata", e);
			}

			boolean snapshotRequired = false;
			if (snapshot.indexChanged() && fileName.equals(snapshotIndexCommit.getSegmentsFileName())) {
				snapshotRequired = true;
			}

			CommitPoint.FileInfo fileInfo = commitPoints.findPhysicalIndexFile(fileName);
			if (fileInfo == null || !fileInfo.isSame(md) || !commitPointFileExistsInBlobs(fileInfo, blobs)) {

				snapshotRequired = true;
			}

			if (snapshotRequired) {
				indexNumberOfFiles++;
				indexTotalFilesSize += md.length();

				try {
					CommitPoint.FileInfo snapshotFileInfo = new CommitPoint.FileInfo(
							fileNameFromGeneration(++generation), fileName, md.length(), md.checksum());
					indexCommitPointFiles.add(snapshotFileInfo);
					snapshotFile(snapshotIndexCommit.getDirectory(), snapshotFileInfo, indexLatch, failures);
				} catch (IOException e) {
					failures.add(e);
					indexLatch.countDown();
				}
			} else {
				indexCommitPointFiles.add(fileInfo);
				indexLatch.countDown();
			}
		}
		currentSnapshotStatus.index().files(indexNumberOfFiles, indexTotalFilesSize);

		try {
			indexLatch.await();
		} catch (InterruptedException e) {
			failures.add(e);
		}
		if (!failures.isEmpty()) {
			throw new IndexShardGatewaySnapshotFailedException(shardId(), "Failed to perform snapshot (index files)",
					failures.get(failures.size() - 1));
		}

		currentSnapshotStatus.index().time(System.currentTimeMillis() - currentSnapshotStatus.index().startTime());

		currentSnapshotStatus.updateStage(SnapshotStatus.Stage.TRANSLOG);
		currentSnapshotStatus.translog().startTime(System.currentTimeMillis());

		List<CommitPoint.FileInfo> translogCommitPointFiles = Lists.newArrayList();
		int expectedNumberOfOperations = 0;
		boolean snapshotRequired = false;
		if (snapshot.newTranslogCreated()) {
			if (translogSnapshot.lengthInBytes() > 0) {
				snapshotRequired = true;
				expectedNumberOfOperations = translogSnapshot.estimatedTotalOperations();
			}
		} else {

			if (!commitPoints.commits().isEmpty()) {
				CommitPoint commitPoint = commitPoints.commits().get(0);
				boolean allTranslogFilesExists = true;
				for (CommitPoint.FileInfo fileInfo : commitPoint.translogFiles()) {
					if (!commitPointFileExistsInBlobs(fileInfo, blobs)) {
						allTranslogFilesExists = false;
						break;
					}
				}

				if (allTranslogFilesExists) {
					translogCommitPointFiles.addAll(commitPoint.translogFiles());
					if (snapshot.sameTranslogNewOperations()) {
						translogSnapshot.seekForward(snapshot.lastTranslogLength());
						if (translogSnapshot.lengthInBytes() > 0) {
							snapshotRequired = true;
							expectedNumberOfOperations = translogSnapshot.estimatedTotalOperations()
									- snapshot.lastTotalTranslogOperations();
						}
					}
				} else {

					if (translogSnapshot.lengthInBytes() > 0) {
						expectedNumberOfOperations = translogSnapshot.estimatedTotalOperations();
						snapshotRequired = true;
					}
				}
			} else {

				if (translogSnapshot.lengthInBytes() > 0) {
					expectedNumberOfOperations = translogSnapshot.estimatedTotalOperations();
					snapshotRequired = true;
				}
			}
		}
		currentSnapshotStatus.translog().expectedNumberOfOperations(expectedNumberOfOperations);

		if (snapshotRequired) {
			CommitPoint.FileInfo addedTranslogFileInfo = new CommitPoint.FileInfo(fileNameFromGeneration(++generation),
					"translog-" + translogSnapshot.translogId(), translogSnapshot.lengthInBytes(), null);
			translogCommitPointFiles.add(addedTranslogFileInfo);
			try {
				snapshotTranslog(translogSnapshot, addedTranslogFileInfo);
			} catch (Exception e) {
				throw new IndexShardGatewaySnapshotFailedException(shardId, "Failed to snapshot translog", e);
			}
		}
		currentSnapshotStatus.translog()
				.time(System.currentTimeMillis() - currentSnapshotStatus.translog().startTime());

		currentSnapshotStatus.updateStage(SnapshotStatus.Stage.FINALIZE);
		long version = 0;
		if (!commitPoints.commits().isEmpty()) {
			version = commitPoints.commits().iterator().next().version() + 1;
		}
		String commitPointName = "commit-" + Long.toString(version, Character.MAX_RADIX);
		CommitPoint commitPoint = new CommitPoint(version, commitPointName, CommitPoint.Type.GENERATED,
				indexCommitPointFiles, translogCommitPointFiles);
		try {
			byte[] commitPointData = CommitPoints.toXContent(commitPoint);
			blobContainer.writeBlob(commitPointName, new FastByteArrayInputStream(commitPointData),
					commitPointData.length);
		} catch (Exception e) {
			throw new IndexShardGatewaySnapshotFailedException(shardId, "Failed to write commit point", e);
		}

		List<CommitPoint> newCommitPointsList = Lists.newArrayList();
		newCommitPointsList.add(commitPoint);
		for (CommitPoint point : commitPoints) {
			if (point.type() == CommitPoint.Type.SAVED) {
				newCommitPointsList.add(point);
			}
		}
		CommitPoints newCommitPoints = new CommitPoints(newCommitPointsList);

		for (String blobName : blobs.keySet()) {
			if (!blobName.startsWith("commit-")) {
				continue;
			}
			long checkedVersion = Long.parseLong(blobName.substring("commit-".length()), Character.MAX_RADIX);
			if (!newCommitPoints.hasVersion(checkedVersion)) {
				try {
					blobContainer.deleteBlob(blobName);
				} catch (IOException e) {

				}
			}
		}

		for (String blobName : blobs.keySet()) {
			String name = blobName;
			if (!name.startsWith("__")) {
				continue;
			}
			if (blobName.contains(".part")) {
				name = blobName.substring(0, blobName.indexOf(".part"));
			}
			if (newCommitPoints.findNameFile(name) == null) {
				try {
					blobContainer.deleteBlob(blobName);
				} catch (IOException e) {

				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.gateway.IndexShardGateway#recover(boolean, cn.com.summall.search.core.index.gateway.RecoveryStatus)
	 */
	@Override
	public void recover(boolean indexShouldExists, RecoveryStatus recoveryStatus)
			throws IndexShardGatewayRecoveryException {
		this.recoveryStatus = recoveryStatus;

		final ImmutableMap<String, BlobMetaData> blobs;
		try {
			blobs = blobContainer.listBlobs();
		} catch (IOException e) {
			throw new IndexShardGatewayRecoveryException(shardId, "Failed to list content of gateway", e);
		}

		List<CommitPoint> commitPointsList = Lists.newArrayList();
		boolean atLeastOneCommitPointExists = false;
		for (String name : blobs.keySet()) {
			if (name.startsWith("commit-")) {
				atLeastOneCommitPointExists = true;
				try {
					commitPointsList.add(CommitPoints.fromXContent(blobContainer.readBlobFully(name)));
				} catch (Exception e) {
					logger.warn("failed to read commit point [{}]", e, name);
				}
			}
		}
		if (atLeastOneCommitPointExists && commitPointsList.isEmpty()) {
			//throw new IndexShardGatewayRecoveryException(shardId, "Commit points exists but none could be loaded", null);
			return;
		}
		CommitPoints commitPoints = new CommitPoints(commitPointsList);

		if (commitPoints.commits().isEmpty()) {

			try {
				indexShard.store().deleteContent();
			} catch (IOException e) {
				logger.warn("failed to clean store before starting shard", e);
			}
			recoveryStatus.index().startTime(System.currentTimeMillis());
			recoveryStatus.index().time(System.currentTimeMillis() - recoveryStatus.index().startTime());
			return;
		}

		for (CommitPoint commitPoint : commitPoints) {
			if (!commitPointExistsInBlobs(commitPoint, blobs)) {
				logger.warn("listed commit_point [{}]/[{}], but not all files exists, ignoring", commitPoint.name(),
						commitPoint.version());
				continue;
			}
			try {
				recoveryStatus.index().startTime(System.currentTimeMillis());
				recoverIndex(commitPoint, blobs);
				recoveryStatus.index().time(System.currentTimeMillis() - recoveryStatus.index().startTime());

				recoverTranslog(commitPoint, blobs);
				return;
			} catch (Exception e) {
				throw new IndexShardGatewayRecoveryException(shardId, "failed to recover commit_point ["
						+ commitPoint.name() + "]/[" + commitPoint.version() + "]", e);
			}
		}
		//throw new IndexShardGatewayRecoveryException(shardId, "No commit point data is available in gateway", null);
	}

	/**
	 * Recover translog.
	 *
	 * @param commitPoint the commit point
	 * @param blobs the blobs
	 * @throws IndexShardGatewayRecoveryException the index shard gateway recovery exception
	 */
	private void recoverTranslog(CommitPoint commitPoint, ImmutableMap<String, BlobMetaData> blobs)
			throws IndexShardGatewayRecoveryException {
		if (commitPoint.translogFiles().isEmpty()) {

			recoveryStatus.start().startTime(System.currentTimeMillis());
			recoveryStatus.updateStage(RecoveryStatus.Stage.START);
			indexShard.start("post recovery from gateway, no translog");
			recoveryStatus.start().time(System.currentTimeMillis() - recoveryStatus.start().startTime());
			recoveryStatus.start().checkIndexTime(indexShard.checkIndexTook());
			return;
		}

		try {
			recoveryStatus.start().startTime(System.currentTimeMillis());
			recoveryStatus.updateStage(RecoveryStatus.Stage.START);
			indexShard.performRecoveryPrepareForTranslog();
			recoveryStatus.start().time(System.currentTimeMillis() - recoveryStatus.start().startTime());
			recoveryStatus.start().checkIndexTime(indexShard.checkIndexTook());

			recoveryStatus.updateStage(RecoveryStatus.Stage.TRANSLOG);
			recoveryStatus.translog().startTime(System.currentTimeMillis());

			final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
			final CountDownLatch latch = new CountDownLatch(1);

			final Iterator<CommitPoint.FileInfo> transIt = commitPoint.translogFiles().iterator();

			blobContainer.readBlob(transIt.next().name(), new BlobContainer.ReadBlobListener() {
				FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
				boolean ignore = false;

				@Override
				public synchronized void onPartial(byte[] data, int offset, int size) throws IOException {
					if (ignore) {
						return;
					}
					bos.write(data, offset, size);

					if (bos.size() < 4) {
						return;
					}
					BytesStreamInput si = new BytesStreamInput(bos.underlyingBytes(), 0, bos.size(), false);
					int position;
					while (true) {
						try {
							position = si.position();
							if (position + 4 > bos.size()) {
								break;
							}
							int opSize = si.readInt();
							int curPos = si.position();
							if ((si.position() + opSize) > bos.size()) {
								break;
							}
							Translog.Operation operation = TranslogStreams.readTranslogOperation(si);
							if ((si.position() - curPos) != opSize) {
								logger.warn("mismatch in size, expected [{}], got [{}]", opSize, si.position() - curPos);
							}
							recoveryStatus.translog().addTranslogOperations(1);
							indexShard.performRecoveryOperation(operation);
							if (si.position() >= bos.size()) {
								position = si.position();
								break;
							}
						} catch (Exception e) {
							logger.warn(
									"failed to retrieve translog after [{}] operations, ignoring the rest, considered corrupted",
									e, recoveryStatus.translog().currentTranslogOperations());
							ignore = true;
							latch.countDown();
							return;
						}
					}

					FastByteArrayOutputStream newBos = new FastByteArrayOutputStream();

					int leftOver = bos.size() - position;
					if (leftOver > 0) {
						newBos.write(bos.underlyingBytes(), position, leftOver);
					}

					bos = newBos;
				}

				@Override
				public synchronized void onCompleted() {
					if (ignore) {
						return;
					}
					if (!transIt.hasNext()) {
						latch.countDown();
						return;
					}
					blobContainer.readBlob(transIt.next().name(), this);
				}

				@Override
				public void onFailure(Throwable t) {
					failure.set(t);
					latch.countDown();
				}
			});

			latch.await();
			if (failure.get() != null) {
				throw failure.get();
			}

			indexShard.performRecoveryFinalization(true);
			recoveryStatus.translog().time(System.currentTimeMillis() - recoveryStatus.translog().startTime());
		} catch (Throwable e) {
			throw new IndexShardGatewayRecoveryException(shardId, "Failed to recover translog", e);
		}
	}

	/**
	 * Recover index.
	 *
	 * @param commitPoint the commit point
	 * @param blobs the blobs
	 * @throws Exception the exception
	 */
	private void recoverIndex(CommitPoint commitPoint, ImmutableMap<String, BlobMetaData> blobs) throws Exception {
		recoveryStatus.updateStage(RecoveryStatus.Stage.INDEX);
		int numberOfFiles = 0;
		long totalSize = 0;
		int numberOfReusedFiles = 0;
		long reusedTotalSize = 0;

		List<CommitPoint.FileInfo> filesToRecover = Lists.newArrayList();
		for (CommitPoint.FileInfo fileInfo : commitPoint.indexFiles()) {
			String fileName = fileInfo.physicalName();
			StoreFileMetaData md = null;
			try {
				md = store.metaData(fileName);
			} catch (Exception e) {

			}

			if (!fileName.startsWith("segments") && md != null && fileInfo.isSame(md)) {
				numberOfFiles++;
				totalSize += md.length();
				numberOfReusedFiles++;
				reusedTotalSize += md.length();
				if (logger.isTraceEnabled()) {
					logger.trace("not_recovering [{}], exists in local store and is same", fileInfo.physicalName());
				}
			} else {
				if (logger.isTraceEnabled()) {
					if (md == null) {
						logger.trace("recovering [{}], does not exists in local store", fileInfo.physicalName());
					} else {
						logger.trace("recovering [{}], exists in local store but is different", fileInfo.physicalName());
					}
				}
				numberOfFiles++;
				totalSize += fileInfo.length();
				filesToRecover.add(fileInfo);
			}
		}

		recoveryStatus.index().files(numberOfFiles, totalSize, numberOfReusedFiles, reusedTotalSize);
		if (filesToRecover.isEmpty()) {
			logger.trace("no files to recover, all exists within the local store");
		}

		if (logger.isTraceEnabled()) {
			logger.trace(
					"recovering_files [" + numberOfFiles + "] with total_size [" + new ByteSizeValue(totalSize).bytes()
							+ "], reusing_files [{}] with reused_size [{}]", numberOfReusedFiles, new ByteSizeValue(
							reusedTotalSize));
		}

		final CountDownLatch latch = new CountDownLatch(filesToRecover.size());
		final CopyOnWriteArrayList<Throwable> failures = new CopyOnWriteArrayList<Throwable>();

		for (final CommitPoint.FileInfo fileToRecover : filesToRecover) {
			recoverFile(fileToRecover, blobs, latch, failures);
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new IndexShardGatewayRecoveryException(shardId, "Interrupted while recovering index", e);
		}

		if (!failures.isEmpty()) {
			throw new IndexShardGatewayRecoveryException(shardId, "Failed to recover index", failures.get(0));
		}

		long version = -1;
		try {
			if (IndexReader.indexExists(store.directory())) {
				version = IndexReader.getCurrentVersion(store.directory());
			}
		} catch (IOException e) {
			throw new IndexShardGatewayRecoveryException(shardId(),
					"Failed to fetch index version after copying it over", e);
		}
		recoveryStatus.index().updateVersion(version);

		try {
			for (String storeFile : store.directory().listAll()) {
				if (!commitPoint.containPhysicalIndexFile(storeFile)) {
					try {
						store.directory().deleteFile(storeFile);
					} catch (Exception e) {

					}
				}
			}
		} catch (Exception e) {

		}
	}

	/**
	 * Recover file.
	 *
	 * @param fileInfo the file info
	 * @param blobs the blobs
	 * @param latch the latch
	 * @param failures the failures
	 */
	private void recoverFile(final CommitPoint.FileInfo fileInfo, final ImmutableMap<String, BlobMetaData> blobs,
			final CountDownLatch latch, final List<Throwable> failures) {
		final IndexOutput indexOutput;
		try {

			indexOutput = store.createOutputWithNoChecksum(fileInfo.physicalName());
		} catch (IOException e) {
			failures.add(e);
			latch.countDown();
			return;
		}

		String firstFileToRecover = fileInfo.name();
		if (!blobs.containsKey(fileInfo.name())) {

			firstFileToRecover = fileInfo.name() + ".part0";
		}
		if (!blobs.containsKey(firstFileToRecover)) {

			logger.warn("no file [{}]/[{}] to recover, ignoring it", fileInfo.name(), fileInfo.physicalName());
			latch.countDown();
			return;
		}
		final AtomicInteger partIndex = new AtomicInteger();

		blobContainer.readBlob(firstFileToRecover, new BlobContainer.ReadBlobListener() {
			@Override
			public synchronized void onPartial(byte[] data, int offset, int size) throws IOException {
				recoveryStatus.index().addCurrentFilesSize(size);
				indexOutput.writeBytes(data, offset, size);
			}

			@Override
			public synchronized void onCompleted() {
				int part = partIndex.incrementAndGet();
				String partName = fileInfo.name() + ".part" + part;
				if (blobs.containsKey(partName)) {

					blobContainer.readBlob(partName, this);
					return;
				} else {

					try {
						indexOutput.close();

						if (fileInfo.checksum() != null) {
							store.writeChecksum(fileInfo.physicalName(), fileInfo.checksum());
						}
						store.directory().sync(Collections.singleton(fileInfo.physicalName()));
					} catch (IOException e) {
						onFailure(e);
						return;
					}
				}
				latch.countDown();
			}

			@Override
			public void onFailure(Throwable t) {
				failures.add(t);
				latch.countDown();
			}
		});
	}

	/**
	 * Snapshot translog.
	 *
	 * @param snapshot the snapshot
	 * @param fileInfo the file info
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void snapshotTranslog(Translog.Snapshot snapshot, CommitPoint.FileInfo fileInfo) throws IOException {
		blobContainer.writeBlob(fileInfo.name(), snapshot.stream(), snapshot.lengthInBytes());

	}

	/**
	 * Snapshot file.
	 *
	 * @param dir the dir
	 * @param fileInfo the file info
	 * @param latch the latch
	 * @param failures the failures
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void snapshotFile(Directory dir, final CommitPoint.FileInfo fileInfo, final CountDownLatch latch,
			final List<Throwable> failures) throws IOException {
		long chunkBytes = Long.MAX_VALUE;
		if (chunkSize != null) {
			chunkBytes = chunkSize.bytes();
		}

		long totalLength = fileInfo.length();
		long numberOfChunks = totalLength / chunkBytes;
		if (totalLength % chunkBytes > 0) {
			numberOfChunks++;
		}
		if (numberOfChunks == 0) {
			numberOfChunks++;
		}

		final long fNumberOfChunks = numberOfChunks;
		final AtomicLong counter = new AtomicLong(numberOfChunks);
		for (long i = 0; i < fNumberOfChunks; i++) {
			final long partNumber = i;

			IndexInput indexInput = null;
			try {
				indexInput = dir.openInput(fileInfo.physicalName());
				indexInput.seek(partNumber * chunkBytes);
				InputStreamIndexInput is = new ThreadSafeInputStreamIndexInput(indexInput, chunkBytes);

				String blobName = fileInfo.name();
				if (fNumberOfChunks > 1) {

					blobName += ".part" + partNumber;
				}

				final IndexInput fIndexInput = indexInput;
				blobContainer.writeBlob(blobName, is, is.actualSizeToRead(),
						new ImmutableBlobContainer.WriterListener() {
							@Override
							public void onCompleted() {
								try {
									fIndexInput.close();
								} catch (IOException e) {

								}
								if (counter.decrementAndGet() == 0) {
									latch.countDown();
								}
							}

							@Override
							public void onFailure(Throwable t) {
								try {
									fIndexInput.close();
								} catch (IOException e) {

								}
								failures.add(t);
								if (counter.decrementAndGet() == 0) {
									latch.countDown();
								}
							}
						});
			} catch (Exception e) {
				if (indexInput != null) {
					try {
						indexInput.close();
					} catch (IOException e1) {

					}
				}
				failures.add(e);
				latch.countDown();
			}
		}
	}

	/**
	 * Commit point exists in blobs.
	 *
	 * @param commitPoint the commit point
	 * @param blobs the blobs
	 * @return true, if successful
	 */
	private boolean commitPointExistsInBlobs(CommitPoint commitPoint, ImmutableMap<String, BlobMetaData> blobs) {
		for (CommitPoint.FileInfo fileInfo : Iterables.concat(commitPoint.indexFiles(), commitPoint.translogFiles())) {
			if (!commitPointFileExistsInBlobs(fileInfo, blobs)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Commit point file exists in blobs.
	 *
	 * @param fileInfo the file info
	 * @param blobs the blobs
	 * @return true, if successful
	 */
	private boolean commitPointFileExistsInBlobs(CommitPoint.FileInfo fileInfo, ImmutableMap<String, BlobMetaData> blobs) {
		BlobMetaData blobMetaData = blobs.get(fileInfo.name());
		if (blobMetaData != null) {
			if (blobMetaData.length() != fileInfo.length()) {
				return false;
			}
		} else if (blobs.containsKey(fileInfo.name() + ".part0")) {

			int part = 0;
			long totalSize = 0;
			while (true) {
				blobMetaData = blobs.get(fileInfo.name() + ".part" + part++);
				if (blobMetaData == null) {
					break;
				}
				totalSize += blobMetaData.length();
			}
			if (totalSize != fileInfo.length()) {
				return false;
			}
		} else {

			return false;
		}
		return true;
	}

	/**
	 * Builds the commit points.
	 *
	 * @param blobs the blobs
	 * @return the commit points
	 */
	private CommitPoints buildCommitPoints(ImmutableMap<String, BlobMetaData> blobs) {
		List<CommitPoint> commitPoints = Lists.newArrayList();
		for (String name : blobs.keySet()) {
			if (name.startsWith("commit-")) {
				try {
					commitPoints.add(CommitPoints.fromXContent(blobContainer.readBlobFully(name)));
				} catch (Exception e) {
					logger.warn("failed to read commit point [{}]", e, name);
				}
			}
		}
		return new CommitPoints(commitPoints);
	}

	/**
	 * File name from generation.
	 *
	 * @param generation the generation
	 * @return the string
	 */
	private String fileNameFromGeneration(long generation) {
		return "__" + Long.toString(generation, Character.MAX_RADIX);
	}

	/**
	 * Find latest file name generation.
	 *
	 * @param blobs the blobs
	 * @return the long
	 */
	private long findLatestFileNameGeneration(ImmutableMap<String, BlobMetaData> blobs) {
		long generation = -1;
		for (String name : blobs.keySet()) {
			if (!name.startsWith("__")) {
				continue;
			}
			if (name.contains(".part")) {
				name = name.substring(0, name.indexOf(".part"));
			}

			try {
				long currentGen = Long.parseLong(name.substring(2), Character.MAX_RADIX);
				if (currentGen > generation) {
					generation = currentGen;
				}
			} catch (NumberFormatException e) {
				logger.warn("file [{}] does not conform to the '__' schema");
			}
		}
		return generation;
	}
}
