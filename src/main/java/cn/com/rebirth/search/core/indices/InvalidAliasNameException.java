/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InvalidAliasNameException.java 2012-3-29 15:02:15 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexException;


/**
 * The Class InvalidAliasNameException.
 *
 * @author l.xue.nong
 */
public class InvalidAliasNameException extends IndexException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8973453270831556236L;

	
	/**
	 * Instantiates a new invalid alias name exception.
	 *
	 * @param index the index
	 * @param name the name
	 * @param desc the desc
	 */
	public InvalidAliasNameException(Index index, String name, String desc) {
		super(index, "Invalid alias name [" + name + "], " + desc);
	}

}