/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NettyHttpRequest.java 2012-7-6 14:29:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.http.netty;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpMethod;

import cn.com.rebirth.search.core.http.HttpRequest;
import cn.com.rebirth.search.core.rest.support.AbstractRestRequest;
import cn.com.rebirth.search.core.rest.support.RestUtils;

import com.google.common.base.Charsets;

/**
 * The Class NettyHttpRequest.
 *
 * @author l.xue.nong
 */
public class NettyHttpRequest extends AbstractRestRequest implements HttpRequest {

	/** The request. */
	private final org.jboss.netty.handler.codec.http.HttpRequest request;

	/** The params. */
	private final Map<String, String> params;

	/** The raw path. */
	private final String rawPath;

	/** The cached data. */
	private byte[] cachedData;

	/**
	 * Instantiates a new netty http request.
	 *
	 * @param request the request
	 */
	public NettyHttpRequest(org.jboss.netty.handler.codec.http.HttpRequest request) {
		this.request = request;
		this.params = new HashMap<String, String>();

		String uri = request.getUri();
		int pathEndPos = uri.indexOf('?');
		if (pathEndPos < 0) {
			this.rawPath = uri;
		} else {
			this.rawPath = uri.substring(0, pathEndPos);
			RestUtils.decodeQueryString(uri, pathEndPos + 1, params);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#method()
	 */
	@Override
	public Method method() {
		HttpMethod httpMethod = request.getMethod();
		if (httpMethod == HttpMethod.GET)
			return Method.GET;

		if (httpMethod == HttpMethod.POST)
			return Method.POST;

		if (httpMethod == HttpMethod.PUT)
			return Method.PUT;

		if (httpMethod == HttpMethod.DELETE)
			return Method.DELETE;

		if (httpMethod == HttpMethod.HEAD) {
			return Method.HEAD;
		}

		if (httpMethod == HttpMethod.OPTIONS) {
			return Method.OPTIONS;
		}

		return Method.GET;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#uri()
	 */
	@Override
	public String uri() {
		return request.getUri();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#rawPath()
	 */
	@Override
	public String rawPath() {
		return rawPath;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#params()
	 */
	@Override
	public Map<String, String> params() {
		return params;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#hasContent()
	 */
	@Override
	public boolean hasContent() {
		return request.getContent().readableBytes() > 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#contentLength()
	 */
	@Override
	public int contentLength() {
		return request.getContent().readableBytes();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#contentUnsafe()
	 */
	@Override
	public boolean contentUnsafe() {

		return false;

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#contentByteArray()
	 */
	@Override
	public byte[] contentByteArray() {
		if (request.getContent().hasArray()) {
			return request.getContent().array();
		}
		if (cachedData != null) {
			return cachedData;
		}
		cachedData = new byte[request.getContent().readableBytes()];
		request.getContent().getBytes(request.getContent().readerIndex(), cachedData);
		return cachedData;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#contentByteArrayOffset()
	 */
	@Override
	public int contentByteArrayOffset() {
		if (request.getContent().hasArray()) {

			return request.getContent().arrayOffset() + request.getContent().readerIndex();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#contentAsString()
	 */
	@Override
	public String contentAsString() {
		return request.getContent().toString(Charsets.UTF_8);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#header(java.lang.String)
	 */
	@Override
	public String header(String name) {
		return request.getHeader(name);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#hasParam(java.lang.String)
	 */
	@Override
	public boolean hasParam(String key) {
		return params.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestRequest#param(java.lang.String)
	 */
	@Override
	public String param(String key) {
		return params.get(key);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent.Params#param(java.lang.String, java.lang.String)
	 */
	@Override
	public String param(String key, String defaultValue) {
		String value = params.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
}
