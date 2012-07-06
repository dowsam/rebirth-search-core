/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ActionTransportException.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.search.commons.transport.TransportAddress;


/**
 * The Class ActionTransportException.
 *
 * @author l.xue.nong
 */
public class ActionTransportException extends TransportException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2984862862556114256L;

	
	/** The address. */
	private TransportAddress address;

	
	/** The action. */
	private String action;

	
	/**
	 * Instantiates a new action transport exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public ActionTransportException(String msg, Throwable cause) {
		super(msg, cause);
	}

	
	/**
	 * Instantiates a new action transport exception.
	 *
	 * @param name the name
	 * @param address the address
	 * @param action the action
	 * @param cause the cause
	 */
	public ActionTransportException(String name, TransportAddress address, String action, Throwable cause) {
		super(buildMessage(name, address, action, null), cause);
		this.address = address;
		this.action = action;
	}

	
	/**
	 * Instantiates a new action transport exception.
	 *
	 * @param name the name
	 * @param address the address
	 * @param action the action
	 * @param msg the msg
	 * @param cause the cause
	 */
	public ActionTransportException(String name, TransportAddress address, String action, String msg, Throwable cause) {
		super(buildMessage(name, address, action, msg), cause);
		this.address = address;
		this.action = action;
	}

	
	/**
	 * Address.
	 *
	 * @return the transport address
	 */
	public TransportAddress address() {
		return address;
	}

	
	/**
	 * Action.
	 *
	 * @return the string
	 */
	public String action() {
		return action;
	}

	
	/**
	 * Builds the message.
	 *
	 * @param name the name
	 * @param address the address
	 * @param action the action
	 * @param msg the msg
	 * @return the string
	 */
	private static String buildMessage(String name, TransportAddress address, String action, String msg) {
		StringBuilder sb = new StringBuilder();
		if (name != null) {
			sb.append('[').append(name).append(']');
		}
		if (address != null) {
			sb.append('[').append(address).append(']');
		}
		if (action != null) {
			sb.append('[').append(action).append(']');
		}
		if (msg != null) {
			sb.append(" ").append(msg);
		}
		return sb.toString();
	}
}
