/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportRequestOptions.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.unit.TimeValue;

/**
 * The Class TransportRequestOptions.
 *
 * @author l.xue.nong
 */
public class TransportRequestOptions {

	/** The Constant EMPTY. */
	public static final TransportRequestOptions EMPTY = options();

	/**
	 * Options.
	 *
	 * @return the transport request options
	 */
	public static TransportRequestOptions options() {
		return new TransportRequestOptions();
	}

	/**
	 * The Enum Type.
	 *
	 * @author l.xue.nong
	 */
	public static enum Type {

		/** The low. */
		LOW,

		/** The med. */
		MED,

		/** The high. */
		HIGH
	}

	/** The timeout. */
	private TimeValue timeout;

	/** The compress. */
	private boolean compress;

	/** The type. */
	private Type type = Type.MED;

	/**
	 * With timeout.
	 *
	 * @param timeout the timeout
	 * @return the transport request options
	 */
	public TransportRequestOptions withTimeout(long timeout) {
		return withTimeout(TimeValue.timeValueMillis(timeout));
	}

	/**
	 * With timeout.
	 *
	 * @param timeout the timeout
	 * @return the transport request options
	 */
	public TransportRequestOptions withTimeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * With compress.
	 *
	 * @param compress the compress
	 * @return the transport request options
	 */
	public TransportRequestOptions withCompress(boolean compress) {
		this.compress = compress;
		return this;
	}

	/**
	 * With type.
	 *
	 * @param type the type
	 * @return the transport request options
	 */
	public TransportRequestOptions withType(Type type) {
		this.type = type;
		return this;
	}

	/**
	 * With high type.
	 *
	 * @return the transport request options
	 */
	public TransportRequestOptions withHighType() {
		this.type = Type.HIGH;
		return this;
	}

	/**
	 * With med type.
	 *
	 * @return the transport request options
	 */
	public TransportRequestOptions withMedType() {
		this.type = Type.MED;
		return this;
	}

	/**
	 * With low type.
	 *
	 * @return the transport request options
	 */
	public TransportRequestOptions withLowType() {
		this.type = Type.LOW;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @return the time value
	 */
	public TimeValue timeout() {
		return this.timeout;
	}

	/**
	 * Compress.
	 *
	 * @return true, if successful
	 */
	public boolean compress() {
		return this.compress;
	}

	/**
	 * Type.
	 *
	 * @return the type
	 */
	public Type type() {
		return this.type;
	}
}
