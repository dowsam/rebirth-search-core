/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportResponseOptions.java 2012-7-6 14:28:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

/**
 * The Class TransportResponseOptions.
 *
 * @author l.xue.nong
 */
public class TransportResponseOptions {

	/** The Constant EMPTY. */
	public static final TransportResponseOptions EMPTY = options();

	/**
	 * Options.
	 *
	 * @return the transport response options
	 */
	public static TransportResponseOptions options() {
		return new TransportResponseOptions();
	}

	/** The compress. */
	private boolean compress;

	/**
	 * With compress.
	 *
	 * @param compress the compress
	 * @return the transport response options
	 */
	public TransportResponseOptions withCompress(boolean compress) {
		this.compress = compress;
		return this;
	}

	/**
	 * Compress.
	 *
	 * @return true, if successful
	 */
	public boolean compress() {
		return this.compress;
	}
}
