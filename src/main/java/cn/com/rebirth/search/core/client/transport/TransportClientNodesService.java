/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportClientNodesService.java 2012-7-6 14:29:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.client.transport;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.transport.TransportAddress;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodeInfo;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoAction;
import cn.com.rebirth.search.core.action.admin.cluster.node.info.NodesInfoResponse;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.ConnectTransportException;
import cn.com.rebirth.search.core.transport.FutureTransportResponseHandler;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * The Class TransportClientNodesService.
 *
 * @author l.xue.nong
 */
public class TransportClientNodesService extends AbstractComponent {

	/** The nodes sampler interval. */
	private final TimeValue nodesSamplerInterval;

	/** The ping timeout. */
	private final long pingTimeout;

	/** The cluster name. */
	private final ClusterName clusterName;

	/** The transport service. */
	private final TransportService transportService;

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The listed nodes. */
	private volatile ImmutableList<DiscoveryNode> listedNodes = ImmutableList.of();

	/** The transport mutex. */
	private final Object transportMutex = new Object();

	/** The nodes. */
	private volatile ImmutableList<DiscoveryNode> nodes = ImmutableList.of();

	/** The temp node id generator. */
	private final AtomicInteger tempNodeIdGenerator = new AtomicInteger();

	/** The nodes sampler. */
	private final NodeSampler nodesSampler;

	/** The nodes sampler future. */
	private volatile ScheduledFuture nodesSamplerFuture;

	/** The random node generator. */
	private final AtomicInteger randomNodeGenerator = new AtomicInteger();

	/** The closed. */
	private volatile boolean closed;

	/**
	 * Instantiates a new transport client nodes service.
	 *
	 * @param settings the settings
	 * @param clusterName the cluster name
	 * @param transportService the transport service
	 * @param threadPool the thread pool
	 */
	@Inject
	public TransportClientNodesService(Settings settings, ClusterName clusterName, TransportService transportService,
			ThreadPool threadPool) {
		super(settings);
		this.clusterName = clusterName;
		this.transportService = transportService;
		this.threadPool = threadPool;

		this.nodesSamplerInterval = componentSettings
				.getAsTime("nodes_sampler_interval", TimeValue.timeValueSeconds(5));
		this.pingTimeout = componentSettings.getAsTime("ping_timeout", TimeValue.timeValueSeconds(5)).millis();

		if (logger.isDebugEnabled()) {
			logger.debug("node_sampler_interval[" + nodesSamplerInterval + "]");
		}

		if (componentSettings.getAsBoolean("sniff", false)) {
			this.nodesSampler = new SniffNodesSampler();
		} else {
			this.nodesSampler = new SimpleNodeSampler();
		}
		this.nodesSamplerFuture = threadPool.schedule(nodesSamplerInterval, ThreadPool.Names.GENERIC,
				new ScheduledNodeSampler());

		transportService.throwConnectException(true);
	}

	/**
	 * Transport addresses.
	 *
	 * @return the immutable list
	 */
	public ImmutableList<TransportAddress> transportAddresses() {
		ImmutableList.Builder<TransportAddress> lstBuilder = ImmutableList.builder();
		for (DiscoveryNode listedNode : listedNodes) {
			lstBuilder.add(listedNode.address());
		}
		return lstBuilder.build();
	}

	/**
	 * Connected nodes.
	 *
	 * @return the immutable list
	 */
	public ImmutableList<DiscoveryNode> connectedNodes() {
		return this.nodes;
	}

	/**
	 * Adds the transport address.
	 *
	 * @param transportAddress the transport address
	 * @return the transport client nodes service
	 */
	public TransportClientNodesService addTransportAddress(TransportAddress transportAddress) {
		synchronized (transportMutex) {
			ImmutableList.Builder<DiscoveryNode> builder = ImmutableList.builder();
			listedNodes = builder.addAll(listedNodes)
					.add(new DiscoveryNode("#transport#-" + tempNodeIdGenerator.incrementAndGet(), transportAddress))
					.build();
		}
		nodesSampler.sample();
		return this;
	}

	/**
	 * Removes the transport address.
	 *
	 * @param transportAddress the transport address
	 * @return the transport client nodes service
	 */
	public TransportClientNodesService removeTransportAddress(TransportAddress transportAddress) {
		synchronized (transportMutex) {
			ImmutableList.Builder<DiscoveryNode> builder = ImmutableList.builder();
			for (DiscoveryNode otherNode : listedNodes) {
				if (!otherNode.address().equals(transportAddress)) {
					builder.add(otherNode);
				}
			}
			listedNodes = builder.build();
		}
		nodesSampler.sample();
		return this;
	}

