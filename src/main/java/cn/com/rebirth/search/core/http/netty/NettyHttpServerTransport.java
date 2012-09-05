/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NettyHttpServerTransport.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.http.netty;

import static cn.com.rebirth.commons.concurrent.EsExecutors.daemonThreadFactory;
import static cn.com.rebirth.search.commons.network.NetworkService.TcpSettings.TCP_BLOCKING;
import static cn.com.rebirth.search.commons.network.NetworkService.TcpSettings.TCP_BLOCKING_SERVER;
import static cn.com.rebirth.search.commons.network.NetworkService.TcpSettings.TCP_DEFAULT_RECEIVE_BUFFER_SIZE;
import static cn.com.rebirth.search.commons.network.NetworkService.TcpSettings.TCP_DEFAULT_SEND_BUFFER_SIZE;
import static cn.com.rebirth.search.commons.network.NetworkService.TcpSettings.TCP_KEEP_ALIVE;
import static cn.com.rebirth.search.commons.network.NetworkService.TcpSettings.TCP_NO_DELAY;
import static cn.com.rebirth.search.commons.network.NetworkService.TcpSettings.TCP_RECEIVE_BUFFER_SIZE;
import static cn.com.rebirth.search.commons.network.NetworkService.TcpSettings.TCP_REUSE_ADDRESS;
import static cn.com.rebirth.search.commons.network.NetworkService.TcpSettings.TCP_SEND_BUFFER_SIZE;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import cn.com.rebirth.commons.PortsRange;
import cn.com.rebirth.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeUnit;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.utils.NetworkUtils;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.netty.OpenChannelsHandler;
import cn.com.rebirth.search.commons.network.NetworkService;
import cn.com.rebirth.search.commons.transport.BoundTransportAddress;
import cn.com.rebirth.search.commons.transport.InetSocketTransportAddress;
import cn.com.rebirth.search.commons.transport.NetworkExceptionHelper;
import cn.com.rebirth.search.core.http.BindHttpException;
import cn.com.rebirth.search.core.http.HttpChannel;
import cn.com.rebirth.search.core.http.HttpRequest;
import cn.com.rebirth.search.core.http.HttpServerAdapter;
import cn.com.rebirth.search.core.http.HttpServerTransport;
import cn.com.rebirth.search.core.http.HttpStats;
import cn.com.rebirth.search.core.transport.BindTransportException;
import cn.com.rebirth.search.core.transport.netty.NettyInternalLoggerFactory;

/**
 * The Class NettyHttpServerTransport.
 *
 * @author l.xue.nong
 */
