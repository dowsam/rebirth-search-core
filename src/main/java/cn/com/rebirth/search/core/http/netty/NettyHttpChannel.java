/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NettyHttpChannel.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.http.netty;

import java.io.IOException;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import cn.com.rebirth.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.http.HttpChannel;
import cn.com.rebirth.search.core.http.HttpException;
import cn.com.rebirth.search.core.rest.RestResponse;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.rest.XContentRestResponse;
import cn.com.rebirth.search.core.rest.support.RestUtils;
import cn.com.rebirth.search.core.transport.netty.NettyTransport;

/**
 * The Class NettyHttpChannel.
 *
 * @author l.xue.nong
 */
public class NettyHttpChannel implements HttpChannel {

	/** The transport. */
	private final NettyHttpServerTransport transport;

	/** The channel. */
	private final Channel channel;

	/** The request. */
	private final org.jboss.netty.handler.codec.http.HttpRequest request;

	/**
	 * Instantiates a new netty http channel.
	 *
	 * @param transport the transport
	 * @param channel the channel
	 * @param request the request
	 */
	public NettyHttpChannel(NettyHttpServerTransport transport, Channel channel,
			org.jboss.netty.handler.codec.http.HttpRequest request) {
		this.transport = transport;
		this.channel = channel;
		this.request = request;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestChannel#sendResponse(cn.com.rebirth.search.core.rest.RestResponse)
	 */
	@Override
	public void sendResponse(RestResponse response) {

		boolean http10 = request.getProtocolVersion().equals(HttpVersion.HTTP_1_0);
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION))
				|| (http10 && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request
						.getHeader(HttpHeaders.Names.CONNECTION)));

		HttpResponseStatus status = getStatus(response.status());
		org.jboss.netty.handler.codec.http.HttpResponse resp;
		if (http10) {
			resp = new DefaultHttpResponse(HttpVersion.HTTP_1_0, status);
			if (!close) {
				resp.addHeader(HttpHeaders.Names.CONNECTION, "Keep-Alive");
			}
		} else {
			resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		}
		if (RestUtils.isBrowser(request.getHeader(HttpHeaders.Names.USER_AGENT))) {

			resp.addHeader("Access-Control-Allow-Origin", "*");
			if (request.getMethod() == HttpMethod.OPTIONS) {

				resp.addHeader("Access-Control-Max-Age", 1728000);
				resp.addHeader("Access-Control-Allow-Methods", "PUT, DELETE");
				resp.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
			}
		}

		String opaque = request.getHeader("X-Opaque-Id");
		if (opaque != null) {
			resp.addHeader("X-Opaque-Id", opaque);
		}

		ChannelFutureListener releaseContentListener = null;
		ChannelBuffer buf;
		try {
			if (response instanceof XContentRestResponse) {

				XContentBuilder builder = ((XContentRestResponse) response).builder();
				if (builder.payload() instanceof CachedStreamOutput.Entry) {
					releaseContentListener = new NettyTransport.CacheFutureListener(
							(CachedStreamOutput.Entry) builder.payload());
					buf = ChannelBuffers.wrappedBuffer(builder.underlyingBytes(), 0, builder.underlyingBytesLength());
				} else if (response.contentThreadSafe()) {
					buf = ChannelBuffers.wrappedBuffer(response.content(), 0, response.contentLength());
				} else {
					buf = ChannelBuffers.copiedBuffer(response.content(), 0, response.contentLength());
				}
			} else {
				if (response.contentThreadSafe()) {
					buf = ChannelBuffers.wrappedBuffer(response.content(), 0, response.contentLength());
				} else {
					buf = ChannelBuffers.copiedBuffer(response.content(), 0, response.contentLength());
				}
			}
		} catch (IOException e) {
			throw new HttpException("Failed to convert response to bytes", e);
		}
		if (response.prefixContent() != null || response.suffixContent() != null) {
			ChannelBuffer prefixBuf = ChannelBuffers.EMPTY_BUFFER;
			if (response.prefixContent() != null) {
				prefixBuf = ChannelBuffers.copiedBuffer(response.prefixContent(), 0, response.prefixContentLength());
			}
			ChannelBuffer suffixBuf = ChannelBuffers.EMPTY_BUFFER;
			if (response.suffixContent() != null) {
				suffixBuf = ChannelBuffers.copiedBuffer(response.suffixContent(), 0, response.suffixContentLength());
			}
			buf = ChannelBuffers.wrappedBuffer(prefixBuf, buf, suffixBuf);
		}
		resp.setContent(buf);
		resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, response.contentType());

		resp.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));

		if (transport.resetCookies) {
			String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
			if (cookieString != null) {
				CookieDecoder cookieDecoder = new CookieDecoder();
				Set<Cookie> cookies = cookieDecoder.decode(cookieString);
				if (!cookies.isEmpty()) {

					CookieEncoder cookieEncoder = new CookieEncoder(true);
					for (Cookie cookie : cookies) {
						cookieEncoder.addCookie(cookie);
					}
					resp.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
				}
			}
		}

		ChannelFuture future = channel.write(resp);
		if (releaseContentListener != null) {
			future.addListener(releaseContentListener);
		}

		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	/**
	 * Gets the status.
	 *
	 * @param status the status
	 * @return the status
	 */
	private HttpResponseStatus getStatus(RestStatus status) {
		switch (status) {
		case CONTINUE:
			return HttpResponseStatus.CONTINUE;
		case SWITCHING_PROTOCOLS:
			return HttpResponseStatus.SWITCHING_PROTOCOLS;
		case OK:
			return HttpResponseStatus.OK;
		case CREATED:
			return HttpResponseStatus.CREATED;
		case ACCEPTED:
			return HttpResponseStatus.ACCEPTED;
		case NON_AUTHORITATIVE_INFORMATION:
			return HttpResponseStatus.NON_AUTHORITATIVE_INFORMATION;
		case NO_CONTENT:
			return HttpResponseStatus.NO_CONTENT;
		case RESET_CONTENT:
			return HttpResponseStatus.RESET_CONTENT;
		case PARTIAL_CONTENT:
			return HttpResponseStatus.PARTIAL_CONTENT;
		case MULTI_STATUS:

			return HttpResponseStatus.INTERNAL_SERVER_ERROR;
		case MULTIPLE_CHOICES:
			return HttpResponseStatus.MULTIPLE_CHOICES;
		case MOVED_PERMANENTLY:
			return HttpResponseStatus.MOVED_PERMANENTLY;
		case FOUND:
			return HttpResponseStatus.FOUND;
		case SEE_OTHER:
			return HttpResponseStatus.SEE_OTHER;
		case NOT_MODIFIED:
			return HttpResponseStatus.NOT_MODIFIED;
		case USE_PROXY:
			return HttpResponseStatus.USE_PROXY;
		case TEMPORARY_REDIRECT:
			return HttpResponseStatus.TEMPORARY_REDIRECT;
		case BAD_REQUEST:
			return HttpResponseStatus.BAD_REQUEST;
		case UNAUTHORIZED:
			return HttpResponseStatus.UNAUTHORIZED;
		case PAYMENT_REQUIRED:
			return HttpResponseStatus.PAYMENT_REQUIRED;
		case FORBIDDEN:
			return HttpResponseStatus.FORBIDDEN;
		case NOT_FOUND:
			return HttpResponseStatus.NOT_FOUND;
		case METHOD_NOT_ALLOWED:
			return HttpResponseStatus.METHOD_NOT_ALLOWED;
		case NOT_ACCEPTABLE:
			return HttpResponseStatus.NOT_ACCEPTABLE;
		case PROXY_AUTHENTICATION:
			return HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED;
		case REQUEST_TIMEOUT:
			return HttpResponseStatus.REQUEST_TIMEOUT;
		case CONFLICT:
			return HttpResponseStatus.CONFLICT;
		case GONE:
			return HttpResponseStatus.GONE;
		case LENGTH_REQUIRED:
			return HttpResponseStatus.LENGTH_REQUIRED;
		case PRECONDITION_FAILED:
			return HttpResponseStatus.PRECONDITION_FAILED;
		case REQUEST_ENTITY_TOO_LARGE:
			return HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE;
		case REQUEST_URI_TOO_LONG:
			return HttpResponseStatus.REQUEST_URI_TOO_LONG;
		case UNSUPPORTED_MEDIA_TYPE:
			return HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE;
		case REQUESTED_RANGE_NOT_SATISFIED:
			return HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
		case EXPECTATION_FAILED:
			return HttpResponseStatus.EXPECTATION_FAILED;
		case UNPROCESSABLE_ENTITY:
			return HttpResponseStatus.BAD_REQUEST;
		case LOCKED:
			return HttpResponseStatus.BAD_REQUEST;
		case FAILED_DEPENDENCY:
			return HttpResponseStatus.BAD_REQUEST;
		case INTERNAL_SERVER_ERROR:
			return HttpResponseStatus.INTERNAL_SERVER_ERROR;
		case NOT_IMPLEMENTED:
			return HttpResponseStatus.NOT_IMPLEMENTED;
		case BAD_GATEWAY:
			return HttpResponseStatus.BAD_GATEWAY;
		case SERVICE_UNAVAILABLE:
			return HttpResponseStatus.SERVICE_UNAVAILABLE;
		case GATEWAY_TIMEOUT:
			return HttpResponseStatus.GATEWAY_TIMEOUT;
		case HTTP_VERSION_NOT_SUPPORTED:
			return HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED;
		default:
			return HttpResponseStatus.INTERNAL_SERVER_ERROR;
		}
	}
}
