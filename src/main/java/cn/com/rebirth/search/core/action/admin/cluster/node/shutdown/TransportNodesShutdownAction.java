/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportNodesShutdownAction.java 2012-3-29 15:00:47 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.node.shutdown;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.exception.RestartIllegalStateException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.stream.VoidStreamable;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.node.Node;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportService;
import cn.com.rebirth.search.core.transport.VoidTransportResponseHandler;

import com.google.common.collect.Sets;


/**
 * The Class TransportNodesShutdownAction.
 *
 * @author l.xue.nong
 */
public class TransportNodesShutdownAction extends
		TransportMasterNodeOperationAction<NodesShutdownRequest, NodesShutdownResponse> {

	
	/** The node. */
	private final Node node;

	
	/** The cluster name. */
	private final ClusterName clusterName;

	
	/** The disabled. */
	private final boolean disabled;

	
	/** The delay. */
	private final TimeValue delay;

	
	/**
	 * Instantiates a new transport nodes shutdown action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param node the node
	 * @param clusterName the cluster name
	 */
	@Inject
	public TransportNodesShutdownAction(Settings settings, TransportService transportService,
			ClusterService clusterService, ThreadPool threadPool, Node node, ClusterName clusterName) {
		super(settings, transportService, clusterService, threadPool);
		this.node = node;
		this.clusterName = clusterName;
		this.disabled = settings.getAsBoolean("action.disable_shutdown",
				componentSettings.getAsBoolean("disabled", false));
		this.delay = componentSettings.getAsTime("delay", TimeValue.timeValueMillis(200));

		this.transportService.registerHandler(NodeShutdownRequestHandler.ACTION, new NodeShutdownRequestHandler());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.GENERIC;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return NodesShutdownAction.NAME;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected NodesShutdownRequest newRequest() {
		return new NodesShutdownRequest();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected NodesShutdownResponse newResponse() {
		return new NodesShutdownResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#processBeforeDelegationToMaster(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest, cn.com.summall.search.core.cluster.ClusterState)
	 */
	@Override
	protected void processBeforeDelegationToMaster(NodesShutdownRequest request, ClusterState state) {
		String[] nodesIds = request.nodesIds;
		if (nodesIds != null) {
			for (int i = 0; i < nodesIds.length; i++) {
				
				if ("_local".equals(nodesIds[i])) {
					nodesIds[i] = state.nodes().localNodeId();
				}
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest, cn.com.summall.search.core.cluster.ClusterState)
	 */
	@Override
	protected NodesShutdownResponse masterOperation(final NodesShutdownRequest request, final ClusterState state)
			throws RestartException {
		if (disabled) {
			throw new RestartIllegalStateException("Shutdown is disabled");
		}
		Set<DiscoveryNode> nodes = Sets.newHashSet();
		if (state.nodes().isAllNodes(request.nodesIds)) {
			logger.info("[cluster_shutdown]: requested, shutting down in [{}]", request.delay);
			nodes.addAll(state.nodes().dataNodes().values());
			nodes.addAll(state.nodes().masterNodes().values());
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(request.delay.millis());
					} catch (InterruptedException e) {
						
					}
					
					logger.trace("[cluster_shutdown]: stopping the cluster service so no re-routing will occur");
					clusterService.stop();

					final CountDownLatch latch = new CountDownLatch(state.nodes().size());
					for (final DiscoveryNode node : state.nodes()) {
						if (node.id().equals(state.nodes().masterNodeId())) {
							
							latch.countDown();
						} else {
							logger.trace("[cluster_shutdown]: sending shutdown request to [{}]", node);
							transportService.sendRequest(node, NodeShutdownRequestHandler.ACTION,
									new NodeShutdownRequest(request.exit), new VoidTransportResponseHandler(
											ThreadPool.Names.SAME) {
										@Override
										public void handleResponse(VoidStreamable response) {
											logger.trace("[cluster_shutdown]: received shutdown response from [{}]",
													node);
											latch.countDown();
										}

										@Override
										public void handleException(TransportException exp) {
											logger.warn(
													"[cluster_shutdown]: received failed shutdown response from [{}]",
													exp, node);
											latch.countDown();
										}
									});
						}
					}
					try {
						latch.await();
					} catch (InterruptedException e) {
						
					}
					logger.info("[cluster_shutdown]: done shutting down all nodes except master, proceeding to master");

					
					logger.trace("[cluster_shutdown]: shutting down the master [{}]", state.nodes().masterNode());
					transportService.sendRequest(state.nodes().masterNode(), NodeShutdownRequestHandler.ACTION,
							new NodeShutdownRequest(request.exit), new VoidTransportResponseHandler(
									ThreadPool.Names.SAME) {
								@Override
								public void handleResponse(VoidStreamable response) {
									logger.trace("[cluster_shutdown]: received shutdown response from master");
								}

								@Override
								public void handleException(TransportException exp) {
									logger.warn("[cluster_shutdown]: received failed shutdown response master", exp);
								}
							});
				}
			});
			t.start();
		} else {
			final String[] nodesIds = state.nodes().resolveNodes(request.nodesIds);
			logger.info("[partial_cluster_shutdown]: requested, shutting down [{}] in [{}]", nodesIds, request.delay);

			for (String nodeId : nodesIds) {
				final DiscoveryNode node = state.nodes().get(nodeId);
				if (node != null) {
					nodes.add(node);
				}
			}

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(request.delay.millis());
					} catch (InterruptedException e) {
						
					}

					final CountDownLatch latch = new CountDownLatch(nodesIds.length);
					for (String nodeId : nodesIds) {
						final DiscoveryNode node = state.nodes().get(nodeId);
						if (node == null) {
							logger.warn("[partial_cluster_shutdown]: no node to shutdown for node_id [{}]", nodeId);
							latch.countDown();
							continue;
						}

						logger.trace("[partial_cluster_shutdown]: sending shutdown request to [{}]", node);
						transportService.sendRequest(node, NodeShutdownRequestHandler.ACTION, new NodeShutdownRequest(
								request.exit), new VoidTransportResponseHandler(ThreadPool.Names.SAME) {
							@Override
							public void handleResponse(VoidStreamable response) {
								logger.trace("[partial_cluster_shutdown]: received shutdown response from [{}]", node);
								latch.countDown();
							}

							@Override
							public void handleException(TransportException exp) {
								logger.warn("[partial_cluster_shutdown]: received failed shutdown response from [{}]",
										exp, node);
								latch.countDown();
							}
						});
					}

					try {
						latch.await();
					} catch (InterruptedException e) {
						
					}

					logger.info("[partial_cluster_shutdown]: done shutting down [{}]", ((Object) nodesIds));
				}
			});
			t.start();
		}
		return new NodesShutdownResponse(clusterName, nodes.toArray(new DiscoveryNode[nodes.size()]));
	}

	
	/**
	 * The Class NodeShutdownRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	private class NodeShutdownRequestHandler extends BaseTransportRequestHandler<NodeShutdownRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "/cluster/nodes/shutdown/node";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public NodeShutdownRequest newInstance() {
			return new NodeShutdownRequest();
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
		public void messageReceived(final NodeShutdownRequest request, TransportChannel channel) throws Exception {
			if (disabled) {
				throw new RestartIllegalStateException("Shutdown is disabled");
			}
			logger.info("shutting down in [{}]", delay);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(delay.millis());
					} catch (InterruptedException e) {
						
					}
					if (!request.exit) {
						logger.info("initiating requested shutdown (no exit)...");
						try {
							node.close();
						} catch (Exception e) {
							logger.warn("Failed to shutdown", e);
						}
						return;
					}
					boolean shutdownWithWrapper = false;
					if (System.getProperty("summallsearch-service") != null) {
						try {
							Class wrapperManager = settings.getClassLoader().loadClass(
									"org.tanukisoftware.wrapper.WrapperManager");
							logger.info("initiating requested shutdown (using service)");
							wrapperManager.getMethod("stopAndReturn", int.class).invoke(null, 0);
							shutdownWithWrapper = true;
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
					if (!shutdownWithWrapper) {
						logger.info("initiating requested shutdown...");
						try {
							node.close();
						} catch (Exception e) {
							logger.warn("Failed to shutdown", e);
						} finally {
							
							System.exit(0);
						}
					}
				}
			});
			t.start();

			channel.sendResponse(VoidStreamable.INSTANCE);
		}
	}

	
	/**
	 * The Class NodeShutdownRequest.
	 *
	 * @author l.xue.nong
	 */
	static class NodeShutdownRequest implements Streamable {

		
		/** The exit. */
		boolean exit;

		
		/**
		 * Instantiates a new node shutdown request.
		 */
		NodeShutdownRequest() {
		}

		
		/**
		 * Instantiates a new node shutdown request.
		 *
		 * @param exit the exit
		 */
		NodeShutdownRequest(boolean exit) {
			this.exit = exit;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			exit = in.readBoolean();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeBoolean(exit);
		}
	}
}
