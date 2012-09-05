/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NettyTransport.java 2012-7-6 14:29:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport.netty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import cn.com.rebirth.commons.PortsRange;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.concurrent.EsExecutors;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.utils.NetworkUtils;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.commons.netty.OpenChannelsHandler;
import cn.com.rebirth.search.commons.network.NetworkService;
import cn.com.rebirth.search.commons.network.NetworkService.TcpSettings;
import cn.com.rebirth.search.commons.transport.BoundTransportAddress;
import cn.com.rebirth.search.commons.transport.InetSocketTransportAddress;
import cn.com.rebirth.search.commons.transport.NetworkExceptionHelper;
import cn.com.rebirth.search.commons.transport.TransportAddress;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.transport.BindTransportException;
import cn.com.rebirth.search.core.transport.ConnectTransportException;
import cn.com.rebirth.search.core.transport.NodeNotConnectedException;
import cn.com.rebirth.search.core.transport.Transport;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;
import cn.com.rebirth.search.core.transport.TransportServiceAdapter;
import cn.com.rebirth.search.core.transport.support.TransportStreams;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * The Class NettyTransport.
 *
 * @author l.xue.nong
 */
public class NettyTransport extends AbstractLifecycleComponent<Transport> implements Transport {

	static {
		InternalLoggerFactory.setDefaultFactory(new NettyInternalLoggerFactory() {
			@Override
			public InternalLogger newInstance(String name) {
				return super.newInstance(name.replace("org.jboss.netty.", "netty.").replace("org.jboss.netty.",
						"netty."));
			}
		});
	}

	/** The network service. */
	private final NetworkService networkService;

	/** The worker count. */
	final int workerCount;

	/** The blocking server. */
	final boolean blockingServer;

	/** The blocking client. */
	final boolean blockingClient;

	/** The port. */
	final String port;

	/** The bind host. */
	final String bindHost;

	/** The publish host. */
	final String publishHost;

	/** The compress. */
	final boolean compress;

	/** The connect timeout. */
	final TimeValue connectTimeout;

	/** The tcp no delay. */
	final Boolean tcpNoDelay;

	/** The tcp keep alive. */
	final Boolean tcpKeepAlive;

	/** The reuse address. */
	final Boolean reuseAddress;

	/** The tcp send buffer size. */
	final ByteSizeValue tcpSendBufferSize;

	/** The tcp receive buffer size. */
	final ByteSizeValue tcpReceiveBufferSize;

	/** The connections per node low. */
	final int connectionsPerNodeLow;

	/** The connections per node med. */
	final int connectionsPerNodeMed;

	/** The connections per node high. */
	final int connectionsPerNodeHigh;

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The server open channels. */
	private volatile OpenChannelsHandler serverOpenChannels;

	/** The client bootstrap. */
	private volatile ClientBootstrap clientBootstrap;

	/** The server bootstrap. */
	private volatile ServerBootstrap serverBootstrap;

	/** The connected nodes. */
	final ConcurrentMap<DiscoveryNode, NodeChannels> connectedNodes = ConcurrentCollections.newConcurrentMap();

	/** The server channel. */
	private volatile Channel serverChannel;

	/** The transport service adapter. */
	private volatile TransportServiceAdapter transportServiceAdapter;

	/** The bound address. */
	private volatile BoundTransportAddress boundAddress;

	/**
	 * Instantiates a new netty transport.
	 *
	 * @param threadPool the thread pool
	 */
	public NettyTransport(ThreadPool threadPool) {
		this(ImmutableSettings.Builder.EMPTY_SETTINGS, threadPool, new NetworkService(
				ImmutableSettings.Builder.EMPTY_SETTINGS));
	}

	/**
	 * Instantiates a new netty transport.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 */
	public NettyTransport(Settings settings, ThreadPool threadPool) {
		this(settings, threadPool, new NetworkService(settings));
	}

