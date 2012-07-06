/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core UnicastZenPing.java 2012-3-29 15:02:35 l.xue.nong$$
 */


package cn.com.rebirth.search.core.discovery.zen.ping.unicast;

import static cn.com.rebirth.search.core.discovery.zen.ping.ZenPing.PingResponse.readPingResponse;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jsr166y.LinkedTransferQueue;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.concurrent.EsExecutors;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.exception.RestartIllegalStateException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.commons.transport.TransportAddress;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.discovery.zen.DiscoveryNodesProvider;
import cn.com.rebirth.search.core.discovery.zen.ping.ZenPing;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.BaseTransportResponseHandler;
import cn.com.rebirth.search.core.transport.ConnectTransportException;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Lists;


/**
 * The Class UnicastZenPing.
 *
 * @author l.xue.nong
 */
public class UnicastZenPing extends AbstractLifecycleComponent<ZenPing> implements ZenPing {

	
	/** The Constant LIMIT_PORTS_COUNT. */
	public static final int LIMIT_PORTS_COUNT = 1;

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The transport service. */
	private final TransportService transportService;

	
	/** The cluster name. */
	private final ClusterName clusterName;

	
	/** The concurrent connects. */
	private final int concurrentConnects;

	
	/** The nodes. */
	private final DiscoveryNode[] nodes;

	
	/** The nodes provider. */
	private volatile DiscoveryNodesProvider nodesProvider;

	
	/** The ping id generator. */
	private final AtomicInteger pingIdGenerator = new AtomicInteger();

	
	/** The received responses. */
	private final Map<Integer, ConcurrentMap<DiscoveryNode, PingResponse>> receivedResponses = ConcurrentCollections.newConcurrentMap();

	
	
