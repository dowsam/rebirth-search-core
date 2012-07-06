/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core XContentThrowableRestResponse.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest;

import java.io.IOException;

import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;

/**
 * The Class XContentThrowableRestResponse.
 *
 * @author l.xue.nong
 */
public class XContentThrowableRestResponse extends XContentRestResponse {

	/**
	 * Instantiates a new x content throwable rest response.
	 *
	 * @param request the request
	 * @param t the t
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public XContentThrowableRestResponse(RestRequest request, Throwable t) throws IOException {
		this(request, RestStatus.INTERNAL_SERVER_ERROR, t);
	}

	/**
	 * Instantiates a new x content throwable rest response.
	 *
	 * @param request the request
	 * @param status the status
	 * @param t the t
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public XContentThrowableRestResponse(RestRequest request, RestStatus status, Throwable t) throws IOException {
		super(request, status, convert(request, status, t));
	}

	/**
	 * Convert.
	 *
	 * @param request the request
	 * @param status the status
	 * @param t the t
	 * @return the x content builder
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static XContentBuilder convert(RestRequest request, RestStatus status, Throwable t) throws IOException {
		XContentBuilder builder = RestXContentBuilder.restContentBuilder(request).startObject()
				.field("error", ExceptionsHelper.detailedMessage(t)).field("status", status.getStatus());
		if (t != null && request.paramAsBoolean("error_trace", false)) {
			builder.startObject("error_trace");
			boolean first = true;
			while (t != null) {
				if (!first) {
					builder.startObject("cause");
				}
				buildThrowable(t, builder);
				if (!first) {
					builder.endObject();
				}
				t = t.getCause();
				first = false;
			}
			builder.endObject();
		}
		builder.endObject();
		return builder;
	}

	/**
	 * Builds the throwable.
	 *
	 * @param t the t
	 * @param builder the builder
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void buildThrowable(Throwable t, XContentBuilder builder) throws IOException {
		builder.field("message", t.getMessage());
		for (StackTraceElement stElement : t.getStackTrace()) {
			builder.startObject("at").field("class", stElement.getClassName())
					.field("method", stElement.getMethodName());
			if (stElement.getFileName() != null) {
				builder.field("file", stElement.getFileName());
			}
			if (stElement.getLineNumber() >= 0) {
				builder.field("line", stElement.getLineNumber());
			}
			builder.endObject();
		}
	}
}