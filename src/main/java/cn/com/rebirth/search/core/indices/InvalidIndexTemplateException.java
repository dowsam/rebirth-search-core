/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InvalidIndexTemplateException.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class InvalidIndexTemplateException.
 *
 * @author l.xue.nong
 */
public class InvalidIndexTemplateException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3831258589976066298L;

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new invalid index template exception.
	 *
	 * @param name the name
	 * @param msg the msg
	 */
	public InvalidIndexTemplateException(String name, String msg) {
		super("index_template [" + name + "] invalid, cause [" + msg + "]");
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
