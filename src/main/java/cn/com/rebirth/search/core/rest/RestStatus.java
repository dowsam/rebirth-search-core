/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestStatus.java 2012-7-6 14:29:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;

/**
 * The Enum RestStatus.
 *
 * @author l.xue.nong
 */
public enum RestStatus {

	/** The continue. */
	CONTINUE(100),

	/** The switching protocols. */
	SWITCHING_PROTOCOLS(101),

	/** The ok. */
	OK(200),

	/** The created. */
	CREATED(201),

	/** The accepted. */
	ACCEPTED(202),

	/** The non authoritative information. */
	NON_AUTHORITATIVE_INFORMATION(203),

	/** The no content. */
	NO_CONTENT(204),

	/** The reset content. */
	RESET_CONTENT(205),

	/** The partial content. */
	PARTIAL_CONTENT(206),

	/** The multi status. */
	MULTI_STATUS(207),

	/** The multiple choices. */
	MULTIPLE_CHOICES(300),

	/** The moved permanently. */
	MOVED_PERMANENTLY(301),

	/** The found. */
	FOUND(302),

	/** The see other. */
	SEE_OTHER(303),

	/** The not modified. */
	NOT_MODIFIED(304),

	/** The use proxy. */
	USE_PROXY(305),

	/** The temporary redirect. */
	TEMPORARY_REDIRECT(307),

	/** The bad request. */
	BAD_REQUEST(400),

	/** The unauthorized. */
	UNAUTHORIZED(401),

	/** The payment required. */
	PAYMENT_REQUIRED(402),

	/** The forbidden. */
	FORBIDDEN(403),

	/** The not found. */
	NOT_FOUND(404),

	/** The method not allowed. */
	METHOD_NOT_ALLOWED(405),

	/** The not acceptable. */
	NOT_ACCEPTABLE(406),

	/** The proxy authentication. */
	PROXY_AUTHENTICATION(407),

	/** The request timeout. */
	REQUEST_TIMEOUT(408),

	/** The conflict. */
	CONFLICT(409),

	/** The gone. */
	GONE(410),

	/** The length required. */
	LENGTH_REQUIRED(411),

	/** The precondition failed. */
	PRECONDITION_FAILED(412),

	/** The request entity too large. */
	REQUEST_ENTITY_TOO_LARGE(413),

	/** The request uri too long. */
	REQUEST_URI_TOO_LONG(414),

	/** The unsupported media type. */
	UNSUPPORTED_MEDIA_TYPE(415),

	/** The requested range not satisfied. */
	REQUESTED_RANGE_NOT_SATISFIED(416),

	/** The expectation failed. */
	EXPECTATION_FAILED(417),

	/** The unprocessable entity. */
	UNPROCESSABLE_ENTITY(422),

	/** The locked. */
	LOCKED(423),

	/** The failed dependency. */
	FAILED_DEPENDENCY(424),

	/** The internal server error. */
	INTERNAL_SERVER_ERROR(500),

	/** The not implemented. */
	NOT_IMPLEMENTED(501),

	/** The bad gateway. */
	BAD_GATEWAY(502),

	/** The service unavailable. */
	SERVICE_UNAVAILABLE(503),

	/** The gateway timeout. */
	GATEWAY_TIMEOUT(504),

	/** The http version not supported. */
	HTTP_VERSION_NOT_SUPPORTED(505),

	/** The insufficient storage. */
	INSUFFICIENT_STORAGE(506);

	/** The status. */
	private int status;

	/**
	 * Instantiates a new rest status.
	 *
	 * @param status the status
	 */
	RestStatus(int status) {
		this.status = (short) status;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Read from.
	 *
	 * @param in the in
	 * @return the rest status
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static RestStatus readFrom(StreamInput in) throws IOException {
		return RestStatus.valueOf(in.readUTF());
	}

	/**
	 * Write to.
	 *
	 * @param out the out
	 * @param status the status
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeTo(StreamOutput out, RestStatus status) throws IOException {
		out.writeUTF(status.name());
	}
}