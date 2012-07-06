/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MulticastZenPing.java 2012-3-29 15:02:14 l.xue.nong$$
 */


package cn.com.rebirth.search.core.discovery.zen.ping.multicast;

import static cn.com.rebirth.commons.concurrent.ConcurrentCollections.newConcurrentMap;
import static cn.com.rebirth.commons.concurrent.EsExecutors.daemonThreadFactory;
import static cn.com.rebirth.search.commons.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;
import static cn.com.rebirth.search.core.cluster.node.DiscoveryNode.readNode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.exception.RestartIllegalStateException;
import cn.com.rebirth.commons.io.stream.CachedStreamInput;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.io.stream.BytesStreamInput;
import cn.com.rebirth.search.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.search.commons.io.stream.HandlesStreamOutput;
import cn.com.rebirth.search.commons.io.stream.VoidStreamable;
import cn.com.rebirth.search.commons.network.NetworkService;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.RestartSearchCoreVersion;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodes;
import cn.com.rebirth.search.core.discovery.zen.DiscoveryNodesProvider;
import cn.com.rebirth.search.core.discovery.zen.ping.ZenPing;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.BaseTransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportService;
import cn.com.rebirth.search.core.transport.VoidTransportResponseHandler;


/**
 * The Class MulticastZenPing.
 *
 * @author l.xue.nong
 */
