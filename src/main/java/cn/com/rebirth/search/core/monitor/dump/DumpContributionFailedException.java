/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DumpContributionFailedException.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.dump;

/**
 * The Class DumpContributionFailedException.
 *
 * @author l.xue.nong
 */
public class DumpContributionFailedException extends DumpException {

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new dump contribution failed exception.
	 *
	 * @param name the name
	 * @param msg the msg
	 */
	public DumpContributionFailedException(String name, String msg) {
		this(name, msg, null);
	}

	/**
	 * Instantiates a new dump contribution failed exception.
	 *
	 * @param name the name
	 * @param msg the msg
	 * @param cause the cause
	 */
	public DumpContributionFailedException(String name, String msg, Throwable cause) {
		super(name + ": " + msg, cause);
		this.name = name;
	}

	/**
	 * Name.
	 *
	 * @return the string
	 */
	public String name() {
		return this.name;
	}
}
