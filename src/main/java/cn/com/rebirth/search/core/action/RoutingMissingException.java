/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RoutingMissingException.java 2012-3-29 15:00:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class RoutingMissingException.
 *
 * @author l.xue.nong
 */
public class RoutingMissingException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4299852296910579297L;

	
	/** The index. */
	private final String index;

	
	/** The type. */
	private final String type;

	
	/** The id. */
	private final String id;

	
	/**
	 * Instantiates a new routing missing exception.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 */
	public RoutingMissingException(String index, String type, String id) {
		super("routing is required for [" + index + "]/[" + type + "]/[" + id + "]");
		this.index = index;
		this.type = type;
		this.id = id;
	}

	
	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return index;
	}

	
	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return type;
	}

	
	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		return id;
	}
}
