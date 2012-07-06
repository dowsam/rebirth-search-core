/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportMasterNodeOperationAction.java 2012-3-29 15:01:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support.master;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.support.TransportAction;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.TimeoutClusterStateListener;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.discovery.MasterNotDiscoveredException;
import cn.com.rebirth.search.core.node.NodeClosedException;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.ConnectTransportException;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportService;


/**
 * The Class TransportMasterNodeOperationAction.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public abstract class TransportMasterNodeOperationAction<Request extends MasterNodeOperationRequest, Response extends ActionResponse>
		extends TransportAction<Request, Response> {

	
	/** The transport service. */
	protected final TransportService transportService;

	
	/** The cluster service. */
	protected final ClusterService clusterService;

	
	/** The transport action. */
	final String transportAction;

	
	/** The executor. */
	final String executor;

	
	/**
	 * Instantiates a new transport master node operation action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 */
	protected TransportMasterNodeOperationAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool) {
		super(settings, threadPool);
		this.transportService = transportService;
		this.clusterService = clusterService;

		this.transportAction = transportAction();
		this.executor = executor();

		transportService.registerHandler(transportAction, new TransportHandler());
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
	 * @return the response
	 */
	protected abstract Response newResponse();

	
	/**
	 * Master operation.
	 *
	 * @param request the request
	 * @param state the state
	 * @return the response
	 * @throws SumMallSearchException the sum mall search exception
	 */
	protected abstract Response masterOperation(Request request, ClusterState state) throws RestartException;

	
	/**
	 * Local execute.
	 *
	 * @param request the request
	 * @return true, if successful
	 */
	protected boolean localExecute(Request request) {
		return false;
	}

	
	/**
	 * Check block.
	 *
	 * @param request the request
	 * @param state the state
	 * @return the cluster block exception
	 */
	protected ClusterBlockException checkBlock(Request request, ClusterState state) {
		return null;
	}

	
	/**
	 * Process before delegation to master.
	 *
	 * @param request the request
	 * @param state the state
	 */
	protected void processBeforeDelegationToMaster(Request request, ClusterState state) {

	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.TransportAction#doExecute(cn.com.summall.search.core.action.ActionRequest, cn.com.summall.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(final Request request, final ActionListener<Response> listener) {
		innerExecute(request, listener, false);
	}

	
	/**
	 * Inner execute.
	 *
	 * @param request the request
	 * @param listener the listener
	 * @param retrying the retrying
	 */
	private void innerExecute(final Request request, final ActionListener<Response> listener, final boolean retrying) {
		final ClusterState clusterState = clusterService.state();
		final DiscoveryNodes nodes = clusterState.nodes();
		if (nodes.localNodeMaster() || localExecute(request)) {
			
			final ClusterBlockException blockException = checkBlock(request, clusterState);
			if (blockException != null) {
				if (!blockException.retryable()) {
					listener.onFailure(blockException);
					return;
				}
				clusterService.add(request.masterNodeTimeout(), new TimeoutClusterStateListener() {
					@Override
					public void postAdded() {
						ClusterBlockException blockException = checkBlock(request, clusterService.state());
						if (blockException == null || !blockException.retryable()) {
							clusterService.remove(this);
							innerExecute(request, listener, false);
						}
					}

					@Override
					public void onClose() {
						clusterService.remove(this);
						listener.onFailure(blockException);
					}

					@Override
					public void onTimeout(TimeValue timeout) {
						clusterService.remove(this);
						listener.onFailure(blockException);
					}

					@Override
					public void clusterChanged(ClusterChangedEvent event) {
						ClusterBlockException blockException = checkBlock(request, event.state());
						if (blockException == null || !blockException.retryable()) {
							clusterService.remove(this);
							innerExecute(request, listener, false);
						}
					}
				});
			} else {
				threadPool.executor(executor).execute(new Runnable() {
					@Override
					public void run() {
						try {
							Response response = masterOperation(request, clusterState);
							listener.onResponse(response);
						} catch (Exception e) {
							listener.onFailure(e);
						}
					}
				});
			}
		} else {
			if (nodes.masterNode() == null) {
				if (retrying) {
					listener.onFailure(new MasterNotDiscoveredException());
				} else {
					clusterService.add(request.masterNodeTimeout(), new TimeoutClusterStateListener() {
						@Override
						public void postAdded() {
							ClusterState clusterStateV2 = clusterService.state();
							if (clusterStateV2.nodes().masterNodeId() != null) {
								
								clusterService.remove(this);
								innerExecute(request, listener, true);
							}
						}

						@Override
						public void onClose() {
							clusterService.remove(this);
							listener.onFailure(new NodeClosedException(nodes.localNode()));
						}

						@Override
						public void onTimeout(TimeValue timeout) {
							clusterService.remove(this);
							listener.onFailure(new MasterNotDiscoveredException("waited for [" + timeout + "]"));
						}

						@Override
						public void clusterChanged(ClusterChangedEvent event) {
							if (event.nodesDelta().masterNodeChanged()) {
								clusterService.remove(this);
								innerExecute(request, listener, true);
							}
						}
					});
				}
				return;
			}
			processBeforeDelegationToMaster(request, clusterState);
			transportService.sendRequest(nodes.masterNode(), transportAction, request,
					new BaseTransportResponseHandler<Response>() {
						@Override
						public Response newInstance() {
							return newResponse();
						}

						@Override
						public void handleResponse(Response response) {
							listener.onResponse(response);
						}

						@Override
						public String executor() {
							return ThreadPool.Names.SAME;
						}

						@Override
						public void handleException(final TransportException exp) {
							if (exp.unwrapCause() instanceof ConnectTransportException) {
								
								clusterService.add(request.masterNodeTimeout(), new TimeoutClusterStateListener() {
									@Override
									public void postAdded() {
										ClusterState clusterStateV2 = clusterService.state();
										if (!clusterState.nodes().masterNodeId()
												.equals(clusterStateV2.nodes().masterNodeId())) {
											
											clusterService.remove(this);
											innerExecute(request, listener, false);
										}
									}

									@Override
									public void onClose() {
										clusterService.remove(this);
										listener.onFailure(new NodeClosedException(nodes.localNode()));
									}

									@Override
									public void onTimeout(TimeValue timeout) {
										clusterService.remove(this);
										listener.onFailure(new MasterNotDiscoveredException());
									}

									@Override
									public void clusterChanged(ClusterChangedEvent event) {
										if (event.nodesDelta().masterNodeChanged()) {
											clusterService.remove(this);
											innerExecute(request, listener, false);
										}
									}
								});
							} else {
								listener.onFailure(exp);
							}
						}
					});
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
		public void messageReceived(final Request request, final TransportChannel channel) throws Exception {
			
			request.listenerThreaded(false);
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
}
