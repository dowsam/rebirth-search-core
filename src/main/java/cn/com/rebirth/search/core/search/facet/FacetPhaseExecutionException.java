/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FacetPhaseExecutionException.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet;

import cn.com.rebirth.commons.exception.RestartException;



/**
 * The Class FacetPhaseExecutionException.
 *
 * @author l.xue.nong
 */
public class FacetPhaseExecutionException extends RestartException {

	
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