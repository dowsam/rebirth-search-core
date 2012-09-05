/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportInstanceSingleOperationAction.java 2012-7-6 14:30:29 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.single.instance;

import java.util.concurrent.atomic.AtomicBoolean;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.UnavailableShardsException;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.TimeoutClusterStateListener;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.node.NodeClosedException;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.ConnectTransportException;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportInstanceSingleOperationAction.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public abstract class TransportInstanceSingleOperationAction<Request extends InstanceShardOperationRequest, Response extends ActionResponse>
		extends TransportAction<Request, Response> {

	/** The cluster service. */
	protected final ClusterService clusterService;

	/** The transport service. */
	protected final TransportService transportService;

	/** The transport action. */
	final String transportAction;

	/** The executor. */
	final String executor;

	/**
	 * Instantiates a new transport instance single operation action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 */
	protected TransportInstanceSingleOperationAction(Settings settings, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService) {
		super(settings, threadPool);
		this.clusterService = clusterService;
		this.transportService = transportService;

		this.transportAction = transportAction();
		this.executor = executor();

		transportService.registerHandler(transportAction, new TransportHandler());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.TransportAction#doExecute(cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(Request request, ActionListener<Response> listener) {
		new AsyncSingleAction(request, listener).start();
	}

	/**
	 * Executor.
	 *
	 * @return the string
	 */
	protected abstract String executor();

	/**
	 * Transport action.
	 *
	 * @return the string
	 */
	protected abstract String transportAction();

	/**
	 * Shard operation.
	 *
	 * @param request the request
	 * @param listener the listener
	 * @throws RebirthException the rebirth exception
	 */
	protected abstract void shardOperation(Request request, ActionListener<Response> listener) throws RebirthException;

	/**
	 * New request.
	 *
	 * @return the request
	 */
	protected abstract Request newRequest();

	/**
	 * New response.
	 *
	 * @return the response
	 */
	protected abstract Response newResponse();

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
	 * @return the cluster block exception
	 */
	protected abstract ClusterBlockException checkRequestBlock(ClusterState state, Request request);

	/**
	 * Retry on failure.
	 *
	 * @param e the e
	 * @return true, if successful
	 */
	protected boolean retryOnFailure(Throwable e) {
		return false;
	}

	/**
	 * Transport options.
	 *
	 * @return the transport request options
	 */
	protected TransportRequestOptions transportOptions() {
		return TransportRequestOptions.EMPTY;
	}

	/**
	 * Shards.
	 *
	 * @param clusterState the cluster state
	 * @param request the request
	 * @return the shard iterator
	 * @throws RebirthException the rebirth exception
	 */
	protected abstract ShardIterator shards(ClusterState clusterState, Request request) throws RebirthException;

	/**
	 * The Class AsyncSingleAction.
	 *
	 * @author l.xue.nong
	 */
	class AsyncSingleAction {

		/** The listener. */
		private final ActionListener<Response> listener;

		/** The request. */
		private final Request request;

		/** The shard it. */
		private ShardIterator shardIt;

		/** The nodes. */
		private DiscoveryNodes nodes;

		/** The operation started. */
		private final AtomicBoolean operationStarted = new AtomicBoolean();

		/**
		 * Instantiates a new async single action.
		 *
		 * @param request the request
		 * @param listener the listener
		 */
		private AsyncSingleAction(Request request, ActionListener<Response> listener) {
			this.request = request;
			this.listener = listener;
		}

		/**
		 * Start.
		 */
		public void start() {
			start(false);
		}

		/**
		 * Start.
		 *
		 * @param fromClusterEvent the from cluster event
		 * @return true, if successful
		 * @throws RebirthException the rebirth exception
		 */
		public boolean start(final boolean fromClusterEvent) throws RebirthException {
			final ClusterState clusterState = clusterService.state();
			nodes = clusterState.nodes();
			try {
				ClusterBlockException blockException = checkGlobalBlock(clusterState, request);
				if (blockException != null) {
					if (blockException.retryable()) {
						retry(fromClusterEvent, blockException);
						return false;
					} else {
						throw blockException;
					}
				}
				request.index(clusterState.metaData().concreteIndex(request.index()));
				blockException = checkRequestBlock(clusterState, request);
				if (blockException != null) {
					if (blockException.retryable()) {
						retry(fromClusterEvent, blockException);
						return false;
					} else {
						throw blockException;
					}
				}
				shardIt = shards(clusterState, request);
			} catch (Exception e) {
				listener.onFailure(e);
				return true;
			}

			if (shardIt.size() == 0) {
				retry(fromClusterEvent, null);
				return false;
			}

			assert shardIt.size() == 1;

			ShardRouting shard = shardIt.nextOrNull();
			assert shard != null;

			if (!shard.active()) {
				retry(fromClusterEvent, null);
				return false;
			}

			if (!operationStarted.compareAndSet(false, true)) {
				return true;
			}

			request.shardId = shardIt.shardId().id();
			if (shard.currentNodeId().equals(nodes.localNodeId())) {
				request.beforeLocalFork();
				threadPool.executor(executor).execute(new Runnable() {
					@Override
					public void run() {
						try {
							shardOperation(request, listener);
						} catch (Exception e) {
							if (retryOnFailure(e)) {
								retry(fromClusterEvent, null);
							} else {
								listener.onFailure(e);
							}
						}
					}
				});
			} else {
				DiscoveryNode node = nodes.get(shard.currentNodeId());
				transportService.sendRequest(node, transportAction, request, transportOptions(),
						new BaseTransportResponseHandler<Response>() {

							@Override
							public Response newInstance() {
								return newResponse();
							}

							@Override
							public String executor() {
								return ThreadPool.Names.SAME;
							}

							@Override
							public void handleResponse(Response response) {
								listener.onResponse(response);
							}

							@Override
							public void handleException(TransportException exp) {

								if (exp.unwrapCause() instanceof ConnectTransportException
										|| exp.unwrapCause() instanceof NodeClosedException || retryOnFailure(exp)) {
									operationStarted.set(false);

									retry(false, null);
								} else {
									listener.onFailure(exp);
								}
							}
						});
			}
			return true;
		}

		/**
		 * Retry.
		 *
		 * @param fromClusterEvent the from cluster event
		 * @param failure the failure
		 */
		void retry(final boolean fromClusterEvent, final @Nullable Throwable failure) {
			if (!fromClusterEvent) {

				request.beforeLocalFork();
				clusterService.add(request.timeout(), new TimeoutClusterStateListener() {
					@Override
					public void postAdded() {
						if (start(true)) {

							clusterService.remove(this);
						}
					}

					@Override
					public void onClose() {
						clusterService.remove(this);
						listener.onFailure(new NodeClosedException(nodes.localNode()));
					}

					@Override
					public void clusterChanged(ClusterChangedEvent event) {
						if (start(true)) {

							clusterService.remove(this);
						}
					}

					@Override
					public void onTimeout(TimeValue timeValue) {

						if (start(true)) {
							clusterService.remove(this);
							return;
						}
						clusterService.remove(this);
						Throwable listenFailure = failure;
						if (listenFailure == null) {
							if (shardIt == null) {
								listenFailure = new UnavailableShardsException(new ShardId(request.index(), -1),
										"Timeout waiting for [" + timeValue + "], request: " + request.toString());
							} else {
								listenFailure = new UnavailableShardsException(shardIt.shardId(), "[" + shardIt.size()
										+ "] shardIt, [" + shardIt.sizeActive() + "] active : Timeout waiting for ["
										+ timeValue + "], request: " + request.toString());
							}
						}
						listener.onFailure(listenFailure);
					}
				});
			}
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
			execute(request, new ActionListener<Response>() {
				@Override
				public void onResponse(Response result) {
					try {
						channel.sendResponse(result);
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(e);
					} catch (Exception e1) {
						logger.warn("Failed to send response for get", e1);
					}
				}
			});
		}
	}
}
