/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RecoveryTarget.java 2012-3-29 15:02:03 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices.recovery;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.IndexOutput;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.StopWatch;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.stream.VoidStreamable;
import cn.com.rebirth.search.core.index.IndexShardMissingException;
import cn.com.rebirth.search.core.index.engine.RecoveryEngineException;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.IllegalIndexShardStateException;
import cn.com.rebirth.search.core.index.shard.IndexShardClosedException;
import cn.com.rebirth.search.core.index.shard.IndexShardNotStartedException;
import cn.com.rebirth.search.core.index.shard.IndexShardState;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.shard.service.InternalIndexShard;
import cn.com.rebirth.search.core.index.store.Store;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.indices.IndicesLifecycle;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.ConnectTransportException;
import cn.com.rebirth.search.core.transport.FutureTransportResponseHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Sets;


/**
 * The Class RecoveryTarget.
 *
 * @author l.xue.nong
 */
public class RecoveryTarget extends AbstractComponent {

	
	/**
	 * The Class Actions.
	 *
	 * @author l.xue.nong
	 */
	public static class Actions {

		
		/** The Constant FILES_INFO. */
		public static final String FILES_INFO = "index/shard/recovery/filesInfo";

		
		/** The Constant FILE_CHUNK. */
		public static final String FILE_CHUNK = "index/shard/recovery/fileChunk";

		
		/** The Constant CLEAN_FILES. */
		public static final String CLEAN_FILES = "index/shard/recovery/cleanFiles";

		
		/** The Constant TRANSLOG_OPS. */
		public static final String TRANSLOG_OPS = "index/shard/recovery/translogOps";

		
		/** The Constant PREPARE_TRANSLOG. */
		public static final String PREPARE_TRANSLOG = "index/shard/recovery/prepareTranslog";

		
		/** The Constant FINALIZE. */
		public static final String FINALIZE = "index/shard/recovery/finalize";
	}

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The transport service. */
	private final TransportService transportService;

	
	/** The indices service. */
	private final IndicesService indicesService;

	
	/** The recovery settings. */
	private final RecoverySettings recoverySettings;

	
	/** The on going recoveries. */
	private final ConcurrentMap<ShardId, RecoveryStatus> onGoingRecoveries = ConcurrentCollections.newConcurrentMap();

	
	/**
	 * Instantiates a new recovery target.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 * @param indicesLifecycle the indices lifecycle
	 * @param recoverySettings the recovery settings
	 */
	@Inject
	public RecoveryTarget(Settings settings, ThreadPool threadPool, TransportService transportService,
			IndicesService indicesService, IndicesLifecycle indicesLifecycle, RecoverySettings recoverySettings) {
		super(settings);
		this.threadPool = threadPool;
		this.transportService = transportService;
		this.indicesService = indicesService;
		this.recoverySettings = recoverySettings;

		transportService.registerHandler(Actions.FILES_INFO, new FilesInfoRequestHandler());
		transportService.registerHandler(Actions.FILE_CHUNK, new FileChunkTransportRequestHandler());
		transportService.registerHandler(Actions.CLEAN_FILES, new CleanFilesRequestHandler());
		transportService.registerHandler(Actions.PREPARE_TRANSLOG, new PrepareForTranslogOperationsRequestHandler());
		transportService.registerHandler(Actions.TRANSLOG_OPS, new TranslogOperationsRequestHandler());
		transportService.registerHandler(Actions.FINALIZE, new FinalizeRecoveryRequestHandler());

		indicesLifecycle.addListener(new IndicesLifecycle.Listener() {
			@Override
			public void beforeIndexShardClosed(ShardId shardId, @Nullable IndexShard indexShard, boolean delete) {
				removeAndCleanOnGoingRecovery(shardId);
			}
		});
	}

	
	/**
	 * Peer recovery status.
	 *
	 * @param shardId the shard id
	 * @return the recovery status
	 */
	public RecoveryStatus peerRecoveryStatus(ShardId shardId) {
		RecoveryStatus peerRecoveryStatus = onGoingRecoveries.get(shardId);
		if (peerRecoveryStatus == null) {
			return null;
		}
		
		if (peerRecoveryStatus.startTime > 0 && peerRecoveryStatus.stage != RecoveryStatus.Stage.DONE) {
			peerRecoveryStatus.time = System.currentTimeMillis() - peerRecoveryStatus.startTime;
		}
		return peerRecoveryStatus;
	}

	
	/**
	 * Start recovery.
	 *
	 * @param request the request
	 * @param fromRetry the from retry
	 * @param listener the listener
	 */
	public void startRecovery(final StartRecoveryRequest request, final boolean fromRetry,
			final RecoveryListener listener) {
		if (request.sourceNode() == null) {
			listener.onIgnoreRecovery(false, "No node to recover from, retry on next cluster state update");
			return;
		}
		IndexService indexService = indicesService.indexService(request.shardId().index().name());
		if (indexService == null) {
			removeAndCleanOnGoingRecovery(request.shardId());
			listener.onIgnoreRecovery(false, "index missing locally, stop recovery");
			return;
		}
		final InternalIndexShard shard = (InternalIndexShard) indexService.shard(request.shardId().id());
		if (shard == null) {
			removeAndCleanOnGoingRecovery(request.shardId());
			listener.onIgnoreRecovery(false, "shard missing locally, stop recovery");
			return;
		}
		if (!fromRetry) {
			try {
				shard.recovering("from " + request.sourceNode());
			} catch (IllegalIndexShardStateException e) {
				
				listener.onIgnoreRecovery(false, "already in recovering process, " + e.getMessage());
				return;
			}
		}
		if (shard.state() == IndexShardState.CLOSED) {
			removeAndCleanOnGoingRecovery(request.shardId());
			listener.onIgnoreRecovery(false, "local shard closed, stop recovery");
			return;
		}
		threadPool.generic().execute(new Runnable() {
			@Override
			public void run() {
				doRecovery(shard, request, fromRetry, listener);
			}
		});
	}

	
	/**
	 * Do recovery.
	 *
	 * @param shard the shard
	 * @param request the request
	 * @param fromRetry the from retry
	 * @param listener the listener
	 */
	private void doRecovery(final InternalIndexShard shard, final StartRecoveryRequest request,
			final boolean fromRetry, final RecoveryListener listener) {
		if (shard.state() == IndexShardState.CLOSED) {
			removeAndCleanOnGoingRecovery(request.shardId());
			listener.onIgnoreRecovery(false, "local shard closed, stop recovery");
			return;
		}

		RecoveryStatus recovery;
		if (fromRetry) {
			recovery = onGoingRecoveries.get(request.shardId());
		} else {
			recovery = new RecoveryStatus();
			onGoingRecoveries.put(request.shardId(), recovery);
		}

		try {
			logger.trace("[" + request.shardId().index().name() + "][{}] starting recovery from {}", request.shardId()
					.id(), request.sourceNode());

			StopWatch stopWatch = new StopWatch().start();
			RecoveryResponse recoveryStatus = transportService.submitRequest(request.sourceNode(),
					RecoverySource.Actions.START_RECOVERY, request,
					new FutureTransportResponseHandler<RecoveryResponse>() {
						@Override
						public RecoveryResponse newInstance() {
							return new RecoveryResponse();
						}
					}).txGet();
			if (shard.state() == IndexShardState.CLOSED) {
				removeAndCleanOnGoingRecovery(shard.shardId());
				listener.onIgnoreRecovery(false, "local shard closed, stop recovery");
				return;
			}
			stopWatch.stop();
			if (logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append('[').append(request.shardId().index().name()).append(']').append('[')
						.append(request.shardId().id()).append("] ");
				sb.append("recovery completed from ").append(request.sourceNode()).append(", took[")
						.append(stopWatch.totalTime()).append("]\n");
				sb.append("   phase1: recovered_files [").append(recoveryStatus.phase1FileNames.size()).append("]")
						.append(" with total_size of [").append(new ByteSizeValue(recoveryStatus.phase1TotalSize))
						.append("]").append(", took [").append(TimeValue.timeValueMillis(recoveryStatus.phase1Time))
						.append("], throttling_wait [")
						.append(TimeValue.timeValueMillis(recoveryStatus.phase1ThrottlingWaitTime)).append(']')
						.append("\n");
				sb.append("         : reusing_files   [").append(recoveryStatus.phase1ExistingFileNames.size())
						.append("] with total_size of [")
						.append(new ByteSizeValue(recoveryStatus.phase1ExistingTotalSize)).append("]\n");
				sb.append("   phase2: start took [").append(TimeValue.timeValueMillis(recoveryStatus.startTime))
						.append("]\n");
				sb.append("         : recovered [").append(recoveryStatus.phase2Operations).append("]")
						.append(" transaction log operations").append(", took [")
						.append(TimeValue.timeValueMillis(recoveryStatus.phase2Time)).append("]").append("\n");
				sb.append("   phase3: recovered [").append(recoveryStatus.phase3Operations).append("]")
						.append(" transaction log operations").append(", took [")
						.append(TimeValue.timeValueMillis(recoveryStatus.phase3Time)).append("]");
				logger.debug(sb.toString());
			}
			removeAndCleanOnGoingRecovery(request.shardId());
			listener.onRecoveryDone();
		} catch (Exception e) {
			
			if (shard.state() == IndexShardState.CLOSED) {
				removeAndCleanOnGoingRecovery(request.shardId());
				listener.onIgnoreRecovery(false, "local shard closed, stop recovery");
				return;
			}
			Throwable cause = ExceptionsHelper.unwrapCause(e);
			if (cause instanceof RecoveryEngineException) {
				
				cause = cause.getCause();
			}
			
			cause = ExceptionsHelper.unwrapCause(cause);
			if (cause instanceof RecoveryEngineException) {
				
				cause = cause.getCause();
			}

			

			if (cause instanceof IndexShardNotStartedException || cause instanceof IndexMissingException
					|| cause instanceof IndexShardMissingException) {
				
				listener.onRetryRecovery(TimeValue.timeValueMillis(500));
				return;
			}

			

			
			

			removeAndCleanOnGoingRecovery(request.shardId());

			if (cause instanceof ConnectTransportException) {
				listener.onIgnoreRecovery(true, "source node disconnected (" + request.sourceNode() + ")");
				return;
			}

			if (cause instanceof IndexShardClosedException) {
				listener.onIgnoreRecovery(true, "source shard is closed (" + request.sourceNode() + ")");
				return;
			}

			if (cause instanceof AlreadyClosedException) {
				listener.onIgnoreRecovery(true, "source shard is closed (" + request.sourceNode() + ")");
				return;
			}

			logger.trace("[" + request.shardId().index().name() + "][" + request.shardId().id() + "] recovery from ["
					+ request.sourceNode() + "] failed", e);
			listener.onRecoveryFailure(new RecoveryFailedException(request, e), true);
		}
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
		 * On retry recovery.
		 *
		 * @param retryAfter the retry after
		 */
		void onRetryRecovery(TimeValue retryAfter);

		
		/**
		 * On ignore recovery.
		 *
		 * @param removeShard the remove shard
		 * @param reason the reason
		 */
		void onIgnoreRecovery(boolean removeShard, String reason);

		
		/**
		 * On recovery failure.
		 *
		 * @param e the e
		 * @param sendShardFailure the send shard failure
		 */
		void onRecoveryFailure(RecoveryFailedException e, boolean sendShardFailure);
	}

	
	/**
	 * Removes the and clean on going recovery.
	 *
	 * @param shardId the shard id
	 */
	private void removeAndCleanOnGoingRecovery(ShardId shardId) {
		
		RecoveryStatus peerRecoveryStatus = onGoingRecoveries.remove(shardId);
		if (peerRecoveryStatus != null) {
			
			for (Map.Entry<String, IndexOutput> entry : peerRecoveryStatus.openIndexOutputs.entrySet()) {
				synchronized (entry.getValue()) {
					try {
						entry.getValue().close();
					} catch (IOException e) {
						
					}
				}
			}
			peerRecoveryStatus.openIndexOutputs = null;
			peerRecoveryStatus.checksums = null;
		}
	}

	
	/**
	 * The Class PrepareForTranslogOperationsRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	class PrepareForTranslogOperationsRequestHandler extends
			BaseTransportRequestHandler<RecoveryPrepareForTranslogOperationsRequest> {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public RecoveryPrepareForTranslogOperationsRequest newInstance() {
			return new RecoveryPrepareForTranslogOperationsRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.GENERIC;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(RecoveryPrepareForTranslogOperationsRequest request, TransportChannel channel)
				throws Exception {
			InternalIndexShard shard = (InternalIndexShard) indicesService.indexServiceSafe(
					request.shardId().index().name()).shardSafe(request.shardId().id());

			RecoveryStatus onGoingRecovery = onGoingRecoveries.get(shard.shardId());
			if (onGoingRecovery == null) {
				
				throw new IndexShardClosedException(shard.shardId());
			}
			onGoingRecovery.stage = RecoveryStatus.Stage.TRANSLOG;

			shard.performRecoveryPrepareForTranslog();
			channel.sendResponse(VoidStreamable.INSTANCE);
		}
	}

	
	/**
	 * The Class FinalizeRecoveryRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	class FinalizeRecoveryRequestHandler extends BaseTransportRequestHandler<RecoveryFinalizeRecoveryRequest> {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public RecoveryFinalizeRecoveryRequest newInstance() {
			return new RecoveryFinalizeRecoveryRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.GENERIC;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(RecoveryFinalizeRecoveryRequest request, TransportChannel channel) throws Exception {
			InternalIndexShard shard = (InternalIndexShard) indicesService.indexServiceSafe(
					request.shardId().index().name()).shardSafe(request.shardId().id());
			RecoveryStatus peerRecoveryStatus = onGoingRecoveries.get(shard.shardId());
			if (peerRecoveryStatus == null) {
				
				throw new IndexShardClosedException(shard.shardId());
			}
			peerRecoveryStatus.stage = RecoveryStatus.Stage.FINALIZE;
			shard.performRecoveryFinalization(false, peerRecoveryStatus);
			peerRecoveryStatus.time = System.currentTimeMillis() - peerRecoveryStatus.startTime;
			peerRecoveryStatus.stage = RecoveryStatus.Stage.DONE;
			channel.sendResponse(VoidStreamable.INSTANCE);
		}
	}

	
	/**
	 * The Class TranslogOperationsRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	class TranslogOperationsRequestHandler extends BaseTransportRequestHandler<RecoveryTranslogOperationsRequest> {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public RecoveryTranslogOperationsRequest newInstance() {
			return new RecoveryTranslogOperationsRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.GENERIC;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(RecoveryTranslogOperationsRequest request, TransportChannel channel)
				throws Exception {
			InternalIndexShard shard = (InternalIndexShard) indicesService.indexServiceSafe(
					request.shardId().index().name()).shardSafe(request.shardId().id());
			for (Translog.Operation operation : request.operations()) {
				shard.performRecoveryOperation(operation);
			}

			RecoveryStatus onGoingRecovery = onGoingRecoveries.get(shard.shardId());
			if (onGoingRecovery == null) {
				
				throw new IndexShardClosedException(shard.shardId());
			}
			onGoingRecovery.currentTranslogOperations += request.operations().size();

			channel.sendResponse(VoidStreamable.INSTANCE);
		}
	}

	
	/**
	 * The Class FilesInfoRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	class FilesInfoRequestHandler extends BaseTransportRequestHandler<RecoveryFilesInfoRequest> {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public RecoveryFilesInfoRequest newInstance() {
			return new RecoveryFilesInfoRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.GENERIC;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(RecoveryFilesInfoRequest request, TransportChannel channel) throws Exception {
			InternalIndexShard shard = (InternalIndexShard) indicesService.indexServiceSafe(
					request.shardId.index().name()).shardSafe(request.shardId.id());
			RecoveryStatus onGoingRecovery = onGoingRecoveries.get(shard.shardId());
			if (onGoingRecovery == null) {
				
				throw new IndexShardClosedException(shard.shardId());
			}
			onGoingRecovery.phase1FileNames = request.phase1FileNames;
			onGoingRecovery.phase1FileSizes = request.phase1FileSizes;
			onGoingRecovery.phase1ExistingFileNames = request.phase1ExistingFileNames;
			onGoingRecovery.phase1ExistingFileSizes = request.phase1ExistingFileSizes;
			onGoingRecovery.phase1TotalSize = request.phase1TotalSize;
			onGoingRecovery.phase1ExistingTotalSize = request.phase1ExistingTotalSize;
			onGoingRecovery.stage = RecoveryStatus.Stage.INDEX;
			channel.sendResponse(VoidStreamable.INSTANCE);
		}
	}

	
	/**
	 * The Class CleanFilesRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	class CleanFilesRequestHandler extends BaseTransportRequestHandler<RecoveryCleanFilesRequest> {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public RecoveryCleanFilesRequest newInstance() {
			return new RecoveryCleanFilesRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.GENERIC;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(RecoveryCleanFilesRequest request, TransportChannel channel) throws Exception {
			InternalIndexShard shard = (InternalIndexShard) indicesService.indexServiceSafe(
					request.shardId().index().name()).shardSafe(request.shardId().id());
			RecoveryStatus onGoingRecovery = onGoingRecoveries.get(shard.shardId());
			if (onGoingRecovery == null) {
				
				throw new IndexShardClosedException(shard.shardId());
			}

			
			
			
			String suffix = "." + onGoingRecovery.startTime;
			Set<String> filesToRename = Sets.newHashSet();
			for (String existingFile : shard.store().directory().listAll()) {
				if (existingFile.endsWith(suffix)) {
					filesToRename.add(existingFile.substring(0, existingFile.length() - suffix.length()));
				}
			}
			Exception failureToRename = null;
			if (!filesToRename.isEmpty()) {
				
				for (String fileToRename : filesToRename) {
					shard.store().directory().deleteFile(fileToRename);
				}
				for (String fileToRename : filesToRename) {
					
					try {
						shard.store().renameFile(fileToRename + suffix, fileToRename);
					} catch (Exception e) {
						failureToRename = e;
						break;
					}
				}
			}
			if (failureToRename != null) {
				throw failureToRename;
			}
			
			shard.store().writeChecksums(onGoingRecovery.checksums);

			for (String existingFile : shard.store().directory().listAll()) {
				
				if (!request.snapshotFiles().contains(existingFile) && !Store.isChecksum(existingFile)) {
					try {
						shard.store().directory().deleteFile(existingFile);
					} catch (Exception e) {
						
					}
				}
			}
			channel.sendResponse(VoidStreamable.INSTANCE);
		}
	}

	
	/**
	 * The Class FileChunkTransportRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	class FileChunkTransportRequestHandler extends BaseTransportRequestHandler<RecoveryFileChunkRequest> {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public RecoveryFileChunkRequest newInstance() {
			return new RecoveryFileChunkRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.GENERIC;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(final RecoveryFileChunkRequest request, TransportChannel channel) throws Exception {
			InternalIndexShard shard = (InternalIndexShard) indicesService.indexServiceSafe(
					request.shardId().index().name()).shardSafe(request.shardId().id());
			RecoveryStatus onGoingRecovery = onGoingRecoveries.get(shard.shardId());
			if (onGoingRecovery == null) {
				
				throw new IndexShardClosedException(shard.shardId());
			}
			IndexOutput indexOutput;
			if (request.position() == 0) {
				
				onGoingRecovery.checksums.remove(request.name());
				indexOutput = onGoingRecovery.openIndexOutputs.remove(request.name());
				if (indexOutput != null) {
					try {
						indexOutput.close();
					} catch (IOException e) {
						
					}
				}
				
				

				
				
				
				

				String name = request.name();
				if (shard.store().directory().fileExists(name)) {
					name = name + "." + onGoingRecovery.startTime;
				}

				indexOutput = shard.store().createOutputWithNoChecksum(name);

				onGoingRecovery.openIndexOutputs.put(request.name(), indexOutput);
			} else {
				indexOutput = onGoingRecovery.openIndexOutputs.get(request.name());
			}
			if (indexOutput == null) {
				
				throw new IndexShardClosedException(shard.shardId());
			}
			synchronized (indexOutput) {
				try {
					if (recoverySettings.rateLimiter() != null) {
						recoverySettings.rateLimiter().pause(request.content().length());
					}
					indexOutput.writeBytes(request.content().bytes(), request.content().offset(), request.content()
							.length());
					onGoingRecovery.currentFilesSize.addAndGet(request.length());
					if (indexOutput.getFilePointer() == request.length()) {
						
						indexOutput.close();
						
						if (request.checksum() != null) {
							onGoingRecovery.checksums.put(request.name(), request.checksum());
						}
						shard.store().directory().sync(Collections.singleton(request.name()));
						onGoingRecovery.openIndexOutputs.remove(request.name());
					}
				} catch (IOException e) {
					onGoingRecovery.openIndexOutputs.remove(request.name());
					try {
						indexOutput.close();
					} catch (IOException e1) {
						
					}
					throw e;
				}
			}
			channel.sendResponse(VoidStreamable.INSTANCE);
		}
	}
}
