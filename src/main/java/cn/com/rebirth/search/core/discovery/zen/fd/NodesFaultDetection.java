/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesFaultDetection.java 2012-7-6 14:29:20 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery.zen.fd;

import static cn.com.rebirth.search.core.cluster.node.DiscoveryNodes.EMPTY_NODES;
import static cn.com.rebirth.search.core.transport.TransportRequestOptions.options;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.ConnectTransportException;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportConnectionListener;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Maps;

/**
 * The Class NodesFaultDetection.
 *
 * @author l.xue.nong
 */
public class NodesFaultDetection extends AbstractComponent {

	/**
	 * The Interface Listener.
	 *
	 * @author l.xue.nong
	 */
	public static interface Listener {

		/**
		 * On node failure.
		 *
		 * @param node the node
		 * @param reason the reason
		 */
		void onNodeFailure(DiscoveryNode node, String reason);
	}

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The transport service. */
	private final TransportService transportService;

	/** The connect on network disconnect. */
	private final boolean connectOnNetworkDisconnect;

	/** The ping interval. */
	private final TimeValue pingInterval;

	/** The ping retry timeout. */
	private final TimeValue pingRetryTimeout;

	/** The ping retry count. */
	private final int pingRetryCount;

	/** The register connection listener. */
	private final boolean registerConnectionListener;

	/** The listeners. */
	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	/** The nodes fd. */
	private final ConcurrentMap<DiscoveryNode, NodeFD> nodesFD = Maps.newConcurrentMap();

	/** The connection listener. */
	private final FDConnectionListener connectionListener;

	/** The latest nodes. */
	private volatile DiscoveryNodes latestNodes = EMPTY_NODES;

	/** The running. */
	private volatile boolean running = false;

	/**
	 * Instantiates a new nodes fault detection.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 */
	public NodesFaultDetection(Settings settings, ThreadPool threadPool, TransportService transportService) {
		super(settings);
		this.threadPool = threadPool;
		this.transportService = transportService;

		this.connectOnNetworkDisconnect = componentSettings.getAsBoolean("connect_on_network_disconnect", true);
		this.pingInterval = componentSettings.getAsTime("ping_interval", TimeValue.timeValueSeconds(1));
		this.pingRetryTimeout = componentSettings.getAsTime("ping_timeout", TimeValue.timeValueSeconds(30));
		this.pingRetryCount = componentSettings.getAsInt("ping_retries", 3);
		this.registerConnectionListener = componentSettings.getAsBoolean("register_connection_listener", true);

		logger.debug("[node  ] uses ping_interval [" + pingInterval + "], ping_timeout [" + pingRetryTimeout
				+ "], ping_retries [" + pingRetryCount + "]");

		transportService.registerHandler(PingRequestHandler.ACTION, new PingRequestHandler());

		this.connectionListener = new FDConnectionListener();
		if (registerConnectionListener) {
			transportService.addConnectionListener(connectionListener);
		}
	}

	/**
	 * Adds the listener.
	 *
	 * @param listener the listener
	 */
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the listener.
	 *
	 * @param listener the listener
	 */
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	/**
	 * Update nodes.
	 *
	 * @param nodes the nodes
	 */
	public void updateNodes(DiscoveryNodes nodes) {
		DiscoveryNodes prevNodes = latestNodes;
		this.latestNodes = nodes;
		if (!running) {
			return;
		}
		DiscoveryNodes.Delta delta = nodes.delta(prevNodes);
		for (DiscoveryNode newNode : delta.addedNodes()) {
			if (newNode.id().equals(nodes.localNodeId())) {

				continue;
			}
			if (!nodesFD.containsKey(newNode)) {
				nodesFD.put(newNode, new NodeFD());
				threadPool.schedule(pingInterval, ThreadPool.Names.SAME, new SendPingRequest(newNode));
			}
		}
		for (DiscoveryNode removedNode : delta.removedNodes()) {
			nodesFD.remove(removedNode);
		}
	}

	/**
	 * Start.
	 *
	 * @return the nodes fault detection
	 */
	public NodesFaultDetection start() {
		if (running) {
			return this;
		}
		running = true;
		return this;
	}

	/**
	 * Stop.
	 *
	 * @return the nodes fault detection
	 */
	public NodesFaultDetection stop() {
		if (!running) {
			return this;
		}
		running = false;
		return this;
	}

	/**
	 * Close.
	 */
	public void close() {
		stop();
		transportService.removeHandler(PingRequestHandler.ACTION);
		transportService.removeConnectionListener(connectionListener);
	}

	/**
	 * Handle transport disconnect.
	 *
	 * @param node the node
	 */
	private void handleTransportDisconnect(DiscoveryNode node) {
		if (!latestNodes.nodeExists(node.id())) {
			return;
		}
		NodeFD nodeFD = nodesFD.remove(node);
		if (nodeFD == null) {
			return;
		}
		if (!running) {
			return;
		}
		nodeFD.running = false;
		if (connectOnNetworkDisconnect) {
			try {
				transportService.connectToNode(node);
				nodesFD.put(node, new NodeFD());
				threadPool.schedule(pingInterval, ThreadPool.Names.SAME, new SendPingRequest(node));
			} catch (Exception e) {
				logger.trace("[node  ] [{}] transport disconnected (with verified connect)", node);
				notifyNodeFailure(node, "transport disconnected (with verified connect)");
			}
		} else {
			logger.trace("[node  ] [{}] transport disconnected", node);
			notifyNodeFailure(node, "transport disconnected");
		}
	}