	/**
	 * Instantiates a new netty transport.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param networkService the network service
	 */
	@Inject
	public NettyTransport(Settings settings, ThreadPool threadPool, NetworkService networkService) {
		super(settings);
		this.threadPool = threadPool;
		this.networkService = networkService;

		this.workerCount = componentSettings.getAsInt("worker_count", Runtime.getRuntime().availableProcessors() * 2);
		this.blockingServer = settings.getAsBoolean(
				"transport.tcp.blocking_server",
				settings.getAsBoolean(TcpSettings.TCP_BLOCKING_SERVER,
						settings.getAsBoolean(TcpSettings.TCP_BLOCKING, false)));
		this.blockingClient = settings.getAsBoolean(
				"transport.tcp.blocking_client",
				settings.getAsBoolean(TcpSettings.TCP_BLOCKING_CLIENT,
						settings.getAsBoolean(TcpSettings.TCP_BLOCKING, false)));
		this.port = componentSettings.get("port", settings.get("transport.tcp.port", "9300-9400"));
		this.bindHost = componentSettings.get("bind_host",
				settings.get("transport.bind_host", settings.get("transport.host")));
		this.publishHost = componentSettings.get("publish_host",
				settings.get("transport.publish_host", settings.get("transport.host")));
		this.compress = settings.getAsBoolean("transport.tcp.compress", false);
		this.connectTimeout = componentSettings.getAsTime(
				"connect_timeout",
				settings.getAsTime("transport.tcp.connect_timeout",
						settings.getAsTime(TcpSettings.TCP_CONNECT_TIMEOUT, TcpSettings.TCP_DEFAULT_CONNECT_TIMEOUT)));
		this.tcpNoDelay = componentSettings.getAsBoolean("tcp_no_delay",
				settings.getAsBoolean(TcpSettings.TCP_NO_DELAY, true));
		this.tcpKeepAlive = componentSettings.getAsBoolean("tcp_keep_alive",
				settings.getAsBoolean(TcpSettings.TCP_KEEP_ALIVE, true));
		this.reuseAddress = componentSettings.getAsBoolean("reuse_address",
				settings.getAsBoolean(TcpSettings.TCP_REUSE_ADDRESS, NetworkUtils.defaultReuseAddress()));
		this.tcpSendBufferSize = componentSettings.getAsBytesSize("tcp_send_buffer_size",
				settings.getAsBytesSize(TcpSettings.TCP_SEND_BUFFER_SIZE, TcpSettings.TCP_DEFAULT_SEND_BUFFER_SIZE));
		this.tcpReceiveBufferSize = componentSettings.getAsBytesSize("tcp_receive_buffer_size", settings
				.getAsBytesSize(TcpSettings.TCP_RECEIVE_BUFFER_SIZE, TcpSettings.TCP_DEFAULT_RECEIVE_BUFFER_SIZE));
		this.connectionsPerNodeLow = componentSettings.getAsInt("connections_per_node.low",
				settings.getAsInt("transport.connections_per_node.low", 2));
		this.connectionsPerNodeMed = componentSettings.getAsInt("connections_per_node.med",
				settings.getAsInt("transport.connections_per_node.med", 4));
		this.connectionsPerNodeHigh = componentSettings.getAsInt("connections_per_node.high",
				settings.getAsInt("transport.connections_per_node.high", 1));

		logger.debug("using worker_count[" + workerCount + "], port[" + port + "], bind_host[" + bindHost
				+ "], publish_host[" + publishHost + "], compress[" + compress + "], connect_timeout[" + connectTimeout
				+ "], connections_per_node[" + connectionsPerNodeLow + "/" + connectionsPerNodeMed + "/"
				+ connectionsPerNodeHigh + "]");
	}