	/** The temporal responses. */
	private final Queue<PingResponse> temporalResponses = new LinkedTransferQueue<PingResponse>();

	
	/** The hosts providers. */
	private final CopyOnWriteArrayList<UnicastHostsProvider> hostsProviders = new CopyOnWriteArrayList<UnicastHostsProvider>();

	
	/**
	 * Instantiates a new unicast zen ping.
	 *
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterName the cluster name
	 */
	public UnicastZenPing(ThreadPool threadPool, TransportService transportService, ClusterName clusterName) {
		this(ImmutableSettings.Builder.EMPTY_SETTINGS, threadPool, transportService, clusterName);
	}

	
	/**
	 * Instantiates a new unicast zen ping.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterName the cluster name
	 */
	public UnicastZenPing(Settings settings, ThreadPool threadPool, TransportService transportService,
			ClusterName clusterName) {
		super(settings);
		this.threadPool = threadPool;
		this.transportService = transportService;
		this.clusterName = clusterName;

		this.concurrentConnects = componentSettings.getAsInt("concurrent_connects", 10);
		String[] hostArr = componentSettings.getAsArray("hosts");
		
		for (int i = 0; i < hostArr.length; i++) {
			hostArr[i] = hostArr[i].trim();
		}
		List<String> hosts = Lists.newArrayList(hostArr);
		logger.debug("using initial hosts {}, with concurrent_connects [{}]", hosts, concurrentConnects);

		List<DiscoveryNode> nodes = Lists.newArrayList();
		int idCounter = 0;
		for (String host : hosts) {
			try {
				TransportAddress[] addresses = transportService.addressesFromString(host);
				
				for (int i = 0; (i < addresses.length && i < LIMIT_PORTS_COUNT); i++) {
					nodes.add(new DiscoveryNode("#zen_unicast_" + (++idCounter) + "#", addresses[i]));
				}
			} catch (Exception e) {
				throw new RestartIllegalArgumentException("Failed to resolve address for [" + host + "]", e);
			}
		}
		this.nodes = nodes.toArray(new DiscoveryNode[nodes.size()]);

		transportService.registerHandler(UnicastPingRequestHandler.ACTION, new UnicastPingRequestHandler());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RestartException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RestartException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RestartException {
		transportService.removeHandler(UnicastPingRequestHandler.ACTION);
	}

	
	/**
	 * Adds the hosts provider.
	 *
	 * @param provider the provider
	 */
	public void addHostsProvider(UnicastHostsProvider provider) {
		hostsProviders.add(provider);
	}

	
	/**
	 * Removes the hosts provider.
	 *
	 * @param provider the provider
	 */
	public void removeHostsProvider(UnicastHostsProvider provider) {
		hostsProviders.remove(provider);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.discovery.zen.ping.ZenPing#setNodesProvider(cn.com.summall.search.core.discovery.zen.DiscoveryNodesProvider)
	 */
	@Override
	public void setNodesProvider(DiscoveryNodesProvider nodesProvider) {
		this.nodesProvider = nodesProvider;
	}

	
	/**
	 * Ping and wait.
	 *
	 * @param timeout the timeout
	 * @return the ping response[]
	 */
	public PingResponse[] pingAndWait(TimeValue timeout) {
		final AtomicReference<PingResponse[]> response = new AtomicReference<PingResponse[]>();
		final CountDownLatch latch = new CountDownLatch(1);
		ping(new PingListener() {
			@Override
			public void onPing(PingResponse[] pings) {
				response.set(pings);
				latch.countDown();
			}
		}, timeout);
		try {
			latch.await();
			return response.get();
		} catch (InterruptedException e) {
			return null;
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.discovery.zen.ping.ZenPing#ping(cn.com.summall.search.core.discovery.zen.ping.ZenPing.PingListener, cn.com.summall.search.commons.unit.TimeValue)
	 */
	@Override
	public void ping(final PingListener listener, final TimeValue timeout) throws RestartException {
		final SendPingsHandler sendPingsHandler = new SendPingsHandler(pingIdGenerator.incrementAndGet());
		receivedResponses.put(sendPingsHandler.id(), new ConcurrentHashMap<DiscoveryNode, PingResponse>());
		sendPings(timeout, null, sendPingsHandler);
		threadPool.schedule(TimeValue.timeValueMillis(timeout.millis() / 2), ThreadPool.Names.GENERIC, new Runnable() {
			@Override
			public void run() {
				sendPings(timeout, null, sendPingsHandler);
				threadPool.schedule(TimeValue.timeValueMillis(timeout.millis() / 2), ThreadPool.Names.GENERIC,
						new Runnable() {
							@Override
							public void run() {
								sendPings(timeout, TimeValue.timeValueMillis(timeout.millis() / 2), sendPingsHandler);
								ConcurrentMap<DiscoveryNode, PingResponse> responses = receivedResponses
										.remove(sendPingsHandler.id());
								listener.onPing(responses.values().toArray(new PingResponse[responses.size()]));
								for (DiscoveryNode node : sendPingsHandler.nodeToDisconnect) {
									transportService.disconnectFromNode(node);
								}
								sendPingsHandler.close();
							}
						});
			}
		});
	}

	
	/**
	 * The Class SendPingsHandler.
	 *
	 * @author l.xue.nong
	 */
	class SendPingsHandler {

		
		/** The id. */
		private final int id;

		
		/** The executor. */
		private volatile ExecutorService executor;

		
		/** The node to disconnect. */
		private final Set<DiscoveryNode> nodeToDisconnect = ConcurrentCollections.newConcurrentSet();

		
		/** The closed. */
		private volatile boolean closed;

		
		/**
		 * Instantiates a new send pings handler.
		 *
		 * @param id the id
		 */
		SendPingsHandler(int id) {
			this.id = id;
		}

		
		/**
		 * Id.
		 *
		 * @return the int
		 */
		public int id() {
			return this.id;
		}

		
		/**
		 * Checks if is closed.
		 *
		 * @return true, if is closed
		 */
		public boolean isClosed() {
			return this.closed;
		}

		
		/**
		 * Executor.
		 *
		 * @return the executor
		 */
		public Executor executor() {
			if (executor == null) {
				ThreadFactory threadFactory = EsExecutors.daemonThreadFactory(settings, "[unicast_connect]");
				executor = EsExecutors.newScalingExecutorService(0, concurrentConnects, 60, TimeUnit.SECONDS,
						threadFactory);
			}
			return executor;
		}

		
		/**
		 * Close.
		 */
		public void close() {
			closed = true;
			if (executor != null) {
				executor.shutdownNow();
				executor = null;
			}
			nodeToDisconnect.clear();
		}
	}

	
	/**
	 * Send pings.
	 *
	 * @param timeout the timeout
	 * @param waitTime the wait time
	 * @param sendPingsHandler the send pings handler
	 */
	void sendPings(final TimeValue timeout, @Nullable TimeValue waitTime, final SendPingsHandler sendPingsHandler) {
		final UnicastPingRequest pingRequest = new UnicastPingRequest();
		pingRequest.id = sendPingsHandler.id();
		pingRequest.timeout = timeout;
		DiscoveryNodes discoNodes = nodesProvider.nodes();
		pingRequest.pingResponse = new PingResponse(discoNodes.localNode(), discoNodes.masterNode(), clusterName);

		List<DiscoveryNode> nodesToPing = newArrayList(nodes);
		for (UnicastHostsProvider provider : hostsProviders) {
			nodesToPing.addAll(provider.buildDynamicNodes());
		}

		final CountDownLatch latch = new CountDownLatch(nodesToPing.size());
		for (final DiscoveryNode node : nodesToPing) {
			
			boolean nodeFoundByAddressX;
			DiscoveryNode nodeToSendX = discoNodes.findByAddress(node.address());
			if (nodeToSendX != null) {
				nodeFoundByAddressX = true;
			} else {
				nodeToSendX = node;
				nodeFoundByAddressX = false;
			}
			final DiscoveryNode nodeToSend = nodeToSendX;

			final boolean nodeFoundByAddress = nodeFoundByAddressX;
			if (!transportService.nodeConnected(nodeToSend)) {
				if (sendPingsHandler.isClosed()) {
					return;
				}
				sendPingsHandler.nodeToDisconnect.add(nodeToSend);
				
				sendPingsHandler.executor().execute(new Runnable() {
					@Override
					public void run() {
						try {
							
							if (!nodeFoundByAddress) {
								transportService.connectToNodeLight(nodeToSend);
							} else {
								transportService.connectToNode(nodeToSend);
							}
							
							sendPingRequestToNode(sendPingsHandler.id(), timeout, pingRequest, latch, node, nodeToSend);
						} catch (ConnectTransportException e) {
							
							logger.trace("[" + sendPingsHandler.id() + "] failed to connect to " + nodeToSend, e);
							latch.countDown();
						}
					}
				});
			} else {
				sendPingRequestToNode(sendPingsHandler.id(), timeout, pingRequest, latch, node, nodeToSend);
			}
		}
		if (waitTime != null) {
			try {
				latch.await(waitTime.millis(), TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				
			}
		}
	}

	
	/**
	 * Send ping request to node.
	 *
	 * @param id the id
	 * @param timeout the timeout
	 * @param pingRequest the ping request
	 * @param latch the latch
	 * @param node the node
	 * @param nodeToSend the node to send
	 */
	private void sendPingRequestToNode(final int id, TimeValue timeout, UnicastPingRequest pingRequest,
			final CountDownLatch latch, final DiscoveryNode node, final DiscoveryNode nodeToSend) {
		logger.trace("[{}] connecting to {}", id, nodeToSend);
		transportService.sendRequest(nodeToSend, UnicastPingRequestHandler.ACTION, pingRequest, TransportRequestOptions
				.options().withTimeout((long) (timeout.millis() * 1.25)),
				new BaseTransportResponseHandler<UnicastPingResponse>() {

					@Override
					public UnicastPingResponse newInstance() {
						return new UnicastPingResponse();
					}

					@Override
					public String executor() {
						return ThreadPool.Names.SAME;
					}

					@Override
					public void handleResponse(UnicastPingResponse response) {
						logger.trace("[" + id + "] received response from " + nodeToSend + ": "
								+ Arrays.toString(response.pingResponses));
						try {
							DiscoveryNodes discoveryNodes = nodesProvider.nodes();
							for (PingResponse pingResponse : response.pingResponses) {
								if (pingResponse.target().id().equals(discoveryNodes.localNodeId())) {
									
									continue;
								}
								if (!pingResponse.clusterName().equals(clusterName)) {
									
									logger.debug("[" + id + "] filtering out response from " + pingResponse.target()
											+ ", not same cluster_name [{}]", pingResponse.clusterName().value());
									continue;
								}
								ConcurrentMap<DiscoveryNode, PingResponse> responses = receivedResponses
										.get(response.id);
								if (responses == null) {
									logger.warn("received ping response {} with no matching id [{}]", pingResponse,
											response.id);
								} else {
									responses.put(pingResponse.target(), pingResponse);
								}
							}
						} finally {
							latch.countDown();
						}
					}

					@Override
					public void handleException(TransportException exp) {
						latch.countDown();
						if (exp instanceof ConnectTransportException) {
							
							logger.trace("failed to connect to {}", exp, nodeToSend);
						} else {
							logger.warn("failed to send ping to [{}]", exp, node);
						}
					}
				});
	}

	
	/**
	 * Handle ping request.
	 *
	 * @param request the request
	 * @return the unicast ping response
	 */
	private UnicastPingResponse handlePingRequest(final UnicastPingRequest request) {
		if (lifecycle.stoppedOrClosed()) {
			throw new RestartIllegalStateException("received ping request while stopped/closed");
		}
		temporalResponses.add(request.pingResponse);
		threadPool.schedule(TimeValue.timeValueMillis(request.timeout.millis() * 2), ThreadPool.Names.SAME,
				new Runnable() {
					@Override
					public void run() {
						temporalResponses.remove(request.pingResponse);
					}
				});

		List<PingResponse> pingResponses = newArrayList(temporalResponses);
		DiscoveryNodes discoNodes = nodesProvider.nodes();
		pingResponses.add(new PingResponse(discoNodes.localNode(), discoNodes.masterNode(), clusterName));

		UnicastPingResponse unicastPingResponse = new UnicastPingResponse();
		unicastPingResponse.id = request.id;
		unicastPingResponse.pingResponses = pingResponses.toArray(new PingResponse[pingResponses.size()]);

		return unicastPingResponse;
	}

	
	/**
	 * The Class UnicastPingRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	class UnicastPingRequestHandler extends BaseTransportRequestHandler<UnicastPingRequest> {

		
		/** The Constant ACTION. */
		static final String ACTION = "discovery/zen/unicast";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public UnicastPingRequest newInstance() {
			return new UnicastPingRequest();
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
		public void messageReceived(UnicastPingRequest request, TransportChannel channel) throws Exception {
			channel.sendResponse(handlePingRequest(request));
		}
	}

	
	/**
	 * The Class UnicastPingRequest.
	 *
	 * @author l.xue.nong
	 */
	static class UnicastPingRequest implements Streamable {

		
		/** The id. */
		int id;

		
		/** The timeout. */
		TimeValue timeout;

		
		/** The ping response. */
		PingResponse pingResponse;

		
		/**
		 * Instantiates a new unicast ping request.
		 */
		UnicastPingRequest() {
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			id = in.readInt();
			timeout = TimeValue.readTimeValue(in);
			pingResponse = readPingResponse(in);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeInt(id);
			timeout.writeTo(out);
			pingResponse.writeTo(out);
		}
	}

	
	/**
	 * The Class UnicastPingResponse.
	 *
	 * @author l.xue.nong
	 */
	static class UnicastPingResponse implements Streamable {

		
		/** The id. */
		int id;

		
		/** The ping responses. */
		PingResponse[] pingResponses;

		
		/**
		 * Instantiates a new unicast ping response.
		 */
		UnicastPingResponse() {
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			id = in.readInt();
			pingResponses = new PingResponse[in.readVInt()];
			for (int i = 0; i < pingResponses.length; i++) {
				pingResponses[i] = readPingResponse(in);
			}
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeInt(id);
			out.writeVInt(pingResponses.length);
			for (PingResponse pingResponse : pingResponses) {
				pingResponse.writeTo(out);
			}
		}
	}
}
