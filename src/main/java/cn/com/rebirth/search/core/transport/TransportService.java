/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportService.java 2012-7-6 14:30:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.concurrent.ConcurrentMapLong;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.search.SearchConstants;
import cn.com.rebirth.commons.search.config.support.ZooKeeperExpand;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.commons.metrics.MeanMetric;
import cn.com.rebirth.search.commons.transport.BoundTransportAddress;
import cn.com.rebirth.search.commons.transport.TransportAddress;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

import com.google.common.collect.ImmutableMap;

/**
 * The Class TransportService.
 *
 * @author l.xue.nong
 */
public class TransportService extends AbstractLifecycleComponent<TransportService> {

	/** The transport. */
	private final Transport transport;

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The server handlers. */
	volatile ImmutableMap<String, TransportRequestHandler> serverHandlers = ImmutableMap.of();

	/** The server handlers mutex. */
	final Object serverHandlersMutex = new Object();

	/** The client handlers. */
	final ConcurrentMapLong<RequestHolder> clientHandlers = ConcurrentCollections.newConcurrentMapLong();

	/** The request ids. */
	final AtomicLong requestIds = new AtomicLong();

	/** The connection listeners. */
	final CopyOnWriteArrayList<TransportConnectionListener> connectionListeners = new CopyOnWriteArrayList<TransportConnectionListener>();

	/** The timeout info handlers. */
	final Map<Long, TimeoutInfoHolder> timeoutInfoHandlers = Collections
			.synchronizedMap(new LinkedHashMap<Long, TimeoutInfoHolder>(100, .75F, true) {
				protected boolean removeEldestEntry(Map.Entry eldest) {
					return size() > 100;
				}
			});

	/** The throw connect exception. */
	private boolean throwConnectException = false;

	/** The adapter. */
	private final TransportService.Adapter adapter = new Adapter();

	/**
	 * Instantiates a new transport service.
	 *
	 * @param transport the transport
	 * @param threadPool the thread pool
	 */
	public TransportService(Transport transport, ThreadPool threadPool) {
		this(ImmutableSettings.Builder.EMPTY_SETTINGS, transport, threadPool);
	}

	/**
	 * Instantiates a new transport service.
	 *
	 * @param settings the settings
	 * @param transport the transport
	 * @param threadPool the thread pool
	 */
	@Inject
	public TransportService(Settings settings, Transport transport, ThreadPool threadPool) {
		super(settings);
		this.transport = transport;
		this.threadPool = threadPool;
	}