	/**
	 * Settings.
	 *
	 * @return the settings
	 */
	public Settings settings() {
		return this.settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#transportServiceAdapter(cn.com.rebirth.search.core.transport.TransportServiceAdapter)
	 */
	@Override
	public void transportServiceAdapter(TransportServiceAdapter service) {
		this.transportServiceAdapter = service;
	}

	/**
	 * Transport service adapter.
	 *
	 * @return the transport service adapter
	 */
	TransportServiceAdapter transportServiceAdapter() {
		return transportServiceAdapter;
	}

	/**
	 * Thread pool.
	 *
	 * @return the thread pool
	 */
	ThreadPool threadPool() {
		return threadPool;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
		if (blockingClient) {
			clientBootstrap = new ClientBootstrap(
					new OioClientSocketChannelFactory(Executors.newCachedThreadPool(EsExecutors.daemonThreadFactory(
							settings, "transport_client_worker"))));
		} else {
			clientBootstrap = new ClientBootstrap(
					new NioClientSocketChannelFactory(Executors.newCachedThreadPool(EsExecutors.daemonThreadFactory(
							settings, "transport_client_boss")), Executors.newCachedThreadPool(EsExecutors
							.daemonThreadFactory(settings, "transport_client_worker")), workerCount));
		}
		ChannelPipelineFactory clientPipelineFactory = new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("dispatcher", new MessageChannelHandler(NettyTransport.this));
				return pipeline;
			}
		};
		clientBootstrap.setPipelineFactory(clientPipelineFactory);
		clientBootstrap.setOption("connectTimeoutMillis", connectTimeout.millis());
		if (tcpNoDelay != null) {
			clientBootstrap.setOption("tcpNoDelay", tcpNoDelay);
		}
		if (tcpKeepAlive != null) {
			clientBootstrap.setOption("keepAlive", tcpKeepAlive);
		}
		if (tcpSendBufferSize != null) {
			clientBootstrap.setOption("sendBufferSize", tcpSendBufferSize.bytes());
		}
		if (tcpReceiveBufferSize != null) {
			clientBootstrap.setOption("receiveBufferSize", tcpReceiveBufferSize.bytes());
		}
		if (reuseAddress != null) {
			clientBootstrap.setOption("reuseAddress", reuseAddress);
		}

		if (!settings.getAsBoolean("network.server", true)) {
			return;
		}

