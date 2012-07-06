/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexTemplateAlreadyExistsException.java 2012-7-6 14:29:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class IndexTemplateAlreadyExistsException.
 *
 * @author l.xue.nong
 */
public class IndexTemplateAlreadyExistsException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6975685123006995080L;

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new index template already exists exception.
	 *
	 * @param name the name
	 */
	public IndexTemplateAlreadyExistsException(String name) {
		super("index_template [" + name + "] already exists");
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