public class NettyHttpServerTransport extends AbstractLifecycleComponent<HttpServerTransport> implements
		HttpServerTransport {

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

	/** The max content length. */
	final ByteSizeValue maxContentLength;

	/** The max initial line length. */
	final ByteSizeValue maxInitialLineLength;

	/** The max header size. */
	final ByteSizeValue maxHeaderSize;

	/** The max chunk size. */
	final ByteSizeValue maxChunkSize;

	/** The worker count. */
	private final int workerCount;

	/** The blocking server. */
	private final boolean blockingServer;

	/** The compression. */
	final boolean compression;

	/** The compression level. */
	private final int compressionLevel;

	/** The reset cookies. */
	final boolean resetCookies;

	/** The port. */
	private final String port;

	/** The bind host. */
	private final String bindHost;

	/** The publish host. */
	private final String publishHost;

	/** The tcp no delay. */
	private final Boolean tcpNoDelay;

	/** The tcp keep alive. */
	private final Boolean tcpKeepAlive;

	/** The reuse address. */
	private final Boolean reuseAddress;

	/** The tcp send buffer size. */
	private final ByteSizeValue tcpSendBufferSize;

	/** The tcp receive buffer size. */
	private final ByteSizeValue tcpReceiveBufferSize;

	/** The server bootstrap. */
	private volatile ServerBootstrap serverBootstrap;

	/** The bound address. */
	private volatile BoundTransportAddress boundAddress;

	/** The server channel. */
	private volatile Channel serverChannel;

	/** The server open channels. */
	OpenChannelsHandler serverOpenChannels;

	/** The http server adapter. */
	private volatile HttpServerAdapter httpServerAdapter;

	/**
	 * Instantiates a new netty http server transport.
	 *
	 * @param settings the settings
	 * @param networkService the network service
	 */
	@Inject
	public NettyHttpServerTransport(Settings settings, NetworkService networkService) {
		super(settings);
		this.networkService = networkService;
		ByteSizeValue maxContentLength = componentSettings.getAsBytesSize("max_content_length",
				settings.getAsBytesSize("http.max_content_length", new ByteSizeValue(100, ByteSizeUnit.MB)));
		this.maxChunkSize = componentSettings.getAsBytesSize("max_chunk_size",
				settings.getAsBytesSize("http.max_chunk_size", new ByteSizeValue(8, ByteSizeUnit.KB)));
		this.maxHeaderSize = componentSettings.getAsBytesSize("max_header_size",
				settings.getAsBytesSize("http.max_header_size", new ByteSizeValue(8, ByteSizeUnit.KB)));
		this.maxInitialLineLength = componentSettings.getAsBytesSize("max_initial_line_length",
				settings.getAsBytesSize("http.max_initial_line_length", new ByteSizeValue(4, ByteSizeUnit.KB)));

		this.resetCookies = componentSettings.getAsBoolean("reset_cookies",
				settings.getAsBoolean("http.reset_cookies", false));
		this.workerCount = componentSettings.getAsInt("worker_count", Runtime.getRuntime().availableProcessors() * 2);
		this.blockingServer = settings.getAsBoolean("http.blocking_server",
				settings.getAsBoolean(TCP_BLOCKING_SERVER, settings.getAsBoolean(TCP_BLOCKING, false)));
		this.port = componentSettings.get("port", settings.get("http.port", "9200-9300"));
		this.bindHost = componentSettings.get("bind_host", settings.get("http.bind_host", settings.get("http.host")));
		this.publishHost = componentSettings.get("publish_host",
				settings.get("http.publish_host", settings.get("http.host")));
		this.tcpNoDelay = componentSettings.getAsBoolean("tcp_no_delay", settings.getAsBoolean(TCP_NO_DELAY, true));
		this.tcpKeepAlive = componentSettings.getAsBoolean("tcp_keep_alive",
				settings.getAsBoolean(TCP_KEEP_ALIVE, true));
		this.reuseAddress = componentSettings.getAsBoolean("reuse_address",
				settings.getAsBoolean(TCP_REUSE_ADDRESS, NetworkUtils.defaultReuseAddress()));
		this.tcpSendBufferSize = componentSettings.getAsBytesSize("tcp_send_buffer_size",
				settings.getAsBytesSize(TCP_SEND_BUFFER_SIZE, TCP_DEFAULT_SEND_BUFFER_SIZE));
		this.tcpReceiveBufferSize = componentSettings.getAsBytesSize("tcp_receive_buffer_size",
				settings.getAsBytesSize(TCP_RECEIVE_BUFFER_SIZE, TCP_DEFAULT_RECEIVE_BUFFER_SIZE));

		this.compression = settings.getAsBoolean("http.compression", false);
		this.compressionLevel = settings.getAsInt("http.compression_level", 6);

		if (maxContentLength.bytes() > Integer.MAX_VALUE) {
			logger.warn("maxContentLength[" + maxContentLength + "] set to high value, resetting it to [100mb]");
			maxContentLength = new ByteSizeValue(100, ByteSizeUnit.MB);
		}
		this.maxContentLength = maxContentLength;

		logger.debug("using max_chunk_size[" + maxChunkSize + "], max_header_size[" + maxHeaderSize
				+ "], max_initial_line_length[" + maxInitialLineLength + "], max_content_length[" + maxContentLength
				+ "]");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.http.HttpServerTransport#httpServerAdapter(cn.com.rebirth.search.core.http.HttpServerAdapter)
	 */
	public void httpServerAdapter(HttpServerAdapter httpServerAdapter) {
		this.httpServerAdapter = httpServerAdapter;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
		this.serverOpenChannels = new OpenChannelsHandler();

		if (blockingServer) {
			serverBootstrap = new ServerBootstrap(new OioServerSocketChannelFactory(
					Executors.newCachedThreadPool(daemonThreadFactory(settings, "http_server_boss")),
					Executors.newCachedThreadPool(daemonThreadFactory(settings, "http_server_worker"))));
		} else {
			serverBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
					Executors.newCachedThreadPool(daemonThreadFactory(settings, "http_server_boss")),
					Executors.newCachedThreadPool(daemonThreadFactory(settings, "http_server_worker")), workerCount));
		}

		serverBootstrap.setPipelineFactory(new MyChannelPipelineFactory(this));

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
			throw new BindHttpException("Failed to resolve host [" + bindHost + "]", e);
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
			throw new BindHttpException("Failed to bind to [" + port + "]", lastException.get());
		}

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
		if (serverChannel != null) {
			serverChannel.close().awaitUninterruptibly();
			serverChannel = null;
		}

		if (serverOpenChannels != null) {
			serverOpenChannels.close();
			serverOpenChannels = null;
		}

		if (serverBootstrap != null) {
			serverBootstrap.releaseExternalResources();
			serverBootstrap = null;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.http.HttpServerTransport#boundAddress()
	 */
	public BoundTransportAddress boundAddress() {
		return this.boundAddress;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.http.HttpServerTransport#stats()
	 */
	@Override
	public HttpStats stats() {
		OpenChannelsHandler channels = serverOpenChannels;
		return new HttpStats(channels == null ? 0 : channels.numberOfOpenChannels(), channels == null ? 0
				: channels.totalChannels());
	}

	/**
	 * Dispatch request.
	 *
	 * @param request the request
	 * @param channel the channel
	 */
	void dispatchRequest(HttpRequest request, HttpChannel channel) {
		httpServerAdapter.dispatchRequest(request, channel);
	}

	/**
	 * Exception caught.
	 *
	 * @param ctx the ctx
	 * @param e the e
	 * @throws Exception the exception
	 */
	void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if (e.getCause() instanceof ReadTimeoutException) {
			if (logger.isTraceEnabled()) {
				logger.trace("Connection timeout [{}]", ctx.getChannel().getRemoteAddress());
			}
			ctx.getChannel().close();
		} else {
			if (!lifecycle.started()) {

				return;
			}
			if (!NetworkExceptionHelper.isCloseConnectionException(e.getCause())) {
				logger.warn("Caught exception while handling client http traffic, closing connection {}", e.getCause(),
						ctx.getChannel());
				ctx.getChannel().close();
			} else {
				logger.debug("Caught exception while handling client http traffic, closing connection {}",
						e.getCause(), ctx.getChannel());
				ctx.getChannel().close();
			}
		}
	}

	/**
	 * A factory for creating MyChannelPipeline objects.
	 */
	static class MyChannelPipelineFactory implements ChannelPipelineFactory {

		/** The transport. */
		private final NettyHttpServerTransport transport;

		/** The request handler. */
		private final HttpRequestHandler requestHandler;

		/**
		 * Instantiates a new my channel pipeline factory.
		 *
		 * @param transport the transport
		 */
		MyChannelPipelineFactory(NettyHttpServerTransport transport) {
			this.transport = transport;
			this.requestHandler = new HttpRequestHandler(transport);
		}

		/* (non-Javadoc)
		 * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
		 */
		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("openChannels", transport.serverOpenChannels);
			pipeline.addLast("decoder", new HttpRequestDecoder((int) transport.maxInitialLineLength.bytes(),
					(int) transport.maxHeaderSize.bytes(), (int) transport.maxChunkSize.bytes()));
			if (transport.compression) {
				pipeline.addLast("decoder_compress", new HttpContentDecompressor());
			}
			pipeline.addLast("aggregator", new HttpChunkAggregator((int) transport.maxContentLength.bytes()));
			pipeline.addLast("encoder", new HttpResponseEncoder());
			if (transport.compression) {
				pipeline.addLast("encoder_compress", new HttpContentCompressor(transport.compressionLevel));
			}
			pipeline.addLast("handler", requestHandler);
			return pipeline;
		}
	}
}