	/**
	 * Execute.
	 *
	 * @param <T> the generic type
	 * @param callback the callback
	 * @return the t
	 * @throws RebirthException the rebirth exception
	 */
	public <T> T execute(NodeCallback<T> callback) throws RebirthException {
		ImmutableList<DiscoveryNode> nodes = this.nodes;
		if (nodes.isEmpty()) {
			throw new NoNodeAvailableException();
		}
		int index = randomNodeGenerator.incrementAndGet();
		if (index < 0) {
			index = 0;
			randomNodeGenerator.set(0);
		}
		for (int i = 0; i < nodes.size(); i++) {
			DiscoveryNode node = nodes.get((index + i) % nodes.size());
			try {
				return callback.doWithNode(node);
			} catch (RebirthException e) {
				if (!(e.unwrapCause() instanceof ConnectTransportException)) {
					throw e;
				}
			}
		}
		throw new NoNodeAvailableException();
	}

	/**
	 * Execute.
	 *
	 * @param <Response> the generic type
	 * @param callback the callback
	 * @param listener the listener
	 * @throws RebirthException the rebirth exception
	 */
	public <Response> void execute(NodeListenerCallback<Response> callback, ActionListener<Response> listener)
			throws RebirthException {
		ImmutableList<DiscoveryNode> nodes = this.nodes;
		if (nodes.isEmpty()) {
			throw new NoNodeAvailableException();
		}
		int index = randomNodeGenerator.incrementAndGet();
		if (index < 0) {
			index = 0;
			randomNodeGenerator.set(0);
		}
		RetryListener<Response> retryListener = new RetryListener<Response>(callback, listener, nodes, index);
		try {
			callback.doWithNode(nodes.get((index) % nodes.size()), retryListener);
		} catch (RebirthException e) {
			if (e.unwrapCause() instanceof ConnectTransportException) {
				retryListener.onFailure(e);
			} else {
				throw e;
			}
		}
	}

	/**
	 * The listener interface for receiving retry events.
	 * The class that is interested in processing a retry
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addRetryListener<code> method. When
	 * the retry event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @param <Response> the generic type
	 * @see RetryEvent
	 */
	public static class RetryListener<Response> implements ActionListener<Response> {

		/** The callback. */
		private final NodeListenerCallback<Response> callback;

		/** The listener. */
		private final ActionListener<Response> listener;

		/** The nodes. */
		private final ImmutableList<DiscoveryNode> nodes;

		/** The index. */
		private final int index;

		/** The i. */
		private volatile int i;

