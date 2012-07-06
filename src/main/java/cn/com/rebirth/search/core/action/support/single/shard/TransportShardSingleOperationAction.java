/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportShardSingleOperationAction.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support.single.shard;

import java.io.IOException;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.NoShardAvailableActionException;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.cluster.routing.ShardIterator;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportService;


/**
 * The Class TransportShardSingleOperationAction.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public abstract class TransportShardSingleOperationAction<Request extends SingleShardOperationRequest, Response extends ActionResponse>
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
	 * Instantiates a new transport shard single operation action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 */
	protected TransportShardSingleOperationAction(Settings settings, ThreadPool threadPool,
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
	 * @see cn.com.summall.search.core.action.support.TransportAction#doExecute(cn.com.summall.search.core.action.ActionRequest, cn.com.summall.search.core.action.ActionListener)
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
	 * Shard operation.
	 *
	 * @param request the request
	 * @param shardId the shard id
	 * @return the response
	 * @throws SumMallSearchException the sum mall search exception
	 */
	protected abstract Response shardOperation(Request request, int shardId) throws RestartException;

	
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
	 * Resolve request.
	 *
	 * @param state the state
	 * @param request the request
	 */
	protected void resolveRequest(ClusterState state, Request request) {
		request.index(state.metaData().concreteIndex(request.index()));
	}

	
	/**
	 * Shards.
	 *
	 * @param state the state
	 * @param request the request
	 * @return the shard iterator
	 * @throws SumMallSearchException the sum mall search exception
	 */
	protected abstract ShardIterator shards(ClusterState state, Request request) throws RestartException;

	
	/**
	 * The Class AsyncSingleAction.
	 *
	 * @author l.xue.nong
	 */
	class AsyncSingleAction {

		
		/** The listener. */
		private final ActionListener<Response> listener;

		
		/** The shard it. */
		private final ShardIterator shardIt;

		
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
			resolveRequest(clusterState, request);
			blockException = checkRequestBlock(clusterState, request);
			if (blockException != null) {
				throw blockException;
			}

			this.shardIt = shards(clusterState, request);
		}

		
		/**
		 * Start.
		 */
		public void start() {
			perform(null);
		}

		
		/**
		 * On failure.
		 *
		 * @param shardRouting the shard routing
		 * @param e the e
		 */
		private void onFailure(ShardRouting shardRouting, Exception e) {
			if (logger.isTraceEnabled() && e != null) {
				logger.trace(shardRouting.shortSummary() + ": Failed to execute [{}]", e, request);
			}
			perform(e);
		}

		
		/**
		 * Perform.
		 *
		 * @param lastException the last exception
		 */
		private void perform(@Nullable final Exception lastException) {
			final ShardRouting shardRouting = shardIt.nextOrNull();
			if (shardRouting == null) {
				Exception failure = lastException;
				if (failure == null) {
					failure = new NoShardAvailableActionException(shardIt.shardId(), "No shard available for ["
							+ request + "]");
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug(shardIt.shardId() + ": Failed to execute [{}]", failure, request);
					}
				}
				listener.onFailure(failure);
				return;
			}

			if (shardRouting.currentNodeId().equals(nodes.localNodeId())) {
				if (request.operationThreaded()) {
					threadPool.executor(executor).execute(new Runnable() {
						@Override
						public void run() {
							try {
								Response response = shardOperation(request, shardRouting.id());
								listener.onResponse(response);
							} catch (Exception e) {
								onFailure(shardRouting, e);
							}
						}
					});
				} else {
					try {
						final Response response = shardOperation(request, shardRouting.id());
						listener.onResponse(response);
					} catch (Exception e) {
						onFailure(shardRouting, e);
					}
				}
			} else {
				DiscoveryNode node = nodes.get(shardRouting.currentNodeId());
				transportService.sendRequest(node, transportShardAction, new ShardSingleOperationRequest(request,
						shardRouting.id()), new BaseTransportResponseHandler<Response>() {

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
						onFailure(shardRouting, exp);
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
	private class TransportHandler extends BaseTransportRequestHandler<Request> {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public Request newInstance() {
			return newRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SAME;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
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
	}

	
	/**
	 * The Class ShardTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class ShardTransportHandler extends BaseTransportRequestHandler<ShardSingleOperationRequest> {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public ShardSingleOperationRequest newInstance() {
			return new ShardSingleOperationRequest();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return executor;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
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
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			request = newRequest();
			request.readFrom(in);
			shardId = in.readVInt();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			request.writeTo(out);
			out.writeVInt(shardId);
		}
	}
}