		serverOpenChannels = new OpenChannelsHandler();
		if (blockingServer) {
			serverBootstrap = new ServerBootstrap(
					new OioServerSocketChannelFactory(Executors.newCachedThreadPool(EsExecutors.daemonThreadFactory(
							settings, "transport_server_boss")), Executors.newCachedThreadPool(EsExecutors
							.daemonThreadFactory(settings, "transport_server_worker"))));
		} else {
			serverBootstrap = new ServerBootstrap(
					new NioServerSocketChannelFactory(Executors.newCachedThreadPool(EsExecutors.daemonThreadFactory(
							settings, "transport_server_boss")), Executors.newCachedThreadPool(EsExecutors
							.daemonThreadFactory(settings, "transport_server_worker")), workerCount));
		}
		ChannelPipelineFactory serverPipelineFactory = new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("openChannels", serverOpenChannels);
				pipeline.addLast("dispatcher", new MessageChannelHandler(NettyTransport.this));
				return pipeline;
			}
		};
		serverBootstrap.setPipelineFactory(serverPipelineFactory);
		if (tcpNoDelay != null) {
			serverBootstrap.setOption("child.tcpNoDelay", tcpNoDelay);
		}
		if (tcpKeepAlive != null) {
			serverBootstrap.setOption("child.keepAlive", tcpKeepAlive);
		}
		if (tcpSendBufferSize != null) {
			serverBootstrap.setOption("child.sendBufferSize", tcpSendBufferSize.bytes());
		}
		if (tcpReceiveBufferSize != null) {
			serverBootstrap.setOption("child.receiveBufferSize", tcpReceiveBufferSize.bytes());
		}
		if (reuseAddress != null) {
			serverBootstrap.setOption("reuseAddress", reuseAddress);
			serverBootstrap.setOption("child.reuseAddress", reuseAddress);
		}

		InetAddress hostAddressX;
		try {
			hostAddressX = networkService.resolveBindHostAddress(bindHost);
		} catch (IOException e) {
			throw new BindTransportException("Failed to resolve host [" + bindHost + "]", e);
		}
		final InetAddress hostAddress = hostAddressX;

		PortsRange portsRange = new PortsRange(port);
		final AtomicReference<Exception> lastException = new AtomicReference<Exception>();
		boolean success = portsRange.iterate(new PortsRange.PortCallback() {
			@Override
			public boolean onPortNumber(int portNumber) {
				try {
					serverChannel = serverBootstrap.bind(new InetSocketAddress(hostAddress, portNumber));
				} catch (Exception e) {
					lastException.set(e);
					return false;
				}
				return true;
			}
		});
		if (!success) {
			throw new BindTransportException("Failed to bind to [" + port + "]", lastException.get());
		}

		logger.debug("Bound to address [{}]", serverChannel.getLocalAddress());

		InetSocketAddress boundAddress = (InetSocketAddress) serverChannel.getLocalAddress();
		InetSocketAddress publishAddress;
		try {
			publishAddress = new InetSocketAddress(networkService.resolvePublishHostAddress(publishHost),
					boundAddress.getPort());
		} catch (Exception e) {
			throw new BindTransportException("Failed to resolve publish address", e);
		}
		this.boundAddress = new BoundTransportAddress(new InetSocketTransportAddress(boundAddress),
				new InetSocketTransportAddress(publishAddress));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {
		final CountDownLatch latch = new CountDownLatch(1);

		threadPool.generic().execute(new Runnable() {
			@Override
			public void run() {
				try {
					for (Iterator<NodeChannels> it = connectedNodes.values().iterator(); it.hasNext();) {
						NodeChannels nodeChannels = it.next();
						it.remove();
						nodeChannels.close();
					}

					if (serverChannel != null) {
						try {
							serverChannel.close().awaitUninterruptibly();
						} finally {
							serverChannel = null;
						}
					}

					if (serverOpenChannels != null) {
						serverOpenChannels.close();
						serverOpenChannels = null;
					}

					if (serverBootstrap != null) {
						serverBootstrap.releaseExternalResources();
						serverBootstrap = null;
					}

					for (Iterator<NodeChannels> it = connectedNodes.values().iterator(); it.hasNext();) {
						NodeChannels nodeChannels = it.next();
						it.remove();
						nodeChannels.close();
					}

					if (clientBootstrap != null) {
						clientBootstrap.releaseExternalResources();
						clientBootstrap = null;
					}
				} finally {
					latch.countDown();
				}
			}
		});

		try {
			latch.await(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {

		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#addressesFromString(java.lang.String)
	 */
	@Override
	public TransportAddress[] addressesFromString(String address) throws Exception {
		int index = address.indexOf('[');
		if (index != -1) {
			String host = address.substring(0, index);
			Set<String> ports = Strings.commaDelimitedListToSet(address.substring(index + 1, address.indexOf(']')));
			List<TransportAddress> addresses = Lists.newArrayList();
			for (String port : ports) {
				int[] iPorts = new PortsRange(port).ports();
				for (int iPort : iPorts) {
					addresses.add(new InetSocketTransportAddress(host, iPort));
				}
			}
			return addresses.toArray(new TransportAddress[addresses.size()]);
		} else {
			index = address.lastIndexOf(':');
			if (index == -1) {
				List<TransportAddress> addresses = Lists.newArrayList();
				int[] iPorts = new PortsRange(this.port).ports();
				for (int iPort : iPorts) {
					addresses.add(new InetSocketTransportAddress(address, iPort));
				}
				return addresses.toArray(new TransportAddress[addresses.size()]);
			} else {
				String host = address.substring(0, index);
				int port = Integer.parseInt(address.substring(index + 1));
				return new TransportAddress[] { new InetSocketTransportAddress(host, port) };
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#addressSupported(java.lang.Class)
	 */
	@Override
	public boolean addressSupported(Class<? extends TransportAddress> address) {
		return InetSocketTransportAddress.class.equals(address);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#boundAddress()
	 */
	@Override
	public BoundTransportAddress boundAddress() {
		return this.boundAddress;
	}

	/**
	 * Exception caught.
	 *
	 * @param ctx the ctx
	 * @param e the e
	 * @throws Exception the exception
	 */
	void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if (!lifecycle.started()) {

		}
		if (NetworkExceptionHelper.isCloseConnectionException(e.getCause())) {

			Channel channel = ctx.getChannel();
			for (Map.Entry<DiscoveryNode, NodeChannels> entry : connectedNodes.entrySet()) {
				if (entry.getValue().hasChannel(channel)) {
					disconnectFromNode(entry.getKey());
				}
			}
		} else if (NetworkExceptionHelper.isConnectException(e.getCause())) {
			if (logger.isTraceEnabled()) {
				logger.trace("(Ignoring) Exception caught on netty layer [" + ctx.getChannel() + "]", e.getCause());
			}
		} else {
			logger.warn("Exception caught on netty layer [" + ctx.getChannel() + "]", e.getCause());
		}
	}

	/**
	 * Wrap address.
	 *
	 * @param socketAddress the socket address
	 * @return the transport address
	 */
	TransportAddress wrapAddress(SocketAddress socketAddress) {
		return new InetSocketTransportAddress((InetSocketAddress) socketAddress);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#serverOpen()
	 */
	@Override
	public long serverOpen() {
		OpenChannelsHandler channels = serverOpenChannels;
		return channels == null ? 0 : channels.numberOfOpenChannels();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#sendRequest(cn.com.rebirth.search.core.cluster.node.DiscoveryNode, long, java.lang.String, cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportRequestOptions)
	 */
	@Override
	public <T extends Streamable> void sendRequest(final DiscoveryNode node, final long requestId, final String action,
			final Streamable message, TransportRequestOptions options) throws IOException, TransportException {
		Channel targetChannel = nodeChannel(node, options);

		if (compress) {
			options.withCompress(true);
		}

		CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
		TransportStreams.buildRequest(cachedEntry, requestId, action, message, options);
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(cachedEntry.bytes().underlyingBytes(), 0, cachedEntry
				.bytes().size());
		ChannelFuture future = targetChannel.write(buffer);
		future.addListener(new CacheFutureListener(cachedEntry));

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#nodeConnected(cn.com.rebirth.search.core.cluster.node.DiscoveryNode)
	 */
	@Override
	public boolean nodeConnected(DiscoveryNode node) {
		return connectedNodes.containsKey(node);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#connectToNodeLight(cn.com.rebirth.search.core.cluster.node.DiscoveryNode)
	 */
	@Override
	public void connectToNodeLight(DiscoveryNode node) throws ConnectTransportException {
		connectToNode(node, true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#connectToNode(cn.com.rebirth.search.core.cluster.node.DiscoveryNode)
	 */
	@Override
	public void connectToNode(DiscoveryNode node) {
		connectToNode(node, false);
	}

	/**
	 * Connect to node.
	 *
	 * @param node the node
	 * @param light the light
	 */
	public void connectToNode(DiscoveryNode node, boolean light) {
		if (!lifecycle.started()) {
			throw new RebirthIllegalStateException("Can't add nodes to a stopped transport");
		}
		if (node == null) {
			throw new ConnectTransportException(null, "Can't connect to a null node");
		}
		try {
			NodeChannels nodeChannels = connectedNodes.get(node);
			if (nodeChannels != null) {
				return;
			}

			if (light) {
				nodeChannels = connectToChannelsLight(node);
			} else {
				nodeChannels = new NodeChannels(new Channel[connectionsPerNodeLow], new Channel[connectionsPerNodeMed],
						new Channel[connectionsPerNodeHigh]);
				try {
					connectToChannels(nodeChannels, node);
				} catch (Exception e) {
					nodeChannels.close();
					throw e;
				}
			}

			NodeChannels existing = connectedNodes.putIfAbsent(node, nodeChannels);
			if (existing != null) {

				nodeChannels.close();
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Connected to node [{}]", node);
				}
				transportServiceAdapter.raiseNodeConnected(node);
			}

		} catch (ConnectTransportException e) {
			throw e;
		} catch (Exception e) {
			throw new ConnectTransportException(node, "General node connection failure", e);
		}
	}

	/**
	 * Connect to channels light.
	 *
	 * @param node the node
	 * @return the node channels
	 */
	private NodeChannels connectToChannelsLight(DiscoveryNode node) {
		InetSocketAddress address = ((InetSocketTransportAddress) node.address()).address();
		ChannelFuture connect = clientBootstrap.connect(address);
		connect.awaitUninterruptibly((long) (connectTimeout.millis() * 1.5));
		if (!connect.isSuccess()) {
			throw new ConnectTransportException(node, "connect_timeout[" + connectTimeout + "]", connect.getCause());
		}
		Channel[] channels = new Channel[1];
		channels[0] = connect.getChannel();
		channels[0].getCloseFuture().addListener(new ChannelCloseListener(node));
		return new NodeChannels(channels, channels, channels);
	}

	/**
	 * Connect to channels.
	 *
	 * @param nodeChannels the node channels
	 * @param node the node
	 */
	private void connectToChannels(NodeChannels nodeChannels, DiscoveryNode node) {
		ChannelFuture[] connectLow = new ChannelFuture[nodeChannels.low.length];
		ChannelFuture[] connectMed = new ChannelFuture[nodeChannels.med.length];
		ChannelFuture[] connectHigh = new ChannelFuture[nodeChannels.high.length];
		InetSocketAddress address = ((InetSocketTransportAddress) node.address()).address();
		for (int i = 0; i < connectLow.length; i++) {
			connectLow[i] = clientBootstrap.connect(address);
		}
		for (int i = 0; i < connectMed.length; i++) {
			connectMed[i] = clientBootstrap.connect(address);
		}
		for (int i = 0; i < connectHigh.length; i++) {
			connectHigh[i] = clientBootstrap.connect(address);
		}

		try {
			for (int i = 0; i < connectLow.length; i++) {
				connectLow[i].awaitUninterruptibly((long) (connectTimeout.millis() * 1.5));
				if (!connectLow[i].isSuccess()) {
					throw new ConnectTransportException(node, "connect_timeout[" + connectTimeout + "]",
							connectLow[i].getCause());
				}
				nodeChannels.low[i] = connectLow[i].getChannel();
				nodeChannels.low[i].getCloseFuture().addListener(new ChannelCloseListener(node));
			}

			for (int i = 0; i < connectMed.length; i++) {
				connectMed[i].awaitUninterruptibly((long) (connectTimeout.millis() * 1.5));
				if (!connectMed[i].isSuccess()) {
					throw new ConnectTransportException(node, "connect_timeout[" + connectTimeout + "]",
							connectMed[i].getCause());
				}
				nodeChannels.med[i] = connectMed[i].getChannel();
				nodeChannels.med[i].getCloseFuture().addListener(new ChannelCloseListener(node));
			}

			for (int i = 0; i < connectHigh.length; i++) {
				connectHigh[i].awaitUninterruptibly((long) (connectTimeout.millis() * 1.5));
				if (!connectHigh[i].isSuccess()) {
					throw new ConnectTransportException(node, "connect_timeout[" + connectTimeout + "]",
							connectHigh[i].getCause());
				}
				nodeChannels.high[i] = connectHigh[i].getChannel();
				nodeChannels.high[i].getCloseFuture().addListener(new ChannelCloseListener(node));
			}

			if (nodeChannels.low.length == 0) {
				if (nodeChannels.med.length > 0) {
					nodeChannels.low = nodeChannels.med;
				} else {
					nodeChannels.low = nodeChannels.high;
				}
			}
			if (nodeChannels.med.length == 0) {
				if (nodeChannels.high.length > 0) {
					nodeChannels.med = nodeChannels.high;
				} else {
					nodeChannels.med = nodeChannels.low;
				}
			}
			if (nodeChannels.high.length == 0) {
				if (nodeChannels.med.length > 0) {
					nodeChannels.high = nodeChannels.med;
				} else {
					nodeChannels.high = nodeChannels.low;
				}
			}
		} catch (RuntimeException e) {

			for (ChannelFuture future : ImmutableList.<ChannelFuture> builder().add(connectLow).add(connectMed)
					.add(connectHigh).build()) {
				future.cancel();
				if (future.getChannel() != null && future.getChannel().isOpen()) {
					try {
						future.getChannel().close();
					} catch (Exception e1) {

					}
				}
			}
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#disconnectFromNode(cn.com.rebirth.search.core.cluster.node.DiscoveryNode)
	 */
	@Override
	public void disconnectFromNode(DiscoveryNode node) {
		NodeChannels nodeChannels = connectedNodes.remove(node);
		if (nodeChannels != null) {
			try {
				nodeChannels.close();
			} finally {
				logger.debug("Disconnected from [{}]", node);
				transportServiceAdapter.raiseNodeDisconnected(node);
			}
		}
	}

	/**
	 * Node channel.
	 *
	 * @param node the node
	 * @param options the options
	 * @return the channel
	 * @throws ConnectTransportException the connect transport exception
	 */
	private Channel nodeChannel(DiscoveryNode node, TransportRequestOptions options) throws ConnectTransportException {
		NodeChannels nodeChannels = connectedNodes.get(node);
		if (nodeChannels == null) {
			throw new NodeNotConnectedException(node, "Node not connected");
		}
		return nodeChannels.channel(options.type());
	}

	/**
	 * The listener interface for receiving channelClose events.
	 * The class that is interested in processing a channelClose
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addChannelCloseListener<code> method. When
	 * the channelClose event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see ChannelCloseEvent
	 */
	private class ChannelCloseListener implements ChannelFutureListener {

		/** The node. */
		private final DiscoveryNode node;

		/**
		 * Instantiates a new channel close listener.
		 *
		 * @param node the node
		 */
		private ChannelCloseListener(DiscoveryNode node) {
			this.node = node;
		}

		/* (non-Javadoc)
		 * @see org.jboss.netty.channel.ChannelFutureListener#operationComplete(org.jboss.netty.channel.ChannelFuture)
		 */
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			disconnectFromNode(node);
		}
	}

	/**
	 * The Class NodeChannels.
	 *
	 * @author l.xue.nong
	 */
	public static class NodeChannels {

		/** The low. */
		private Channel[] low;

		/** The low counter. */
		private final AtomicInteger lowCounter = new AtomicInteger();

		/** The med. */
		private Channel[] med;

		/** The med counter. */
		private final AtomicInteger medCounter = new AtomicInteger();

		/** The high. */
		private Channel[] high;

		/** The high counter. */
		private final AtomicInteger highCounter = new AtomicInteger();

		/**
		 * Instantiates a new node channels.
		 *
		 * @param low the low
		 * @param med the med
		 * @param high the high
		 */
		public NodeChannels(Channel[] low, Channel[] med, Channel[] high) {
			this.low = low;
			this.med = med;
			this.high = high;
		}

		/**
		 * Checks for channel.
		 *
		 * @param channel the channel
		 * @return true, if successful
		 */
		public boolean hasChannel(Channel channel) {
			return hasChannel(channel, low) || hasChannel(channel, med) || hasChannel(channel, high);
		}

		/**
		 * Checks for channel.
		 *
		 * @param channel the channel
		 * @param channels the channels
		 * @return true, if successful
		 */
		private boolean hasChannel(Channel channel, Channel[] channels) {
			for (Channel channel1 : channels) {
				if (channel.equals(channel1)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Channel.
		 *
		 * @param type the type
		 * @return the channel
		 */
		public Channel channel(TransportRequestOptions.Type type) {
			if (type == TransportRequestOptions.Type.MED) {
				return med[Math.abs(medCounter.incrementAndGet()) % med.length];
			} else if (type == TransportRequestOptions.Type.HIGH) {
				return high[Math.abs(highCounter.incrementAndGet()) % high.length];
			} else {
				return low[Math.abs(lowCounter.incrementAndGet()) % low.length];
			}
		}

		/**
		 * Close.
		 */
		public synchronized void close() {
			List<ChannelFuture> futures = new ArrayList<ChannelFuture>();
			closeChannelsAndWait(low, futures);
			closeChannelsAndWait(med, futures);
			closeChannelsAndWait(high, futures);
			for (ChannelFuture future : futures) {
				future.awaitUninterruptibly();
			}
		}

		/**
		 * Close channels and wait.
		 *
		 * @param channels the channels
		 * @param futures the futures
		 */
		private void closeChannelsAndWait(Channel[] channels, List<ChannelFuture> futures) {
			for (Channel channel : channels) {
				try {
					if (channel != null && channel.isOpen()) {
						futures.add(channel.close());
					}
				} catch (Exception e) {

				}
			}
		}
	}

	/**
	 * The listener interface for receiving cacheFuture events.
	 * The class that is interested in processing a cacheFuture
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addCacheFutureListener<code> method. When
	 * the cacheFuture event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see CacheFutureEvent
	 */
	public static class CacheFutureListener implements ChannelFutureListener {

		/** The cached entry. */
		private final CachedStreamOutput.Entry cachedEntry;

		/**
		 * Instantiates a new cache future listener.
		 *
		 * @param cachedEntry the cached entry
		 */
		public CacheFutureListener(CachedStreamOutput.Entry cachedEntry) {
			this.cachedEntry = cachedEntry;
		}

		/* (non-Javadoc)
		 * @see org.jboss.netty.channel.ChannelFutureListener#operationComplete(org.jboss.netty.channel.ChannelFuture)
		 */
		@Override
		public void operationComplete(ChannelFuture channelFuture) throws Exception {
			CachedStreamOutput.pushEntry(cachedEntry);
		}
	}

}