		/**
		 * Instantiates a new retry listener.
		 *
		 * @param callback the callback
		 * @param listener the listener
		 * @param nodes the nodes
		 * @param index the index
		 */
		public RetryListener(NodeListenerCallback<Response> callback, ActionListener<Response> listener,
				ImmutableList<DiscoveryNode> nodes, int index) {
			this.callback = callback;
			this.listener = listener;
			this.nodes = nodes;
			this.index = index;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.ActionListener#onResponse(java.lang.Object)
		 */
		@Override
		public void onResponse(Response response) {
			listener.onResponse(response);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.ActionListener#onFailure(java.lang.Throwable)
		 */
		@Override
		public void onFailure(Throwable e) {
			if (ExceptionsHelper.unwrapCause(e) instanceof ConnectTransportException) {
				int i = ++this.i;
				if (i == nodes.size()) {
					listener.onFailure(new NoNodeAvailableException());
				} else {
					try {
						callback.doWithNode(nodes.get((index + i) % nodes.size()), this);
					} catch (Exception e1) {

						onFailure(e);
					}
				}
			} else {
				listener.onFailure(e);
			}
		}
	}

	/**
	 * Close.
	 */
	public void close() {
		closed = true;
		nodesSamplerFuture.cancel(true);
		for (DiscoveryNode node : nodes) {
			transportService.disconnectFromNode(node);
		}
		for (DiscoveryNode listedNode : listedNodes) {
			transportService.disconnectFromNode(listedNode);
		}
		nodes = ImmutableList.of();
	}

	/**
	 * The Interface NodeSampler.
	 *
	 * @author l.xue.nong
	 */
	interface NodeSampler {

		/**
		 * Sample.
		 */
		void sample();
	}

	/**
	 * The Class ScheduledNodeSampler.
	 *
	 * @author l.xue.nong
	 */
	class ScheduledNodeSampler implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				nodesSampler.sample();
				if (!closed) {
					nodesSamplerFuture = threadPool.schedule(nodesSamplerInterval, ThreadPool.Names.GENERIC, this);
				}
			} catch (Exception e) {
				logger.warn("failed to sample", e);
			}
		}
	}

	/**
	 * The Class SimpleNodeSampler.
	 *
	 * @author l.xue.nong
	 */
	class SimpleNodeSampler implements NodeSampler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.client.transport.TransportClientNodesService.NodeSampler#sample()
		 */
		@Override
		public synchronized void sample() {
			if (closed) {
				return;
			}
			HashSet<DiscoveryNode> newNodes = new HashSet<DiscoveryNode>();
			for (DiscoveryNode node : listedNodes) {
				if (!transportService.nodeConnected(node)) {
					try {
						transportService.connectToNode(node);
					} catch (Exception e) {
						logger.debug("failed to connect to node [{}], removed from nodes list", e, node);
						continue;
					}
				}
				try {
					NodesInfoResponse nodeInfo = transportService.submitRequest(node, NodesInfoAction.NAME,
							Requests.nodesInfoRequest("_local"),
							TransportRequestOptions.options().withTimeout(pingTimeout),
							new FutureTransportResponseHandler<NodesInfoResponse>() {
								@Override
								public NodesInfoResponse newInstance() {
									return new NodesInfoResponse();
								}
							}).txGet();
					if (!clusterName.equals(nodeInfo.clusterName())) {
						logger.warn("node {} not part of the cluster {}, ignoring...", node, clusterName);
					} else {
						newNodes.add(node);
					}
				} catch (Exception e) {
					logger.info("failed to get node info for {}, disconnecting...", e, node);
					transportService.disconnectFromNode(node);
				}
			}
			nodes = new ImmutableList.Builder<DiscoveryNode>().addAll(newNodes).build();
		}
	}

	/**
	 * The Class SniffNodesSampler.
	 *
	 * @author l.xue.nong
	 */
	class SniffNodesSampler implements NodeSampler {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.client.transport.TransportClientNodesService.NodeSampler#sample()
		 */
		@Override
		public synchronized void sample() {
			if (closed) {
				return;
			}

			Map<TransportAddress, DiscoveryNode> nodesToPing = Maps.newHashMap();
			for (DiscoveryNode node : listedNodes) {
				nodesToPing.put(node.address(), node);
			}
			for (DiscoveryNode node : nodes) {
				nodesToPing.put(node.address(), node);
			}

			final CountDownLatch latch = new CountDownLatch(nodesToPing.size());
			final CopyOnWriteArrayList<NodesInfoResponse> nodesInfoResponses = new CopyOnWriteArrayList<NodesInfoResponse>();
			for (final DiscoveryNode listedNode : nodesToPing.values()) {
				threadPool.executor(ThreadPool.Names.MANAGEMENT).execute(new Runnable() {
					@Override
					public void run() {
						try {
							if (!transportService.nodeConnected(listedNode)) {
								try {
									transportService.connectToNode(listedNode);
								} catch (Exception e) {
									logger.debug("failed to connect to node [{}], removed from nodes list", e,
											listedNode);
									return;
								}
							}
							transportService.sendRequest(listedNode, NodesInfoAction.NAME,
									Requests.nodesInfoRequest("_all"),
									TransportRequestOptions.options().withTimeout(pingTimeout),
									new BaseTransportResponseHandler<NodesInfoResponse>() {

										@Override
										public NodesInfoResponse newInstance() {
											return new NodesInfoResponse();
										}

										@Override
										public String executor() {
											return ThreadPool.Names.SAME;
										}

										@Override
										public void handleResponse(NodesInfoResponse response) {
											nodesInfoResponses.add(response);
											latch.countDown();
										}

										@Override
										public void handleException(TransportException e) {
											logger.info("failed to get node info for {}, disconnecting...", e,
													listedNode);
											transportService.disconnectFromNode(listedNode);
											latch.countDown();
										}
									});
						} catch (Exception e) {
							logger.info("failed to get node info for {}, disconnecting...", e, listedNode);
							transportService.disconnectFromNode(listedNode);
							latch.countDown();
						}
					}
				});
			}

			try {
				latch.await();
			} catch (InterruptedException e) {
				return;
			}

			HashSet<DiscoveryNode> newNodes = new HashSet<DiscoveryNode>();
			for (NodesInfoResponse nodesInfoResponse : nodesInfoResponses) {
				for (NodeInfo nodeInfo : nodesInfoResponse) {
					if (!clusterName.equals(nodesInfoResponse.clusterName())) {
						logger.warn("node {} not part of the cluster {}, ignoring...", nodeInfo.node(), clusterName);
					} else {
						if (nodeInfo.node().dataNode()) {
							newNodes.add(nodeInfo.node());
						}
					}
				}
			}

			for (Iterator<DiscoveryNode> it = newNodes.iterator(); it.hasNext();) {
				DiscoveryNode node = it.next();
				try {
					transportService.connectToNode(node);
				} catch (Exception e) {
					it.remove();
					logger.debug("failed to connect to discovered node [" + node + "]", e);
				}
			}
			nodes = new ImmutableList.Builder<DiscoveryNode>().addAll(newNodes).build();
		}
	}

	/**
	 * The Interface NodeCallback.
	 *
	 * @param <T> the generic type
	 * @author l.xue.nong
	 */
	public static interface NodeCallback<T> {

		/**
		 * Do with node.
		 *
		 * @param node the node
		 * @return the t
		 * @throws RebirthException the rebirth exception
		 */
		T doWithNode(DiscoveryNode node) throws RebirthException;
	}

	/**
	 * The Interface NodeListenerCallback.
	 *
	 * @param <Response> the generic type
	 * @author l.xue.nong
	 */
	public static interface NodeListenerCallback<Response> {

		/**
		 * Do with node.
		 *
		 * @param node the node
		 * @param listener the listener
		 * @throws RebirthException the rebirth exception
		 */
		void doWithNode(DiscoveryNode node, ActionListener<Response> listener) throws RebirthException;
	}
}
