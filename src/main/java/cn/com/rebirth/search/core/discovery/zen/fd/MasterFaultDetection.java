/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MasterFaultDetection.java 2012-7-6 14:30:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery.zen.fd;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

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
import cn.com.rebirth.search.core.discovery.zen.DiscoveryNodesProvider;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.ConnectTransportException;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportConnectionListener;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class MasterFaultDetection.
 *
 * @author l.xue.nong
 */
public class MasterFaultDetection extends AbstractComponent {

	/**
	 * The Interface Listener.
	 *
	 * @author l.xue.nong
	 */
	public static interface Listener {

		/**
		 * On master failure.
		 *
		 * @param masterNode the master node
		 * @param reason the reason
		 */
		void onMasterFailure(DiscoveryNode masterNode, String reason);

		/**
		 * On disconnected from master.
		 */
		void onDisconnectedFromMaster();
	}

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The transport service. */
	private final TransportService transportService;

	/** The nodes provider. */
	private final DiscoveryNodesProvider nodesProvider;

	/** The listeners. */
	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();

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

	/** The connection listener. */
	private final FDConnectionListener connectionListener;

	/** The master pinger. */
	private volatile MasterPinger masterPinger;

	/** The master node mutex. */
	private final Object masterNodeMutex = new Object();

	/** The master node. */
	private volatile DiscoveryNode masterNode;

	/** The retry count. */
	private volatile int retryCount;

	/** The notified master failure. */
	private final AtomicBoolean notifiedMasterFailure = new AtomicBoolean();

	/**
	 * Instantiates a new master fault detection.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param nodesProvider the nodes provider
	 */
	public MasterFaultDetection(Settings settings, ThreadPool threadPool, TransportService transportService,
			DiscoveryNodesProvider nodesProvider) {
		super(settings);
		this.threadPool = threadPool;
		this.transportService = transportService;
		this.nodesProvider = nodesProvider;

		this.connectOnNetworkDisconnect = componentSettings.getAsBoolean("connect_on_network_disconnect", true);
		this.pingInterval = componentSettings.getAsTime("ping_interval", TimeValue.timeValueSeconds(1));
		this.pingRetryTimeout = componentSettings.getAsTime("ping_timeout", TimeValue.timeValueSeconds(30));
		this.pingRetryCount = componentSettings.getAsInt("ping_retries", 3);
		this.registerConnectionListener = componentSettings.getAsBoolean("register_connection_listener", true);

		logger.debug("[master] uses ping_interval [" + pingInterval + "], ping_timeout [" + pingRetryTimeout
				+ "], ping_retries [" + pingRetryCount + "]");

		this.connectionListener = new FDConnectionListener();
		if (registerConnectionListener) {
			transportService.addConnectionListener(connectionListener);
		}

		transportService.registerHandler(MasterPingRequestHandler.ACTION, new MasterPingRequestHandler());
	}

	/**
	 * Master node.
	 *
	 * @return the discovery node
	 */
	public DiscoveryNode masterNode() {
		return this.masterNode;
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
	 * Restart.
	 *
	 * @param masterNode the master node
	 * @param reason the reason
	 */
	public void restart(DiscoveryNode masterNode, String reason) {
		synchronized (masterNodeMutex) {
			if (logger.isDebugEnabled()) {
				logger.debug("[master] restarting fault detection against master [{}], reason [{}]", masterNode, reason);
			}
			innerStop();
			innerStart(masterNode);
		}
	}

	/**
	 * Start.
	 *
	 * @param masterNode the master node
	 * @param reason the reason
	 */
	public void start(final DiscoveryNode masterNode, String reason) {
		synchronized (masterNodeMutex) {
			if (logger.isDebugEnabled()) {
				logger.debug("[master] starting fault detection against master [{}], reason [{}]", masterNode, reason);
			}
			innerStart(masterNode);
		}
	}

	/**
	 * Inner start.
	 *
	 * @param masterNode the master node
	 */
	private void innerStart(final DiscoveryNode masterNode) {
		this.masterNode = masterNode;
		this.retryCount = 0;
		this.notifiedMasterFailure.set(false);

		try {
			transportService.connectToNode(masterNode);
		} catch (final Exception e) {

			notifyMasterFailure(masterNode, "failed to perform initial connect [" + e.getMessage() + "]");
			return;
		}
		if (masterPinger != null) {
			masterPinger.stop();
		}
		this.masterPinger = new MasterPinger();

		threadPool.schedule(pingInterval, ThreadPool.Names.SAME, masterPinger);
	}

	/**
	 * Stop.
	 *
	 * @param reason the reason
	 */
	public void stop(String reason) {
		synchronized (masterNodeMutex) {
			if (masterNode != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("[master] stopping fault detection against master [{}], reason [{}]", masterNode,
							reason);
				}
			}
			innerStop();
		}
	}

