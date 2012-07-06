/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RecoverySource.java 2012-7-6 14:29:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.recovery;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.lucene.store.IndexInput;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.StopWatch;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.deletionpolicy.SnapshotIndexCommit;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.shard.IllegalIndexShardStateException;
import cn.com.rebirth.search.core.index.shard.IndexShardClosedException;
import cn.com.rebirth.search.core.index.shard.IndexShardState;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;
import cn.com.rebirth.search.core.index.store.StoreFileMetaData;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;
import cn.com.rebirth.search.core.transport.TransportService;
import cn.com.rebirth.search.core.transport.VoidTransportResponseHandler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * The Class RecoverySource.
 *
 * @author l.xue.nong
 */
public class RecoverySource extends AbstractComponent {

	/**
	 * The Class Actions.
	 *
	 * @author l.xue.nong
	 */
	public static class Actions {

		/** The Constant START_RECOVERY. */
		public static final String START_RECOVERY = "index/shard/recovery/startRecovery";
	}

	/** The transport service. */
	private final TransportService transportService;

	/** The indices service. */
	private final IndicesService indicesService;

	/** The recovery settings. */
	private final RecoverySettings recoverySettings;

	/**
	 * Instantiates a new recovery source.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 * @param recoverySettings the recovery settings
	 */
	@Inject
	public RecoverySource(Settings settings, TransportService transportService, IndicesService indicesService,
			RecoverySettings recoverySettings) {
		super(settings);
		this.transportService = transportService;
		this.indicesService = indicesService;

		this.recoverySettings = recoverySettings;

		transportService.registerHandler(Actions.START_RECOVERY, new StartRecoveryTransportRequestHandler());
	}

