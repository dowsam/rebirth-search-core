/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NotSerializableTransportException.java 2012-7-6 14:30:01 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

/**
 * The Class NotSerializableTransportException.
 *
 * @author l.xue.nong
 */
public class NotSerializableTransportException extends TransportException {

	/**
	 * Instantiates a new not serializable transport exception.
	 *
	 * @param t the t
	 */
	public NotSerializableTransportException(Throwable t) {
		super(buildMessage(t));
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#fillInStackTrace()
	 */
	@Override
	public Throwable fillInStackTrace() {
		return null;
	}

	/**
	 * Builds the message.
	 *
	 * @param t the t
	 * @return the string
	 */
	private static String buildMessage(Throwable t) {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(t.getClass().getName()).append("] ");
		while (t != null) {
			sb.append(t.getMessage()).append("; ");
			t = t.getCause();
		}
		return sb.toString();
	}
}
