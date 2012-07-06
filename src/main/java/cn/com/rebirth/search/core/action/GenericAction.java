/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GenericAction.java 2012-7-6 14:30:15 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

import cn.com.rebirth.search.core.transport.TransportRequestOptions;

/**
 * The Class GenericAction.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public abstract class GenericAction<Request extends ActionRequest, Response extends ActionResponse> {

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new generic action.
	 *
	 * @param name the name
	 */
	protected GenericAction(String name) {
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

	/**
	 * New response.
	 *
	 * @return the response
	 */
	public abstract Response newResponse();

	/**
	 * Options.
	 *
	 * @return the transport request options
	 */
	public TransportRequestOptions options() {
		return TransportRequestOptions.EMPTY;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return name.equals(((GenericAction) o).name());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
