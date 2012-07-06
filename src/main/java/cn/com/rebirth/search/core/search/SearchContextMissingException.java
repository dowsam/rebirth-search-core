/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchContextMissingException.java 2012-3-29 15:02:42 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class SearchContextMissingException.
 *
 * @author l.xue.nong
 */
public class SearchContextMissingException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2918318842488332314L;

	
	/** The id. */
	private final long id;

	
	/**
	 * Instantiates a new search context missing exception.
	 *
	 * @param id the id
	 */
	public SearchContextMissingException(long id) {
		super("No search context found for id [" + id + "]");
		this.id = id;
	}

	
	/**
	 * Id.
	 *
	 * @return the long
	 */
	public long id() {
		return this.id;
	}
}
