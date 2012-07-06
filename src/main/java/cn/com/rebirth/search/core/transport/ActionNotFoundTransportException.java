/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ActionNotFoundTransportException.java 2012-7-6 14:29:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

/**
 * The Class ActionNotFoundTransportException.
 *
 * @author l.xue.nong
 */
public class ActionNotFoundTransportException extends TransportException {

	/** The action. */
	private final String action;

	/**
	 * Instantiates a new action not found transport exception.
	 *
	 * @param action the action
	 */
	public ActionNotFoundTransportException(String action) {
		super("No handler for action [" + action + "]");
		this.action = action;
	}

	/**
	 * Action.
	 *
	 * @return the string
	 */
	public String action() {
		return this.action;
	}
}
