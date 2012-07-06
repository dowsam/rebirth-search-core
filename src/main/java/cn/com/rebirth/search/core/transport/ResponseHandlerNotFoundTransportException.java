/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ResponseHandlerNotFoundTransportException.java 2012-7-6 14:28:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

/**
 * The Class ResponseHandlerNotFoundTransportException.
 *
 * @author l.xue.nong
 */
public class ResponseHandlerNotFoundTransportException extends TransportException {

	/** The request id. */
	private final long requestId;

	/**
	 * Instantiates a new response handler not found transport exception.
	 *
	 * @param requestId the request id
	 */
	public ResponseHandlerNotFoundTransportException(long requestId) {
		super("Transport response handler not found of id [" + requestId + "]");
		this.requestId = requestId;
	}

	/**
	 * Request id.
	 *
	 * @return the long
	 */
	public long requestId() {
		return requestId;
	}
}
