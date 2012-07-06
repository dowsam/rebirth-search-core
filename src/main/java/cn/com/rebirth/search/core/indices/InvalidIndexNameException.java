/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InvalidIndexNameException.java 2012-3-29 15:02:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexException;


/**
 * The Class InvalidIndexNameException.
 *
 * @author l.xue.nong
 */
public class InvalidIndexNameException extends IndexException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5144901015685215806L;

	
	/**
	 * Instantiates a new invalid index name exception.
	 *
	 * @param index the index
	 * @param name the name
	 * @param desc the desc
	 */
	public InvalidIndexNameException(Index index, String name, String desc) {
		super(index, "Invalid index name [" + name + "], " + desc);
	}

}