public class MulticastZenPing extends AbstractLifecycleComponent<ZenPing> implements ZenPing {

	
	/** The Constant INTERNAL_HEADER. */
	private static final byte[] INTERNAL_HEADER = new byte[] { 1, 9, 8, 4 };

	
	/** The address. */
	private final String address;

	
	/** The port. */
	private final int port;

	
	/** The group. */
	private final String group;

	
	/** The buffer size. */
	private final int bufferSize;

	
	/** The ttl. */
	private final int ttl;

	
	/** The thread pool. */
	private final ThreadPool threadPool;

	
	/** The transport service. */
	private final TransportService transportService;

	
	/** The cluster name. */
	private final ClusterName clusterName;

	
	/** The network service. */
	private final NetworkService networkService;

	
	/** The ping enabled. */
	private final boolean pingEnabled;

	
	/** The nodes provider. */
	private volatile DiscoveryNodesProvider nodesProvider;

	
	/** The receiver. */
	private volatile Receiver receiver;

	
	/** The receiver thread. */
	private volatile Thread receiverThread;

	
	/** The multicast socket. */
	private MulticastSocket multicastSocket;

	
	/** The datagram packet send. */
	private DatagramPacket datagramPacketSend;

	
	/** The datagram packet receive. */
	private DatagramPacket datagramPacketReceive;

	
	/** The ping id generator. */
	private final AtomicInteger pingIdGenerator = new AtomicInteger();

	
	/** The received responses. */
	private final Map<Integer, ConcurrentMap<DiscoveryNode, PingResponse>> receivedResponses = newConcurrentMap();

	
	/** The send mutex. */
	private final Object sendMutex = new Object();

	
	/** The receive mutex. */
	private final Object receiveMutex = new Object();

	
	/**
	 * Instantiates a new multicast zen ping.
	 *
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterName the cluster name
	 */
	public MulticastZenPing(ThreadPool threadPool, TransportService transportService, ClusterName clusterName) {
		this(EMPTY_SETTINGS, threadPool, transportService, clusterName, new NetworkService(EMPTY_SETTINGS));
	}

	
	/**
	 * Instantiates a new multicast zen ping.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterName the cluster name
	 * @param networkService the network service
	 */
	public MulticastZenPing(Settings settings, ThreadPool threadPool, TransportService transportService,
			ClusterName clusterName, NetworkService networkService) {
		super(settings);
		this.threadPool = threadPool;
		this.transportService = transportService;
		this.clusterName = clusterName;
		this.networkService = networkService;

		this.address = componentSettings.get("address");
		this.port = componentSettings.getAsInt("port", 54328);
		this.group = componentSettings.get("group", "224.2.2.4");
		this.bufferSize = componentSettings.getAsInt("buffer_size", 2048);
		this.ttl = componentSettings.getAsInt("ttl", 3);

		this.pingEnabled = componentSettings.getAsBoolean("ping.enabled", true);

		logger.debug("using group [" + group + "], with port [" + port + "], ttl [" + ttl + "], and address ["
				+ address + "]");

		this.transportService.registerHandler(MulticastPingResponseRequestHandler.ACTION,
				new MulticastPingResponseRequestHandler());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.discovery.zen.ping.ZenPing#setNodesProvider(cn.com.summall.search.core.discovery.zen.DiscoveryNodesProvider)
	 */
	@Override
	public void setNodesProvider(DiscoveryNodesProvider nodesProvider) {
		if (lifecycle.started()) {
			throw new RestartIllegalStateException("Can't set nodes provider when started");
		}
		this.nodesProvider = nodesProvider;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RestartException {
		try {
			this.datagramPacketReceive = new DatagramPacket(new byte[bufferSize], bufferSize);
			this.datagramPacketSend = new DatagramPacket(new byte[bufferSize], bufferSize,
					InetAddress.getByName(group), port);
		} catch (Exception e) {
			logger.warn("disabled, failed to setup multicast (datagram) discovery : {}", e.getMessage());
			if (logger.isDebugEnabled()) {
				logger.debug("disabled, failed to setup multicast (datagram) discovery", e);
			}
			return;
		}

		InetAddress multicastInterface = null;
		try {
			MulticastSocket multicastSocket;
			multicastSocket = new MulticastSocket(port);

			multicastSocket.setTimeToLive(ttl);

			multicastInterface = networkService.resolvePublishHostAddress(address);
			multicastSocket.setInterface(multicastInterface);
			multicastSocket.joinGroup(InetAddress.getByName(group));

			multicastSocket.setReceiveBufferSize(bufferSize);
			multicastSocket.setSendBufferSize(bufferSize);
			multicastSocket.setSoTimeout(60000);

			this.multicastSocket = multicastSocket;

			this.receiver = new Receiver();
			this.receiverThread = daemonThreadFactory(settings, "discovery#multicast#receiver").newThread(receiver);
			this.receiverThread.start();
		} catch (Exception e) {
			datagramPacketReceive = null;
			datagramPacketSend = null;
			if (multicastSocket != null) {
				multicastSocket.close();
				multicastSocket = null;
			}
			logger.warn("disabled, failed to setup multicast discovery on port [" + port + "], [" + multicastInterface
					+ "]: " + e.getMessage());
			if (logger.isDebugEnabled()) {
				logger.debug("disabled, failed to setup multicast discovery on {}", e, multicastInterface);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RestartException {
		if (receiver != null) {
			receiver.stop();
		}
		if (receiverThread != null) {
			receiverThread.interrupt();
		}
		if (multicastSocket != null) {
			multicastSocket.close();
			multicastSocket = null;
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RestartException {
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
	public void ping(final PingListener listener, final TimeValue timeout) {
		if (!pingEnabled) {
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					listener.onPing(new PingResponse[0]);
				}
			});
			return;
		}
		final int id = pingIdGenerator.incrementAndGet();
		receivedResponses.put(id, new ConcurrentHashMap<DiscoveryNode, PingResponse>());
		sendPingRequest(id);
		
		
		threadPool.schedule(TimeValue.timeValueMillis(timeout.millis() / 2), ThreadPool.Names.GENERIC, new Runnable() {
			@Override
			public void run() {
				try {
					sendPingRequest(id);
				} catch (Exception e) {
					logger.warn("[{}] failed to send second ping request", e, id);
				}
			}
		});
		threadPool.schedule(timeout, ThreadPool.Names.GENERIC, new Runnable() {
			@Override
			public void run() {
				ConcurrentMap<DiscoveryNode, PingResponse> responses = receivedResponses.remove(id);
				listener.onPing(responses.values().toArray(new PingResponse[responses.size()]));
			}
		});
	}

	
	/**
	 * Send ping request.
	 *
	 * @param id the id
	 */
	private void sendPingRequest(int id) {
		if (multicastSocket == null) {
			return;
		}
		synchronized (sendMutex) {
			CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
			try {
				HandlesStreamOutput out = cachedEntry.cachedHandlesBytes();
				out.writeBytes(INTERNAL_HEADER);
				out.writeInt(id);
				clusterName.writeTo(out);
				nodesProvider.nodes().localNode().writeTo(out);
				datagramPacketSend.setData(cachedEntry.bytes().copiedByteArray());
				multicastSocket.send(datagramPacketSend);
				if (logger.isTraceEnabled()) {
					logger.trace("[{}] sending ping request", id);
				}
			} catch (Exception e) {
				if (lifecycle.stoppedOrClosed()) {
					return;
				}
				if (logger.isDebugEnabled()) {
					logger.debug("failed to send multicast ping request", e);
				} else {
					logger.warn("failed to send multicast ping request: {}", ExceptionsHelper.detailedMessage(e));
				}
			} finally {
				CachedStreamOutput.pushEntry(cachedEntry);
			}
		}
	}

	
	/**
	 * The Class MulticastPingResponseRequestHandler.
	 *
	 * @author l.xue.nong
	 */
	class MulticastPingResponseRequestHandler extends BaseTransportRequestHandler<MulticastPingResponse> {

		
		/** The Constant ACTION. */
		static final String ACTION = "discovery/zen/multicast";

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#newInstance()
		 */
		@Override
		public MulticastPingResponse newInstance() {
			return new MulticastPingResponse();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#messageReceived(cn.com.summall.search.commons.io.stream.Streamable, cn.com.summall.search.core.transport.TransportChannel)
		 */
		@Override
		public void messageReceived(MulticastPingResponse request, TransportChannel channel) throws Exception {
			if (logger.isTraceEnabled()) {
				logger.trace("[{}] received {}", request.id, request.pingResponse);
			}
			ConcurrentMap<DiscoveryNode, PingResponse> responses = receivedResponses.get(request.id);
			if (responses == null) {
				logger.warn("received ping response {} with no matching id [{}]", request.pingResponse, request.id);
			} else {
				responses.put(request.pingResponse.target(), request.pingResponse);
			}
			channel.sendResponse(VoidStreamable.INSTANCE);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.transport.TransportRequestHandler#executor()
		 */
		@Override
		public String executor() {
			return ThreadPool.Names.SAME;
		}
	}

	
	/**
	 * The Class MulticastPingResponse.
	 *
	 * @author l.xue.nong
	 */
	static class MulticastPingResponse implements Streamable {

		
		/** The id. */
		int id;

		
		/** The ping response. */
		PingResponse pingResponse;

		
		/**
		 * Instantiates a new multicast ping response.
		 */
		MulticastPingResponse() {
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			id = in.readInt();
			pingResponse = PingResponse.readPingResponse(in);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeInt(id);
			pingResponse.writeTo(out);
		}
	}

	
	/**
	 * The Class Receiver.
	 *
	 * @author l.xue.nong
	 */
	private class Receiver implements Runnable {

		
		/** The running. */
		private volatile boolean running = true;

		
		/**
		 * Stop.
		 */
		public void stop() {
			running = false;
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (running) {
				try {
					int id = -1;
					DiscoveryNode requestingNodeX = null;
					ClusterName clusterName = null;

					Map<String, Object> externalPingData = null;
					XContentType xContentType = null;

					synchronized (receiveMutex) {
						try {
							multicastSocket.receive(datagramPacketReceive);
						} catch (SocketTimeoutException ignore) {
							continue;
						} catch (Exception e) {
							if (running) {
								logger.warn("failed to receive packet", e);
							}
							continue;
						}
						try {
							boolean internal = false;
							if (datagramPacketReceive.getLength() > 4) {
								int counter = 0;
								for (; counter < INTERNAL_HEADER.length; counter++) {
									if (datagramPacketReceive.getData()[datagramPacketReceive.getOffset() + counter] != INTERNAL_HEADER[counter]) {
										break;
									}
								}
								if (counter == INTERNAL_HEADER.length) {
									internal = true;
								}
							}
							if (internal) {
								StreamInput input = CachedStreamInput.cachedHandles(new BytesStreamInput(
										datagramPacketReceive.getData(), datagramPacketReceive.getOffset()
												+ INTERNAL_HEADER.length, datagramPacketReceive.getLength(), true));
								id = input.readInt();
								clusterName = ClusterName.readClusterName(input);
								requestingNodeX = readNode(input);
							} else {
								xContentType = XContentFactory.xContentType(datagramPacketReceive.getData(),
										datagramPacketReceive.getOffset(), datagramPacketReceive.getLength());
								if (xContentType != null) {
									
									externalPingData = XContentFactory
											.xContent(xContentType)
											.createParser(datagramPacketReceive.getData(),
													datagramPacketReceive.getOffset(),
													datagramPacketReceive.getLength()).mapAndClose();
								} else {
									throw new RestartIllegalStateException(
											"failed multicast message, probably message from previous version");
								}
							}
						} catch (Exception e) {
							logger.warn("failed to read requesting data from {}", e,
									datagramPacketReceive.getSocketAddress());
							continue;
						}
					}
					if (externalPingData != null) {
						handleExternalPingRequest(externalPingData, xContentType,
								datagramPacketReceive.getSocketAddress());
					} else {
						handleNodePingRequest(id, requestingNodeX, clusterName);
					}
				} catch (Exception e) {
					logger.warn("unexpected exception in multicast receiver", e);
				}
			}
		}

		
		/**
		 * Handle external ping request.
		 *
		 * @param externalPingData the external ping data
		 * @param contentType the content type
		 * @param remoteAddress the remote address
		 */
		@SuppressWarnings("unchecked")
		private void handleExternalPingRequest(Map<String, Object> externalPingData, XContentType contentType,
				SocketAddress remoteAddress) {
			if (externalPingData.containsKey("response")) {
				
				logger.trace("got an external ping response (ignoring) from {}, content {}", remoteAddress,
						externalPingData);
				return;
			}

			if (multicastSocket == null) {
				logger.debug("can't send ping response, no socket, from {}, content {}", remoteAddress,
						externalPingData);
				return;
			}

			Map<String, Object> request = (Map<String, Object>) externalPingData.get("request");
			if (request == null) {
				logger.warn("malformed external ping request, no 'request' element from {}, content {}", remoteAddress,
						externalPingData);
				return;
			}

			String clusterName = request.containsKey("cluster_name") ? request.get("cluster_name").toString() : request
					.containsKey("clusterName") ? request.get("clusterName").toString() : null;
			if (clusterName == null) {
				logger.warn(
						"malformed external ping request, missing 'cluster_name' element within request, from {}, content {}",
						remoteAddress, externalPingData);
				return;
			}

			if (!clusterName.equals(MulticastZenPing.this.clusterName.value())) {
				logger.trace("got request for cluster_name " + clusterName + ", but our cluster_name is "
						+ MulticastZenPing.this.clusterName.value() + ", from " + remoteAddress + ", content "
						+ externalPingData);
				return;
			}
			if (logger.isTraceEnabled()) {
				logger.trace("got external ping request from {}, content {}", remoteAddress, externalPingData);
			}

			try {
				DiscoveryNode localNode = nodesProvider.nodes().localNode();

				XContentBuilder builder = XContentFactory.contentBuilder(contentType);
				builder.startObject().startObject("response");
				builder.field("cluster_name", MulticastZenPing.this.clusterName.value());
				builder.startObject("version").field("number", new RestartSearchCoreVersion().getModuleVersion())
						.field("snapshot_build", new RestartSearchCoreVersion().getModuleName()).endObject();
				builder.field("transport_address", localNode.address().toString());

				if (nodesProvider.nodeService() != null) {
					for (Map.Entry<String, String> attr : nodesProvider.nodeService().attributes().entrySet()) {
						builder.field(attr.getKey(), attr.getValue());
					}
				}

				builder.startObject("attributes");
				for (Map.Entry<String, String> attr : localNode.attributes().entrySet()) {
					builder.field(attr.getKey(), attr.getValue());
				}
				builder.endObject();

				builder.endObject().endObject();
				synchronized (sendMutex) {
					datagramPacketSend.setData(builder.underlyingBytes(), 0, builder.underlyingBytesLength());
					multicastSocket.send(datagramPacketSend);
					if (logger.isTraceEnabled()) {
						logger.trace("sending external ping response {}", builder.string());
					}
				}
			} catch (Exception e) {
				logger.warn("failed to send external multicast response", e);
			}
		}

		
		/**
		 * Handle node ping request.
		 *
		 * @param id the id
		 * @param requestingNodeX the requesting node x
		 * @param clusterName the cluster name
		 */
		private void handleNodePingRequest(int id, DiscoveryNode requestingNodeX, ClusterName clusterName) {
			if (!pingEnabled) {
				return;
			}
			DiscoveryNodes discoveryNodes = nodesProvider.nodes();
			final DiscoveryNode requestingNode = requestingNodeX;
			if (requestingNode.id().equals(discoveryNodes.localNodeId())) {
				
				return;
			}
			if (!clusterName.equals(MulticastZenPing.this.clusterName)) {
				if (logger.isTraceEnabled()) {
					logger.trace("[" + id + "] received ping_request from [" + requestingNode
							+ "], but wrong cluster_name [" + clusterName + "], expected ["
							+ MulticastZenPing.this.clusterName + "], ignoring");
				}
				return;
			}
			
			if (!discoveryNodes.localNode().shouldConnectTo(requestingNode)) {
				if (logger.isTraceEnabled()) {
					logger.trace("[" + id
							+ "] received ping_request from [clusterName], both are client nodes, ignoring");
				}
				return;
			}
			final MulticastPingResponse multicastPingResponse = new MulticastPingResponse();
			multicastPingResponse.id = id;
			multicastPingResponse.pingResponse = new PingResponse(discoveryNodes.localNode(),
					discoveryNodes.masterNode(), clusterName);

			if (logger.isTraceEnabled()) {
				logger.trace("[" + id + "] received ping_request from [" + requestingNode + "], sending "
						+ multicastPingResponse.pingResponse);
			}

			if (!transportService.nodeConnected(requestingNode)) {
				
				threadPool.generic().execute(new Runnable() {
					@Override
					public void run() {
						
						try {
							transportService.connectToNode(requestingNode);
							transportService.sendRequest(requestingNode, MulticastPingResponseRequestHandler.ACTION,
									multicastPingResponse, new VoidTransportResponseHandler(ThreadPool.Names.SAME) {
										@Override
										public void handleException(TransportException exp) {
											logger.warn("failed to receive confirmation on sent ping response to [{}]",
													exp, requestingNode);
										}
									});
						} catch (Exception e) {
							logger.warn("failed to connect to requesting node {}", e, requestingNode);
						}
					}
				});
			} else {
				transportService.sendRequest(requestingNode, MulticastPingResponseRequestHandler.ACTION,
						multicastPingResponse, new VoidTransportResponseHandler(ThreadPool.Names.SAME) {
							@Override
							public void handleException(TransportException exp) {
								logger.warn("failed to receive confirmation on sent ping response to [{}]", exp,
										requestingNode);
							}
						});
			}
		}
	}
}
