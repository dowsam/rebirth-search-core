/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexTemplateMissingException.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class IndexTemplateMissingException.
 *
 * @author l.xue.nong
 */
public class IndexTemplateMissingException extends RebirthException {

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