	/**
	 * Check zk jar.
	 *
	 * @return true, if successful
	 */
	protected boolean checkZkJar() {
		try {
			Class.forName("org.apache.zookeeper.ZooKeeper", false, Thread.currentThread().getContextClassLoader());
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
		adapter.rxMetric.clear();
		adapter.txMetric.clear();
		transport.transportServiceAdapter(adapter);
		transport.start();
		if (transport.boundAddress() != null && logger.isInfoEnabled()) {
			logger.info("{}", transport.boundAddress());
		}

		boolean isClientNode = settings.getAsBoolean("node.client", false);
		if (!isClientNode && checkZkJar()) {
			String nodeName = settings.get("name");
			ZooKeeperExpand.getInstance().create(SearchConstants.getRebirthSearchBulidZKConfig() + "/" + nodeName,
					transport.boundAddress().publishAddress());
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {

		boolean isClientNode = settings.getAsBoolean("node.client", false);
		if (!isClientNode && checkZkJar()) {
			String nodeName = settings.get("name");
			ZooKeeperExpand.getInstance().delete(SearchConstants.getRebirthSearchBulidZKConfig() + "/" + nodeName);
		}
		transport.stop();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
		transport.close();
	}

	/**
	 * Address supported.
	 *
	 * @param address the address
	 * @return true, if successful
	 */
	public boolean addressSupported(Class<? extends TransportAddress> address) {
		return transport.addressSupported(address);
	}

	/**
	 * Info.
	 *
	 * @return the transport info
	 */
	public TransportInfo info() {
		return new TransportInfo(boundAddress());
	}

	/**
	 * Stats.
	 *
	 * @return the transport stats
	 */
	public TransportStats stats() {
		return new TransportStats(transport.serverOpen(), adapter.rxMetric.count(), adapter.rxMetric.sum(),
				adapter.txMetric.count(), adapter.txMetric.sum());
	}

	/**
	 * Bound address.
	 *
	 * @return the bound transport address
	 */
	public BoundTransportAddress boundAddress() {
		return transport.boundAddress();
	}

	/**
	 * Node connected.
	 *
	 * @param node the node
	 * @return true, if successful
	 */
	public boolean nodeConnected(DiscoveryNode node) {
		return transport.nodeConnected(node);
	}

	/**
	 * Connect to node.
	 *
	 * @param node the node
	 * @throws ConnectTransportException the connect transport exception
	 */
	public void connectToNode(DiscoveryNode node) throws ConnectTransportException {
		transport.connectToNode(node);
	}

	/**
	 * Connect to node light.
	 *
	 * @param node the node
	 * @throws ConnectTransportException the connect transport exception
	 */
	public void connectToNodeLight(DiscoveryNode node) throws ConnectTransportException {
		transport.connectToNodeLight(node);
	}

	/**
	 * Disconnect from node.
	 *
	 * @param node the node
	 */
	public void disconnectFromNode(DiscoveryNode node) {
		transport.disconnectFromNode(node);
	}

	/**
	 * Adds the connection listener.
	 *
	 * @param listener the listener
	 */
	public void addConnectionListener(TransportConnectionListener listener) {
		connectionListeners.add(listener);
	}

	/**
	 * Removes the connection listener.
	 *
	 * @param listener the listener
	 */
	public void removeConnectionListener(TransportConnectionListener listener) {
		connectionListeners.remove(listener);
	}

	/**
	 * Throw connect exception.
	 *
	 * @param throwConnectException the throw connect exception
	 */
	public void throwConnectException(boolean throwConnectException) {
		this.throwConnectException = throwConnectException;
	}

	/**
	 * Submit request.
	 *
	 * @param <T> the generic type
	 * @param node the node
	 * @param action the action
	 * @param message the message
	 * @param handler the handler
	 * @return the transport future
	 * @throws TransportException the transport exception
	 */
	public <T extends Streamable> TransportFuture<T> submitRequest(DiscoveryNode node, String action,
			Streamable message, TransportResponseHandler<T> handler) throws TransportException {
		return submitRequest(node, action, message, TransportRequestOptions.EMPTY, handler);
	}

	/**
	 * Submit request.
	 *
	 * @param <T> the generic type
	 * @param node the node
	 * @param action the action
	 * @param message the message
	 * @param options the options
	 * @param handler the handler
	 * @return the transport future
	 * @throws TransportException the transport exception
	 */
	public <T extends Streamable> TransportFuture<T> submitRequest(DiscoveryNode node, String action,
			Streamable message, TransportRequestOptions options, TransportResponseHandler<T> handler)
			throws TransportException {
		PlainTransportFuture<T> futureHandler = new PlainTransportFuture<T>(handler);
		sendRequest(node, action, message, options, futureHandler);
		return futureHandler;
	}

	/**
	 * Send request.
	 *
	 * @param <T> the generic type
	 * @param node the node
	 * @param action the action
	 * @param message the message
	 * @param handler the handler
	 * @throws TransportException the transport exception
	 */
	public <T extends Streamable> void sendRequest(final DiscoveryNode node, final String action,
			final Streamable message, final TransportResponseHandler<T> handler) throws TransportException {
		sendRequest(node, action, message, TransportRequestOptions.EMPTY, handler);
	}

	/**
	 * Send request.
	 *
	 * @param <T> the generic type
	 * @param node the node
	 * @param action the action
	 * @param message the message
	 * @param options the options
	 * @param handler the handler
	 * @throws TransportException the transport exception
	 */
	public <T extends Streamable> void sendRequest(final DiscoveryNode node, final String action,
			final Streamable message, final TransportRequestOptions options, final TransportResponseHandler<T> handler)
			throws TransportException {
		final long requestId = newRequestId();
		TimeoutHandler timeoutHandler = null;
		try {
			if (options.timeout() != null) {
				timeoutHandler = new TimeoutHandler(requestId);
				timeoutHandler.future = threadPool
						.schedule(options.timeout(), ThreadPool.Names.GENERIC, timeoutHandler);
			}
			clientHandlers.put(requestId, new RequestHolder<T>(handler, node, action, timeoutHandler));
			transport.sendRequest(node, requestId, action, message, options);
		} catch (final Exception e) {

			clientHandlers.remove(requestId);
			if (timeoutHandler != null) {
				timeoutHandler.future.cancel(false);
			}
			if (throwConnectException) {
				if (e instanceof ConnectTransportException) {
					throw (ConnectTransportException) e;
				}
			}

			final SendRequestTransportException sendRequestException = new SendRequestTransportException(node, action,
					e);
			threadPool.executor(ThreadPool.Names.GENERIC).execute(new Runnable() {
				@Override
				public void run() {
					handler.handleException(sendRequestException);
				}
			});
		}
	}

	/**
	 * New request id.
	 *
	 * @return the long
	 */
	private long newRequestId() {
		return requestIds.getAndIncrement();
	}

	/**
	 * Addresses from string.
	 *
	 * @param address the address
	 * @return the transport address[]
	 * @throws Exception the exception
	 */
	public TransportAddress[] addressesFromString(String address) throws Exception {
		return transport.addressesFromString(address);
	}

	/**
	 * Register handler.
	 *
	 * @param handler the handler
	 */
	public void registerHandler(ActionTransportRequestHandler handler) {
		registerHandler(handler.action(), handler);
	}

	/**
	 * Register handler.
	 *
	 * @param action the action
	 * @param handler the handler
	 */
	public void registerHandler(String action, TransportRequestHandler handler) {
		synchronized (serverHandlersMutex) {
			TransportRequestHandler handlerReplaced = serverHandlers.get(action);
			serverHandlers = MapBuilder.newMapBuilder(serverHandlers).put(action, handler).immutableMap();
			if (handlerReplaced != null) {
				logger.warn("Registered two transport handlers for action " + action + ", handlers: " + handler + ", "
						+ handlerReplaced);
			}
		}
	}

	/**
	 * Removes the handler.
	 *
	 * @param action the action
	 */
	public void removeHandler(String action) {
		synchronized (serverHandlersMutex) {
			serverHandlers = MapBuilder.newMapBuilder(serverHandlers).remove(action).immutableMap();
		}
	}

	/**
	 * The Class Adapter.
	 *
	 * @author l.xue.nong
	 */
	class Adapter implements TransportServiceAdapter {

		/** The rx metric. */
		final MeanMetric rxMetric = new MeanMetric();

		/** The tx metric. */
		final MeanMetric txMetric = new MeanMetric();

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportServiceAdapter#received(long)
		 */
		@Override
		public void received(long size) {
			rxMetric.inc(size);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportServiceAdapter#sent(long)
		 */
		@Override
		public void sent(long size) {
			txMetric.inc(size);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportServiceAdapter#handler(java.lang.String)
		 */
		@Override
		public TransportRequestHandler handler(String action) {
			return serverHandlers.get(action);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportServiceAdapter#remove(long)
		 */
		@Override
		public TransportResponseHandler remove(long requestId) {
			RequestHolder holder = clientHandlers.remove(requestId);
			if (holder == null) {

				TimeoutInfoHolder timeoutInfoHolder = timeoutInfoHandlers.remove(requestId);
				if (timeoutInfoHolder != null) {
					long time = System.currentTimeMillis();
					logger.warn("Received response for a request that has timed out, sent ["
							+ (time - timeoutInfoHolder.sentTime()) + "ms] ago, timed out ["
							+ (time - timeoutInfoHolder.timeoutTime()) + "ms] ago, action ["
							+ timeoutInfoHolder.action() + "], node [" + timeoutInfoHolder.node() + "], id ["
							+ requestId + "]");
				} else {
					logger.warn("Transport response handler not found of id [{}]", requestId);
				}
				return null;
			}
			holder.cancel();
			return holder.handler();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportServiceAdapter#raiseNodeConnected(cn.com.rebirth.search.core.cluster.node.DiscoveryNode)
		 */
		@Override
		public void raiseNodeConnected(final DiscoveryNode node) {
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					for (TransportConnectionListener connectionListener : connectionListeners) {
						connectionListener.onNodeConnected(node);
					}
				}
			});
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.transport.TransportServiceAdapter#raiseNodeDisconnected(cn.com.rebirth.search.core.cluster.node.DiscoveryNode)
		 */
		@Override
		public void raiseNodeDisconnected(final DiscoveryNode node) {
			if (lifecycle.stoppedOrClosed()) {
				return;
			}
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					for (TransportConnectionListener connectionListener : connectionListeners) {
						connectionListener.onNodeDisconnected(node);
					}

					for (Map.Entry<Long, RequestHolder> entry : clientHandlers.entrySet()) {
						RequestHolder holder = entry.getValue();
						if (holder.node().equals(node)) {
							final RequestHolder holderToNotify = clientHandlers.remove(entry.getKey());
							if (holderToNotify != null) {

								threadPool.generic().execute(new Runnable() {
									@Override
									public void run() {
										holderToNotify.handler().handleException(
												new NodeDisconnectedException(node, holderToNotify.action()));
									}
								});
							}
						}
					}
				}
			});
		}
	}

	/**
	 * The Class TimeoutHandler.
	 *
	 * @author l.xue.nong
	 */
	class TimeoutHandler implements Runnable {

		/** The request id. */
		private final long requestId;

		/** The sent time. */
		private final long sentTime = System.currentTimeMillis();

		/** The future. */
		ScheduledFuture future;

		/**
		 * Instantiates a new timeout handler.
		 *
		 * @param requestId the request id
		 */
		TimeoutHandler(long requestId) {
			this.requestId = requestId;
		}

		/**
		 * Sent time.
		 *
		 * @return the long
		 */
		public long sentTime() {
			return sentTime;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (future.isCancelled()) {
				return;
			}
			final RequestHolder holder = clientHandlers.remove(requestId);
			if (holder != null) {

				long timeoutTime = System.currentTimeMillis();
				timeoutInfoHandlers.put(requestId, new TimeoutInfoHolder(holder.node(), holder.action(), sentTime,
						timeoutTime));
				holder.handler().handleException(
						new ReceiveTimeoutTransportException(holder.node(), holder.action(), "request_id [" + requestId
								+ "] timed out after [" + (timeoutTime - sentTime) + "ms]"));
			}
		}
	}

	/**
	 * The Class TimeoutInfoHolder.
	 *
	 * @author l.xue.nong
	 */
	static class TimeoutInfoHolder {

		/** The node. */
		private final DiscoveryNode node;

		/** The action. */
		private final String action;

		/** The sent time. */
		private final long sentTime;

		/** The timeout time. */
		private final long timeoutTime;

		/**
		 * Instantiates a new timeout info holder.
		 *
		 * @param node the node
		 * @param action the action
		 * @param sentTime the sent time
		 * @param timeoutTime the timeout time
		 */
		TimeoutInfoHolder(DiscoveryNode node, String action, long sentTime, long timeoutTime) {
			this.node = node;
			this.action = action;
			this.sentTime = sentTime;
			this.timeoutTime = timeoutTime;
		}

		/**
		 * Node.
		 *
		 * @return the discovery node
		 */
		public DiscoveryNode node() {
			return node;
		}

		/**
		 * Action.
		 *
		 * @return the string
		 */
		public String action() {
			return action;
		}

		/**
		 * Sent time.
		 *
		 * @return the long
		 */
		public long sentTime() {
			return sentTime;
		}

		/**
		 * Timeout time.
		 *
		 * @return the long
		 */
		public long timeoutTime() {
			return timeoutTime;
		}
	}

	/**
	 * The Class RequestHolder.
	 *
	 * @param <T> the generic type
	 * @author l.xue.nong
	 */
	static class RequestHolder<T extends Streamable> {

		/** The handler. */
		private final TransportResponseHandler<T> handler;

		/** The node. */
		private final DiscoveryNode node;

		/** The action. */
		private final String action;

		/** The timeout. */
		private final TimeoutHandler timeout;

		/**
		 * Instantiates a new request holder.
		 *
		 * @param handler the handler
		 * @param node the node
		 * @param action the action
		 * @param timeout the timeout
		 */
		RequestHolder(TransportResponseHandler<T> handler, DiscoveryNode node, String action, TimeoutHandler timeout) {
			this.handler = handler;
			this.node = node;
			this.action = action;
			this.timeout = timeout;
		}

		/**
		 * Handler.
		 *
		 * @return the transport response handler
		 */
		public TransportResponseHandler<T> handler() {
			return handler;
		}

		/**
		 * Node.
		 *
		 * @return the discovery node
		 */
		public DiscoveryNode node() {
			return this.node;
		}

		/**
		 * Action.
		 *
		 * @return the string
		 */
		public String action() {
			return this.action;
		}

		/**
		 * Cancel.
		 */
		public void cancel() {
			if (timeout != null) {
				timeout.future.cancel(false);
			}
		}
	}
}