	/**
	 * Recover.
	 *
	 * @param request the request
	 * @return the recovery response
	 */
	private RecoveryResponse recover(final StartRecoveryRequest request) {
		final InternalIndexShard shard = (InternalIndexShard) indicesService.indexServiceSafe(
				request.shardId().index().name()).shardSafe(request.shardId().id());
		logger.trace("[" + request.shardId().index().name() + "][" + request.shardId().id()
				+ "] starting recovery to {}, mark_as_relocated {}", request.targetNode(), request.markAsRelocated());
		final RecoveryResponse response = new RecoveryResponse();
		shard.recover(new Engine.RecoveryHandler() {
			@Override
			public void phase1(final SnapshotIndexCommit snapshot) throws RebirthException {
				long totalSize = 0;
				long existingTotalSize = 0;
				try {
					StopWatch stopWatch = new StopWatch().start();

					for (String name : snapshot.getFiles()) {
						StoreFileMetaData md = shard.store().metaData(name);
						boolean useExisting = false;
						if (request.existingFiles().containsKey(name)) {

							if (!name.startsWith("segments") && md.isSame(request.existingFiles().get(name))) {
								response.phase1ExistingFileNames.add(name);
								response.phase1ExistingFileSizes.add(md.length());
								existingTotalSize += md.length();
								useExisting = true;
								if (logger.isTraceEnabled()) {
									logger.trace("[" + request.shardId().index().name() + "][" + request.shardId().id()
											+ "] recovery [phase1] to " + request.targetNode() + ": not recovering ["
											+ name + "], exists in local store and has checksum [{}], size [{}]",
											md.checksum(), md.length());
								}
							}
						}
						if (!useExisting) {
							if (request.existingFiles().containsKey(name)) {
								logger.trace("[" + request.shardId().index().name() + "][" + request.shardId().id()
										+ "] recovery [phase1] to " + request.targetNode() + ": recovering [" + name
										+ "], exists in local store, but is different: remote [{}], local [{}]",
										request.existingFiles().get(name), md);
							} else {
								logger.trace("[" + request.shardId().index().name() + "][" + request.shardId().id()
										+ "] recovery [phase1] to {}: recovering [{}], does not exists in remote",
										request.targetNode(), name);
							}
							response.phase1FileNames.add(name);
							response.phase1FileSizes.add(md.length());
						}
						totalSize += md.length();
					}
					response.phase1TotalSize = totalSize;
					response.phase1ExistingTotalSize = existingTotalSize;

					logger.trace("[" + request.shardId().index().name() + "][" + request.shardId().id()
							+ "] recovery [phase1] to " + request.targetNode() + ": recovering_files ["
							+ response.phase1FileNames.size() + "] with total_size [" + new ByteSizeValue(totalSize)
							+ "], reusing_files [{}] with total_size [{}]", response.phase1ExistingFileNames.size(),
							new ByteSizeValue(existingTotalSize));

					RecoveryFilesInfoRequest recoveryInfoFilesRequest = new RecoveryFilesInfoRequest(request.shardId(),
							response.phase1FileNames, response.phase1FileSizes, response.phase1ExistingFileNames,
							response.phase1ExistingFileSizes, response.phase1TotalSize,
							response.phase1ExistingTotalSize);
					transportService.submitRequest(request.targetNode(), RecoveryTarget.Actions.FILES_INFO,
							recoveryInfoFilesRequest, VoidTransportResponseHandler.INSTANCE_SAME).txGet();

					final CountDownLatch latch = new CountDownLatch(response.phase1FileNames.size());
					final AtomicReference<Exception> lastException = new AtomicReference<Exception>();
					for (final String name : response.phase1FileNames) {
						recoverySettings.concurrentStreamPool().execute(new Runnable() {
							@Override
							public void run() {
								IndexInput indexInput = null;
								try {
									final int BUFFER_SIZE = (int) recoverySettings.fileChunkSize().bytes();
									byte[] buf = new byte[BUFFER_SIZE];
									StoreFileMetaData md = shard.store().metaData(name);
									indexInput = snapshot.getDirectory().openInput(name);
									long len = indexInput.length();
									long readCount = 0;
									while (readCount < len) {
										if (shard.state() == IndexShardState.CLOSED) {
											throw new IndexShardClosedException(shard.shardId());
										}
										int toRead = readCount + BUFFER_SIZE > len ? (int) (len - readCount)
												: BUFFER_SIZE;
										long position = indexInput.getFilePointer();

										if (recoverySettings.rateLimiter() != null) {
											recoverySettings.rateLimiter().pause(toRead);
										}

										indexInput.readBytes(buf, 0, toRead, false);
										BytesHolder content = new BytesHolder(buf, 0, toRead);
										transportService.submitRequest(
												request.targetNode(),
												RecoveryTarget.Actions.FILE_CHUNK,
												new RecoveryFileChunkRequest(request.shardId(), name, position, len, md
														.checksum(), content),
												TransportRequestOptions.options()
														.withCompress(recoverySettings.compress()).withLowType(),
												VoidTransportResponseHandler.INSTANCE_SAME).txGet();
										readCount += toRead;
									}
									indexInput.close();
								} catch (Exception e) {
									lastException.set(e);
								} finally {
									if (indexInput != null) {
										try {
											indexInput.close();
										} catch (IOException e) {

										}
									}
									latch.countDown();
								}
							}
						});
					}

					latch.await();

					if (lastException.get() != null) {
						throw lastException.get();
					}

					Set<String> snapshotFiles = Sets.newHashSet(snapshot.getFiles());
					transportService.submitRequest(request.targetNode(), RecoveryTarget.Actions.CLEAN_FILES,
							new RecoveryCleanFilesRequest(shard.shardId(), snapshotFiles),
							VoidTransportResponseHandler.INSTANCE_SAME).txGet();

					stopWatch.stop();
					logger.trace("[" + request.shardId().index().name() + "][" + request.shardId().id()
							+ "] recovery [phase1] to {}: took [{}]", request.targetNode(), stopWatch.totalTime());
					response.phase1Time = stopWatch.totalTime().millis();
				} catch (Throwable e) {
					throw new RecoverFilesRecoveryException(request.shardId(), response.phase1FileNames.size(),
							new ByteSizeValue(totalSize), e);
				}
			}

			@Override
			public void phase2(Translog.Snapshot snapshot) throws RebirthException {
				if (shard.state() == IndexShardState.CLOSED) {
					throw new IndexShardClosedException(request.shardId());
				}
				logger.trace("[" + request.shardId().index().name() + "][{}] recovery [phase2] to {}: start", request
						.shardId().id(), request.targetNode());
				StopWatch stopWatch = new StopWatch().start();
				transportService.submitRequest(request.targetNode(), RecoveryTarget.Actions.PREPARE_TRANSLOG,
						new RecoveryPrepareForTranslogOperationsRequest(request.shardId()),
						VoidTransportResponseHandler.INSTANCE_SAME).txGet();
				stopWatch.stop();
				response.startTime = stopWatch.totalTime().millis();
				logger.trace("[" + request.shardId().index().name() + "][" + request.shardId().id()
						+ "] recovery [phase2] to {}: start took [{}]", request.targetNode(), stopWatch.totalTime());

				logger.trace("[" + request.shardId().index().name()
						+ "][{}] recovery [phase2] to {}: sending transaction log operations", request.shardId().id(),
						request.targetNode());
				stopWatch = new StopWatch().start();
				int totalOperations = sendSnapshot(snapshot);
				stopWatch.stop();
				logger.trace("[" + request.shardId().index().name() + "][" + request.shardId().id()
						+ "] recovery [phase2] to {}: took [{}]", request.targetNode(), stopWatch.totalTime());
				response.phase2Time = stopWatch.totalTime().millis();
				response.phase2Operations = totalOperations;
			}

			@Override
			public void phase3(Translog.Snapshot snapshot) throws RebirthException {
				if (shard.state() == IndexShardState.CLOSED) {
					throw new IndexShardClosedException(request.shardId());
				}
				logger.trace("[" + request.shardId().index().name()
						+ "][{}] recovery [phase3] to {}: sending transaction log operations", request.shardId().id(),
						request.targetNode());
				StopWatch stopWatch = new StopWatch().start();
				int totalOperations = sendSnapshot(snapshot);
				transportService.submitRequest(request.targetNode(), RecoveryTarget.Actions.FINALIZE,
						new RecoveryFinalizeRecoveryRequest(request.shardId()),
						VoidTransportResponseHandler.INSTANCE_SAME).txGet();
				if (request.markAsRelocated()) {

					try {
						shard.relocated("to " + request.targetNode());
					} catch (IllegalIndexShardStateException e) {

					}
				}
				stopWatch.stop();
				logger.trace("[" + request.shardId().index().name() + "][" + request.shardId().id()
						+ "] recovery [phase3] to {}: took [{}]", request.targetNode(), stopWatch.totalTime());
				response.phase3Time = stopWatch.totalTime().millis();
				response.phase3Operations = totalOperations;
			}

			private int sendSnapshot(Translog.Snapshot snapshot) throws RebirthException {
				int ops = 0;
				long size = 0;
				int totalOperations = 0;
				List<Translog.Operation> operations = Lists.newArrayList();
				while (snapshot.hasNext()) {
					if (shard.state() == IndexShardState.CLOSED) {
						throw new IndexShardClosedException(request.shardId());
					}
					Translog.Operation operation = snapshot.next();
					operations.add(operation);
					ops += 1;
					size += operation.estimateSize();
					totalOperations++;
					if (ops >= recoverySettings.translogOps() || size >= recoverySettings.translogSize().bytes()) {

						if (recoverySettings.rateLimiter() != null) {
							recoverySettings.rateLimiter().pause(size);
						}

						RecoveryTranslogOperationsRequest translogOperationsRequest = new RecoveryTranslogOperationsRequest(
								request.shardId(), operations);
						transportService.submitRequest(
								request.targetNode(),
								RecoveryTarget.Actions.TRANSLOG_OPS,
								translogOperationsRequest,
								TransportRequestOptions.options().withCompress(recoverySettings.compress())
										.withLowType(), VoidTransportResponseHandler.INSTANCE_SAME).txGet();
						ops = 0;
						size = 0;
						operations.clear();
					}
				}

				if (!operations.isEmpty()) {
					RecoveryTranslogOperationsRequest translogOperationsRequest = new RecoveryTranslogOperationsRequest(
							request.shardId(), operations);
					transportService.submitRequest(request.targetNode(), RecoveryTarget.Actions.TRANSLOG_OPS,
							translogOperationsRequest,
							TransportRequestOptions.options().withCompress(recoverySettings.compress()).withLowType(),
							VoidTransportResponseHandler.INSTANCE_SAME).txGet();
				}
				return totalOperations;
			}
		});
		return response;
	}

	/**
	 * The Class StartRecoveryTransportRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	class StartRecoveryTransportRequestHandler extends BaseTransportRequestHandler<StartRecoveryRequest> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public StartRecoveryRequest newInstance() {
			return new StartRecoveryRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.GENERIC;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(final StartRecoveryRequest request, final TransportChannel channel)
				throws Exception {
			RecoveryResponse response = recover(request);
			channel.sendResponse(response);
		}
	}
}