	/**
	 * Inner stop.
	 */
	private void innerStop() {

		this.retryCount = 0;
		if (masterPinger != null) {
			masterPinger.stop();
			masterPinger = null;
		}
		this.masterNode = null;
	}

	/**
	 * Close.
	 */
	public void close() {
		stop("closing");
		this.listeners.clear();
		transportService.removeConnectionListener(connectionListener);
		transportService.removeHandler(MasterPingRequestHandler.ACTION);
	}

	/**
	 * Handle transport disconnect.
	 *
	 * @param node the node
	 */
	private void handleTransportDisconnect(DiscoveryNode node) {
		synchronized (masterNodeMutex) {
			if (!node.equals(this.masterNode)) {
				return;
			}
			if (connectOnNetworkDisconnect) {
				try {
					transportService.connectToNode(node);

					if (masterPinger != null) {
						masterPinger.stop();
					}
					this.masterPinger = new MasterPinger();
					threadPool.schedule(pingInterval, ThreadPool.Names.SAME, masterPinger);
				} catch (Exception e) {
					logger.trace("[master] [{}] transport disconnected (with verified connect)", masterNode);
					notifyMasterFailure(masterNode, "transport disconnected (with verified connect)");
				}
			} else {
				logger.trace("[master] [{}] transport disconnected", node);
				notifyMasterFailure(node, "transport disconnected");
			}
		}
	}

	/**
	 * Notify disconnected from master.
	 */
	private void notifyDisconnectedFromMaster() {
		threadPool.generic().execute(new Runnable() {
			@Override
			public void run() {
				for (Listener listener : listeners) {
					listener.onDisconnectedFromMaster();
				}
			}
		});
	}