	/**
	 * Notify node failure.
	 *
	 * @param node the node
	 * @param reason the reason
	 */
	private void notifyNodeFailure(final DiscoveryNode node, final String reason) {
		threadPool.generic().execute(new Runnable() {
			@Override
			public void run() {
				for (Listener listener : listeners) {
					listener.onNodeFailure(node, reason);
				}
			}
		});
	}

	/**
	 * The Class SendPingRequest.
	 *
	 * @author l.xue.nong
	 */
	private class SendPingRequest implements Runnable {

		/** The node. */
		private final DiscoveryNode node;

		/**
		 * Instantiates a new send ping request.
		 *
		 * @param node the node
		 */
		private SendPingRequest(DiscoveryNode node) {
			this.node = node;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (!running) {
				return;
			}
			transportService.sendRequest(node, PingRequestHandler.ACTION, new PingRequest(node.id()), options()
					.withHighType().withTimeout(pingRetryTimeout), new BaseTransportResponseHandler<PingResponse>() {
				@Override
				public PingResponse newInstance() {
					return new PingResponse();
				}

				@Override
				public void handleResponse(PingResponse response) {
					if (!running) {
						return;
					}
					NodeFD nodeFD = nodesFD.get(node);
					if (nodeFD != null) {
						if (!nodeFD.running) {
							return;
						}
						nodeFD.retryCount = 0;
						threadPool.schedule(pingInterval, ThreadPool.Names.SAME, SendPingRequest.this);
					}
				}

				@Override
				public void handleException(TransportException exp) {

					if (!running) {
						return;
					}
					if (exp instanceof ConnectTransportException) {

						return;
					}
					NodeFD nodeFD = nodesFD.get(node);
					if (nodeFD != null) {
						if (!nodeFD.running) {
							return;
						}
						int retryCount = ++nodeFD.retryCount;
						logger.trace("[node  ] failed to ping [" + node + "], retry [" + retryCount + "] out of ["
								+ pingRetryCount + "]", exp);
						if (retryCount >= pingRetryCount) {
							logger.debug("[node  ] failed to ping [" + node + "], tried [" + pingRetryCount
									+ "] times, each with  maximum [" + pingRetryTimeout + "] timeout");

							if (nodesFD.remove(node) != null) {
								notifyNodeFailure(node, "failed to ping, tried [" + pingRetryCount
										+ "] times, each with maximum [" + pingRetryTimeout + "] timeout");
							}
						} else {

							transportService.sendRequest(node, PingRequestHandler.ACTION, new PingRequest(node.id()),
									options().withHighType().withTimeout(pingRetryTimeout), this);
						}
					}
				}

				@Override
				public String executor() {
					return ThreadPool.Names.SAME;
				}
			});
		}
	}

	/**
	 * The Class NodeFD.
	 *
	 * @author l.xue.nong
	 */
	static class NodeFD {

		/** The retry count. */
		volatile int retryCount;

		/** The running. */
		volatile boolean running = true;
	}

	/**
	 * The listener interface for receiving FDConnection events.
	 * The class that is interested in processing a FDConnection
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addFDConnectionListener<code> method. When
	 * the FDConnection event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see FDConnectionEvent
	 */
	private class FDConnectionListener implements TransportConnectionListener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportConnectionListener#onNodeConnected(cn.com.rebirth.search.core.cluster.node.DiscoveryNode)
		 */
		@Override
		public void onNodeConnected(DiscoveryNode node) {
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportConnectionListener#onNodeDisconnected(cn.com.rebirth.search.core.cluster.node.DiscoveryNode)
		 */
		@Override
		public void onNodeDisconnected(DiscoveryNode node) {
			handleTransportDisconnect(node);
		}
	}

	/**
	 * The Class PingRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	class PingRequestHandler extends BaseTransportRequestHandler<PingRequest> {

		/** The Constant ACTION. */
		public static final String ACTION = "discovery/zen/fd/ping";

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public PingRequest newInstance() {
			return new PingRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(PingRequest request, TransportChannel channel) throws Exception {

			if (!latestNodes.localNodeId().equals(request.nodeId)) {
				throw new RebirthIllegalStateException("Got pinged as node [" + request.nodeId + "], but I am node ["
						+ latestNodes.localNodeId() + "]");
			}
			channel.sendResponse(new PingResponse());
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
	 * The Class PingRequest.
	 *
	 * @author l.xue.nong
	 */
	static class PingRequest implements Streamable {

		/** The node id. */
		private String nodeId;

		/**
		 * Instantiates a new ping request.
		 */
		PingRequest() {
		}

		/**
		 * Instantiates a new ping request.
		 *
		 * @param nodeId the node id
		 */
		PingRequest(String nodeId) {
			this.nodeId = nodeId;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			nodeId = in.readUTF();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(nodeId);
		}
	}

	/**
	 * The Class PingResponse.
	 *
	 * @author l.xue.nong
	 */
	private static class PingResponse implements Streamable {

		/**
		 * Instantiates a new ping response.
		 */
		private PingResponse() {
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
		}
	}
}
