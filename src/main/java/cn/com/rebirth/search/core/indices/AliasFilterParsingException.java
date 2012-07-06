/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AliasFilterParsingException.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexException;


/**
 * The Class AliasFilterParsingException.
 *
 * @author l.xue.nong
 */
public class AliasFilterParsingException extends IndexException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3184923614780342337L;

	
	/**
	 * Instantiates a new alias filter parsing exception.
	 *
	 * @param index the index
	 * @param name the name
	 * @param desc the desc
	 */
	public AliasFilterParsingException(Index index, String name, String desc) {
		super(index, "[" + name + "], " + desc);
	}

	
	/**
	 * Instantiates a new alias filter parsing exception.
	 *
	 * @param index the index
	 * @param name the name
	 * @param desc the desc
	 * @param ex the ex
	 */
	public AliasFilterParsingException(Index index, String name, String desc, Throwable ex) {
		super(index, "[" + name + "], " + desc, ex);
	}

}