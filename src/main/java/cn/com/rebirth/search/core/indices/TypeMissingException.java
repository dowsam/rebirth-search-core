/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TypeMissingException.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexException;
import cn.com.rebirth.search.core.rest.RestStatus;

/**
 * The Class TypeMissingException.
 *
 * @author l.xue.nong
 */
public class TypeMissingException extends IndexException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6726532518392687510L;

	/**
	 * Instantiates a new type missing exception.
	 *
	 * @param index the index
	 * @param type the type
	 */
	public TypeMissingException(Index index, String type) {
		super(index, "type[" + type + "] missing");
	}

	/**
	 * Instantiates a new type missing exception.
	 *
	 * @param index the index
	 * @param type the type
	 * @param message the message
	 */
	public TypeMissingException(Index index, String type, String message) {
		super(index, "type[" + type + "] missing: " + message);
	}

}
