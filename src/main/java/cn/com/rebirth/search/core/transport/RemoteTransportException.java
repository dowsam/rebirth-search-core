/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RemoteTransportException.java 2012-7-6 14:30:24 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.exception.RebirthWrapperException;
import cn.com.rebirth.search.commons.transport.TransportAddress;

/**
 * The Class RemoteTransportException.
 *
 * @author l.xue.nong
 */
public class RemoteTransportException extends ActionTransportException implements RebirthWrapperException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4019103061587911348L;

	/**
	 * Instantiates a new remote transport exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public RemoteTransportException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Instantiates a new remote transport exception.
	 *
	 * @param name the name
	 * @param address the address
	 * @param action the action
	 * @param cause the cause
	 */
	public RemoteTransportException(String name, TransportAddress address, String action, Throwable cause) {
		super(name, address, action, cause);
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#fillInStackTrace()
	 */
	@Override
	public Throwable fillInStackTrace() {

		return null;
	}
}