	/**
	 * Notify master failure.
	 *
	 * @param masterNode the master node
	 * @param reason the reason
	 */
	private void notifyMasterFailure(final DiscoveryNode masterNode, final String reason) {
		if (notifiedMasterFailure.compareAndSet(false, true)) {
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					for (Listener listener : listeners) {
						listener.onMasterFailure(masterNode, reason);
					}
				}
			});
			stop("master failure, " + reason);
		}
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
	 * The Class MasterPinger.
	 *
	 * @author l.xue.nong
	 */
	private class MasterPinger implements Runnable {

		/** The running. */
		private volatile boolean running = true;

		/**
		 * Stop.
		 */
		public void stop() {
			this.running = false;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (!running) {

				return;
			}
			final DiscoveryNode masterToPing = masterNode;
			if (masterToPing == null) {

				threadPool.schedule(pingInterval, ThreadPool.Names.SAME, MasterPinger.this);
				return;
			}
			transportService.sendRequest(masterToPing, MasterPingRequestHandler.ACTION, new MasterPingRequest(
					nodesProvider.nodes().localNode().id(), masterToPing.id()), TransportRequestOptions.options()
					.withHighType().withTimeout(pingRetryTimeout),
					new BaseTransportResponseHandler<MasterPingResponseResponse>() {
						@Override
						public MasterPingResponseResponse newInstance() {
							return new MasterPingResponseResponse();
						}

						@Override
						public void handleResponse(MasterPingResponseResponse response) {
							if (!running) {
								return;
							}

							MasterFaultDetection.this.retryCount = 0;

							if (masterToPing.equals(MasterFaultDetection.this.masterNode())) {
								if (!response.connectedToMaster) {
									logger.trace("[master] [{}] does not have us registered with it...", masterToPing);
									notifyDisconnectedFromMaster();
								}

								threadPool.schedule(pingInterval, ThreadPool.Names.SAME, MasterPinger.this);
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
							synchronized (masterNodeMutex) {

								if (masterToPing.equals(MasterFaultDetection.this.masterNode())) {
									if (exp.getCause() instanceof NoLongerMasterException) {
										logger.debug("[master] pinging a master {" + masterNode
												+ "} that is no longer a master");
										notifyMasterFailure(masterToPing, "no longer master");
									}
									int retryCount = ++MasterFaultDetection.this.retryCount;
									logger.trace("[master] failed to ping [" + masterNode + "], retry [" + retryCount
											+ "] out of [" + pingRetryCount + "]", exp);
									if (retryCount >= pingRetryCount) {
										logger.debug("[master] failed to ping [" + masterNode + "], tried ["
												+ pingRetryCount + "] times, each with maximum [" + pingRetryTimeout
												+ "] timeout");

										notifyMasterFailure(masterToPing, "failed to ping, tried [" + pingRetryCount
												+ "] times, each with  maximum [" + pingRetryTimeout + "] timeout");
									} else {

										transportService.sendRequest(masterToPing, MasterPingRequestHandler.ACTION,
												new MasterPingRequest(nodesProvider.nodes().localNode().id(),
														masterToPing.id()), TransportRequestOptions.options()
														.withHighType().withTimeout(pingRetryTimeout), this);
									}
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
	 * The Class NoLongerMasterException.
	 *
	 * @author l.xue.nong
	 */
	private static class NoLongerMasterException extends RebirthIllegalStateException {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -6283719009619554381L;

		/* (non-Javadoc)
		 * @see java.lang.Throwable#fillInStackTrace()
		 */
		@Override
		public Throwable fillInStackTrace() {
			return null;
		}
	}

	/**
	 * The Class MasterPingRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	private class MasterPingRequestHandler extends BaseTransportRequestHandler<MasterPingRequest> {

		/** The Constant ACTION. */
		public static final String ACTION = "discovery/zen/fd/masterPing";

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public MasterPingRequest newInstance() {
			return new MasterPingRequest();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportRequestHandler#messageReceived(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(MasterPingRequest request, TransportChannel channel) throws Exception {
			DiscoveryNodes nodes = nodesProvider.nodes();

			if (!request.masterNodeId.equals(nodes.localNodeId())) {
				throw new RebirthIllegalStateException("Got ping as master with id [" + request.masterNodeId
						+ "], but not master and no id");
			}

			if (!nodes.localNodeMaster()) {
				throw new NoLongerMasterException();
			}

			channel.sendResponse(new MasterPingResponseResponse(nodes.nodeExists(request.nodeId)));
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
	 * The Class MasterPingRequest.
	 *
	 * @author l.xue.nong
	 */
	private static class MasterPingRequest implements Streamable {

		/** The node id. */
		private String nodeId;

		/** The master node id. */
		private String masterNodeId;

		/**
		 * Instantiates a new master ping request.
		 */
		private MasterPingRequest() {
		}

		/**
		 * Instantiates a new master ping request.
		 *
		 * @param nodeId the node id
		 * @param masterNodeId the master node id
		 */
		private MasterPingRequest(String nodeId, String masterNodeId) {
			this.nodeId = nodeId;
			this.masterNodeId = masterNodeId;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			nodeId = in.readUTF();
			masterNodeId = in.readUTF();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(nodeId);
			out.writeUTF(masterNodeId);
		}
	}

	/**
	 * The Class MasterPingResponseResponse.
	 *
	 * @author l.xue.nong
	 */
	private static class MasterPingResponseResponse implements Streamable {

		/** The connected to master. */
		private boolean connectedToMaster;

		/**
		 * Instantiates a new master ping response response.
		 */
		private MasterPingResponseResponse() {
		}

		/**
		 * Instantiates a new master ping response response.
		 *
		 * @param connectedToMaster the connected to master
		 */
		private MasterPingResponseResponse(boolean connectedToMaster) {
			this.connectedToMaster = connectedToMaster;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			connectedToMaster = in.readBoolean();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeBoolean(connectedToMaster);
		}
	}
}
