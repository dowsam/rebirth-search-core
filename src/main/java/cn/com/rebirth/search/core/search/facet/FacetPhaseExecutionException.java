/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FacetPhaseExecutionException.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class FacetPhaseExecutionException.
 *
 * @author l.xue.nong
 */
public class FacetPhaseExecutionException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -976905987152946672L;

	/**
	 * Instantiates a new facet phase execution exception.
	 *
	 * @param facetName the facet name
	 * @param msg the msg
	 */
	public FacetPhaseExecutionException(String facetName, String msg) {
		super("Facet [" + facetName + "]: " + msg);
	}

	/**
	 * Instantiates a new facet phase execution exception.
	 *
	 * @param facetName the facet name
	 * @param msg the msg
	 * @param t the t
	 */
	public FacetPhaseExecutionException(String facetName, String msg, Throwable t) {
		super("Facet [" + facetName + "]: " + msg, t);
	}
}