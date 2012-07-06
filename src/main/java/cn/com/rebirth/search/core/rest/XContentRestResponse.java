/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core XContentRestResponse.java 2012-7-6 14:30:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest;

import java.io.IOException;

import org.apache.lucene.util.UnicodeUtil;

import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class XContentRestResponse.
 *
 * @author l.xue.nong
 */
public class XContentRestResponse extends AbstractRestResponse {

	/** The Constant END_JSONP. */
	private static final byte[] END_JSONP;

	static {
		UnicodeUtil.UTF8Result U_END_JSONP = new UnicodeUtil.UTF8Result();
		UnicodeUtil.UTF16toUTF8(");", 0, ");".length(), U_END_JSONP);
		END_JSONP = new byte[U_END_JSONP.length];
		System.arraycopy(U_END_JSONP.result, 0, END_JSONP, 0, U_END_JSONP.length);
	}

	/** The prefix cache. */
	private static ThreadLocal<ThreadLocals.CleanableValue<UnicodeUtil.UTF8Result>> prefixCache = new ThreadLocal<ThreadLocals.CleanableValue<UnicodeUtil.UTF8Result>>() {
		@Override
		protected ThreadLocals.CleanableValue<UnicodeUtil.UTF8Result> initialValue() {
			return new ThreadLocals.CleanableValue<UnicodeUtil.UTF8Result>(new UnicodeUtil.UTF8Result());
		}
	};

	/** The prefix utf8 result. */
	private final UnicodeUtil.UTF8Result prefixUtf8Result;

	/** The status. */
	private final RestStatus status;

	/** The builder. */
	private final XContentBuilder builder;

	/**
	 * Instantiates a new x content rest response.
	 *
	 * @param request the request
	 * @param status the status
	 * @param builder the builder
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public XContentRestResponse(RestRequest request, RestStatus status, XContentBuilder builder) throws IOException {
		this.builder = builder;
		this.status = status;
		this.prefixUtf8Result = startJsonp(request);
	}

	/**
	 * Builder.
	 *
	 * @return the x content builder
	 */
	public XContentBuilder builder() {
		return this.builder;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestResponse#contentType()
	 */
	@Override
	public String contentType() {
		return builder.contentType().restContentType();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestResponse#contentThreadSafe()
	 */
	@Override
	public boolean contentThreadSafe() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestResponse#content()
	 */
	@Override
	public byte[] content() throws IOException {
		return builder.underlyingBytes();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestResponse#contentLength()
	 */
	@Override
	public int contentLength() throws IOException {
		return builder.underlyingBytesLength();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.RestResponse#status()
	 */
	@Override
	public RestStatus status() {
		return this.status;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.AbstractRestResponse#prefixContent()
	 */
	@Override
	public byte[] prefixContent() {
		if (prefixUtf8Result != null) {
			return prefixUtf8Result.result;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.AbstractRestResponse#prefixContentLength()
	 */
	@Override
	public int prefixContentLength() {
		if (prefixUtf8Result != null) {
			return prefixUtf8Result.length;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.AbstractRestResponse#suffixContent()
	 */
	@Override
	public byte[] suffixContent() {
		if (prefixUtf8Result != null) {
			return END_JSONP;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.rest.AbstractRestResponse#suffixContentLength()
	 */
	@Override
	public int suffixContentLength() {
		if (prefixUtf8Result != null) {
			return END_JSONP.length;
		}
		return 0;
	}

	/**
	 * Start jsonp.
	 *
	 * @param request the request
	 * @return the unicode util. ut f8 result
	 */
	private static UnicodeUtil.UTF8Result startJsonp(RestRequest request) {
		String callback = request.param("callback");
		if (callback == null) {
			return null;
		}
		UnicodeUtil.UTF8Result result = prefixCache.get().get();
		UnicodeUtil.UTF16toUTF8(callback, 0, callback.length(), result);
		result.result[result.length] = '(';
		result.length++;
		return result;
	}
}