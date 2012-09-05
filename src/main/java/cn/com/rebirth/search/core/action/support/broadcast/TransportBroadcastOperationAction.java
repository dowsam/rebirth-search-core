/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportBroadcastOperationAction.java 2012-7-6 14:30:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.broadcast;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.GroupShardsIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportBroadcastOperationAction.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @param <ShardRequest> the generic type
 * @param <ShardResponse> the generic type
 * @author l.xue.nong
 */
public abstract class TransportBroadcastOperationAction<Request extends BroadcastOperationRequest, Response extends BroadcastOperationResponse, ShardRequest extends BroadcastShardOperationRequest, ShardResponse extends BroadcastShardOperationResponse>
		extends TransportAction<Request, Response> {

	/** The cluster service. */
	protected final ClusterService clusterService;

	/** The transport service. */
	protected final TransportService transportService;

	/** The thread pool. */
	protected final ThreadPool threadPool;

	/** The transport action. */
	final String transportAction;

	/** The transport shard action. */
	final String transportShardAction;

	/** The executor. */
	final String executor;

	/**
	 * Instantiates a new transport broadcast operation action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 */
	protected TransportBroadcastOperationAction(Settings settings, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService) {
		super(settings, threadPool);
		this.clusterService = clusterService;
		this.transportService = transportService;
		this.threadPool = threadPool;

		this.transportAction = transportAction();
		this.transportShardAction = transportAction() + "/s";
		this.executor = executor();

		transportService.registerHandler(transportAction, new TransportHandler());
		transportService.registerHandler(transportShardAction, new ShardTransportHandler());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.TransportAction#doExecute(cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(Request request, ActionListener<Response> listener) {
		new AsyncBroadcastAction(request, listener).start();
	}

	/**
	 * Transport action.
	 *
	 * @return the string
	 */
	protected abstract String transportAction();

	/**
	 * Executor.
	 *
	 * @return the string
	 */
	protected abstract String executor();

	/**
	 * New request.
	 *
	 * @return the request
	 */
	protected abstract Request newRequest();

	/**
	 * New response.
	 *
	 * @param request the request
	 * @param shardsResponses the shards responses
	 * @param clusterState the cluster state
	 * @return the response
	 */
	protected abstract Response newResponse(Request request, AtomicReferenceArray shardsResponses,
			ClusterState clusterState);

	/**
	 * New shard request.
	 *
	 * @return the shard request
	 */
	protected abstract ShardRequest newShardRequest();

	/**
	 * New shard request.
	 *
	 * @param shard the shard
	 * @param request the request
	 * @return the shard request
	 */
	protected abstract ShardRequest newShardRequest(ShardRouting shard, Request request);

	/**
	 * New shard response.
	 *
	 * @return the shard response
	 */
	protected abstract ShardResponse newShardResponse();

	/**
	 * Shard operation.
	 *
	 * @param request the request
	 * @return the shard response
	 * @throws RebirthException the rebirth exception
	 */
	protected abstract ShardResponse shardOperation(ShardRequest request) throws RebirthException;

	/**
	 * Shards.
	 *
	 * @param clusterState the cluster state
	 * @param request the request
	 * @param concreteIndices the concrete indices
	 * @return the group shards iterator
	 */
	protected abstract GroupShardsIterator shards(ClusterState clusterState, Request request, String[] concreteIndices);

	/**
	 * Accumulate exceptions.
	 *
	 * @return true, if successful
	 */
	protected boolean accumulateExceptions() {
		return true;
	}

	/**
	 * Ignore exception.
	 *
	 * @param t the t
	 * @return true, if successful
	 */
	protected boolean ignoreException(Throwable t) {
		return false;
	}

	/**
	 * Ignore non active exceptions.
	 *
	 * @return true, if successful
	 */
	protected boolean ignoreNonActiveExceptions() {
		return false;
	}

	/**
	 * Check global block.
	 *
	 * @param state the state
	 * @param request the request
	 * @return the cluster block exception
	 */
	protected abstract ClusterBlockException checkGlobalBlock(ClusterState state, Request request);

	/**
	 * Check request block.
	 *
	 * @param state the state
	 * @param request the request
	 * @param concreteIndices the concrete indices
	 * @return the cluster block exception
	 */
	protected abstract ClusterBlockException checkRequestBlock(ClusterState state, Request request,
			String[] concreteIndices);

	/**
	 * The Class AsyncBroadcastAction.
	 *
	 * @author l.xue.nong
	 */
	class AsyncBroadcastAction {

		/** The request. */
		private final Request request;

		/** The listener. */
		private final ActionListener<Response> listener;

		/** The cluster state. */
		private final ClusterState clusterState;

		/** The nodes. */
		private final DiscoveryNodes nodes;

		/** The shards its. */
		private final GroupShardsIterator shardsIts;

		/** The expected ops. */
		private final int expectedOps;

		/** The counter ops. */
		private final AtomicInteger counterOps = new AtomicInteger();

		/** The index counter. */
		private final AtomicInteger indexCounter = new AtomicInteger();

		/** The shards responses. */
		private final AtomicReferenceArray shardsResponses;

		/**
		 * Instantiates a new async broadcast action.
		 *
		 * @param request the request
		 * @param listener the listener
		 */
		AsyncBroadcastAction(Request request, ActionListener<Response> listener) {
			this.request = request;
			this.listener = listener;

			clusterState = clusterService.state();

			ClusterBlockException blockException = checkGlobalBlock(clusterState, request);
			if (blockException != null) {
				throw blockException;
			}

			String[] concreteIndices = clusterState.metaData().concreteIndices(request.indices(), false, true);
			blockException = checkRequestBlock(clusterState, request, concreteIndices);
			if (blockException != null) {
				throw blockException;
			}

			nodes = clusterState.nodes();
			shardsIts = shards(clusterState, request, concreteIndices);
			expectedOps = shardsIts.size();

			shardsResponses = new AtomicReferenceArray<Object>(expectedOps);
		}

		/**
		 * Start.
		 */
		public void start() {
			if (shardsIts.size() == 0) {

				listener.onResponse(newResponse(request, new AtomicReferenceArray(0), clusterState));
			}
			request.beforeStart();

			int localOperations = 0;
			for (final ShardIterator shardIt : shardsIts) {
				final ShardRouting shard = shardIt.firstOrNull();
				if (shard != null) {
					if (shard.currentNodeId().equals(nodes.localNodeId())) {
						localOperations++;
					} else {

						performOperation(shardIt, true);
					}
				} else {

					onOperation(null, shardIt, null);
				}
			}

			if (localOperations > 0) {
				if (request.operationThreading() == BroadcastOperationThreading.SINGLE_THREAD) {
					request.beforeLocalFork();
					threadPool.executor(executor).execute(new Runnable() {
						@Override
						public void run() {
							for (final ShardIterator shardIt : shardsIts) {
								final ShardRouting shard = shardIt.firstOrNull();
								if (shard != null) {
									if (shard.currentNodeId().equals(nodes.localNodeId())) {
										performOperation(shardIt, false);
									}
								}
							}
						}
					});
				} else {
					boolean localAsync = request.operationThreading() == BroadcastOperationThreading.THREAD_PER_SHARD;
					if (localAsync) {
						request.beforeLocalFork();
					}
					for (final ShardIterator shardIt : shardsIts) {
						final ShardRouting shard = shardIt.firstOrNull();
						if (shard != null) {
							if (shard.currentNodeId().equals(nodes.localNodeId())) {
								performOperation(shardIt, localAsync);
							}
						}
					}
				}
			}
		}

		/**
		 * Perform operation.
		 *
		 * @param shardIt the shard it
		 * @param localAsync the local async
		 */
		void performOperation(final ShardIterator shardIt, boolean localAsync) {
			performOperation(shardIt, shardIt.nextOrNull(), localAsync);
		}

		/**
		 * Perform operation.
		 *
		 * @param shardIt the shard it
		 * @param shard the shard
		 * @param localAsync the local async
		 */
		void performOperation(final ShardIterator shardIt, final ShardRouting shard, boolean localAsync) {
			if (shard == null) {

				onOperation(null, shardIt, null);
			} else {
				final ShardRequest shardRequest = newShardRequest(shard, request);
				if (shard.currentNodeId().equals(nodes.localNodeId())) {
					if (localAsync) {
						threadPool.executor(executor).execute(new Runnable() {
							@Override
							public void run() {
								try {
									onOperation(shard, shardOperation(shardRequest));
								} catch (Exception e) {
									onOperation(shard, shardIt, e);
								}
							}
						});
					} else {
						try {
							onOperation(shard, shardOperation(shardRequest));
						} catch (Exception e) {
							onOperation(shard, shardIt, e);
						}
					}
				} else {
					DiscoveryNode node = nodes.get(shard.currentNodeId());
					if (node == null) {

						onOperation(shard, shardIt, null);
					} else {
						transportService.sendRequest(node, transportShardAction, shardRequest,
								new BaseTransportResponseHandler<ShardResponse>() {
									@Override
									public ShardResponse newInstance() {
										return newShardResponse();
									}

									@Override
									public String executor() {
										return ThreadPool.Names.SAME;
									}

									@Override
									public void handleResponse(ShardResponse response) {
										onOperation(shard, response);
									}

									@Override
									public void handleException(TransportException e) {
										onOperation(shard, shardIt, e);
									}
								});
					}
				}
			}
		}

		/**
		 * On operation.
		 *
		 * @param shard the shard
		 * @param response the response
		 */
		@SuppressWarnings({ "unchecked" })
		void onOperation(ShardRouting shard, ShardResponse response) {
			shardsResponses.set(indexCounter.getAndIncrement(), response);
			if (expectedOps == counterOps.incrementAndGet()) {
				finishHim();
			}
		}

		/**
		 * On operation.
		 *
		 * @param shard the shard
		 * @param shardIt the shard it
		 * @param t the t
		 */
		@SuppressWarnings({ "unchecked" })
		void onOperation(@Nullable ShardRouting shard, final ShardIterator shardIt, Throwable t) {
			ShardRouting nextShard = shardIt.nextOrNull();
			if (nextShard != null) {
				if (t != null) {

					if (logger.isTraceEnabled()) {
						if (!ignoreException(t)) {
							if (shard != null) {
								logger.trace(shard.shortSummary() + ": Failed to execute [" + request + "]", t);
							} else {
								logger.trace(shardIt.shardId() + ": Failed to execute [" + request + "]", t);
							}
						}
					}
				}

				performOperation(shardIt, nextShard, true);
			} else {

				if (logger.isDebugEnabled()) {
					if (t != null) {
						if (!ignoreException(t)) {
							if (shard != null) {
								logger.debug(shard.shortSummary() + ": Failed to execute [" + request + "]", t);
							} else {
								logger.debug(shardIt.shardId() + ": Failed to execute [" + request + "]", t);
							}
						}
					}
				}

				int index = indexCounter.getAndIncrement();
				if (accumulateExceptions()) {
					if (t == null) {
						if (!ignoreNonActiveExceptions()) {
							t = new BroadcastShardOperationFailedException(shardIt.shardId(), "No active shard(s)");
						}
					} else {
						if (ignoreException(t)) {
							t = null;
						} else {
							if (!(t instanceof BroadcastShardOperationFailedException)) {
								t = new BroadcastShardOperationFailedException(shardIt.shardId(), t);
							}
						}
					}
					shardsResponses.set(index, t);
				}
				if (expectedOps == counterOps.incrementAndGet()) {
					finishHim();
				}
			}
		}

		/**
		 * Finish him.
		 */
		void finishHim() {
			listener.onResponse(newResponse(request, shardsResponses, clusterState));
		}
	}

	/**
	 * The Class TransportHandler.
	 *
	 * @author l.xue.nong
	 */
	class TransportHandler extends BaseTransportRequestHandler<Request> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public Request newInstance() {
			return newRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SAME;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(Request request, final TransportChannel channel) throws Exception {

			request.listenerThreaded(false);

			if (request.operationThreading() == BroadcastOperationThreading.NO_THREADS) {
				request.operationThreading(BroadcastOperationThreading.SINGLE_THREAD);
			}
			execute(request, new ActionListener<Response>() {
				@Override
				public void onResponse(Response response) {
					try {
						channel.sendResponse(response);
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(e);
					} catch (Exception e1) {
						logger.warn("Failed to send response", e1);
					}
				}
			});
		}
	}

	/**
	 * The Class ShardTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	class ShardTransportHandler extends BaseTransportRequestHandler<ShardRequest> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public ShardRequest newInstance() {
			return newShardRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return executor;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(final ShardRequest request, final TransportChannel channel) throws Exception {
			channel.sendResponse(shardOperation(request));
		}
	}
}
