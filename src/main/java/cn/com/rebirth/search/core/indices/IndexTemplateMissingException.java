/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexTemplateMissingException.java 2012-3-29 15:01:43 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.commons.exception.RestartException;


/**
 * The Class IndexTemplateMissingException.
 *
 * @author l.xue.nong
 */
public class IndexTemplateMissingException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -9032594151745257628L;

	
	/** The name. */
	private final String name;

	
	/**
	 * Instantiates a new index template missing exception.
	 *
	 * @param name the name
	 */
	public IndexTemplateMissingException(String name) {
		super("index_template [" + name + "] missing");
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
