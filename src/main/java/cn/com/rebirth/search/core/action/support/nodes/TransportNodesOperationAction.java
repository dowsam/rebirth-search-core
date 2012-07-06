/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportNodesOperationAction.java 2012-7-6 14:29:51 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.nodes;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.FailedNodeException;
import cn.com.rebirth.search.core.action.NoSuchNodeException;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;
import cn.com.rebirth.search.core.transport.TransportResponseOptions;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class TransportNodesOperationAction.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @param <NodeRequest> the generic type
 * @param <NodeResponse> the generic type
 * @author l.xue.nong
 */
public abstract class TransportNodesOperationAction<Request extends NodesOperationRequest, Response extends NodesOperationResponse, NodeRequest extends NodeOperationRequest, NodeResponse extends NodeOperationResponse>
		extends TransportAction<Request, Response> {

	/** The cluster name. */
	protected final ClusterName clusterName;

	/** The cluster service. */
	protected final ClusterService clusterService;

	/** The transport service. */
	protected final TransportService transportService;

	/** The transport action. */
	final String transportAction;

	/** The transport node action. */
	final String transportNodeAction;

	/** The executor. */
	final String executor;

	/**
	 * Instantiates a new transport nodes operation action.
	 *
	 * @param settings the settings
	 * @param clusterName the cluster name
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 */
	@Inject
	public TransportNodesOperationAction(Settings settings, ClusterName clusterName, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService) {
		super(settings, threadPool);
		this.clusterName = clusterName;
		this.clusterService = clusterService;
		this.transportService = transportService;

		this.transportAction = transportAction();
		this.transportNodeAction = transportAction() + "/n";
		this.executor = executor();

		transportService.registerHandler(transportAction, new TransportHandler());
		transportService.registerHandler(transportNodeAction, new NodeTransportHandler());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.TransportAction#doExecute(cn.com.rebirth.search.core.action.ActionRequest, cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(Request request, ActionListener<Response> listener) {
		new AsyncAction(request, listener).start();
	}

	/**
	 * Transport action.
	 *
	 * @return the string
	 */
	protected abstract String transportAction();

	/**
	 * Transport compress.
	 *
	 * @return true, if successful
	 */
	protected boolean transportCompress() {
		return false;
	}

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
	 * @param nodesResponses the nodes responses
	 * @return the response
	 */
	protected abstract Response newResponse(Request request, AtomicReferenceArray nodesResponses);

	/**
	 * New node request.
	 *
	 * @return the node request
	 */
	protected abstract NodeRequest newNodeRequest();

	/**
	 * New node request.
	 *
	 * @param nodeId the node id
	 * @param request the request
	 * @return the node request
	 */
	protected abstract NodeRequest newNodeRequest(String nodeId, Request request);

	/**
	 * New node response.
	 *
	 * @return the node response
	 */
	protected abstract NodeResponse newNodeResponse();

	/**
	 * Node operation.
	 *
	 * @param request the request
	 * @return the node response
	 * @throws RebirthException the rebirth exception
	 */
	protected abstract NodeResponse nodeOperation(NodeRequest request) throws RebirthException;

	/**
	 * Accumulate exceptions.
	 *
	 * @return true, if successful
	 */
	protected abstract boolean accumulateExceptions();

	/**
	 * Filter node ids.
	 *
	 * @param nodes the nodes
	 * @param nodesIds the nodes ids
	 * @return the string[]
	 */
	protected String[] filterNodeIds(DiscoveryNodes nodes, String[] nodesIds) {
		return nodesIds;
	}

	/**
	 * The Class AsyncAction.
	 *
	 * @author l.xue.nong
	 */
	private class AsyncAction {

		/** The request. */
		private final Request request;

		/** The nodes ids. */
		private final String[] nodesIds;

		/** The listener. */
		private final ActionListener<Response> listener;

		/** The cluster state. */
		private final ClusterState clusterState;

		/** The responses. */
		private final AtomicReferenceArray<Object> responses;

		/** The index. */
		private final AtomicInteger index = new AtomicInteger();

		/** The counter. */
		private final AtomicInteger counter = new AtomicInteger();

		/**
		 * Instantiates a new async action.
		 *
		 * @param request the request
		 * @param listener the listener
		 */
		private AsyncAction(Request request, ActionListener<Response> listener) {
			this.request = request;
			this.listener = listener;
			clusterState = clusterService.state();
			String[] nodesIds = clusterState.nodes().resolveNodes(request.nodesIds());
			this.nodesIds = filterNodeIds(clusterState.nodes(), nodesIds);
			this.responses = new AtomicReferenceArray<Object>(this.nodesIds.length);
		}

		/**
		 * Start.
		 */
		private void start() {
			if (nodesIds.length == 0) {

				threadPool.generic().execute(new Runnable() {
					@Override
					public void run() {
						listener.onResponse(newResponse(request, responses));
					}
				});
				return;
			}
			TransportRequestOptions transportRequestOptions = TransportRequestOptions.options();
			if (request.timeout() != null) {
				transportRequestOptions.withTimeout(request.timeout());
			}
			transportRequestOptions.withCompress(transportCompress());
			for (final String nodeId : nodesIds) {
				final DiscoveryNode node = clusterState.nodes().nodes().get(nodeId);
				if (nodeId.equals("_local") || nodeId.equals(clusterState.nodes().localNodeId())) {
					threadPool.executor(executor()).execute(new Runnable() {
						@Override
						public void run() {
							try {
								onOperation(nodeOperation(newNodeRequest(clusterState.nodes().localNodeId(), request)));
							} catch (Exception e) {
								onFailure(clusterState.nodes().localNodeId(), e);
							}
						}
					});
				} else if (nodeId.equals("_master")) {
					threadPool.executor(executor()).execute(new Runnable() {
						@Override
						public void run() {
							try {
								onOperation(nodeOperation(newNodeRequest(clusterState.nodes().masterNodeId(), request)));
							} catch (Exception e) {
								onFailure(clusterState.nodes().masterNodeId(), e);
							}
						}
					});
				} else {
					if (node == null) {
						onFailure(nodeId, new NoSuchNodeException(nodeId));
					} else {
						NodeRequest nodeRequest = newNodeRequest(nodeId, request);
						transportService.sendRequest(node, transportNodeAction, nodeRequest, transportRequestOptions,
								new BaseTransportResponseHandler<NodeResponse>() {
									@Override
									public NodeResponse newInstance() {
										return newNodeResponse();
									}

									@Override
									public void handleResponse(NodeResponse response) {
										onOperation(response);
									}

									@Override
									public void handleException(TransportException exp) {
										onFailure(node.id(), exp);
									}

									@Override
									public String executor() {
										return ThreadPool.Names.SAME;
									}
								});
					}
				}
			}
		}

		/**
		 * On operation.
		 *
		 * @param nodeResponse the node response
		 */
		private void onOperation(NodeResponse nodeResponse) {

			responses.set(index.getAndIncrement(), nodeResponse);
			if (counter.incrementAndGet() == responses.length()) {
				finishHim();
			}
		}

		/**
		 * On failure.
		 *
		 * @param nodeId the node id
		 * @param t the t
		 */
		private void onFailure(String nodeId, Throwable t) {
			if (logger.isDebugEnabled()) {
				logger.debug("failed to execute on node [{}]", t, nodeId);
			}
			int idx = index.getAndIncrement();
			if (accumulateExceptions()) {
				responses.set(idx, new FailedNodeException(nodeId, "Failed node [" + nodeId + "]", t));
			}
			if (counter.incrementAndGet() == responses.length()) {
				finishHim();
			}
		}

		/**
		 * Finish him.
		 */
		private void finishHim() {
			listener.onResponse(newResponse(request, responses));
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
		public void messageReceived(final Request request, final TransportChannel channel) throws Exception {
			request.listenerThreaded(false);
			execute(request, new ActionListener<Response>() {
				@Override
				public void onResponse(Response response) {
					TransportResponseOptions options = TransportResponseOptions.options().withCompress(
							transportCompress());
					try {
						channel.sendResponse(response, options);
					} catch (Exception e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable e) {
					try {
						channel.sendResponse(e);
					} catch (Exception e1) {
						logger.warn("Failed to send response", e);
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

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return transportAction;
		}
	}

	/**
	 * The Class NodeTransportHandler.
	 *
	 * @author l.xue.nong
	 */
	private class NodeTransportHandler extends BaseTransportRequestHandler<NodeRequest> {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public NodeRequest newInstance() {
			return newNodeRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(final NodeRequest request, final TransportChannel channel) throws Exception {
			channel.sendResponse(nodeOperation(request));
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return transportNodeAction;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return executor;
		}
	}
}
