/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ResponseHandlerFailureTransportException.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

/**
 * The Class ResponseHandlerFailureTransportException.
 *
 * @author l.xue.nong
 */
public class ResponseHandlerFailureTransportException extends TransportException {

	/**
	 * Instantiates a new response handler failure transport exception.
	 *
	 * @param cause the cause
	 */
	public ResponseHandlerFailureTransportException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#fillInStackTrace()
	 */
	@Override
	public Throwable fillInStackTrace() {
		return null;
	}
}
