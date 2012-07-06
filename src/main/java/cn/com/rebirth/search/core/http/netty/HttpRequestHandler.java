/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HttpRequestHandler.java 2012-7-6 14:28:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.http.netty;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * The Class HttpRequestHandler.
 *
 * @author l.xue.nong
 */
@ChannelHandler.Sharable
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {

	/** The server transport. */
	private final NettyHttpServerTransport serverTransport;

	/**
	 * Instantiates a new http request handler.
	 *
	 * @param serverTransport the server transport
	 */
	public HttpRequestHandler(NettyHttpServerTransport serverTransport) {
		this.serverTransport = serverTransport;
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();

		serverTransport.dispatchRequest(new NettyHttpRequest(request),
				new NettyHttpChannel(serverTransport, e.getChannel(), request));
		super.messageReceived(ctx, e);
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		serverTransport.exceptionCaught(ctx, e);
	}
}
