/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportSingleCustomOperationAction.java 2012-7-6 14:29:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.single.custom;

import java.io.IOException;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.NoShardAvailableActionException;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.ShardsIterator;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportSingleCustomOperationAction.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public abstract class TransportSingleCustomOperationAction<Request extends SingleCustomOperationRequest, Response extends ActionResponse>
		extends TransportAction<Request, Response> {

	/** The cluster service. */
	protected final ClusterService clusterService;

	/** The transport service. */
	protected final TransportService transportService;

	/** The transport action. */
	final String transportAction;

	/** The transport shard action. */
	final String transportShardAction;

	/** The executor. */
	final String executor;

	/**
	 * Instantiates a new transport single custom operation action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 */
	protected TransportSingleCustomOperationAction(Settings settings, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService) {
		super(settings, threadPool);
		this.clusterService = clusterService;
		this.transportService = transportService;

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
		new AsyncSingleAction(request, listener).start();
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
	 * Shards.
	 *
	 * @param state the state
	 * @param request the request
	 * @return the shards iterator
	 */
	protected abstract ShardsIterator shards(ClusterState state, Request request);

	/**
	 * Shard operation.
	 *
	 * @param request the request
	 * @param shardId the shard id
	 * @return the response
	 * @throws RebirthException the rebirth exception
	 */
	protected abstract Response shardOperation(Request request, int shardId) throws RebirthException;

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
	 * The Class AsyncSingleAction.
	 *
	 * @author l.xue.nong
	 */
	private class AsyncSingleAction {

		/** The listener. */
		private final ActionListener<Response> listener;

		/** The shards it. */
		private final ShardsIterator shardsIt;

		/** The request. */
		private final Request request;

		/** The nodes. */
		private final DiscoveryNodes nodes;

		/**
		 * Instantiates a new async single action.
		 *
		 * @param request the request
		 * @param listener the listener
		 */
		private AsyncSingleAction(Request request, ActionListener<Response> listener) {
			this.request = request;
			this.listener = listener;

			ClusterState clusterState = clusterService.state();
			nodes = clusterState.nodes();
			ClusterBlockException blockException = checkGlobalBlock(clusterState, request);
			if (blockException != null) {
				throw blockException;
			}
			blockException = checkRequestBlock(clusterState, request);
			if (blockException != null) {
				throw blockException;
			}
			this.shardsIt = shards(clusterState, request);
		}

		/**
		 * Start.
		 */
		public void start() {
			performFirst();
		}

		/**
		 * On failure.
		 *
		 * @param shardRouting the shard routing
		 * @param e the e
		 */
		private void onFailure(ShardRouting shardRouting, Exception e) {
			if (logger.isTraceEnabled() && e != null) {
				logger.trace(shardRouting.shortSummary() + ": Failed to execute [" + request + "]", e);
			}
			perform(e);
		}

		/**
		 * Perform first.
		 */
		private void performFirst() {
			if (shardsIt == null) {

				if (request.operationThreaded()) {
					request.beforeLocalFork();
					threadPool.executor(executor()).execute(new Runnable() {
						@Override
						public void run() {
							try {
								Response response = shardOperation(request, -1);
								listener.onResponse(response);
							} catch (Exception e) {
								onFailure(null, e);
							}
						}
					});
					return;
				} else {
					try {
						final Response response = shardOperation(request, -1);
						listener.onResponse(response);
						return;
					} catch (Exception e) {
						onFailure(null, e);
					}
				}
				return;
			}

			if (request.preferLocalShard()) {
				boolean foundLocal = false;
				ShardRouting shardX;
				while ((shardX = shardsIt.nextOrNull()) != null) {
					final ShardRouting shard = shardX;
					if (shard.currentNodeId().equals(nodes.localNodeId())) {
						foundLocal = true;
						if (request.operationThreaded()) {
							request.beforeLocalFork();
							threadPool.executor(executor()).execute(new Runnable() {
								@Override
								public void run() {
									try {
										Response response = shardOperation(request, shard.id());
										listener.onResponse(response);
									} catch (Exception e) {
										shardsIt.reset();
										onFailure(shard, e);
									}
								}
							});
							return;
						} else {
							try {
								final Response response = shardOperation(request, shard.id());
								listener.onResponse(response);
								return;
							} catch (Exception e) {
								shardsIt.reset();
								onFailure(shard, e);
							}
						}
					}
				}
				if (!foundLocal) {

					shardsIt.reset();
					perform(null);
				}
			} else {
				perform(null);
			}
		}

		/**
		 * Perform.
		 *
		 * @param lastException the last exception
		 */
		private void perform(final Exception lastException) {
			final ShardRouting shard = shardsIt == null ? null : shardsIt.nextOrNull();
			if (shard == null) {
				Exception failure = lastException;
				if (failure == null) {
					failure = new NoShardAvailableActionException(null, "No shard available for [" + request + "]");
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("failed to execute [" + request + "]", failure);
					}
				}
				listener.onFailure(failure);
			} else {
				if (shard.currentNodeId().equals(nodes.localNodeId())) {

					if (!request.preferLocalShard()) {
						if (request.operationThreaded()) {
							request.beforeLocalFork();
							threadPool.executor(executor).execute(new Runnable() {
								@Override
								public void run() {
									try {
										Response response = shardOperation(request, shard.id());
										listener.onResponse(response);
									} catch (Exception e) {
										onFailure(shard, e);
									}
								}
							});
						} else {
							try {
								final Response response = shardOperation(request, shard.id());
								listener.onResponse(response);
							} catch (Exception e) {
								onFailure(shard, e);
							}
						}
					} else {
						perform(lastException);
					}
				} else {
					DiscoveryNode node = nodes.get(shard.currentNodeId());
					transportService.sendRequest(node, transportShardAction, new ShardSingleOperationRequest(request,
							shard.id()), new BaseTransportResponseHandler<Response>() {
						@Override
						public Response newInstance() {
							return newResponse();
						}

						@Override
						public String executor() {
							return ThreadPool.Names.SAME;
						}

						@Override
						public void handleResponse(final Response response) {
							listener.onResponse(response);
						}

						@Override
						public void handleException(TransportException exp) {
							onFailure(shard, exp);
						}
					});
				}
			}
		}
	}

	/**
	 * The Class TransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class TransportHandler extends BaseTransportRequestHandler<Request> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public Request newInstance() {
			return newRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(Request request, final TransportChannel channel) throws Exception {

			request.listenerThreaded(false);

			request.operationThreaded(true);
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

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SAME;
		}
	}

	/**
	 * The Class ShardTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class ShardTransportHandler extends BaseTransportRequestHandler<ShardSingleOperationRequest> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public ShardSingleOperationRequest newInstance() {
			return new ShardSingleOperationRequest();
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
		public void messageReceived(final ShardSingleOperationRequest request, final TransportChannel channel)
				throws Exception {
			Response response = shardOperation(request.request(), request.shardId());
			channel.sendResponse(response);
		}
	}

	/**
	 * The Class ShardSingleOperationRequest.
	 *
	 * @author l.xue.nong
	 */
	protected class ShardSingleOperationRequest implements Streamable {

		/** The request. */
		private Request request;

		/** The shard id. */
		private int shardId;

		/**
		 * Instantiates a new shard single operation request.
		 */
		ShardSingleOperationRequest() {
		}

		/**
		 * Instantiates a new shard single operation request.
		 *
		 * @param request the request
		 * @param shardId the shard id
		 */
		public ShardSingleOperationRequest(Request request, int shardId) {
			this.request = request;
			this.shardId = shardId;
		}

		/**
		 * Request.
		 *
		 * @return the request
		 */
		public Request request() {
			return request;
		}

		/**
		 * Shard id.
		 *
		 * @return the int
		 */
		public int shardId() {
			return shardId;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			request = newRequest();
			request.readFrom(in);
			shardId = in.readVInt();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			request.writeTo(out);
			out.writeVInt(shardId);
		}
	}
}
